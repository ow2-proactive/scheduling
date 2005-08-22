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
import java.util.Vector;


/**
 * This is the root class of all our API <code>Task</code> classes.
 *
 * @author Alexandre di Costanzo
 *
 * Created on May 2, 2005
 */
public abstract class Task implements Serializable {

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
     * Create a new tasks with given params.
     * @param params the params of tasks.
     */
    public Task(Object[] params) {
        this.params = params;
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
     * Called after the creation of the active object <code>Task</code>. This
     * method is executed before the <code>execute</code> method. By default, it
     * does nothing. You can override this method to do some jobs <b>before</b>
     * executing the task.
     */
    public void initialization() {
        // nothing to do
    }

    /**
     *
     * @return the computed result of this task.
     */
    public abstract Result execute();

    /**
     * Called after the <code>execute</code> method. By default, it does
     * nothing. You can override this method to do some jobs <b>after</b>
     * executing the task.
     */
    public void finalization() {
        // nothing to do
    }

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
}
