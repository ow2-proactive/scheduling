package org.objectweb.proactive.core.component.interception;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.Interface;
import org.objectweb.fractal.api.type.InterfaceType;

import org.objectweb.proactive.core.component.ProActiveInterface;
import org.objectweb.proactive.core.mop.StubObject;

import java.io.Serializable;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 *
 * This class is a factory for adding output interceptors before output invocations from components.
 * The mechanism is based on java 1.3 dynamic proxies.
 *
 *
 *
 * @author Matthieu Morel
 *
 */
public class ProActiveOutputInterfaceInterceptor implements InvocationHandler {
    private ProActiveInterface delegatee;
    private static List nonFunctionalInterfaces;
    private List outputInterceptors;

    static {
        nonFunctionalInterfaces = new ArrayList();
        nonFunctionalInterfaces.add(StubObject.class.getName());
        nonFunctionalInterfaces.add(ProActiveInterface.class.getName());
        nonFunctionalInterfaces.add(Interface.class.getName());
        nonFunctionalInterfaces.add(Component.class.getName());
        nonFunctionalInterfaces.add(Serializable.class.getName());
    }

    /**
     * This method creates a new proxy before the given interface. The output interception takes place in this proxy.
     * @param itf a serverInterface
     * @param outputInterceptors the list of output interceptors
     * @return a proxied interface to the given server interface.
     * @throws ClassNotFoundException if the class corresponding to the signature of the server interface cannot be found.
     */
    public static ProActiveInterface newInstance(ProActiveInterface itf,
        List outputInterceptors) throws ClassNotFoundException {
        // get implemented interfaces and add ProActiveInterface
        Class[] implemented = new Class[] {
                ProActiveInterface.class,
                Class.forName(((InterfaceType) itf.getFcItfType()).getFcItfSignature())
            };
        ProActiveInterface result = (ProActiveInterface) Proxy.newProxyInstance(itf.getClass()
                                                                                   .getClassLoader(),
                implemented,
                new ProActiveOutputInterfaceInterceptor(itf, outputInterceptors));
        return result;
    }

    private ProActiveOutputInterfaceInterceptor(Object itf,
        List outputInterceptors) {
        delegatee = (ProActiveInterface) itf;
        this.outputInterceptors = outputInterceptors;
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
                if (!outputInterceptors.isEmpty()) {
                    Iterator it = outputInterceptors.iterator();
                    while (it.hasNext()) {
                        OutputInterceptor interceptor = (OutputInterceptor) it.next();
                        interceptor.beforeOutputMethodInvocation(method, args);
                    }
                }
            }
            result = method.invoke(delegatee, args);
            if (!nonFunctionalInterfaces.contains(method.getDeclaringClass()
                                                            .getName())) {
                if (!outputInterceptors.isEmpty()) {
                    Iterator it = outputInterceptors.iterator();
                    while (it.hasNext()) {
                        OutputInterceptor interceptor = (OutputInterceptor) it.next();
                        interceptor.afterOutputMethodInvocation(method, args);
                    }
                }
            }
        } catch (Exception e) {
            throw e;
        } finally {
            System.out.println("after method " + method.getName());
        }
        return result;
    }

    //    private void beforeMethodInvocation(Method method, Object[] args)
    //        throws Throwable {
    //        try {
    //            if (!(LifeCycleController.STOPPED).equals(
    //                        Fractal.getLifeCycleController(componentIdentity)
    //                                   .getFcState())) {
    //                throw new IllegalLifeCycleException("cannot invoke method " +
    //                    method.getName() + " when component is not stopped");
    //            }
    //        } catch (NoSuchInterfaceException e) {
    //            // no life cycle controller? 
    //            // TODO log me!
    //        }
    //    }
}
