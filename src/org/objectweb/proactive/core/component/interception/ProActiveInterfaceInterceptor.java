package org.objectweb.proactive.core.component.interception;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.api.control.LifeCycleController;
import org.objectweb.fractal.util.Fractal;

import org.objectweb.proactive.core.component.ProActiveInterface;
import org.objectweb.proactive.core.mop.StubObject;

import java.io.Serializable;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import java.util.ArrayList;
import java.util.List;


/**
 * TODO optimize me ? (or generate class instead of dynamic proxy)
 *
 * a draft interceptor for outgoing invocations
 * 
 *  
 * @author Matthieu Morel
 *
 */
public class ProActiveInterfaceInterceptor implements InvocationHandler {
    private ProActiveInterface delegatee;
    private Component componentIdentity;
    private static List nonFunctionalInterfaces;

    static {
        nonFunctionalInterfaces = new ArrayList();
        nonFunctionalInterfaces.add(StubObject.class.getName());
        nonFunctionalInterfaces.add(ProActiveInterface.class.getName());
        nonFunctionalInterfaces.add(Serializable.class.getName());
    }

    public static Object newInstance(Object itf, Component componentIdentity) {
        // get implemented interfaces and add ProActiveInterface
        Class[] implemented = new Class[itf.getClass().getInterfaces().length +
            1];
        Class[] tmp = itf.getClass().getInterfaces();
        implemented[0] = ProActiveInterface.class;
        for (int i = 1; i < implemented.length; i++) {
            implemented[i] = tmp[i - 1];
        }
        Object result = Proxy.newProxyInstance(itf.getClass().getClassLoader(),
                implemented,
                new ProActiveInterfaceInterceptor(itf, componentIdentity));
        return result;
    }

    private ProActiveInterfaceInterceptor(Object itf,
        Component componentIdentity) {
        this.componentIdentity = componentIdentity;
        delegatee = (ProActiveInterface) itf;
    }

    /* (non-Javadoc)
     * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
     */
    public Object invoke(Object proxy, Method method, Object[] args)
        throws Throwable {
        Object result;
        try {
            if (!nonFunctionalInterfaces.contains(method.getDeclaringClass()
                                                            .getName())) {
                beforeMethodInvocation(method, args);
            }
            result = method.invoke(delegatee, args);
        } catch (Exception e) {
            throw e;
        } finally {
            System.out.println("after method " + method.getName());
        }
        return result;
    }

    private void beforeMethodInvocation(Method method, Object[] args)
        throws Throwable {
        try {
            if (!(LifeCycleController.STOPPED).equals(
                        Fractal.getLifeCycleController(componentIdentity)
                                   .getFcState())) {
                throw new IllegalLifeCycleException("cannot invoke method " +
                    method.getName() + " when component is not stopped");
            }
        } catch (NoSuchInterfaceException e) {
            // no life cycle controller? 
            // TODO log me!
        }
    }
}
