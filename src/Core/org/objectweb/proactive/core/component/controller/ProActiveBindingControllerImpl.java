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
package org.objectweb.proactive.core.component.controller;

import java.io.Serializable;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.api.PAGroup;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.component.Binding;
import org.objectweb.proactive.core.component.Bindings;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.Fractive;
import org.objectweb.proactive.core.component.ItfStubObject;
import org.objectweb.proactive.core.component.ProActiveInterface;
import org.objectweb.proactive.core.component.Utils;
import org.objectweb.proactive.core.component.exceptions.InterfaceGenerationFailedException;
import org.objectweb.proactive.core.component.gen.GatherItfAdapterProxy;
import org.objectweb.proactive.core.component.gen.OutputInterceptorClassGenerator;
import org.objectweb.proactive.core.component.identity.ProActiveComponent;
import org.objectweb.proactive.core.component.identity.ProActiveComponentImpl;
import org.objectweb.proactive.core.component.representative.ItfID;
import org.objectweb.proactive.core.component.type.ProActiveInterfaceType;
import org.objectweb.proactive.core.component.type.ProActiveInterfaceTypeImpl;
import org.objectweb.proactive.core.component.type.ProActiveTypeFactoryImpl;


/**
 * Implementation of the
 * {@link org.objectweb.fractal.api.control.BindingController BindingController} interface.
 *
 * @author The ProActive Team
 *
 */
public class ProActiveBindingControllerImpl extends AbstractProActiveController implements
        ProActiveBindingController, Serializable {
    protected Bindings bindings; // key = clientInterfaceName ; value = Binding

    //    private Map<String, Map<ProActiveComponent, List<String>>> bindingsOnServerItfs = new HashMap<String, Map<ProActiveComponent,List<String>>>(0);

    // Map(serverItfName, Map(owner, clientItfName))
    public ProActiveBindingControllerImpl(Component owner) {
        super(owner);
        bindings = new Bindings();
    }

    @Override
    protected void setControllerItfType() {
        try {
            setItfType(ProActiveTypeFactoryImpl.instance().createFcItfType(Constants.BINDING_CONTROLLER,
                    ProActiveBindingController.class.getName(), TypeFactory.SERVER, TypeFactory.MANDATORY,
                    TypeFactory.SINGLE));
        } catch (InstantiationException e) {
            throw new ProActiveRuntimeException("cannot create controller type for controller " +
                this.getClass().getName());
        }
    }

    public void addBinding(Binding binding) {
        bindings.add(binding);
    }

    protected void checkBindability(String clientItfName, Interface serverItf)
            throws NoSuchInterfaceException, IllegalBindingException, IllegalLifeCycleException {
        if (!(serverItf instanceof ProActiveInterface)) {
            throw new IllegalBindingException("Can only bind interfaces of type ProActiveInterface");
        }

        ProActiveInterfaceType clientItfType = (ProActiveInterfaceType) Utils
                .getItfType(clientItfName, owner);

        // TODO_M handle internal interfaces
        // if (server_itf_type.isFcClientItf()) {
        // throw new IllegalBindingException("cannot bind client interface " +
        // clientItfName + " to other client interface "
        // +server_itf_type.getFcItfName() );
        // }
        // if (!client_itf_type.isFcClientItf()) {
        // throw new IllegalBindingException("cannot bind client interface " +
        // clientItfName + " to other client interface "
        // +server_itf_type.getFcItfName() );
        // }
        if (!(Fractal.getLifeCycleController(getFcItfOwner())).getFcState().equals(
                LifeCycleController.STOPPED)) {
            throw new IllegalLifeCycleException("component has to be stopped to perform binding operations");
        }

        // multicast interfaces : interfaces must be compatible
        // (rem : itf is null when it is a single itf not yet bound
        if (Utils.isMulticastItf(clientItfName, getFcItfOwner())) {
            Fractive.getMulticastController(owner).ensureCompatibility(clientItfType,
                    (ProActiveInterface) serverItf);

            // ensure multicast interface of primitive component is initialized
            if (isPrimitive()) {
                BindingController userBindingController = (BindingController) (owner)
                        .getReferenceOnBaseObject();

                if ((userBindingController.lookupFc(clientItfName) == null) ||
                    !(PAGroup.isGroup(userBindingController.lookupFc(clientItfName)))) {
                    userBindingController.bindFc(clientItfName, owner.getFcInterface(clientItfName));
                }
            }
        }

        if (Utils.isGathercastItf(serverItf)) {
            Fractive.getGathercastController(owner).ensureCompatibility(clientItfType,
                    (ProActiveInterface) serverItf);
        }
        //  TODO type checkings for other cardinalities
        else if (Utils.isSingletonItf(clientItfName, getFcItfOwner())) {
            InterfaceType sType = (InterfaceType) serverItf.getFcItfType();

            //InterfaceType cType = (InterfaceType)((ProActiveInterface)owner.getFcInterface(clientItfName)).getFcItfType();
            InterfaceType cType = ((ComponentType) owner.getFcType()).getFcInterfaceType(clientItfName);

            try {
                Class<?> s = Class.forName(sType.getFcItfSignature());
                Class<?> c = Class.forName(cType.getFcItfSignature());
                if (!c.isAssignableFrom(s)) {
                    throw new IllegalBindingException("The server interface type " + s.getName() +
                        " is not a subtype of the client interface type " + c.getName());
                }
            } catch (ClassNotFoundException e) {
                throw new IllegalBindingException("Cannot find type of interface : " + e.getMessage());
            }
        }

        // check for binding primitive component can only be performed in the
        // primitive component
        if (!isPrimitive()) {
            // removed the following checkings as they did not consider composite server itfs
            //            checkClientInterfaceName(clientItfName);
            if (existsBinding(clientItfName)) {
                if (!((ProActiveInterfaceTypeImpl) ((Interface) getFcItfOwner().getFcInterface(clientItfName))
                        .getFcItfType()).isFcCollectionItf()) {
                    // binding from a single client interface : only 1 binding
                    // is allowed
                    controllerLogger.warn(Fractal.getNameController(getFcItfOwner()).getFcName() + "." +
                        clientItfName + " is already bound");

                    throw new IllegalBindingException(clientItfName + " is already bound");
                } else {
                    // binding from a collection interface
                    if (((InterfaceType) serverItf.getFcItfType()).isFcClientItf()) {
                        // binding to a client(external) interface --> not OK
                        throw new IllegalBindingException(serverItf.getFcItfName() +
                            " is not a server interface");
                    }
                }
            }
        }

        // TODO_M : check bindings between external client interfaces
        // see next, but need to consider internal interfaces (i.e. valid if
        // server AND internal)
        // TODO_M : check bindings crossing composite membranes
    }

    protected void checkUnbindability(String clientItfName) throws NoSuchInterfaceException,
            IllegalBindingException, IllegalLifeCycleException {
        checkLifeCycleIsStopped();
        checkClientInterfaceName(clientItfName);

        if (!existsBinding(clientItfName)) {
            throw new IllegalBindingException(clientItfName + " is not yet bound");
        }

        if (Utils.getItfType(clientItfName, owner).isFcCollectionItf()) {
            throw new IllegalBindingException(
                "In this implementation, for coherency reasons, it is not possible to unbind members of a collection interface");
        }
    }

    /**
     *
     * @param clientItfName
     *            the name of the client interface
     * @return a Binding object if single binding, List of Binding objects
     *         otherwise
     */
    public Object removeBinding(String clientItfName) {
        return bindings.remove(clientItfName);
    }

    /**
     *
     * @param clientItfName
     *            the name of the client interface
     * @return a Binding object if single binding, List of Binding objects
     *         otherwise
     */
    public Object getBinding(String clientItfName) {
        return bindings.get(clientItfName);
    }

    /**
     * see
     *
     * @link org.objectweb.fractal.api.control.BindingController#lookupFc(String)
     */
    public Object lookupFc(String clientItfName) throws NoSuchInterfaceException {
        if (isPrimitive()) {
            return ((BindingController) ((ProActiveComponent) getFcItfOwner()).getReferenceOnBaseObject())
                    .lookupFc(clientItfName);
        } else {
            if (!existsBinding(clientItfName)) {
                return null;
            } else {
                return ((Binding) getBinding(clientItfName)).getServerInterface();
            }
        }
    }

    /**
     * implementation of the interface BindingController see
     * {@link BindingController#bindFc(java.lang.String, java.lang.Object)}
     */
    public void bindFc(String clientItfName, Object serverItf) throws NoSuchInterfaceException,
            IllegalBindingException, IllegalLifeCycleException {
        // get value of (eventual) future before casting
        serverItf = PAFuture.getFutureValue(serverItf);

        ProActiveInterface sItf = (ProActiveInterface) serverItf;
        if (controllerLogger.isDebugEnabled()) {
            String serverComponentName;

            if (PAGroup.isGroup(serverItf)) {
                serverComponentName = "a group of components ";
            } else {
                serverComponentName = Fractal.getNameController((sItf).getFcItfOwner()).getFcName();
            }

            controllerLogger.debug("binding " + Fractal.getNameController(getFcItfOwner()).getFcName() + "." +
                clientItfName + " to " + serverComponentName + "." + (sItf).getFcItfName());
        }

        checkBindability(clientItfName, (Interface) serverItf);

        ((ItfStubObject) serverItf).setSenderItfID(new ItfID(clientItfName,
            ((ProActiveComponent) getFcItfOwner()).getID()));

        // if output interceptors are defined
        // TODO_M check with groups : interception is here done at the beginning
        // of the group invocation,
        // not for each element of the group
        List outputInterceptors = ((ProActiveComponentImpl) getFcItfOwner()).getOutputInterceptors();

        if (!outputInterceptors.isEmpty()) {
            try {
                // replace server itf with an interface of the same type+same proxy, but with interception code
                sItf = OutputInterceptorClassGenerator.instance().generateInterface(sItf, outputInterceptors);
            } catch (InterfaceGenerationFailedException e) {
                controllerLogger.error("could not generate output interceptor for client interface " +
                    clientItfName + " : " + e.getMessage());

                e.printStackTrace();
                throw new IllegalBindingException(
                    "could not generate output interceptor for client interface " + clientItfName + " : " +
                        e.getMessage());
            }
        }

        // Multicast bindings are handled here
        if (Utils.isMulticastItf(clientItfName, owner)) {
            if (Utils.isGathercastItf(sItf)) {
                //                Fractive.getMulticastController(owner)
                //                .bindFcMulticast(clientItfName, getGathercastAdaptor(clientItfName, serverItf, sItf));
                // no adaptor here
                ((MulticastControllerImpl) Fractive.getMulticastController(owner))
                        .bindFc(clientItfName, sItf);
                // add a callback ref in the server gather interface
                // TODO should throw a binding event
                Fractive.getGathercastController((sItf).getFcItfOwner()).addedBindingOnServerItf(
                        sItf.getFcItfName(), (owner).getRepresentativeOnThis(), clientItfName);
            } else {
                ((MulticastControllerImpl) Fractive.getMulticastController(owner))
                        .bindFc(clientItfName, sItf);
            }
            return;
        }

        if (isPrimitive()) {
            // binding operation is delegated
            ProActiveInterfaceType sItfType = ((ProActiveInterfaceType) sItf.getFcItfType());

            if (Utils.isGathercastItf(sItf)) {
                primitiveBindFc(clientItfName, getGathercastAdaptor(clientItfName, serverItf, sItf));
                // add a callback ref in the server gather interface
                // TODO should throw a binding event
                Fractive.getGathercastController((sItf).getFcItfOwner()).addedBindingOnServerItf(
                        sItf.getFcItfName(), (owner).getRepresentativeOnThis(), clientItfName);
            } else {
                primitiveBindFc(clientItfName, sItf);
            }
            return;
        }

        // composite or parallel
        InterfaceType client_itf_type;

        client_itf_type = Utils.getItfType(clientItfName, owner);

        if (isComposite()) {
            if (Utils.isGathercastItf(sItf)) {
                compositeBindFc(clientItfName, client_itf_type, getGathercastAdaptor(clientItfName,
                        serverItf, sItf));
                // add a callback ref in the server gather interface
                // TODO should throw a binding event
                Fractive.getGathercastController(sItf.getFcItfOwner()).addedBindingOnServerItf(
                        sItf.getFcItfName(), owner.getRepresentativeOnThis(), clientItfName);
            } else {
                compositeBindFc(clientItfName, client_itf_type, sItf);
            }
        }
    }

    private ProActiveInterface getGathercastAdaptor(String clientItfName, Object serverItf,
            ProActiveInterface sItf) throws NoSuchInterfaceException {
        // add an adaptor proxy for matching interface types
        Class<?> clientItfClass = null;
        try {
            InterfaceType[] cItfTypes = ((ComponentType) owner.getFcType()).getFcInterfaceTypes();
            for (int i = 0; i < cItfTypes.length; i++) {
                if (clientItfName.equals(cItfTypes[i].getFcItfName()) ||
                    (cItfTypes[i].isFcCollectionItf() && clientItfName
                            .startsWith(cItfTypes[i].getFcItfName()))) {
                    clientItfClass = Class.forName(cItfTypes[i].getFcItfSignature());
                }
            }
            if (clientItfClass == null) {
                throw new ProActiveRuntimeException("could not find type of client interface " +
                    clientItfName);
            }
        } catch (ClassNotFoundException e) {
            throw new ProActiveRuntimeException("cannot find client interface class for client interface : " +
                clientItfName);
        }
        ProActiveInterface itfProxy = (ProActiveInterface) Proxy.newProxyInstance(Thread.currentThread()
                .getContextClassLoader(), new Class<?>[] { ProActiveInterface.class, clientItfClass },
                new GatherItfAdapterProxy(serverItf));
        return itfProxy;
    }

    private void primitiveBindFc(String clientItfName, ProActiveInterface serverItf)
            throws NoSuchInterfaceException, IllegalBindingException, IllegalLifeCycleException {
        // delegate binding operation to the reified object
        BindingController user_binding_controller = (BindingController) ((ProActiveComponent) getFcItfOwner())
                .getReferenceOnBaseObject();

        // serverItf cannot be a Future (because it has to be casted) => make
        // sure if binding to a composite's internal interface
        serverItf = (ProActiveInterface) PAFuture.getFutureValue(serverItf);
        user_binding_controller.bindFc(clientItfName, serverItf);
        //        addBinding(new Binding(clientItf, clientItfName, serverItf));
    }

    /*
     * binding method enforcing Interface type for the server interface, for composite components
     */
    private void compositeBindFc(String clientItfName, InterfaceType clientItfType, Interface serverItf)
            throws NoSuchInterfaceException, IllegalBindingException, IllegalLifeCycleException {
        ProActiveInterface clientItf = null;
        clientItf = (ProActiveInterface) getFcItfOwner().getFcInterface(clientItfName);
        // TODO remove this as we should now use multicast interfaces for this purpose
        // if we have a collection interface, the impl object is actually a
        // group of references to interfaces
        // Thus we have to add the link to the new interface in this group
        // same for client interfaces of parallel components
        if (clientItfType.getFcItfName().equals(clientItfName)) {
            //            if ((isParallel() && !clientItfType.isFcClientItf())) {
            //                // collective binding, unnamed interface
            //                // TODO provide a default name?
            //                Group itf_group = ProActiveGroup.getGroup(clientItf.getFcItfImpl());
            //                itf_group.add(serverItf);
            //            } else {
            // single binding
            clientItf.setFcItfImpl(serverItf);
            //            }
        } else {
            if (Utils.getItfType(clientItfName, owner).isFcCollectionItf()) {
                clientItf.setFcItfImpl(serverItf);
            } else {
                //            if ((isParallel() && !clientItfType.isFcClientItf())) {
                //            		
                //                Group itf_group = ProActiveGroup.getGroup(clientItf.getFcItfImpl());
                //                itf_group.addNamedElement(clientItfName, serverItf);
                //            } else {
                throw new NoSuchInterfaceException("Cannot bind interface " + clientItfName +
                    " because it does not correspond to the specified type");
            }
        }
        //        }
        addBinding(new Binding(clientItf, clientItfName, serverItf));
    }

    /*
     * @see org.objectweb.fractal.api.control.BindingController#unbindFc(String)
     *
     * CAREFUL : unbinding action on collective interfaces will remove all the bindings to this
     * interface. This is also the case when removing bindings from the server interface of a
     * parallel component (yes you can do unbindFc(parallelServerItfName) !)
     */
    public void unbindFc(String clientItfName) throws NoSuchInterfaceException, IllegalBindingException,
            IllegalLifeCycleException {
        // remove from bindings and set impl object to null
        if (isPrimitive()) {
            // delegate to primitive component
            BindingController user_binding_controller = (BindingController) ((ProActiveComponent) getFcItfOwner())
                    .getReferenceOnBaseObject();
            if (Utils.isGathercastItf((Interface) user_binding_controller.lookupFc(clientItfName))) {
                ProActiveInterface sItf = (ProActiveInterface) user_binding_controller
                        .lookupFc(clientItfName);
                Fractive.getGathercastController(sItf.getFcItfOwner()).removedBindingOnServerItf(
                        sItf.getFcItfName(), (ProActiveComponent) sItf.getFcItfOwner(), clientItfName);
            }
            user_binding_controller.unbindFc(clientItfName);
        } else {
            checkUnbindability(clientItfName);
        }
        removeBinding(clientItfName);
    }

    /**
     * @see org.objectweb.fractal.api.control.BindingController#listFc() In case
     *      of a client collection interface, only the interfaces generated at
     *      runtime and members of the collection are returned (a reference on
     *      the collection interface itself is not returned, because it is just
     *      a typing artifact and does not exist at runtime).
     */
    public String[] listFc() {
        if (isPrimitive()) {
            return ((BindingController) ((ProActiveComponent) getFcItfOwner()).getReferenceOnBaseObject())
                    .listFc();
        }

        InterfaceType[] itfs_types = ((ComponentType) getFcItfOwner().getFcType()).getFcInterfaceTypes();
        List client_itfs_names = new ArrayList();

        for (int i = 0; i < itfs_types.length; i++) {
            if (itfs_types[i].isFcClientItf()) {
                if (itfs_types[i].isFcCollectionItf()) {
                    List collection_itfs = (List) bindings.get(itfs_types[i].getFcItfName());

                    if (collection_itfs != null) {
                        Iterator it = collection_itfs.iterator();

                        while (it.hasNext()) {
                            client_itfs_names.add(((Interface) it.next()).getFcItfName());
                        }
                    }
                } else {
                    client_itfs_names.add(itfs_types[i].getFcItfName());
                }
            }
        }

        return (String[]) client_itfs_names.toArray(new String[client_itfs_names.size()]);
    }

    protected boolean existsBinding(String clientItfName) throws NoSuchInterfaceException {
        if (isPrimitive() &&
            !(((ProActiveInterfaceType) ((ComponentType) owner.getFcType()).getFcInterfaceType(clientItfName))
                    .isFcMulticastItf())) {
            return (((BindingController) ((ProActiveComponent) getFcItfOwner()).getReferenceOnBaseObject())
                    .lookupFc(clientItfName) != null);
        } else {
            return bindings.containsBindingOn(clientItfName);
        }
    }

    protected void checkClientInterfaceName(String clientItfName) throws NoSuchInterfaceException {
        if (Utils.hasSingleCardinality(clientItfName, owner)) {
            return;
        }

        if (Utils.pertainsToACollectionInterface(clientItfName, owner) != null) {
            return;
        }

        if (Utils.isMulticastItf(clientItfName, owner)) {
            return;
        }

        throw new NoSuchInterfaceException(clientItfName +
            " does not correspond to a single nor a collective interface");
    }

    public Boolean isBound() {
        String[] client_itf_names = listFc();

        for (int i = 0; i < client_itf_names.length; i++) {
            try {
                if (existsBinding(client_itf_names[i])) {
                    return true;
                }
            } catch (NoSuchInterfaceException logged) {
                controllerLogger.error("cannot find interface " + client_itf_names[i] + " : " +
                    logged.getMessage());
            }
        }

        return new Boolean(false);
    }

    private Object[] filterServerItfs(Object[] itfs) {
        ArrayList<Object> newListItfs = new ArrayList<Object>();
        for (int i = 0; i < itfs.length; i++) {
            if (!((ProActiveInterfaceType) ((Interface) itfs[i]).getFcItfType()).isFcClientItf())
                newListItfs.add(itfs[i]);
        }
        return newListItfs.toArray();
    }

    public Boolean isBoundTo(Component component) {
        Object[] serverItfsComponent = filterServerItfs(component.getFcInterfaces());
        Object[] itfs = getFcItfOwner().getFcInterfaces();
        for (int i = 0; i < itfs.length; i++) {
            Interface curItf = (Interface) itfs[i];
            for (int j = 0; j < serverItfsComponent.length; j++) {
                Interface curServerItf = (Interface) serverItfsComponent[j];
                Binding binding = (Binding) getBinding(curItf.getFcItfName());
                if ((binding != null) &&
                    binding.getServerInterface().getFcItfOwner().equals(curServerItf.getFcItfOwner()) &&
                    binding.getServerInterface().getFcItfType().equals(curServerItf.getFcItfType())) {
                    return new Boolean(true);
                }
            }
        }
        return new Boolean(false);
    }
}
