package org.safehaus.jettyjam.utils;


import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * An HttpConnector configuration annotation for Jetty.
 */
@Retention( RetentionPolicy.RUNTIME )
@Target( { ElementType.FIELD, ElementType.TYPE } )
@Inherited
public @interface HttpConnector {
    String id();
    int port() default 0;
}
