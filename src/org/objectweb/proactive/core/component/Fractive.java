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
package org.objectweb.proactive.core.component;

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
import org.objectweb.proactive.core.component.controller.ComponentParametersController;
import org.objectweb.proactive.core.component.representative.ProActiveComponentRepresentative;
import org.objectweb.proactive.core.component.representative.ProActiveComponentRepresentativeFactory;
import org.objectweb.proactive.core.component.type.Composite;
import org.objectweb.proactive.core.component.type.ParallelComposite;
import org.objectweb.proactive.core.component.type.ProActiveTypeFactory;
import org.objectweb.proactive.core.group.Group;
import org.objectweb.proactive.core.group.ProActiveComponentGroup;
import org.objectweb.proactive.core.group.ProActiveGroup;
import org.objectweb.proactive.core.mop.Proxy;
import org.objectweb.proactive.core.mop.StubObject;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.ProActiveLogger;

import java.util.Hashtable;
import java.util.Map;


/**
 * This class is used for creating components.
 * It acts as :<br>
 * 1. a bootstrap component<br>
 * 2. a GenericFactory for instantiating new components
 * 3. a utility class providing static methods to create collective interfaces and retreive references to ComponentParametersController<br>
 */
public class Fractive implements GenericFactory, Component, Factory {
    private static Fractive instance = null;
    private TypeFactory typeFactory = (TypeFactory) ProActiveTypeFactory.instance();
    private Type type = null;

    /**
     * no-arg constructor (used by Fractal to get a bootstrap component)
     *
     */
    public Fractive() {
    }

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
     * Returns a generated interface reference, whose impl field is a group
     * It is able to handle multiple bindings
     */
    public static ProActiveInterface createCollectiveClientInterface(
        String itfName, String itfSignature) throws ProActiveRuntimeException {
        try {
            InterfaceType itf_type = ProActiveTypeFactory.instance()
                                                         .createFcItfType(itfName,
                    itfSignature, TypeFactory.CLIENT, TypeFactory.MANDATORY,
                    TypeFactory.COLLECTION);
            ProActiveInterface itf_ref_group = ProActiveComponentGroup.newComponentInterfaceGroup(itf_type);
            return itf_ref_group;
        } catch (Exception e) {
            throw new ProActiveRuntimeException("Impossible to create a collective client interface ",
                e);
        }
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
                contentDesc.setFactory(new ProActiveMetaObjectFactory(
                        factory_params));
                //		   factory = ProActiveComponentMetaObjectFactory.newInstance(componentParameters);
            }

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
                    Component components = ProActiveComponentGroup.newComponentRepresentativeGroup(componentParameters.getComponentType());
                    Group group_of_components = ProActiveGroup.getGroup(components);
                    Proxy proxy = null;

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
                    myProxy);
            representative.setStubOnBaseObject((StubObject) ao);
            return representative;
        } catch (ActiveObjectCreationException e) {
            throw new InstantiationException(e.getMessage());
        } catch (NodeException e) {
            throw new InstantiationException(e.getMessage());
        } catch (ClassNotFoundException e) {
            throw new InstantiationException(e.getMessage());
        } catch (java.lang.InstantiationException e) {
            e.printStackTrace();
            throw new InstantiationException(e.getMessage());
        }
    }

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
     * see {@link org.objectweb.fractal.api.factory.GenericFactory#newFcInstance(org.objectweb.fractal.api.Type, java.lang.Object, java.lang.Object)}
     */
    public Component newFcInstance(Type arg0, Object arg1, Object arg2)
        throws InstantiationException {
        try {
            return newFcInstance(arg0, (ControllerDescription) arg1,
                (ContentDescription) arg2);
        } catch (ClassCastException e) {
            if ((arg0 == null) && (arg1 == null) && (arg2 instanceof Map)) {
                // for compatibility with the new org.objectweb.fractal.util.Fractal class
                return this;
            }
            if ((arg1 instanceof ControllerDescription) &&
                    ((arg2 instanceof String) || (arg2 == null))) {
                // for the ADL, when only type and ControllerDescription are given
                return newFcInstance(arg0, arg1,
                    (arg2 == null) ? null : new ContentDescription(
                        (String) arg2));
            }

            // code compatibility with Julia
            if ("composite".equals(arg1) && (arg2 == null)) {
                return newFcInstance(arg0,
                    new ControllerDescription(null, Constants.COMPOSITE), null);
            }
            if ("primitive".equals(arg1) && (arg2 instanceof String)) {
                return newFcInstance(arg0,
                    new ControllerDescription(null, Constants.PRIMITIVE),
                    new ContentDescription((String) arg2));
            }
            if ("parallel".equals(arg1) && (arg2 == null)) {
                return newFcInstance(arg0,
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

    /**
     * see {@link org.objectweb.fractal.api.Component#getFcInterface(java.lang.String)}
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

    /**
     * see {@link org.objectweb.fractal.api.Component#getFcInterfaces()}
     */
    public Object[] getFcInterfaces() {
        return null;
    }

    /**
     * see {@link org.objectweb.fractal.api.Component#getFcType()}
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
                ProActiveLogger.getLogger("components").error(e.getMessage());
                return null;
            }
        } else {
            return type;
        }
    }

    /**
     * see {@link org.objectweb.fractal.api.factory.Factory#getFcContentDesc()}
     */
    public Object getFcContentDesc() {
        return null;
    }

    /**
     * see {@link org.objectweb.fractal.api.factory.Factory#getFcControllerDesc()}
     */
    public Object getFcControllerDesc() {
        return null;
    }

    /**
     * see {@link org.objectweb.fractal.api.factory.Factory#getFcInstanceType()}
     */
    public Type getFcInstanceType() {
        return null;
    }

    /**
     * see {@link org.objectweb.fractal.api.factory.Factory#newFcInstance()}
     */
    public Component newFcInstance() throws InstantiationException {
        return this;
    }
}
