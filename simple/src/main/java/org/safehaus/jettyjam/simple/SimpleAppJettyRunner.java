package org.safehaus.jettyjam.simple;


import org.safehaus.jettyjam.utils.JettyContext;
import org.safehaus.jettyjam.utils.JettyRunner;
import org.safehaus.jettyjam.utils.ServletMapping;


/**
 * A simple JettyLauncher.
 */
@JettyContext(
    servletMappings = { @ServletMapping( servlet = HelloWorldServlet.class, spec = "/*" ) },
    filterMappings = {}
)
public class SimpleAppJettyRunner extends JettyRunner {

    public SimpleAppJettyRunner() {
        super( "TestApp" );
    }


    @Override
    public String getSubClass() {
        return getClass().getName();
    }


    public static void main( String [] args ) throws Exception {
        SimpleAppJettyRunner launcher = new SimpleAppJettyRunner();
        launcher.start();
    }
}
