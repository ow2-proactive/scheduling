package org.objectweb.proactive.core.component.identity;

import org.apache.log4j.Logger;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.Interface;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.Type;
import org.objectweb.fractal.api.type.InterfaceType;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.component.ComponentParameters;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.ProActiveInterface;
import org.objectweb.proactive.core.component.asmgen.MetaObjectInterfaceClassGenerator;
import org.objectweb.proactive.core.component.controller.ComponentParametersController;
import org.objectweb.proactive.core.component.controller.ProActiveBindingController;
import org.objectweb.proactive.core.component.controller.ProActiveComponentParametersController;
import org.objectweb.proactive.core.component.controller.ProActiveContentController;
import org.objectweb.proactive.core.component.controller.ProActiveLifeCycleController;
import org.objectweb.proactive.core.component.request.ComponentRequestQueue;
import org.objectweb.proactive.core.group.Group;
import org.objectweb.proactive.core.group.ProActiveComponentGroup;
import org.objectweb.proactive.core.group.ProActiveGroup;

import java.io.Serializable;

import java.util.Iterator;
import java.util.Vector;


/**
 * The base class for managing components.
 * It defines the "membrane" of the fractal model : the controllers of the
 * components.
 *
 *
 * @author Matthieu Morel
 */
public class ProActiveComponentImpl implements ProActiveComponent, Interface,
    Serializable {
    protected static Logger logger = Logger.getLogger(ProActiveComponentImpl.class.getName());

    //private ComponentParameters componentParameters;
    private Interface[] interfaceReferences;
    private Body body;

    public ProActiveComponentImpl() {
    }

    /** Constructor for ProActiveComponent.
     * @param componentParameters
     * @param myBody a reference on the body (required notably to get a reference on the request
     * queue, used to control the life cycle of the component)
     */
    public ProActiveComponentImpl(ComponentParameters componentParameters,
        Body myBody) {
        this.body = myBody;

        //this.body = new RemoteBodyAdapter(myBody);
        //this.body = myBody;
        //this.componentParameters = componentParameters;
        boolean component_is_primitive = componentParameters.getHierarchicalType()
                                                            .equals(ComponentParameters.PRIMITIVE);

        // add interface references
        Vector interface_references_vector = new Vector(4);

        // 1. component identity
        interface_references_vector.addElement(this);

        //2. control interfaces
        interface_references_vector.add(new ProActiveLifeCycleController(this));

        // add component parameters controller
        ComponentParametersController component_parameters_controller = new ProActiveComponentParametersController(this);
        component_parameters_controller.setComponentParameters(componentParameters);
        interface_references_vector.add(component_parameters_controller);

        // add a binding controller
        // only exception : primitive without any client interface 
        if (!(componentParameters.getHierarchicalType().equals(ComponentParameters.PRIMITIVE) &&
                (componentParameters.getClientInterfaceTypes().length == 0))) {
            interface_references_vector.add(new ProActiveBindingController(this));
        } else {
            //bindingController = null;
            if (logger.isDebugEnabled()) {
                logger.debug("user component class of '" +
                    componentParameters.getName() +
                    "' does not have any client interface. It will have no BindingController");
            }
        }

        // create a content controller only if the component is a composite (or a parallel composite)
        if (componentParameters.getHierarchicalType().equals(ComponentParameters.COMPOSITE) ||
                (componentParameters.getHierarchicalType().equals(ComponentParameters.PARALLEL))) {
            // add and resize
            interface_references_vector.addElement(new ProActiveContentController(
                    this));
        }

        // 3. external functional interfaces
        InterfaceType[] interface_types = componentParameters.getComponentType()
                                                             .getFcInterfaceTypes();
        try {
            for (int i = 0; i < interface_types.length; i++) {
                ProActiveInterface itf_ref = null;

                // if we have a COLLECTION CLIENT interface, we should see the delegatee ("impl" field) as a group
                if (interface_types[i].isFcClientItf() &&
                        interface_types[i].isFcCollectionItf()) {
                    //Component itfg = ProActiveComponentGroup.newActiveComponentGroup(interface_types[i]);
                    itf_ref = createInterfaceOnGroupOfDelegatees(interface_types[i],
                            component_is_primitive);
                }
                // if we have a server port of a  PARALLEL component, we also create a group proxy on the delegatee field
                else if (componentParameters.getHierarchicalType().equals(ComponentParameters.PARALLEL) &&
                        (!interface_types[i].isFcClientItf())) {
                    // parallel component have a collective port on their server interfaces
                    itf_ref = createInterfaceOnGroupOfDelegatees(interface_types[i],
                            component_is_primitive);
                } else {
                    itf_ref = MetaObjectInterfaceClassGenerator.instance()
                                                               .generateInterface(interface_types[i].getFcItfName(),
                            this, interface_types[i], true,
                            component_is_primitive);
                    // all interfaces are also internal
                    //component_is_composite ? true : false); // interfaces are both external and internal for composite components
                }

                // set delegation link
                if (componentParameters.getHierarchicalType().equals(ComponentParameters.PRIMITIVE)) {
                    // no group case
                    if (!interface_types[i].isFcCollectionItf()) {
                        if (!interface_types[i].isFcClientItf()) {
                            (itf_ref).setFcItfImpl(getReifiedObject());
                        } else if (interface_types[i].isFcClientItf()) {
                            (itf_ref).setFcItfImpl(null);
                        }
                    }
                } else {
                    // we have a composite component
                    // thus the reified object is not the one implementing the functional interface
                    // the designation of the delegation object (impl) is deferred to binding time
                }
                interface_references_vector.addElement(itf_ref);

                if (logger.isDebugEnabled()) {
                    //logger.debug(((ProActiveInterface) itf_ref).toString());
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

        // put all in a table
        interfaceReferences = (Interface[]) interface_references_vector.toArray(new Interface[interface_references_vector.size()]);
    }

    // returns a generated interface reference, whose impl field is a group
    // It is able to handle multiple bindings
    private ProActiveInterface createInterfaceOnGroupOfDelegatees(
        InterfaceType itfType, boolean isPrimitive) throws Exception {
        ProActiveInterface itf_ref = MetaObjectInterfaceClassGenerator.instance()
                                                                      .generateInterface(itfType.getFcItfName(),
                this, itfType, true, isPrimitive);

        // create a group of impl target objects
        //		// should not have a ProActiveComponentRepresentativeImpl, but a sub-type of the functional interface 
        //	Component ci_group = ProActiveComponentGroup.newActiveComponentGroup(itfType);
        //	itf_ref.setFcItfImpl(ci_group);
        ProActiveInterface itf_ref_group = ProActiveComponentGroup.newActiveComponentInterfaceGroup(itfType);
        itf_ref.setFcItfImpl(itf_ref_group);
        return itf_ref;
    }

    /**
     * @see org.objectweb.fractal.api.Component#getFcInterface(String)
     */
    public Object getFcInterface(String interfaceName)
        throws NoSuchInterfaceException {
        if (interfaceReferences != null) {
            for (int i = 0; i < interfaceReferences.length; i++) {
                if (ProActiveGroup.isGroup(interfaceReferences[i])) {
                    // need to find at least one occurence of the interface
                    // if exists but not for all elements of the group -> throw error
                    // if does not exist -> do nothing
                    int count = 0;
                    Group itf_ref_group = ProActiveGroup.getGroup(interfaceReferences[i]);
                    Iterator iterator = itf_ref_group.iterator();

                    // ensure groups are coherent (ie : if 1 interface of the given name exists,
                    // all of the group elements should be of this kind
                    while (iterator.hasNext()) {
                        Interface group_element = (Interface) iterator.next();
                        if (group_element.getFcItfName().equals(interfaceName)) {
                            count++;
                        }
                    }
                    if (count > 0) {
                        if (count == itf_ref_group.size()) {
                            return interfaceReferences[i];
                        } else {
                            throw new NoSuchInterfaceException(
                                "some elements of the collection are not named " +
                                interfaceName);
                        }
                    }
                } else {
                    //looking into single interface
                    if (interfaceReferences[i].getFcItfName().equals(interfaceName)) {
                        return interfaceReferences[i];
                    }
                }
            }
        }
        throw new NoSuchInterfaceException(interfaceName);
    }

    /**
     * @see org.objectweb.fractal.api.Component#getFcInterfaces()
     */
    public Object[] getFcInterfaces() {
        Vector external_interfaces = new Vector(interfaceReferences.length);
        for (int i = 0; i < interfaceReferences.length; i++) {
            if (!interfaceReferences[i].isFcInternalItf()) {
                external_interfaces.add(interfaceReferences[i]);
            }
        }
        external_interfaces.trimToSize();
        return external_interfaces.toArray();
    }

    /**
     * @see org.objectweb.fractal.api.Component#getFcType()
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
     * @see org.objectweb.fractal.api.Interface#getFcItfName()
     */
    public String getFcItfName() {
        return Constants.COMPONENT;
    }

    /**
     * @see org.objectweb.fractal.api.Interface#getFcItfOwner()
     */
    public Component getFcItfOwner() {
        return (Component) this;
    }

    /**
     * @see org.objectweb.fractal.api.Interface#getFcItfType()
     */
    public Type getFcItfType() {
        return getFcType();
    }

    /**
     * @see org.objectweb.fractal.api.Interface#isFcInternalItf()
     */
    public boolean isFcInternalItf() {
        return false;
    }

    /**
     * @see org.objectweb.proactive.core.body.component.ProActiveComponent#getComponentIdentity()
     */
    public Component getComponentIdentity() {
        return this;
    }

    /**
     * @see org.objectweb.proactive.core.body.component.ProActiveComponent#getHierarchicalType()
     */
    public String getHierarchicalType() throws NoSuchInterfaceException {
        //return componentParameters.getHierarchicalType();
        return ((ComponentParametersController) getFcInterface(Constants.COMPONENT_PARAMETERS_CONTROLLER)).getComponentParameters()
                .getHierarchicalType();
    }

    /**
     * Returns the reifiedObject.
     * If the component is a composite, a basic do-nothing instance of class Composite
     * is returned.
     * @return the reified object underneath
     */
    public Object getReifiedObject() {
        return getBody().getReifiedObject();
    }

    /**
     * @return the request queue of the current active object.
     */
    public ComponentRequestQueue getRequestQueue() {
        return (ComponentRequestQueue) getBody().getRequestQueue();
    }

    /**
     * @return a ComponentParameters instance, corresponding to the configuration of the current component
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
     * components are equal if they have the same parameters.
     * @see org.objectweb.proactive.core.component.ProActiveComponent#equals(org.objectweb.fractal.api.Component)
     */
    public boolean equals(Component componentIdentity) {
        try {
            return ((ComponentParametersController) componentIdentity
                    .getFcInterface(Constants.COMPONENT_PARAMETERS_CONTROLLER)).getComponentParameters()
                    .equals(getComponentParameters());
        } catch (NoSuchInterfaceException nsie) {
            throw new ProActiveRuntimeException("cannot compare objects", nsie);
        }
    }

    /**
     * FIXME : improper implementation ?
     */
    public int hashCode() {
        return 0;
    }
}
