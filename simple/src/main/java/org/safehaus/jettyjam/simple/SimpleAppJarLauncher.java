package org.safehaus.jettyjam.simple;


import org.safehaus.jettyjam.utils.JarJarClassLoader;


/**
 * Launches the main() of the SimpleAppJettyRunner.
 */
public class SimpleAppJarLauncher {

    public static void main( String[] args ) throws Throwable {
        JarJarClassLoader cl = new JarJarClassLoader();
        cl.invokeMain( "org.safehaus.jettyjam.simple.SimpleAppJettyRunner", args );
    }
}
