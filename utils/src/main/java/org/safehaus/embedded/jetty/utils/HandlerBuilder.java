package org.safehaus.embedded.jetty.utils;


import java.lang.reflect.Field;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.DispatcherType;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;

import org.reflections.Reflections;
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
     * @param packageBase the package base to start scanning from
     * @return the handler collection
     */
    public ServletContextHandler buildForLauncher( String packageBase, Server server ) {
        Reflections reflections = new Reflections( packageBase );
        Set<Class<? extends Launcher>> classes = reflections.getSubTypesOf( Launcher.class );
        Set<Class<? extends Launcher>> matching = new HashSet<Class<? extends Launcher>>();

        for ( Class<? extends Launcher> launcherClass : classes ) {
            if ( launcherClass.isAnnotationPresent( JettyContext.class ) ) {
                matching.add( launcherClass );
            }
        }

        if ( matching.size() > 1 ) {
            StringBuilder sb = new StringBuilder();
            sb.append( "Cannot have more than one Launcher annotated with @JettyContext\n" );

            for ( Class<? extends Launcher> laucherClass : matching ) {
                sb.append( "\t ==> " ).append( laucherClass.getName() ).append( "\n" );
            }

            throw new RuntimeException( sb.toString() );
        }

        if ( matching.size() == 0 ) {
            throw new RuntimeException( "Could not find a Launcher with @JettyContext annotation." );
        }

        Class<? extends Launcher> launcherClass = matching.iterator().next();
        JettyContext contextAnnotation = launcherClass.getAnnotation( JettyContext.class );
        return build( contextAnnotation, server );
    }


    /**
     * Builds a collection of handlers by scanning a test class for an annotated
     * JettyResource field.
     *
     * @param testClass the test class to scan
     * @return the collection of handlers
     */
    public ServletContextHandler build( Class testClass, Server server) {
        // Check to make sure we have a JettyResource field
        Field jettyResource = getJettyResource( testClass );
        if ( jettyResource == null ) {
            throw new IllegalStateException( "There's no JettyResource rule on class " + testClass );
        }

        // Check to see that we have a JettyContext annotation on JettyResource field
        JettyContext contextAnnotation = jettyResource.getAnnotation( JettyContext.class );
        if ( contextAnnotation == null ) {
            throw new IllegalStateException( "There's no JettyContext annotation on " +
                    "JettyResource field of testClass " + testClass );
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

        return handler;
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
