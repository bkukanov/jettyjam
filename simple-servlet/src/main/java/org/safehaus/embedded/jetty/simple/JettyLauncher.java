package org.safehaus.embedded.jetty.simple;


import org.safehaus.embedded.jetty.utils.JettyHandlers;
import org.safehaus.embedded.jetty.utils.ServletMapping;


/**
 * A simple JettyLauncher.
 */
@JettyHandlers(
    servletMappings = { @ServletMapping( servlet = HelloWorldServlet.class, spec = "/*" ) },
    filterMappings = {}
)
public class JettyLauncher extends org.safehaus.embedded.jetty.utils.Launcher {

    public JettyLauncher() {
        super( "TestApp", JettyLauncher.class.getClassLoader() );
    }


    @Override
    public String getPackageBase() {
        return getClass().getPackage().getName();
    }


    public static void main( String [] args ) throws Exception {
        JettyLauncher launcher = new JettyLauncher();
        launcher.start();
    }
}
