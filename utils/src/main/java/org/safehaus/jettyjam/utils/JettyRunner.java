package org.safehaus.jettyjam.utils;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.net.URL;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/** A Jetty launcher. */
public abstract class JettyRunner {
    private static final Logger LOG = LoggerFactory.getLogger( JettyRunner.class );

    public static final String PRINT_ALL = "print_all";
    public static final String SHUTDOWN = "shutdown";
    public static final String SERVER_URL = "serverUrl";
    public static final String SERVER_PORT = "serverPort";
    public static final String APP_ID = "appId";
    public static final String APP_NAME = "appName";
    public static final String PID_FILE = "pidFile";
    public static final String IS_SECURE = "isSecure";
    public static final String PID = "pid";

    private final Server server;
    private URL serverUrl;
    private int port;
    private boolean started;
    private Timer timer = new Timer();
    private File pidFile;
    private final UUID appId;
    private final String appName;
    private String hostname;
    private boolean secure;


    protected JettyRunner( String appName ) {
        server = new Server();
        appId = UUID.randomUUID();
        this.appName = appName;
        LOG.info( "JettyRunner for appId {}", appId );
    }


    protected void start() throws Exception {
        ServerConnector defaultConnector = ConnectorBuilder.setConnectors( getSubClass(), server );

        if ( defaultConnector.getHost() == null ) {
            hostname = "localhost";
        }

        HandlerBuilder handlerBuilder = new HandlerBuilder();
        server.setHandler( handlerBuilder.buildForLauncher( getSubClass(), server ) );
        server.start();

        this.port = defaultConnector.getLocalPort();
        String protocol = "http";
        if ( defaultConnector.getDefaultProtocol().contains( "SSL" ) ) {
            protocol = "https";
            secure = true;
        }
        this.serverUrl = new URL( protocol, hostname, port, "" );

        setupPidFile();

        Runtime.getRuntime().addShutdownHook( new Thread( new Runnable() {
            @Override
            public void run() {
                try {
                    JettyRunner.this.stop();
                }
                catch ( Exception e ) {
                    LOG.error( "Failed to stop jetty server in JVM shutdown hook.", e );
                }
            }
        } ) );

        timer.scheduleAtFixedRate( new TimerTask() {
            @Override
            public void run() {
                if ( !pidFile.exists() ) {
                    try {
                        JettyRunner.this.stop();
                    }
                    catch ( Exception e ) {
                        LOG.error( "Failed to stop jetty server after pidFile removal", e );
                    }
                }
            }
        }, 200, 200 ); // @todo make these configurable command line options (archaius?)

        started = true;

        new Thread( new Runnable() {
            @Override
            public void run() {
                String line;
                BufferedReader in = new BufferedReader( new InputStreamReader( System.in ) );

                while ( started ) {
                    try {
                        line = in.readLine();

                        LOG.info( "Line gotten from CLI: {}", line );

                        if ( line.equalsIgnoreCase( SHUTDOWN ) ) {
                            try {
                                stop();
                                System.exit( 0 );
                            }
                            catch ( Exception e ) {
                                LOG.error( "While shutting down", e );
                            }
                        }
                        else if ( line.equalsIgnoreCase( SERVER_URL ) ) {
                            System.err.println( serverUrl.toString() );
                        }
                        else if ( line.equalsIgnoreCase( SERVER_PORT ) ) {
                            System.err.println( port );
                        }
                        else if ( line.equalsIgnoreCase( APP_ID ) ) {
                            System.err.println( appId.toString() );
                        }
                        else if ( line.equalsIgnoreCase( APP_NAME ) ) {
                            System.err.println( appName );
                        }
                        else if ( line.equalsIgnoreCase( PID_FILE ) ) {
                            System.err.println( pidFile.getCanonicalPath() );
                        }
                        else if ( line.equalsIgnoreCase( PRINT_ALL ) ) {
                            System.err.println( SERVER_URL + ": " + serverUrl.toString() );
                            System.err.println( SERVER_PORT + ": " + port );
                            System.err.println( APP_ID + ": " + appId.toString() );
                            System.err.println( APP_NAME + ": " + appName );
                            System.err.println( PID_FILE + ": " + pidFile.getCanonicalPath() );
                            System.err.println( IS_SECURE + ": " + secure );
                        }
                    }
                    catch ( IOException e ) {
                        LOG.error( "While reading from input stream", e );
                    }
                }
            }
        } ).start();
    }


    // Also listen to standard in for a shutdown command - shutdown on pid file removal also
    // Removal of pid file destroys the application and exists
    // Remove the pid file on premature JVM shutdown - need a RT hook for that
    // Later on make this a real pid file with the process ID and enable the use of many apps


    private void setupPidFile() throws IOException {
        pidFile = File.createTempFile( getAppId().toString(), ".pid" );
        Properties properties = new Properties();
        properties.setProperty( APP_ID, getAppId().toString() );
        properties.setProperty( APP_NAME, getAppName() );
        properties.setProperty( SERVER_URL, getServerUrl().toString() );
        properties.setProperty( SERVER_PORT, String.valueOf( port ) );
        properties.setProperty( IS_SECURE, String.valueOf( secure ) );

        String pid = ManagementFactory.getRuntimeMXBean().getName();
        properties.setProperty( PID, pid );

        FileWriter out = new FileWriter( pidFile );
        properties.store( out, "Generated by launcher process: " + pid );
        out.flush();
        out.close();

        System.out.println( "pidFile: " + pidFile.getCanonicalPath() );
    }


    private void cleanupPidFile() throws IOException {
        if ( !pidFile.delete() ) {
            pidFile.deleteOnExit();
        }
    }


    protected void stop() throws Exception {
        server.stop();
        cleanupPidFile();
        started = false;
    }


    public abstract String getSubClass();


    public String getAppName() {
        return appName;
    }


    public UUID getAppId() {
        return appId;
    }


    public String getHostname() {
        return hostname;
    }


    public int getPort() {
        return port;
    }


    public boolean isSecure() {
        return secure;
    }


    public URL getServerUrl() {
        return serverUrl;
    }
}

