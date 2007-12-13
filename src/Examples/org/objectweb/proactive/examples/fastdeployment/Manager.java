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
package org.objectweb.proactive.examples.fastdeployment;

import java.io.Serializable;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.core.util.ProActiveRandom;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.util.wrapper.IntWrapper;
import org.objectweb.proactive.core.util.wrapper.LongWrapper;


public class Manager implements Serializable, InitActive, RunActive {

    /** The application logger */
    final static private Logger logger = ProActiveLogger.getLogger(Loggers.CORE + ".app");
    /** List of slaves already in the computation */
    HashMap<Integer, CPUBurner> slaves;
    final private int ITERATIONS = 3000;
    private int iteration;

    public Manager() {
        // No-args constructor
    }

    public void initActivity(Body body) {
        slaves = new HashMap<Integer, CPUBurner>();
        iteration = 0;
    }

    /**
     * Default FIFO policy.
     *
     * nodeAvailable or resultAvailable can be prioritized by changing the
     * runActivity.
     */
    public void runActivity(Body body) {
        Service service = new Service(body);
        while (body.isActive()) {
            service.blockingServeOldest();
        }
    }

    /**         Used to print some statistics about deployment speed */
    private long firstNodeCreatedTime = -1;

    /**
     * This method is called by the VNActivator when a new slave is ready to enter
     * in the computation. To maximize the deployed speed remote calls should be avoided in
     * this method. That's why the slaveID is given by the VNActivator and not asked to the slave
     * (even if the slave.getId method has @Cache annotation the first call is remote)
     *
     * @param slaveID ID of the slave
     * @param slave the slave
     */
    public void nodeAvailable(IntWrapper slaveID, CPUBurner slave) {
        if (firstNodeCreatedTime == -1) {
            firstNodeCreatedTime = System.currentTimeMillis();
        }

        slaves.put(slaveID.intValue(), slave);

        /* Deployment statistics */
        if ((slaves.size() % 50) == 0) {
            long elapsed = System.currentTimeMillis() - firstNodeCreatedTime;
            double throughput = (1000.0 * slaves.size()) / elapsed;
            logger.info(" Slaves already in the computation:  " + slaves.size() + " in " + elapsed +
                " ms. => " + throughput);
        }

        /* CHANGEME: Assign a task to the slave */
        slave.compute(new LongWrapper(1L * ProActiveRandom.nextInt(60) * Integer.MAX_VALUE));
    }

    /**
     * A new result is available
     *
     * When a slave finish the computation of its current task, it sends the result
     * to the manager.
     * @param result
     */
    public void resultAvailable(Result result) {
        int id = result.getSlaveID();

        logger.info("Got a result from slave #" + id);

        // CHANGEME Resubmit a task to the slave if needed
        if (iteration < ITERATIONS) {
            slaves.get(id).compute(new LongWrapper(1L * ProActiveRandom.nextInt(60) * Integer.MAX_VALUE));
            iteration++;
        }
    }
}
