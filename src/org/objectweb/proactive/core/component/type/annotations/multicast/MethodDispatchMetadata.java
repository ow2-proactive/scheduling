package org.objectweb.proactive.core.component.type.annotations.multicast;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotates a method: it specifies the dispatch mode applicable for <b>all</b>
 * parameters of the method
 * 
 * @author Matthieu Morel
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface MethodDispatchMetadata {
    
	/**
	 * specifies the dispatch mode
	 * @return the dispatch mode
	 */
    ParamDispatchMetadata mode();
        

}
