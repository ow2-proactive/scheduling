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

import java.io.Serializable;

import org.apache.log4j.Logger;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.Interface;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.Type;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.ContentController;
import org.objectweb.fractal.api.control.LifeCycleController;
import org.objectweb.fractal.api.control.NameController;
import org.objectweb.fractal.api.control.SuperController;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.proxy.UniversalBodyProxy;
import org.objectweb.proactive.core.component.ComponentParameters;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.ProActiveInterface;
import org.objectweb.proactive.core.component.asmgen.RepresentativeInterfaceClassGenerator;
import org.objectweb.proactive.core.component.controller.ComponentParametersController;
import org.objectweb.proactive.core.component.controller.ProActiveSuperController;
import org.objectweb.proactive.core.component.request.ComponentRequestQueue;
import org.objectweb.proactive.core.group.ProxyForGroup;
import org.objectweb.proactive.core.mop.MethodCall;
import org.objectweb.proactive.core.mop.Proxy;
import org.objectweb.proactive.core.mop.StubObject;
import org.objectweb.proactive.core.util.ProActiveLogger;


public class ProActiveComponentRepresentativeImpl
    implements ProActiveComponentRepresentative, BindingController,
        LifeCycleController, ContentController, ComponentParametersController,
        ProActiveSuperController, Interface, Serializable {
    private static Logger logger = ProActiveLogger.getLogger("components");
    private Interface[] interfaceReferences;
    private Proxy proxy;

    //private ComponentParameters componentParameters;
    private ComponentType componentType = null; // immutable
    private StubObject stubOnBaseObject = null;

    public ProActiveComponentRepresentativeImpl(ComponentType componentType) {
        this.componentType = componentType;

        // create the interface references tables
        // the size is the addition of :  
        // - 1 for the current ItfRef (that is at the same time a binding controller, lifecycle controller,
        // content controller and name controller
        // - the number of client functional interfaces
        // - the number of server functional interfaces
        interfaceReferences = new Interface[1 +
            componentType.getFcInterfaceTypes().length];

        int i = 0;

        // add controllers
        interfaceReferences[i] = (Interface) this;
        i++;

        // add functional interfaces
        // functional interfaces are proxies on the corresponding meta-objects
        // 3. external functional interfaces
        InterfaceType[] interface_types = componentType.getFcInterfaceTypes();
        try {
            for (int j = 0; j < interface_types.length; j++) {
                Interface interface_reference = RepresentativeInterfaceClassGenerator.instance()
                                                                                     .generateInterface(interface_types[j].getFcItfName(),
                        this, interface_types[j], false);

                // all calls are to be reified
                interfaceReferences[i] = interface_reference;
                i++;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("cannot create interface references : " +
                e.getMessage());
        }
    }

    /*
     *implements  org.objectweb.fractal.api.control.BindingController#lookupFc(String)}
     */
    public Object lookupFc(String clientItfName) {
        return (Interface) reifyCall(BindingController.class.getName(),
            "lookupFc", new Class[] { String.class },
            new Object[] { clientItfName });
    }

    /*
     *implements  BindingController#bindFc(java.lang.String, java.lang.Object)}
     */
    public void bindFc(String clientItfName, Object serverItf) {
        reifyCall(BindingController.class.getName(), "bindFc",
            new Class[] { String.class, Object.class },
            new Object[] { clientItfName, serverItf });
    }

    /*
     *implements  org.objectweb.fractal.api.control.BindingController#unbindFc(String)}
     */
    public void unbindFc(String clientItfName) {
        reifyCall(BindingController.class.getName(), "unbindFc",
            new Class[] { String.class }, new Object[] { clientItfName });
    }

    /*
     *implements  org.objectweb.fractal.api.control.LifeCycleController#getFcState()}
     */
    public String getFcState() {
        return (String) reifyCall(LifeCycleController.class.getName(),
            "getFcState", new Class[] {  }, new Object[] {  });
    }

    /*
     *implements  org.objectweb.fractal.api.control.LifeCycleController#startFc()}
     */
    public void startFc() {
        reifyCall(LifeCycleController.class.getName(), "startFc",
            new Class[] {  }, new Object[] {  });
    }

    /*
     *implements  org.objectweb.fractal.api.control.LifeCycleController#stopFc()}
     */
    public void stopFc() {
        reifyCall(LifeCycleController.class.getName(), "stopFc",
            new Class[] {  }, new Object[] {  });
    }

    /*
     *implements  org.objectweb.fractal.api.control.ContentController#getFcInternalInterfaces()}
     */
    public Object[] getFcInternalInterfaces() {
        return (Object[]) reifyCall(ContentController.class.getName(),
            "getFcInternalInterfaces", new Class[] {  }, new Object[] {  });
    }

    /*
     * in this implementation, internal interfaces are also external interfaces.
     *implements  org.objectweb.fractal.api.control.ContentController#getFcInternalInterface(String)}
     */
    public Object getFcInternalInterface(String interfaceName)
        throws NoSuchInterfaceException {
        return getFcInterface(interfaceName);
    }

    /*
     *implements  org.objectweb.fractal.api.control.ContentController#getFcSubComponents()}
     */
    public Component[] getFcSubComponents() {
        return (Component[]) reifyCall(ContentController.class.getName(),
            "getFcSubComponents", new Class[] {  }, new Object[] {  });
    }

    /*
     *implements  org.objectweb.fractal.api.control.ContentController#addFcSubComponent(Component)}
     */
    public void addFcSubComponent(Component subComponent) {
        reifyCall(ContentController.class.getName(), "addFcSubComponent",
            new Class[] { Component.class }, new Object[] { subComponent });
    }

    /*
     *implements  org.objectweb.fractal.api.control.ContentController#removeFcSubComponent(Component)}
     */
    public void removeFcSubComponent(Component subComponent) {
        reifyCall(ContentController.class.getName(), "removeFcSubComponent",
            new Class[] { Component.class }, new Object[] { subComponent });
    }

    /*
     *implements  org.objectweb.fractal.api.Interface#getFcItfOwner()}
     */
    public Component getFcItfOwner() {
        return this;
        //        return (Component) reifyCall(Interface.class.getName(),
        //            "getFcItfOwner", new Class[] {  }, new Object[] {  });
    }

    /*
     *implements  org.objectweb.fractal.api.Interface#getFcItfName()}
     */
    public String getFcItfName() {
        // PB is that the current object implements several functional interfaces.
        // Thus it has several names...
        return null;
    }

    /*
     *implements  org.objectweb.fractal.api.Interface#getFcItfType()}
     */
    public Type getFcItfType() {
        return getFcType();
    }

    /*
     *implements  org.objectweb.fractal.api.Interface#isFcInternalItf()}
     */
    public boolean isFcInternalItf() {
        return false;
    }

    protected Object reifyCall(String className, String methodName,
        Class[] parameterTypes, Object[] effectiveParameters) {
        try {
            return proxy.reify((MethodCall) MethodCall.getComponentMethodCall(
                    Class.forName(className).getDeclaredMethod(methodName,
                        parameterTypes), effectiveParameters, null));

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
            return this;
        }
        for (int i = 0; i < interfaceReferences.length; i++) {
            if (interfaceReferences[i].getFcItfName() != null) {
                if (interfaceReferences[i].getFcItfName().equals(interfaceName) ||
                        interfaceName.startsWith(
                            interfaceReferences[i].getFcItfName())) {
                    if (getProxy() instanceof ProxyForGroup) {
                        //create a new group of called functional interfaces 
                        try {
                            StubObject stub_on_group_of_itfs = (StubObject) reifyCall(Component.class.getName(),
                                    "getFcInterface",
                                    new Class[] { String.class },
                                    new Object[] { interfaceName });

                            // create a component stub and affect the proxy containing the group resulting from the previous call
                            ProActiveInterface result = (ProActiveInterface) interfaceReferences[i].getClass()
                                                                                                   .newInstance();

                            // fill in data
                            result.setFcItfName(interfaceReferences[i].getFcItfName());
                            result.setFcOwner(interfaceReferences[i].getFcItfOwner());
                            result.setFcType(interfaceReferences[i].getFcItfType());
                            // set proxy
                            ((StubObject) result).setProxy(stub_on_group_of_itfs.getProxy());
                            return result;
                        } catch (Exception e) {
                            throw new NoSuchInterfaceException(
                                "could not generate a group of interfaces");
                        }
                    } else {
                        return interfaceReferences[i];
                    }
                }
            }
        }
        throw new NoSuchInterfaceException(interfaceName);
    }

    /*
     *implements  org.objectweb.fractal.api.Component#getFcInterfaces()}
     */
    public Object[] getFcInterfaces() {
        return (Object[]) reifyCall(Component.class.getName(),
            "getFcInterfaces", new Class[] {  }, new Object[] {  });
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
        this.proxy = proxy;
        for (int i = 0; i < interfaceReferences.length; i++) {
            if (interfaceReferences[i] != this) {
                ((StubObject) interfaceReferences[i]).setProxy(proxy);
            }
        }
    }

    /**
     *  The comparison of component references is actually a comparison of unique
     * identifiers accross jvms.
     */
    public boolean equals(Object component) {
        Object result = reifyCall(Object.class.getName(),
                "equals", new Class[] {Object.class  }, new Object[] { component  });
        return ((Boolean)result).booleanValue();
//        if (!(component instanceof ProActiveComponent)) {
//            logger.error(
//                "can only compare proactive components to proactive components ");
//            return false;
//        }
//        return getProxy().equals(((ProActiveComponentRepresentative) component).getProxy());
    }

    public int hashCode() {
        Object result = reifyCall(Object.class.getName(),
                "hashCode", new Class[] {}, new Object[] {});
        return ((Integer)result).intValue();
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
     *implements  ComponentParametersController#setComponentParameters(ComponentParameters)}
     */
    public void setComponentParameters(ComponentParameters componentParameters) {
        logger.error("only available in the meta-objects");
    }

    /*
     *implements  ComponentParametersController#getComponentParameters()}
     */
    public ComponentParameters getComponentParameters() {
        return (ComponentParameters) reifyCall(ComponentParametersController.class.getName(),
            "getComponentParameters", new Class[] {  }, new Object[] {  });
    }

    /*
     *implements  org.objectweb.fractal.api.control.BindingController#listFc()}
     */
    public String[] listFc() {
        return null;
    }

    /*
     * implements org.objectweb.fractal.api.control.NameController#setFcName(String name)}
     */
    public void setFcName(String componentName) {
        reifyCall(NameController.class.getName(), "setFcName",
            new Class[] { String.class }, new Object[] { componentName });
    }

    /*
     * implements org.objectweb.fractal.api.control.NameController#getFcName()}
     */
    public String getFcName() {
        return (String) reifyCall(NameController.class.getName(), "getFcName",
            new Class[] {  }, new Object[] {  });
    }

    /*
     * implements org.objectweb.proactive.core.component.identity.ProActiveComponent#getReferenceOnBaseObject()}
     */
    public Object getReferenceOnBaseObject() {
        return null;
    }

    /** (non-Javadoc)
     * only available in the meta-objects
     */
    public ComponentRequestQueue getRequestQueue() {
        logger.error("only available in the meta-objects");
        return null;
    }

    /* (non-Javadoc)
     * implements org.objectweb.proactive.core.component.identity.ProActiveComponent#getRepresentativeOnThis()}
     */
    public Component getRepresentativeOnThis() {
        return this;
    }

    /* (non-Javadoc)
     * @see org.objectweb.fractal.api.control.SuperController#getFcSuperComponents()
     */
    public Component[] getFcSuperComponents() {
        return (Component[]) reifyCall(SuperController.class.getName(),
            "getFcSuperComponents", new Class[] {  }, new Object[] {  });
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.component.controller.ProActiveSuperController#addParent(org.objectweb.fractal.api.Component)
     */
    public void addParent(Component parent) {
        reifyCall(ProActiveSuperController.class.getName(), "addParent",
            new Class[] { Component.class }, new Object[] { parent });
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.component.controller.ProActiveSuperController#removed(org.objectweb.fractal.api.Component)
     */
    public void removeParent(Component parent) {
        reifyCall(ProActiveSuperController.class.getName(), "removeParent",
            new Class[] { Component.class }, new Object[] { parent });
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.component.representative.ProActiveComponentRepresentative#getStubOnReifiedObject()
     */
    public StubObject getStubOnBaseObject() {
        return stubOnBaseObject;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.component.representative.ProActiveComponentRepresentative#setStubOnReifiedObject(org.objectweb.proactive.core.mop.StubObject)
     */
    public void setStubOnBaseObject(StubObject stub) {
        stubOnBaseObject = stub;
    }

    protected boolean isControllerInterface(String interfaceName) {
        if (interfaceName.equals(Constants.BINDING_CONTROLLER) ||
                interfaceName.equals(Constants.CONTENT_CONTROLLER) ||
                interfaceName.equals(Constants.LIFECYCLE_CONTROLLER) ||
                interfaceName.equals(Constants.COMPONENT_PARAMETERS_CONTROLLER) ||
                interfaceName.equals(Constants.NAME_CONTROLLER) ||
                interfaceName.equals(Constants.SUPER_CONTROLLER)) {
            return true;
        } else {
            return false;
        }
    }
}
