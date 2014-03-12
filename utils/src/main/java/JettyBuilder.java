package org.safehaus.embedded.jetty.simple;


import java.lang.reflect.Field;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.servlet.ServletHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Builds Jetty JettyHandlers based on annotations.
 */
public class JettyBuilder {
    private static final Logger LOG = LoggerFactory.getLogger( JettyBuilder.class );


    public Handler build( Class testClass ) {
        HandlerCollection collection = new HandlerCollection();
        collection.addHandler( new DefaultHandler() );

        // Check to make sure we have a JettyResource field
        Field jettResource = getJettyResource( testClass );
        if ( jettResource == null ) {
            LOG.warn( "There's no JettyResource rule on class {}", testClass );
            return collection;
        }

        // Check to see that we have a JettyHandlers annotation on JettyResource field
        JettyHandlers handlersAnnotation = jettResource.getAnnotation( JettyHandlers.class );
        if ( handlersAnnotation == null ) {
            LOG.warn( "There's no JettyHandlers annotation on JettyResource field of testClass {}", testClass );
            return collection;
        }

        for ( ServletMapping mapping : handlersAnnotation.servletMappings() ) {
            ServletHandler handler = new ServletHandler();
            handler.addServletWithMapping( mapping.servlet(), mapping.spec() );
            collection.addHandler( handler );
        }

        for ( FilterMapping mapping : handlersAnnotation.filterMappings() ) {
            ServletHandler handler = new ServletHandler();
            handler.addFilterWithMapping( mapping.filter(), mapping.spec(), null );
            collection.addHandler( handler );
        }

        return collection;
    }


    Field getJettyResource( Class testClass ) {
        for ( Field field : testClass.getDeclaredFields() ) {
            if ( field.getClass().isAssignableFrom( JettyResource.class ) ) {
                return field;
            }
        }

        return null;
    }
}
