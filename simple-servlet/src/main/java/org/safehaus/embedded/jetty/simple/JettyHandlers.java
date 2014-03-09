package org.safehaus.embedded.jetty.simple;


import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * An annotation containing various directives to configure JettyHandlers.
 */
@Retention( RetentionPolicy.RUNTIME )
@Target( ElementType.FIELD )
@Inherited
public @interface JettyHandlers {
    ServletMapping[] servletMappings();
    FilterMapping[] filterMappings();
}
