package org.objectweb.proactive.core.component.type.annotations.gathercast;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>Annotation for specifying timeouts at the level of interfaces.</p>
 * <p>Timeouts are only handled for invocations of methods that return a result</b>
 * <p>When a timeout is detected, the default behavior is to throw a GathercastTimeoutException to the clients.</p>
 * 
 * @author Matthieu Morel
 *
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ItfSynchro {
    
	/**
	 * 
	 * @return the timeout in seconds
	 */
    long timeout() default 0;
    
//    /**
//     * experimental
//     */
//    Class synchroVisitor();

}
