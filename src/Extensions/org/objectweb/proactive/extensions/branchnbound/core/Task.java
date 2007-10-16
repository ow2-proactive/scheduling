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
import org.objectweb.proactive.api.ProActiveObject;
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
    protected Result initLowerBound;
    protected Result initUpperBound;
    protected Worker worker = null;
    protected Object bestKnownSolution = null;

    /**
     * The no arg constructor for ProActive.
     */
    public Task() {
        // nothing to do
    }

    /**
     * Set the immediate services for this active object
     */
    public int setImmediateServices() {
        ProActiveObject.setImmediateService("setBestKnownResult");
        ProActiveObject.setImmediateService("immediateTerminate");
        return 0; // for synchronous call
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
    public abstract Vector<Task> split();

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
                }
                best = current;
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

    /**
     * <p><b>***FOR INTERNAL USE ONLY***</b></p>
     * <p>Push the best current solution which is broadcasted in this task.</p>
     * @param newBestKnownResult the best current solution.
     */
    public void setBestKnownSolution(Object newBestKnownResult) {
        if (this.bestKnownSolution != null) {
            synchronized (this.bestKnownSolution) {
                if (((Comparable) this.bestKnownSolution).compareTo(
                            newBestKnownResult) > 0) {
                    this.bestKnownSolution = newBestKnownResult;
                }
            }
        }
    }

    /**
     * Terminate this task.
     */
    public void immediateTerminate() {
        ProActiveObject.terminateActiveObject(true);
    }
}
