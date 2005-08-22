package org.objectweb.proactive.core.component.interception;

import java.lang.reflect.Method;


/**
 * This interface must be implemented by controllers that need to intercept
 * incoming functional invocations. <br>
 * Before executing (in the case of a primitive component) or transferring (in
 * the case of a composite component) a functional request, the
 * <code> beforeInputMethodInvocation  </code> method is called, and the
 * <code> afterInputMethodInvocation  </code> is called after the execution or
 * transfer of the invocation. <br>
 * These methods are executed on the controllers of the current component that
 * implement this interface. <br>
 * The <code> beforeInputMethodInvocation  </code> method is called sequentially
 * for each controller in the order they are defined in the controllers
 * configuration file. <br>
 * The <code> afterInputMethodInvocation  </code> method is called sequentially
 * for each controller in the <b> reverse order </b> they are defined in the
 * controllers configuration file. <br>
 * Example : <br>
 * if in the controller config file, the list of input interceptors is in this
 * order (the order in the controller config file is from top to bottom) : <br>
 * <code> InputInterceptor1  </code> <br>
 * <code> InputInterceptor2  </code> <br>
 * This means that an invocation on a server interface will follow this path :
 * <br>
 * <code> --> caller  </code> <br>
 * <code> --> InputInterceptor1.beforeInputMethodInvocation  </code> <br>
 * <code> --> InputInterceptor2.beforeInputMethodInvocation  </code> <br>
 * <code> --> callee.invocation  </code> <br>
 * <code> --> InputInterceptor2.afterInputMethodInvocation  </code> <br>
 * <code> --> InputInterceptor1.afterInputMethodInvocation  </code>
 *
 * @author Matthieu Morel
 */
public interface InputInterceptor {

    /**
     * This method is when an input invocation is intercepted, before executing the input invocation.
     * @param method the method to be executed
     * @param args the parameters of the method
     */
    public void beforeInputMethodInvocation(Method method, Object[] args);

    /**
     * This method is when an input invocation has been intercepted, after the execution of the input invocation.
     * @param method the method that has been executed
     * @param args the parameters of the method
     */
    public void afterInputMethodInvocation(Method method, Object[] args);
}
