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
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.Interface;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.ContentController;
import org.objectweb.fractal.api.control.IllegalContentException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.api.factory.InstantiationException;
import org.objectweb.fractal.api.type.TypeFactory;
import org.objectweb.fractal.util.Fractal;
import org.objectweb.proactive.api.PAGroup;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.ProActiveInterface;
import org.objectweb.proactive.core.component.exceptions.ContentControllerExceptionListException;
import org.objectweb.proactive.core.component.identity.ProActiveComponent;
import org.objectweb.proactive.core.component.type.ProActiveInterfaceType;
import org.objectweb.proactive.core.component.type.ProActiveTypeFactoryImpl;
import org.objectweb.proactive.core.group.Group;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * Implementation of {@link org.objectweb.fractal.api.control.ContentController content controller}
 *
 * @author Matthieu Morel
 *
 */
public class ProActiveContentControllerImpl extends AbstractProActiveController implements
        ProActiveContentController, Serializable {
    protected static Logger logger = ProActiveLogger.getLogger(Loggers.COMPONENTS);
    protected List<Component> fcSubComponents;
    protected Map<Component, IllegalContentException> contentExceptions = new Hashtable<Component, IllegalContentException>();
    protected Map<Component, IllegalLifeCycleException> lifeCycleExceptions = new Hashtable<Component, IllegalLifeCycleException>();

    /**
     * Constructor for ProActiveContentController.
     */
    public ProActiveContentControllerImpl(Component owner) {
        super(owner);
        fcSubComponents = new ArrayList<Component>();
    }

    @Override
    protected void setControllerItfType() {
        try {
            setItfType(ProActiveTypeFactoryImpl.instance().createFcItfType(Constants.CONTENT_CONTROLLER,
                    ProActiveContentController.class.getName(), TypeFactory.SERVER, TypeFactory.MANDATORY,
                    TypeFactory.SINGLE));
        } catch (InstantiationException e) {
            throw new ProActiveRuntimeException("cannot create controller " + this.getClass().getName());
        }
    }

    /*
     * @see org.objectweb.fractal.api.control.ContentController#getFcInternalInterfaces()
     *
     * in this implementation, the external interfaces are also internal interfaces
     */
    public Object[] getFcInternalInterfaces() {
        //        logger.error(
        //            "Internal interfaces are only accessible from the stub, i.e. from outside of this component");
        Object[] itfs = ((ProActiveComponent) getFcItfOwner()).getRepresentativeOnThis().getFcInterfaces();
        ArrayList<Object> internalItf = new ArrayList<Object>();
        for (Object itf : itfs) {
            String itfName = ((Interface) itf).getFcItfName();
            if (itfName.startsWith("component") || (itfName.indexOf("-controller") != -1)) {
                continue;
            }

            //((ProActiveInterface)itf).setFcIsInternal(true);
            boolean isClient = ((ProActiveInterfaceType) ((Interface) itf).getFcItfType()).isFcClientItf();
            ((ProActiveInterfaceType) ((Interface) itf).getFcItfType()).setIsClient(!isClient);

            internalItf.add(itf);
        }

        return internalItf.toArray(new Object[internalItf.size()]);
    }

    /*
     * @see org.objectweb.fractal.api.control.ContentController#getFcInternalInterface(String)
     *
     *  in this implementation, the external interfaces are also internal interfaces
     *         */
    public Object getFcInternalInterface(String interfaceName) throws NoSuchInterfaceException {
        ProActiveInterface itf = (ProActiveInterface) ((ProActiveComponent) getFcItfOwner())
                .getRepresentativeOnThis().getFcInterface(interfaceName);

        ////    	((ProActiveInterface)itf).setFcIsInternal(true);
        //        boolean isClient = ((ProActiveInterfaceType)itf.getFcItfType()).isFcClientItf();
        //        ((ProActiveInterfaceType)((Interface) itf).getFcItfType()).setIsClient(!isClient);
        return itf;
    }

    /*
     * @see org.objectweb.fractal.api.control.ContentController#getFcSubComponents()
     */
    public Component[] getFcSubComponents() {
        return fcSubComponents.toArray(new Component[fcSubComponents.size()]);
    }

    public boolean isSubComponent(Component component) {
        if (PAGroup.isGroup(component)) {
            Group<Component> group = PAGroup.getGroup(component);
            for (Iterator<Component> it = group.iterator(); it.hasNext();) {
                if (!fcSubComponents.contains(it.next())) {
                    return false;
                }
            }
        } else if (!fcSubComponents.contains(component)) {
            return false;
        }

        return true;
    }

    /*
     * @see org.objectweb.fractal.api.control.ContentController#addFcSubComponent(Component)
     *
     * if subComponent is a group, each element of the group is added as a subcomponent
     */
    public void addFcSubComponent(Component subComponent) throws IllegalLifeCycleException,
            IllegalContentException {
        checkLifeCycleIsStopped();
        // no sharing in the current implementation of Fractal
        // => only one parent for a given component
        // FIXME control requests are enqueued
        // pb is that the subComponent might not be stopped
        try {
            // could not do this invocation on a group (non reifiable return type)
            if (PAGroup.isGroup(subComponent)) {
                try {
                    addFcSubComponent(PAGroup.getGroup(subComponent));
                } catch (ContentControllerExceptionListException e) {
                    e.printStackTrace();
                    throw new IllegalContentException("problem adding a list of component to a composite : " +
                        e.getMessage());
                }
                return;
            }
            if (Fractal.getSuperController(subComponent).getFcSuperComponents().length != 0) {
                throw new IllegalContentException(
                    "This implementation of the Fractal model does not currently allow sharing : " +
                        Fractal.getNameController(subComponent).getFcName() + " has no super controller");
            }
        } catch (NoSuchInterfaceException e) {
            logger
                    .error("could not check that the subcomponent is not shared, continuing ignoring this verification ... " +
                        e);
        }

        ProActiveComponent this_component = ((ProActiveComponent) getFcItfOwner());
        Component ref_on_this_component = this_component.getRepresentativeOnThis();

        // check whether the subComponent is the component itself
        if (ref_on_this_component.equals(subComponent)) {
            try {
                throw new IllegalArgumentException("cannot add " +
                    Fractal.getNameController(getFcItfOwner()).getFcName() + " component into itself ");
            } catch (NoSuchInterfaceException e) {
                logger.error(e.getMessage());
            }
        }

        // check whether already a sub component
        if (getAllSubComponents(this_component).contains(ref_on_this_component)) {
            String name;
            try {
                name = Fractal.getNameController(subComponent).getFcName();
            } catch (NoSuchInterfaceException nsie) {
                throw new ProActiveRuntimeException("cannot access the component parameters controller", nsie);
            }
            throw new IllegalArgumentException("already a sub component : " + name);
        }

        fcSubComponents.add(subComponent);
        // add a ref on the current component
        try {
            Object itf = subComponent.getFcInterface(Constants.SUPER_CONTROLLER);

            ((ProActiveSuperController) itf).addParent(ref_on_this_component);
        } catch (NoSuchInterfaceException e) {
            throw new IllegalContentException(
                "Cannot add component : cannot find super-controller interface.");
        }
    }

    /*
     * @see org.objectweb.fractal.api.control.ContentController#removeFcSubComponent(Component)
     */
    public void removeFcSubComponent(Component subComponent) throws IllegalLifeCycleException,
            IllegalContentException {
        checkLifeCycleIsStopped();
        try {
            if (((ProActiveBindingController) Fractal.getBindingController(getFcItfOwner())).isBound()
                    .booleanValue()) {
                throw new IllegalContentException(
                    "cannot remove a sub component that holds bindings on its external client interfaces");
            }
        } catch (NoSuchInterfaceException ignored) {
            // no binding controller
        }
        if (!fcSubComponents.remove(subComponent)) {
            throw new IllegalContentException("not a sub component : " + subComponent);
        }
        try {
            ((ProActiveSuperController) Fractal.getSuperController(subComponent)).removeParent(subComponent);
        } catch (NoSuchInterfaceException e) {
            fcSubComponents.add(subComponent);
            throw new IllegalContentException(
                "cannot remove component : cannot find super-controller interface");
        }
    }

    /*
     * Returns all the direct and indirect sub components of the given component.
     * @param component a component.
     * @return all the direct and indirect sub components of the given component.
     */
    private List<Component> getAllSubComponents(final Component component) {
        List<Component> allSubComponents = new ArrayList<Component>();
        List<Component> stack = new ArrayList<Component>();

        // first layer of sub components retreived directly (do not go through the representative)
        Component[] subComponents = getFcSubComponents();

        for (int i = subComponents.length - 1; i >= 0; --i) {
            stack.add(subComponents[i]);
        }
        while (stack.size() > 0) {
            int index = stack.size() - 1;
            Component c = stack.get(index);
            stack.remove(index);

            if (!allSubComponents.contains(c)) {
                try {
                    ContentController cc = (ContentController) c.getFcInterface(Constants.CONTENT_CONTROLLER);
                    subComponents = cc.getFcSubComponents();
                    for (int i = subComponents.length - 1; i >= 0; --i) {
                        stack.add(subComponents[i]);
                    }
                } catch (NoSuchInterfaceException ignored) {
                    // c is not a composite component: nothing to do
                }
                allSubComponents.add(c);
            }
        }
        return allSubComponents;
    }

    // TODO factorize code
    public void addFcSubComponent(List<Component> subComponents)
            throws ContentControllerExceptionListException {
        lifeCycleExceptions.clear();
        contentExceptions.clear();

        ExecutorService threadPool = Executors.newCachedThreadPool();
        ContentControllerExceptionListException e = new ContentControllerExceptionListException();
        for (Iterator<Component> iter = subComponents.iterator(); iter.hasNext();) {
            Component element = iter.next();
            AddSubComponentTask task = new AddSubComponentTask(e, this, element);
            threadPool.execute(task);
        }
        threadPool.shutdown();
        if (!e.isEmpty()) {
            throw e;
        }
    }

    public void removeFcSubComponent(List<Component> subComponents)
            throws ContentControllerExceptionListException {
        lifeCycleExceptions.clear();
        contentExceptions.clear();

        ExecutorService threadPool = Executors.newCachedThreadPool();
        ContentControllerExceptionListException e = new ContentControllerExceptionListException();
        for (Iterator<Component> iter = subComponents.iterator(); iter.hasNext();) {
            Component element = iter.next();
            RemoveSubComponentTask task = new RemoveSubComponentTask(e, this, element);
            threadPool.execute(task);
        }
        threadPool.shutdown();
        if (!e.isEmpty()) {
            throw e;
        }
    }

    private static class AddSubComponentTask implements Runnable {
        ContentControllerExceptionListException exceptions;
        ProActiveContentControllerImpl controller;
        Component component;

        public AddSubComponentTask(ContentControllerExceptionListException exceptions,
                ProActiveContentControllerImpl controller, Component component) {
            this.exceptions = exceptions;
            this.controller = controller;
            this.component = component;
        }

        public void run() {
            try {
                controller.addFcSubComponent(component);
            } catch (IllegalContentException e) {
                e.printStackTrace();
                exceptions.addIllegalContentException(component, e);
            } catch (IllegalLifeCycleException e) {
                e.printStackTrace();
                exceptions.addIllegalLifeCycleException(component, e);
            }
        }
    }

    private static class RemoveSubComponentTask implements Runnable {
        ContentControllerExceptionListException exceptions;
        ProActiveContentControllerImpl controller;
        Component component;

        public RemoveSubComponentTask(ContentControllerExceptionListException exceptions,
                ProActiveContentControllerImpl controller, Component component) {
            this.exceptions = exceptions;
            this.controller = controller;
            this.component = component;
        }

        public void run() {
            try {
                controller.removeFcSubComponent(component);
            } catch (IllegalContentException e) {
                e.printStackTrace();
                exceptions.addIllegalContentException(component, e);
            } catch (IllegalLifeCycleException e) {
                e.printStackTrace();
                exceptions.addIllegalLifeCycleException(component, e);
            }
        }
    }
}
