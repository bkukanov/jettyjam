package org.safehaus.jettyjam.vaadin;


import java.io.InputStream;

import javax.ws.rs.core.MediaType;

import org.junit.ClassRule;
import org.junit.Test;
import org.safehaus.jettyjam.utils.ContextListener;
import org.safehaus.jettyjam.utils.FilterMapping;
import org.safehaus.jettyjam.utils.JettyContext;
import org.safehaus.jettyjam.utils.JettyUnitResource;
import org.safehaus.jettyjam.utils.ServletMapping;

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
public class VaadinAppTest {

    @JettyContext(
        enableSession = true,
        servletMappings = {
            @ServletMapping( servlet = UIServlet.class, spec = "/VAADIN/*" )
        },
        contextListeners = {
            @ContextListener( listener = VaadinContextListener.class )
        },
        filterMappings = {
            @FilterMapping( filter = GuiceFilter.class, spec = "/*" )
        }
    )
    @ClassRule
    public static JettyUnitResource service = new JettyUnitResource( VaadinAppTest.class );


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
        InputStream in = getClass().getClassLoader().getResourceAsStream( "log4j.properties" );

        FormDataMultiPart part = new FormDataMultiPart();
        part.field( UploadResource.FILENAME_PARAM, "log4j.properties" );

        FormDataBodyPart body = new FormDataBodyPart( UploadResource.CONTENT,
                in, MediaType.APPLICATION_OCTET_STREAM_TYPE );
        part.bodyPart( body );

        String serverUrl = service.getServerUrl().toExternalForm();
        WebResource resource = Client.create().resource( serverUrl + UploadResource.ENDPOINT_URL );
        String result = resource.type( MediaType.MULTIPART_FORM_DATA_TYPE ).post( String.class, part );
    }
}
