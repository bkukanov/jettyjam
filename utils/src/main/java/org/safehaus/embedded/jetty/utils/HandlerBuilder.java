package org.safehaus.embedded.jetty.utils;


import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.servlet.ServletHandler;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Builds Jetty JettyHandlers based on annotations.
 */
public class HandlerBuilder {
    private static final Logger LOG = LoggerFactory.getLogger( HandlerBuilder.class );


    /**
     * Builds a collection of handlers by scanning from a package base for annotated Jetty
     * web components.
     *
     * @param packageBase the package base to start scanning from
     * @return the handler collection
     */
    public Handler buildForLauncher( String packageBase ) {
        HandlerCollection collection = new HandlerCollection();

        Reflections reflections = new Reflections( packageBase );
        Set<Class<? extends Launcher>> classes = reflections.getSubTypesOf( Launcher.class );
        Set<Class<? extends Launcher>> matching = new HashSet<Class<? extends Launcher>>();

        for ( Class<? extends Launcher> launcherClass : classes ) {
            if ( launcherClass.isAnnotationPresent( JettyHandlers.class ) ) {
                matching.add( launcherClass );
            }
        }

        if ( matching.size() > 1 ) {
            StringBuilder sb = new StringBuilder();
            sb.append( "Cannot have more than one Launcher annotated with @JettyHandlers\n" );

            for ( Class<? extends Launcher> laucherClass : matching ) {
                sb.append( "\t ==> " ).append( laucherClass.getName() ).append( "\n" );
            }

            throw new RuntimeException( sb.toString() );
        }

        if ( matching.size() == 0 ) {
            throw new RuntimeException( "Could not find a Launcher with @JettyHandlers annotation." );
        }

        Class<? extends Launcher> launcherClass = matching.iterator().next();
        JettyHandlers handlers = launcherClass.getAnnotation( JettyHandlers.class );

        for ( ServletMapping mapping : handlers.servletMappings() ) {
            ServletHandler handler = new ServletHandler();
            handler.addServletWithMapping( mapping.servlet(), mapping.spec() );
            collection.addHandler( handler );
        }

        for ( FilterMapping mapping : handlers.filterMappings() ) {
            ServletHandler handler = new ServletHandler();
            handler.addFilterWithMapping( mapping.filter(), mapping.spec(), null );
            collection.addHandler( handler );
        }

        collection.addHandler( new DefaultHandler() );
        return collection;
    }


    /**
     * Builds a collection of handlers by scanning a test class for an annotated
     * JettyResource field.
     *
     * @param testClass the test class to scan
     * @return the collection of handlers
     */
    public Handler build( Class testClass ) {
        HandlerCollection collection = new HandlerCollection();

        // Check to make sure we have a JettyResource field
        Field jettyResource = getJettyResource( testClass );
        if ( jettyResource == null ) {
            LOG.warn( "There's no JettyResource rule on class {}", testClass );
            return collection;
        }

        // Check to see that we have a JettyHandlers annotation on JettyResource field
        JettyHandlers handlersAnnotation = jettyResource.getAnnotation( JettyHandlers.class );
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

        collection.addHandler( new DefaultHandler() );
        return collection;
    }


    Field getJettyResource( Class testClass ) {
        for ( Field field : testClass.getDeclaredFields() ) {
            LOG.debug( "Looking at {} field of {} test class", field.getName(), testClass.getName() );

            if ( JettyResource.class.isAssignableFrom( field.getType() ) ) {
                LOG.debug( "Found JettyResource for {} field of {} test class", field.getName(), testClass.getName() );

                return field;
            }
        }

        return null;
    }
}
