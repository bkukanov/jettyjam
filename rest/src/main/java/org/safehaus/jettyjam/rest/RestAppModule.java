/*
 * Created by IntelliJ IDEA.
 * User: akarasulu
 * Date: 11/22/13
 * Time: 11:44 PM
 */
package org.safehaus.jettyjam.rest;


import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.sun.jersey.guice.JerseyServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;


public class RestAppModule extends JerseyServletModule {
    public static final String PACKAGES_KEY = "com.sun.jersey.config.property.packages";


    protected void configureServlets() {
        // Hook Jersey into Guice Servlet
        bind( GuiceContainer.class );

        // Hook Jackson into Jersey as the POJO <-> JSON mapper
        bind( JacksonJsonProvider.class ).asEagerSingleton();

        bind( FooResource.class ).asEagerSingleton();
        bind( UploadResource.class ).asEagerSingleton();

        Map<String, String> params = new HashMap<String, String>();
        params.put( PACKAGES_KEY, getClass().getPackage().toString() );
        serve( "/*" ).with( GuiceContainer.class, params );
    }
}
