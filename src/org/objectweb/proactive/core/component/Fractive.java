/*
 * Created on Dec 4, 2003
 * author : Matthieu Morel
  */
package org.objectweb.proactive.core.component;

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
import org.objectweb.proactive.core.component.controller.ComponentParametersController;
import org.objectweb.proactive.core.component.representative.ProActiveComponentRepresentativeFactory;
import org.objectweb.proactive.core.component.type.Composite;
import org.objectweb.proactive.core.component.type.ParallelComposite;
import org.objectweb.proactive.core.component.type.ProActiveTypeFactory;
import org.objectweb.proactive.core.group.Group;
import org.objectweb.proactive.core.group.ProActiveComponentGroup;
import org.objectweb.proactive.core.group.ProActiveGroup;
import org.objectweb.proactive.core.mop.ClassNotReifiableException;
import org.objectweb.proactive.core.mop.Proxy;
import org.objectweb.proactive.core.mop.StubObject;
import org.objectweb.proactive.core.node.NodeException;

import java.util.Hashtable;


/**
 * This class is used for creating components.
 * It acts as :<br>
 * 1. a bootstrap component<br>
 * 2. a GenericFactory for instantiating new components
 * 3. a utility class providing static methods to create collective interfaces and retreive references to ComponentParametersController<br>
 */
public class Fractive implements GenericFactory, Component, Factory {
    private static Logger logger = Logger.getLogger(Fractive.class.getName());
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
                     * Returns the {@link ContentController} interface of the given component.
                     *
                     * @param component a component.
                     * @return the {@link ContentController} interface of the given component.
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
            ProActiveInterface itf_ref_group = ProActiveComponentGroup.newActiveComponentInterfaceGroup(itf_type);
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
                if (contentDesc.getVirtualNode().getNodes().length > 1) { // cyclic node
                    Component components = (Component) ProActiveGroup.newGroup(Component.class.getName());
                    Group group_of_components = ProActiveGroup.getGroup(components);
                    Proxy proxy = null;

                    if (componentParameters.getHierarchicalType().equals(Constants.PRIMITIVE)) {
                        String original_component_name = componentParameters.getName();
                        Object active_objects = ProActive.newActive(contentDesc.getClassName(),
                                contentDesc.getConstructorParameters(),
                                contentDesc.getVirtualNode(),
                                contentDesc.getActivity(),
                                contentDesc.getFactory());

                        // iterate over the group : set names and proxies + add elements in the group
                        Object[] ao_table = ProActiveGroup.getGroup(active_objects)
                                                          .toArray();
                        for (int i = 0; i < ao_table.length; i++) {
                            ComponentParameters params = new ComponentParameters(componentParameters);

                            // change the name of each component (add an suffix)
                            params.setName(original_component_name +
                                Constants.CYCLIC_NODE_SUFFIX + i);
                            params.setStubOnReifiedObject(ao_table[i]);
                            // Find each proxy
                            proxy = ((StubObject) ao_table[i]).getProxy();
                            if (proxy == null) {
                                throw new ProActiveRuntimeException(
                                    "Cannot find a Proxy on the stub object: " +
                                    ao_table[i]);
                            }
                            group_of_components.add(ProActiveComponentRepresentativeFactory.instance()
                                                                                           .createComponentRepresentative(params,
                                    proxy));
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
                    // create the component on the first node retreived from the virtual node
                    ao = ProActive.newActive(contentDesc.getClassName(),
                            contentDesc.getConstructorParameters(),
                            contentDesc.getVirtualNode().getNode(),
                            contentDesc.getActivity(), contentDesc.getFactory());
                }
            }

            // the following corresponds to the case when the component is created on the local vm or on a single node.
            componentParameters.setStubOnReifiedObject(ao);
            // Find the proxy
            org.objectweb.proactive.core.mop.Proxy myProxy = ((StubObject) ao).getProxy();
            if (myProxy == null) {
                throw new ProActiveRuntimeException(
                    "Cannot find a Proxy on the stub object: " + ao);
            }
            return ProActiveComponentRepresentativeFactory.instance()
                                                          .createComponentRepresentative(componentParameters,
                myProxy);
        } catch (ActiveObjectCreationException e) {
            throw new InstantiationException(e.getMessage());
        } catch (NodeException e) {
            throw new InstantiationException(e.getMessage());
        } catch (ClassNotReifiableException e) {
            throw new InstantiationException(e.getMessage());
        } catch (ClassNotFoundException e) {
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
     * see {@link org.objectweb.fractal.api.factory.GenericFactory#newFcInstance(org.objectweb.fractal.api.Type, java.lang.Object, java.lang.Object)
     */
    public Component newFcInstance(Type arg0, Object arg1, Object arg2)
        throws InstantiationException {
        try {
            return newFcInstance(arg0, (ControllerDescription) arg1,
                (ContentDescription) arg2);
        } catch (ClassCastException e) {
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
                logger.error(e.getMessage());
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
