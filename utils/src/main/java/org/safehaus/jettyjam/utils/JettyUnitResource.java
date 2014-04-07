package org.safehaus.jettyjam.utils;


import java.net.URL;
import java.util.Map;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/** A Jetty ExternalResource for this project. */
public class JettyUnitResource implements JettyResource {
    private static final Logger LOG = LoggerFactory.getLogger( JettyUnitResource.class );
    private final Server server = new Server();
    private ServerConnector defaultConnector;
    private URL serverUrl;
    private int port;
    private boolean secure;
    private boolean started;
    private String hostname;


    @SuppressWarnings( "UnusedDeclaration" )
    public Server getServer() {
        return server;
    }


    @Override
    public int getPort() {
        return port;
    }


    @Override
    public URL getServerUrl() {
        return serverUrl;
    }


    @Override
    @SuppressWarnings( "UnusedDeclaration" )
    public boolean isStarted() {
        return started;
    }


    public TestMode getMode() {
        return TestMode.UNIT;
    }


    /*
     * The URL should be the the base of the server. There might however also
     * be a set of URLs for mappings? Mappings to one or more applications
     * specified by annotations on the JettyUnitResource? Maybe we can automatically
     * also generate a client that will hit the resource?
     */
    @Override
    public void before() throws Exception {
        // Fire up the servlet with the handler
        server.start();

        this.port = defaultConnector.getLocalPort();
        this.hostname = defaultConnector.getHost();

        if ( this.hostname == null ) {
            this.hostname = "localhost";
        }

        String protocol = "http";
        secure = false;
        if ( defaultConnector.getDefaultProtocol().contains( "SSL" ) ) {
            protocol = "https";
            secure = true;
            // CertUtils.preparations( hostname, port );
        }

        this.serverUrl = new URL( protocol, "localhost", port, "" );
        this.started = true;
    }


    @Override
    public void after() {
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


    @Override
    public String getHostname() {
        return hostname;
    }


    @Override
    public boolean isSecure() {
        return secure;
    }


    @Override
    public TestParams newTestParams() {
        if ( ! started ) {
            throw new IllegalStateException( "This JettyUnitResource not started." );
        }

        return new TestParams( this );
    }


    @Override
    public TestParams newTestParams( final Map<String, String> queryParams ) {
        if ( ! started ) {
            throw new IllegalStateException( "This JettyUnitResource not started." );
        }

        return new TestParams( this, queryParams );
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append( getClass().getSimpleName() ).append( ":\n{" );
        sb.append( "\n\tmode = " ).append( getMode() );
        sb.append( "\n\thostname = " ).append( getHostname() );
        sb.append( "\n\tport" ).append( getPort() );
        sb.append( "\n\tserverUrl" ).append( getServerUrl() );
        sb.append( "\n\tsecure" ).append( isSecure() );
        sb.append( "\n\tstarted" ).append( isStarted() );
        sb.append( "\n}" );

        return sb.toString();
    }
}
