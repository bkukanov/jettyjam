package org.safehaus.embedded.jetty.utils;


import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * A simple servlet that prints out hello world.
 */
public class HelloWorldServlet extends HttpServlet {
    @Override
    protected void doGet( final HttpServletRequest req, final HttpServletResponse resp )
            throws ServletException, IOException
    {
        resp.setContentType( "text/plain" );
        resp.getOutputStream().println( "Hello World" );
        resp.flushBuffer();
    }
}
