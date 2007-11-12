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
package org.objectweb.proactive.extra.masterworker.interfaces.internal;

import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;


/**
 * <i><font size="-1" color="#FF0000">**For internal use only** </font></i><br>
 * The interface of the worker objects <br/>
 * A worker is connected to a TaskProvider (i.e. Master) which will provide tasks to execute <br/>
 * @author fviale
 *
 */
public interface Worker {

    /**
     * Returns the name of this worker
     * @return name of the worker
     */
    String getName();

    /**
     * tells that this worker is alive
     */
    void heartBeat();

    /**
     * terminates this worker
     * @return true if the object terminated successfully
     */
    BooleanWrapper terminate();

    /**
     * Asks the worker to wake up
     */
    void wakeup();
}
