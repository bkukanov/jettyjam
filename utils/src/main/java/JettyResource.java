package org.safehaus.embedded.jetty.simple;


import java.net.URL;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/** A Jetty ExternalResource for this project. */
public class JettyResource implements TestRule {
    private static final Logger LOG = LoggerFactory.getLogger( JettyResource.class );
    private final Server server = new Server( 0 );
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


    /*
     * The URL should be the the base of the server. There might however also
     * be a set of URLs for mappings? Mappings to one or more applications
     * specified by annotations on the JettyResource? Maybe we can automatically
     * also generate a client that will hit the resource?
     */
    protected void before() throws Throwable {
        // Fire up the servlet with the handler
        server.start();

        ServerConnector connector = ( ServerConnector ) server.getConnectors()[0];
        this.port = connector.getLocalPort();
        this.serverUrl = new URL( "http", "localhost", port, "" );
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
        JettyBuilder builder = new JettyBuilder();
        server.setHandler( builder.build( description.getTestClass() ) );
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
}
