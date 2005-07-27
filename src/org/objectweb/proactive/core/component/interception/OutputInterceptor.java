package org.objectweb.proactive.core.component.interception;

import java.lang.reflect.Method;

public interface OutputInterceptor {
    
    public void beforeOutputMethodInvocation(Method method, Object[] args);
    
    public void afterOutputMethodInvocation(Method method, Object[] args);


}
