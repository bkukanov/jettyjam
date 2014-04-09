package org.safehaus.jettyjam.rest;


import java.io.File;
import java.io.InputStream;
import java.util.Random;

import javax.ws.rs.core.MediaType;

import org.junit.ClassRule;
import org.junit.Test;
import org.safehaus.jettyjam.utils.ContextListener;
import org.safehaus.jettyjam.utils.FilterMapping;
import org.safehaus.jettyjam.utils.JettyContext;
import org.safehaus.jettyjam.utils.JettyUnitResource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.servlet.GuiceFilter;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.multipart.FormDataBodyPart;
import com.sun.jersey.multipart.FormDataMultiPart;

import static junit.framework.TestCase.assertEquals;


/**
 * Tests the JSON rest service foo in embedded mode.
 */
public class RestAppTest {
    private final static Logger LOG = LoggerFactory.getLogger( RestAppTest.class );

    @JettyContext(
        contextListeners = {
            @ContextListener( listener = RestAppContextListener.class )
        },
        filterMappings = {
            @FilterMapping( filter = GuiceFilter.class, spec = "/*" )
        }
    )
    @ClassRule
    public static JettyUnitResource service = new JettyUnitResource( RestAppTest.class );


    @Test
    public void testFooResource() {
        DefaultClientConfig clientConfig = new DefaultClientConfig();
        Client client = Client.create( clientConfig );
        String result = client
                .resource( service.getServerUrl().toString() )
                .path( FooResource.ENDPOINT_URL )
                .accept( MediaType.APPLICATION_JSON )
                .get( String.class );

        assertEquals( FooResource.JSON_MESSAGE, result );
    }


    @Test
    public void testUploadResource() throws Exception {
        File downloads = new File( UploadResource.getDownloadDir() );
        File testFile = new File( downloads, "log4j.properties" + new Random().nextDouble() );

        LOG.warn( "testFile = {}", testFile.getAbsolutePath() );

        InputStream in = getClass().getClassLoader().getResourceAsStream( "log4j.properties" );

        FormDataMultiPart part = new FormDataMultiPart();
        part.field( UploadResource.FILENAME_PARAM, testFile.getName() );

        FormDataBodyPart body = new FormDataBodyPart( UploadResource.CONTENT,
                in, MediaType.APPLICATION_OCTET_STREAM_TYPE );
        part.bodyPart( body );

        LOG.debug( "Server URL = {}", service.getServerUrl() );

        WebResource resource = Client.create()
                                     .resource( service.getServerUrl().toString() )
                                     .path( UploadResource.ENDPOINT_URL );
        String result = resource.type( MediaType.MULTIPART_FORM_DATA_TYPE ).post( String.class, part );

        LOG.warn( "Got back result = {}", result );
    }
}
