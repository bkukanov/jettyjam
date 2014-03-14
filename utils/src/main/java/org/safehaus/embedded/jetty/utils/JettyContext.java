package org.safehaus.embedded.jetty.utils;


import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * An annotation containing various directives to configure JettyContext.
 */
@Retention( RetentionPolicy.RUNTIME )
@Target( { ElementType.FIELD, ElementType.TYPE } )
@Inherited
public @interface JettyContext {
    String contextRoot() default "/";
    ServletMapping[] servletMappings() default {};
    FilterMapping[] filterMappings() default {};
    ContextListener[] contextListeners() default {};
}
