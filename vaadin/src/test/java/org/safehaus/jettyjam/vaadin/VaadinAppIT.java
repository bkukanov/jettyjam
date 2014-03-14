package org.safehaus.jettyjam.vaadin;


import javax.ws.rs.core.MediaType;

import org.junit.ClassRule;
import org.junit.Test;
import org.safehaus.embedded.jetty.utils.JettyJarResource;
import org.safehaus.embedded.jetty.utils.Launcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.config.DefaultClientConfig;

import static junit.framework.TestCase.assertEquals;


/**
 * A basic integration test for the embedded jetty application.
 */
public class VaadinAppIT {
    private final static Logger LOG = LoggerFactory.getLogger( VaadinAppIT.class );


    @ClassRule
    public static JettyJarResource app = new JettyJarResource();

    @Test
    public void testEmbeddedApp() throws Exception {
        LOG.info( "integration testing embedded jetty application executable jar file" );

        DefaultClientConfig clientConfig = new DefaultClientConfig();
        Client client = Client.create( clientConfig );
        String result = client
                .resource( app.getAppProperties().getProperty( Launcher.SERVER_URL ) )
                .path( FooResource.ENDPOINT_URL )
                .accept( MediaType.APPLICATION_JSON )
                .get( String.class );

        assertEquals( FooResource.JSON_MESSAGE, result );
    }
}
