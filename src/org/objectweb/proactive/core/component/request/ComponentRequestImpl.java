/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2004 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive-support@inria.fr
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.core.component.request;

import org.apache.log4j.Logger;

import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.LifeCycleController;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.future.FutureResult;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.body.request.RequestImpl;
import org.objectweb.proactive.core.body.request.ServeException;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.Fractive;
import org.objectweb.proactive.core.component.ProActiveInterface;
import org.objectweb.proactive.core.component.body.ComponentBodyImpl;
import org.objectweb.proactive.core.component.controller.ComponentParametersController;
import org.objectweb.proactive.core.component.identity.ProActiveComponentImpl;
import org.objectweb.proactive.core.component.interception.Interceptor;
import org.objectweb.proactive.core.mop.MethodCall;
import org.objectweb.proactive.core.mop.MethodCallExecutionFailedException;
import org.objectweb.proactive.core.mop.StubObject;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;

import java.io.IOException;
import java.io.Serializable;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;


/**
 * Method calls to components are actually reified calls, and ComponentRequest
 * contains a reification of the call.
 * <p>
 * This class handles the tagging of the call (a component call), and the
 * redispatching to the targeted component metaobject, interface reference, base
 * object. It also allows pre and post processing of functional invocations with
 * interceptors.
 *
 * @author Matthieu Morel
 */
public class ComponentRequestImpl extends RequestImpl
    implements ComponentRequest, Serializable {
    protected static Logger logger = ProActiveLogger.getLogger(Loggers.COMPONENTS_REQUEST);

    //private int shortcutCounter = 0;
    //private Shortcut shortcut;
    private final Class declaringClass;
    private final Class targetClass;

    // priorities for NF requests (notably when using filters on functional requests) : 
    //private short priority=ComponentRequest.STRICT_FIFO_PRIORITY;
    public ComponentRequestImpl(MethodCall methodCall, UniversalBody sender,
        boolean isOneWay, long nextSequenceID) {
        super(methodCall, sender, isOneWay, nextSequenceID);
        declaringClass = methodCall.getReifiedMethod().getDeclaringClass();
        targetClass = methodCall.getReifiedMethod().getDeclaringClass();
    }

    public ComponentRequestImpl(Request request) {
        super(request.getMethodCall(), request.getSender(), request.isOneWay(),
            request.getSequenceNumber());
        declaringClass = methodCall.getReifiedMethod().getDeclaringClass();
        targetClass = methodCall.getReifiedMethod().getDeclaringClass();
    }

    /**
     * redirects the call to the adequate component metaobject : either to a controller, through the chain of controllers, to
     * a functional interface in the case of a composite (no preprocessing in that case), or directly executes the invocation
     * on the base object if this component is a primitive component and the invocation is a functional invocation.
     */
    protected FutureResult serveInternal(Body targetBody)
        throws ServeException {
        Object result = null;
        Throwable exception = null;

        try {
            if (isControllerRequest()) {
                result = ((ProActiveComponentImpl) ((ComponentBodyImpl) targetBody)
                          .getProActiveComponent()).getControllerRequestHandler()
                          .handleRequest(this);
            } else {
                if (((ComponentBodyImpl) targetBody).getProActiveComponent() != null) {
                    interceptBeforeInvocation(targetBody);

                    String hierarchical_type = Fractive.getComponentParametersController(((ComponentBodyImpl) targetBody).getProActiveComponent())
                                                       .getComponentParameters()
                                                       .getHierarchicalType();

                    // if the component is a composite OR A PARALLEL , forward to functional interface 
                    if (hierarchical_type.equals(Constants.COMPOSITE) ||
                            hierarchical_type.equals(Constants.PARALLEL)) {
                        //						// forward to functional interface whose name is given as a parameter in the method call
                        if (logger.isDebugEnabled()) {
                            logger.debug("forwarding an invocation on method [" +
                                methodCall.getComponentInterfaceName() +
                                "] to : " +
                                ((ComponentParametersController) ((ComponentBodyImpl) targetBody)
                                 .getProActiveComponent().getFcInterface(Constants.COMPONENT_PARAMETERS_CONTROLLER)).getComponentParameters()
                                 .getName());
                        }
                        try {
                            if (getShortcut() != null) {
                                // TODO_M allow stopping shortcut here
                            }
                            result = ((StubObject) ((ProActiveInterface) (((ComponentBodyImpl) targetBody)
                                      .getProActiveComponent())
                                      .getFcInterface(methodCall.getComponentInterfaceName()))
                                      .getFcItfImpl()).getProxy().reify(methodCall);
                        } catch (IllegalArgumentException e) {
                            e.printStackTrace();
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                    } else {
                        // the component is a primitive
                        // directly execute the method on the active object
                        if (logger.isDebugEnabled()) {
                            logger.debug("executing the method [" +
                                methodCall.getComponentInterfaceName() +
                                "] on a primitive component");

                            if (getShortcutLength() > 0) {
                                logger.debug("request has crossed " +
                                    (getShortcutLength() - 1) +
                                    " membranes before reaching a primitive component");
                            }
                        }
                        result = methodCall.execute(targetBody.getReifiedObject());
                    }
                    interceptAfterInvocation(targetBody);
                } else {
                    throw new ServeException(
                        "trying to execute a component method on an object that is not a component");
                }
            }
        } catch (NoSuchInterfaceException nsie) {
            nsie.printStackTrace();
            throw new ServeException("cannot serve request : problem accessing a component controller",
                nsie);
        } catch (MethodCallExecutionFailedException e) {
            e.printStackTrace();
            throw new ServeException("serve method " +
                methodCall.getReifiedMethod().toString() + " failed", e);
        } catch (java.lang.reflect.InvocationTargetException e) {
            exception = e.getTargetException();

            if (isOneWay) {
                throw new ServeException("serve method " +
                    methodCall.getReifiedMethod().toString() + " failed",
                    exception);
            }
        }

        return new FutureResult(result, exception, null);
    }

    // intercept and delegate for preprocessing from the interceptors 
    private void interceptBeforeInvocation(Body targetBody) {
        List interceptors = ((ComponentBodyImpl) targetBody).getProActiveComponent()
                             .getInterceptors();
        Iterator it = interceptors.iterator();
        while (it.hasNext()) {
            ((Interceptor) it.next()).beforeMethodInvocation();
        }
    }

    // intercept and delegate for postprocessing from the interceptors 
    private void interceptAfterInvocation(Body targetBody) {
        if (((ComponentBodyImpl) targetBody).getProActiveComponent() != null) {
            List interceptors = ((ComponentBodyImpl) targetBody).getProActiveComponent()
                                 .getInterceptors();

            // use interceptors in reverse order after invocation
            ListIterator it = interceptors.listIterator();

            // go to the end of the list first
            while (it.hasNext()) {
                it.next();
            }
            while (it.hasPrevious()) {
                ((Interceptor) it.previous()).afterMethodInvocation();
            }
        }
    }

    /*
     * @see org.objectweb.proactive.core.component.request.ComponentRequest#isControllerRequest()
     */
    public boolean isControllerRequest() {
        return methodCall.isComponentControllerMethodCall();
    }

    /*
     * @see org.objectweb.proactive.core.component.request.ComponentRequest#isStopFcRequest()
     */
    public boolean isStopFcRequest() {
        return (declaringClass.equals(LifeCycleController.class) &&
        "stopFc".equals(getMethodName()));
    }

    /*
     * @see org.objectweb.proactive.core.component.request.ComponentRequest#isStartFcRequest()
     */
    public boolean isStartFcRequest() {
        return (declaringClass.equals(LifeCycleController.class) &&
        "startFc".equals(getMethodName()));
    }

    public void notifyReception(UniversalBody bodyReceiver)
        throws IOException {
        if (getShortcut() != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("notifying reception of method " +
                    methodCall.getName());
            }
            Shortcut shortcut = getShortcut();
            shortcut.updateDestination(bodyReceiver.getRemoteAdapter());
            shortcut.getSender().createShortcut(shortcut);

            ((ComponentBodyImpl) bodyReceiver).keepShortcut(shortcut);
        }
        super.notifyReception(bodyReceiver);
    }

    public void shortcutNotification(UniversalBody sender,
        UniversalBody intermediate) {
        methodCall.shortcutNotification(sender, intermediate);
    }

    public Shortcut getShortcut() {
        return methodCall.getShortcut();
    }

    public int getShortcutLength() {
        return ((getShortcut() == null) ? 0 : getShortcut().length());
    }

    /*
     * @see org.objectweb.proactive.core.component.request.ComponentRequest#getNFPriority()
     */
    public short getPriority() {
        return methodCall.getPriority();
    }

    /*
     * @see org.objectweb.proactive.core.component.request.ComponentRequest#getTargetClass()
     */
    public Class getTargetClass() {
        return targetClass;
    }
}
