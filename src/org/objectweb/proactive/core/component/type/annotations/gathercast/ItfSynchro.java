package org.objectweb.proactive.core.component.type.annotations.gathercast;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ItfSynchro {
    
    long timeout() default 0;
    
    Class synchroVisitor();

}
