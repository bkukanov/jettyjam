package org.safehaus.jettyjam.https;


import javax.ws.rs.core.MediaType;

import org.junit.Rule;
import org.junit.Test;
import org.safehaus.jettyjam.utils.JettyIntegResource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static junit.framework.TestCase.assertEquals;


/**
 * A basic integration test for the embedded jetty application.
 */
public class HttpsAppIT {
    private final static Logger LOG = LoggerFactory.getLogger( HttpsAppIT.class );


    @Rule
    public JettyIntegResource app = new JettyIntegResource( this );

    @Test
    public void testHelloWorld() throws Exception {
        LOG.info( "integration testing embedded jetty application executable jar file" );

        String result = app
                .newTestParams()
                .setEndpoint( "/" )
                .newWebResource()
                .accept( MediaType.TEXT_PLAIN )
                .get( String.class );

        assertEquals( HelloWorldServlet.MESSAGE, result );
    }
}
