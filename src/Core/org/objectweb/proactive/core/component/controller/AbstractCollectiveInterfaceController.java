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

import java.lang.reflect.Method;

import org.objectweb.fractal.api.Component;
import org.objectweb.proactive.core.component.ProActiveInterface;


/**
 * Abstract parent class for controllers of collective interfaces
 *
 * @author The ProActive Team
 *
 */
public abstract class AbstractCollectiveInterfaceController extends AbstractProActiveController {

    /**
     * called after creation of all controllers and interfaces
     */
    @Override
    public abstract void init();

    public AbstractCollectiveInterfaceController(Component owner) {
        super(owner);
        // TODO Auto-generated constructor stub
    }

    //    
    protected abstract Method searchMatchingMethod(Method clientSideMethod, Method[] serverSideMethods,
            boolean clientItfIsMulticast, boolean serverItfIsGathercast, ProActiveInterface serverSideItf);
}
