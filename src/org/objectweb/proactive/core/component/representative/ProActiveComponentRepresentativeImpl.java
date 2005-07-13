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
package org.objectweb.proactive.core.component.representative;

import org.apache.log4j.Logger;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.Interface;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.Type;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.ContentController;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.api.type.InterfaceType;

import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.proxy.UniversalBodyProxy;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.Fractive;
import org.objectweb.proactive.core.component.ProActiveInterface;
import org.objectweb.proactive.core.component.asmgen.RepresentativeInterfaceClassGenerator;
import org.objectweb.proactive.core.component.config.ComponentConfigurationHandler;
import org.objectweb.proactive.core.component.controller.AbstractProActiveController;
import org.objectweb.proactive.core.component.identity.ProActiveComponentImpl;
import org.objectweb.proactive.core.component.request.ComponentRequest;
import org.objectweb.proactive.core.group.ProxyForGroup;
import org.objectweb.proactive.core.mop.MethodCall;
import org.objectweb.proactive.core.mop.Proxy;
import org.objectweb.proactive.core.mop.StubObject;
import org.objectweb.proactive.core.util.log.ProActiveLogger;

import java.io.File;
import java.io.Serializable;

import java.lang.reflect.Constructor;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;


/**
 * An object of type <code> Component  </code> which is a remote reference on a
 * component. <br>
 * When creating an active object of type <code> A  </code>, you get a reference
 * on the active object through a dynamically generated stub of type
 * <code> A  </code>. Similarly, when creating a component, you get a reference
 * on an object of type <code> Component  </code>, in other words an instance of
 * this class.
 * <p>
 * During the construction of an instance of this class, references to
 * interfaces of the component are also dynamically generated : references to
 * functional interfaces corresponding to the server interfaces of the
 * component, and references to control interfaces. The idea is to save remote
 * invocations : when requesting a controller or an interface, the generated
 * corresponding interface is directly returned. Then, invocations on this
 * interface are reified and transferred to the actual component. <br>
 *
 * @author Matthieu Morel
 */
public class ProActiveComponentRepresentativeImpl
    implements ProActiveComponentRepresentative, Serializable {
    private static Logger logger = ProActiveLogger.getLogger("components");

    //private Interface[] interfaceReferences;
    private Map fcInterfaceReferences;
    private Map nfInterfaceReferences;
    private Proxy proxy;

    //private ComponentParameters componentParameters;
    private ComponentType componentType = null; // immutable
    private StubObject stubOnBaseObject = null;
    private String hierarchicalType = null;
    private String currentControllerInterface = null;
    private boolean useShortcuts;

    public ProActiveComponentRepresentativeImpl(ComponentType componentType,
        String hierarchicalType, File controllersConfigFile) {
        this.componentType = componentType;
        this.hierarchicalType = hierarchicalType;
        ComponentConfigurationHandler componentConfiguration = ProActiveComponentImpl.loadComponentConfiguration(controllersConfigFile);
        Map controllersConfiguration = componentConfiguration.getControllers();

        useShortcuts = ("true".equals(System.getProperty(
                    "proactive.components.use_shortcuts")));

        // create the interface references tables
        // the size is the addition of :  
        // - 1 for the current ItfRef (that is at the same time a binding controller, lifecycle controller,
        // content controller and name controller
        // - the number of client functional interfaces
        // - the number of server functional interfaces
        //ArrayList interface_references_list = new ArrayList(1 +componentType.getFcInterfaceTypes().length+controllersConfiguration.size());
        nfInterfaceReferences = new HashMap(1 +
                controllersConfiguration.size());

        //        interfaceReferences = new Interface[1 +
        //            componentType.getFcInterfaceTypes().length+controllersConfiguration.size()];
        int i = 0;

        // add controllers
        //Enumeration controllersInterfaces = controllersConfiguration.propertyNames();
        Iterator iteratorOnControllers = controllersConfiguration.keySet().iterator(); 
        Class controllerClass = null;
        AbstractProActiveController currentController;
        ProActiveInterface currentInterface = null;
        Class controllerItf;
        while (iteratorOnControllers.hasNext()) {
            String controllerItfName = (String) iteratorOnControllers.next();
            try {
                controllerItf = Class.forName(controllerItfName);
                controllerClass = Class.forName((String)controllersConfiguration.get(
                            controllerItf.getName()));
                Constructor controllerClassConstructor = controllerClass.getConstructor(new Class[] {
                            Component.class
                        });
                currentController = (AbstractProActiveController) controllerClassConstructor.newInstance(new Object[] {
                            this
                        });
                currentInterface = RepresentativeInterfaceClassGenerator.instance()
                                                                        .generateControllerInterface(currentController.getFcItfName(),
                        this, (InterfaceType) currentController.getFcItfType());
            } catch (Exception e) {
                logger.error("could not create controller " +
                    controllersConfiguration.get(controllerItfName) +
                    " : " + e.getMessage());
                continue;
            }

            if (BindingController.class.isAssignableFrom(controllerClass)) {
                if ((hierarchicalType.equals(Constants.PRIMITIVE) &&
                        (Fractive.getClientInterfaceTypes(
                            componentType).length == 0))) {
                    //bindingController = null;
                    if (logger.isDebugEnabled()) {
                        logger.debug(
                            "user component class of this component does not have any client interface. It will have no BindingController");
                    }
                    continue;
                }
            }
            if (ContentController.class.isAssignableFrom(controllerClass)) {
                if (hierarchicalType.equals(Constants.PRIMITIVE)) {
                    // no content controller here
                    continue;
                }
            }
            if (currentInterface != null) {
                nfInterfaceReferences.put(currentController.getFcItfName(),
                    currentInterface);

                i++;
            }
        }

        // add functional interfaces
        // functional interfaces are proxies on the corresponding meta-objects
        // 3. external functional interfaces
        fcInterfaceReferences = new HashMap(componentType.getFcInterfaceTypes().length);
        InterfaceType[] interface_types = componentType.getFcInterfaceTypes();
        try {
            for (int j = 0; j < interface_types.length; j++) {
                Interface interface_reference = RepresentativeInterfaceClassGenerator.instance()
                                                                                     .generateFunctionalInterface(interface_types[j].getFcItfName(),
                        this, interface_types[j]);

                // all calls are to be reified
                fcInterfaceReferences.put(interface_reference.getFcItfName(),
                    interface_reference);
                i++;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("cannot create interface references : " +
                e.getMessage());
        }
    }

    protected Object reifyCall(String className, String methodName,
        Class[] parameterTypes, Object[] effectiveParameters, short priority) {
        try {
            return proxy.reify((MethodCall) MethodCall.getComponentMethodCall(
                    Class.forName(className).getDeclaredMethod(methodName,
                        parameterTypes), effectiveParameters, (String) null,
                    false, priority));

            // functional interface name is null
        } catch (NoSuchMethodException e) {
            throw new ProActiveRuntimeException(e.toString());
        } catch (ClassNotFoundException e) {
            throw new ProActiveRuntimeException(e.toString());
        } catch (Throwable e) {
            e.printStackTrace();
            throw new ProActiveRuntimeException(e.toString());
        }
    }

    /*
     *implements  org.objectweb.fractal.api.Component#getFcInterface(String)}
     */
    public Object getFcInterface(String interfaceName)
        throws NoSuchInterfaceException {
        if (isControllerInterface(interfaceName)) {
            if (Constants.CONTENT_CONTROLLER.equals(interfaceName) &&
                    isPrimitive()) {
                throw new NoSuchInterfaceException(
                    "there is no content controller in this component");
            }
            return nfInterfaceReferences.get(interfaceName);
        }

        Iterator iterator = fcInterfaceReferences.keySet().iterator();
        while (iterator.hasNext()) {
            String itfName = (String) iterator.next();
            ProActiveInterface itf = (ProActiveInterface) fcInterfaceReferences.get(itfName);
            if (interfaceName.startsWith(itfName) ||
                    interfaceName.startsWith(itfName)) {
                if (getProxy() instanceof ProxyForGroup) {
                    //create a new group of called functional interfaces 
                    try {
                        StubObject stub_on_group_of_itfs = (StubObject) reifyCall(Component.class.getName(),
                                "getFcInterface", new Class[] { String.class },
                                new Object[] { interfaceName },
                                ComponentRequest.STRICT_FIFO_PRIORITY);

                        // create a component stub and affect the proxy containing the group resulting from the previous call
                        ProActiveInterface result = (ProActiveInterface) itf.getClass()
                                                                            .newInstance();

                        // fill in data
                        result.setFcItfName(itf.getFcItfName());
                        result.setFcItfOwner(itf.getFcItfOwner());
                        result.setFcType(itf.getFcItfType());
                        // set proxy
                        ((StubObject) result).setProxy(stub_on_group_of_itfs.getProxy());
                        return result;
                    } catch (Exception e) {
                        throw new NoSuchInterfaceException(
                            "could not generate a group of interfaces");
                    }
                } else {
                    return itf;
                }
            }
        }

        throw new NoSuchInterfaceException(interfaceName);
    }

    /*
     *implements  org.objectweb.fractal.api.Component#getFcInterfaces()}
     */
    public Object[] getFcInterfaces() {
        Interface[] nfInterfaces = (Interface[]) (nfInterfaceReferences.values()
                                                                       .toArray(new Interface[nfInterfaceReferences.size()]));
        Interface[] fcInterfaces = (Interface[]) (fcInterfaceReferences.values()
                                                                       .toArray(new Interface[fcInterfaceReferences.size()]));
        Interface[] result = new Interface[nfInterfaces.length +
            fcInterfaces.length];
        System.arraycopy(nfInterfaces, 0, result, 0, nfInterfaces.length);
        System.arraycopy(fcInterfaces, 0, result, nfInterfaces.length,
            fcInterfaces.length);
        return result;
    }

    /*
     *implements  org.objectweb.fractal.api.Component#getFcType()}
     */
    public Type getFcType() {
        return componentType;
    }

    /*
     *implements  org.objectweb.proactive.core.mop.StubObject#getProxy()}
     */
    public Proxy getProxy() {
        return proxy;
    }

    /*
     *implements  org.objectweb.proactive.core.mop.StubObject#setProxy(Proxy)}
     */
    public void setProxy(Proxy proxy) {
        // sets proxy for non functional interfaces
        this.proxy = proxy;
        // sets the same proxy for all interfaces of this component
        Object[] interfaces = getFcInterfaces();
        ProActiveInterface[] interface_references = new ProActiveInterface[interfaces.length];
        for (int i = 0; i < interfaces.length; i++) {
            interface_references[i] = (ProActiveInterface) interfaces[i];
        }
        for (int i = 0; i < interface_references.length; i++) {
            if (useShortcuts) {
                // adds an intermediate FunctionalInterfaceProxy for functional interfaces, to manage shortcutting
                ((StubObject) interface_references[i]).setProxy(new FunctionalInterfaceProxyImpl(
                        proxy, interface_references[i].getFcItfName()));
            } else {
                try {
                    ((StubObject) interface_references[i]).setProxy(proxy);
                } catch (RuntimeException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     *  The comparison of component references is actually a comparison of unique
     * identifiers accross jvms.
     */
    public boolean equals(Object component) {
        Object result = reifyCall(Object.class.getName(), "equals",
                new Class[] { Object.class }, new Object[] { component },
                ComponentRequest.STRICT_FIFO_PRIORITY);
        return ((Boolean) result).booleanValue();
    }

    public int hashCode() {
        Object result = reifyCall(Object.class.getName(), "hashCode",
                new Class[] {  }, new Object[] {  },
                ComponentRequest.STRICT_FIFO_PRIORITY);
        return ((Integer) result).intValue();
    }

    /**
     * Only valid for a single element. return null for a group.
     */
    public UniqueID getID() {
        if (!(getProxy() instanceof ProxyForGroup)) {
            return ((UniversalBodyProxy) getProxy()).getBodyID();
        } else {
            return null;
        }
    }

    /*
     * implements org.objectweb.proactive.core.component.identity.ProActiveComponent#getReferenceOnBaseObject()}
     */
    public Object getReferenceOnBaseObject() {
        logger.error(
            "getReferenceOnBaseObject() method is not available in component representatives");
        return null;
    }

    /*
     * implements org.objectweb.proactive.core.component.identity.ProActiveComponent#getRepresentativeOnThis()}
     */
    public Component getRepresentativeOnThis() {
        return this;
    }

    /*
     * @see org.objectweb.proactive.core.component.representative.ProActiveComponentRepresentative#getStubOnReifiedObject()
     */
    public StubObject getStubOnBaseObject() {
        return stubOnBaseObject;
    }

    /*
     * @see org.objectweb.proactive.core.component.representative.ProActiveComponentRepresentative#setStubOnReifiedObject(org.objectweb.proactive.core.mop.StubObject)
     */
    public void setStubOnBaseObject(StubObject stub) {
        stubOnBaseObject = stub;
    }

    protected boolean isControllerInterface(String interfaceName)
        throws NoSuchInterfaceException {
        if (nfInterfaceReferences.keySet().contains(interfaceName)) {
            if (interfaceName.equals(Constants.CONTENT_CONTROLLER)) {
                if (Constants.PRIMITIVE.equals(hierarchicalType)) {
                    throw new NoSuchInterfaceException(interfaceName);
                } else {
                    return true;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    private boolean isPrimitive() {
        return Constants.PRIMITIVE.equals(hierarchicalType);
    }
}
