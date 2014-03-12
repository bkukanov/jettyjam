package org.safehaus.embedded.jetty.simple;


import org.safehaus.embedded.jetty.utils.JettyHandlers;
import org.safehaus.embedded.jetty.utils.ServletMapping;


/**
 * A simple Launcher.
 */
@JettyHandlers(
    servletMappings = { @ServletMapping( servlet = HelloWorldServlet.class, spec = "/*" ) },
    filterMappings = {}
)
public class Launcher extends org.safehaus.embedded.jetty.utils.Launcher {

    public Launcher() {
        super( 0 );
    }


    @Override
    public String getPackageBase() {
        return getClass().getPackage().getName();
    }


    public static void main( String [] args ) throws Exception {
        Launcher launcher = new Launcher();
        launcher.start();
    }
}
