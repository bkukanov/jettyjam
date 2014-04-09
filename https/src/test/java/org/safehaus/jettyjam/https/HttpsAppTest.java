package org.safehaus.jettyjam.https;


import javax.ws.rs.core.MediaType;

import org.junit.Rule;
import org.junit.Test;
import org.safehaus.jettyjam.utils.HttpConnector;
import org.safehaus.jettyjam.utils.HttpsConnector;
import org.safehaus.jettyjam.utils.JettyConnectors;
import org.safehaus.jettyjam.utils.JettyContext;
import org.safehaus.jettyjam.utils.JettyUnitResource;
import org.safehaus.jettyjam.utils.ServletMapping;

import static junit.framework.TestCase.assertEquals;


/**
 * Tests the Hello World Servlet in embedded mode.
 */
public class HttpsAppTest {

    @JettyContext(
        servletMappings = {
                @ServletMapping( servlet = HelloWorldServlet.class, spec = "/*" )
        }
    )
    @JettyConnectors(
        defaultId = "https",
        httpConnectors = { @HttpConnector( id = "http" ) },
        httpsConnectors = { @HttpsConnector( id = "https" ) }
    )
    @Rule
    public JettyUnitResource service = new JettyUnitResource( this );


    @Test
    public void testHelloWorld() {
        String result = service
                .newTestParams()
                .setEndpoint( "/" )
                .newWebResource()
                .accept( MediaType.TEXT_PLAIN )
                .get( String.class );

        assertEquals( HelloWorldServlet.MESSAGE, result );
    }
}
