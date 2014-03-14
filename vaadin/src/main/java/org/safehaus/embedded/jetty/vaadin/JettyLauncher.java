package org.safehaus.embedded.jetty.vaadin;


import org.safehaus.embedded.jetty.utils.ContextListener;
import org.safehaus.embedded.jetty.utils.FilterMapping;
import org.safehaus.embedded.jetty.utils.JettyContext;
import org.safehaus.embedded.jetty.utils.ServletMapping;

import com.google.inject.servlet.GuiceFilter;


/**
 * A simple JettyLauncher.
 */
@JettyContext(
    enableSession = true,
    contextListeners = {
        @ContextListener( listener = VaadinContextListener.class )
    },
    filterMappings = {
        @FilterMapping( filter = GuiceFilter.class, spec = "/*" )
    }
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
