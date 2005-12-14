/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2005 INRIA/University of Nice-Sophia Antipolis
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
package org.objectweb.proactive.core.component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;

import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.Type;
import org.objectweb.fractal.api.factory.Factory;
import org.objectweb.fractal.api.factory.GenericFactory;
import org.objectweb.fractal.api.factory.InstantiationException;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.fractal.api.type.TypeFactory;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.body.ProActiveMetaObjectFactory;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.http.HttpBodyAdapter;
import org.objectweb.proactive.core.body.ibis.IbisBodyAdapter;
import org.objectweb.proactive.core.body.rmi.RmiBodyAdapter;
import org.objectweb.proactive.core.body.rmi.SshRmiBodyAdapter;
import org.objectweb.proactive.core.component.body.ComponentBody;
import org.objectweb.proactive.core.component.controller.ComponentParametersController;
import org.objectweb.proactive.core.component.identity.ProActiveComponent;
import org.objectweb.proactive.core.component.representative.ProActiveComponentRepresentative;
import org.objectweb.proactive.core.component.representative.ProActiveComponentRepresentativeFactory;
import org.objectweb.proactive.core.component.type.Composite;
import org.objectweb.proactive.core.component.type.ParallelComposite;
import org.objectweb.proactive.core.component.type.ProActiveTypeFactory;
import org.objectweb.proactive.core.group.Group;
import org.objectweb.proactive.core.group.ProActiveComponentGroup;
import org.objectweb.proactive.core.group.ProActiveGroup;
import org.objectweb.proactive.core.mop.StubObject;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.UrlBuilder;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * This class is used for creating components.
 * It acts as : <ol>
 * <li> a bootstrap component</li>
 * <li> a GenericFactory for instantiating new components</li>
 * <li> a utility class providing static methods to create collective interfaces and retreive references to ComponentParametersController</li>
 * </ol>
 * @author Matthieu Morel
 */
public class Fractive implements GenericFactory, Component, Factory {
    private static Fractive instance = null;
    private TypeFactory typeFactory = (TypeFactory) ProActiveTypeFactory.instance();
    private Type type = null;
    private static Logger logger = ProActiveLogger.getLogger(Loggers.COMPONENTS);

    /**
     * no-arg constructor (used by Fractal to get a bootstrap component)
     *
     */
    public Fractive() {
    }

    /**
     * Method instance.
     * @return Fractive
     */
    private static Fractive instance() {
        if (instance == null) {
            instance = new Fractive();
        }
        return instance;
    }

    /**
     * Returns the {@link org.objectweb.fractal.api.control.ContentController} interface of the given component.
     *
     * @param component a component.
     * @return the {@link org.objectweb.fractal.api.control.ContentController} interface of the given component.
     * @throws NoSuchInterfaceException if there is no such interface.
     */
    public static ComponentParametersController getComponentParametersController(
        final Component component) throws NoSuchInterfaceException {
        return (ComponentParametersController) component.getFcInterface(Constants.COMPONENT_PARAMETERS_CONTROLLER);
    }

    /**
     * Returns a generated interface reference, whose impl field is a group It
     * is able to handle multiple bindings, and automatically adds it to an owner component
     *
     * @param itfName the name of the interface
     * @param itfSignature the signature of the interface
     * @param owner the component to which this interface belongs
     * @return ProActiveInterface the resulting collective client interface
     * @throws ProActiveRuntimeException in case of a problem while creating the collective interface
     */
    public static ProActiveInterface createCollectiveClientInterface(
        String itfName, String itfSignature, Component owner)
        throws ProActiveRuntimeException {
        try {
            InterfaceType itf_type = ProActiveTypeFactory.instance()
                                                         .createFcItfType(itfName,
                    itfSignature, TypeFactory.CLIENT, TypeFactory.MANDATORY,
                    TypeFactory.COLLECTION);
            ProActiveInterface itf_ref_group = ProActiveComponentGroup.newComponentInterfaceGroup(itf_type,
                    owner);
            return itf_ref_group;
        } catch (Exception e) {
            throw new ProActiveRuntimeException("Impossible to create a collective client interface ",
                e);
        }
    }

    /**
     * Returns a generated interface reference, whose impl field is a group It
     * is able to handle multiple bindings
     *
     * @param itfName the name of the interface
     * @param itfSignature the signature of the interface
     * @return ProActiveInterface the resulting collective client interface
     * @throws ProActiveRuntimeException in case of a problem while creating the collective interface
     */
    public static ProActiveInterface createCollectiveClientInterface(
        String itfName, String itfSignature) throws ProActiveRuntimeException {
        return Fractive.createCollectiveClientInterface(itfName, itfSignature,
            null);
    }

    private static Component newFcInstance(ContentDescription contentDesc,
        ComponentParameters componentParameters) throws InstantiationException {
        //MetaObjectFactory factory = null;
        try {
            // instantiate the component metaobject factory with parameters of the component
            if (contentDesc.getFactory() == null) {
                // first create a hashtable with the parameters
                Hashtable factory_params = new Hashtable(1);
                factory_params.put(ProActiveMetaObjectFactory.COMPONENT_PARAMETERS_KEY,
                    componentParameters);
                if (componentParameters.getControllerDescription()
                                           .isSynchronous() &&
                        (Constants.COMPOSITE.equals(
                            componentParameters.getHierarchicalType()) ||
                        Constants.PARALLEL.equals(
                            componentParameters.getHierarchicalType()))) {
                    factory_params.put(ProActiveMetaObjectFactory.SYNCHRONOUS_COMPOSITE_COMPONENT_KEY,
                        new Boolean(Constants.SYNCHRONOUS));
                }
                contentDesc.setFactory(new ProActiveMetaObjectFactory(
                        factory_params));
                //		   factory = ProActiveComponentMetaObjectFactory.newInstance(componentParameters);
            }

            // TODO_M : add controllers in the component metaobject factory?
            Object ao = null;

            // 2 possibilities : either the component is created on a node (or null), or it is created on a virtual node
            if (!contentDesc.isLocalizedOnAVirtualNode()) {
                // case 1. Node
                ao = ProActive.newActive(contentDesc.getClassName(),
                        contentDesc.getConstructorParameters(),
                        contentDesc.getNode(), contentDesc.getActivity(),
                        contentDesc.getFactory());
            } else {
                // case 2. Virtual Node
                contentDesc.getVirtualNode().activate();
                if (contentDesc.getVirtualNode().getNodes().length == 0) {
                    throw new InstantiationException(
                        "Cannot create component on virtual node as no node is associated with this virtual node");
                }
                Node[] nodes = contentDesc.getVirtualNode().getNodes();
                if ((nodes.length > 1) && !contentDesc.uniqueInstance()) { // cyclic node + 1 instance per node
                    //Component components = (Component) ProActiveGroup.newGroup(Component.class.getName());
                    Component components = ProActiveComponentGroup.newComponentRepresentativeGroup(componentParameters.getComponentType(),
                            componentParameters.getControllerDescription());
                    Group group_of_components = ProActiveGroup.getGroup(components);

                    if (componentParameters.getHierarchicalType().equals(Constants.PRIMITIVE)) {
                        // task = instantiate a component with a different name 
                        // on each of the node mapped to the given virtual node
                        String original_component_name = componentParameters.getName();
                        contentDesc.getVirtualNode().activate();

                        for (int i = 0; i < nodes.length; i++) {
                            // change the name of each component (add a suffix)
                            String new_name = original_component_name +
                                Constants.CYCLIC_NODE_SUFFIX + i;
                            componentParameters.setName(new_name);
                            // change location of each component 
                            contentDesc.setNode(nodes[i]);
                            group_of_components.add(Fractive.newFcInstance(
                                    contentDesc, componentParameters));
                        }

                        return components;
                    } else {
                        // component is a parallel or a composite : it will be created on the first node from this virtual node
                        ao = ProActive.newActive(contentDesc.getClassName(),
                                contentDesc.getConstructorParameters(),
                                contentDesc.getVirtualNode().getNode(),
                                contentDesc.getActivity(),
                                contentDesc.getFactory());
                    }
                } else {
                    // this is the case where contentDesc.getVirtualNode().getNodeCount() == 1) {
                    // or when virtual node is multiple but only 1 component should be instantiated on the virtual node 
                    // create the component on the first node retreived from the virtual node
                    ao = ProActive.newActive(contentDesc.getClassName(),
                            contentDesc.getConstructorParameters(),
                            contentDesc.getVirtualNode().getNode(),
                            contentDesc.getActivity(), contentDesc.getFactory());
                }
            }

            // Find the proxy
            org.objectweb.proactive.core.mop.Proxy myProxy = ((StubObject) ao).getProxy();
            if (myProxy == null) {
                throw new ProActiveRuntimeException(
                    "Cannot find a Proxy on the stub object: " + ao);
            }
            ProActiveComponentRepresentative representative = ProActiveComponentRepresentativeFactory.instance()
                                                                                                     .createComponentRepresentative(componentParameters.getComponentType(),
                    componentParameters.getHierarchicalType(), myProxy,
                    componentParameters.getControllerDescription()
                                       .getControllersConfigFileLocation());
            representative.setStubOnBaseObject((StubObject) ao);
            return representative;
        } catch (ActiveObjectCreationException e) {
            throw new InstantiationException(e.getMessage());
        } catch (NodeException e) {
            throw new InstantiationException(e.getMessage());
        } catch (ClassNotFoundException e) {
            throw new InstantiationException(e.getMessage());
        }
    }

    /**
     * Method newFcInstance.
     * @param type Type
     * @param controllerDesc ControllerDescription
     * @param contentDesc ContentDescription
     * @return Component
     * @throws InstantiationException
     */
    private Component newFcInstance(Type type,
        ControllerDescription controllerDesc, ContentDescription contentDesc)
        throws InstantiationException {
        if (contentDesc == null) {
            // either a parallel or a composite component, no activitiy/factory/node specified
            if (Constants.COMPOSITE.equals(controllerDesc.getHierarchicalType())) {
                contentDesc = new ContentDescription(Composite.class.getName());
            } else if (Constants.PARALLEL.equals(
                        controllerDesc.getHierarchicalType())) {
                contentDesc = new ContentDescription(ParallelComposite.class.getName());
            } else {
                throw new InstantiationException(
                    "Content can be null only if the hierarchical type of the component is composite or parallel");
            }
        }
        ComponentParameters component_params = new ComponentParameters((ComponentType) type,
                controllerDesc);
        return Fractive.newFcInstance(contentDesc, component_params);
    }

    /**
     * @param componentType Type the type of the component
     * @param controllerDesc Object a description of the controller. In this implementation, a {@link ControllerDescription} object
     * @param contentDesc Object a description of the content. In this implementation, a {@link ContentDescription} object
     * @return Component a reference on the instantiated component
     * @throws InstantiationException if the instantiation of the component failed
     * @see org.objectweb.fractal.api.factory.GenericFactory#newFcInstance(Type, Object, Object)
     */
    public Component newFcInstance(Type componentType, Object controllerDesc,
        Object contentDesc) throws InstantiationException {
        try {
            return newFcInstance(componentType,
                (ControllerDescription) controllerDesc,
                (ContentDescription) contentDesc);
        } catch (ClassCastException e) {
            if ((componentType == null) && (controllerDesc == null) &&
                    (contentDesc instanceof Map)) {
                // for compatibility with the new org.objectweb.fractal.util.Fractal class
                return this;
            }
            if ((controllerDesc instanceof ControllerDescription) &&
                    ((contentDesc instanceof String) || (contentDesc == null))) {
                // for the ADL, when only type and ControllerDescription are given
                return newFcInstance(componentType, controllerDesc,
                    (contentDesc == null) ? null
                                          : new ContentDescription(
                        (String) contentDesc));
            }

            // code compatibility with Julia
            if ("composite".equals(controllerDesc) && (contentDesc == null)) {
                return newFcInstance(componentType,
                    new ControllerDescription(null, Constants.COMPOSITE), null);
            }
            if ("primitive".equals(controllerDesc) &&
                    (contentDesc instanceof String)) {
                return newFcInstance(componentType,
                    new ControllerDescription(null, Constants.PRIMITIVE),
                    new ContentDescription((String) contentDesc));
            }
            if ("parallel".equals(controllerDesc) && (contentDesc == null)) {
                return newFcInstance(componentType,
                    new ControllerDescription(null, Constants.PARALLEL), null);
            }

            // any other case
            throw new InstantiationException(
                "With this implementation, parameters must be of respective types : " +
                Type.class.getName() + ',' +
                ControllerDescription.class.getName() + ',' +
                ContentDescription.class.getName());
        }
    }

    /*
     *
     * @see org.objectweb.fractal.api.Component#getFcInterface(java.lang.String)
     */
    public Object getFcInterface(String itfName)
        throws NoSuchInterfaceException {
        if ("generic-factory".equals(itfName)) {
            return this;
        } else if ("type-factory".equals(itfName)) {
            return typeFactory;
        } else {
            throw new NoSuchInterfaceException(itfName);
        }
    }

    /*
     *
     * @see org.objectweb.fractal.api.Component#getFcInterfaces()
     */
    public Object[] getFcInterfaces() {
        return null;
    }

    /*
     *
     * @see org.objectweb.fractal.api.Component#getFcType()
     */
    public Type getFcType() {
        if (type == null) {
            try {
                return type = typeFactory.createFcType(new InterfaceType[] {
                            typeFactory.createFcItfType("generic-factory",
                                GenericFactory.class.getName(), false, false,
                                false),
                            typeFactory.createFcItfType("type-factory",
                                TypeFactory.class.getName(), false, false, false)
                        });
            } catch (InstantiationException e) {
                ProActiveLogger.getLogger(Loggers.COMPONENTS).error(e.getMessage());
                return null;
            }
        } else {
            return type;
        }
    }

    /*
     *
     * @see org.objectweb.fractal.api.factory.Factory#getFcContentDesc()
     */
    public Object getFcContentDesc() {
        return null;
    }

    /*
     *
     * @see org.objectweb.fractal.api.factory.Factory#getFcControllerDesc()
     */
    public Object getFcControllerDesc() {
        return null;
    }

    /*
     *
     * @see org.objectweb.fractal.api.factory.Factory#getFcInstanceType()
     */
    public Type getFcInstanceType() {
        return null;
    }

    /*
     *
     * @see org.objectweb.fractal.api.factory.Factory#newFcInstance()
     */
    public Component newFcInstance() throws InstantiationException {
        return this;
    }

    /**
     * Helper method for extracting the types of client interfaces from the type of a component
     * @param componentType ComponentType
     * @return the types of client interfacess
     */
    public static InterfaceType[] getClientInterfaceTypes(
        ComponentType componentType) {
        ArrayList client_interfaces = new ArrayList();
        InterfaceType[] interfaceTypes = componentType.getFcInterfaceTypes();
        for (int i = 0; i < interfaceTypes.length; i++) {
            if (interfaceTypes[i].isFcClientItf()) {
                client_interfaces.add(interfaceTypes[i]);
            }
        }
        return (InterfaceType[]) client_interfaces.toArray(new InterfaceType[client_interfaces.size()]);
    }

    /**
     * Returns a component representative pointing to the component associated to the component
     * whose active thread is calling this method. It can be used for a component to pass callback references to itself.
     * @return a component representative for the component in which the current thread is running
     */
    public static Component getComponentRepresentativeOnThis() {
        ComponentBody componentBody;
        try {
            componentBody = (ComponentBody) ProActive.getBodyOnThis();
        } catch (ClassCastException e) {
            logger.error(
                "Cannot get a component representative from the current object, because this object is not a component");
            return null;
        }
        ProActiveComponent currentComponent = componentBody.getProActiveComponentImpl();
        return currentComponent.getRepresentativeOnThis();
    }

    /**
     * Registers a component into a registry(RMI or IBIS or HTTP, default is RMI).
     * This method is similar to {@link ProActive#register(Object, String)}.
     * @param ref the component to register.
     * @param url the url under which the component is registered. The url must point to the localhost
     * since registering is always a local action. The url can take the form:protocol://localhost:port/nam
     * or //localhost:port/name if protocol is RMI or //localhost/name if port is 1099 or only the name.
     * The registered component will be reachable with the following url: protocol://machine_name:port/name
     * using lookupActive method. Protocol and port can be removed if default
     * @exception java.io.IOException if the remote body cannot be registered
     */
    public static void register(Component ref, String url)
        throws IOException {
        if (!(ref instanceof ProActiveComponentRepresentative)) {
            throw new IllegalArgumentException(
                "This method can only register ProActive components");
        }
        ProActive.register(ref, url);
    }

    /**
     * Looks-up a component object previously registered in a registry(RMI, IBIS, HTTP).
     * It is similar to the {@link ProActive#lookupActive(String, String)} method.
     *
     * @param url the url under which the remote component is registered. The url takes the following form:
     * protocol://machine_name:port/name. Protocol and port can be ommited if respectively RMI and 1099:
     * //machine_name/name
     * @return a remote reference on a Component
     * @exception java.io.IOException if the remote component cannot be found under the given url
     */
    public static ProActiveComponentRepresentative lookup(String url)
        throws IOException, NamingException {
        UniversalBody b = null;

        String protocol = UrlBuilder.getProtocol(url);

        // First step towards Body factory, will be introduced after the release
        if (protocol.equals("rmi:")) {
            b = new RmiBodyAdapter().lookup(url);
        } else if (protocol.equals("rmissh:")) {
            b = new SshRmiBodyAdapter().lookup(url);
        } else if (protocol.equals("http:")) {
            b = new HttpBodyAdapter().lookup(url);
        } else if (protocol.equals("ibis:")) {
            b = new IbisBodyAdapter().lookup(url);
        } else {
            throw new IOException("Protocol " + protocol + " not defined");
        }

        try {
            StubObject stub = (StubObject) ProActive.createStubObject(ProActiveComponentRepresentative.class.getName(),
                    b);
            return ProActiveComponentRepresentativeFactory.instance()
                                                          .createComponentRepresentative(stub.getProxy());
        } catch (Throwable t) {
            t.printStackTrace();
            logger.error("Could not perform lookup for component at URL: " +
                url.toString() +
                ", because construction of component representative failed." +
                t.toString());
            throw new IOException(
                "Could not perform lookup for component at URL: " +
                url.toString() +
                ", because construction of component representative failed.");
        }
    }
}
