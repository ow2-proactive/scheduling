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
package performanceTests.simple;

import org.objectweb.proactive.api.ProActiveObject;


/**
 *
 * @author vbodnart
 */
public class Caller implements java.io.Serializable {
    public static final int NB_ITERATIONS = 15;
    private Callee calleeReference;

    public Caller() {
    }

    public Caller(Callee calleeReference) {
        this.calleeReference = calleeReference;
    }

    /**
     * Synchronous
     * @return Always true to force syncronous call
     */
    public boolean performTest() {
        for (int i = 0; i < NB_ITERATIONS; i++) {
            // perform future async call in order to have all basic timers used
            int[] arr = this.calleeReference.futureAsyncCall2(new int[15000]);
            arr.hashCode();
        }
        return true;
    }

    /**
     * Since an instance of this class could be active this method
     * should be called to terminate its activity.
     */
    public boolean kill() {
        ProActiveObject.terminateActiveObject(true);
        return true;
    }
}
