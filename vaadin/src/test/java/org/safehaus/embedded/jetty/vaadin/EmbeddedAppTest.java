package org.safehaus.embedded.jetty.vaadin;


import javax.ws.rs.core.MediaType;

import org.junit.ClassRule;
import org.junit.Test;
import org.safehaus.embedded.jetty.utils.ContextListener;
import org.safehaus.embedded.jetty.utils.FilterMapping;
import org.safehaus.embedded.jetty.utils.JettyContext;
import org.safehaus.embedded.jetty.utils.JettyResource;
import org.safehaus.embedded.jetty.utils.ServletMapping;

import com.google.inject.servlet.GuiceFilter;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.config.DefaultClientConfig;

import static junit.framework.TestCase.assertEquals;


/**
 * Tests the JSON rest service foo in embedded mode.
 */
public class EmbeddedAppTest {

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
    public static JettyResource service = new JettyResource();


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
