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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.group.ExceptionInGroup;
import org.objectweb.proactive.core.group.ExceptionListException;
import org.objectweb.proactive.core.group.Group;
import org.objectweb.proactive.core.group.ProActiveGroup;
import org.objectweb.proactive.core.mop.ClassNotReifiableException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;

import ibis.impl.messagePassing.InputStream;


/**
 * @author Alexandre di Costanzo
 *
 * Created on May 31, 2005
 */
public class Manager implements Serializable, InitActive {
    private static Logger logger = ProActiveLogger.getLogger(Loggers.P2P_SKELETONS_MANAGER);
    private static final boolean enableRealloc = false; // TODO turn it
    private static final boolean enableBackup = false; // TODO turn it
    private static final int backupTask = 10; // TODO turn it configurable
    private static final String backupResultFile = System.getProperty(
            "user.home") + System.getProperty("file.separator") +
        "framework.results.backup"; // TODO turn it configurable
    public static final String backupTaskFile = System.getProperty("user.home") +
        System.getProperty("file.separator") + "framework.tasks.backup"; // TODO turn it configurable 
    private Task rootTask = null;
    private TaskQueue taskProviderQueue = null;

    // Worker nodes
    private Node[] nodes = null;
    private Node[][] arrayOfNodes = null;

    // Worker group
    private Worker workerGroup = null;
    private ListIterator workerGroupListIt = null;

    // managing task
    private Vector futureTaskList = new Vector();
    private Vector pendingTaskList = new Vector();
    private Vector workingWorkerList = new Vector();
    private Vector freeWorkerList = new Vector();
    private Vector toReallocTaskList = new Vector();

    /**
     * The no args constructor for ProActive.
     */
    public Manager() {
        // nothing to do
    }

    private Manager(Task root, Node myNode, String queueType) {
        // Activate the root task
        try {
            this.rootTask = (Task) ProActive.turnActive(root, myNode);
        } catch (ActiveObjectCreationException e) {
            logger.fatal("Problem with the turn active of the root task", e);
            throw new RuntimeException(e);
        } catch (NodeException e) {
            logger.fatal("Problem with the node of the root task", e);
            throw new RuntimeException(e);
        }

        // Activate the task queue
        try {
            this.taskProviderQueue = (TaskQueue) ProActive.newActive(queueType,
                    null, myNode);
        } catch (ActiveObjectCreationException e1) {
            logger.fatal("Couldn't create the Task Provider", e1);
            throw new ProActiveRuntimeException(e1);
        } catch (NodeException e1) {
            logger.fatal("Couldn't create the Task Provider", e1);
            throw new ProActiveRuntimeException(e1);
        }
    }

    public Manager(Task root, Node myNode, Node[] nodes, String queueType) {
        this(root, myNode, queueType);
        this.nodes = nodes;
    }

    public Manager(Task root, Node myNode, Node[][] nodes, String queueType) {
        this(root, myNode, queueType);
        this.arrayOfNodes = nodes;
    }

    public void initActivity(Body body) {
        // All asynchronous call on the root Task
        logger.info("Compute the lower bound for the root task");
        this.rootTask.initLowerBound();
        logger.info("Compute the upper bound for the root task");
        this.rootTask.initUpperBound();
        logger.info("Calling for the first time split on the root task");
        Vector subTaskList = this.rootTask.split();
        logger.info("The ROOT task sends " + subTaskList.size());
        this.taskProviderQueue.addAll(subTaskList);

        // Group of Worker
        try {
            Object[][] args = new Object[(this.arrayOfNodes != null)
                ? this.arrayOfNodes.length : this.nodes.length][1];
            for (int i = 0; i < args.length; i++) {
                args[i][0] = this.taskProviderQueue;
            }
            if (this.nodes != null) {
                logger.info("Manager is deplying a group of workers");
                // Node[]
                this.workerGroup = (Worker) ProActiveGroup.newGroupBuiltWithMultithreading(Worker.class.getName(),
                        args, this.nodes);
            } else if ((this.arrayOfNodes != null) &&
                    (this.arrayOfNodes.length > 0)) {
                logger.info("Manager is deploying " + this.arrayOfNodes.length +
                    " groups of workers");
                // Node[][]
                this.workerGroup = (Worker) ProActiveGroup.newGroupBuiltWithMultithreading(Worker.class.getName(),
                        args, this.arrayOfNodes[0]);
                Group tmpGroup = ProActiveGroup.getGroup(this.workerGroup);
                for (int i = 1; i < this.arrayOfNodes.length; i++) {
                    Worker tmpWorkers = (Worker) ProActiveGroup.newGroupBuiltWithMultithreading(Worker.class.getName(),
                            args, this.arrayOfNodes[i]);
                    tmpGroup.add(tmpWorkers);
                }
            } else {
                logger.fatal("No nodes for distributing the computation");
                throw new ProActiveRuntimeException(
                    "No nodes for distributing the computation");
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
        if (logger.isInfoEnabled()) {
            logger.info("Manager successfuly activate with " +
                groupOfWorkers.size() + " workers");
        }
    }

    public Result start() {
        logger.info("Starting computation");
        // Nothing to do if the manager is not actived
        if (!ProActive.getBodyOnThis().isActive()) {
            logger.fatal("The manager is not active");
            throw new ProActiveRuntimeException("The manager is not active");
        }

        int backupCounter = 0;
        int reallocCounter = 0;

        // Serving requests and waiting for results
        while (this.taskProviderQueue.hasNext().booleanValue() ||
                (this.pendingTaskList.size() != 0) ||
                (!this.toReallocTaskList.isEmpty())) {
            boolean hasAddedTask = false;
            if (!this.toReallocTaskList.isEmpty() &&
                    !this.freeWorkerList.isEmpty()) {
                Task tReallocated = (Task) this.toReallocTaskList.remove(0);
                try {
                    this.assignTaskToWorker((Worker) this.freeWorkerList.remove(
                            0), tReallocated);
                    logger.info("A task just reallocated");
                } catch (Exception e) {
                    logger.info("A worker is down");
                    this.toReallocTaskList.add(tReallocated);
                }
            }
            try {
                Task t = this.taskProviderQueue.next();
                if (this.taskProviderQueue.hasNext().booleanValue()) {
                    if (this.workerGroupListIt.hasNext()) {
                        try {
                            this.assignTaskToWorker((Worker) this.workerGroupListIt.next(),
                                t);
                            hasAddedTask = true;
                        } catch (Exception e) {
                            logger.info("A worker is down");
                            this.taskProviderQueue.addTask(t);
                        }
                    } else if ((this.freeWorkerList.size() > 0)) {
                        try {
                            this.assignTaskToWorker((Worker) this.freeWorkerList.remove(
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
                    int index = ProActive.waitForAny(this.futureTaskList, 1000);
                    backupCounter++;
                    Result currentResult = (Result) this.futureTaskList.remove(index);
                    this.taskProviderQueue.addResult(currentResult);
                    this.pendingTaskList.remove(index);
                    Worker freeWorker = (Worker) this.workingWorkerList.remove(index);
                    if (this.taskProviderQueue.hasNext().booleanValue()) {
                        Task t1 = this.taskProviderQueue.next();
                        try {
                            this.assignTaskToWorker(freeWorker, t);
                        } catch (Exception e) {
                            logger.info("A worker is down");
                            this.taskProviderQueue.addTask(t1);
                        }
                    } else {
                        this.freeWorkerList.add(freeWorker);
                    }
                    if (logger.isInfoEnabled()) {
                        logger.info(currentResult);

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
                	if (e instanceof NullPointerException) {
                		e.printStackTrace();
                		return null;
                	}
                	logger.info("Manager is waiting for result: "+e);
                    reallocCounter++;
                    // Reallocating tasks
                    if (enableRealloc && !this.freeWorkerList.isEmpty() &&
                            (reallocCounter == 60)) {
                        reallocCounter = 0;
                        for (int i = 0; i < this.workingWorkerList.size();
                                i++) {
                            Worker current = (Worker) this.workingWorkerList.get(i);
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
        return this.rootTask.gather((Result[]) this.taskProviderQueue.getAllResults()
                                                                     .toArray(new Result[this.taskProviderQueue.howManyResults()
                                                                                                               .intValue()]));
    }

    public Result start(Task rootTask) {
        this.taskProviderQueue.reset();

        this.workerGroup.reset();

        try {
            this.rootTask = (Task) ProActive.turnActive(rootTask,
                    ProActive.getBodyOnThis().getNodeURL());
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
        Vector subTaskList = this.rootTask.split();
        logger.info("The ROOT task sends " + subTaskList.size());
        this.taskProviderQueue.addAll(subTaskList);

        return ((Manager) ProActive.getStubOnThis()).start();
    }

    public Result start(InputStream task, InputStream result) {
        this.loadTasks(task);
        this.loadResults(result);
        return ((Manager) ProActive.getStubOnThis()).start();
    }

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

    private void loadTasks(InputStream taskFile) {
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

    private void loadResults(InputStream resultFile) {
        this.taskProviderQueue.loadResults(resultFile);
    }
}
