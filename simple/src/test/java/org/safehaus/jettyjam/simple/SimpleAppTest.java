package org.safehaus.jettyjam.simple;


import javax.ws.rs.core.MediaType;

import org.junit.Rule;
import org.junit.Test;
import org.safehaus.jettyjam.utils.JettyContext;
import org.safehaus.jettyjam.utils.JettyUnitResource;
import org.safehaus.jettyjam.utils.ServletMapping;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.config.DefaultClientConfig;

import static junit.framework.TestCase.assertEquals;


/**
 * Tests the Hello World Servlet in embedded mode.
 */
public class SimpleAppTest {

    @JettyContext(
        servletMappings = {
            @ServletMapping( servlet = HelloWorldServlet.class, spec = "/*" )
        }
    )
    @Rule
    public JettyUnitResource service = new JettyUnitResource( this );


    @Test
    public void testHelloWorld() {
        DefaultClientConfig clientConfig = new DefaultClientConfig();
        Client client = Client.create( clientConfig );
        String result = client
                .resource( service.getServerUrl().toString() )
                .path( "/" )
                .accept( MediaType.TEXT_PLAIN )
                .get( String.class );

        assertEquals( HelloWorldServlet.MESSAGE, result );
    }
}
