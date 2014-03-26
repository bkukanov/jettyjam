package org.safehaus.jettyjam.rest;


import java.io.InputStream;

import javax.ws.rs.core.MediaType;

import org.junit.ClassRule;
import org.junit.Test;
import org.safehaus.jettyjam.utils.JettyJarResource;
import org.safehaus.jettyjam.utils.JettyRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.multipart.FormDataBodyPart;
import com.sun.jersey.multipart.FormDataMultiPart;

import static junit.framework.TestCase.assertEquals;


/**
 * A basic integration test for the embedded jetty application.
 */
public class RestAppIT {
    private final static Logger LOG = LoggerFactory.getLogger( RestAppIT.class );


    @ClassRule
    public static JettyJarResource app = new JettyJarResource( 5005 );


//    @Test
    public void testEmbeddedApp() throws Exception {
        LOG.info( "integration testing embedded jetty application executable jar file" );

        DefaultClientConfig clientConfig = new DefaultClientConfig();
        Client client = Client.create( clientConfig );
        String result = client
                .resource( app.getAppProperties().getProperty( JettyRunner.SERVER_URL ) )
                .path( "/" )
                .accept( MediaType.TEXT_PLAIN )
                .get( String.class );

        assertEquals( HelloWorldServlet.MESSAGE, result );
    }


//    @Test
    public void testFooResource() {
        DefaultClientConfig clientConfig = new DefaultClientConfig();
        Client client = Client.create( clientConfig );
        String result = client
                .resource( app.getAppProperties().getProperty( JettyRunner.SERVER_URL ) )
                .path( FooResource.ENDPOINT_URL )
                .accept( MediaType.TEXT_PLAIN )
                .get( String.class );

        assertEquals( FooResource.JSON_MESSAGE, result );
    }


    @Test
    public void testUploadResource() throws Exception {
        InputStream in = getClass().getClassLoader().getResourceAsStream( "log4j.properties" );

        FormDataMultiPart part = new FormDataMultiPart();
        part.field( UploadResource.FILENAME_PARAM, "log4j.properties" );

        FormDataBodyPart body = new FormDataBodyPart( UploadResource.CONTENT,
                in, MediaType.APPLICATION_OCTET_STREAM_TYPE );
        part.bodyPart( body );

        LOG.debug( "Server URL = {}", app.getServerUrl() );

        WebResource resource = Client.create().resource( app.getServerUrl() + UploadResource.ENDPOINT_URL );
        String result = resource.type( MediaType.MULTIPART_FORM_DATA_TYPE ).post( String.class, part );

        LOG.debug( "Got back result = {}", result );
    }
}
