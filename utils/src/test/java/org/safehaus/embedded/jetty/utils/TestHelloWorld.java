package java.org.safehaus.embedded.jetty.utils;


import org.junit.Rule;
import org.junit.Test;
import org.safehaus.embedded.jetty.utils.JettyHandlers;
import org.safehaus.embedded.jetty.utils.JettyResource;
import org.safehaus.embedded.jetty.utils.ServletMapping;


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
