package org.safehaus.embedded.jetty.utils;


import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * A Connector annotations for configuring Jetty connectors.
 */
@Retention( RetentionPolicy.RUNTIME )
@Target( { ElementType.FIELD, ElementType.TYPE } )
@Inherited
public @interface JettyConnectors {
    HttpConnector[] httpConnectors();
    HttpsConnector[] httpsConnectors();
    String defaultId();
}
