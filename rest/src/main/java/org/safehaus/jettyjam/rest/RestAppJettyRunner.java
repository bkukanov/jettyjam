package org.safehaus.jettyjam.rest;


import org.safehaus.jettyjam.utils.ContextListener;
import org.safehaus.jettyjam.utils.FilterMapping;
import org.safehaus.jettyjam.utils.JettyContext;
import org.safehaus.jettyjam.utils.JettyRunner;

import com.google.inject.servlet.GuiceFilter;


/**
 * A simple JettyLauncher.
 */
@JettyContext(
    contextListeners = { @ContextListener( listener = RestAppContextListener.class ) },
    filterMappings = { @FilterMapping( filter = GuiceFilter.class, spec = "/*") }
)
public class RestAppJettyRunner extends JettyRunner {

    public RestAppJettyRunner() {
        super( "TestApp" );
    }


    @Override
    public String getSubClass() {
        return getClass().getName();
    }


    public static void main( String [] args ) throws Exception {
        RestAppJettyRunner launcher = new RestAppJettyRunner();
        launcher.start();
    }
}
