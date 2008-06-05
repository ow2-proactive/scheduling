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
package org.objectweb.proactive.extensions.masterworker.core;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.annotation.Cache;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.extensions.masterworker.interfaces.DivisibleTask;
import org.objectweb.proactive.extensions.masterworker.interfaces.WorkerMemory;
import org.objectweb.proactive.extensions.masterworker.interfaces.internal.TaskIntern;
import org.objectweb.proactive.extensions.masterworker.interfaces.internal.Worker;
import org.objectweb.proactive.extensions.masterworker.interfaces.internal.WorkerMaster;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;


/**
 * <i><font size="-1" color="#FF0000">**For internal use only** </font></i><br>
 * The Workers Active Objects are the workers in the Master/Worker API.<br>
 * They execute tasks needed by the master
 *
 * @author The ProActive Team
 */
public class AOWorker implements InitActive, Serializable, Worker {

    /** log4j logger of the worker */
    protected static final Logger logger = ProActiveLogger.getLogger(Loggers.MASTERWORKER_WORKERS);
    protected static final boolean debug = logger.isDebugEnabled();

    /** stub on this active object */
    protected AOWorker stubOnThis;

    /** Name of the worker */
    protected String name;

    /** The entity which will provide tasks to the worker (i.e. the master) */
    protected WorkerMaster provider;

    protected Map<String, Serializable> initialMemory;
    protected transient WorkerMemory memory;

    /** The current list of tasks to compute */
    protected Queue<TaskIntern<Serializable>> pendingTasks;
    protected Queue<Queue<TaskIntern<Serializable>>> pendingTasksFutures;

    private long subWorkerNameCounter = 0;

    /** ProActive no arg contructor */
    public AOWorker() {
    }

    /**
     * Creates a worker with the given name
     *
     * @param name          name of the worker
     * @param provider      the entity which will provide tasks to the worker
     * @param initialMemory initial memory of the worker
     */
    public AOWorker(final String name, final WorkerMaster provider,
            final Map<String, Serializable> initialMemory) {
        this.name = name;
        this.provider = provider;
        this.memory = new WorkerMemoryImpl(initialMemory);
        this.initialMemory = initialMemory;
        this.pendingTasksFutures = new LinkedList<Queue<TaskIntern<Serializable>>>();
        this.pendingTasks = new LinkedList<TaskIntern<Serializable>>();
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(final Object obj) {
        return (obj instanceof AOWorker) && name.equals(((Worker) obj).getName());
    }

    /** {@inheritDoc} */
    @Cache
    public String getName() {
        return name;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return name.hashCode();
    }

    /** {@inheritDoc} */
    public BooleanWrapper heartBeat() {
        // we want the rendez-vous to work
        // But we need as well an answer for synchronization
        return new BooleanWrapper(true);
    }

    /** {@inheritDoc} */
    public void initActivity(final Body body) {
        stubOnThis = (AOWorker) PAActiveObject.getStubOnThis();
        PAActiveObject.setImmediateService("heartBeat");
        PAActiveObject.setImmediateService("terminate");

        // Initial Task
        stubOnThis.getTaskAndSchedule();
    }

    /** gets the initial task to solve */
    @SuppressWarnings("unchecked")
    public void getTasks() {
        if (debug) {
            logger.debug(name + " asks a new task...");
        }

        // InitialTask
        Queue<TaskIntern<Serializable>> newTasks = (Queue<TaskIntern<Serializable>>) PAFuture
                .getFutureValue(provider.getTasks((Worker) stubOnThis, name, true));

        if (debug) {
            logger.debug(name + " received " + newTasks.size() + " tasks.");
        }
        pendingTasks.addAll(newTasks);

    }

    /** gets the initial task to solve */
    @SuppressWarnings("unchecked")
    public void getTaskAndSchedule() {
        // We get some tasks
        getTasks();

        // We schedule the execution
        stubOnThis.scheduleTask();
    }

    /**
     * Handle a task (run it)
     *
     * @param task task to run
     */
    public void handleTask(final TaskIntern<Serializable> task) {

        // if the task is a divisible one, we spawn a new specialized worker for it

        if (task.getTask() instanceof DivisibleTask) {
            try {
                Worker spawnedWorker = (Worker) PAActiveObject.newActive(AODivisibleTaskWorker.class
                        .getName(), new Object[] { name + "_" + subWorkerNameCounter, provider,
                        initialMemory, name, task });
                subWorkerNameCounter = (subWorkerNameCounter + 1) % (Long.MAX_VALUE - 1);
            } catch (ActiveObjectCreationException e) {
                e.printStackTrace();
            } catch (NodeException e) {
                e.printStackTrace();
            }
            // We send the result back to the master
            Queue<TaskIntern<Serializable>> newTasks;
            if ((pendingTasks.size() == 0) && (pendingTasksFutures.size() == 0)) {
                if (debug) {
                    logger.debug(name + " requests a task flooding...");
                }
                newTasks = provider.getTasks(stubOnThis, name, true);
            } else {
                newTasks = provider.getTasks(stubOnThis, name, false);
            }
            pendingTasksFutures.offer(newTasks);
        } else {
            Serializable resultObj = null;
            ResultInternImpl result = new ResultInternImpl(task);

            // We run the task and listen to exception thrown by the task itself
            try {
                if (debug) {
                    logger.debug(name + " runs task " + task.getId() + "...");
                }

                resultObj = task.run(memory);

            } catch (Exception e) {
                result.setException(e);
            }

            // We store the result inside our internal version of the task
            result.setResult(resultObj);
            if (debug) {
                logger
                        .debug(name + " sends the result of task " + result.getId() +
                            " and asks a new task...");
            }

            // We send the result back to the master
            Queue<TaskIntern<Serializable>> newTasks;
            if ((pendingTasks.size() == 0) && (pendingTasksFutures.size() == 0)) {
                if (debug) {
                    logger.debug(name + " requests a task flooding...");
                }
                newTasks = provider.sendResultAndGetTasks(result, name, true);
            } else {
                newTasks = provider.sendResultAndGetTasks(result, name, false);
            }
            pendingTasksFutures.offer(newTasks);

        }
        // Schedule
        stubOnThis.scheduleTask();
    }

    /** ScheduleTask : find a new task to run */
    public void scheduleTask() {
        while ((pendingTasks.size() == 0) && (pendingTasksFutures.size() > 0)) {
            pendingTasks.addAll(pendingTasksFutures.remove());
        }
        if (pendingTasks.size() > 0) {
            TaskIntern<Serializable> newTask = pendingTasks.remove();
            // We handle the current Task
            stubOnThis.handleTask(newTask);

        } else {
            // if there is nothing to do we ask a last time for a task to the master
            getTasks();
            if (pendingTasks.size() > 0) {
                TaskIntern<Serializable> newTask = pendingTasks.remove();
                // We handle the current Task
                stubOnThis.handleTask(newTask);
            } else {

                if (logger.isDebugEnabled()) {
                    logger.debug(name + " sleeps...");
                }
            }
        }
    }

    /** {@inheritDoc} */
    public BooleanWrapper terminate() {
        if (debug) {
            logger.debug("Terminating " + name + "...");
        }

        PAActiveObject.terminateActiveObject(true);
        if (debug) {
            logger.debug(name + " terminated...");
        }

        return new BooleanWrapper(true);
    }

    /** {@inheritDoc} */
    public void wakeup() {
        if (debug) {
            logger.debug(name + " receives a wake up message...");
        }

        if (pendingTasks.size() == 0) {
            if (debug) {
                logger.debug(name + " wakes up...");
            }
            // Initial Task
            stubOnThis.getTaskAndSchedule();
        } else {
            if (debug) {
                logger.debug(name + " ignored wake up message ...");
            }
        }

    }

    public BooleanWrapper clear() {
        pendingTasks.clear();
        //pendingTasksFutures.clear();
        return new BooleanWrapper(true);
    }
}
