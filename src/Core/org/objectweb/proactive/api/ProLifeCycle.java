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
package org.objectweb.proactive.api;

import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.core.gc.HalfBodies;


@PublicAPI
public class ProLifeCycle {

    /**
     * Inform the ProActive DGC that all non active threads will not use
     * anymore their references to active objects. This is needed when the
     * local GC does not reclaim stubs quickly enough.
     */
    public static void userThreadTerminated() {
        HalfBodies.end();
    }

    /**
     * Call this method at the end of the application if it completed
     * successfully, for the launcher to be aware of it.
     */
    public static void exitSuccess() {
        System.exit(0);
    }

    /**
     * Call this method at the end of the application if it did not complete
     * successfully, for the launcher to be aware of it.
     */
    public static void exitFailure() {
        System.exit(1);
    }
}
