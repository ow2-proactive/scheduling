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

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.factory.InstantiationException;
import org.objectweb.fractal.api.type.TypeFactory;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.type.ProActiveTypeFactoryImpl;


/**
 * An implementation of the {@link org.objectweb.fractal.api.control.SuperController} interface.
 *
 * @author The ProActive Team
 */
public class ProActiveSuperControllerImpl extends AbstractProActiveController implements Serializable,
        ProActiveSuperController, ControllerStateDuplication {
    public ProActiveSuperControllerImpl(Component owner) {
        super(owner);
    }

    @Override
    protected void setControllerItfType() {
        try {
            setItfType(ProActiveTypeFactoryImpl.instance().createFcItfType(Constants.SUPER_CONTROLLER,
                    ProActiveSuperController.class.getName(), TypeFactory.SERVER, TypeFactory.MANDATORY,
                    TypeFactory.SINGLE));
        } catch (InstantiationException e) {
            throw new ProActiveRuntimeException("cannot create controller " + this.getClass().getName());
        }
    }

    // the following is borrowed from the Julia implementation
    public Component[] fcParents;

    public Component[] getFcSuperComponents() {
        if (fcParents == null) {
            return new Component[0];
        } else {
            return fcParents;
        }
    }

    public void addParent(final Component parent) {
        int length = (fcParents == null) ? 1 : (fcParents.length + 1);
        Component[] parents = new Component[length];
        if (fcParents != null) {
            System.arraycopy(fcParents, 0, parents, 1, fcParents.length);
        }
        parents[0] = parent;
        fcParents = parents;
    }

    public void removeParent(final Component parent) {
        int length = fcParents.length - 1;
        if (length == 0) {
            fcParents = null;
        } else {
            Component[] parents = new Component[length];
            int i = 0;
            for (int j = 0; j < fcParents.length; ++j) {
                if (!fcParents[j].equals(parent)) {
                    parents[i++] = fcParents[j];
                }
            }
            fcParents = parents;
        }
    }

    public void duplicateController(Object c) {
        if (c instanceof SuperControllerState) {
            fcParents = ((SuperControllerState) c).getParents();
        } else {
            throw new ProActiveRuntimeException(
                "ProActiveSuperControllerImpl : Impossible to duplicate the controller " + this +
                    " from the controller" + c);
        }
    }

    public ControllerState getState() {
        return new ControllerState(new SuperControllerState(fcParents));
    }

    class SuperControllerState implements Serializable {
        private Component[] parents;

        public SuperControllerState(Component[] parents) {

            this.parents = parents;
        }

        public Component[] getParents() {
            return parents;
        }

        public void setParents(Component[] parents) {
            this.parents = parents;
        }
    }
}
