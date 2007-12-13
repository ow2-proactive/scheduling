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

import java.util.Hashtable;
import java.util.Map;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.factory.InstantiationException;
import org.objectweb.fractal.api.type.TypeFactory;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.Utils;
import org.objectweb.proactive.core.component.type.ProActiveTypeFactoryImpl;


/**
 * @author Cedric Dalmasso
 *
 */
public class PriorityControllerImpl extends AbstractProActiveController implements PriorityController {
    private static final String ANY_PARAMETERS = "any-parameters";
    private Map<String, Object> nf2s;
    private Map<String, Object> nf3s;

    public PriorityControllerImpl(Component owner) {
        super(owner);
        nf2s = new Hashtable<String, Object>(2);
        nf2s.put("setPriorityNF2", ANY_PARAMETERS);
        nf3s = new Hashtable<String, Object>(3);
        nf3s.put("setPriorityNF3", ANY_PARAMETERS);
    }

    @Override
    protected void setControllerItfType() {
        try {
            setItfType(ProActiveTypeFactoryImpl.instance().createFcItfType(
                    Constants.REQUEST_PRIORITY_CONTROLLER, PriorityController.class.getName(),
                    TypeFactory.SERVER, TypeFactory.MANDATORY, TypeFactory.SINGLE));
        } catch (InstantiationException e) {
            throw new ProActiveRuntimeException("cannot create controller " + this.getClass().getName());
        }
    }

    ///////////////////////////////////////
    // PriorityController IMPLEMENTATION //
    ///////////////////////////////////////
    // TODO_C for a NF? priority check that the method is in a controller
    public void setPriority(String interfaceName, String methodName, RequestPriority priority) {
        switch (priority) {
            case NF1:
                nf2s.remove(methodName);
                nf3s.remove(methodName);
                break;
            case NF2:
                nf3s.remove(methodName);
                nf2s.put(methodName, ANY_PARAMETERS);
                break;
            case NF3:
                nf2s.remove(methodName);
                nf3s.put(methodName, ANY_PARAMETERS);
                break;
            default:
                break;
        }
    }

    // TODO_C for a NF? priority check that the method is in a controller
    public void setPriority(String interfaceName, String methodName, Class<?>[] parametersTypes,
            RequestPriority priority) {
        switch (priority) {
            case NF1:
                nf2s.remove(methodName);
                nf3s.remove(methodName);
                break;
            case NF2:
                nf3s.remove(methodName);
                nf2s.put(methodName, parametersTypes);
                break;
            case NF3:
                nf2s.remove(methodName);
                nf3s.put(methodName, parametersTypes);
                break;
            default:
                break;
        }
    }

    public RequestPriority getPriority(String interfaceName, String methodName, Class<?>[] parametersTypes) {
        if (nf2s.get(methodName) != null) {
            return RequestPriority.NF2;
        } else if (nf3s.get(methodName) != null) {
            return RequestPriority.NF3;
        } else if (Utils.isControllerInterfaceName(interfaceName)) {
            return RequestPriority.NF1;
        } else {
            return RequestPriority.F;
        }
    }
}
