package org.safehaus.jettyjam.https;


import org.safehaus.jettyjam.utils.HttpConnector;
import org.safehaus.jettyjam.utils.HttpsConnector;
import org.safehaus.jettyjam.utils.JettyConnectors;
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
@JettyConnectors(
    defaultId = "https",
    httpConnectors = { @HttpConnector( id = "http" ) },
    httpsConnectors = { @HttpsConnector( id = "https" ) }
)
public class HttpsAppJettyRunner extends JettyRunner {

    public HttpsAppJettyRunner() {
        super( "TestApp" );
    }


    @Override
    public String getSubClass() {
        return getClass().getName();
    }


    public static void main( String [] args ) throws Exception {
        HttpsAppJettyRunner launcher = new HttpsAppJettyRunner();
        launcher.start();
    }
}
