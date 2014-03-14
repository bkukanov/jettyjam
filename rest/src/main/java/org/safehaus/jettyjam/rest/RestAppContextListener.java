package org.safehaus.jettyjam.rest;


import javax.servlet.ServletContextEvent;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;


/**
 * A application context listener for Guice.
 */
public class RestAppContextListener extends GuiceServletContextListener {
    @Override
    protected Injector getInjector() {
        return Guice.createInjector( new RestAppModule() );
    }


    @Override
    public void contextInitialized( ServletContextEvent servletContextEvent ) {
        super.contextInitialized( servletContextEvent );
    }


    @Override
    public void contextDestroyed( ServletContextEvent servletContextEvent ) {
        super.contextDestroyed( servletContextEvent );
    }
}
