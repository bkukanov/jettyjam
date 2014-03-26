package org.safehaus.jettyjam.utils;


import javax.ws.rs.core.MediaType;

import org.junit.Test;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.config.DefaultClientConfig;

import static junit.framework.TestCase.assertEquals;


/**
 * Tests the JettyRunner functionality.
 */
@JettyContext(
    servletMappings = { @ServletMapping( servlet = TestServlet.class, spec = "/*" ) }
)
public class JettyRunnerTest extends JettyRunner {
    private static boolean runAsTest;

    public JettyRunnerTest() {
        super( "SimpleTestApp" );
    }


    @Override
    public String getSubClass() {
        return getClass().getName();
    }


    public static void main( String [] args ) throws Throwable {
        JettyRunnerTest launcher = new JettyRunnerTest();
        launcher.start();

        if ( runAsTest ) {
            DefaultClientConfig clientConfig = new DefaultClientConfig();
            Client client = Client.create( clientConfig );
            String result = client
                    .resource( launcher.getServerUrl().toString() )
                    .path( "/" )
                    .accept( MediaType.TEXT_PLAIN )
                    .get( String.class );

            assertEquals( TestServlet.MESSAGE, result );

            launcher.stop();
        }
    }


    @Test
    public void testLauncher() throws Throwable {
        runAsTest = true;
        main( null );
    }
}
