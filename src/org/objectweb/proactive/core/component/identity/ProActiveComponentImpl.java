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

import org.apache.log4j.Logger;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.Interface;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.Type;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.api.type.InterfaceType;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.component.ComponentParameters;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.ProActiveInterface;
import org.objectweb.proactive.core.component.asmgen.MetaObjectInterfaceClassGenerator;
import org.objectweb.proactive.core.component.controller.ComponentParametersController;
import org.objectweb.proactive.core.component.controller.ProActiveBindingController;
import org.objectweb.proactive.core.component.controller.ProActiveComponentParametersController;
import org.objectweb.proactive.core.component.controller.ProActiveContentController;
import org.objectweb.proactive.core.component.controller.ProActiveLifeCycleController;
import org.objectweb.proactive.core.component.controller.ProActiveSuperControllerImpl;
import org.objectweb.proactive.core.component.representative.ProActiveComponentRepresentative;
import org.objectweb.proactive.core.component.representative.ProActiveComponentRepresentativeFactory;
import org.objectweb.proactive.core.component.request.ComponentRequestQueue;
import org.objectweb.proactive.core.group.ProActiveComponentGroup;
import org.objectweb.proactive.core.mop.ClassNotReifiableException;
import org.objectweb.proactive.core.mop.ConstructionOfProxyObjectFailedException;
import org.objectweb.proactive.core.mop.InvalidProxyClassException;
import org.objectweb.proactive.core.mop.MOP;
import org.objectweb.proactive.core.mop.ReifiedCastException;
import org.objectweb.proactive.core.mop.StubObject;
import org.objectweb.proactive.core.util.ProActiveLogger;

import java.io.Serializable;

import java.util.ArrayList;


/**
 * The base class for managing components. It builds the "membrane" in the
 * Fractal terminology : the controllers of the components.
 *
 * @author Matthieu Morel
 */
public class ProActiveComponentImpl implements ProActiveComponent, Interface,
    Serializable {
    
    protected static final Logger logger = ProActiveLogger.getLogger("components");

    //private ComponentParameters componentParameters;
    private Interface[] interfaceReferences;
    private Body body;

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
        interface_references_list.add(new ProActiveLifeCycleController(this));
        // add component parameters controller
        ComponentParametersController component_parameters_controller = new ProActiveComponentParametersController(this);
        component_parameters_controller.setComponentParameters(componentParameters);
        interface_references_list.add(component_parameters_controller);
        // add a binding controller
        // only exception : primitive without any client interface
        if (!(componentParameters.getHierarchicalType().equals(Constants.PRIMITIVE) &&
                (componentParameters.getClientInterfaceTypes().length == 0))) {
            interface_references_list.add(new ProActiveBindingController(this));
        } else {
            //bindingController = null;
            if (logger.isDebugEnabled()) {
                logger.debug("user component class of '" +
                    componentParameters.getName() +
                    "' does not have any client interface. It will have no BindingController");
            }
        }

        // create a content controller only if the component is a composite (or
        // a parallel composite)
        boolean current_component_is_primitive = !(componentParameters.getHierarchicalType()
                                                                      .equals(Constants.COMPOSITE) ||
            (componentParameters.getHierarchicalType().equals(Constants.PARALLEL)));
        if (!current_component_is_primitive) {
            // add and resize
            interface_references_list.add(new ProActiveContentController(this));
        }

        // add a super-controller
        interface_references_list.add(new ProActiveSuperControllerImpl(this));
        // 3. external functional interfaces
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
                        //Component itfg =
                        // ProActiveComponentGroup.newActiveComponentGroup(interface_types[i]);
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
                    } else {
                        itf_ref = MetaObjectInterfaceClassGenerator.instance()
                                                                   .generateInterface(interface_types[i].getFcItfName(),
                                this, interface_types[i], false,
                                component_is_primitive);
                        // functional interfaces are external interfaces (at
                        // least they are tagged as external)
                    } // set delegation link
                    if (componentParameters.getHierarchicalType().equals(Constants.PRIMITIVE)) {
                        // TODO no group case
                        if (!interface_types[i].isFcCollectionItf()) {
                            if (!interface_types[i].isFcClientItf()) {
                                (itf_ref).setFcItfImpl(getReferenceOnBaseObject());
                            } else if (interface_types[i].isFcClientItf()) {
                                (itf_ref).setFcItfImpl(null);
                            }
                        }
                    } else { // we have a composite component
                        // thus the reified object is not the one implementing
                        // the functional interface
                        // the designation of the delegation object (impl) is
                        // deferred to binding time
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
        } // put all in a table
        interfaceReferences = (Interface[]) interface_references_list.toArray(new Interface[interface_references_list.size()]);
    }

    // returns a generated interface reference, whose impl field is a group
    // It is able to handle multiple bindings
    private ProActiveInterface createInterfaceOnGroupOfDelegatees(
        InterfaceType itfType, boolean isPrimitive) throws Exception {
        ProActiveInterface itf_ref = MetaObjectInterfaceClassGenerator.instance()
                                                                      .generateInterface(itfType.getFcItfName(),
                this, itfType, false, isPrimitive);

        // create a group of impl target objects
        //		// should not have a ProActiveComponentRepresentativeImpl, but a
        // sub-type of the functional interface
        //	Component ci_group =
        // ProActiveComponentGroup.newActiveComponentGroup(itfType);
        //	itf_ref.setFcItfImpl(ci_group);
        ProActiveInterface itf_ref_group = ProActiveComponentGroup.newComponentInterfaceGroup(itfType);
        itf_ref.setFcItfImpl(itf_ref_group);
        return itf_ref;
    }

    /**
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

                //}
            }
        }
        throw new NoSuchInterfaceException(interfaceName);
    }

    /**
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

    /**
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
     * @return the request queue of the current active object.
     */
    public ComponentRequestQueue getRequestQueue() {
        return (ComponentRequestQueue) getBody().getRequestQueue();
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
     * {@org.objectweb.proactive.core.component.identity.ProActiveComponent#getRepresentativeOnThis()}
     */
    public Component getRepresentativeOnThis() {
        try {
            return ProActiveComponentRepresentativeFactory.instance()
                                                          .createComponentRepresentative((ComponentType) getFcType(),
                ((StubObject) MOP.turnReified(body.getReifiedObject().getClass()
                                                  .getName(),
                    org.objectweb.proactive.core.Constants.DEFAULT_BODY_PROXY_CLASS_NAME,
                    new Object[] { body }, body.getReifiedObject())).getProxy());
        } catch (ConstructionOfProxyObjectFailedException e) {
            throw new ProActiveRuntimeException("This component could not generate a reference on itself",
                e);
        } catch (InvalidProxyClassException e) {
            throw new ProActiveRuntimeException("This component could not generate a reference on itself",
                e);
        } catch (ClassNotFoundException e) {
            throw new ProActiveRuntimeException("This component could not generate a reference on itself",
                e);
        } catch (ReifiedCastException e) {
            throw new ProActiveRuntimeException("This component could not generate a reference on itself",
                e);
        } catch (ClassNotReifiableException e) {
            throw new ProActiveRuntimeException("This component could not generate a reference on itself",
                e);
        }
    }
}
