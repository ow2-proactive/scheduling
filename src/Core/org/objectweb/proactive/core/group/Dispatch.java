package org.objectweb.proactive.core.group;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


/**
 * An annotation for specifying the dispatch operation.
 * 
 * @author The ProActive Team
 *
 */

@Retention(RetentionPolicy.RUNTIME)
public @interface Dispatch {

    DispatchMode mode();

    Class<?> customMode() default DispatchMode.class;

    int bufferSize() default 1;

    //    String context() default "";

}
