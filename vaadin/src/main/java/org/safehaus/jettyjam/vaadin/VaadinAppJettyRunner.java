package org.safehaus.jettyjam.vaadin;


import org.safehaus.jettyjam.utils.ContextListener;
import org.safehaus.jettyjam.utils.FilterMapping;
import org.safehaus.jettyjam.utils.JettyContext;
import org.safehaus.jettyjam.utils.JettyRunner;

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
        super( "TestApp" );
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
