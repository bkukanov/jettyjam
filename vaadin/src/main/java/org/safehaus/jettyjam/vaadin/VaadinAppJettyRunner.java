package org.safehaus.jettyjam.vaadin;


import org.safehaus.embedded.jetty.utils.ContextListener;
import org.safehaus.embedded.jetty.utils.FilterMapping;
import org.safehaus.embedded.jetty.utils.JettyContext;
import org.safehaus.embedded.jetty.utils.JettyRunner;

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
public class VaadinAppJettyRunner extends JettyRunner {

    public VaadinAppJettyRunner() {
        super( "TestApp", VaadinAppJettyRunner.class.getClassLoader() );
    }


    @Override
    public String getSubClass() {
        return getClass().getName();
    }


    public static void main( String [] args ) throws Exception {
        VaadinAppJettyRunner launcher = new VaadinAppJettyRunner();
        launcher.start();
    }
}
