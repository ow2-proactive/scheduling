package org.objectweb.proactive.core.component.type.annotations.multicast;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface ParamDispatchMetadata {
    
    ParamDispatchMode mode();
    
    Class customMode() default ParamDispatchMode.class; 
    

}
