package org.safehaus.jettyjam.vaadin;


import org.safehaus.embedded.jetty.utils.JarJarClassLoader;


/**
 * Launches the main() of the VaadinAppJettyRunner.
 */
public class VaadinAppJarLauncher {

    public static void main( String[] args ) throws Throwable {
        JarJarClassLoader cl = new JarJarClassLoader();
        cl.invokeMain( "org.safehaus.jettyjam.vaadin.VaadinAppJettyRunner", args );
    }
}
