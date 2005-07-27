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
package org.objectweb.proactive.core.component.controller;

import org.apache.log4j.Logger;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.Interface;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.api.control.LifeCycleController;
import org.objectweb.fractal.api.factory.InstantiationException;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.fractal.api.type.TypeFactory;
import org.objectweb.fractal.util.Fractal;

import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.component.Binding;
import org.objectweb.proactive.core.component.Bindings;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.ProActiveInterface;
import org.objectweb.proactive.core.component.identity.ProActiveComponent;
import org.objectweb.proactive.core.component.interception.ProActiveOutputInterfaceInterceptor;
import org.objectweb.proactive.core.component.type.ProActiveInterfaceType;
import org.objectweb.proactive.core.component.type.ProActiveTypeFactory;
import org.objectweb.proactive.core.group.Group;
import org.objectweb.proactive.core.group.ProActiveGroup;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;


/**
 * Implementation of the {@link org.objectweb.fractal.api.control.BindingController} interface.
 *
 * @author Matthieu Morel
 *
 */
public class ProActiveBindingControllerImpl extends AbstractProActiveController
    implements ProActiveBindingController, Serializable {
    private static Logger logger = ProActiveLogger.getLogger(Loggers.COMPONENTS);
    private Bindings bindings; // key = clientInterfaceName ; value = Binding
    private List collectiveInterfacesNames = null;
    private List singleInterfacesNames = null;

    public ProActiveBindingControllerImpl(Component owner) {
        super(owner);
        try {
            setItfType(ProActiveTypeFactory.instance().createFcItfType(Constants.BINDING_CONTROLLER,
                    ProActiveBindingController.class.getName(),
                    TypeFactory.SERVER, TypeFactory.MANDATORY,
                    TypeFactory.SINGLE));
        } catch (InstantiationException e) {
            throw new ProActiveRuntimeException("cannot create controller " +
                this.getClass().getName());
        }

        bindings = new Bindings();
    }

    public void addBinding(Binding binding) {
        bindings.add(binding);
    }

    protected void checkBindability(String clientItfName, Interface serverItf)
        throws NoSuchInterfaceException, IllegalBindingException, 
            IllegalLifeCycleException {
        // TODO_M handle internal interfaces
        //        if (server_itf_type.isFcClientItf()) {
        //            throw new IllegalBindingException("cannot bind client interface " + clientItfName + " to other client interface " +server_itf_type.getFcItfName() );
        //        }
        //        if (!client_itf_type.isFcClientItf()) {
        //            throw new IllegalBindingException("cannot bind client interface " + clientItfName + " to other client interface " +server_itf_type.getFcItfName() );
        //        }
        if (!(Fractal.getLifeCycleController(getFcItfOwner())).getFcState()
                  .equals(LifeCycleController.STOPPED)) {
            throw new IllegalLifeCycleException(
                "component has to be stopped to perform binding operations");
        }

        // check for binding primitive component can only be performed in the primitive component 
        if (!isPrimitive()) {
            checkClientInterfaceName(clientItfName);
            if (existsBinding(clientItfName)) {
                if (!((ProActiveInterfaceType) ((Interface) getFcItfOwner()
                                                                    .getFcInterface(clientItfName)).getFcItfType()).isFcCollectionItf()) {
                    // binding from a single client interface : only 1 binding is allowed
                    // except for parallell components
                    if (!(isParallel() &&
                            ProActiveGroup.isGroup(
                                ((ProActiveInterface) getFcItfOwner()
                                                              .getFcInterface(clientItfName)).getFcItfImpl()))) {
                        logger.warn(Fractal.getNameController(getFcItfOwner())
                                           .getFcName() + "." + clientItfName +
                            " is already bound");

                        throw new IllegalBindingException(clientItfName +
                            " is already bound");
                    }
                } else {
                    // binding from a collective interface
                    if (((InterfaceType) serverItf.getFcItfType()).isFcClientItf()) {
                        // binding to a client(external) interface --> not OK
                        throw new IllegalBindingException(serverItf.getFcItfName() +
                            " is not a server interface");
                    }
                }
            }
        }

        // TODO_M : check bindings between external client interfaces   
        // see next, but need to consider internal interfaces (i.e. valid if server AND internal)
        //		if (((InterfaceType) serverItf.getFcItfType()).isFcClientItf()) {
        //			throw new IllegalBindingException(serverItf.getFcItfName() + " is not a server interface");
        //		}
        // TODO_M : other checks are to be performed (viability of the bindings)
    }

    protected void checkUnbindability(String clientItfName)
        throws NoSuchInterfaceException, IllegalBindingException, 
            IllegalLifeCycleException {
        checkLifeCycleIsStopped();
        checkClientInterfaceName(clientItfName);
        if (!existsBinding(clientItfName)) {
            throw new IllegalBindingException(clientItfName +
                " is not yet bound");
        }
    }

    /**
     *
     * @param clientItfName the name of the client interface
     * @return a Binding object if single binding, List of Binding objects otherwise
     */
    public Object removeBinding(String clientItfName) {
        return bindings.remove(clientItfName);
    }

    /**
     *
     * @param clientItfName the name of the client interface
     * @return a Binding object if single binding, List of Binding objects otherwise
     */
    public Object getBinding(String clientItfName) {
        return bindings.get(clientItfName);
    }

    /**
     * see @link org.objectweb.fractal.api.control.BindingController#lookupFc(String)
     */
    public Object lookupFc(String clientItfName)
        throws NoSuchInterfaceException {
        if (!existsBinding(clientItfName)) {
            return null;
        } else {
            //            // FIXME 
            //            if (getBinding(clientItfName) instanceof Collection) {
            //                logger.error(
            //                    "you are looking up a collection of bindings. This method cannot return one single Interface object");
            //                return null;
            //            } else {
            return ((Binding) getBinding(clientItfName)).getServerInterface();
        }
    }

    /**
     * implementation of the interface BindingController
     * see {@link BindingController#bindFc(java.lang.String, java.lang.Object)}
     */
    public void bindFc(String clientItfName, Object serverItf)
        throws NoSuchInterfaceException, IllegalBindingException, 
            IllegalLifeCycleException {
        checkBindability(clientItfName, (Interface) serverItf);
        
        // if output interceptors are defined
        // TODO_M check with groups : interception is here done at the beginning of the group invocation,
        // not for each element of the group
        List outputInterceptors = ((ProActiveComponent)getFcItfOwner()).getOutputInterceptors(); 
        if (!outputInterceptors.isEmpty()) {
            try {
                serverItf = ProActiveOutputInterfaceInterceptor.newInstance((ProActiveInterface)serverItf,
                        outputInterceptors);
            } catch (ClassNotFoundException e) {
                    logger.error("could not generate output interceptor for client interface " + clientItfName + " : " + e.getMessage());
                    if (logger.isDebugEnabled()) {
                        logger.error(e.getStackTrace().toString());
                }
            }
        }


        if (isPrimitive()) {
            // binding operation is delegated
            primitiveBindFc(clientItfName, (Interface) serverItf);
            return;
        }

        // composite or parallel
        InterfaceType client_itf_type;
        String collective_itf_name = pertainsToACollectiveInterface(clientItfName);
        if (collective_itf_name != null) {
            // this interface is part of a collective interface
            client_itf_type = (InterfaceType) ((Interface) getFcItfOwner()
                                                               .getFcInterface(collective_itf_name)).getFcItfType();
        } else {
            client_itf_type = (InterfaceType) ((Interface) getFcItfOwner()
                                                               .getFcInterface(clientItfName)).getFcItfType();
        }

        if (isComposite()) {
            compositeBindFc(clientItfName, client_itf_type,
                (Interface) serverItf);
        } else {
            parallelBindFc(clientItfName, client_itf_type, (Interface) serverItf);
        }
    }

    private void primitiveBindFc(String clientItfName, Interface serverItf)
        throws NoSuchInterfaceException, IllegalBindingException, 
            IllegalLifeCycleException {
        // delegate binding operation to the reified object
        BindingController user_binding_controller = (BindingController) ((ProActiveComponent) getFcItfOwner()).getReferenceOnBaseObject();

        // serverItf cannot be a Future (because it has to be casted) => make sure if binding to a composite's internal interface
        serverItf = (Interface) ProActive.getFutureValue(serverItf);
        user_binding_controller.bindFc(clientItfName, serverItf);
    }

    private void parallelBindFc(String clientItfName,
        InterfaceType clientItfType, Interface serverItf)
        throws NoSuchInterfaceException, IllegalBindingException, 
            IllegalLifeCycleException {
        ProActiveInterface clientItf = (ProActiveInterface) getFcItfOwner()
                                                                .getFcInterface(clientItfType.getFcItfName());

        // 1. parallel.serverItf -- > subcomponent.serverItf
        // check : 
        // 	- whether the client interface is actually a server interface for the parallel component
        boolean condition1 = !clientItfType.isFcClientItf();

        // - whether the targeted server interface belongs to an internal component  
        boolean condition2 = ((ProActiveContentController) (Fractal.getContentController(getFcItfOwner()))).isSubComponent(serverItf.getFcItfOwner());
        if (condition1 && condition2) {
            if (ProActiveGroup.isGroup(clientItf.getFcItfImpl())) {
                Group group = ProActiveGroup.getGroup(clientItf.getFcItfImpl());
                if (clientItfType.getFcItfName().equals(clientItfName)) {
                    group.add(serverItf);
                } else {
                    // different names and "lazy instantiation", as in the Fractal2.0 spec
                    group.addNamedElement(clientItfName, serverItf);
                }
            } else {
                throw new IllegalBindingException(
                    "illegal binding : server interface " + clientItfName +
                    " of parallel component " +
                    Fractal.getNameController(getFcItfOwner()).getFcName() +
                    " should be a collective interface");
            }
            addBinding(new Binding(clientItf, clientItfName, serverItf));
        } else if (!condition1 && !condition2) {
            // 2. parallel.clientItf --> othercomponent.serverItf
            // it is a standard composite binding
            compositeBindFc(clientItfName, clientItfType, serverItf);
        } else {
            throw new IllegalBindingException("illegal binding of " +
                Fractal.getNameController(getFcItfOwner()).getFcName() + '.' +
                clientItfName);
        }
    }

    /*
     * binding method enforcing Interface type for the server interface, for composite components
     */
    private void compositeBindFc(String clientItfName,
        InterfaceType clientItfType, Interface serverItf)
        throws NoSuchInterfaceException, IllegalBindingException, 
            IllegalLifeCycleException {
        ProActiveInterface clientItf = (ProActiveInterface) getFcItfOwner()
                                                                .getFcInterface(clientItfType.getFcItfName());

        // if we have a collection interface, the impl object is actually a group of references to interfaces
        // Thus we have to add the link to the new interface in this group
        // same for client interfaces of parallel components
        if (clientItfType.getFcItfName().equals(clientItfName)) {
            if (clientItfType.isFcCollectionItf() ||
                    (isParallel() && !clientItfType.isFcClientItf())) {
                // collective binding, unnamed interface
                // TODO provide a default name? 
                Group itf_group = ProActiveGroup.getGroup(clientItf.getFcItfImpl());
                itf_group.add(serverItf);
            } else {
                // single binding
                clientItf.setFcItfImpl(serverItf);
            }
        } else {
            if (clientItfType.isFcCollectionItf() ||
                    (isParallel() && !clientItfType.isFcClientItf())) {
                // collective binding, named interface (as in the Fractal 2.0 spec)
                Group itf_group = ProActiveGroup.getGroup(clientItf.getFcItfImpl());
                itf_group.addNamedElement(clientItfName, serverItf);
            } else {
                throw new NoSuchInterfaceException("Cannot bind interface " +
                    clientItfName +
                    " because it does not correspond to the specified type");
            }
        }
        addBinding(new Binding(clientItf, clientItfName, serverItf));
    }

    /*
     * @see org.objectweb.fractal.api.control.BindingController#unbindFc(String)
     *
     * CAREFUL : unbinding action on collective interfaces will remove all the bindings to this interface.
     * This is also the case when removing bindings from the server interface of a parallel component
     *  (yes you can do unbindFc(parallelServerItfName) !)
     */
    public void unbindFc(String clientItfName)
        throws NoSuchInterfaceException, IllegalBindingException, 
            IllegalLifeCycleException {
        // remove from bindings and set impl object to null
        if (isPrimitive()) {
            // delegate to primitive component
            BindingController user_binding_controller = (BindingController) ((ProActiveComponent) getFcItfOwner()).getReferenceOnBaseObject();
            user_binding_controller.unbindFc(clientItfName);
        } else {
            checkUnbindability(clientItfName);

            if (pertainsToACollectiveInterface(clientItfName) != null) {
                Binding binding = (Binding) (removeBinding(clientItfName));
                ((ProActiveInterface) (binding.getClientInterface())).setFcItfImpl(null);
            } else {
                ProActiveInterface clientItf = (ProActiveInterface) getFcItfOwner()
                                                                        .getFcInterface(clientItfName);

                // if the name is this of a collection interface, we actually remove ALL THE BINDINGS to this interface
                if (ProActiveGroup.isGroup(clientItf.getFcItfImpl())) {
                    // we do not set the delegatee to null, the delegatee being a group, 
                    // but we remove all the elements of this group
                    Group group = ProActiveGroup.getGroup(clientItf.getFcItfImpl());
                    group.clear();
                    removeBinding(clientItfName);
                } else {
                    Binding binding = (Binding) (removeBinding(clientItfName));
                    ((ProActiveInterface) (binding.getClientInterface())).setFcItfImpl(null);
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see org.objectweb.fractal.api.control.BindingController#listFc()
     */
    public String[] listFc() {
        InterfaceType[] itfs_types = ((ComponentType) getFcItfOwner().getFcType()).getFcInterfaceTypes();
        List client_itfs_names = new ArrayList();
        for (int i = 0; i < itfs_types.length; i++) {
            if (itfs_types[i].isFcClientItf()) {
                client_itfs_names.add(itfs_types[i].getFcItfName());
            }
        }
        return (String[]) client_itfs_names.toArray(new String[client_itfs_names.size()]);
    }

    private void setInterfacesNames() {
        collectiveInterfacesNames = new ArrayList();
        singleInterfacesNames = new ArrayList();
        Interface[] interfaces = (Interface[]) (Arrays.asList(getFcItfOwner()
                                                                  .getFcInterfaces())
                                                      .toArray(new Interface[getFcItfOwner()
                                                                                 .getFcInterfaces().length]));
        for (int i = 0; i < interfaces.length; i++) {
            if (((InterfaceType) interfaces[i].getFcItfType()).isFcCollectionItf()) {
                getCollectiveInterfacesNames().add(interfaces[i].getFcItfName());
            } else {
                singleInterfacesNames.add(interfaces[i].getFcItfName());
            }
        }
    }

    protected boolean existsBinding(String clientItfName)
        throws NoSuchInterfaceException {
        if (isPrimitive()) {
            return (((BindingController) ((ProActiveComponent) getFcItfOwner()).getReferenceOnBaseObject()).lookupFc(clientItfName) != null);
        } else {
            return bindings.containsBindingOn(clientItfName);
        }
    }

    protected void checkClientInterfaceName(String clientItfName)
        throws NoSuchInterfaceException {
        if (getSingleInterfacesNames().contains(clientItfName)) {
            return;
        } else {
            if (pertainsToACollectiveInterface(clientItfName) != null) {
                return;
            }
            throw new NoSuchInterfaceException(clientItfName +
                " does not correspond to a single nor a collective interface");
        }
    }

    // returns null if clientItfName does not begin with the name of a collective interface
    // returns the name of the collective interface otherwise
    private String pertainsToACollectiveInterface(String clientItfName) {
        String collective_itf_name = null;
        Iterator it = getCollectiveInterfacesNames().iterator();
        String current_itf_name = null;
        while (it.hasNext()) {
            current_itf_name = (String) it.next();
            if (clientItfName.startsWith(current_itf_name)) {
                collective_itf_name = current_itf_name;
                break;
            }
        }
        return collective_itf_name;
    }

    private List getCollectiveInterfacesNames() {
        if (collectiveInterfacesNames == null) {
            setInterfacesNames();
        }
        return collectiveInterfacesNames;
    }

    /**
     * Returns the interfaces with cardinality equal to single
     * @return a list of the names of the interfaces with single cardinality
     */
    public List getSingleInterfacesNames() {
        if (singleInterfacesNames == null) {
            setInterfacesNames();
        }
        return singleInterfacesNames;
    }

    public Boolean isBound() {
        String[] client_itf_names = listFc();
        for (int i = 0; i < client_itf_names.length; i++) {
            try {
                if (existsBinding(client_itf_names[i])) {
                    return new Boolean(true);
                }
            } catch (NoSuchInterfaceException logged) {
                logger.error("cannot find interface " + client_itf_names[i] +
                    " : " + logged.getMessage());
            }
        }
        return new Boolean(false);
    }
}
