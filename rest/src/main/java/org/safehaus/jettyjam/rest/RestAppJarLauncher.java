package org.safehaus.jettyjam.rest;


import org.safehaus.jettyjam.utils.JarJarClassLoader;


/**
 * Launches the main() of the RestAppJettyRunner.
 */
public class RestAppJarLauncher {

    public static void main( String[] args ) throws Throwable {
        JarJarClassLoader cl = new JarJarClassLoader();
        cl.invokeMain( "org.safehaus.jettyjam.rest.RestAppJettyRunner", args );
    }
}
