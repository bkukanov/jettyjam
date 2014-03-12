package org.safehaus.embedded.jetty.utils;


import org.eclipse.jetty.server.Server;


/**
 * A Jetty Launcher.
 */
public abstract class Launcher {
    private final Server server;


    protected Launcher( int port ) {
        this.server = new Server( port );
    }


    protected void start() throws Exception {
        HandlerBuilder handlerBuilder = new HandlerBuilder();
        server.setHandler( handlerBuilder.buildForLauncher( getPackageBase() ) );
        server.start();
    }


    protected void stop() throws Exception {
        server.stop();
    }


    public abstract String getPackageBase();
}

