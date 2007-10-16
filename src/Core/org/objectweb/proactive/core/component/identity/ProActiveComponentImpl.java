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
package org.objectweb.proactive.core.component.identity;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.Interface;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.Type;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.ContentController;
import org.objectweb.fractal.api.control.NameController;
import org.objectweb.fractal.api.factory.InstantiationException;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.migration.MigrationException;
import org.objectweb.proactive.core.component.ComponentParameters;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.ControllerDescription;
import org.objectweb.proactive.core.component.Fractive;
import org.objectweb.proactive.core.component.ProActiveInterface;
import org.objectweb.proactive.core.component.config.ComponentConfigurationHandler;
import org.objectweb.proactive.core.component.controller.AbstractProActiveController;
import org.objectweb.proactive.core.component.controller.AbstractRequestHandler;
import org.objectweb.proactive.core.component.controller.ComponentParametersController;
import org.objectweb.proactive.core.component.controller.ProActiveController;
import org.objectweb.proactive.core.component.controller.RequestHandler;
import org.objectweb.proactive.core.component.exceptions.InterfaceGenerationFailedException;
import org.objectweb.proactive.core.component.gen.MetaObjectInterfaceClassGenerator;
import org.objectweb.proactive.core.component.interception.InputInterceptor;
import org.objectweb.proactive.core.component.interception.OutputInterceptor;
import org.objectweb.proactive.core.component.representative.ProActiveComponentRepresentativeFactory;
import org.objectweb.proactive.core.component.type.ProActiveInterfaceType;
import org.objectweb.proactive.core.group.ProActiveComponentGroup;
import org.objectweb.proactive.core.mop.MOP;
import org.objectweb.proactive.core.mop.StubObject;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * The base class for managing components. It builds the "membrane" in the
 * Fractal terminology : the controllers of the components.
 *
 * @author Matthieu Morel
 */
public class ProActiveComponentImpl extends AbstractRequestHandler
    implements ProActiveComponent, Interface, Serializable {
    protected static final Logger logger = ProActiveLogger.getLogger(Loggers.COMPONENTS);
    private transient ProActiveComponent representativeOnMyself = null;
    private Map<String, Interface> serverItfs = new HashMap<String, Interface>();
    private Map<String, Interface> clientItfs = new HashMap<String, Interface>();
    private Map<String, ProActiveController> controlItfs = new HashMap<String, ProActiveController>();
    private Map<String, Interface> collectionItfsMembers = new HashMap<String, Interface>();
    private Body body;
    private RequestHandler firstControllerRequestHandler;

    // need Vector-specific operations for inserting elements
    private Vector<AbstractProActiveController> inputInterceptors = new Vector<AbstractProActiveController>();
    private Vector<AbstractProActiveController> outputInterceptors = new Vector<AbstractProActiveController>();

    public ProActiveComponentImpl() {
    }

    /**
     * Constructor for ProActiveComponent.
     *
     * @param componentParameters
     * @param myBody
     *            a reference on the body (required notably to get a reference
     *            on the request queue, used to control the life cycle of the
     *            component)
     * @throws InstantiationException
     */
    public ProActiveComponentImpl(ComponentParameters componentParameters,
        Body myBody) {
        this.body = myBody;
        boolean component_is_primitive = componentParameters.getHierarchicalType()
                                                            .equals(Constants.PRIMITIVE);

        // add interface references
        // ArrayList<Interface> interface_references_list = new ArrayList<Interface>(4);

        // 1. component identity
        //interface_references_list.add(this);

        // 2. control interfaces
        addControllers(componentParameters, component_is_primitive);

        // 3. external functional interfaces
        addFunctionalInterfaces(componentParameters, component_is_primitive);

        // put all in a table
        // interfaceReferences = interface_references_list.toArray(new Interface[interface_references_list.size()]);
        if (logger.isDebugEnabled()) {
            logger.debug("created component : " +
                componentParameters.getControllerDescription().getName());
        }
    }

    /**
     * @param componentParameters
     * @param component_is_primitive
     */
    private void addFunctionalInterfaces(
        ComponentParameters componentParameters, boolean component_is_primitive) {
        InterfaceType[] tmp = componentParameters.getComponentType()
                                                 .getFcInterfaceTypes();
        ProActiveInterfaceType[] interface_types = new ProActiveInterfaceType[tmp.length];
        System.arraycopy(tmp, 0, interface_types, 0, tmp.length);

        try {
            for (int i = 0; i < interface_types.length; i++) {
                ProActiveInterface itf_ref = null;

                if (interface_types[i].isFcCollectionItf()) {
                    // members of collection itfs are created dynamically
                    continue;
                }
                if (interface_types[i].isFcMulticastItf()) {
                    itf_ref = createInterfaceOnGroupOfDelegatees(interface_types[i]);
                    //                    itf_ref = ProActiveComponentGroup.newComponentInterfaceGroup(interface_types[i],
                    //                            getFcItfOwner());
                } else {
                    // no interface generated for client itfs of primitive
                    // components
                    if (!(interface_types[i].isFcClientItf() &&
                            component_is_primitive)) {
                        itf_ref = MetaObjectInterfaceClassGenerator.instance()
                                                                   .generateFunctionalInterface(interface_types[i].getFcItfName(),
                                this, interface_types[i]);
                        // server functional interfaces are external interfaces (at
                        // least they are tagged as external)

                        // set delegation link
                        if (componentParameters.getHierarchicalType()
                                                   .equals(Constants.PRIMITIVE)) {
                            if (!interface_types[i].isFcCollectionItf()) {
                                if (!interface_types[i].isFcClientItf()) {
                                    (itf_ref).setFcItfImpl(getReferenceOnBaseObject());
                                } else if (interface_types[i].isFcClientItf()) {
                                    (itf_ref).setFcItfImpl(null);
                                }
                            }
                        }
                    }

                    // non multicast client itf of primitive comp : do nothing
                }

                if (!interface_types[i].isFcClientItf()) {
                    //System.err.println("SERVER" + interface_types[i].getFcItfName() + itf_ref );
                    serverItfs.put(interface_types[i].getFcItfName(), itf_ref);
                } else if (itf_ref != null) {
                    //System.err.println("CLIENT" + interface_types[i].getFcItfName() + itf_ref );
                    clientItfs.put(interface_types[i].getFcItfName(), itf_ref);
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

    private void addControllers(ComponentParameters componentParameters,
        boolean isPrimitive) {
        ComponentConfigurationHandler componentConfiguration = ProActiveComponentImpl.loadControllerConfiguration(componentParameters.getControllerDescription()
                                                                                                                                     .getControllersConfigFileLocation());
        Map controllers = componentConfiguration.getControllers();
        List inputInterceptorsSignatures = componentConfiguration.getInputInterceptors();
        inputInterceptors.setSize(inputInterceptorsSignatures.size());
        List outputInterceptorsSignatures = componentConfiguration.getOutputInterceptors();
        outputInterceptors.setSize(outputInterceptorsSignatures.size());

        // Properties controllers =
        // loadControllersConfiguration(componentParameters.getControllerDescription().getControllersConfigFile());
        Iterator iteratorOnControllers = controllers.keySet().iterator();
        AbstractProActiveController lastController = null;

        while (iteratorOnControllers.hasNext()) {
            Class<?> controllerClass = null;
            AbstractProActiveController currentController;
            String controllerItfName = (String) iteratorOnControllers.next();

            try {
                if (null == controllerItfName) {
                    throw new Exception(
                        "You must specify the java interface of a controller.");
                }
                Class<?> controllerItf = Class.forName(controllerItfName);
                if (null == controllers.get(controllerItf.getName())) {
                    throw new Exception(
                        "You must specify the java implementation for the controller describe by the interface " +
                        controllerItfName + ".");
                }
                controllerClass = Class.forName((String) controllers.get(
                            controllerItf.getName()));
                Constructor<?> controllerClassConstructor = controllerClass.getConstructor(new Class[] {
                            Component.class
                        });
                currentController = (AbstractProActiveController) controllerClassConstructor.newInstance(new Object[] {
                            this
                        });

                // add interceptor
                if (InputInterceptor.class.isAssignableFrom(controllerClass)) {
                    // keep the sequence order of the interceptors
                    inputInterceptors.setElementAt(currentController,
                        inputInterceptorsSignatures.indexOf(
                            controllerClass.getName()));
                } else if (inputInterceptorsSignatures.contains(
                            controllerClass.getName())) {
                    logger.error(controllerClass.getName() +
                        " was specified as input interceptor in the configuration file, but it is not an input interceptor since it does not implement the InputInterceptor interface");
                }

                if (OutputInterceptor.class.isAssignableFrom(controllerClass)) {
                    outputInterceptors.setElementAt(currentController,
                        outputInterceptorsSignatures.indexOf(
                            controllerClass.getName()));
                } else if (outputInterceptorsSignatures.contains(
                            controllerClass.getName())) {
                    logger.error(controllerClass.getName() +
                        " was specified as output interceptor in the configuration file, but it is not an output interceptor since it does not implement the OutputInterceptor interface");
                }
            } catch (Exception e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("could not create controller", e);
                }
                throw new ProActiveRuntimeException(
                    "could not create controller '" +
                    controllers.get(controllerItfName) +
                    "' (please check your configuration file " +
                    componentParameters.getControllerDescription()
                                       .getControllersConfigFileLocation() +
                    " : " + e.getMessage(), e);
            }

            // there are some special cases for some controllers
            if (ComponentParametersController.class.isAssignableFrom(
                        controllerClass)) {
                ((ComponentParametersController) currentController).setComponentParameters(componentParameters);
            }

            if (BindingController.class.isAssignableFrom(controllerClass)) {
                if ((componentParameters.getHierarchicalType()
                                            .equals(Constants.PRIMITIVE) &&
                        (componentParameters.getClientInterfaceTypes().length == 0))) {
                    // bindingController = null;
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
                ((NameController) currentController).setFcName(componentParameters.getName());
            }

            if (lastController != null) {
                lastController.setNextHandler(currentController);
            } else {
                firstControllerRequestHandler = currentController;
            }

            lastController = currentController;
            controlItfs.put(currentController.getFcItfName(), currentController);
        }

        // add the "component" control itfs
        lastController.setNextHandler(this);
    }

    /**
     * @param controllerConfigFileLocation
     *            the location of the configuration file
     * @return a xml parsing handler
     */
    public static ComponentConfigurationHandler loadControllerConfiguration(
        String controllerConfigFileLocation) {
        try {
            return ComponentConfigurationHandler.createComponentConfigurationHandler(controllerConfigFileLocation);
        } catch (Exception e) {
            logger.error("could not load controller config file : " +
                controllerConfigFileLocation +
                ". Reverting to default controllers configuration.");

            try {
                return ComponentConfigurationHandler.createComponentConfigurationHandler(ControllerDescription.DEFAULT_COMPONENT_CONFIG_FILE_LOCATION);
            } catch (Exception e1) {
                logger.error(
                    "could not load default controller config file either. Check that the default controller config file is available in your classpath at : " +
                    ControllerDescription.DEFAULT_COMPONENT_CONFIG_FILE_LOCATION);
                throw new ProActiveRuntimeException(
                    "could not load default controller config file either. Check that the default controller config file is available on your system at : " +
                    ControllerDescription.DEFAULT_COMPONENT_CONFIG_FILE_LOCATION,
                    e1);
            }
        }
    }

    // returns a generated interface reference, whose impl field is a group
    // It is able to handle multiple bindings
    private ProActiveInterface createInterfaceOnGroupOfDelegatees(
        ProActiveInterfaceType itfType) throws Exception {
        ProActiveInterface itf_ref = MetaObjectInterfaceClassGenerator.instance()
                                                                      .generateFunctionalInterface(itfType.getFcItfName(),
                this, itfType);

        // create a group of impl target objects
        ProActiveInterface itf_ref_group = ProActiveComponentGroup.newComponentInterfaceGroup(itfType,
                this);
        itf_ref.setFcItfImpl(itf_ref_group);
        return itf_ref;
    }

    /*
     * see {@link org.objectweb.fractal.api.Component#getFcInterface(String)}
     */
    public Object getFcInterface(String interfaceName)
        throws NoSuchInterfaceException {
        if (!("attribute-controller".equals(interfaceName)) &&
                (interfaceName.endsWith("-controller"))) {
            if (!controlItfs.containsKey(interfaceName)) {
                throw new NoSuchInterfaceException(interfaceName);
            }
            return (controlItfs.get(interfaceName));
        }
        if (interfaceName.equals("component")) {
            return this;
        }
        if (serverItfs.containsKey(interfaceName)) {
            return serverItfs.get(interfaceName);
        }
        if (clientItfs.containsKey(interfaceName)) {
            return clientItfs.get(interfaceName);
        }

        // a member of a collection itf?
        InterfaceType[] itfTypes = ((ComponentType) getFcType()).getFcInterfaceTypes();
        for (int i = 0; i < itfTypes.length; i++) {
            InterfaceType type = itfTypes[i];
            if (type.isFcCollectionItf()) {
                if ((interfaceName.startsWith(type.getFcItfName()) &&
                        !type.getFcItfName().equals(interfaceName))) {
                    if (collectionItfsMembers.containsKey(interfaceName)) {
                        return collectionItfsMembers.get(interfaceName);
                    } else {
                        // generate a new interface and add it to the list of members of collection its
                        try {
                            Interface clientItf = MetaObjectInterfaceClassGenerator.instance()
                                                                                   .generateFunctionalInterface(interfaceName,
                                    this, (ProActiveInterfaceType) itfTypes[i]);
                            collectionItfsMembers.put(interfaceName, clientItf);
                            return clientItf;
                        } catch (InterfaceGenerationFailedException e1) {
                            logger.info("Generation of the interface '" +
                                interfaceName + "' failed.", e1);
                        }
                    }
                }
            }
        }

        throw new NoSuchInterfaceException(interfaceName);
    }

    /*
     * see {@link org.objectweb.fractal.api.Component#getFcInterfaces()}
     */
    public Object[] getFcInterfaces() {
        //        List<Object> itfs = new ArrayList<Object>(15); //we have at least 10 control itfs
        //
        //        // add interface component
        //        itfs.add(this);
        //        // add controller interface
        //        for (Object object : controlItfs.values()) {
        //            itfs.add(object);
        //        }
        //
        //        //add server interface
        //        for (Object object : serverItfs.values()) {
        //            itfs.add(object);
        //        }
        //
        //        //add client interface
        //        for (Object object : clientItfs.values()) {
        //            itfs.add(object);
        //        }
        //
        //        return itfs.toArray(new Object[itfs.size()]);
        return getRepresentativeOnThis().getFcInterfaces();
    }

    /*
     * see {@link org.objectweb.fractal.api.Component#getFcType()}
     */
    public Type getFcType() {
        try {
            return Fractive.getComponentParametersController(getFcItfOwner())
                           .getComponentParameters().getComponentType();
        } catch (NoSuchInterfaceException nsie) {
            throw new ProActiveRuntimeException("There is no component parameters controller for this component, cannot retreive the type of the component.",
                nsie);
        }
    }

    /**
     * see {@link org.objectweb.fractal.api.Interface#getFcItfName()}
     */
    @Override
    public String getFcItfName() {
        return Constants.COMPONENT;
    }

    /**
     * see {@link org.objectweb.fractal.api.Interface#getFcItfOwner()}
     */
    @Override
    public Component getFcItfOwner() {
        return this;
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
     *         configuration of the current component
     */
    public ComponentParameters getComponentParameters()
        throws NoSuchInterfaceException {
        // return componentParameters;
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
     * There should be no generic return type in the methods of the representative class
     */
    public ProActiveComponent getRepresentativeOnThis() {
        // optimization : cache self reference
        if (representativeOnMyself != null) {
            return representativeOnMyself;
        }

        try {
            return representativeOnMyself = ProActiveComponentRepresentativeFactory.instance()
                                                                                   .createComponentRepresentative((ComponentType) getFcType(),
                    getComponentParameters().getHierarchicalType(),
                    ((StubObject) MOP.turnReified(body.getReifiedObject()
                                                      .getClass().getName(),
                        org.objectweb.proactive.core.Constants.DEFAULT_BODY_PROXY_CLASS_NAME,
                        new Object[] { body }, body.getReifiedObject(), null)).getProxy(),
                    getComponentParameters().getControllerDescription()
                        .getControllersConfigFileLocation());
        } catch (Exception e) {
            throw new ProActiveRuntimeException("This component could not generate a reference on itself",
                e);
        }
    }

    @Override
    public String toString() {
        String string = "name : " + getFcItfName() + "\n" + getFcItfType() +
            "\n" + "isInternal : " + isFcInternalItf() + "\n";
        return string;
    }

    /**
     * @return the first controller request handler in the chain of controllers
     */
    public RequestHandler getControllerRequestHandler() {
        return firstControllerRequestHandler;
    }

    public List<AbstractProActiveController> getInputInterceptors() {
        return inputInterceptors;
    }

    public List<AbstractProActiveController> getOutputInterceptors() {
        return outputInterceptors;
    }

    public void migrateControllersDependentActiveObjectsTo(Node node)
        throws MigrationException {
        for (ProActiveController controller : controlItfs.values()) {
            controller.migrateDependentActiveObjectsTo(node);
        }
    }

    private void writeObject(java.io.ObjectOutputStream out)
        throws java.io.IOException {
        //        System.out.println("writing ProActiveComponentImpl");
        out.defaultWriteObject();
    }

    private void readObject(java.io.ObjectInputStream in)
        throws java.io.IOException, ClassNotFoundException {
        //        System.out.println("reading ProActiveComponentImpl");
        in.defaultReadObject();
    }
}
