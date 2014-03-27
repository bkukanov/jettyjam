package org.safehaus.jettyjam.utils;


import java.lang.reflect.Field;
import java.util.EnumSet;

import javax.servlet.DispatcherType;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Builds Jetty JettyContext based on annotations.
 */
public class HandlerBuilder {
    private static final Logger LOG = LoggerFactory.getLogger( HandlerBuilder.class );


    /**
     * Builds a collection of handlers by scanning from a package base for annotated Jetty
     * web components.
     *
     * @param className the name of the class to get annotations from
     * @return the handler collection
     */
    public ServletContextHandler buildForLauncher( String className, Server server ) {
        Class launcherClass = null;

        try {
            launcherClass = Class.forName( className );
        }
        catch ( Exception e ) {
            throw new RuntimeException( e );
        }

        if ( ! launcherClass.isAnnotationPresent( JettyContext.class ) ) {
            throw new RuntimeException( "JettyRunner " + launcherClass + " not annotated with @JettyContext." );
        }


        JettyContext contextAnnotation = ( JettyContext ) launcherClass.getAnnotation( JettyContext.class );
        return build( contextAnnotation, server );
    }


    /**
     * Builds a collection of handlers by scanning a test class for an annotated
     * JettyUnitResource field.
     *
     * @param testClass the test class to scan
     * @return the collection of handlers
     */
    public ServletContextHandler build( Class testClass, Server server) {
        // Check to make sure we have a JettyUnitResource field
        Field jettyResource = getJettyResource( testClass );
        if ( jettyResource == null ) {
            throw new IllegalStateException( "There's no JettyUnitResource rule on class " + testClass );
        }

        // Check to see that we have a JettyContext annotation on JettyUnitResource field
        JettyContext contextAnnotation = jettyResource.getAnnotation( JettyContext.class );
        if ( contextAnnotation == null ) {
            throw new IllegalStateException( "There's no JettyContext annotation on " +
                    "JettyUnitResource field of testClass " + testClass );
        }

        // setup the servlet context
        return build( contextAnnotation, server );
    }


    public ServletContextHandler build( JettyContext contextAnnotation, Server server) {
        ServletContextHandler handler = new ServletContextHandler( server, contextAnnotation.contextRoot() );

        if ( contextAnnotation.servletMappings().length == 0 ) {
            handler.addServlet( DefaultServlet.class, "/" );
        }
        else {
            for ( ServletMapping mapping : contextAnnotation.servletMappings() ) {
                handler.addServlet( mapping.servlet(), mapping.spec() );
            }
        }

        for ( FilterMapping mapping : contextAnnotation.filterMappings() ) {
            handler.addFilter( mapping.filter(), mapping.spec(), EnumSet.allOf( DispatcherType.class ) );
        }

        for ( ContextListener contextListener : contextAnnotation.contextListeners() ) {
            try {
                handler.addEventListener( contextListener.listener().newInstance() );
            }
            catch ( Exception e ) {
                throw new RuntimeException( "Failed to instantiate listener: "
                        + contextListener.listener(), e );
            }
        }

        if ( contextAnnotation.enableSession() )  {
            handler.setSessionHandler( new SessionHandler() );
        }

        return handler;
    }


    Field getJettyResource( Class testClass ) {
        for ( Field field : testClass.getDeclaredFields() ) {
            LOG.debug( "Looking at {} field of {} test class", field.getName(), testClass.getName() );

            if ( JettyResource.class.isAssignableFrom( field.getType() ) ) {
                LOG.debug( "Found JettyUnitResource for {} field of {} test class", field.getName(), testClass.getName() );

                return field;
            }
        }

        return null;
    }
}
