package org.objectweb.proactive.core.component.type.annotations.multicast;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used for specifying the dispatch mode for all parameters of all methods of the annotated interface
 *  
 * @author Matthieu Morel
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ClassDispatchMetadata {
    
	/**
	 * Specifies the dispatch mode
	 * @return the dispatch mode of all the parameters
	 */
    ParamDispatchMetadata mode();
    

}
