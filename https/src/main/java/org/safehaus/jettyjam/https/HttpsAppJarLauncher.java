package org.safehaus.jettyjam.https;


import org.safehaus.embedded.jetty.utils.JarJarClassLoader;


/**
 * Launches the main() of the HttpsAppJettyRunner.
 */
public class HttpsAppJarLauncher {

    public static void main( String[] args ) throws Throwable {
        JarJarClassLoader cl = new JarJarClassLoader();
        cl.invokeMain( "org.safehaus.jettyjam.https.HttpsAppJettyRunner", args );
    }
}
