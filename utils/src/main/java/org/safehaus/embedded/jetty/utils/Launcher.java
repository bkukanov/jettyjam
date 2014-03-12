package org.safehaus.embedded.jetty.utils;


import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;


/**
 * A Simple Jetty Launcher.
 */
public class Launcher {

    public static void main( String [] args ) throws Exception {

        // Add a simple servlet to the Jetty container
        ServletHolder holder = new ServletHolder( HelloWorldServlet.class );
        ServletHandler handler = new ServletHandler();
        handler.addServletWithMapping( holder, "/*" );

        Server server = new Server( 0 );
        server.setHandler( handler );
        server.start();
    }
}

