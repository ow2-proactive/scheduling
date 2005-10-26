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
package org.objectweb.proactive.branchnbound.core.queue;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.objectweb.proactive.branchnbound.core.Result;
import org.objectweb.proactive.branchnbound.core.Task;
import org.objectweb.proactive.branchnbound.core.exception.NoResultsException;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.util.wrapper.BooleanMutableWrapper;
import org.objectweb.proactive.core.util.wrapper.IntMutableWrapper;


public abstract class TaskQueue implements Serializable {
    public final static Logger logger = ProActiveLogger.getLogger(Loggers.P2P_SKELETONS_MANAGER);
    private Result bestCurrentResult;

    public abstract void addAll(Collection tasks);

    public abstract IntMutableWrapper size();

    public abstract BooleanMutableWrapper hasNext();

    public abstract Task next();

    public abstract void flushAll();

    public abstract BooleanMutableWrapper isHungry();

    public abstract void setHungryLevel(int level);

    public abstract void backupTasks(Task rootTask, Vector pendingTasks,
        OutputStream backupOutputStream);

    public abstract void loadTasks(InputStream taskFile);

    public abstract Task getRootTaskFromBackup();

    public abstract Collection getPendingTasksFromBackup();

    public abstract void reset();

    // --------------------------------------------------------------------------
    // Managing results
    // --------------------------------------------------------------------------
    public abstract void addResult(Result result);

    public abstract IntMutableWrapper howManyResults();

    public abstract Collection getAllResults();

    public abstract void backupResults(OutputStream backupResultFile);

    public abstract void loadResults(InputStream backupResultFile);

    public abstract void addTask(Task t);

    public void informNewBestResult(Result newBest) {
        if ((this.bestCurrentResult == null) ||
                newBest.isBetterThan(this.bestCurrentResult)) {
            this.bestCurrentResult = newBest;
            if (logger.isInfoEnabled()) {
                logger.info("A new best result was found: " +
                    this.bestCurrentResult);
            }
        }
    }

    public Result getBestCurrentResult() {
        if (this.bestCurrentResult != null) {
            return this.bestCurrentResult;
        }
        return new Result(new NoResultsException());
    }
}
