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
import org.objectweb.fractal.api.control.NameController;
import org.objectweb.fractal.api.factory.InstantiationException;
import org.objectweb.fractal.api.type.TypeFactory;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.type.ProActiveTypeFactoryImpl;


/**
 * Implementation of the {@link org.objectweb.fractal.api.control.NameController}
 *
 * @author Matthieu Morel
 *
 */
public class ProActiveNameController extends AbstractProActiveController implements NameController {
    // FIXME coherency between this value and the one in component parameters controller
    String name;

    /**
     * @param owner
     */
    public ProActiveNameController(Component owner) {
        super(owner);
    }

    @Override
    protected void setControllerItfType() {
        try {
            setItfType(ProActiveTypeFactoryImpl.instance().createFcItfType(Constants.NAME_CONTROLLER,
                    NameController.class.getName(), TypeFactory.SERVER, TypeFactory.MANDATORY,
                    TypeFactory.SINGLE));
        } catch (InstantiationException e) {
            throw new ProActiveRuntimeException("cannot create controller " + this.getClass().getName(), e);
        }
    }

    /*
     * @see org.objectweb.fractal.api.control.NameController#getFcName()
     */
    public String getFcName() {
        return name;
    }

    /*
     * @see org.objectweb.fractal.api.control.NameController#setFcName(java.lang.String)
     */
    public void setFcName(String name) {
        this.name = name;
    }
}
