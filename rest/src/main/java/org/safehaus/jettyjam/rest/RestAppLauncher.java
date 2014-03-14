package org.safehaus.jettyjam.rest;


import org.safehaus.embedded.jetty.utils.FilterMapping;
import org.safehaus.embedded.jetty.utils.JettyContext;
import org.safehaus.embedded.jetty.utils.ServletMapping;

import com.google.inject.servlet.GuiceFilter;


/**
 * A simple JettyLauncher.
 */
@JettyContext(
    servletMappings = { @ServletMapping( servlet = HelloWorldServlet.class, spec = "/*" ) },
    filterMappings = { @FilterMapping( filter = GuiceFilter.class, spec = "/*") }
)
public class RestAppLauncher extends org.safehaus.embedded.jetty.utils.Launcher {

    public RestAppLauncher() {
        super( "TestApp", RestAppLauncher.class.getClassLoader() );
    }


    @Override
    public String getPackageBase() {
        return getClass().getPackage().getName();
    }


    public static void main( String [] args ) throws Exception {
        RestAppLauncher launcher = new RestAppLauncher();
        launcher.start();
    }
}
