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
package org.objectweb.proactive.extensions.branchnbound.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.api.ProActiveObject;
import org.objectweb.proactive.api.ProFuture;
import org.objectweb.proactive.api.ProGroup;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.group.Group;
import org.objectweb.proactive.core.mop.ClassNotReifiableException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.branchnbound.core.queue.TaskQueue;


/**
 *
 * <p><Manage and control the Branch and Bound computation.</p>
 * @author Alexandre di Costanzo
 *
 * Created on May 31, 2005
 */
@PublicAPI
public class Manager implements Serializable, InitActive {
    private static Logger logger = ProActiveLogger.getLogger(Loggers.P2P_SKELETONS_MANAGER);
    private static final boolean enableRealloc = false; // TODO turn it
    private static final boolean enableBackup = false; // TODO turn it
    private static final int backupTask = 10; // TODO turn it configurable
    private static final String backupResultFile = System.getProperty(
            "user.home") + File.separator + "framework.results.backup"; // TODO turn it configurable
    public static final String backupTaskFile = System.getProperty("user.home") +
        File.separator + "framework.tasks.backup"; // TODO turn it configurable
    private Task rootTask = null;
    private TaskQueue taskProviderQueue = null;

    // Worker nodes
    private Node[] nodes = null;
    private Node[][] arrayOfNodes = null;
    private VirtualNode[] arrayOfVns = null;

    // Worker group
    private Worker workerGroup = null;

    // managing task
    private Vector<Result> futureTaskList = new Vector<Result>();
    private Vector<Task> pendingTaskList = new Vector<Task>();
    private Vector<Worker> workingWorkerList = new Vector<Worker>();
    private Vector<Worker> freeWorkerList = new Vector<Worker>();
    private Vector<Task> toReallocTaskList = new Vector<Task>();

    /**
     * The no args constructor for ProActive.
     */
    public Manager() {
        // nothing to do
    }

    /**
     * The main constructor.
     * @param root the root task.
     * @param myNode the node where <code>this</code> is running.
     * @param queueType the class name of the task queue.
     */
    private Manager(Task root, Node myNode, String queueType) {
        // Activate the root task
        try {
            this.rootTask = (Task) ProActiveObject.turnActive(root, myNode);
        } catch (ActiveObjectCreationException e) {
            logger.fatal("Problem with the turn active of the root task", e);
            throw new RuntimeException(e);
        } catch (NodeException e) {
            logger.fatal("Problem with the node of the root task", e);
            throw new RuntimeException(e);
        }

        // Activate the task queue
        try {
            if (logger.isInfoEnabled()) {
                logger.info("Activing the task queue: " + queueType);
            }
            this.taskProviderQueue = (TaskQueue) ProActiveObject.newActive(queueType,
                    null, myNode);
        } catch (ActiveObjectCreationException e1) {
            logger.fatal("Couldn't create the Task Provider", e1);
            throw new ProActiveRuntimeException(e1);
        } catch (NodeException e1) {
            logger.fatal("Couldn't create the Task Provider", e1);
            throw new ProActiveRuntimeException(e1);
        }
    }

    /**
     * Contruct a new Manager.
     * @param root the root task.
     * @param myNode the node where <code>this</code> is running.
     * @param nodes the array of nodes for the computation.
     * @param queueType the class name of the task queue.
     */
    public Manager(Task root, Node myNode, Node[] nodes, String queueType) {
        this(root, myNode, queueType);
        this.nodes = nodes;
    }

    /**
     * Contruct a new Manager. Hierarchic communication are used between the
     * given array of nodes.
     * @param root the root task.
     * @param myNode the node where <code>this</code> is running.
     * @param nodes the array of array of nodes for the computation.
     * @param queueType the class name of the task queue.
     */
    public Manager(Task root, Node myNode, Node[][] nodes, String queueType) {
        this(root, myNode, queueType);
        this.arrayOfNodes = nodes;
    }

    /**
     * Contruct a new Manager. Hierarchic communication are used between the
     * given virtual nodes. For a faster deployment, it is suggested to do not
     * activate virtual nodes before.
     * @param root the root task.
     * @param myNode the node where <code>this</code> is running.
     * @param virtualNodes the array of vitrual nodes for the computation.
     * @param queueType the class name of the task queue.
     */
    public Manager(Task root, Node myNode, VirtualNode[] virtualNodes,
        String queueType) {
        this(root, myNode, queueType);
        this.arrayOfVns = virtualNodes;
    }

    /**
     * Prepare everything for the computation. Activate the task queue, create
     * Workers, etc.
     * @see org.objectweb.proactive.InitActive#initActivity(org.objectweb.proactive.Body)
     */
    public void initActivity(Body body) {
        // All asynchronous call on the root Task
        logger.info("Compute the lower bound for the root task");
        this.rootTask.initLowerBound();
        logger.info("Compute the upper bound for the root task");
        this.rootTask.initUpperBound();
        logger.info("Calling for the first time split on the root task");
        Vector<Task> subTaskList = this.rootTask.split();
        logger.info("The ROOT task sends " + subTaskList.size());
        this.taskProviderQueue.addAll(subTaskList);

        // Group of Worker
        try {
            Object[] args = new Object[] { this.taskProviderQueue };

            // TODO Factoring
            if (this.nodes != null) {
                logger.info("Manager is deploying a group of workers");
                // Node[]
                long singleStartTime = System.currentTimeMillis();
                this.workerGroup = (Worker) ProGroup.newGroupInParallel(Worker.class.getName(),
                        args, this.nodes);
                ProGroup.getGroup(this.workerGroup).setAutomaticPurge(true);
                this.freeWorkerList.addAll(ProGroup.getGroup(this.workerGroup));
                long singleEndTime = System.currentTimeMillis();
                if (logger.isInfoEnabled()) {
                    logger.info("The  Group was created in " +
                        (singleEndTime - singleStartTime) + " ms");
                }
            } else if ((this.arrayOfNodes != null) &&
                    (this.arrayOfNodes.length > 0)) {
                logger.info("Manager is deploying " + this.arrayOfNodes.length +
                    " groups of workers");
                // Node[][]
                this.workerGroup = (Worker) ProGroup.newGroup(Worker.class.getName());
                Group<Worker> mainGroup = ProGroup.getGroup(this.workerGroup);
                for (int i = 0; i < this.arrayOfNodes.length; i++) {
                    GroupThread gt = new GroupThread(this.arrayOfNodes[i],
                            args, mainGroup);
                    new Thread(gt).start();
                }
            } else if ((this.arrayOfVns != null) &&
                    (this.arrayOfVns.length > 0)) {
                logger.info("Manager is deploying " + this.arrayOfVns.length +
                    " groups of workers");
                // VN []
                this.workerGroup = (Worker) ProGroup.newGroup(Worker.class.getName());
                Group<Worker> vnGroup = ProGroup.getGroup(this.workerGroup);
                for (int i = 0; i < this.arrayOfVns.length; i++) {
                    VnThread vt = new VnThread(this.arrayOfVns[i], args, vnGroup);
                    new Thread(vt).start();
                }
            } else {
                logger.fatal("No nodes for distributing the computation");
                throw new ProActiveRuntimeException(
                    "No nodes for distributing the computation");
            }
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

        Group<Worker> groupOfWorkers = ProGroup.getGroup(this.workerGroup);
        this.workerGroup.setWorkerGroup(this.workerGroup);

        if (logger.isInfoEnabled()) {
            logger.info("Manager successfuly activate with " +
                this.freeWorkerList.size() + " workers");
        }

        this.nodes = null;
        this.arrayOfNodes = null;
    }

    /**
     * Start the computation.
     * @return the best found solution.
     */
    public Result start() {
        logger.info("Starting computation");
        // Nothing to do if the manager is not actived
        if (!ProActiveObject.getBodyOnThis().isActive()) {
            logger.fatal("The manager is not active");
            throw new ProActiveRuntimeException("The manager is not active");
        }

        int backupCounter = 0;
        int reallocCounter = 0;

        // Serving requests and waiting for results
        boolean hasNext;
        while ((hasNext = this.taskProviderQueue.hasNext().booleanValue()) ||
                (this.pendingTaskList.size() != 0) ||
                (!this.toReallocTaskList.isEmpty())) {
            boolean hasAddedTask = false;
            if (!this.toReallocTaskList.isEmpty() &&
                    !this.freeWorkerList.isEmpty()) {
                Task tReallocated = this.toReallocTaskList.remove(0);
                try {
                    this.assignTaskToWorker(this.freeWorkerList.remove(0),
                        tReallocated);
                    logger.info("A task just reallocated");
                } catch (Exception e) {
                    logger.info("A worker is down");
                    this.toReallocTaskList.add(tReallocated);
                }
            }
            try {
                if (hasNext && (this.freeWorkerList.size() > 0)) {
                    if ((this.freeWorkerList.size() > 0)) {
                        Task t = this.taskProviderQueue.next();
                        try {
                            this.assignTaskToWorker(this.freeWorkerList.remove(
                                    0), t);
                            hasAddedTask = true;
                        } catch (Exception e) {
                            logger.info("A worker is down");
                            this.taskProviderQueue.addTask(t);
                        }
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
                            this.pendingTaskList.size() +
                            " - Achivied tasks: " +
                            this.taskProviderQueue.howManyResults() +
                            " - Not calculated tasks: " +
                            this.taskProviderQueue.size());
                        continue;
                    }
                }

                try {
                    int index = ProFuture.waitForAny(this.futureTaskList, 1000);
                    backupCounter++;
                    Result currentResult = this.futureTaskList.remove(index);
                    this.taskProviderQueue.addResult(currentResult);
                    this.pendingTaskList.remove(index);
                    Worker freeWorker = this.workingWorkerList.remove(index);
                    if (this.taskProviderQueue.hasNext().booleanValue()) {
                        Task t1 = this.taskProviderQueue.next();
                        try {
                            this.assignTaskToWorker(freeWorker, t1);
                        } catch (Exception e) {
                            logger.info("A worker is down");
                            this.taskProviderQueue.addTask(t1);
                        }
                    } else {
                        this.freeWorkerList.add(freeWorker);
                    }
                    if (logger.isDebugEnabled()) {
                        logger.debug(currentResult);
                    }
                    if (logger.isInfoEnabled()) {
                        logger.info("Pending tasks: " +
                            this.pendingTaskList.size() +
                            " - Achivied tasks: " +
                            this.taskProviderQueue.howManyResults() +
                            " - Not calculated tasks: " +
                            this.taskProviderQueue.size());
                    }
                    if (enableBackup && ((backupCounter % backupTask) == 0)) {
                        this.backupAll(this.rootTask);
                    }
                } catch (Exception e) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Manager is waiting for result: " + e);
                    }
                    reallocCounter++;
                    // Reallocating tasks
                    if (enableRealloc && !this.freeWorkerList.isEmpty() &&
                            (reallocCounter == 60)) {
                        reallocCounter = 0;
                        for (int i = 0; i < this.workingWorkerList.size();
                                i++) {
                            Worker current = this.workingWorkerList.get(i);
                            try {
                                current.alive();
                            } catch (Exception down) {
                                this.futureTaskList.remove(i);
                                this.workingWorkerList.remove(i);
                                this.toReallocTaskList.add(this.pendingTaskList.remove(
                                        i));
                            }
                        }
                    }
                    continue;
                }
            } catch (Exception e) {
                continue;
            }
        }
        logger.info("Total of results = " +
            this.taskProviderQueue.howManyResults());
        logger.info("Total of tasks = " + this.taskProviderQueue.size());

        // Set the final result
        Collection<Result> resultsFuture = this.taskProviderQueue.getAllResults();
        ProFuture.waitFor(resultsFuture);
        Result[] results = resultsFuture.toArray(new Result[this.taskProviderQueue.howManyResults()
                                                                                  .intValue()]);
        return this.rootTask.gather(results);
    }

    /**
     * Start the computation with a new root task.
     * @param rootTask the new root task.
     * @return the best found solution.
     */
    public Result start(Task rootTask) {
        this.taskProviderQueue.flushAll();

        this.workerGroup.reset();

        try {
            this.rootTask = (Task) ProActiveObject.turnActive(rootTask,
                    ProActiveObject.getBodyOnThis().getNodeURL());
        } catch (ActiveObjectCreationException e) {
            logger.fatal("Problem with the turn active of the root task", e);
            throw new RuntimeException(e);
        } catch (NodeException e) {
            logger.fatal("Problem with the node of the root task", e);
            throw new RuntimeException(e);
        }

        // Spliting
        logger.info("Compute the lower bound for the root task");
        this.rootTask.initLowerBound();
        logger.info("Compute the upper bound for the root task");
        this.rootTask.initUpperBound();
        logger.info("Calling for the first time split on the root task");
        Vector<Task> subTaskList = this.rootTask.split();
        logger.info("The ROOT task sends " + subTaskList.size());
        this.taskProviderQueue.addAll(subTaskList);

        return ((Manager) ProActiveObject.getStubOnThis()).start();
    }

    /**
     * Start a computation from a previous backup.
     * @param task the stream with task backup.
     * @param result the stream with result backup.
     * @return the best found solution.
     */
    public Result start(InputStream task, InputStream result) {
        this.loadTasks(task);
        this.loadResults(result);
        return ((Manager) ProActiveObject.getStubOnThis()).start();
    }

    /**
     * Set the hungry level of the task queue.
     * @param level the hungry level.
     * @see TaskQueue#setHungryLevel(int)
     */
    public void setHungryLevel(int level) {
        assert this.taskProviderQueue != null : "Manager is not active";
        this.taskProviderQueue.setHungryLevel(level);
    }

    // -------------------------------------------------------------------------
    // Private methods
    // -------------------------------------------------------------------------

    /**
     * Assign a task to a worker.
     *
     * @param worker
     *            the worker.
     * @param task
     *            the task.
     */
    private void assignTaskToWorker(Worker worker, Task task)
        throws Exception {
        this.futureTaskList.add(worker.execute(task));
        this.pendingTaskList.add(task);
        this.workingWorkerList.add(worker);
    }

    /**
     * Backup everythings.
     * @param rootTask the root task.
     */
    private void backupAll(Task rootTask) {
        logger.info("Backuping");
        try {
            this.taskProviderQueue.backupResults(new FileOutputStream(
                    backupResultFile));
            this.taskProviderQueue.backupTasks(rootTask, this.pendingTaskList,
                new FileOutputStream(backupTaskFile));
        } catch (FileNotFoundException e) {
            logger.fatal("Problem with backup", e);
            throw new ProActiveRuntimeException(e);
        }
    }

    /**
     * Restoring tasks from a previous backup.
     * @param taskFile the stream for restoring.
     */
    private void loadTasks(InputStream taskFile) {
        if (!ProActiveObject.getBodyOnThis().isActive()) {
            logger.fatal("The manager is not active");
            throw new ProActiveRuntimeException("The manager is not active");
        }
        this.taskProviderQueue.loadTasks(taskFile);
        this.taskProviderQueue.getRootTaskFromBackup();
        try {
            this.rootTask = (Task) ProActiveObject.turnActive(this.taskProviderQueue.getRootTaskFromBackup(),
                    ProActiveObject.getBodyOnThis().getNodeURL());
        } catch (ActiveObjectCreationException e) {
            logger.fatal("Problem with the turn active of the root task", e);
            throw new RuntimeException(e);
        } catch (NodeException e) {
            logger.fatal("Problem with the node of the root task", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Retsoring results from a previous backup.
     * @param resultFile the strem for restoring.
     */
    private void loadResults(InputStream resultFile) {
        this.taskProviderQueue.loadResults(resultFile);
    }

    // -------------------------------------------------------------------------
    // Inner Threads for groups creation and activing deploying
    // -------------------------------------------------------------------------

    /**
     * Inner class for faster deploying.
     *
     * @author Alexandre di Costanzo
     *
     * Created on Nov 3, 2005
     */
    private class GroupThread implements Runnable {
        private Node[] nodes;
        private Object[] args;
        private Group<Worker> group;

        public GroupThread(Node[] nodes, Object[] args, Group<Worker> group) {
            this.nodes = nodes;
            this.args = args;
            this.group = group;
        }

        public void run() {
            Worker tmpWorkers = null;
            if (this.nodes.length > 0) {
                long startTime = System.currentTimeMillis();
                try {
                    tmpWorkers = (Worker) ProGroup.newGroupInParallel(Worker.class.getName(),
                            args, this.nodes);
                    freeWorkerList.addAll(ProGroup.getGroup(tmpWorkers));
                    Worker activedTmpWorkers = (Worker) ProGroup.turnActiveGroup(tmpWorkers,
                            this.nodes[0]);
                    this.group.add(activedTmpWorkers);
                } catch (Exception e) {
                    logger.fatal("Problem with group creation", e);
                    return;
                }
                long endTime = System.currentTimeMillis();
                if (logger.isInfoEnabled()) {
                    logger.info("The remote Group " +
                        this.nodes[0].getVMInformation().getHostName() +
                        " was created in " + (endTime - startTime) +
                        " ms with " + ProGroup.getGroup(tmpWorkers).size() +
                        " members");
                }
            } else {
                if (logger.isInfoEnabled()) {
                    logger.info(
                        "A remote Group was not created because no deployed nodes");
                }
            }
        }
    }

    /**
     * Inner class for faster deployment.
     * @author Alexandre di Costanzo
     *
     * Created on Nov 3, 2005
     */
    private class VnThread implements Runnable {
        private VirtualNode vn;
        private Object[] args;
        private Group<Worker> group;
        long startTime;
        long endTime;

        public VnThread(VirtualNode virtualNode, Object[] args,
            Group<Worker> vnGroup) {
            this.vn = virtualNode;
            this.args = args;
            this.group = vnGroup;
        }

        public void run() {
            Node[] nodes = null;
            try {
                startTime = System.currentTimeMillis();
                this.vn.activate();
                nodes = this.vn.getNodes();
                endTime = System.currentTimeMillis();
                if (logger.isInfoEnabled()) {
                    logger.info("The VN " + this.vn.getName() +
                        " was deployed in " + (endTime - startTime) +
                        " ms with " + nodes.length + " nodes");
                }
            } catch (NodeException e) {
                if (logger.isInfoEnabled()) {
                    logger.info("No nodes returned for " + this.vn.getName());
                }
                return;
            }
            Worker tmpWorkers = null;
            if (nodes.length > 0) {
                startTime = System.currentTimeMillis();
                try {
                    tmpWorkers = (Worker) ProGroup.newGroupInParallel(Worker.class.getName(),
                            args, nodes);
                    freeWorkerList.addAll(ProGroup.getGroup(tmpWorkers));
                    Worker activedTmpWorkers = (Worker) ProGroup.turnActiveGroup(tmpWorkers,
                            nodes[0]);
                    this.group.add(activedTmpWorkers);
                } catch (Exception e) {
                    logger.fatal("Problem with group creation", e);
                    return;
                }
                workerGroup.setWorkerGroup(workerGroup);
                endTime = System.currentTimeMillis();
                if (logger.isInfoEnabled()) {
                    logger.info("The remote Group " + this.vn.getName() +
                        " was created in " + (endTime - startTime) +
                        " ms with " + ProGroup.getGroup(tmpWorkers).size() +
                        " members");
                }
            } else {
                if (logger.isInfoEnabled()) {
                    logger.info(
                        "A remote Group was not created because no deployed nodes");
                }
            }
        }
    }
}
