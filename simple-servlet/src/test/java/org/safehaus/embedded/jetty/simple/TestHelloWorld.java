package org.safehaus.embedded.jetty.simple;


import org.junit.Rule;
import org.junit.Test;


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

    }
}
