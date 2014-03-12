package org.safehaus.embedded.jetty.utils;


import java.net.URL;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A Jetty Launcher.
 */
public abstract class Launcher {
    private static final Logger LOG = LoggerFactory.getLogger( Launcher.class );
    private final Server server;
    private URL serverUrl;
    private int port;
    private boolean started;


    public Server getServer() {
        return server;
    }


    public int getPort() {
        return port;
    }


    public URL getServerUrl() {
        return serverUrl;
    }


    public boolean isStarted() {
        return started;
    }


    protected Launcher( int port ) {
        this.server = new Server( port );
        LOG.info( "Launcher created on port {}", port );
    }


    protected void start() throws Exception {
        HandlerBuilder handlerBuilder = new HandlerBuilder();
        server.setHandler( handlerBuilder.buildForLauncher( getPackageBase() ) );
        server.start();

        ServerConnector connector = ( ServerConnector ) server.getConnectors()[0];
        this.port = connector.getLocalPort();
        this.serverUrl = new URL( "http", "localhost", port, "" );

        started = true;
    }


    protected void stop() throws Exception {
        server.stop();
        started = false;
    }


    public abstract String getPackageBase();
}

