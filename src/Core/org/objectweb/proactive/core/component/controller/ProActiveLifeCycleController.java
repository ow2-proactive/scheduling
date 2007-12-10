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

import org.objectweb.fractal.api.control.LifeCycleController;
import org.objectweb.proactive.annotation.PublicAPI;


/**
 * This interface defines an extension of the @see org.objectweb.fractal.api.control.LifeCycleController, which
 * is able to handle prioritized requests.
 *<p>
 * (Under development)
 * </p>
 *
 * @see org.objectweb.fractal.api.control.LifeCycleController
 *
 * @author Matthieu Morel
 *
 */
@PublicAPI
public interface ProActiveLifeCycleController extends LifeCycleController {

    /**
     * @see org.objectweb.fractal.api.control.LifeCycleController#getFcState()
     */
    public String getFcState(short priority);

    /**
     * @see org.objectweb.fractal.api.control.LifeCycleController#startFc()
     */
    public void startFc(short priority);

    /**
     * @see org.objectweb.fractal.api.control.LifeCycleController#stopFc()
     */
    public void stopFc(short priority);
}
