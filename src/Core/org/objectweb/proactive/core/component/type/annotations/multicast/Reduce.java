package org.objectweb.proactive.core.component.type.annotations.multicast;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Reduce {

    //	String closureCode();

    ReduceMode reductionMode();

    Class<?> customReductionMode() default ReduceMode.class;

}
