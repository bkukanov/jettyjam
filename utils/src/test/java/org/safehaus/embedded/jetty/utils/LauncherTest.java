package org.safehaus.embedded.jetty.utils;


/**
 * Tests the Launcher functionality.
 */
@JettyHandlers(
    servletMappings = { @ServletMapping( servlet = HelloWorldServlet.class, spec = "/*" ) },
    filterMappings = {}
)
public class LauncherTest extends Launcher {

    protected LauncherTest() {
        super( 0 );
    }


    @Override
    public String getPackageBase() {
        return getClass().getPackage().getName();
    }


    public static void main( String [] args ) throws Exception {
        new LauncherTest().start();
    }
}
