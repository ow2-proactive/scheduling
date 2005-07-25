package org.objectweb.proactive.core.component.interception;

import org.objectweb.proactive.core.component.controller.ProActiveController;
import org.objectweb.proactive.core.mop.MethodCall;

/**
 * This interface must be implemented by controllers that need to intercept
 * incoming functional invocations. <br>
 * Before executing (in the case of a primitive component) or transferring (in
 * the case of a composite component) a functional request, the
 * <code> beforeMethodInvocation()  </code> method is called, and the
 * <code> afterMethodInvocation()  </code> is called after the execution or
 * transfer of the invocation. <br>
 * These methods are executed on the controllers of the current component that
 * implement this interface. <br>
 * The <code> beforeMethodInvocation()  </code> method is called sequentially for
 * each controller in the order they are defined in the controllers
 * configuration file. <br>
 * The <code> afterMethodInvocation()  </code> method is called sequentially for
 * each controller in the <b> reverse order </b> they are defined in the
 * controllers configuration file.
 * 
 * @author Matthieu Morel
 */
public interface Interceptor extends ProActiveController {
    
    public void beforeMethodInvocation(MethodCall methodCall);
    
    public void afterMethodInvocation(MethodCall methodCall);

}
