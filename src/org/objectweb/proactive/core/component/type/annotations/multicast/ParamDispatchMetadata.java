package org.objectweb.proactive.core.component.type.annotations.multicast;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for specifying the dispatch strategy of a given parameter.
 * 
 * @author Matthieu Morel
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface ParamDispatchMetadata {
    
	/**
	 * Selection of dispatch strategy from the {@link ParamDispatchMode} enumeration.
	 * @return selected dispatch strategy
	 */
    ParamDispatchMode mode();
    
    /**
     * Used for specifying a custom dispatch strategy. Custom dispatch strategies are defined in classes that 
     * implement the {@link ParamDispatch} interface.
     * <br>
     * For a custom dispatch strategy to be specified, the ParamDispatchMode.CUSTOM value must be selected for
     * the {@link ParamDispatchMetadata#mode()} method.
     * @return a class defining the dispatch strategy
     */
    Class customMode() default ParamDispatchMode.class; 
    

}
