package org.safehaus.embedded.jetty.utils;


import javax.ws.rs.core.MediaType;

import org.junit.Rule;
import org.junit.Test;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.config.DefaultClientConfig;


/**
 * Tests the Hello World Servlet in embedded mode.
 */
public class TestHelloWorld {

    @JettyHandlers(
        servletMappings = {
                @ServletMapping( servlet = HelloWorldServlet.class, spec = "/*" )
        },
        filterMappings = { }
    )
    @Rule
    public JettyResource service = new JettyResource();


    @Test
    public void testHelloWorld() {
        DefaultClientConfig clientConfig = new DefaultClientConfig();
        Client client = Client.create( clientConfig );
        String result = client
                .resource( service.getServerUrl().toString() )
                .path( "/" )
                .accept( MediaType.TEXT_PLAIN )
                .get( String.class );

        System.out.println( result );
    }
}
