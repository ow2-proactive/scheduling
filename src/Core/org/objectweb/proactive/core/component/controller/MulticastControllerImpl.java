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
package org.objectweb.proactive.core.component.controller;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.api.factory.InstantiationException;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.fractal.api.type.TypeFactory;
import org.objectweb.fractal.util.Fractal;
import org.objectweb.proactive.api.ProGroup;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.Fractive;
import org.objectweb.proactive.core.component.ProActiveInterface;
import org.objectweb.proactive.core.component.collectiveitfs.MulticastBindingChecker;
import org.objectweb.proactive.core.component.exceptions.ParameterDispatchException;
import org.objectweb.proactive.core.component.group.ProxyForComponentInterfaceGroup;
import org.objectweb.proactive.core.component.type.ProActiveInterfaceType;
import org.objectweb.proactive.core.component.type.ProActiveTypeFactoryImpl;
import org.objectweb.proactive.core.component.type.annotations.multicast.ParamDispatch;
import org.objectweb.proactive.core.group.Group;
import org.objectweb.proactive.core.group.ProActiveComponentGroup;
import org.objectweb.proactive.core.mop.ClassNotReifiableException;
import org.objectweb.proactive.core.mop.MethodCall;
import org.objectweb.proactive.core.mop.Proxy;
import org.objectweb.proactive.core.mop.StubObject;
import org.objectweb.proactive.core.util.SerializableMethod;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


public class MulticastControllerImpl extends AbstractProActiveController
    implements MulticastController, Serializable {
    private static Logger logger = ProActiveLogger.getLogger(Loggers.COMPONENTS_CONTROLLERS);
    private static Logger multicastLogger = ProActiveLogger.getLogger(Loggers.COMPONENTS_MULTICAST);
    private Map<String, ProActiveInterface> multicastItfs = new HashMap<String, ProActiveInterface>();
    private Map clientSideProxies = new HashMap();
    private Map<String, Map<SerializableMethod, SerializableMethod>> matchingMethods =
        new HashMap<String, Map<SerializableMethod, SerializableMethod>>();

    public MulticastControllerImpl(Component owner) {
        super(owner);
    }

    @Override
    public void init() {
        // this method is called once the component is fully instantiated with all its interfaces created
        InterfaceType[] itfTypes = ((ComponentType) owner.getFcType()).getFcInterfaceTypes();
        for (int i = 0; i < itfTypes.length; i++) {
            ProActiveInterfaceType type = (ProActiveInterfaceType) itfTypes[i];
            if (type.isFcMulticastItf()) {
                try {
                    addClientSideProxy(type.getFcItfName(),
                        (ProActiveInterface) owner.getFcInterface(
                            type.getFcItfName()));
                } catch (NoSuchInterfaceException e) {
                    throw new ProActiveRuntimeException(e);
                }
            }
        }
        List<InterfaceType> interfaceTypes = Arrays.asList(((ComponentType) owner.getFcType()).getFcInterfaceTypes());
        Iterator<InterfaceType> it = interfaceTypes.iterator();

        while (it.hasNext()) {
            // keep ref on interfaces of cardinality multicast
            addManagedInterface((ProActiveInterfaceType) it.next());
        }
    }

    /**
    * client and server interfaces must have the same methods, except that
    * the client methods always returns a java.util.List<E>, whereas
    * the server methods return E. (for multicast interfaces)
    * <br>
    *
    */
    public void ensureCompatibility(ProActiveInterfaceType clientSideItfType,
        ProActiveInterface serverSideItf) throws IllegalBindingException {
        try {
            ProActiveInterfaceType serverSideItfType = (ProActiveInterfaceType) serverSideItf.getFcItfType();
            Class clientSideItfClass;
            clientSideItfClass = Class.forName(clientSideItfType.getFcItfSignature());
            Class serverSideItfClass = Class.forName(serverSideItfType.getFcItfSignature());

            Method[] clientSideItfMethods = clientSideItfClass.getMethods();
            Method[] serverSideItfMethods = serverSideItfClass.getMethods();

            if (clientSideItfMethods.length != serverSideItfMethods.length) {
                throw new IllegalBindingException(
                    "incompatible binding between client interface " +
                    clientSideItfType.getFcItfName() + " (" +
                    clientSideItfType.getFcItfSignature() +
                    ")  and server interface " +
                    serverSideItfType.getFcItfName() + " (" +
                    serverSideItfType.getFcItfSignature() +
                    ") : there is not the same number of methods (including those inherited) in both interfaces !");
            }

            Map<SerializableMethod, SerializableMethod> matchingMethodsForThisItf =
                new HashMap<SerializableMethod, SerializableMethod>(clientSideItfMethods.length);

            for (Method method : clientSideItfMethods) {
                Method serverSideMatchingMethod = searchMatchingMethod(method,
                        serverSideItfMethods,
                        clientSideItfType.isFcMulticastItf(),
                        serverSideItfType.isFcGathercastItf(), serverSideItf);
                if (serverSideMatchingMethod == null) {
                    throw new IllegalBindingException(
                        "binding incompatibility between " +
                        clientSideItfType.getFcItfName() + " and " +
                        serverSideItfType.getFcItfName() +
                        " : cannot find matching method");
                }
                matchingMethodsForThisItf.put(new SerializableMethod(method),
                    new SerializableMethod(serverSideMatchingMethod));
            }

            matchingMethods.put(clientSideItfType.getFcItfName(),
                matchingMethodsForThisItf);
        } catch (ClassNotFoundException e) {
            throw new IllegalBindingException(
                "cannot find class corresponding to given signature " +
                e.getMessage());
        }
    }

    /*
     * @see org.objectweb.proactive.core.component.controller.AbstractCollectiveInterfaceController#searchMatchingMethod(java.lang.reflect.Method, java.lang.reflect.Method[])
     */
    protected Method searchMatchingMethod(Method clientSideMethod,
        Method[] serverSideMethods, boolean clientItfIsMulticast,
        boolean serverItfIsGathercast, ProActiveInterface serverSideItf) {
        try {
            return MulticastBindingChecker.searchMatchingMethod(clientSideMethod,
                serverSideMethods, serverItfIsGathercast, serverSideItf);
        } catch (ParameterDispatchException e) {
            e.printStackTrace();
            return null;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void setControllerItfType() {
        try {
            setItfType(ProActiveTypeFactoryImpl.instance()
                                               .createFcItfType(Constants.MULTICAST_CONTROLLER,
                    MulticastController.class.getName(), TypeFactory.SERVER,
                    TypeFactory.MANDATORY, TypeFactory.SINGLE));
        } catch (InstantiationException e) {
            throw new ProActiveRuntimeException(
                "cannot create controller type for controller " +
                this.getClass().getName());
        }
    }

    private boolean addManagedInterface(ProActiveInterfaceType itfType) {
        if (!itfType.isFcMulticastItf()) {
            return false;
        }
        if (multicastItfs.containsKey(itfType.getFcItfName())) {
            //            logger.error("the interface named " + itfType.getFcItfName() +
            //                " is already managed by the collective interfaces controller");
            return false;
        }

        try {
            ProActiveInterface multicastItf = (ProActiveInterface) owner.getFcInterface(itfType.getFcItfName());
            multicastItfs.put(itfType.getFcItfName(), multicastItf);
        } catch (NoSuchInterfaceException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /*
     * @see org.objectweb.proactive.core.component.controller.MulticastController#bindFc(java.lang.String, org.objectweb.proactive.core.component.ProActiveInterface)
     */
    public void bindFcMulticast(String clientItfName,
        ProActiveInterface serverItf) {
        try {
            // bindFcMulticast is just a renaming of the bindFc method in the BindingController
            // this avoid to rewrite similar code
            // the specific part is in the bindFc method in this class
            Fractive.getBindingController(owner).bindFc(clientItfName, serverItf);
        } catch (NoSuchInterfaceException e) {
            logger.warn("No such interface: " + clientItfName, e);
        } catch (IllegalBindingException e) {
            logger.warn("Illegal binding between " + clientItfName + " and " +
                serverItf.getFcItfName(), e);
        } catch (IllegalLifeCycleException e) {
            logger.warn("Illegal life cycle component for binding " +
                clientItfName + " and " + serverItf.getFcItfName(), e);
        }
    }

    /*
     * @see org.objectweb.proactive.core.component.controller.MulticastController#unbindFc(java.lang.String, org.objectweb.proactive.core.component.ProActiveInterface)
     */
    public void unbindFcMulticast(String clientItfName,
        ProActiveInterface serverItf) {
        if (multicastItfs.containsKey(clientItfName)) {
            if (ProGroup.getGroup(multicastItfs.get(clientItfName))
                                  .remove(serverItf)) {
                logger.debug(
                    "removed connected interface from multicast interface : " +
                    clientItfName);
            } else {
                logger.error(
                    "cannot remove connected interface from multicast interface : " +
                    clientItfName);
            }
        }
    }

    /*
     * @see org.objectweb.proactive.core.component.controller.MulticastController#lookupFc(java.lang.String)
     */
    public ProxyForComponentInterfaceGroup lookupFcMulticast(
        String clientItfName) {
        if (multicastItfs.containsKey(clientItfName)) {
            return (ProxyForComponentInterfaceGroup) ((ProActiveInterface) multicastItfs.get(clientItfName)
                                                                                        .getFcItfImpl()).getProxy();
        } else {
            return null;
        }
    }

    public Map<MethodCall, Integer> generateMethodCallsForMulticastDelegatee(
        MethodCall mc, ProxyForComponentInterfaceGroup delegatee)
        throws ParameterDispatchException {
        // read from annotations
        Object[] clientSideEffectiveArguments = mc.getEffectiveArguments();

        ProActiveInterfaceType itfType = (ProActiveInterfaceType) multicastItfs.get(mc.getComponentMetadata()
                                                                                      .getComponentInterfaceName())
                                                                               .getFcItfType();

        Method matchingMethodInClientInterface; // client itf as well as parent interfaces

        try {
            matchingMethodInClientInterface = Class.forName(itfType.getFcItfSignature())
                                                   .getMethod(mc.getReifiedMethod()
                                                                .getName(),
                    mc.getReifiedMethod().getParameterTypes());
        } catch (Exception e) {
            e.printStackTrace();
            throw new ParameterDispatchException(e.fillInStackTrace());
        }

        Class[] clientSideParamTypes = matchingMethodInClientInterface.getParameterTypes();
        ParamDispatch[] clientSideParamDispatchModes = MulticastBindingChecker.getDispatchModes(matchingMethodInClientInterface);

        List<List<Object>> dispatchedParameters = new ArrayList<List<Object>>();

        int expectedMethodCallsNb = 0;

        // compute dispatch sizes for annotated parameters
        Vector<Integer> dispatchSizes = new Vector<Integer>();

        for (int i = 0; i < clientSideParamTypes.length; i++) {
            dispatchSizes.addElement(clientSideParamDispatchModes[i].expectedDispatchSize(
                    clientSideEffectiveArguments[i], delegatee.size()));
        }

        // -1 mean there are no suggested max dispatch size 
        int max = -1;
        if (dispatchSizes.size() > 0) {
            max = dispatchSizes.get(0);
        }

        for (int i = 1; i < dispatchSizes.size(); i++) {
            if (dispatchSizes.get(i) > max) {
                max = dispatchSizes.get(i);
            }
        }
        if (max == -1) {
            max = delegatee.size();
        }
        for (int i = 0; i < dispatchSizes.size(); i++) {
            if (dispatchSizes.get(i) == -1) {
                dispatchSizes.set(i, max);
            }
        }

        if (dispatchSizes.size() > 0) {
            // ok, found some annotated elements
            expectedMethodCallsNb = dispatchSizes.get(0);

            for (int i = 1; i < dispatchSizes.size(); i++) {
                if (dispatchSizes.get(i).intValue() != expectedMethodCallsNb) {
                    throw new ParameterDispatchException(
                        "cannot generate invocation for multicast interface " +
                        itfType.getFcItfName() +
                        " because the specified distribution of parameters is incorrect in method " +
                        matchingMethodInClientInterface.getName() + "(" +
                        dispatchSizes.get(i).intValue() + " instead of " +
                        expectedMethodCallsNb + ")");
                }
            }
        } else {
            // broadcast to every member of the group
            expectedMethodCallsNb = delegatee.size();
        }

        // get distributed parameters
        for (int i = 0; i < clientSideParamTypes.length; i++) {
            List<Object> dispatchedParameter = clientSideParamDispatchModes[i].dispatch(clientSideEffectiveArguments[i],
                    expectedMethodCallsNb);
            //delegatee.size()); FIXME 
            dispatchedParameters.add(dispatchedParameter);
        }

        Map<MethodCall, Integer> result = new HashMap<MethodCall, Integer>(expectedMethodCallsNb);

        // need to find matching method in server interface
        try {
            //            if (matchingMethods.get(mc.getComponentMetadata()
            //                                          .getComponentInterfaceName()) == null) {
            //                System.out.println("########## \n" +
            //                    matchingMethods.toString());
            //            }
            Method matchingMethodInServerInterface = matchingMethods.get(mc.getComponentMetadata()
                                                                           .getComponentInterfaceName())
                                                                    .get(new SerializableMethod(
                        mc.getReifiedMethod())).getMethod();

            // now we have all dispatched parameters
            // proceed to generation of method calls
            for (int generatedMethodCallIndex = 0;
                    generatedMethodCallIndex < expectedMethodCallsNb;
                    generatedMethodCallIndex++) {
                Object[] individualEffectiveArguments = new Object[matchingMethodInServerInterface.getParameterTypes().length];

                for (int parameterIndex = 0;
                        parameterIndex < individualEffectiveArguments.length;
                        parameterIndex++) {
                    individualEffectiveArguments[parameterIndex] = dispatchedParameters.get(parameterIndex)
                                                                                       .get(generatedMethodCallIndex); // initialize
                }

                result.put(new MethodCall(matchingMethodInServerInterface,
                        mc.getGenericTypesMapping(),
                        individualEffectiveArguments, mc.getExceptionContext()),
                    generatedMethodCallIndex);
                //                      generatedMethodCallIndex % delegatee.size());
                // default is to do some round robin when nbGeneratedMethodCalls > nbReceivers
            }
        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return result;
    }

    protected void bindFc(String clientItfName, ProActiveInterface serverItf) {
        init();
        if (logger.isDebugEnabled()) {
            try {
                if (!ProGroup.isGroup(serverItf.getFcItfOwner())) {
                    logger.debug("multicast binding : " + clientItfName +
                        " to : " +
                        Fractal.getNameController(serverItf.getFcItfOwner())
                               .getFcName() + "." + serverItf.getFcItfName());
                }
            } catch (NoSuchInterfaceException e) {
                e.printStackTrace();
            }
        }
        if (multicastItfs.containsKey(clientItfName)) {
            try {
                ProxyForComponentInterfaceGroup clientSideProxy = (ProxyForComponentInterfaceGroup) clientSideProxies.get(clientItfName);

                if (clientSideProxy.getDelegatee() == null) {
                    ProActiveInterface groupItf = ProActiveComponentGroup.newComponentInterfaceGroup((ProActiveInterfaceType) serverItf.getFcItfType(),
                            owner);
                    ProxyForComponentInterfaceGroup proxy = (ProxyForComponentInterfaceGroup) ((StubObject) groupItf).getProxy();
                    clientSideProxy.setDelegatee(proxy);
                }

                ((Group) clientSideProxy.getDelegatee()).add(serverItf);
            } catch (ClassNotReifiableException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean hasClientSideProxy(String itfName) {
        return clientSideProxies.containsKey(itfName);
    }

    private void addClientSideProxy(String itfName, ProActiveInterface itf) {
        Proxy proxy = ((ProActiveInterface) itf.getFcItfImpl()).getProxy();

        if (!(proxy instanceof Group)) {
            throw new ProActiveRuntimeException(
                "client side proxies for multicast interfaces must be Group instances");
        }

        clientSideProxies.put(itfName, proxy);
    }
}
