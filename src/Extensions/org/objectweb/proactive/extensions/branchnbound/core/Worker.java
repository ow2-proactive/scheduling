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

import java.io.Serializable;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.api.PAGroup;
import org.objectweb.proactive.core.group.Group;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.extensions.branchnbound.core.exception.NoResultsException;
import org.objectweb.proactive.extensions.branchnbound.core.queue.TaskQueue;


/**
 * <p><b>***FOR INTERNAL USE ONLY</b></p>
 *
 * @author Alexandre di Costanzo
 *
 * Created on Apr 25, 2005
 */
@PublicAPI
public class Worker implements Serializable {
    protected static Logger logger = ProActiveLogger.getLogger(Loggers.P2P_SKELETONS_WORKER);
    private Worker selfWorkerGroup = null;
    private Result bestCurrentResult = null;
    private TaskQueue taskProvider = null;
    private String workerNodeUrl = null;
    private Task currentTask = null;

    /**
     * The active object empty constructor.
     */
    public Worker() {
        // The emplty constructor
    }

    /**
     * Construct a new worker with a reference on a task queue.
     * @param taskProvider this task queue.
     */
    public Worker(TaskQueue taskProvider) {
        this.taskProvider = taskProvider;
    }

    /**
     * Start the computation of the given task.
     * @param task the task to compute.
     * @return the result or a result with the exception.
     */
    public Result execute(Task task) {
        if (this.bestCurrentResult == null) {
            this.bestCurrentResult = this.taskProvider.getBestCurrentResult();
        }
        Exception exception = null;
        Task activedTask = null;
        try {
            // Activing the task
            if (this.workerNodeUrl == null) {
                this.workerNodeUrl = PAActiveObject.getBodyOnThis().getNodeURL();
            }
            activedTask = (Task) PAActiveObject.turnActive(PAFuture.getFutureValue(task), workerNodeUrl);
            activedTask.setWorker((Worker) PAActiveObject.getStubOnThis());
            this.currentTask = activedTask;
            this.currentTask.setImmediateServices();
        } catch (ActiveObjectCreationException e) {
            logger.fatal("Couldn't actived the task", e);
            exception = e;
        } catch (NodeException e) {
            logger.fatal("A problem with the task's node", e);
            exception = e;
        } catch (Exception e) {
            logger.fatal("Failed immediate service", e);
            exception = e;
        }

        if (this.bestCurrentResult.getException() != null) {
            this.bestCurrentResult = null;
        }
        if (activedTask != null) {
            activedTask.setBestKnownSolution(this.bestCurrentResult.getSolution());
            activedTask.initLowerBound();
            activedTask.initUpperBound();

            return activedTask.execute();
        }
        logger.fatal("The task was not actived");
        if (exception == null) {
            return new Result(new NoResultsException("The task was not actived"));
        }
        return new Result(exception);
    }

    /**
     * Set the group of this worker.
     * @param workerGroup the group of workers.
     */
    public void setWorkerGroup(Worker workerGroup) {
        Group<Worker> group = PAGroup.getGroup(workerGroup);
        group.remove(PAActiveObject.getStubOnThis());
        this.selfWorkerGroup = workerGroup;
    }

    /**
     * Update the best local result with the new one. If the new best result is no the best,
     * nothing is did.
     * @param newBest a new best result.
     */
    public void setBestCurrentResult(Result newBest) {
        if ((this.bestCurrentResult == null) || newBest.isBetterThan(this.bestCurrentResult)) {
            this.bestCurrentResult = newBest;
            if (this.selfWorkerGroup != null) {
                this.selfWorkerGroup.informNewBestResult(this.bestCurrentResult);
                this.taskProvider.informNewBestResult(this.bestCurrentResult);
            }
            if (this.currentTask != null) {
                this.currentTask.setBestKnownSolution(this.bestCurrentResult.getSolution());
            }
            logger.debug("A new best result was localy found: " + this.bestCurrentResult);
        }
        logger.debug("The new best result is NOT BETTER");
    }

    /**
     * @return the best local result.
     */
    public Result getBestCurrentResult() {
        if (this.bestCurrentResult == null) {
            return new Result(new NoResultsException());
        }
        return this.bestCurrentResult;
    }

    /**
     * Broadcast the best new localy found solution to all task and to the task
     * queue.
     * @param newBest the best new solution.
     */
    public void informNewBestResult(Result newBest) {
        if ((this.bestCurrentResult == null) || newBest.isBetterThan(this.bestCurrentResult)) {
            this.bestCurrentResult = newBest;
            if (this.currentTask != null) {
                this.currentTask.setBestKnownSolution(this.bestCurrentResult.getSolution());
            }
            if (logger.isInfoEnabled()) {
                logger.info("I was informed from a new remote best result: " + this.bestCurrentResult);
            }
        }
    }

    /**
     * Add a set of sub-task to the task queue.
     * @param subTaskList the set of sub-tasks.
     */
    public void sendSubTasksToTheManager(Vector<Task> subTaskList) {
        if (logger.isDebugEnabled()) {
            logger.debug("The task sends " + subTaskList.size() + " sub tasks");
        }
        this.taskProvider.addAll(subTaskList);
    }

    /**
     * @return <code>true</code> if the task queue needs more tasks.
     */
    public BooleanWrapper isHungry() {
        return this.taskProvider.isHungry();
    }

    /**
     * Stop the current task computation.
     */
    public void immediateStopComputation() {
        this.currentTask.immediateTerminate();
        this.currentTask = null;
    }

    /**
     * @return the current task.
     */
    public Task getCurrentTask() {
        return this.currentTask;
    }

    /**
     * Pinging the current worker.
     */
    public void alive() {
        // nothing to do here
    }

    /**
     * Reset the worker for a new computation.
     */
    public void reset() {
        this.bestCurrentResult = null;
        this.currentTask = null;
    }
}
