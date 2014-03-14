package org.safehaus.jettyjam.simple;


import org.safehaus.embedded.jetty.utils.JettyContext;
import org.safehaus.embedded.jetty.utils.ServletMapping;


/**
 * A simple JettyLauncher.
 */
@JettyContext(
    servletMappings = { @ServletMapping( servlet = HelloWorldServlet.class, spec = "/*" ) },
    filterMappings = {}
)
public class SimpleAppLauncher extends org.safehaus.embedded.jetty.utils.Launcher {

    public SimpleAppLauncher() {
        super( "TestApp", SimpleAppLauncher.class.getClassLoader() );
    }


    @Override
    public String getPackageBase() {
        return getClass().getPackage().getName();
    }


    public static void main( String [] args ) throws Exception {
        SimpleAppLauncher launcher = new SimpleAppLauncher();
        launcher.start();
    }
}
