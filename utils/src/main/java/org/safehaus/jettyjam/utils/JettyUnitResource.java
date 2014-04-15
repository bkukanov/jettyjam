package org.safehaus.jettyjam.utils;


import java.net.URL;
import java.util.List;

import javax.servlet.ServletContextListener;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.junit.runner.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/** A Jetty ExternalResource for this project. */
public class JettyUnitResource<CL extends ServletContextListener> extends AbstractJettyResource {
    private static final Logger LOG = LoggerFactory.getLogger( JettyUnitResource.class );

    private final Server server = new Server();

    private String name = "unset";
    private ServerConnector defaultConnector;
    private HandlerBuilder<CL> handlerBuilder = new HandlerBuilder<CL>();


    /**
     * For static members in a test class usually used with @ClassRule annotation.
     */
    public JettyUnitResource( Class testClass ) {
        super( testClass, TestMode.UNIT );
    }


    /**
     * For non-static test class member which requires the test class instance.
     *
     * @param testInstance the test class instance
     */
    public JettyUnitResource( Object testInstance ) {
        super( testInstance, TestMode.UNIT );
    }


    /**
     * For static members in a test class usually used with @ClassRule annotation.
     */
    public JettyUnitResource( Class testClass, String name ) {
        super( testClass, TestMode.UNIT );
        this.name = name;
    }


    /**
     * For non-static test class member which requires the test class instance.
     *
     * @param testInstance the test class instance
     */
    public JettyUnitResource( Object testInstance, String name ) {
        super( testInstance, TestMode.UNIT );
        this.name = name;
    }


    protected void prepare() {
        super.prepare();
        defaultConnector = ConnectorBuilder.setConnectors( getTestField(), server );
        handlerBuilder = new HandlerBuilder<CL>();
        server.setHandler( handlerBuilder.build( testClass, getTestField(), server ) );
    }


    public CL getFirstContextListener() {
        return handlerBuilder.getFirstContextListener();
    }


    public List<CL> getContextListeners() {
        return handlerBuilder.getContextListeners();
    }


    @SuppressWarnings( "UnusedDeclaration" )
    public Server getServer() {
        return server;
    }


    /*
     * The URL should be the the base of the server. There might however also
     * be a set of URLs for mappings? Mappings to one or more applications
     * specified by annotations on the JettyUnitResource? Maybe we can automatically
     * also generate a client that will hit the resource?
     */
    @Override
    public void start( Description description ) throws Exception {
        super.start( description );

        if ( defaultConnector == null ) {
            throw new NullPointerException( "The defaultConnector cannot be null. Check that "
              + "the proper annotations have been applied to the resource to configure Jetty." );
        }

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
    public void stop( Description description ) {
        try {
            server.stop();
            started = false;
        }
        catch ( Exception e ) {
            LOG.error( "Failed to stop the server.", e );
        }
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append( getClass().getSimpleName() ).append( ":\n{" );
        sb.append( "\n\tmode = " ).append( getMode() );
        sb.append( "\n\thostname = " ).append( getHostname() );
        sb.append( "\n\tport = " ).append( getPort() );
        sb.append( "\n\tserverUrl = " ).append( getServerUrl() );
        sb.append( "\n\tsecure = " ).append( isSecure() );
        sb.append( "\n\tstarted = " ).append( isStarted() );
        sb.append( "\n}" );

        return sb.toString();
    }
}
