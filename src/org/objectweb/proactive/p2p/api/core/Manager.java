/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive-support@inria.fr
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
package org.objectweb.proactive.p2p.api.core;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.group.ExceptionListException;
import org.objectweb.proactive.core.group.Group;
import org.objectweb.proactive.core.group.ProActiveGroup;
import org.objectweb.proactive.core.mop.ClassNotReifiableException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.core.util.wrapper.IntWrapper;


/**
 * @author Alexandre di Costanzo
 *
 * Created on May 31, 2005
 */
public class Manager implements Serializable, InitActive {
    private static Logger logger = ProActiveLogger.getLogger(Loggers.P2P_SKELETONS_MANAGER);
    private Task rootTask = null;
    private Node[] nodes = null;
    private Worker workerGroup;
    private TaskProvider taskProvider;
    private boolean isComputing = false;
    private Vector futureTaskList = new Vector();
    private Vector pendingTaskList = new Vector();
    private Vector workingWorkerList = new Vector();
    private Vector freeWorkerList = new Vector();
    private Vector allResults = new Vector();
    private Result finalResult = null;
    private Node myNode = null;

    /**
     * The no args constructor for ProActive.
     */
    public Manager() {
        // nothing to do
    }

    /**
     * @param root the root task.
     * @param nodes the array of nodes for the computation.
     * @param myNode the local node which is associated to this manager.
     */
    public Manager(Task root, Node[] nodes, Node myNode) {
        this.myNode = myNode;
        try {
            this.rootTask = (Task) ProActive.turnActive(root, this.myNode);
        } catch (ActiveObjectCreationException e) {
            logger.fatal("Problem with the turn active of the root task", e);
            throw new RuntimeException(e);
        } catch (NodeException e) {
            logger.fatal("Problem with the node of the root task", e);
            throw new RuntimeException(e);
        }
        this.nodes = nodes;
    }

    public void initActivity(Body body) {
        try {
            this.taskProvider = (TaskProvider) ProActive.newActive(TaskProvider.class.getName(),
                    null, body.getNodeURL());
        } catch (ActiveObjectCreationException e1) {
            logger.fatal("Couldn't create the Task Provider", e1);
        } catch (NodeException e1) {
            logger.fatal("Couldn't create the Task Provider", e1);
        }

        // Group of Worker
        Object[][] args = new Object[this.nodes.length][2];
        for (int i = 0; i < args.length; i++) {
            args[i][0] = ProActive.getStubOnThis();
            args[i][1] = this.taskProvider;
        }
        try {
            this.workerGroup = (Worker) ProActiveGroup.newGroup(Worker.class.getName(),
                    args, this.nodes);
        } catch (ClassNotReifiableException e) {
            logger.fatal("The Worker is not reifiable", e);
        } catch (ActiveObjectCreationException e) {
            logger.fatal("Problem with active objects creation", e);
        } catch (NodeException e) {
            logger.fatal("Problem with a node", e);
        } catch (ClassNotFoundException e) {
            logger.fatal("The class for worker was not found", e);
        }

        try {
            this.workerGroup.setWorkerGroup(this.workerGroup);
        } catch (ExceptionListException e) {
            logger.debug("A worker is down", e);
        }

        // Spliting
        logger.info("Compute the lower bound for the root task");
        this.rootTask.initLowerBound();
        logger.info("Compute the upper bound for the root task");
        this.rootTask.initUpperBound();
        logger.info("Calling for the first time split on the root task");
        this.taskProvider.addAll(this.rootTask.split());
    }

    public void start() {
        Body body = ProActive.getBodyOnThis();
        if (!body.isActive()) {
            logger.fatal("The manager is not active");
            throw new RuntimeException("The manager is not active");
        }

        if (this.isComputing) {
            // nothing to do
            logger.info("The manager is already started");
            return;
        }

        this.isComputing = true;

        Group group = ProActiveGroup.getGroup(this.workerGroup);
        Iterator workerIt = group.iterator();
        while ((workerIt.hasNext()) &&
                this.taskProvider.hasNext().booleanValue()) {
            this.assignTaskToWorker((Worker) workerIt.next(),
                this.taskProvider.next());
        }

        Service service = new Service(body);
        while (this.taskProvider.hasNext().booleanValue()) {
            try {
                // wait for a free worker
                int index = ProActive.waitForAny(this.futureTaskList, 500);
                this.allResults.add(this.futureTaskList.remove(index));
                this.pendingTaskList.remove(index);
                Worker freeWorker = (Worker) this.workingWorkerList.remove(index);
                this.assignTaskToWorker(freeWorker, this.taskProvider.next());
                logger.info("Pending tasks: " + this.pendingTaskList.size() +
                    " - Achivied tasks: " + this.allResults.size() +
                    " - Not calculated tasks: " + this.taskProvider.size());
            } catch (ProActiveException e) {
                while (service.getRequestCount() > 0) {
                    service.serveOldest();
                }
                continue;
            }
        }

        // Serving requests and waiting for results
        while (this.taskProvider.hasNext().booleanValue() ||
                (this.pendingTaskList.size() != 0)) {
            try {
                int index = ProActive.waitForAny(this.futureTaskList, 1000);
                if (this.taskProvider.hasNext().booleanValue()) {
                    this.allResults.add(this.futureTaskList.remove(index));
                    this.pendingTaskList.remove(index);
                    Worker freeWorker = (Worker) this.workingWorkerList.remove(index);
                    this.assignTaskToWorker(freeWorker, this.taskProvider.next());
                    logger.info("Pending tasks: " +
                        this.pendingTaskList.size() + " - Achivied tasks: " +
                        this.allResults.size() + " - Not calculated tasks: " +
                        this.taskProvider.size());
                } else {
                    this.allResults.add(this.futureTaskList.remove(index));
                    this.pendingTaskList.remove(index);
                    this.freeWorkerList.add(this.workingWorkerList.remove(index));
                    logger.info("Pending tasks: " +
                        this.pendingTaskList.size() + " - Achivied tasks: " +
                        this.allResults.size() + " - Not calculated tasks: " +
                        this.taskProvider.size());
                }
            } catch (ProActiveException e) {
                while (service.getRequestCount() > 0) {
                    service.serveOldest();
                }
                continue;
            }
        }
        logger.info("Total of results = " + this.allResults.size());
        logger.info("Total of tasks = " + this.taskProvider.size());
        // Set the final result
        this.finalResult = this.rootTask.gather((Result[]) this.allResults.toArray(
                    new Result[this.allResults.size()]));
        this.isComputing = false;
    }

    /**
     * Assign a task to a worker.
     * @param worker the worker.
     * @param task the task.
     */
    private void assignTaskToWorker(Worker worker, Task task) {
        this.futureTaskList.add(worker.execute(task));
        this.pendingTaskList.add(task);
        this.workingWorkerList.add(worker);
    }

    public BooleanWrapper isFinish() {
        return new BooleanWrapper(!this.isComputing);
    }

    public Result getFinalResult() {
        ProActive.waitFor(this.finalResult);
        return this.finalResult;
    }

    public IntWrapper haveFreeWorkers() {
        return new IntWrapper(this.freeWorkerList.size());
    }
}
