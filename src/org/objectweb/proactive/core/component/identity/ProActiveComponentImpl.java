/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed, Concurrent
 * computing with Security and Mobility
 *
 * Copyright (C) 1997-2004 INRIA/University of Nice-Sophia Antipolis Contact:
 * proactive-support@inria.fr
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * Initial developer(s): The ProActive Team
 * http://www.inria.fr/oasis/ProActive/contacts.html Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.core.component.identity;

import java.io.File;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.Interface;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.Type;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.ContentController;
import org.objectweb.fractal.api.control.NameController;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.component.ComponentParameters;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.ControllerDescription;
import org.objectweb.proactive.core.component.ProActiveInterface;
import org.objectweb.proactive.core.component.asmgen.MetaObjectInterfaceClassGenerator;
import org.objectweb.proactive.core.component.config.ComponentConfigurationHandler;
import org.objectweb.proactive.core.component.controller.AbstractProActiveController;
import org.objectweb.proactive.core.component.controller.ComponentParametersController;
import org.objectweb.proactive.core.component.controller.RequestHandler;
import org.objectweb.proactive.core.component.interception.Interceptor;
import org.objectweb.proactive.core.component.representative.ProActiveComponentRepresentativeFactory;
import org.objectweb.proactive.core.group.ProActiveComponentGroup;
import org.objectweb.proactive.core.mop.MOP;
import org.objectweb.proactive.core.mop.StubObject;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * The base class for managing components. It builds the "membrane" in the
 * Fractal terminology : the controllers of the components.
 *
 * @author Matthieu Morel
 */
public class ProActiveComponentImpl implements ProActiveComponent, Interface,
    Serializable {
    protected static final Logger logger = ProActiveLogger.getLogger(Loggers.COMPONENTS);

    //private ComponentParameters componentParameters;
    private Interface[] interfaceReferences;
    private Body body;
    private RequestHandler firstControllerRequestHandler;
    private List interceptors = new ArrayList();

    public ProActiveComponentImpl() {
    }

    /**
     * Constructor for ProActiveComponent.
     *
     * @param componentParameters
     * @param myBody
     *                     a reference on the body (required notably to get a reference
     *                     on the request queue, used to control the life cycle of the
     *                     component)
     */
    public ProActiveComponentImpl(ComponentParameters componentParameters,
        Body myBody) {
        this.body = myBody;
        boolean component_is_primitive = componentParameters.getHierarchicalType()
                                                            .equals(Constants.PRIMITIVE);
        
        // add interface references
        ArrayList interface_references_list = new ArrayList(4);

        // 1. component identity
        interface_references_list.add(this);

        //2. control interfaces
        Properties controllers = new Properties();
        boolean current_component_is_primitive = !(componentParameters.getHierarchicalType()
                                                                      .equals(Constants.COMPOSITE) ||
            (componentParameters.getHierarchicalType().equals(Constants.PARALLEL)));
        addControllers(interface_references_list, componentParameters,
            current_component_is_primitive);

        // 3. external functional interfaces
        addFunctionalInterfaces(componentParameters, component_is_primitive,
            interface_references_list, current_component_is_primitive);
        // put all in a table
        interfaceReferences = (Interface[]) interface_references_list.toArray(new Interface[interface_references_list.size()]);
        if (logger.isDebugEnabled()) {
            logger.debug("created component : " +
                componentParameters.getControllerDescription().getName());
        }
    }

    /**
     * @param componentParameters
     * @param component_is_primitive
     * @param interface_references_list
     * @param current_component_is_primitive
     */
    private void addFunctionalInterfaces(
        ComponentParameters componentParameters,
        boolean component_is_primitive, ArrayList interface_references_list,
        boolean current_component_is_primitive) {
        InterfaceType[] interface_types = componentParameters.getComponentType()
                                                             .getFcInterfaceTypes();
        try {
            for (int i = 0; i < interface_types.length; i++) {
                // no interface generated for client itfs of primitive
                // components
                if (!(interface_types[i].isFcClientItf() &&
                        current_component_is_primitive)) {
                    ProActiveInterface itf_ref = null;

                    // if we have a COLLECTION CLIENT interface, we should see
                    // the delegatee ("impl" field) as a group
                    if (interface_types[i].isFcClientItf() &&
                            interface_types[i].isFcCollectionItf()) {
                        itf_ref = createInterfaceOnGroupOfDelegatees(interface_types[i],
                                component_is_primitive);
                    } // if we have a server port of a PARALLEL component, we
                    // also create a group proxy on the delegatee field
                    else if (componentParameters.getHierarchicalType().equals(Constants.PARALLEL) &&
                            (!interface_types[i].isFcClientItf())) {
                        // parallel component have a collective port on their
                        // server interfaces
                        itf_ref = createInterfaceOnGroupOfDelegatees(interface_types[i],
                                component_is_primitive);
                    } 
                    else {
                        itf_ref = MetaObjectInterfaceClassGenerator.instance()
                                                                   .generateFunctionalInterface(interface_types[i].getFcItfName(),
                                this, interface_types[i]);
                        // functional interfaces are external interfaces (at
                        // least they are tagged as external)
                    }

                    // set delegation link
                    if (componentParameters.getHierarchicalType().equals(Constants.PRIMITIVE)) {
                        // TODO_M no group case
                        if (!interface_types[i].isFcCollectionItf()) {
                            if (!interface_types[i].isFcClientItf()) {
                                (itf_ref).setFcItfImpl(getReferenceOnBaseObject());
                            } else if (interface_types[i].isFcClientItf()) {
                                (itf_ref).setFcItfImpl(null);
                            }
                        }
                    } else { // we have a composite component
                    }
                    interface_references_list.add(itf_ref);
                }
            }
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("cannot create interface references : " +
                    e.getMessage());
            }
            e.printStackTrace();
            throw new RuntimeException("cannot create interface references : " +
                e.getMessage());
        }
    }

    private void addControllers(ArrayList interface_references_list,
        ComponentParameters componentParameters, boolean isPrimitive) {
        ComponentConfigurationHandler componentConfiguration = loadComponentConfiguration(componentParameters.getControllerDescription().getControllersConfigFile());
        Map controllers = componentConfiguration.getControllers();
        List interceptorsSignatures = componentConfiguration.getInterceptors();
        //Properties controllers = loadControllersConfiguration(componentParameters.getControllerDescription().getControllersConfigFile());
        Iterator iteratorOnControllers = controllers.keySet().iterator();
        AbstractProActiveController lastController = null;
        while (iteratorOnControllers.hasNext()) {
            Class controllerClass = null;
            AbstractProActiveController currentController;
            String controllerItfName = (String) iteratorOnControllers.next();
            try {
                Class controllerItf = Class.forName(controllerItfName);
                controllerClass = Class.forName((String)controllers.get(
                            controllerItf.getName()));
                Constructor controllerClassConstructor = controllerClass.getConstructor(new Class[] {
                            Component.class
                        });
                currentController = (AbstractProActiveController) controllerClassConstructor.newInstance(new Object[] {
                            this
                        });
                if (Interceptor.class.isAssignableFrom(controllerClass)) {
                    // keep the sequence order of the interceptors
                    interceptors.add(interceptorsSignatures.indexOf(controllerClass.getName()),currentController);
                } else if (interceptorsSignatures.contains(controllerClass.getName())) {
                    logger.error(controllerClass.getName() + " was specified as interceptor in the configuration file, but it is not an interceptor since it does not implement the Interceptor interface");
                }
            } catch (Exception e) {
                logger.error("could not create controller " +
                    controllers.get(controllerItfName) + " : " +
                    e.getMessage());
                continue;
            }
            
            // there are some special cases for some controllers
            if (ComponentParametersController.class.isAssignableFrom(
                        controllerClass)) {
                ((ComponentParametersController) currentController).setComponentParameters(componentParameters);
            }
            if (BindingController.class.isAssignableFrom(controllerClass)) {
                if ((componentParameters.getHierarchicalType().equals(Constants.PRIMITIVE) &&
                        (componentParameters.getClientInterfaceTypes().length == 0))) {
                    //bindingController = null;
                    if (logger.isDebugEnabled()) {
                        logger.debug("user component class of '" +
                            componentParameters.getName() +
                            "' does not have any client interface. It will have no BindingController");
                    }
                    continue;
                }
            }
            if (ContentController.class.isAssignableFrom(controllerClass)) {
                if (isPrimitive) {
                    // no content controller here
                    continue;
                }
            }
            if (NameController.class.isAssignableFrom(controllerClass)) {
                ((NameController)currentController).setFcName(componentParameters.getName());
            }
            if (lastController != null) {
                lastController.setNextHandler(currentController);
            } else {
                firstControllerRequestHandler = currentController;
            }
            lastController = currentController;
            interface_references_list.add(currentController);
        }
    }

    /**
     * @param componentParameters
     * @return
     */
    public static ComponentConfigurationHandler loadComponentConfiguration(File controllerConfigFile) {
        try {
        return ComponentConfigurationHandler.createComponentConfigurationHandler(controllerConfigFile.getPath());
    } catch (Exception e) {
            logger.error("could not load controller config file : " +
                controllerConfigFile.getAbsolutePath() +
                ". Reverting to default controllers configuration.");
            try {
                return ComponentConfigurationHandler.createComponentConfigurationHandler(ProActiveComponent.class.getResource(ControllerDescription.DEFAULT_COMPONENT_CONFIG_FILE_LOCATION)
                        .getFile());
            } catch (Exception e2) {
                logger.error(
                    "could not load default controller config file either. Check that the default controller config file is available in your classpath at : " +
                    ControllerDescription.DEFAULT_COMPONENT_CONFIG_FILE_LOCATION);
                throw new ProActiveRuntimeException(
                    "could not load default controller config file either. Check that the default controller config file is available on your system at : " +
                    ControllerDescription.DEFAULT_COMPONENT_CONFIG_FILE_LOCATION,
                    e2);
            }
        }
    }

    // returns a generated interface reference, whose impl field is a group
    // It is able to handle multiple bindings
    private ProActiveInterface createInterfaceOnGroupOfDelegatees(
        InterfaceType itfType, boolean isPrimitive) throws Exception {
        ProActiveInterface itf_ref = MetaObjectInterfaceClassGenerator.instance()
                                                                      .generateFunctionalInterface(itfType.getFcItfName(),
                this, itfType);

        // create a group of impl target objects
        ProActiveInterface itf_ref_group = ProActiveComponentGroup.newComponentInterfaceGroup(itfType, this);
        itf_ref.setFcItfImpl(itf_ref_group);
        return itf_ref;
    }

    /*
     * see {@link org.objectweb.fractal.api.Component#getFcInterface(String)}
     */
    public Object getFcInterface(String interfaceName)
        throws NoSuchInterfaceException {
        if (interfaceReferences != null) {
            for (int i = 0; i < interfaceReferences.length; i++) {
                // TODO check the following
                //				if (ProActiveGroup.isGroup(interfaceReferences[i])) {
                //					// need to find at least one occurence of the interface
                //					// if exists but not for all elements of the group -> throw
                // error
                //					// if does not exist -> do nothing
                //					int count = 0;
                //					Group itf_ref_group =
                // ProActiveGroup.getGroup(interfaceReferences[i]);
                //					Iterator iterator = itf_ref_group.iterator();
                //					// ensure groups are coherent (ie : if 1 interface of the
                // given name exists,
                //					// all of the group elements should be of this kind
                //					while (iterator.hasNext()) {
                //						Interface group_element = (Interface) iterator.next();
                //						if (group_element.getFcItfName().equals(interfaceName)) {
                //							count++;
                //						}
                //					}
                //					if (count > 0) {
                //						if (count == itf_ref_group.size()) {
                //							return interfaceReferences[i];
                //						} else {
                //							throw new NoSuchInterfaceException(
                //								"some elements of the collection are not named " +
                // interfaceName);
                //						}
                //					}
                //				} else { //looking into single interface
                if (interfaceReferences[i].getFcItfName().equals(interfaceName)) {
                    return interfaceReferences[i];
                }
            }
        }
        throw new NoSuchInterfaceException(interfaceName);
    }

    /*
     * see {@link org.objectweb.fractal.api.Component#getFcInterfaces()}
     */
    public Object[] getFcInterfaces() {
        ArrayList external_interfaces = new ArrayList(interfaceReferences.length);
        for (int i = 0; i < interfaceReferences.length; i++) {
            if (!interfaceReferences[i].isFcInternalItf()) {
                external_interfaces.add(interfaceReferences[i]);
            }
        }
        external_interfaces.trimToSize();
        return external_interfaces.toArray();
    }

    /*
     * see {@link org.objectweb.fractal.api.Component#getFcType()}
     */
    public Type getFcType() {
        try {
            return ((ComponentParametersController) getFcInterface(Constants.COMPONENT_PARAMETERS_CONTROLLER)).getComponentParameters()
                    .getComponentType();
        } catch (NoSuchInterfaceException nsie) {
            nsie.printStackTrace();
            throw new ProActiveRuntimeException("cannot retreive the type of the component",
                nsie);
        }
    }

    /**
     * see {@link org.objectweb.fractal.api.Interface#getFcItfName()}
     */
    public String getFcItfName() {
        return Constants.COMPONENT;
    }

    /**
     * see {@link org.objectweb.fractal.api.Interface#getFcItfOwner()}
     */
    public Component getFcItfOwner() {
        return (Component) this;
    }

    /**
     * see {@link org.objectweb.fractal.api.Interface#getFcItfType()}
     */
    public Type getFcItfType() {
        return getFcType();
    }

    /**
     * see {@link org.objectweb.fractal.api.Interface#isFcInternalItf()}
     */
    public boolean isFcInternalItf() {
        return true;
    }

    /**
     * Returns the base object. If the component is a composite, a basic
     * do-nothing instance of class Composite is returned.
     *
     * @return the base object underneath
     */
    public Object getReferenceOnBaseObject() {
        return getBody().getReifiedObject();
    }

    /**
     * @return a ComponentParameters instance, corresponding to the
     *                configuration of the current component
     */
    public ComponentParameters getComponentParameters()
        throws NoSuchInterfaceException {
        //return componentParameters;
        return ((ComponentParametersController) getFcInterface(Constants.COMPONENT_PARAMETERS_CONTROLLER)).getComponentParameters();
    }

    /**
     * @return the body of the current active object
     */
    public Body getBody() {
        return body;
    }

    /**
     * see
     * {@link org.objectweb.proactive.core.component.identity.ProActiveComponent#getID()}
     */
    public UniqueID getID() {
        return getBody().getID();
    }

    /**
     * see
     * {@link org.objectweb.proactive.core.component.identity.ProActiveComponent#getRepresentativeOnThis()}
     */
    public Component getRepresentativeOnThis() {
        try {
            return ProActiveComponentRepresentativeFactory.instance()
                                                          .createComponentRepresentative((ComponentType) getFcType(),
                getComponentParameters().getHierarchicalType(),
                ((StubObject) MOP.turnReified(body.getReifiedObject().getClass()
                                                  .getName(),
                    org.objectweb.proactive.core.Constants.DEFAULT_BODY_PROXY_CLASS_NAME,
                    new Object[] { body }, body.getReifiedObject())).getProxy(), getComponentParameters().getControllerDescription().getControllersConfigFile());
        } catch (Exception e) {
            throw new ProActiveRuntimeException("This component could not generate a reference on itself",
                e);
        }
    }

    /**
     * @return the first controller request handler in the chain of controllers 
     */
    public RequestHandler getControllerRequestHandler() {
        return firstControllerRequestHandler;
    }

    public List getInterceptors() {
        return interceptors;
    }
    
    
}
