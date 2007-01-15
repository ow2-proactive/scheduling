/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2006 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s):
 *
 * ################################################################
 */

/**
 * Task scheulder to be instantiated to connect to the scheduler and submit tasks
 *
 * @author walzouab
 *
 */
package org.objectweb.proactive.scheduler;

import java.util.Vector;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


public class TaskScheduler {
    //creates a logger
    private static Logger logger = ProActiveLogger.getLogger(Loggers.TASK_SCHEDULER);
    private Scheduler scheduler = null;

    public TaskScheduler() {
    }

    public void connectTo(String schedulerURL) throws Exception {
        this.scheduler = Scheduler.connectTo(schedulerURL);
        if (scheduler == null) {
            logger.error(
                "Couldnt connect to scheduler. Please make sure that the deamon is running and is accessible");
            throw new Exception("Scheduler doesnt exist");
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Connected to scheduler.");
        }
    }

    //gets nodes, creates the active object performs the computation and finally frees the resources
    public void submit(ProActiveTask task) throws Exception {
        int estimatedTime = 3;
        if (scheduler == null) {
            logger.error("Not connected to scheduler");
            throw new Exception(
                "Not connected to scheduler, please create a new one or connect to an existing one.");
        }

        Vector nodes = scheduler.getNodes(1, estimatedTime);

        Node node1 = (Node) nodes.get(0);
        String jobId = node1.getNodeInformation().getJobID();

        ActiveExecuter executer1 = (ActiveExecuter) ProActive.newActive(ActiveExecuter.class.getName(),
                new Object[] { task }, node1);

        executer1.start();

        scheduler.del(jobId);
    }
}
