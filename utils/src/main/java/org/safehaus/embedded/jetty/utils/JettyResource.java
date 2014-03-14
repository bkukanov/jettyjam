package org.safehaus.embedded.jetty.utils;


import java.net.URL;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/** A Jetty ExternalResource for this project. */
public class JettyResource implements TestRule {
    private static final Logger LOG = LoggerFactory.getLogger( JettyResource.class );
    private final Server server = new Server();
    private ServerConnector defaultConnector;
    private URL serverUrl;
    private int port;
    private boolean started;
    private String hostname;


    @SuppressWarnings( "UnusedDeclaration" )
    public Server getServer() {
        return server;
    }


    public int getPort() {
        return port;
    }


    public URL getServerUrl() {
        return serverUrl;
    }


    @SuppressWarnings( "UnusedDeclaration" )
    public boolean isStarted() {
        return started;
    }


    /*
     * The URL should be the the base of the server. There might however also
     * be a set of URLs for mappings? Mappings to one or more applications
     * specified by annotations on the JettyResource? Maybe we can automatically
     * also generate a client that will hit the resource?
     */
    protected void before() throws Throwable {
        // Fire up the servlet with the handler
        server.start();

        this.port = defaultConnector.getLocalPort();
        this.hostname = defaultConnector.getHost();

        if ( this.hostname == null ) {
            this.hostname = "localhost";
        }

        String protocol = "http";
        if ( defaultConnector.getDefaultProtocol().contains( "SSL" ) ) {
            protocol = "https";
        }

        this.serverUrl = new URL( protocol, "localhost", port, "" );
        this.started = true;
    }


    protected void after() {
        try {
            server.stop();
        }
        catch ( Exception e ) {
            LOG.error( "Failed to stop the server.", e );
        }
    }


    @Override
    public Statement apply( Statement base, Description description ) {
        defaultConnector = ConnectorBuilder.setConnectors( description.getTestClass(), server );
        HandlerBuilder builder = new HandlerBuilder();
        server.setHandler( builder.build( description.getTestClass(), server ) );
        return statement( base );
    }


    private Statement statement( final Statement base ) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                before();
                try {
                    base.evaluate();
                }
                finally {
                    after();
                }
            }
        };
    }


    public String getHostname() {
        return hostname;
    }
}
