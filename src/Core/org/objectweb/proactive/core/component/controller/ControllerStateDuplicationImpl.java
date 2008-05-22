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

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.factory.InstantiationException;
import org.objectweb.fractal.api.type.TypeFactory;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.identity.ProActiveComponent;
import org.objectweb.proactive.core.component.type.ProActiveTypeFactoryImpl;


/**
 *The controller providing means to retrieve and set the state of a component controller 
 * @author The ProActive Team
 *
 */
public class ControllerStateDuplicationImpl extends AbstractProActiveController implements
        ControllerStateDuplication {

    public ControllerStateDuplicationImpl(Component owner) {
        super(owner);

    }

    @Override
    protected void setControllerItfType() {
        try {
            setItfType(ProActiveTypeFactoryImpl.instance().createFcItfType(
                    Constants.CONTROLLER_STATE_DUPLICATION, ControllerStateDuplication.class.getName(),
                    TypeFactory.SERVER, TypeFactory.MANDATORY, TypeFactory.SINGLE));
        } catch (InstantiationException e) {
            throw new ProActiveRuntimeException("cannot create controller type : " +
                this.getClass().getName());
        }
    }

    public void duplicateController(Object c) {
        ControllerStateDuplication reified_Object = (ControllerStateDuplication) ((ProActiveComponent) getFcItfOwner())
                .getReferenceOnBaseObject();
        reified_Object.duplicateController(c);

    }

    public ControllerState getState() {
        ControllerStateDuplication reified_Object = (ControllerStateDuplication) ((ProActiveComponent) getFcItfOwner())
                .getReferenceOnBaseObject();
        return reified_Object.getState();

    }

}
