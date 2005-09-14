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

import java.io.Serializable;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.branchnbound.core.exception.NoResultsException;
import org.objectweb.proactive.branchnbound.core.queue.TaskQueue;
import org.objectweb.proactive.core.group.Group;
import org.objectweb.proactive.core.group.ProActiveGroup;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * @author Alexandre di Costanzo
 *
 * Created on Apr 25, 2005
 */
public class Worker implements Serializable {
    protected static Logger logger = ProActiveLogger.getLogger(Loggers.P2P_SKELETONS_WORKER);
    private Worker workerGroup = null;
    private Result bestCurrentResult = null;
    private TaskQueue taskProvider = null;
    private String workerNodeUrl = null;
    private String currentTaskTag;
    private Group workerGroupCollection = null;

    /**
     * The active object empty constructor
     */
    public Worker() {
        // The emplty constructor
    }

    /**
     * Construct a new Worker with its name.
     * @param name the Worker's name.
     */
    public Worker(TaskQueue taskProvider) {
        this.taskProvider = taskProvider;
        logger.debug("Worker successfully created");
    }

    /**
     * @param task
     * @return the result or a result with the exception.
     */
    public Result execute(Task task) {
        Exception exception = null;
        Task activedTask = null;
        try {
            // Activing the task
            if (this.workerNodeUrl == null) {
                this.workerNodeUrl = ProActive.getBodyOnThis().getNodeURL();
            }
            activedTask = (Task) ProActive.turnActive(ProActive.getFutureValue(
                        task), workerNodeUrl);
            activedTask.setWorker((Worker) ProActive.getStubOnThis());
        } catch (ActiveObjectCreationException e) {
            logger.fatal("Couldn't actived the task", e);
            exception = e;
        } catch (NodeException e) {
            logger.fatal("A problem with the task's node", e);
            exception = e;
        }
        if (activedTask != null) {
            this.currentTaskTag = activedTask.getTag();
            activedTask.initLowerBound();
            activedTask.initUpperBound();

            return activedTask.execute();
        } else {
            logger.fatal("The task was not actived");
            if (exception == null) {
                return new Result(new NoResultsException());
            } else {
                return new Result(exception);
            }
        }
    }

    public void setWorkerGroup(Worker workerGroup) {
        this.workerGroupCollection = ProActiveGroup.getGroup(workerGroup);
        if (!this.workerGroupCollection.remove(ProActive.getStubOnThis()) &&
                (this.workerGroupCollection.size() > 0)) {
            this.bestCurrentResult = ((Worker) this.workerGroupCollection.get(0)).getBestCurrentResult();
        }
        this.workerGroup = workerGroup;
    }

    /**
     * Update the best local result with the new one. If the new best result is no the best,
     * nothing is did.
     * @param newBest a new best result.
     */
    public void setBestCurrentResult(Result newBest) {
        if (this.bestCurrentResult == null) {
            this.bestCurrentResult = newBest;
            if (this.workerGroup != null) {
                this.workerGroup.informNewBestResult(this.bestCurrentResult);
            }
            logger.info("A new best result was localy found: " +
                this.bestCurrentResult);
        } else if (newBest.isBetterThan(this.bestCurrentResult)) {
            this.bestCurrentResult = newBest;
            if (this.workerGroup != null) {
                this.workerGroup.informNewBestResult(this.bestCurrentResult);
            }
            logger.info(
                "A new best result was localy found and inform others: " +
                this.bestCurrentResult);
        }
    }

    /**
     * @return the local best result.
     */
    public Result getBestCurrentResult() {
        if (this.bestCurrentResult == null) {
            return new Result((Exception) new NoResultsException());
        } else {
            return this.bestCurrentResult;
        }
    }

    public void informNewBestResult(Result newBest) {
        if ((this.bestCurrentResult == null) ||
                newBest.isBetterThan(this.bestCurrentResult)) {
            this.bestCurrentResult = newBest;
            logger.info("I was informed from a new remote best result: " +
                this.bestCurrentResult);
        }
    }

    public void sendSubTasksToTheManager(Vector subTaskList) {
        for (int i = 0; i < subTaskList.size(); i++) {
            Task current = (Task) subTaskList.get(i);
            current.setTag(this.currentTaskTag + "-" + i);
        }
        logger.info("The task sends " + subTaskList.size() + " sub tasks");
        this.taskProvider.addAll(subTaskList);
    }

    public void addMember(Worker newWorker) {
        this.workerGroupCollection.add(newWorker);
    }
}
