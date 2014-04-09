package org.safehaus.jettyjam.utils;


import javax.ws.rs.core.MediaType;

import org.junit.Rule;
import org.junit.Test;

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
    public JettyResource service = new JettyUnitResource( this );


    @Test
    public void testFilter() {
        String result = service.newTestParams()
               .setEndpoint( ENDPOINT )
               .newWebResource()
               .accept( MediaType.TEXT_PLAIN )
               .get( String.class );

        assertEquals( TestServlet.MESSAGE + TestFilter.MESSAGE, result );
    }
}
