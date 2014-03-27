package org.safehaus.jettyjam.utils;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;


/**
 * Contains the information needed to execute tests.
 */
public class TestParams {
    private static final Logger LOG = LoggerFactory.getLogger( TestParams.class );

    private Integer port;
    private String hostname;
    private String serverUrl;
    private String endpoint;
    private boolean secure;
    private Logger logger;
    private TestMode mode;


    TestParams( JettyResource jettyResource ) {
        setHostname( jettyResource.getHostname() )
                .setPort( jettyResource.getPort() )
                .setSecure( jettyResource.isSecure() )
                .setMode( jettyResource.getMode() )
                .setServerUrl( jettyResource.getServerUrl().toString() ).setLogger( LOG );

        if ( isSecure() ) {
            try {
                CertUtils.preparations( getHostname(), getPort() );
            }
            catch ( Exception e ) {
                e.printStackTrace();
            }
        }
    }


    public String getServerUrl() {
        return serverUrl;
    }


    TestParams setServerUrl( final String serverUrl ) {
        this.serverUrl = serverUrl;
        return this;
    }


    public String getEndpoint() {
        return endpoint;
    }


    public TestParams setEndpoint( final String endpoint ) {
        this.endpoint = endpoint;
        return this;
    }


    public Logger getLogger() {
        return logger;
    }


    public TestParams setLogger( final Logger logger ) {
        this.logger = logger;
        return this;
    }


    public String getHostname() {
        return hostname;
    }


    TestParams setHostname( final String hostname ) {
        this.hostname = hostname;
        return this;
    }


    public Integer getPort() {
        return port;
    }


    TestParams setPort( final Integer port ) {
        this.port = port;
        return this;
    }


    public boolean isSecure() {
        return secure;
    }


    TestParams setSecure( final boolean secure ) {
        this.secure = secure;
        return this;
    }


    /**
     * Creates a new WebResource to operate against the JettyUnitResource that created this
     * TestParam. The new WebResource has the resource, and path already set with a
     * query parameter to signal that this is a test: {@link TestMode#TEST_MODE_PROPERTY}.
     *
     * @return a usable web resource
     * @throws IllegalStateException if the endpoint is not set on this TestParam
     * @see TestMode#TEST_MODE_PROPERTY
     * @see JettyIntegResource
     * @see JettyUnitResource
     */
    public WebResource newWebResource() {
        if ( getEndpoint() == null ) {
            throw new IllegalStateException( "Cannot get a web resource without setting the endpoint." );
        }

        return Client.create()
                     .resource( getServerUrl() )
                     .path( getEndpoint() )
                     .queryParam( TestMode.TEST_MODE_PROPERTY, getMode().toString() );
    }


    public TestMode getMode() {
        return mode;
    }


    TestParams setMode( final TestMode mode ) {
        this.mode = mode;
        return this;
    }
}
