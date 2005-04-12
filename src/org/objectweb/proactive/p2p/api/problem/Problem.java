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
package org.objectweb.proactive.p2p.api.problem;

import java.io.Serializable;

import org.objectweb.proactive.p2p.api.worker.Worker;


/**
 * Implement this class to solve a divide&conquer problem.
 *
 * @author Alexandre di Costanzo
 *
 */
public abstract class Problem implements Serializable {
    private Result result = null;
    protected Worker worker = null;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------
    public Problem() {
    }

    public Problem(Worker worker) {
        this.worker = worker;
    }

    // -------------------------------------------------------------------------
    // Abstract methods
    // -------------------------------------------------------------------------

    /**
     * Work of the task. It's like the run method of a thread.
     * @param params
     * @return Return the result of this task.
     */
    public abstract Result execute(Object[] params);

    /**
     * To split this task in a daughter task.
     * @return A second Problem.
     */
    public abstract Problem split();

    /**
     * To split this task in <code>n</code> daughters tasks.
     * @param n Number of daughters tasks.
     * @return Resturn n Tasks.
     */
    public Problem[] split(int n) {
        Problem[] problems = new Problem[n];
        for (int i = 0; i < n; i++)
            problems[i] = this.split();
        return problems;
    }

    /**
     * To merge all results from daughters tasks when they finish their works.
     * @param results All results.
     * @return Return a merged result.
     */
    public abstract Result gather(Result[] results);

    /**
     * If retun 1 <code>split()</code> is called also <code>split(n)</code> with
     * n is the return is called. To do nothing please return 0.
     * @return Return the number of task you want to create.
     */
    public abstract int shouldSplit();

    // -------------------------------------------------------------------------
    // Getters and Setters
    // -------------------------------------------------------------------------

    /**
     * @return Returns the result.
     */
    public Result getCurrentResult() {
        return this.result;
    }

    /**
     * @param worker
     */
    public void setWorker(Worker worker) {
        this.worker = worker;
    }
}
