package org.safehaus.embedded.jetty.utils;


import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.util.ssl.SslContextFactory;

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


    public static ServerConnector setConnectors( String subClass, ClassLoader cl, Server server ) {
        Class<? extends JettyRunner> launcherClass = null;

        try {
            //noinspection unchecked
            launcherClass = ( Class<? extends JettyRunner> ) Class.forName( subClass );
        }
        catch ( ClassNotFoundException e ) {
            throw new RuntimeException( e );
        }

        if ( ! launcherClass.isAnnotationPresent( JettyConnectors.class ) ) {
            LOG.warn( "No connector configuration defined for launchers. " +
                    "Defaulting to an HTTP connector based on an available port." );

            ServerConnector connector = new ServerConnector( server );
            connector.setPort( 0 );
            server.setConnectors( new ServerConnector[] { connector } );
            return connector;
        }

        JettyConnectors connectorsAnnotation = launcherClass.getAnnotation( JettyConnectors.class );
        return setConnectors( new ArrayList<ServerConnector>(), cl, connectorsAnnotation, server );
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
