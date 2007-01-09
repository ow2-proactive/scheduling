package org.objectweb.proactive.core.component.type.annotations.gathercast;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>Annotation for specifying timeouts at the level of methods.</p>
 * <p>Refer to {@link ItfSynchro} annotation for more information on timeouts.</p>
 * 
 * @author Matthieu Morel
 *
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MethodSynchro {
    
	/**
	 * @return the timeout in seconds
	 */
    long timeout() default 0;

}
