package org.objectweb.proactive.core.component.interception;

import java.lang.reflect.Method;

public interface InputInterceptor {
    
    public void beforeInputMethodInvocation(Method method, Object[] args);
    
    public void afterInputMethodInvocation(Method method, Object[] args);
    
    

}
