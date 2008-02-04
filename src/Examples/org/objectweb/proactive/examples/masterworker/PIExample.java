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
package org.objectweb.proactive.examples.masterworker;

import java.util.List;
import java.util.Vector;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.extensions.masterworker.ProActiveMaster;
import org.objectweb.proactive.extensions.masterworker.TaskAlreadySubmittedException;
import org.objectweb.proactive.extensions.masterworker.TaskException;
import org.objectweb.proactive.extensions.masterworker.interfaces.Task;
import org.objectweb.proactive.extensions.masterworker.interfaces.WorkerMemory;


public class PIExample {
    public static final long NUMBER_OF_EXPERIENCES = 1000000;
    public static final int NUMBER_OF_TASKS = 30;

    public static void main(String[] args) throws TaskAlreadySubmittedException, TaskException,
            ProActiveException {
        // creation of the master
        ProActiveMaster<ComputePIMonteCarlo, Long> master = new ProActiveMaster<ComputePIMonteCarlo, Long>();

        // adding resources
        master.addResources(PIExample.class
                .getResource("/org/objectweb/proactive/examples/masterworker/WorkersLocal.xml"));

        // defining tasks
        Vector<ComputePIMonteCarlo> tasks = new Vector<ComputePIMonteCarlo>();
        for (int i = 0; i < NUMBER_OF_TASKS; i++) {
            tasks.add(new ComputePIMonteCarlo());
        }

        // adding tasks to the queue
        master.solve(tasks);

        // waiting for results
        List<Long> successesList = master.waitAllResults();

        // computing PI using the results
        long sumSuccesses = 0;

        for (long successes : successesList) {
            sumSuccesses += successes;
        }

        double pi = (4 * sumSuccesses) / ((double) NUMBER_OF_EXPERIENCES * NUMBER_OF_TASKS);

        System.out.println("Computed PI by Monte-Carlo method : " + pi);

        master.terminate(true);
    }

    /**
     * Task which creates randomly a set of points belonging to the [0, 1[x[0, 1[ interval<br>
     * and tests how many points are inside the uniter circle.
     * @author fviale
     *
     */
    public static class ComputePIMonteCarlo implements Task<Long> {

        /**
         *
         */
        public ComputePIMonteCarlo() {
        }

        public Long run(WorkerMemory memory) throws Exception {
            long remaining = NUMBER_OF_EXPERIENCES;
            long successes = 0;
            while (remaining > 0) {
                remaining--;
                if (experience()) {
                    successes++;
                }
            }
            return successes;
        }

        public boolean experience() {
            double x = Math.random();
            double y = Math.random();
            return Math.hypot(x, y) < 1;
        }
    }
}
