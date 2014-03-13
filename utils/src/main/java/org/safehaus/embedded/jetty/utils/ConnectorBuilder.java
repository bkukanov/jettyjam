package org.safehaus.embedded.jetty.utils;


import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Builds Jetty connectors from configuration annotations.
 */
public class ConnectorBuilder {
    public static final Logger LOG = LoggerFactory.getLogger( ConnectorBuilder.class );


    static Field getJettyResource( Class testClass ) {
        for ( Field field : testClass.getDeclaredFields() ) {
            LOG.debug( "Looking at {} field of {} test class", field.getName(), testClass.getName() );

            if ( JettyResource.class.isAssignableFrom( field.getType() ) ) {
                LOG.debug( "Found JettyResource for {} field of {} test class", field.getName(), testClass.getName() );

                return field;
            }
        }

        return null;
    }


    public static ServerConnector setConnectors( Class testClass, Server server ) {
        Field jettyResource = getJettyResource( testClass );
        if ( jettyResource == null ) {
            LOG.warn( "There's no JettyResource rule on class {} - using a default http connection.", testClass );
            ServerConnector connector = new ServerConnector( server );
            connector.setPort( 0 );
            server.setConnectors( new ServerConnector[] { connector } );
            return connector;
        }

        JettyConnectors connectorsAnnotation = jettyResource.getAnnotation( JettyConnectors.class );
        if ( connectorsAnnotation == null ) {
            LOG.warn( "There's no JettyConnectors annotation on JettyResource field of testClass {}" +
                    " - using default http connection", testClass );
            ServerConnector connector = new ServerConnector( server );
            connector.setPort( 0 );
            server.setConnectors( new ServerConnector[] { connector } );
            return connector;
        }

        return setConnectors( new ArrayList<ServerConnector>(), testClass.getClassLoader(),
                connectorsAnnotation, server );
    }


    public static ServerConnector setConnectors( String packageBase, ClassLoader cl, Server server ) {
        List<ServerConnector> connectors = new ArrayList<ServerConnector>();

        Reflections reflections = new Reflections( packageBase );
        Set<Class<? extends Launcher>> classes = reflections.getSubTypesOf( Launcher.class );
        Set<Class<? extends Launcher>> matching = new HashSet<Class<? extends Launcher>>();

        for ( Class<? extends Launcher> launcherClass : classes ) {
            if ( launcherClass.isAnnotationPresent( JettyConnectors.class ) ) {
                matching.add( launcherClass );
            }
        }

        if ( matching.size() > 1 ) {
            StringBuilder sb = new StringBuilder();
            sb.append( "Cannot have more than one Launcher annotated with @JettyConnectors\n" );

            for ( Class<? extends Launcher> laucherClass : matching ) {
                sb.append( "\t ==> " ).append( laucherClass.getName() ).append( "\n" );
            }

            throw new RuntimeException( sb.toString() );
        }

        if ( matching.size() == 0 ) {
            LOG.warn( "No connector configuration defined for launchers. " +
                    "Defaulting to an HTTP connector based on an available port." );

            ServerConnector connector = new ServerConnector( server );
            connector.setPort( 0 );
            server.setConnectors( new ServerConnector[] { connector } );
            return connector;
        }

        Class<? extends Launcher> launcherClass = matching.iterator().next();
        JettyConnectors connectorsAnnotation = launcherClass.getAnnotation( JettyConnectors.class );
        return setConnectors( connectors, cl, connectorsAnnotation, server );
    }


    static ServerConnector setConnectors( List<ServerConnector> connectors, ClassLoader cl,
                                                 JettyConnectors connectorsAnnotation, Server server ) {
        ServerConnector defaultConnector = null;

        for ( HttpConnector connectorAnnotation : connectorsAnnotation.httpConnectors() ) {
            ServerConnector connector = new ServerConnector( server );
            connector.setPort( connectorAnnotation.port() );
            connectors.add( connector );

            if ( connectorAnnotation.id().equalsIgnoreCase( connectorsAnnotation.defaultId() ) ) {
                defaultConnector = connector;
            }
        }

        for ( HttpsConnector connectorAnnotation : connectorsAnnotation.httpsConnectors() ) {
            HttpConfiguration https = new HttpConfiguration();
            https.addCustomizer( new SecureRequestCustomizer() );

            SslContextFactory sslContextFactory = new SslContextFactory();

            URL ksPathUrl = cl.getResource( connectorAnnotation.keyStore() );
            String ksPath = ksPathUrl.toExternalForm();

            sslContextFactory.setKeyStorePath( ksPath );
            sslContextFactory.setKeyStorePassword( "123456" );
            sslContextFactory.setKeyManagerPassword( "123456" );

            ServerConnector sslConnector = new ServerConnector( server,
                    new SslConnectionFactory( sslContextFactory, "http/1.1" ),
                    new HttpConnectionFactory( https ) );
            sslConnector.setPort( connectorAnnotation.port() );
            connectors.add( sslConnector );

            if ( connectorAnnotation.id().equalsIgnoreCase( connectorsAnnotation.defaultId() ) ) {
                defaultConnector = sslConnector;
            }
        }

        // add a default http based connector if none has been defined
        if ( connectors.size() == 0 ) {
            ServerConnector connector = new ServerConnector( server );
            connector.setPort( 0 );
            connectors.add( connector );
            defaultConnector = connector;
        }

        ServerConnector[] connectorsArray = new ServerConnector[connectors.size()];
        server.setConnectors( connectors.toArray( connectorsArray ) );

        if ( defaultConnector == null ) {
            throw new RuntimeException( "The default connector id " + connectorsAnnotation.defaultId()
                    + " did not match a connector." );
        }

        return defaultConnector;
    }
}
