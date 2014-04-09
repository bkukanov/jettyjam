package org.safehaus.jettyjam.utils;


import javax.ws.rs.core.MediaType;

import org.junit.Rule;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;


/**
 * Tests the Hello World Servlet in embedded mode.
 */
public class ServletTest {

    @JettyContext(
        servletMappings = {
                @ServletMapping( servlet = TestServlet.class, spec = "/*" )
        }
    )
    @Rule
    public JettyResource service = new JettyUnitResource( this );


    @Test
    public void testHelloWorld() {
        String result = service.newTestParams()
                               .setEndpoint( "/" )
                               .newWebResource().accept( MediaType.TEXT_PLAIN )
                               .get( String.class );

        assertEquals( TestServlet.MESSAGE, result );
    }
}
