package org.safehaus.embedded.jetty.utils;


import javax.ws.rs.core.MediaType;

import org.junit.Test;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.config.DefaultClientConfig;

import static junit.framework.TestCase.assertEquals;


/**
 * Tests the Launcher functionality.
 */
@JettyContext(
    servletMappings = { @ServletMapping( servlet = TestServlet.class, spec = "/*" ) }
)
public class LauncherTest extends Launcher {
    private static boolean runAsTest;

    public LauncherTest() {
        super( "SimpleTestApp", LauncherTest.class.getClassLoader() );
    }


    @Override
    public String getPackageBase() {
        return getClass().getPackage().getName();
    }


    public static void main( String [] args ) throws Exception {
        LauncherTest launcher = new LauncherTest();
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
    public void testLauncher() throws Exception {
        runAsTest = true;
        main( null );
    }
}
