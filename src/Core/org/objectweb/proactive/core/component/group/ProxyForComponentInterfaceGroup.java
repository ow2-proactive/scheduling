/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.core.component.group;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.Interface;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.component.collectiveitfs.MulticastHelper;
import org.objectweb.proactive.core.component.exceptions.ParameterDispatchException;
import org.objectweb.proactive.core.component.identity.ProActiveComponent;
import org.objectweb.proactive.core.component.type.ProActiveInterfaceType;
import org.objectweb.proactive.core.component.type.ProActiveInterfaceTypeImpl;
import org.objectweb.proactive.core.group.AbstractProcessForGroup;
import org.objectweb.proactive.core.group.Dispatch;
import org.objectweb.proactive.core.group.Dispatcher;
import org.objectweb.proactive.core.group.ExceptionListException;
import org.objectweb.proactive.core.group.ProxyForGroup;
import org.objectweb.proactive.core.group.TaskFactoryFactory;
import org.objectweb.proactive.core.mop.ClassNotReifiableException;
import org.objectweb.proactive.core.mop.ConstructionOfReifiedObjectFailedException;
import org.objectweb.proactive.core.mop.ConstructorCall;
import org.objectweb.proactive.core.mop.MethodCall;
import org.objectweb.proactive.core.mop.StubObject;


/**
 * An extension of the standard group proxy for handling groups of component
 * interfaces.
 * 
 * @author The ProActive Team
 * 
 */
public class ProxyForComponentInterfaceGroup<E> extends ProxyForGroup<E> {

    protected ProActiveInterfaceType interfaceType;
    protected Class<?> itfSignatureClass = null;
    protected ProActiveComponent owner;
    protected ProxyForComponentInterfaceGroup<E> delegatee = null;
    protected ProxyForComponentInterfaceGroup parent = null; // for a delegatee

    public ProxyForComponentInterfaceGroup() throws ConstructionOfReifiedObjectFailedException {
        super();
        className = Interface.class.getName();
    }

    public ProxyForComponentInterfaceGroup(ConstructorCall c, Object[] p)
            throws ConstructionOfReifiedObjectFailedException {
        super(c, p);
        className = Interface.class.getName();
    }

    public ProxyForComponentInterfaceGroup(String nameOfClass)
            throws ConstructionOfReifiedObjectFailedException {
        this();
        className = Interface.class.getName();
    }

    /**
     * @return Returns the interfaceType.
     */
    public ProActiveInterfaceType getInterfaceType() {
        return interfaceType;
    }

    /*
     * @see org.objectweb.proactive.core.group.ProxyForGroup#reify(org.objectweb.proactive.core.mop.MethodCall)
     */
    @Override
    public synchronized Object reify(MethodCall mc) throws InvocationTargetException {
        if (delegatee != null) {
            // check
            if (itfSignatureClass.equals(mc.getReifiedMethod().getDeclaringClass())) {
                // nothing to do
            } else if (mc.getReifiedMethod().getDeclaringClass().isAssignableFrom(itfSignatureClass)) {
                // need to adapt method call
                Method adaptedMethod;
                try {
                    // TODO optimize (avoid reflective calls!)
                    adaptedMethod = itfSignatureClass.getMethod(mc.getReifiedMethod().getName(), mc
                            .getReifiedMethod().getParameterTypes());
                } catch (SecurityException e) {
                    throw new InvocationTargetException(e,
                        "could not adapt client interface to multicast server interface " +
                            interfaceType.getFcItfName());
                } catch (NoSuchMethodException e) {
                    throw new InvocationTargetException(e,
                        "could not adapt client interface to multicast server interface " +
                            interfaceType.getFcItfName());
                }
                mc = MethodCall.getComponentMethodCall(adaptedMethod, mc.getEffectiveArguments(), mc
                        .getGenericTypesMapping(), mc.getComponentMetadata().getComponentInterfaceName(), mc
                        .getComponentMetadata().getSenderItfID(), mc.getComponentMetadata().getPriority());
            } else {
                throw new InvocationTargetException(null, "method " + mc.getName() + " defined in " +
                    mc.getReifiedMethod().getDeclaringClass() + " cannot be invoked on " +
                    itfSignatureClass.getName());
            }

            return delegatee.reify(mc);
            // System.out.println("delegatee.memberList.size();" +
            // delegatee.memberList.size());
        } else {

            // super.memberList = delegatee.memberList;
            return super.reify(mc);
        }
    }

    /*
     * @see org.objectweb.proactive.core.group.Group#getGroupByType()
     */
    @Override
    public Object getGroupByType() {
        try {
            Interface result = ProActiveComponentGroup.newComponentInterfaceGroup(interfaceType, owner);

            @SuppressWarnings("unchecked")
            ProxyForComponentInterfaceGroup<E> proxy = (ProxyForComponentInterfaceGroup<E>) ((StubObject) result)
                    .getProxy();
            proxy.memberList = this.memberList;
            proxy.className = this.className;
            proxy.interfaceType = this.interfaceType;
            proxy.owner = this.owner;
            proxy.proxyForGroupID = this.proxyForGroupID;
            proxy.waited = this.waited;
            return result;
        } catch (ClassNotReifiableException e) {
            e.printStackTrace();
            return null;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    /*
     * @see org.objectweb.proactive.core.group.ProxyForGroup#oneWayCallOnGroup(org.objectweb.proactive.core.mop.MethodCall,
     *      org.objectweb.proactive.core.group.ExceptionListException)
     */
    @Override
    protected void oneWayCallOnGroup(MethodCall mc, ExceptionListException exceptionList)
            throws InvocationTargetException {
        if (exceptionList == null) {
            Thread.dumpStack();
        }
        if (((ProActiveInterfaceTypeImpl) interfaceType).isFcCollective() && (delegatee != null)) {
            // // 2. generate adapted method calls depending on nb members and
            // parameters distribution
            // // each method call is assigned a given member index

            List<MethodCall> methodsToDispatch;

            try {
                methodsToDispatch = MulticastHelper.generateMethodCallsForMulticastDelegatee(owner, mc,
                        delegatee);
            } catch (ParameterDispatchException e) {
                throw new InvocationTargetException(e, "cannot dispatch invocation parameters for method " +
                    mc.getReifiedMethod().toString() + " from collective interface " +
                    interfaceType.getFcItfName());
            }

            int nbExpectedCalls = methodsToDispatch.size();
            CountDownLatch doneSignal = new CountDownLatch(nbExpectedCalls);
            Queue<AbstractProcessForGroup> tasksToDispatch = taskFactory.generateTasks(mc, methodsToDispatch,
                    null, exceptionList, doneSignal, this);

            dispatcher.dispatchTasks(tasksToDispatch, doneSignal, mc.getReifiedMethod().getAnnotation(
                    Dispatch.class));

            // LocalBodyStore.getInstance().setCurrentThreadBody(ProActive.getBodyOnThis());
        } else {
            super.oneWayCallOnGroup(mc, exceptionList);
        }
    }

    /**
     * The delegatee introduces an indirection which can be used for altering
     * the reified invocation
     * 
     */
    public void setDelegatee(ProxyForComponentInterfaceGroup<E> delegatee) {
        this.delegatee = delegatee;
        delegatee.setOwner(this.owner);
        delegatee.setParent(this);
        // replace dispatcher and task factory so that they use this delegatee
        dispatcher = new Dispatcher(delegatee, false, dispatcher.getBufferSize());
        taskFactory = TaskFactoryFactory.getTaskFactory(delegatee);

    }

    public void setParent(ProxyForComponentInterfaceGroup parent) {
        this.parent = parent;
    }

    public ProxyForComponentInterfaceGroup getParent() {
        return parent;
    }

    /**
     * The delegatee introduces an indirection which can be used for altering
     * the reified invocation
     * 
     */
    public ProxyForComponentInterfaceGroup<E> getDelegatee() {
        return delegatee;
    }

    /*
     * @see org.objectweb.proactive.core.group.ProxyForGroup#size()
     */
    @Override
    public int size() {
        if (getDelegatee() != null) {
            return getDelegatee().size();
        }
        return super.size();
    }

    /**
     * @return Returns the owner.
     */
    public Component getOwner() {
        return owner;
    }

    /**
     * @param owner
     *            The owner to set.
     */
    public void setOwner(Component owner) {
        this.owner = (ProActiveComponent) owner;
    }

    /**
     * @param interfaceType
     *            The interfaceType to set.
     */
    public void setInterfaceType(ProActiveInterfaceType interfaceType) {
        this.interfaceType = interfaceType;
        try {
            itfSignatureClass = Class.forName(interfaceType.getFcItfSignature());
        } catch (ClassNotFoundException e) {
            throw new ProActiveRuntimeException("cannot find Java interface " +
                interfaceType.getFcItfSignature() + " defined in interface named " +
                interfaceType.getFcItfName(), e);
        }
    }

    //	@Override
    //	public Vector getMemberList() {
    //		if (delegatee != null) {
    //			return delegatee.getMemberList();
    //		} else {
    //			return super.getMemberList();
    //		}
    //	}

}
