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
 * this annotation indicates that the method should not reify the call.
 * Must be used only when the method contains only one parameters that inherits from Message
 * in order to prevent a double reification of the method call
 */
public @interface NoReify {
}
