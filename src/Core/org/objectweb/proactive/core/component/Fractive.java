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
package org.objectweb.proactive.core.component;

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.Type;
import org.objectweb.fractal.api.control.AttributeController;
import org.objectweb.fractal.api.factory.Factory;
import org.objectweb.fractal.api.factory.GenericFactory;
import org.objectweb.fractal.api.factory.InstantiationException;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.fractal.api.type.TypeFactory;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAGroup;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.body.ProActiveMetaObjectFactory;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.component.body.ComponentBody;
import org.objectweb.proactive.core.component.controller.ComponentParametersController;
import org.objectweb.proactive.core.component.controller.GathercastController;
import org.objectweb.proactive.core.component.controller.MembraneController;
import org.objectweb.proactive.core.component.controller.MigrationController;
import org.objectweb.proactive.core.component.controller.MulticastController;
import org.objectweb.proactive.core.component.controller.ProActiveBindingController;
import org.objectweb.proactive.core.component.controller.ProActiveContentController;
import org.objectweb.proactive.core.component.exceptions.InstantiationExceptionListException;
import org.objectweb.proactive.core.component.factory.ProActiveGenericFactory;
import org.objectweb.proactive.core.component.identity.ProActiveComponent;
import org.objectweb.proactive.core.component.representative.ProActiveComponentRepresentative;
import org.objectweb.proactive.core.component.representative.ProActiveComponentRepresentativeFactory;
import org.objectweb.proactive.core.component.type.Composite;
import org.objectweb.proactive.core.component.type.ProActiveTypeFactoryImpl;
import org.objectweb.proactive.core.config.PAProperties;
import org.objectweb.proactive.core.component.group.ProActiveComponentGroup;
import org.objectweb.proactive.core.mop.MOP;
import org.objectweb.proactive.core.mop.StubObject;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.remoteobject.RemoteObject;
import org.objectweb.proactive.core.remoteobject.RemoteObjectHelper;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * This class is used for creating components. It acts as :
 * <ol>
 * <li> a bootstrap component</li>
 * <li> a specialized GenericFactory for instantiating new components on remote nodes (a ProActiveGenericFactory)</li>
 * <li> a utility class providing static methods to create collective interfaces
 * and retrieve references to ComponentParametersController</li>
 * </ol>
 *
 * @author The ProActive Team
 */
@PublicAPI
public class Fractive implements ProActiveGenericFactory, Component, Factory {
    private static Fractive instance = null;
    private TypeFactory typeFactory = ProActiveTypeFactoryImpl.instance();
    private Type type = null;
    private static Logger logger = ProActiveLogger.getLogger(Loggers.COMPONENTS);

    /**
     * no-arg constructor (used by Fractal to get a bootstrap component)
     *
     */
    public Fractive() {
    }

    /**
     * Returns singleton
     *
     * @return Fractive a singleton
     */
    private static Fractive instance() {
        if (instance == null) {
            instance = new Fractive();
        }
        return instance;
    }

    /**
     * Returns the {@link org.objectweb.proactive.core.component.controller.ComponentParametersController ComponentParametersController}
     * interface of the given component.
     *
     * @param component a component.
     * @return the {@link org.objectweb.proactive.core.component.controller.ComponentParametersController ComponentParametersController}
     *         interface of the given component.
     * @throws NoSuchInterfaceException if there is no such interface.
     */
    public static ComponentParametersController getComponentParametersController(final Component component)
            throws NoSuchInterfaceException {
        return (ComponentParametersController) component
                .getFcInterface(Constants.COMPONENT_PARAMETERS_CONTROLLER);
    }

    public static ProActiveBindingController getBindingController(final Component component)
            throws NoSuchInterfaceException {
        return (ProActiveBindingController) component.getFcInterface(Constants.BINDING_CONTROLLER);
    }

    /**
     * Returns the {@link org.objectweb.proactive.core.component.controller.MulticastController MulticastController}
     * interface of the given component.
     *
     * @param component a component.
     * @return the {@link org.objectweb.proactive.core.component.controller.MulticastController MulticastController}
     *         interface of the given component.
     * @throws NoSuchInterfaceException if there is no such interface.
     */
    public static MulticastController getMulticastController(final Component component)
            throws NoSuchInterfaceException {
        return (MulticastController) component.getFcInterface(Constants.MULTICAST_CONTROLLER);
    }

    /**
     * Returns the {@link org.objectweb.proactive.core.component.controller.ProActiveContentController ProActiveContentController}
     * interface of the given component.
     *
     * @param component a component.
     * @return the {@link org.objectweb.proactive.core.component.controller.ProActiveContentController ProActiveContentController}
     *         interface of the given component.
     * @throws NoSuchInterfaceException if there is no such interface.
     */
    public static ProActiveContentController getProActiveContentController(final Component component)
            throws NoSuchInterfaceException {
        return (ProActiveContentController) component.getFcInterface(Constants.CONTENT_CONTROLLER);
    }

    /**
     * Returns the {@link org.objectweb.proactive.core.component.controller.MembraneController MembraneController}
     * interface of the given component.
     * @param component omponent a component.
     * @return the {@link org.objectweb.proactive.core.component.controller.MembraneController MembraneController}
     * @throws NoSuchInterfaceException if there is no such interface.
     */
    public static MembraneController getMembraneController(final Component component)
            throws NoSuchInterfaceException {
        return (MembraneController) component.getFcInterface(Constants.MEMBRANE_CONTROLLER);
    }

    /**
     * Returns the {@link org.objectweb.proactive.core.component.controller.GathercastController GatherController}
     * interface of the given component.
     *
     * @param component a component.
     * @return the {@link org.objectweb.proactive.core.component.controller.GathercastController GatherController}
     *         interface of the given component.
     * @throws NoSuchInterfaceException if there is no such interface.
     */
    public static GathercastController getGathercastController(final Component component)
            throws NoSuchInterfaceException {
        return (GathercastController) component.getFcInterface(Constants.GATHERCAST_CONTROLLER);
    }

    /**
     * Returns the {@link org.objectweb.proactive.core.component.controller.MigrationController MigrationController}
     * interface of the given component.
     *
     * @param component a component.
     * @return the {@link org.objectweb.proactive.core.component.controller.MigrationController MigrationController}
     *         interface of the given component.
     * @throws NoSuchInterfaceException if there is no such interface.
     */
    public static MigrationController getMigrationController(final Component component)
            throws NoSuchInterfaceException {
        return (MigrationController) component.getFcInterface(Constants.MIGRATION_CONTROLLER);
    }

    //    /**
    //     * Returns a generated interface reference, whose impl field is a group It
    //     * is able to handle multiple bindings, and automatically adds it to a given
    //     *
    //     * @param itfName the name of the interface
    //     * @param itfSignature the signature of the interface
    //     * @param owner the component to which this interface belongs
    //     * @return ProActiveInterface the resulting collective client interface
    //     * @throws ProActiveRuntimeException in case of a problem while creating the collective interface
    //     */
    //    public static ProActiveInterface createCollectiveClientInterface(String itfName, String itfSignature,
    //            Component owner) throws ProActiveRuntimeException {
    //        try {
    //            ProActiveInterfaceType itf_type = (ProActiveInterfaceType) ProActiveTypeFactoryImpl.instance()
    //                    .createFcItfType(itfName, itfSignature, TypeFactory.CLIENT, TypeFactory.MANDATORY,
    //                            TypeFactory.COLLECTION);
    //            ProActiveInterface itf_ref_group = ProActiveComponentGroup.newComponentInterfaceGroup(itf_type,
    //                    owner);
    //            return itf_ref_group;
    //        } catch (Exception e) {
    //            throw new ProActiveRuntimeException("Impossible to create a collective client interface ", e);
    //        }
    //    }

    //    /**
    //     * Returns a generated interface reference, whose impl field is a group It
    //     * is able to handle multiple bindings, and automatically adds it to a given
    //     *
    //     * @param itfName the name of the interface
    //     * @param itfSignature the signature of the interface
    //     * @param owner the component to which this interface belongs
    //     * @return ProActiveInterface the resulting collective client interface
    //     * @throws ProActiveRuntimeException in case of a problem while creating the collective interface
    //     */
    //    public static ProActiveInterface createMulticastClientInterface(String itfName, String itfSignature,
    //            Component owner) throws ProActiveRuntimeException {
    //        try {
    //            ProActiveInterfaceType itf_type = (ProActiveInterfaceType) ProActiveTypeFactoryImpl.instance()
    //                    .createFcItfType(itfName, itfSignature, TypeFactory.CLIENT, TypeFactory.MANDATORY,
    //                            ProActiveTypeFactory.MULTICAST_CARDINALITY);
    //
    //            ProActiveInterface itf_ref_group = ProActiveComponentGroup.newComponentInterfaceGroup(itf_type,
    //                    owner);
    //            return itf_ref_group;
    //        } catch (Exception e) {
    //            throw new ProActiveRuntimeException("Impossible to create a collective client interface ", e);
    //        }

    //    /**
    //     * Returns a generated interface reference, whose impl field is a group It
    //     * is able to handle multiple bindings
    //     *
    //     * @param itfName the name of the interface
    //     * @param itfSignature the signature of the interface
    //     * @return ProActiveInterface the resulting collective client interface
    //     * @throws ProActiveRuntimeException in case of a problem while creating the collective interface
    //     */
    //
    //    public static ProActiveInterface createCollectiveClientInterface(
    //        String itfName, String itfSignature) throws ProActiveRuntimeException {
    //        return Fractive.createCollectiveClientInterface(itfName, itfSignature,
    //            null);
    //    }
    //    /**
    //     * Returns a generated interface reference, whose impl field is a group It
    //     * is able to handle multiple bindings
    //     *
    //     * @param itfName the name of the interface
    //     * @param itfSignature the signature of the interface
    //     * @return ProActiveInterface the resulting collective client interface
    //     * @throws ProActiveRuntimeException in case of a problem while creating the collective interface
    //     */
    //    public static ProActiveInterface createMulticastClientInterface(String itfName, String itfSignature)
    //            throws ProActiveRuntimeException {
    //        return Fractive.createMulticastClientInterface(itfName, itfSignature, null);
    //    }
    /*
     * 
     * @see org.objectweb.proactive.core.component.factory.ProActiveGenericFactory#newFcInstance(org.objectweb.fractal.api.Type,
     *      org.objectweb.proactive.core.component.ControllerDescription,
     *      org.objectweb.proactive.core.component.ContentDescription)
     */
    public Component newFcInstance(Type type, ControllerDescription controllerDesc,
            ContentDescription contentDesc) throws InstantiationException {
        return newFcInstance(type, controllerDesc, contentDesc, (Node) null);
    }

    /**
     * @see org.objectweb.proactive.core.component.factory.ProActiveGenericFactory#newNFcInstance(org.objectweb.fractal.api.Type, org.objectweb.proactive.core.component.ControllerDescription, org.objectweb.proactive.core.component.ContentDescription)
     */
    public Component newNFcInstance(Type type, ControllerDescription controllerDesc,
            ContentDescription contentDesc) throws InstantiationException {
        return newNFcInstance(type, controllerDesc, contentDesc, (Node) null);
    }

    /*
     * 
     * @see org.objectweb.fractal.api.factory.GenericFactory#newFcInstance(org.objectweb.fractal.api.Type,
     *      java.lang.Object, java.lang.Object)
     */
    public Component newFcInstance(Type type, Object controllerDesc, Object contentDesc)
            throws InstantiationException {
        try {
            return newFcInstance(type, (ControllerDescription) controllerDesc,
                    (ContentDescription) contentDesc);
        } catch (ClassCastException e) {
            if ((type == null) && (controllerDesc == null) && (contentDesc instanceof Map)) {
                // for compatibility with the new
                // org.objectweb.fractal.util.Fractal class
                return this;
            }
            if ((controllerDesc instanceof ControllerDescription) &&
                ((contentDesc instanceof String) || (contentDesc == null))) {
                // for the ADL, when only type and ControllerDescription are
                // given
                return newFcInstance(type, controllerDesc, (contentDesc == null) ? null
                        : new ContentDescription((String) contentDesc));
            }

            // code compatibility with Julia
            if ("composite".equals(controllerDesc)) {
                try {
                    if (contentDesc == null) {
                        return newFcInstance(type, new ControllerDescription(null, Constants.COMPOSITE), null);
                    } else if ((contentDesc instanceof String) &&
                        (AttributeController.class.isAssignableFrom(Class.forName((String) contentDesc)))) {
                        return newFcInstance(type, new ControllerDescription(null, Constants.COMPOSITE),
                                new ContentDescription((String) contentDesc));
                    }
                } catch (ClassNotFoundException e2) {
                    throw new InstantiationException("cannot find classe " + contentDesc + " : " +
                        e2.getMessage());
                }

            }
            if ("primitive".equals(controllerDesc) && (contentDesc instanceof String)) {
                return newFcInstance(type, new ControllerDescription(null, Constants.PRIMITIVE),
                        new ContentDescription((String) contentDesc));
            }

            // any other case
            throw new InstantiationException(
                "With this implementation, parameters must be of respective types : " + Type.class.getName() +
                    ',' + ControllerDescription.class.getName() + ',' + ContentDescription.class.getName());
        }
    }

    /*
     * 
     * @see org.objectweb.fractal.api.factory.GenericFactory#newNFcInstance(org.objectweb.fractal.api.Type,
     *      java.lang.Object, java.lang.Object)
     */
    public Component newNFcInstance(Type type, Object controllerDesc, Object contentDesc)
            throws InstantiationException {
        try {
            return newNFcInstance(type, (ControllerDescription) controllerDesc,
                    (ContentDescription) contentDesc);
        } catch (ClassCastException e) {
            if ((type == null) && (controllerDesc == null) && (contentDesc instanceof Map)) {
                // for compatibility with the new
                // org.objectweb.fractal.util.Fractal class
                return this;
            }
            if ((controllerDesc instanceof ControllerDescription) &&
                ((contentDesc instanceof String) || (contentDesc == null))) {
                // for the ADL, when only type and ControllerDescription are
                // given
                return newNFcInstance(type, controllerDesc, (contentDesc == null) ? null
                        : new ContentDescription((String) contentDesc));
            }

            // code compatibility with Julia
            if ("composite".equals(controllerDesc)) {
                try {
                    if (contentDesc == null) {
                        return newFcInstance(type, new ControllerDescription(null, Constants.COMPOSITE), null);
                    } else if ((contentDesc instanceof String) &&
                        (AttributeController.class.isAssignableFrom(Class.forName((String) contentDesc)))) {
                        return newFcInstance(type, new ControllerDescription(null, Constants.COMPOSITE),
                                new ContentDescription((String) contentDesc));
                    }
                } catch (ClassNotFoundException e2) {
                    throw new InstantiationException("cannot find classe " + contentDesc + " : " +
                        e2.getMessage());
                }

            }
            if ("primitive".equals(controllerDesc) && (contentDesc instanceof String)) {
                return newNFcInstance(type, new ControllerDescription(null, Constants.PRIMITIVE),
                        new ContentDescription((String) contentDesc));
            }

            // any other case
            throw new InstantiationException(
                "With this implementation, parameters must be of respective types : " + Type.class.getName() +
                    ',' + ControllerDescription.class.getName() + ',' + ContentDescription.class.getName());
        }
    }

    /*
     * 
     * @see org.objectweb.proactive.core.component.factory.ProActiveGenericFactory#newFcInstance(org.objectweb.fractal.api.Type,
     *      org.objectweb.proactive.core.component.ControllerDescription,
     *      org.objectweb.proactive.core.component.ContentDescription,
     *      org.objectweb.proactive.core.node.Node)
     */
    public Component newFcInstance(Type type, ControllerDescription controllerDesc,
            ContentDescription contentDesc, Node node) throws InstantiationException {
        try {
            ActiveObjectWithComponentParameters container = commonInstanciation(type, controllerDesc,
                    contentDesc, node);
            return fComponent(type, container);
        } catch (ActiveObjectCreationException e) {
            InstantiationException ie = new InstantiationException(e.getMessage());
            ie.initCause(e);
            throw ie;
        } catch (NodeException e) {
            InstantiationException ie = new InstantiationException(e.getMessage());
            ie.initCause(e);
            throw ie;
        }
    }

    /*
     * 
     * @see org.objectweb.proactive.core.component.factory.ProActiveGenericFactory#newNFcInstance(org.objectweb.fractal.api.Type,
     *      org.objectweb.proactive.core.component.ControllerDescription,
     *      org.objectweb.proactive.core.component.ContentDescription,
     *      org.objectweb.proactive.core.node.Node)
     */
    public Component newNFcInstance(Type type, ControllerDescription controllerDesc,
            ContentDescription contentDesc, Node node) throws InstantiationException {
        try {
            ActiveObjectWithComponentParameters container = commonInstanciation(type, controllerDesc,
                    contentDesc, node);
            return nfComponent(type, container);
        } catch (ActiveObjectCreationException e) {
            e.printStackTrace();
            throw new InstantiationException(e.getMessage());
        } catch (NodeException e) {
            throw new InstantiationException(e.getMessage());
        }
    }

    /*
     * 
     * @see org.objectweb.proactive.core.component.factory.ProActiveGenericFactory#newFcInstance(org.objectweb.fractal.api.Type,
     *      org.objectweb.proactive.core.component.ControllerDescription,
     *      org.objectweb.proactive.core.component.ContentDescription,
     *      org.objectweb.proactive.core.descriptor.data.VirtualNode)
     */
    //    public Component newFcInstance(Type type, ControllerDescription controllerDesc,
    //            ContentDescription contentDesc, VirtualNode virtualNode) throws InstantiationException {
    //        if (virtualNode == null) {
    //            return newFcInstance(type, controllerDesc, contentDesc, (Node) null);
    //        }
    //        try {
    //            virtualNode.activate();
    //            if (virtualNode.getNodes().length == 0) {
    //                throw new InstantiationException(
    //                    "Cannot create component on virtual node as no node is associated with this virtual node");
    //            }
    //            return newFcInstance(type, controllerDesc, contentDesc, virtualNode.getNode());
    //        } catch (NodeException e) {
    //            throw new InstantiationException(
    //                "could not instantiate components due to a deployment problem : " + e.getMessage());
    //        }
    //    }
    /*
     * 
     * @see org.objectweb.proactive.core.component.factory.ProActiveGenericFactory#newNFcInstance(org.objectweb.fractal.api.Type,
     *      org.objectweb.proactive.core.component.ControllerDescription,
     *      org.objectweb.proactive.core.component.ContentDescription,
     *      org.objectweb.proactive.core.descriptor.data.VirtualNode)
     */
    //    public Component newNFcInstance(Type type, ControllerDescription controllerDesc,
    //            ContentDescription contentDesc, VirtualNode virtualNode) throws InstantiationException {
    //        if (virtualNode == null) {
    //            return newNFcInstance(type, controllerDesc, contentDesc, (Node) null);
    //        }
    //        try {
    //            virtualNode.activate();
    //            if (virtualNode.getNodes().length == 0) {
    //                throw new InstantiationException(
    //                    "Cannot create component on virtual node as no node is associated with this virtual node");
    //            }
    //            return newNFcInstance(type, controllerDesc, contentDesc, virtualNode.getNode());
    //        } catch (NodeException e) {
    //            throw new InstantiationException(
    //                "could not instantiate components due to a deployment problem : " + e.getMessage());
    //        }
    //    }
    /*
     * @see org.objectweb.proactive.core.component.factory.ProActiveGenericFactory#newFcInstanceAsList(org.objectweb.fractal.api.Type,
     *      org.objectweb.proactive.core.component.ControllerDescription,
     *      org.objectweb.proactive.core.component.ContentDescription,
     *      org.objectweb.proactive.core.descriptor.data.VirtualNode)
     */
    //    public List<Component> newFcInstanceAsList(Type type, ControllerDescription controllerDesc,
    //            ContentDescription contentDesc, VirtualNode virtualNode) throws InstantiationException {
    //        if (virtualNode == null) {
    //            return newFcInstanceAsList(type, controllerDesc, contentDesc, (Node[]) null);
    //        }
    //        try {
    //            virtualNode.activate();
    //            return newFcInstanceAsList(type, controllerDesc, contentDesc, virtualNode.getNodes());
    //        } catch (NodeException e) {
    //            throw new InstantiationException(
    //                "could not instantiate components due to a deployment problem : " + e.getMessage());
    //        }
    //    }
    /*
     * @see org.objectweb.proactive.core.component.factory.ProActiveGenericFactory#newNFcInstanceAsList(org.objectweb.fractal.api.Type,
     *      org.objectweb.proactive.core.component.ControllerDescription,
     *      org.objectweb.proactive.core.component.ContentDescription,
     *      org.objectweb.proactive.core.descriptor.data.VirtualNode)
     */
    //    public List<Component> newNFcInstanceAsList(Type type, ControllerDescription controllerDesc,
    //            ContentDescription contentDesc, VirtualNode virtualNode) throws InstantiationException {
    //        if (virtualNode == null) {
    //            return newNFcInstanceAsList(type, controllerDesc, contentDesc, (Node[]) null);
    //        }
    //        try {
    //            virtualNode.activate();
    //            return newNFcInstanceAsList(type, controllerDesc, contentDesc, virtualNode.getNodes());
    //        } catch (NodeException e) {
    //            throw new InstantiationException(
    //                "could not instantiate components due to a deployment problem : " + e.getMessage());
    //        }
    //    }
    /*
     * @see org.objectweb.proactive.core.component.factory.ProActiveGenericFactory#newFcInstanceAsList(org.objectweb.fractal.api.Type,
     *      org.objectweb.proactive.core.component.ControllerDescription,
     *      org.objectweb.proactive.core.component.ContentDescription,
     *      org.objectweb.proactive.core.node.Node[])
     */
    public List<Component> newFcInstanceAsList(Type type, ControllerDescription controllerDesc,
            ContentDescription contentDesc, Node[] nodes) throws InstantiationException {
        ContentDescription[] contentDescArray = new ContentDescription[nodes.length];
        Arrays.fill(contentDescArray, contentDesc);
        return newFcInstanceAsList(type, controllerDesc, contentDescArray, nodes);
    }

    /*
     * @see org.objectweb.proactive.core.component.factory.ProActiveGenericFactory#newNFcInstanceAsList(org.objectweb.fractal.api.Type,
     *      org.objectweb.proactive.core.component.ControllerDescription,
     *      org.objectweb.proactive.core.component.ContentDescription,
     *      org.objectweb.proactive.core.node.Node[])
     */
    public List<Component> newNFcInstanceAsList(Type type, ControllerDescription controllerDesc,
            ContentDescription contentDesc, Node[] nodes) throws InstantiationException {
        ContentDescription[] contentDescArray = new ContentDescription[nodes.length];
        Arrays.fill(contentDescArray, contentDesc);
        return newNFcInstanceAsList(type, controllerDesc, contentDescArray, nodes);
    }

    /*
     * 
     * @see org.objectweb.proactive.core.component.factory.ProActiveGenericFactory#newFcInstanceAsList(org.objectweb.fractal.api.Type,
     *      org.objectweb.proactive.core.component.ControllerDescription,
     *      org.objectweb.proactive.core.component.ContentDescription[],
     *      org.objectweb.proactive.core.node.Node[])
     */
    public List<Component> newFcInstanceAsList(Type type, ControllerDescription controllerDesc,
            ContentDescription[] contentDesc, Node[] nodes) throws InstantiationException {
        return groupInstance(type, controllerDesc, contentDesc, nodes, true);
    }

    /*
     * 
     * @see org.objectweb.proactive.core.component.factory.ProActiveGenericFactory#newNFcInstanceAsList(org.objectweb.fractal.api.Type,
     *      org.objectweb.proactive.core.component.ControllerDescription,
     *      org.objectweb.proactive.core.component.ContentDescription[],
     *      org.objectweb.proactive.core.node.Node[])
     */
    public List<Component> newNFcInstanceAsList(Type type, ControllerDescription controllerDesc,
            ContentDescription[] contentDesc, Node[] nodes) throws InstantiationException {
        return groupInstance(type, controllerDesc, contentDesc, nodes, false);
    }

    /*
     * 
     * @see org.objectweb.proactive.core.component.factory.ProActiveGenericFactory#newFcInstanceAsList(org.objectweb.fractal.api.Type,
     *      org.objectweb.proactive.core.component.ControllerDescription,
     *      org.objectweb.proactive.core.component.ContentDescription[],
     *      org.objectweb.proactive.core.descriptor.data.VirtualNode)
     */
    //    public List<Component> newFcInstanceAsList(Type type, ControllerDescription controllerDesc,
    //            ContentDescription[] contentDesc, VirtualNode virtualNode) throws InstantiationException {
    //        if (virtualNode == null) {
    //            return newFcInstanceAsList(type, controllerDesc, contentDesc, (Node[]) null);
    //        }
    //        try {
    //            virtualNode.activate();
    //
    //            return newFcInstanceAsList(type, controllerDesc, contentDesc, virtualNode.getNodes());
    //        } catch (NodeException e) {
    //            throw new InstantiationException(
    //                "could not instantiate components due to a deployment problem : " + e.getMessage());
    //        }
    //    }
    /*
     * 
     * @see org.objectweb.proactive.core.component.factory.ProActiveGenericFactory#newNFcInstanceAsList(org.objectweb.fractal.api.Type,
     *      org.objectweb.proactive.core.component.ControllerDescription,
     *      org.objectweb.proactive.core.component.ContentDescription[],
     *      org.objectweb.proactive.core.descriptor.data.VirtualNode)
     */
    //    public List<Component> newNFcInstanceAsList(Type type, ControllerDescription controllerDesc,
    //            ContentDescription[] contentDesc, VirtualNode virtualNode) throws InstantiationException {
    //        if (virtualNode == null) {
    //            return newNFcInstanceAsList(type, controllerDesc, contentDesc, (Node[]) null);
    //        }
    //        try {
    //            virtualNode.activate();
    //
    //            return newNFcInstanceAsList(type, controllerDesc, contentDesc, virtualNode.getNodes());
    //        } catch (NodeException e) {
    //            throw new InstantiationException(
    //                "could not instantiate components due to a deployment problem : " + e.getMessage());
    //        }
    //    }
    /*
     * 
     * @see org.objectweb.fractal.api.Component#getFcInterface(java.lang.String)
     */
    public Object getFcInterface(String itfName) throws NoSuchInterfaceException {
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
                        typeFactory.createFcItfType("generic-factory", GenericFactory.class.getName(), false,
                                false, false),
                        typeFactory.createFcItfType("type-factory", TypeFactory.class.getName(), false,
                                false, false) });
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
     * Helper method for extracting the types of client interfaces from the type
     * of a component
     *
     * @param componentType a component type
     * @return the types of client interfaces
     */
    public static InterfaceType[] getClientInterfaceTypes(ComponentType componentType) {
        ArrayList<InterfaceType> client_interfaces = new ArrayList<InterfaceType>();
        InterfaceType[] interfaceTypes = componentType.getFcInterfaceTypes();
        for (int i = 0; i < interfaceTypes.length; i++) {
            if (interfaceTypes[i].isFcClientItf()) {
                client_interfaces.add(interfaceTypes[i]);
            }
        }
        return client_interfaces.toArray(new InterfaceType[client_interfaces.size()]);
    }

    /**
     * Returns a component representative pointing to the component associated
     * to the component whose active thread is calling this method. It can be
     * used for a component to pass callback references to itself.
     *
     * @return a component representative for the component in which the current
     *         thread is running
     */
    public static Component getComponentRepresentativeOnThis() {
        ComponentBody componentBody;
        try {
            componentBody = (ComponentBody) PAActiveObject.getBodyOnThis();
        } catch (ClassCastException e) {
            logger
                    .error("Cannot get a component representative from the current object, because this object is not a component");
            return null;
        }
        ProActiveComponent currentComponent = componentBody.getProActiveComponentImpl();
        return currentComponent.getRepresentativeOnThis();
    }

    /**
     * Registers a reference on a component with an URL
     *
     * @param ref
     *            a reference on a component (it should be an instance of
     *            ProActiveComponentRepresentative)
     * @param url
     *            the registration address
     * @throws IOException
     *             if the component cannot be registered
     */
    public static void register(Component ref, String url) throws IOException {
        if (!(ref instanceof ProActiveComponentRepresentative)) {
            throw new IllegalArgumentException("This method can only register ProActive components");
        }
        PAActiveObject.register(ref, url);
    }

    /**
     * Returns a reference on a component (a component representative) for the
     * component associated with the specified name.<br>
     *
     * @param url the registered location of the component
     * @return a reference on the component corresponding to the given name
     * @throws IOException if there is a communication problem with the registry
     * @throws NamingException if a reference on a component could not be found at the
     *             specified URL
     */
    public static ProActiveComponentRepresentative lookup(String url) throws IOException, NamingException {
        UniversalBody b = null;
        RemoteObject<?> rmo;
        URI uri = RemoteObjectHelper.expandURI(URI.create(url));

        try {
            rmo = RemoteObjectHelper.lookup(uri);

            b = (UniversalBody) RemoteObjectHelper.generatedObjectStub(rmo);

            StubObject stub = (StubObject) MOP.createStubObject(ProActiveComponentRepresentative.class
                    .getName(), b);

            return ProActiveComponentRepresentativeFactory.instance().createComponentRepresentative(
                    stub.getProxy());
        } catch (Throwable t) {
            t.printStackTrace();
            logger.error("Could not perform lookup for component at URL: " + url +
                ", because construction of component representative failed." + t.toString());
            throw new NamingException("Could not perform lookup for component at URL: " + url +
                ", because construction of component representative failed.");
        }
    }

    /**
     * Returns the {@link ProActiveGenericFactory} interface of the given
     * component.
     *
     * @param component the component to get the factory from
     * @return the {@link ProActiveGenericFactory} interface of the given
     *         component.
     * @throws NoSuchInterfaceException if there is no such interface.
     */
    public static ProActiveGenericFactory getGenericFactory(final Component component)
            throws NoSuchInterfaceException {
        return (ProActiveGenericFactory) component.getFcInterface("generic-factory");
    }

    /**
     * Common instantiation method called during creation both functional and non functional components
     * @param type
     * @param controllerDesc
     * @param contentDesc
     * @param node
     * @return A container object, containing objects for the generation of the component representative
     * @throws InstantiationException
     * @throws ActiveObjectCreationException
     * @throws NodeException
     */
    private ActiveObjectWithComponentParameters commonInstanciation(Type type,
            ControllerDescription controllerDesc, ContentDescription contentDesc, Node node)
            throws InstantiationException, ActiveObjectCreationException, NodeException {
        if (contentDesc == null) {
            // either a parallel or a composite component, no
            // activity/factory/node specified
            if (Constants.COMPOSITE.equals(controllerDesc.getHierarchicalType())) {
                contentDesc = new ContentDescription(Composite.class.getName());
            } else {
                throw new InstantiationException(
                    "Content can be null only if the hierarchical type of the component is composite");
            }
        } else if (Constants.COMPOSITE.equals(controllerDesc.getHierarchicalType())) {
            try {
                Class<?> contentClass = Class.forName(contentDesc.getClassName());
                if ((!contentClass.equals(Composite.class)) &&
                    (!AttributeController.class.isAssignableFrom(contentClass))) {
                    throw new InstantiationException(
                        "Content can be not null for composite component only if it extends AttributeControler");
                }
            } catch (ClassNotFoundException e) {
                throw new InstantiationException("Cannot find interface defined in content : " +
                    e.getMessage());
            }
        }

        // instantiate the component metaobject factory with parameters of
        // the component

        // type must be a component type
        if (!(type instanceof ComponentType)) {
            throw new InstantiationException("Argument type must be an instance of ComponentType");
        }
        ComponentParameters componentParameters = new ComponentParameters((ComponentType) type,
            controllerDesc);
        if (contentDesc.getFactory() == null) {
            // first create a map with the parameters
            Map<String, Object> factory_params = new Hashtable<String, Object>(1);

            factory_params.put(ProActiveMetaObjectFactory.COMPONENT_PARAMETERS_KEY, componentParameters);
            if (controllerDesc.isSynchronous() &&
                (Constants.COMPOSITE.equals(controllerDesc.getHierarchicalType()))) {
                factory_params.put(ProActiveMetaObjectFactory.SYNCHRONOUS_COMPOSITE_COMPONENT_KEY,
                        Constants.SYNCHRONOUS);
            }
            contentDesc.setFactory(new ProActiveMetaObjectFactory(factory_params));
            // factory =
            // ProActiveComponentMetaObjectFactory.newInstance(componentParameters);
        }

        // TODO_M : add controllers in the component metaobject factory?
        Object ao = null;

        // 3 possibilities : either the component is created on a node (or
        // null), it is created on a virtual node, or on multiple nodes
        ao = PAActiveObject.newActive(contentDesc.getClassName(), null, contentDesc
                .getConstructorParameters(), node, contentDesc.getActivity(), contentDesc.getFactory());

        return new ActiveObjectWithComponentParameters((StubObject) ao, componentParameters);
    }

    /**
     * Creates a component representative for a functional component (to be used with commonInstanciation method)
     * @param container The container containing objects for the generation of component representative
     * @return The created component
     */
    private ProActiveComponentRepresentative fComponent(Type type,
            ActiveObjectWithComponentParameters container) {
        ComponentParameters componentParameters = container.getParameters();
        StubObject ao = container.getActiveObject();
        org.objectweb.proactive.core.mop.Proxy myProxy = (ao).getProxy();
        if (myProxy == null) {
            throw new ProActiveRuntimeException("Cannot find a Proxy on the stub object: " + ao);
        }
        ProActiveComponentRepresentative representative = ProActiveComponentRepresentativeFactory.instance()
                .createComponentRepresentative((ComponentType) type,
                        componentParameters.getHierarchicalType(), myProxy,
                        componentParameters.getControllerDescription().getControllersConfigFileLocation());
        representative.setStubOnBaseObject(ao);
        return representative;
    }

    /**
     * Creates a component representative for a non functional component (to be used with commonInstanciation method)
     * @param container The container containing objects for the generation of component representative
     * @return The created component
     */
    private ProActiveComponentRepresentative nfComponent(Type type,
            ActiveObjectWithComponentParameters container) {
        ComponentParameters componentParameters = container.getParameters();
        StubObject ao = container.getActiveObject();
        org.objectweb.proactive.core.mop.Proxy myProxy = (ao).getProxy();
        if (myProxy == null) {
            throw new ProActiveRuntimeException("Cannot find a Proxy on the stub object: " + ao);
        }
        ProActiveComponentRepresentative representative = ProActiveComponentRepresentativeFactory.instance()
                .createNFComponentRepresentative((ComponentType) type,
                        componentParameters.getHierarchicalType(), myProxy,
                        componentParameters.getControllerDescription().getControllersConfigFileLocation());
        representative.setStubOnBaseObject(ao);
        return representative;
    }

    private List<Component> groupInstance(Type type, ControllerDescription controllerDesc,
            ContentDescription[] contentDesc, Node[] nodes, boolean isFunctional)
            throws InstantiationException {
        try {
            Component components = null;
            if (isFunctional) { /* Functional components */
                components = ProActiveComponentGroup.newComponentRepresentativeGroup((ComponentType) type,
                        controllerDesc);
            } else { /* Non functional components */
                components = ProActiveComponentGroup.newNFComponentRepresentativeGroup((ComponentType) type,
                        controllerDesc);
            }
            List<Component> componentsList = PAGroup.getGroup(components);
            if (Constants.PRIMITIVE.equals(controllerDesc.getHierarchicalType())) {
                if (contentDesc.length > 1) { // cyclic
                    // node
                    // + 1
                    // instance
                    // per
                    // node
                    // task = instantiate a component with a different name
                    // on each of the node mapped to the given virtual node
                    String original_component_name = controllerDesc.getName();

                    // TODO: reuse pool for whole class ?
                    ExecutorService threadPool = Executors.newCachedThreadPool();

                    List<InstantiationException> exceptions = new Vector<InstantiationException>();

                    Component c = new MockComponent();

                    for (int i = 0; i < nodes.length; i++) {
                        componentsList.add(c);
                    }
                    if (isFunctional) { /* Case of functional components */
                        for (int i = 0; i < nodes.length; i++) {
                            ComponentBuilderTask task = new ComponentBuilderTask(exceptions, componentsList,
                                i, type, controllerDesc, original_component_name, contentDesc, nodes);
                            threadPool.execute(task);
                        }
                    } else { /* Case of non functional components */
                        for (int i = 0; i < nodes.length; i++) {
                            NFComponentBuilderTask task = new NFComponentBuilderTask(exceptions,
                                componentsList, i, type, controllerDesc, original_component_name,
                                contentDesc, nodes);
                            threadPool.execute(task);
                        }
                    }
                    threadPool.shutdown();
                    try {
                        threadPool.awaitTermination(new Integer(PAProperties.PA_COMPONENT_CREATION_TIMEOUT
                                .getValue()), TimeUnit.SECONDS);
                    } catch (InterruptedException e) {
                        logger.info("Interruption when waiting for thread pool termination.", e);
                    }
                    if (!exceptions.isEmpty()) {
                        InstantiationException e = new InstantiationException(
                            "Creation of some of the components failed");
                        e.initCause(new InstantiationExceptionListException(exceptions));
                        throw e;
                    }
                } else {
                    // component is a composite : it will be
                    // created on the first node from this virtual node
                    if (isFunctional) { /* Functional components */
                        componentsList.add(newFcInstance(type, controllerDesc, contentDesc[0], nodes[0]));
                    } else { /* Non functional components */
                        componentsList.add(newNFcInstance(type, controllerDesc, contentDesc[0], nodes[0]));
                    }
                }
            }
            return componentsList;
        } catch (ClassNotFoundException e) {
            throw new InstantiationException(e.getMessage());
        }
    }

    private static class ComponentBuilderTask implements Runnable {
        List<InstantiationException> exceptions;
        List<Component> targetList;
        int indexInList;
        Type type;
        ControllerDescription controllerDesc;
        ContentDescription[] contentDesc;
        String originalName;
        Node[] nodes;

        public ComponentBuilderTask(List<InstantiationException> exceptions, List<Component> targetList,
                int indexInList, Type type, ControllerDescription controllerDesc, String originalName,
                ContentDescription[] contentDesc, Node[] nodes) {
            this.exceptions = exceptions;
            this.targetList = targetList;
            this.indexInList = indexInList;
            this.type = type;
            this.controllerDesc = controllerDesc;
            this.contentDesc = contentDesc;
            this.originalName = originalName;
            this.nodes = nodes;
        }

        public void run() {
            controllerDesc.setName(originalName + Constants.CYCLIC_NODE_SUFFIX + indexInList);
            Component instance;
            try {
                instance = Fractive.instance().newFcInstance(type, controllerDesc, contentDesc[indexInList],
                        nodes[indexInList % nodes.length]);
                //				System.out.println("[fractive] created component " + originalName + Constants.CYCLIC_NODE_SUFFIX + indexInList);
                targetList.set(indexInList, instance);
            } catch (InstantiationException e) {
                e.printStackTrace();
                //				targetList.add(null);
                exceptions.add(e);
            }
        }
    }

    private static class NFComponentBuilderTask extends ComponentBuilderTask implements Runnable {
        public NFComponentBuilderTask(List<InstantiationException> exceptions, List<Component> targetList,
                int indexInList, Type type, ControllerDescription controllerDesc, String originalName,
                ContentDescription[] contentDesc, Node[] nodes) {
            super(exceptions, targetList, indexInList, type, controllerDesc, originalName, contentDesc, nodes);
        }

        @Override
        public void run() {
            controllerDesc.setName(originalName + Constants.CYCLIC_NODE_SUFFIX + indexInList);
            Component instance;
            try {
                instance = Fractive.instance().newNFcInstance(type, controllerDesc, contentDesc[indexInList],
                        nodes[indexInList % nodes.length]);
                //				System.out.println("[fractive] created component " + originalName + Constants.CYCLIC_NODE_SUFFIX + indexInList);
                targetList.set(indexInList, instance);
            } catch (InstantiationException e) {
                e.printStackTrace();
                //				targetList.add(null);
                exceptions.add(e);
            }
        }
    }

    // a utility class mocking a component
    private static class MockComponent implements Component, Serializable {
        public Object getFcInterface(String interfaceName) throws NoSuchInterfaceException {
            return null;
        }

        public Object[] getFcInterfaces() {
            return null;
        }

        public Type getFcType() {
            return null;
        }
    }

    private static class ActiveObjectWithComponentParameters {
        StubObject activeObject;
        ComponentParameters parameters;

        public ActiveObjectWithComponentParameters(StubObject ao, ComponentParameters par) {
            this.activeObject = ao;
            this.parameters = par;
        }

        public StubObject getActiveObject() {
            return activeObject;
        }

        public ComponentParameters getParameters() {
            return parameters;
        }
    }
}
