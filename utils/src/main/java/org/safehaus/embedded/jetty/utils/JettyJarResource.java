package org.safehaus.embedded.jetty.utils;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Properties;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * An external resource that start and stops the embedded Jetty application in an
 * executable jar file generated by the build for conducting integration tests with
 * the maven failsafe plugin.
 */
public class JettyJarResource implements TestRule {
    public static final String RESOURCE_FILE = "JettyJarResource.properties";
    private static final Logger LOG = LoggerFactory.getLogger( JettyJarResource.class );

    public static final String JAR_FILE_PATH_KEY = "jar.file.path";
    private final String jarFilePath;
    private Process process;
    private PrintWriter out;
    private BufferedReader in;
    private String pidFilePath;
    private Properties appProperties;
    private String hostname;
    private int port;


    /**
     * Creates a new JettyJarResource.
     *
     * @throws RuntimeException if the {@link #RESOURCE_FILE} cannot be found.
     */
    public JettyJarResource() {
        try {
            jarFilePath = findExecutableJar();
        }
        catch ( IOException e ) {
            throw new RuntimeException( e );
        }
    }


    public Properties getAppProperties() {
        return appProperties;
    }


    /**
     * Finds the path to the executable jar file with the embedded Jetty application.
     * There are a number of approaches that could be taken to discover the executable
     * jar file produced by the build. The most reliable, which will work both on the
     * command line via Maven and in IDE environments is to load a properties
     * resource file containing the property for the executable jar file path. The
     * properties can have variable substitutions applied to it. The down side to this
     * approach is that every project that uses this external resource must have that
     * resource file present in order to run IT tests on the generated executable jar
     * file.
     *
     * @return the path to the executable jar
     * @throws IOException when
     */
    private String findExecutableJar() throws IOException {
        InputStream in = getClass().getClassLoader().getResourceAsStream( RESOURCE_FILE );
        Properties props = new Properties();
        props.load( in );
        return props.getProperty( JAR_FILE_PATH_KEY );
    }


    protected void before() throws Exception {
        File jarFile = new File( jarFilePath );

        if ( ! jarFile.exists() ) {
            throw new FileNotFoundException( "Cannot find jar file: " + jarFile.getCanonicalPath() );
        }

        String[] execArgs = { "java", "-jar", jarFile.getCanonicalPath() };
        process = Runtime.getRuntime().exec( execArgs );

        // the path to the pidFilePath will be output from the stderr stream
        in = new BufferedReader( new InputStreamReader( process.getErrorStream() ) );
        Thread t = new Thread( new Runnable() {
            @Override
            public void run() {
                try {
                    // this will block until we get an output line from stderr
                    pidFilePath = in.readLine();
                    LOG.info( "Got pidFilePath {} from application CLI", pidFilePath );
                }
                catch ( IOException e ) {
                    LOG.error( "Failure while reading from standard input", e );
                }

                // once we get the pidFilePath output we exit - a one time thing!
            }
        });
        t.start();

        // issue the command to get the pid file path from application
        out = new PrintWriter( process.getOutputStream() );
        out.println( Launcher.PID_FILE );
        out.flush();

        // wait until the thread above completes and we get the pidFilePath path
        t.join( 1000 );

        if ( pidFilePath == null ) {
            out.close();
            process.destroy();
            throw new IllegalStateException( "No results found for the pidFile path." );
        }

        LOG.info( "Loading properties from pidFilePath = {}", pidFilePath );

        appProperties = new Properties();
        appProperties.load( new FileInputStream( pidFilePath ) );
        LOG.info( "Loaded properties file: {}", pidFilePath );
        appProperties.list( System.out );

        port = Integer.parseInt( appProperties.getProperty( Launcher.SERVER_PORT ) );
        hostname = "localhost";
    }


    protected void after() throws Exception {
        if ( pidFilePath != null ) {
            File pidFile = new File( pidFilePath );
            if ( pidFile.exists() && ! pidFile.delete() ) pidFile.deleteOnExit();
        }

        out.println( Launcher.SHUTDOWN );
        out.flush();
        out.close();
        process.destroy();
    }


    @Override
    public Statement apply( final Statement base, final Description description ) {
        return statement( base );
    }


    private Statement statement( final Statement base ) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                before();
                try {
                    base.evaluate();
                }
                finally {
                    after();
                }
            }
        };
    }


    public String getHostname() {
        return hostname;
    }


    public int getPort() {
        return port;
    }
}
