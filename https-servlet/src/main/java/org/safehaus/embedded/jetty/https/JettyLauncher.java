package org.safehaus.embedded.jetty.https;


import org.safehaus.embedded.jetty.utils.HttpConnector;
import org.safehaus.embedded.jetty.utils.HttpsConnector;
import org.safehaus.embedded.jetty.utils.JettyConnectors;
import org.safehaus.embedded.jetty.utils.JettyContext;
import org.safehaus.embedded.jetty.utils.ServletMapping;


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
