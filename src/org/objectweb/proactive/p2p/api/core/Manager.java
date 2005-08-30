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
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.group.ExceptionListException;
import org.objectweb.proactive.core.group.Group;
import org.objectweb.proactive.core.group.ProActiveGroup;
import org.objectweb.proactive.core.mop.ClassNotReifiableException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.p2p.api.core.queue.TaskQueue;


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
    private TaskQueue taskProvider;
    private Vector futureTaskList = new Vector();
    private Vector pendingTaskList = new Vector();
    private Vector workingWorkerList = new Vector();
    private Vector freeWorkerList = new Vector();
    private Vector allResults = new Vector();
    private String queueType = null;

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
     * @param queueType
     * @param setAutoPriorityQueue
     */
    public Manager(Task root, Node[] nodes, Node myNode, String queueType) {
        try {
            this.rootTask = (Task) ProActive.turnActive(root, myNode);
        } catch (ActiveObjectCreationException e) {
            logger.fatal("Problem with the turn active of the root task", e);
            throw new RuntimeException(e);
        } catch (NodeException e) {
            logger.fatal("Problem with the node of the root task", e);
            throw new RuntimeException(e);
        }
        this.nodes = nodes;
        this.queueType = queueType;
    }

    public void initActivity(Body body) {
        try {
            this.taskProvider = (TaskQueue) ProActive.newActive(this.queueType,
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
        Vector subTaskList = this.rootTask.split();
        String taskTag = this.rootTask.getTag();
        if (taskTag == null) {
            taskTag = 0 + "";
        }
        logger.info("The ROOT task sends " + subTaskList.size() +
            " with group tag " + taskTag);
        int rootPriority = this.rootTask.getPriority();
        for (int i = 0; i < subTaskList.size(); i++) {
            Task current = (Task) subTaskList.get(i);
            current.setPriority(rootPriority);
            current.incPriority();
            current.setTag(taskTag + "-" + i);
        }
        this.taskProvider.addAll(subTaskList);
    }

    public Result start() {
        // Nothing to do if the manager is not actived
        if (!ProActive.getBodyOnThis().isActive()) {
            logger.fatal("The manager is not active");
            throw new RuntimeException("The manager is not active");
        }

        Group group = ProActiveGroup.getGroup(this.workerGroup);
        Iterator workerIt = group.iterator();
        while ((workerIt.hasNext()) &&
                this.taskProvider.hasNext().booleanValue()) {
            this.assignTaskToWorker((Worker) workerIt.next(),
                this.taskProvider.next());
            logger.info("Init - Pending tasks: " + this.pendingTaskList.size() +
                " - Achivied tasks: " + this.allResults.size() +
                " - Not calculated tasks: " + this.taskProvider.size());
        }

        while (this.taskProvider.hasNext().booleanValue()) {
            if (workerIt.hasNext()) {
                this.assignTaskToWorker((Worker) workerIt.next(),
                    this.taskProvider.next());
                logger.info("Init - Pending tasks: " +
                    this.pendingTaskList.size() + " - Achivied tasks: " +
                    this.allResults.size() + " - Not calculated tasks: " +
                    this.taskProvider.size());
                continue;
            }
            try {
                // wait for a free worker
                int index = ProActive.waitForAny(this.futureTaskList, 1000);
                this.allResults.add(this.futureTaskList.remove(index));
                this.pendingTaskList.remove(index);
                Worker freeWorker = (Worker) this.workingWorkerList.remove(index);
                if (this.taskProvider.hasNext().booleanValue()) {
                    this.assignTaskToWorker(freeWorker, this.taskProvider.next());
                    logger.info("Pending tasks: " +
                        this.pendingTaskList.size() + " - Achivied tasks: " +
                        this.allResults.size() + " - Not calculated tasks: " +
                        this.taskProvider.size());
                } else {
                    continue;
                }
            } catch (ProActiveException e) {
                continue;
            }
        }

        // Serving requests and waiting for results
        while (this.taskProvider.hasNext().booleanValue() ||
                (this.pendingTaskList.size() != 0)) {
            if (workerIt.hasNext() &&
                    this.taskProvider.hasNext().booleanValue()) {
                this.assignTaskToWorker((Worker) workerIt.next(),
                    this.taskProvider.next());
                logger.info("Init - Pending tasks: " +
                    this.pendingTaskList.size() + " - Achivied tasks: " +
                    this.allResults.size() + " - Not calculated tasks: " +
                    this.taskProvider.size());
                continue;
            }
            try {
                int index = ProActive.waitForAny(this.futureTaskList, 1000);
                if (this.taskProvider.hasNext().booleanValue()) {
                    this.allResults.add(this.futureTaskList.remove(index));
                    this.pendingTaskList.remove(index);
                    Worker freeWorker = (Worker) this.workingWorkerList.remove(index);
                    if (this.taskProvider.hasNext().booleanValue()) {
                        this.assignTaskToWorker(freeWorker,
                            this.taskProvider.next());
                        logger.info("Waiting for workers - Pending tasks: " +
                            this.pendingTaskList.size() +
                            " - Achivied tasks: " + this.allResults.size() +
                            " - Not calculated tasks: " +
                            this.taskProvider.size());
                    } else {
                        continue;
                    }
                } else {
                    this.allResults.add(this.futureTaskList.remove(index));
                    this.pendingTaskList.remove(index);
                    this.freeWorkerList.add(this.workingWorkerList.remove(index));
                    logger.info("Waiting for final results - Pending tasks: " +
                        this.pendingTaskList.size() + " - Achivied tasks: " +
                        this.allResults.size() + " - Not calculated tasks: " +
                        this.taskProvider.size());
                }
            } catch (ProActiveException e) {
                continue;
            }
        }
        logger.info("Total of results = " + this.allResults.size());
        logger.info("Total of tasks = " + this.taskProvider.size());
        // Set the final result
        return this.rootTask.gather((Result[]) this.allResults.toArray(
                new Result[this.allResults.size()]));
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
}
