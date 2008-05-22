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
package org.objectweb.proactive.core.component.representative;

import org.objectweb.fractal.api.control.LifeCycleController;
import org.objectweb.fractal.api.control.NameController;
import org.objectweb.fractal.api.type.TypeFactory;
import org.objectweb.fractal.util.Fractal;
import org.objectweb.proactive.core.component.ControllerDescription;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.TypeVariable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

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
import org.objectweb.proactive.core.component.ComponentParameters;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.Fractive;
import org.objectweb.proactive.core.component.ProActiveInterface;
import org.objectweb.proactive.core.component.Utils;
import org.objectweb.proactive.core.component.config.ComponentConfigurationHandler;
import org.objectweb.proactive.core.component.controller.AbstractProActiveController;
import org.objectweb.proactive.core.component.controller.ComponentParametersController;
import org.objectweb.proactive.core.component.gen.RepresentativeInterfaceClassGenerator;
import org.objectweb.proactive.core.component.identity.ProActiveComponent;
import org.objectweb.proactive.core.component.identity.ProActiveComponentImpl;
import org.objectweb.proactive.core.component.request.ComponentRequest;
import org.objectweb.proactive.core.component.type.ProActiveInterfaceType;
import org.objectweb.proactive.core.config.PAProperties;
import org.objectweb.proactive.core.group.ProxyForGroup;
import org.objectweb.proactive.core.mop.MethodCall;
import org.objectweb.proactive.core.mop.Proxy;
import org.objectweb.proactive.core.mop.StubObject;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


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
 * @author The ProActive Team
 */
public class ProActiveComponentRepresentativeImpl implements ProActiveComponentRepresentative, Serializable {
    protected static Logger logger = ProActiveLogger.getLogger(Loggers.COMPONENTS);
    protected Map<String, Interface> fcInterfaceReferences;
    protected Map<String, Interface> nfInterfaceReferences;
    protected Proxy proxy;
    protected ComponentType componentType = null; // immutable
    protected ComponentType componentNfType = null;
    protected StubObject stubOnBaseObject = null;
    protected String hierarchicalType = null;
    protected String currentControllerInterface = null;
    protected boolean useShortcuts;

    public ProActiveComponentRepresentativeImpl(ComponentType componentType, String hierarchicalType,
            String controllersConfigFileLocation) {
        this.componentType = componentType;
        useShortcuts = PAProperties.PA_COMPONENT_USE_SHORTCUTS.isTrue();
        this.hierarchicalType = hierarchicalType;
        addControllers(componentType, controllersConfigFileLocation);

        // add functional interfaces
        // functional interfaces are proxies on the corresponding meta-objects
        addFunctionalInterfaces(componentType);
    }

    public ProActiveComponentRepresentativeImpl(ComponentParameters componentParam) {
        this.componentType = componentParam.getComponentType();
        this.componentNfType = componentParam.getComponentNFType();
        useShortcuts = PAProperties.PA_COMPONENT_USE_SHORTCUTS.isTrue();

        this.hierarchicalType = componentParam.getHierarchicalType();
        ControllerDescription controllerDesc = componentParam.getControllerDescription();
        if (componentNfType != null) { /*A nf type is specified*/
            if (controllerDesc.configFileIsSpecified()) { /*If a config file is specified, it must be used to generate nf interfaces*/
                addControllers(componentType, controllerDesc.getControllersConfigFileLocation());
            } else { /*The config file is not specified, nf interfaces have to be generated from the nf type*/
                addControllers(componentParam.getComponentNFType(), componentParam);
            }
        } else {
            addControllers(componentType, controllerDesc.getControllersConfigFileLocation());
        }
        addFunctionalInterfaces(componentType);
    }

    private boolean specialCasesForNfType(Class<?> controllerItf, boolean isPrimitive,
            ProActiveInterfaceType itfType, ComponentParameters componentParam) throws Exception {
        if (ContentController.class.isAssignableFrom(controllerItf) && !itfType.isFcClientItf() &&
            !itfType.isInternal()) {
            if (isPrimitive) {
                return true;// No external server content controller for primitive component
            }

            return false;//In this case, the ContentController has to be created
        }

        if (BindingController.class.isAssignableFrom(controllerItf) && !itfType.isFcClientItf() &&
            !itfType.isInternal()) {
            if (isPrimitive &&
                (Fractive.getClientInterfaceTypes(componentParam.getComponentType()).length == 0)) {
                // The binding controller is not generated for a component without client interfaces
                if (logger.isDebugEnabled()) {
                    logger.debug("user component class of '" + componentParam.getName() +
                        "' does not have any client interface. It will have no BindingController");
                }
                return true;//In this case, the BindingController is ignored
            }
            return false;// In this case, the BindingController is created
        }

        if (ComponentParametersController.class.isAssignableFrom(controllerItf) && !itfType.isFcClientItf() &&
            !itfType.isInternal()) { /*Mandatory controller, we don't have to recreate it*/
            return true;
        }

        if (NameController.class.isAssignableFrom(controllerItf) && !itfType.isFcClientItf() &&
            !itfType.isInternal()) { /*Mandatory controller, we don't have to recreate it*/
            return true;
        }

        if (LifeCycleController.class.isAssignableFrom(controllerItf) && !itfType.isFcClientItf() &&
            !itfType.isInternal()) { /*Mandatory controller, we don't have to recreate it*/
            return true;
        }
        return false;
    }

    private void addMandatoryControllers() throws Exception {
        Component boot = Fractal.getBootstrapComponent(); /*Getting the Fractal-Proactive bootstrap component*/
        TypeFactory type_factory = Fractal.getTypeFactory(boot);

        ProActiveInterfaceType itfType = (ProActiveInterfaceType) type_factory
                .createFcItfType(
                        Constants.COMPONENT_PARAMETERS_CONTROLLER,
                        /*PARAMETERS CONTROLLER*/org.objectweb.proactive.core.component.controller.ComponentParametersController.class
                                .getName(), TypeFactory.SERVER, TypeFactory.MANDATORY, TypeFactory.SINGLE);
        Interface interface_reference = RepresentativeInterfaceClassGenerator.instance().generateInterface(
                itfType.getFcItfName(), this, (ProActiveInterfaceType) itfType, itfType.isInternal(), false);

        nfInterfaceReferences.put(interface_reference.getFcItfName(), interface_reference);

        itfType = (ProActiveInterfaceType) type_factory
                .createFcItfType(
                        Constants.LIFECYCLE_CONTROLLER,
                        /*LIFECYCLE CONTROLLER*/org.objectweb.proactive.core.component.controller.ProActiveLifeCycleController.class
                                .getName(), TypeFactory.SERVER, TypeFactory.MANDATORY, TypeFactory.SINGLE);
        interface_reference = RepresentativeInterfaceClassGenerator.instance().generateInterface(
                itfType.getFcItfName(), this, (ProActiveInterfaceType) itfType, itfType.isInternal(), false);

        nfInterfaceReferences.put(interface_reference.getFcItfName(), interface_reference);

        itfType = (ProActiveInterfaceType) type_factory.createFcItfType(Constants.NAME_CONTROLLER,
        /*NAME CONTROLLER*/org.objectweb.fractal.api.control.NameController.class.getName(),
                TypeFactory.SERVER, TypeFactory.MANDATORY, TypeFactory.SINGLE);

        interface_reference = RepresentativeInterfaceClassGenerator.instance().generateInterface(
                itfType.getFcItfName(), this, (ProActiveInterfaceType) itfType, itfType.isInternal(), false);

        nfInterfaceReferences.put(interface_reference.getFcItfName(), interface_reference);
    }

    private void addControllers(ComponentType nfType, ComponentParameters params) {
        nfInterfaceReferences = new HashMap<String, Interface>();
        InterfaceType[] tmp = nfType.getFcInterfaceTypes();
        ProActiveInterfaceType[] interface_types = new ProActiveInterfaceType[tmp.length];
        System.arraycopy(tmp, 0, interface_types, 0, tmp.length);
        Class<?> controllerItf = null;

        try {
            addMandatoryControllers();
            for (int j = 0; j < interface_types.length; j++) {
                controllerItf = Class.forName(interface_types[j].getFcItfSignature());
                if (!specialCasesForNfType(controllerItf, params.getHierarchicalType().equals(
                        Constants.PRIMITIVE), interface_types[j], params)) {
                    if (!interface_types[j].isFcCollectionItf()) {
                        // itfs members of collection itfs are dynamically generated
                        Interface interface_reference = RepresentativeInterfaceClassGenerator.instance()
                                .generateInterface(interface_types[j].getFcItfName(), this,
                                        (ProActiveInterfaceType) interface_types[j],
                                        interface_types[j].isInternal(), false);

                        // all calls are to be reified
                        nfInterfaceReferences.put(interface_reference.getFcItfName(), interface_reference);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            //throw new RuntimeException("cannot create interface references : " +
            //  e.getMessage());
        }
    }

    /**
     * @param componentType
     */
    private void addFunctionalInterfaces(ComponentType componentType) {
        InterfaceType[] interface_types = componentType.getFcInterfaceTypes();
        fcInterfaceReferences = new HashMap<String, Interface>(interface_types.length +
            (interface_types.length / 2));

        try {
            for (int j = 0; j < interface_types.length; j++) {
                if (!interface_types[j].isFcCollectionItf()) {
                    // itfs members of collection itfs are dynamically generated
                    Interface interface_reference = RepresentativeInterfaceClassGenerator.instance()
                            .generateFunctionalInterface(interface_types[j].getFcItfName(), this,
                                    (ProActiveInterfaceType) interface_types[j]);

                    // all calls are to be reified
                    if (interface_reference != null) {
                        fcInterfaceReferences.put(interface_reference.getFcItfName(), interface_reference);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("cannot create interface references : " + e.getMessage());
        }
    }

    private void addControllers(ComponentType componentType, String controllersConfigFileLocation) {
        if (controllersConfigFileLocation == null) {
            return;
        }
        ComponentConfigurationHandler componentConfiguration = ProActiveComponentImpl
                .loadControllerConfiguration(controllersConfigFileLocation);
        Map<String, String> controllersConfiguration = componentConfiguration.getControllers();

        addControllers(componentType, controllersConfiguration);
    }

    private void addControllers(ComponentType componentType, Map<String, String> controllersConfiguration) {
        // create the interface references tables
        // the size is the addition of :  
        // - 1 for the current ItfRef (that is at the same time a binding controller, lifecycle controller,
        // content controller and name controller
        // - the number of client functional interfaces
        // - the number of server functional interfaces
        //ArrayList interface_references_list = new ArrayList(1 +componentType.getFcInterfaceTypes().length+controllersConfiguration.size());
        nfInterfaceReferences = new HashMap<String, Interface>(1 + controllersConfiguration.size());

        // add controllers
        //Enumeration controllersInterfaces = controllersConfiguration.propertyNames();
        Iterator<String> iteratorOnControllers = controllersConfiguration.keySet().iterator();
        Class<?> controllerClass = null;
        AbstractProActiveController currentController;
        ProActiveInterface currentInterface = null;
        Class<?> controllerItf;
        Vector<InterfaceType> nfType = new Vector<InterfaceType>();
        while (iteratorOnControllers.hasNext()) {
            String controllerItfName = iteratorOnControllers.next();
            try {
                controllerItf = Class.forName(controllerItfName);
                controllerClass = Class.forName(controllersConfiguration.get(controllerItf.getName()));
                Constructor<?> controllerClassConstructor = controllerClass
                        .getConstructor(new Class<?>[] { Component.class });
                currentController = (AbstractProActiveController) controllerClassConstructor
                        .newInstance(new Object[] { this });
                currentInterface = RepresentativeInterfaceClassGenerator.instance()
                        .generateControllerInterface(currentController.getFcItfName(), this,
                                (ProActiveInterfaceType) currentController.getFcItfType());
                ((StubObject) currentInterface).setProxy(proxy);

            } catch (Exception e) {
                logger.error("could not create controller " +
                    controllersConfiguration.get(controllerItfName) + " : " + e.getMessage());
                continue;
            }

            if (BindingController.class.isAssignableFrom(controllerClass)) {
                if ((hierarchicalType.equals(Constants.PRIMITIVE) && (Fractive
                        .getClientInterfaceTypes(componentType).length == 0))) {
                    //bindingController = null;
                    if (logger.isDebugEnabled()) {
                        logger
                                .debug("user component class of this component does not have any client interface. It will have no BindingController");
                    }
                    continue;
                }
            }
            if (ContentController.class.isAssignableFrom(controllerClass)) {
                if (Constants.PRIMITIVE.equals(hierarchicalType)) {
                    // no content controller here
                    continue;
                }
            }
            if (currentInterface != null) {
                nfInterfaceReferences.put(currentController.getFcItfName(), currentInterface);
                nfType.add((InterfaceType) currentInterface.getFcItfType());
            }
        }

        try {//Setting the real NF type, as some controllers may not be generated
            Component boot = Fractal.getBootstrapComponent();
            TypeFactory type_factory = Fractal.getTypeFactory(boot);
            InterfaceType[] nf = new InterfaceType[nfType.size()];
            nfType.toArray(nf);
            componentNfType = type_factory.createFcType(nf);
        } catch (Exception e) {
            e.printStackTrace();
            logger.warn("NF type could not be set");
        }
    }

    protected Object reifyCall(String className, String methodName, Class<?>[] parameterTypes,
            Object[] effectiveParameters, short priority) {
        try {
            return proxy.reify(MethodCall.getComponentMethodCall(Class.forName(className).getDeclaredMethod(
                    methodName, parameterTypes), effectiveParameters, null, (String) null, null, priority));

            // functional interface name is null
        } catch (NoSuchMethodException e) {
            throw new ProActiveRuntimeException(e.toString());
        } catch (ClassNotFoundException e) {
            throw new ProActiveRuntimeException(e.toString());
        } catch (Throwable e) {
            throw new ProActiveRuntimeException(e.toString());
        }
    }

    /*
     * @see org.objectweb.fractal.api.Component#getFcInterface(String)
     */
    public Object getFcInterface(String interfaceName) throws NoSuchInterfaceException {
        if (interfaceName.endsWith("-controller") && !("attribute-controller".equals(interfaceName))) {
            if (nfInterfaceReferences == null) {
                // retrieve the configuration by calling directly the mandatory component parameters controller itf
                ComponentParameters params = (ComponentParameters) reifyCall(
                        ComponentParametersController.class.getName(), "getComponentParameters",
                        new Class<?>[] {}, new Object[] {}, ComponentRequest.STRICT_FIFO_PRIORITY);
                hierarchicalType = params.getHierarchicalType();
                addControllers(componentType, params.getControllerDescription().getControllersSignatures());
            }
            if (nfInterfaceReferences.containsKey(interfaceName)) {
                return nfInterfaceReferences.get(interfaceName);
            } else {
                throw new NoSuchInterfaceException(interfaceName);
            }
        }

        if (fcInterfaceReferences.containsKey(interfaceName)) {
            return fcInterfaceReferences.get(interfaceName);
        } else {
            if (interfaceName.equals("component")) {
                return this;
            }

            // maybe the member of a collection itf?
            InterfaceType itfType = Utils.getItfType(interfaceName, this);
            if ((itfType != null) && itfType.isFcCollectionItf()) {
                try {
                    // generate the corresponding interface locally
                    Interface interface_reference = RepresentativeInterfaceClassGenerator.instance()
                            .generateFunctionalInterface(interfaceName, this,
                                    (ProActiveInterfaceType) itfType);

                    ((StubObject) interface_reference).setProxy(proxy);
                    // keep it in the list of functional interfaces
                    fcInterfaceReferences.put(interfaceName, interface_reference);
                    return interface_reference;
                } catch (Throwable e) {
                    logger.info("Could not generate " + interfaceName + " collection interface", e);
                }
            }
        }

        throw new NoSuchInterfaceException(interfaceName);
    }

    /*
     *implements  org.objectweb.fractal.api.Component#getFcInterfaces()
     */
    public Object[] getFcInterfaces() {
        Interface[] nfInterfaces = nfInterfaceReferences.values().toArray(
                new Interface[nfInterfaceReferences.size()]);
        Interface[] fcInterfaces = fcInterfaceReferences.values().toArray(
                new Interface[fcInterfaceReferences.size()]);
        Interface[] result = new Interface[nfInterfaces.length + fcInterfaces.length + 1];
        System.arraycopy(nfInterfaces, 0, result, 0, nfInterfaces.length);
        System.arraycopy(fcInterfaces, 0, result, nfInterfaces.length, fcInterfaces.length);
        result[result.length - 1] = this;
        return result;
    }

    /*
     *implements  org.objectweb.fractal.api.Component#getFcType()
     */
    public Type getFcType() {
        return componentType;
    }

    /*
     *implements  org.objectweb.proactive.core.mop.StubObject#getProxy()
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
        ProActiveInterface[] interface_references = new ProActiveInterface[interfaces.length - 1];
        for (int i = 0; i < interfaces.length; i++) {
            if (!interfaces[i].equals(this)) {
                interface_references[i] = (ProActiveInterface) interfaces[i];
            }
        }
        for (int i = 0; i < interface_references.length; i++) {
            if (useShortcuts) {
                // adds an intermediate FunctionalInterfaceProxy for functional interfaces, to manage shortcutting
                ((StubObject) interface_references[i]).setProxy(new FunctionalInterfaceProxyImpl(proxy,
                    interface_references[i].getFcItfName()));
            } else {
                try {
                    ((StubObject) interface_references[i]).setProxy(proxy);
                } catch (RuntimeException e) {
                    logger.error(e.getMessage());
                    throw new ProActiveRuntimeException(e);
                }
            }
        }
    }

    /**
     *  The comparison of component references is actually a comparison of unique
     * identifiers across jvms.
     */
    @Override
    public boolean equals(Object component) {
        Object result = reifyCall(Object.class.getName(), "equals", new Class<?>[] { Object.class },
                new Object[] { component }, ComponentRequest.STRICT_FIFO_PRIORITY);
        return ((Boolean) result).booleanValue();
    }

    @Override
    public int hashCode() {
        // reified as a standard invocation (not a component one)
        Object result;
        try {
            result = proxy.reify(MethodCall.getMethodCall(Class.forName(Object.class.getName())
                    .getDeclaredMethod("hashCode", new Class<?>[] {}), new Object[] {},
                    (Map<TypeVariable, Class<?>>) null));
            return ((Integer) result).intValue();
        } catch (SecurityException e) {
            throw new ProActiveRuntimeException(e.toString());
        } catch (NoSuchMethodException e) {
            throw new ProActiveRuntimeException(e.toString());
        } catch (ClassNotFoundException e) {
            throw new ProActiveRuntimeException(e.toString());
        } catch (Throwable e) {
            throw new ProActiveRuntimeException(e.toString());
        }
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
     * implements org.objectweb.proactive.core.component.identity.ProActiveComponent#getReferenceOnBaseObject()
     */
    public Object getReferenceOnBaseObject() {
        logger.error("getReferenceOnBaseObject() method is not available in component representatives");
        return null;
    }

    /*
     * implements org.objectweb.proactive.core.component.identity.ProActiveComponent#getRepresentativeOnThis()
     */
    public ProActiveComponent getRepresentativeOnThis() {
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

    public boolean isPrimitive() {
        return Constants.PRIMITIVE.equals(hierarchicalType);
    }

    public void _terminateAO(Proxy proxy) {
    }

    public void _terminateAOImmediatly(Proxy proxy) {
    }

    /**
     * @see org.objectweb.fractal.api.Interface#getFcItfName()
     */
    public String getFcItfName() {
        return "component";
    }

    /**
     * @see org.objectweb.fractal.api.Interface#getFcItfOwner()
     */
    public Component getFcItfOwner() {
        return this;
    }

    /**
     * @see org.objectweb.fractal.api.Interface#getFcItfType()
     */
    public Type getFcItfType() {
        return componentType;
    }

    /**
     * @see org.objectweb.fractal.api.Interface#isFcInternalItf()
     */
    public boolean isFcInternalItf() {
        return false;
    }

    @Override
    public String toString() {
        String string = "name : " + getFcItfName() + "\n" + getFcItfType() + "\n" + "isInternal : " +
            isFcInternalItf() + "\n";
        return string;
    }
}
