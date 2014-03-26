package org.safehaus.jettyjam.utils;


import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * An HTTPS connector configuration annotation for Jetty.
 */
@Retention( RetentionPolicy.RUNTIME )
@Target( { ElementType.FIELD, ElementType.TYPE } )
@Inherited
public @interface HttpsConnector {
    String id();
    int port() default 0;
    String keyStore() default "keystore.jks";
    String ksPassword() default "123456";
    String kmPassword() default "123456";
    boolean generate() default true;
}
