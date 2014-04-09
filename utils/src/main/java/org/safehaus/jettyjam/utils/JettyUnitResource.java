package org.safehaus.jettyjam.utils;


import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
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
    private final Object testInstance;
    private final Class testClass;

    private Field testField;
    private ServerConnector defaultConnector;
    private URL serverUrl;
    private int port;
    private boolean secure;
    private boolean started;
    private String hostname;


    /**
     * For static members in a test class usually used with @ClassRule annotation.
     */
    public JettyUnitResource( Class testClass ) {
        this.testInstance = null;
        this.testClass = testClass;
    }


    /**
     * For non-static test class member which requires the test class instance.
     *
     * @param testInstance the test class instance
     */
    public JettyUnitResource( Object testInstance ) {
        if ( testInstance == null ) {
            throw new NullPointerException( "testInstance cannot be null." );
        }

        if ( testInstance instanceof Class ) {
            throw new IllegalStateException( "testInstance should not be a Class" );
        }

        this.testInstance = testInstance;
        this.testClass = testInstance.getClass();
    }


    private void prepare() {
        try {
            findFieldInTest();
        }
        catch ( IllegalAccessException e ) {
            throw new IllegalStateException( "Access modifier must be public." );
        }

        defaultConnector = ConnectorBuilder.setConnectors( testField, server );
        HandlerBuilder builder = new HandlerBuilder();
        server.setHandler( builder.build( testClass, server ) );
    }


    private void findFieldInTest() throws IllegalAccessException {
        for ( Field field : testClass.getDeclaredFields() ) {
            LOG.debug( "Looking at {} field of {} test class", field.getName(), testClass );

            if ( JettyResource.class.isAssignableFrom( field.getType() ) ) {
                LOG.debug( "Found JettyResource for {} field of {} test class", field.getName(), testClass );
                field.setAccessible( true );

                if ( testInstance == null && ! Modifier.isStatic( field.getModifiers() ) ) {
                    throw new IllegalStateException( "A test object instance constructor argument must be provided, " +
                            "for a non-static " + JettyUnitResource.class + " member." );
                }

                if ( testInstance != null && Modifier.isStatic( field.getModifiers() ) ) {
                    throw new IllegalStateException( "The Class constructor argument for the test must be provided, " +
                            "for static " + JettyUnitResource.class + " members." );
                }

                Object obj;
                if ( Modifier.isStatic( field.getModifiers() ) ) {
                    obj = field.get( null );
                }
                else {
                    obj = field.get( testInstance );
                }

                if ( obj == null ) {
                    String msg = "Field " + field.getName() + " in test class " + testClass + " is null.";
                    LOG.error( msg );
                    throw new RuntimeException( msg );
                }

            }
        }
    }


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
    public void start( Description description ) throws Exception {
        prepare();

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
    public Statement apply( final Statement base, final Description description ) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                start( description );
                try {
                    base.evaluate();
                }
                finally {
                    stop( description );
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
        sb.append( "\n\tport = " ).append( getPort() );
        sb.append( "\n\tserverUrl = " ).append( getServerUrl() );
        sb.append( "\n\tsecure = " ).append( isSecure() );
        sb.append( "\n\tstarted = " ).append( isStarted() );
        sb.append( "\n}" );

        return sb.toString();
    }
}
