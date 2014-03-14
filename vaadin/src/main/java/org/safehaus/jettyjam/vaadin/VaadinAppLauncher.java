package org.safehaus.jettyjam.vaadin;


import org.safehaus.embedded.jetty.utils.ContextListener;
import org.safehaus.embedded.jetty.utils.FilterMapping;
import org.safehaus.embedded.jetty.utils.JettyContext;

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
public class VaadinAppLauncher extends org.safehaus.embedded.jetty.utils.Launcher {

    public VaadinAppLauncher() {
        super( "TestApp", VaadinAppLauncher.class.getClassLoader() );
    }


    @Override
    public String getPackageBase() {
        return getClass().getPackage().getName();
    }


    public static void main( String [] args ) throws Exception {
        VaadinAppLauncher launcher = new VaadinAppLauncher();
        launcher.start();
    }
}
