package org.objectweb.proactive.core.component.interception;

import java.lang.reflect.Method;

/**
 * This interface must be implemented by controllers that need to intercept
 * outgoing functional invocations. <br>
 * Before executing (in the case of a primitive component) or transferring (in
 * the case of a composite component) an outgoing functional request, the
 * <code> beforeOutputMethodInvocation  </code> method is called, and the
 * <code> afterOutputMethodInvocation  </code> is called after the execution or
 * transfer of the invocation. <br>
 * These methods are executed on the controllers of the current component that
 * implement this interface. <br>
 * The <code>beforeOutputMethodInvocation </code> method is called sequentially
 * for each controller in the order they are defined in the controllers
 * configuration file. <br>
 * The <code>afterOutputMethodInvocation</code> method is called sequentially
 * for each controller in the <b> reverse order </b> they are defined in the
 * controllers configuration file. <br>
 * Example : <br>
 * if in the controller config file, the list of input interceptors is in this
 * order (the order in the controller config file is from top to bottom) : <br>
 * <code> OutputInterceptor1  </code> <br>
 * <code> OutputInterceptor2  </code> <br>
 * This means that an invocation on a server interface will follow this path :
 * <br>
 * <code> --> currentComponent  </code> <br>
 * <code> --> OutputInterceptor1.beforeOutputMethodInvocation  </code> <br>
 * <code> --> OutputInterceptor2.beforeOutputMethodInvocation  </code> <br>
 * <code> --> callee.invocation  </code> <br>
 * <code> --> OutputInterceptor2.afterOutputMethodInvocation  </code> <br>
 * <code> --> OutputInterceptor1.afterOutputMethodInvocation  </code>
 * 
 * @author Matthieu Morel
 */

public interface OutputInterceptor {
    
    public void beforeOutputMethodInvocation(Method method, Object[] args);
    
    public void afterOutputMethodInvocation(Method method, Object[] args);


}
