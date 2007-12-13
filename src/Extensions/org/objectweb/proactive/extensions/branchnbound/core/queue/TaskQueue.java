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
package org.objectweb.proactive.extensions.branchnbound.core.queue;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.util.wrapper.BooleanMutableWrapper;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.core.util.wrapper.IntMutableWrapper;
import org.objectweb.proactive.extensions.branchnbound.core.Result;
import org.objectweb.proactive.extensions.branchnbound.core.Task;
import org.objectweb.proactive.extensions.branchnbound.core.exception.NoResultsException;


/**
 * <p>This class must be extended by all task queues.</p>
 * <p>It contains and describes all methods needed by the API for handling task
 * allocation, managing results, and backuping / restoring computation.</p>
 *
 * @author Alexandre di Costanzo
 *
 * Created on Nov 3, 2005
 */
public abstract class TaskQueue implements Serializable {

    /** The logger. */
    public final static Logger logger = ProActiveLogger.getLogger(Loggers.P2P_SKELETONS_MANAGER);

    /** Keeping a copy of the best current solution. */
    private Result bestCurrentResult;

    /**
     * Adding a set of tasks in the queue.
     * Usually a set of sub-tasks from a task which is computing.
     * @param tasks a <code>Collection</code> of <code>Task</code> classes.
     */
    public abstract void addAll(Collection<Task> tasks);

    /**
     * @return the number of tasks not yet computed.
     */
    public abstract IntMutableWrapper size();

    /**
     * @return <code>true</code> if the queue has more tasks not computed.
     */
    public abstract BooleanMutableWrapper hasNext();

    /**
     * Return the next task to be computed, and remove it from the queue.
     * @return the next task to be computed.
     */
    public abstract Task next();

    /**
     * Empty the queue for a new computation.
     */
    public abstract void flushAll();

    /**
     * @return <code>true</code> if the queue has reached the hungry level.
     * @see TaskQueue#setHungryLevel(int)
     */
    public abstract BooleanWrapper isHungry();

    /**
     * Set the hungry level for this queue.
     * @param level the starving task level.
     * @see TaskQueue#isHungry()
     */
    public abstract void setHungryLevel(int level);

    /**
     * Write all tasks for backuping.
     * @param rootTask the root task.
     * @param pendingTasks the pending tasks.
     * @param backupOutputStream the stream for backuping.
     */
    public abstract void backupTasks(Task rootTask, Vector<Task> pendingTasks, OutputStream backupOutputStream);

    /**
     * Restoring all tasks from a previous backup.
     * @param taskFile the stream for restoring.
     */
    public abstract void loadTasks(InputStream taskFile);

    /**
     * Restore the root task. The <code>loadTasks(InputStream)</code> must be
     * called before.
     * @return the root task from a restore.
     * @see #loadTasks(InputStream)
     */
    public abstract Task getRootTaskFromBackup();

    /**
     * Add a found result for the final gather.
     * @param result the found result.
     */
    public abstract void addResult(Result result);

    /**
     * @return the current total of result found.
     */
    public abstract IntMutableWrapper howManyResults();

    /**
     * @return a <code>Collection</code> with all current found results.
     */
    public abstract Collection<Result> getAllResults();

    /**
     * Backuping in a stream all current found results.
     * @param backupResultFile the stream for backuping.
     */
    public abstract void backupResults(OutputStream backupResultFile);

    /**
     * Restoring results from a backup.
     * @param backupResultFile the stream for restoring.
     */
    public abstract void loadResults(InputStream backupResultFile);

    /**
     * Add a task for computing in the queue.
     * @param t the task to be computed.
     */
    public abstract void addTask(Task t);

    /**
     * <p><b>***FOR INTERNAL USE ONLY***</b></p>
     * Inform the queue of the best current solution.
     * @param newBest the best current solution.
     */
    public void informNewBestResult(Result newBest) {
        if ((this.bestCurrentResult == null) || newBest.isBetterThan(this.bestCurrentResult)) {
            this.bestCurrentResult = newBest;
            if (logger.isInfoEnabled()) {
                logger.info("A new best result was found: " + this.bestCurrentResult);
            }
        }
    }

    /**
     * @return the best current solution.
     */
    public Result getBestCurrentResult() {
        if (this.bestCurrentResult != null) {
            return this.bestCurrentResult;
        }
        return new Result(new NoResultsException());
    }
}
