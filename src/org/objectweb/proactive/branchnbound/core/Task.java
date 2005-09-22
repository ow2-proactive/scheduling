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
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * This is the root class of all our API <code>Task</code> classes.
 *
 * @author Alexandre di Costanzo
 *
 * Created on May 2, 2005
 */
public abstract class Task implements Serializable, Comparable {
    protected static Logger logger = ProActiveLogger.getLogger(Loggers.P2P_SKELETONS_MANAGER);

    /**
     * The params of the task.
     */
    protected Object[] params = null;
    protected Result initLowerBound;
    protected Result initUpperBound;
    protected Worker worker = null;

    /**
     * The no arg constructor for ProActive.
     */
    public Task() {
        // nothing to do
    }

    /**
     * Construct a new task with given params and pre-computed bounds.
     * @param params the params of the tasks.
     * @param initLowerBound the lower bound.
     * @param initUpperBound the upper bound.
     */
    public Task(Object[] params, Result initLowerBound, Result initUpperBound) {
        this.params = params;
        this.initLowerBound = initLowerBound;
        this.initUpperBound = initUpperBound;
    }

    /**
     *
     * @return the computed result of this task.
     */
    public abstract Result execute();

    /**
     * Split this task in sub-tasks.
     *
     * @return a collection of tasks.
     */
    public abstract Vector split();

    /**
     * As defined by the user, it returns the best results.
     * @param results an array of results.
     * @return the best user defined result or <code>null</code> if no results was found.
     */
    public Result gather(Result[] results) {
        Result best = null;
        for (int i = 0; i < results.length; i++) {
            Result current = results[i];
            if (best == null) {
                if (current.isAnException()) {
                    continue;
                } else {
                    best = current;
                }
            } else {
                best = best.returnTheBest(current);
            }
        }
        return best;
    }

    /**
     * Compute for the first time the problem lower bound.
     */
    public abstract void initLowerBound();

    /**
     * Compute for the first time the problem upper bound.
     */
    public abstract void initUpperBound();

    /**
     * Associate a worker to this task.
     * @param worker A ProActive Stub on the worker.
     */
    public void setWorker(Worker worker) {
        this.worker = worker;
    }

    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object arg) {
        Task t = (Task) arg;
        if (this.equals(t)) {
            return 0;
        } else if (this.hashCode() > t.hashCode()) {
            return -1;
        } else {
            return 1;
        }
    }
}
