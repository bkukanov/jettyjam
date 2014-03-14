package org.safehaus.jettyjam.vaadin;


import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * A simple servlet that prints out hello world.
 */
public class HelloWorldServlet extends HttpServlet {
    public static final String MESSAGE = "Hello World";

    @Override
    protected void doGet( final HttpServletRequest req, final HttpServletResponse resp )
            throws ServletException, IOException
    {
        resp.getOutputStream().print( MESSAGE );
        resp.flushBuffer();
    }
}
