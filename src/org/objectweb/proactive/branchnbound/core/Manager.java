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
package org.objectweb.proactive.branchnbound.core;

import java.io.IOException;
import java.io.Serializable;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.branchnbound.core.queue.TaskQueue;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.descriptor.data.VirtualNodeImpl;
import org.objectweb.proactive.core.event.NodeCreationEvent;
import org.objectweb.proactive.core.event.NodeCreationEventListener;
import org.objectweb.proactive.core.group.ExceptionInGroup;
import org.objectweb.proactive.core.group.ExceptionListException;
import org.objectweb.proactive.core.group.Group;
import org.objectweb.proactive.core.group.ProActiveGroup;
import org.objectweb.proactive.core.mop.ClassNotReifiableException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * @author Alexandre di Costanzo
 *
 * Created on May 31, 2005
 */
public class Manager implements Serializable, InitActive,
    NodeCreationEventListener {
    private static final boolean enableRealloc = false; // TODO turn it configurable
    private static final int backupTask = 10; // TODO turn it configurable
    private static final boolean enableAutoSplit = false; // TODO turn it configurable
    private static final String backupResultFile = System.getProperty(
            "user.home") + System.getProperty("file.separator") +
        "framework.results.backup"; // TODO turn it configurable
    private static Logger logger = ProActiveLogger.getLogger(Loggers.P2P_SKELETONS_MANAGER);
    private Task rootTask = null;
    private Node[] nodes = null;
    private Worker workerGroup = null;
    private ListIterator workerGroupListIt = null;
    private TaskQueue taskProviderQueue = null;
    private Vector futureTaskList = new Vector();
    private Vector pendingTaskList = new Vector();
    private Vector workingWorkerList = new Vector();
    private Vector freeWorkerList = new Vector();
    private String queueType = null;
    private VirtualNode virtualNode = null;
    private VirtualNode[] arrayOfVirtualNodes = null;
    private Vector reallocatedTasks = new Vector();
    private int reallocatedBase = -1;
    private Iterator pendingTasksIt = this.pendingTaskList.iterator();

    /**
     * The no args constructor for ProActive.
     */
    public Manager() {
        // nothing to do
    }

    private Manager(Task root, Node myNode, String queueType) {
        try {
            this.rootTask = (Task) ProActive.turnActive(root, myNode);
        } catch (ActiveObjectCreationException e) {
            logger.fatal("Problem with the turn active of the root task", e);
            throw new RuntimeException(e);
        } catch (NodeException e) {
            logger.fatal("Problem with the node of the root task", e);
            throw new RuntimeException(e);
        }
        this.queueType = queueType;
    }

    public Manager(VirtualNode virtualNode, String queueType) {
        this.queueType = queueType;
        this.virtualNode = virtualNode;
    }

    public Manager(VirtualNode[] virtualNodes, String queueType) {
        this.queueType = queueType;
        this.arrayOfVirtualNodes = virtualNodes;
    }

    public Manager(Node[] workerNodes, String queueType) {
        this.queueType = queueType;
        this.nodes = workerNodes;
    }

    public Manager(Task root, Node[] nodes, Node myNode, String queueType) {
        this(root, myNode, queueType);
        this.nodes = nodes;
    }

    public Manager(Task root, VirtualNode virtualNode, Node myNode,
        String queueType) {
        this(root, myNode, queueType);
        this.virtualNode = virtualNode;
    }

    public Manager(Task root, VirtualNode[] virtualNodes, Node myNode,
        String queueType) {
        this(root, myNode, queueType);
        this.arrayOfVirtualNodes = virtualNodes;
    }

    public void initActivity(Body body) {
        if (this.virtualNode != null) {
            ((VirtualNodeImpl) this.virtualNode).addNodeCreationEventListener(this);
            this.virtualNode.activate();
        } else if (this.arrayOfVirtualNodes != null) {
            for (int i = 0; i < this.arrayOfVirtualNodes.length; i++) {
                VirtualNodeImpl currentVn = (VirtualNodeImpl) this.arrayOfVirtualNodes[i];
                if (currentVn.isActivated()) {
                    logger.warn("The VN " + currentVn.getName() +
                        " is already actived");
                    continue;
                }
                currentVn.addNodeCreationEventListener(this);
                currentVn.activate();
            }
        }

        try {
            this.taskProviderQueue = (TaskQueue) ProActive.newActive(this.queueType,
                    null, body.getNodeURL());
        } catch (ActiveObjectCreationException e1) {
            logger.fatal("Couldn't create the Task Provider", e1);
            throw new ProActiveRuntimeException(e1);
        } catch (NodeException e1) {
            logger.fatal("Couldn't create the Task Provider", e1);
            throw new ProActiveRuntimeException(e1);
        }

        // Group of Worker
        try {
            if (this.nodes != null) {
                Object[][] args = new Object[this.nodes.length][1];
                for (int i = 0; i < args.length; i++) {
                    args[i][0] = this.taskProviderQueue;
                }
                this.workerGroup = (Worker) ProActiveGroup.newGroup(Worker.class.getName(),
                        args, this.nodes);
            } else {
                this.workerGroup = (Worker) ProActiveGroup.newGroup(Worker.class.getName());
            }
            this.workerGroupListIt = ProActiveGroup.getGroup(this.workerGroup)
                                                   .listIterator();
        } catch (ClassNotReifiableException e) {
            logger.fatal("The Worker is not reifiable", e);
            throw new ProActiveRuntimeException(e);
        } catch (ActiveObjectCreationException e) {
            logger.fatal("Problem with active objects creation", e);
            throw new ProActiveRuntimeException(e);
        } catch (NodeException e) {
            logger.fatal("Problem with a node", e);
        } catch (ClassNotFoundException e) {
            logger.fatal("The class for worker was not found", e);
            throw new ProActiveRuntimeException(e);
        }

        Group groupOfWorkers = ProActiveGroup.getGroup(this.workerGroup);
        try {
            this.workerGroup.setWorkerGroup(this.workerGroup);
        } catch (ExceptionListException e) {
            logger.debug("Some workers are down", e);
            Iterator it = e.iterator();
            while (it.hasNext()) {
                groupOfWorkers.remove(((ExceptionInGroup) it.next()).getObject());
            }
        }

        // Spliting
        logger.info("Compute the lower bound for the root task");
        this.rootTask.initLowerBound();
        logger.info("Compute the upper bound for the root task");
        this.rootTask.initUpperBound();
        logger.info("Calling for the first time split on the root task");
        Vector subTaskList = this.rootTask.split();
        logger.info("The ROOT task sends " + subTaskList.size());
        this.taskProviderQueue.addAll(subTaskList);
    }

    public Result start() {
        logger.info("Starting computation");
        // Nothing to do if the manager is not actived
        if (!ProActive.getBodyOnThis().isActive()) {
            logger.fatal("The manager is not active");
            throw new ProActiveRuntimeException("The manager is not active");
        }

        int backupCounter = 0;

        // Serving requests and waiting for results
        while (this.taskProviderQueue.hasNext().booleanValue() ||
                (this.pendingTaskList.size() != 0)) {
            if (this.taskProviderQueue.hasNext().booleanValue()) {
                boolean hasAddedTask = false;
                if (this.workerGroupListIt.hasNext()) {
                    this.assignTaskToWorker((Worker) this.workerGroupListIt.next(),
                        this.taskProviderQueue.next());
                    hasAddedTask = true;
                } else if ((this.freeWorkerList.size() > 0)) {
                    this.assignTaskToWorker((Worker) this.freeWorkerList.remove(
                            0), this.taskProviderQueue.next());
                    hasAddedTask = true;
                } else if (this.futureTaskList.size() == 0) {
                    // Waiting workers
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                    }
                    continue;
                }
                if (hasAddedTask && logger.isInfoEnabled()) {
                    logger.info("Pending tasks: " +
                        this.pendingTaskList.size() + " - Achivied tasks: " +
                        "No results back" + " - Not calculated tasks: " +
                        this.taskProviderQueue.size());
                    continue;
                }
            }

            try {
                int index = ProActive.waitForAny(this.futureTaskList, 1000);
                backupCounter++;
                if (enableRealloc && (this.reallocatedBase != -1)) {
                    int realIndex = (index > this.reallocatedBase)
                        ? (index % this.reallocatedBase) : index;
                    int inc = 0;
                    boolean alreadyFoundIt = false;

                    // Cleaning future list and pending list and worker list
                    for (int i = 0; i < this.futureTaskList.size(); i++) {
                        if (((i - inc) % this.reallocatedBase) == realIndex) {
                            inc++;
                            Result currentResult = (Result) this.futureTaskList.remove(i);
                            Worker workerFree = (Worker) this.workingWorkerList.remove(i);
                            if (enableAutoSplit) {
                                Task theTask = workerFree.getCurrentTask();
                                workerFree.sendSubTasksToTheManager(theTask.split());
                            }
                            workerFree.stopTask();
                            if (!alreadyFoundIt &&
                                    !ProActive.isAwaited(currentResult)) {
                                alreadyFoundIt = true;
                                this.taskProviderQueue.addResult(currentResult);
                                this.freeWorkerList.add(workerFree);
                            }
                            this.pendingTaskList.remove(i);
                        }
                    }
                } else {
                    this.taskProviderQueue.addResult((Result) this.futureTaskList.remove(
                            index));
                    this.pendingTaskList.remove(index);
                    Worker freeWorker = (Worker) this.workingWorkerList.remove(index);
                    if (this.taskProviderQueue.hasNext().booleanValue()) {
                        this.assignTaskToWorker(freeWorker,
                            this.taskProviderQueue.next());
                    } else {
                        this.freeWorkerList.add(freeWorker);
                    }
                }
                logger.info("Pending tasks: " + this.pendingTaskList.size() +
                    " - Achivied tasks: " +
                    this.taskProviderQueue.howManyResults() +
                    " - Not calculated tasks: " +
                    this.taskProviderQueue.size());
                if ((backupCounter % backupTask) == 0) {
                    try {
                        this.backupAll(this.rootTask);
                    } catch (IOException e) {
                        logger.warn("Backup failed", e);
                    }
                }
            } catch (ProActiveException e) {
                // Reallocating tasks
                if (enableRealloc && (this.freeWorkerList.size() > 0)) {
                    if (this.reallocatedBase == -1) {
                        // Fix the base for using modulo on future list
                        this.reallocatedBase = this.pendingTaskList.size();
                    }
                    Task nextTask = null;
                    if (!this.pendingTasksIt.hasNext()) {
                        this.pendingTasksIt = this.pendingTaskList.iterator();
                    }
                    nextTask = (Task) this.pendingTasksIt.next();
                    this.assignTaskToWorker((Worker) this.freeWorkerList.remove(
                            0), nextTask);
                }
                continue;
            }
        }
        logger.info("Total of results = " +
            this.taskProviderQueue.howManyResults());
        logger.info("Total of tasks = " + this.taskProviderQueue.size());
        // Set the final result
        return this.rootTask.gather((Result[]) this.taskProviderQueue.getAllResults()
                                                                     .toArray(new Result[this.taskProviderQueue.howManyResults()
                                                                                                               .intValue()]));
    }

    private void backupAll(Task rootTask) throws IOException {
        logger.info("Backuping");
        this.taskProviderQueue.backupResults(backupResultFile);
        this.taskProviderQueue.backupTasks(rootTask, this.pendingTaskList);
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

    public void nodeCreated(NodeCreationEvent event) {
        Object[] args = new Object[] { this.taskProviderQueue };
        Node createdNode = event.getNode();
        if (logger.isInfoEnabled()) {
            logger.info(">>>> New Node: " +
                createdNode.getNodeInformation().getURL());
        }
        Worker newWorker = null;
        try {
            newWorker = (Worker) ProActive.newActive(Worker.class.getName(),
                    args, createdNode);
        } catch (ActiveObjectCreationException e) {
            logger.warn("Couldn't create a worker", e);
            return;
        } catch (NodeException e) {
            logger.warn("Couldn't create a worker, this caused by a node failure",
                e);
            return;
        }
        this.workerGroup.addMember(newWorker);
        this.workerGroupListIt.add(newWorker);
        newWorker.setWorkerGroup(this.workerGroup);
        this.freeWorkerList.add(newWorker);
    }

    public void setHungryLevel(int level) {
        assert this.taskProviderQueue != null : "Manager is not active";
        this.taskProviderQueue.setHungryLevel(level);
    }

    public void loadTasks(String taskFile) {
        if (!ProActive.getBodyOnThis().isActive()) {
            logger.fatal("The manager is not active");
            throw new ProActiveRuntimeException("The manager is not active");
        }
        this.taskProviderQueue.loadTasks(taskFile);
        this.taskProviderQueue.getRootTaskFromBackup();
        this.pendingTaskList = (Vector) this.taskProviderQueue.getPendingTasksFromBackup();
        try {
            this.rootTask = (Task) ProActive.turnActive(this.taskProviderQueue.getRootTaskFromBackup(),
                    ProActive.getBodyOnThis().getNodeURL());
        } catch (ActiveObjectCreationException e) {
            logger.fatal("Problem with the turn active of the root task", e);
            throw new RuntimeException(e);
        } catch (NodeException e) {
            logger.fatal("Problem with the node of the root task", e);
            throw new RuntimeException(e);
        }
    }

    public void loadResults(String resultFile) {
        this.taskProviderQueue.loadResults(resultFile);
    }

    public VirtualNode getBackVn() {
        return this.virtualNode;
    }

    public VirtualNode[] getBackVns() {
        return this.arrayOfVirtualNodes;
    }
}
