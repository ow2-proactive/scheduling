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
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.ContentController;
import org.objectweb.fractal.api.control.IllegalContentException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.api.factory.InstantiationException;
import org.objectweb.fractal.api.type.TypeFactory;

import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.Fractive;
import org.objectweb.proactive.core.component.identity.ProActiveComponent;
import org.objectweb.proactive.core.component.type.ProActiveTypeFactory;
import org.objectweb.proactive.core.group.Group;
import org.objectweb.proactive.core.group.ProActiveGroup;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Implementation of ContentController (@see org.objectweb.fractal.api.control.ContentController).
 *
 * @author Matthieu Morel
 *
 */
public class ProActiveContentController extends ProActiveController
    implements ContentController, Serializable {
    protected static Logger logger = Logger.getLogger(ProActiveContentController.class.getName());
    List fcSubComponents;

    /**
     * Constructor for ProActiveContentController.
     */
    public ProActiveContentController(Component owner) {
        super(owner);
        try {
            setItfType(ProActiveTypeFactory.instance().createFcItfType(Constants.CONTENT_CONTROLLER,
                    ProActiveContentController.class.getName(),
                    TypeFactory.SERVER, TypeFactory.MANDATORY,
                    TypeFactory.SINGLE));
        } catch (InstantiationException e) {
            throw new ProActiveRuntimeException("cannot create controller " +
                this.getClass().getName());
        }
        fcSubComponents = new ArrayList();
    }

    /**
     * @see org.objectweb.fractal.api.control.ContentController#getFcInternalInterfaces()
     *
     * in this implementation, the external interfaces are also internal interfaces
     */
    public Object[] getFcInternalInterfaces() {
        logger.error(
            "Internal interfaces are only accessible from the stub, i.e. from outside of this component");
        return null;
    }

    /**
     * @see org.objectweb.fractal.api.control.ContentController#getFcInternalInterface(String)
     *
     *  in this implementation, the external interfaces are also internal interfaces
     *         */
    public Object getFcInternalInterface(String interfaceName)
        throws NoSuchInterfaceException {
        throw new NoSuchInterfaceException(
            "Internal interfaces are only accessible from the stub, i.e. from outside of this component");
    }

    /**
     * @see org.objectweb.fractal.api.control.ContentController#getFcSubComponents()
     */
    public Component[] getFcSubComponents() {
        if (fcSubComponents.size() > 0) {
            return (Component[]) fcSubComponents.toArray(new Component[fcSubComponents.size()]);
        } else {
            return null;
        }
    }

    public boolean isSubComponent(Component component) {
        // parse list and see if the given component has an equivalent
        Iterator it = fcSubComponents.iterator();
        ProActiveComponent current;
        Group group = null;
        if (ProActiveGroup.isGroup(component)) {
            group = ProActiveGroup.getGroup(component);
        }
        while (it.hasNext()) {
            current = (ProActiveComponent) it.next();
            if (current.equals(component)) {
                return true;
            }
            if ((group != null) && ProActiveGroup.isGroup(current)) {
                if (ProActiveGroup.getGroup(current).equals(group)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * @see org.objectweb.fractal.api.control.ContentController#addFcSubComponent(Component)
     */
    public void addFcSubComponent(Component subComponent)
        throws IllegalLifeCycleException, IllegalContentException {
        checkLifeCycleIsStopped();

        // check whether the subComponent is the component itself
        if (getFcItfOwner().equals(subComponent)) {
            try {
                throw new IllegalArgumentException("cannot add " +
                    Fractive.getComponentParametersController(getFcItfOwner())
                            .getComponentParameters().getName() +
                    " component into itself ");
            } catch (NoSuchInterfaceException e) {
                logger.error(e.getMessage());
            }
        }

        // check whether already a sub component
        // TODO check in the case of multiple references to the same component
        if (fcSubComponents.contains(subComponent)) {
            String name;
            try {
                name = ((ComponentParametersController) subComponent
                        .getFcInterface(Constants.COMPONENT_PARAMETERS_CONTROLLER)).getComponentParameters()
                        .getName();
            } catch (NoSuchInterfaceException nsie) {
                throw new ProActiveRuntimeException(
                    "cannot access the component parameters controller");
            }
            throw new IllegalArgumentException("already a sub component : " +
                name);
        }
        fcSubComponents.add(subComponent);
        // add a ref on the current component
        try {
            Object itf = subComponent.getFcInterface(Constants.SUPER_CONTROLLER);

            ((ProActiveSuperController) itf).addParent(((ProActiveComponent) getFcItfOwner()).getRepresentativeOnThis());
        } catch (NoSuchInterfaceException e) {
            throw new IllegalContentException(
                "Cannot add component : cannot find super-controller interface.");
        }

        // FIXME : component cycle checking
        // this should raise an exception if the subcomponent is primitive,
        // but proactive remote calls don't relay exception ; they stay in the body and lead to its termination
        //		// check whether current component is contained in subComponent
        //		Component[] subComponent_sub_components =
        //			((ContentController) subComponent.getFcInterface(ContentController.CONTENT_CONTROLLER))
        //				.getFcSubComponents();
        //		for (int i = 0; i < subComponent_sub_components.length; i++) {
        //			if (getFcItfOwner().equals(subComponent_sub_components[i])) {
        //				throw new IllegalArgumentException(
        //					"cannot add "
        //						+ ((ProActiveComponent) subComponent).getComponentParameters().getName()
        //						+ " ; \nthis operation would create a cycle in the component hierarchy");
        //			}
        //		}
        //		int length =
        //			(fcSubComponents == null) ? 0 : fcSubComponents.length;
        //			Component[] oldSubComponents = fcSubComponents;
        //			Component[] subComponents = new Component[length + 1];
        //			if (fcSubComponents != null) {
        //			System.arraycopy(fcSubComponents, 0, subComponents, 0, length); }
        //		subComponents[length] = subComponent;
        //			fcSubComponents = subComponents;
    }

    /**
     * @see org.objectweb.fractal.api.control.ContentController#removeFcSubComponent(Component)
     */
    public void removeFcSubComponent(Component subComponent)
        throws IllegalLifeCycleException, IllegalContentException {
        checkLifeCycleIsStopped();
        if (!fcSubComponents.remove(subComponent)) {
            throw new IllegalArgumentException("not a sub component : " +
                subComponent);
        }
        try {
            ((ProActiveSuperController) subComponent.getFcInterface(Constants.SUPER_CONTROLLER)).removeParent(subComponent);
        } catch (NoSuchInterfaceException e) {
            throw new IllegalContentException(
                "Cannot remove component : cannot find super-controller interface");
        }

        if (logger.isDebugEnabled()) {
            logger.debug("TODO : check the bindings");
        }
    }
}
