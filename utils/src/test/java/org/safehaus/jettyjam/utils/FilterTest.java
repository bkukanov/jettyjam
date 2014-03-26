package org.safehaus.jettyjam.utils;


import javax.ws.rs.core.MediaType;

import org.junit.Rule;
import org.junit.Test;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.config.DefaultClientConfig;

import static junit.framework.TestCase.assertEquals;


/**
 * Tests the Hello World Servlet in embedded mode.
 */
public class FilterTest {
    public static final String ENDPOINT = "/test";

    @JettyContext(
        servletMappings = {
            @ServletMapping( servlet = TestServlet.class, spec = ENDPOINT )
        },
        filterMappings = {
            @FilterMapping( filter = TestFilter.class, spec = "/*" )
        }
    )
    @Rule
    public JettyResource service = new JettyResource();


    @Test
    public void testFilter() {
        DefaultClientConfig clientConfig = new DefaultClientConfig();
        Client client = Client.create( clientConfig );
        String result = client
                .resource( service.getServerUrl().toString() )
                .path( ENDPOINT )
                .accept( MediaType.TEXT_PLAIN )
                .get( String.class );

        assertEquals( TestServlet.MESSAGE + TestFilter.MESSAGE, result );
    }
}
