package org.safehaus.jettyjam.utils;


import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;


/**
 * A simple filter.
 */
public class TestFilter implements Filter {
    public static final String MESSAGE = ": This was appended by a Filter";

    @Override
    public void init( final FilterConfig filterConfig ) throws ServletException {
    }


    @Override
    public void doFilter( final ServletRequest req, final ServletResponse resp, final FilterChain chain )
            throws IOException, ServletException {
        chain.doFilter( req, resp );

        resp.setContentType( "text/plain" );
        resp.getOutputStream().print( MESSAGE );
        resp.flushBuffer();
    }


    @Override
    public void destroy() {
    }
}
