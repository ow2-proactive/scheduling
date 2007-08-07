package org.objectweb.proactive.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
/**
 * this annotation indicates that in tte stub generated for the method, caching must be turn on
 *
 */
public @interface Cache {
}
