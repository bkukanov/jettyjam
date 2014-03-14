package org.safehaus.jettyjam.rest;


import javax.ws.rs.core.MediaType;

import org.junit.Rule;
import org.junit.Test;
import org.safehaus.embedded.jetty.utils.ContextListener;
import org.safehaus.embedded.jetty.utils.FilterMapping;
import org.safehaus.embedded.jetty.utils.JettyContext;
import org.safehaus.embedded.jetty.utils.JettyResource;

import com.google.inject.servlet.GuiceFilter;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.config.DefaultClientConfig;

import static junit.framework.TestCase.assertEquals;


/**
 * Tests the JSON rest service foo in embedded mode.
 */
public class RestAppTest {

    @JettyContext(
        contextListeners = {
            @ContextListener( listener = RestAppContextListener.class )
        },
        filterMappings = {
            @FilterMapping( filter = GuiceFilter.class, spec = "/*" )
        }
    )
    @Rule
    public JettyResource service = new JettyResource();


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
}
