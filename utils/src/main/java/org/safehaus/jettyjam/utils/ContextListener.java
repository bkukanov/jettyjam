package org.safehaus.jettyjam.utils;


import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.servlet.ServletContextListener;


/**
 * A configuration annotation to add a context Listener to Jetty.
 */
@Retention( RetentionPolicy.RUNTIME )
@Target( { ElementType.FIELD, ElementType.TYPE } )
@Inherited
public @interface ContextListener {
    Class<? extends ServletContextListener> listener();
    String contextPath() default "/";
}
