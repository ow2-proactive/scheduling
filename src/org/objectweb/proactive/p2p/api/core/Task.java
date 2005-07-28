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

import org.objectweb.proactive.core.util.wrapper.IntWrapper;

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
    public static final int SPLIT_CODE_MAX = 4;
    public static final int SPLIT_CODE_MIN = 1;

    /**
     * The index of this task. Used by the task generator.
     */
    private int index = -1;

    /**
     * Used by the framework manager for the arg value of split methods.
     */
    private int n = -1;

    /**
     * The params of the task.
     */
    private Object[] params = null;

    /**
     * The no arg constructor for ProActive.
     */
    public Task() {
        // nothing to do
    }

    /**
     * Create a new tasks with given params.
     * @param params the params of tasks
     */
    public Task(Object[] params) {
        this.params = params;
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
     * <p>The return value determines which split methods to call.</p>
     *
     * <p>Do not forget to use the method <code>setNForSplit(int n)</code> to set the value
     * of <code>n</code> when the choosed split method will be call by the manager.</p>
     *
     * <p>Code value returns:</p>
     *
     *        <p>1. The default split method: <code> Vector split()</code></p>
     *         <p>2. The split in n method: <code> Vector splitInN(int n)</code></p>
     *         <p>3. The split at most in n method: <code> Vector splitAtMost(in n)</code></p>
     *         <p>4. The split at least in n method: <code> Vector splitAtLeast(int n)</code></p>
     *
     * <p>Other values do not run any split methods.</p>
     *
     * @return the split method to run or not.
     * @see #split()
     * @see #splitAtMost(int)
     * @see #splitAtLeast(int)
     * @see #splitInN(int)
     * @see #setNForSplit(int)
     * @see #SPLIT_CODE_MAX
     * @see #SPLIT_CODE_MIN
     */
    public abstract IntWrapper shouldISplit();

    /**
     * Split this task in sub-tasks.
     *
     * @return a collection of tasks.
     */
    public abstract Vector split();

    /**
     * Split this task in n sub-tasks.
     *
     * @param n the number of asked sub-tasks.
     * @return a collection of n tasks.
     */
    public abstract Vector splitInN(int n);

    /**
     * Split this task at most n sub-tasks.
     * @param n the maximun number of asked sub-tasks.
     * @return a collection of at most n sub-tasks.
     */
    public abstract Vector splitAtMost(int n);

    /**
     * Split this task at least n sub-tasks.
     * @param n the minimun number of required sub-tasks.
     * @return a collection of at least n sub-tasks.
     */
    public abstract Vector splitAtLeast(int n);

    /**
     * <p>Set the n value for the method call of one of split methods as argument.</p>
     * @param n the arguments for splits methods.
     */
    public void setNForSplit(int n) {
        this.n = n;
    }

    /**
     * @return the n computed by the last call to shouldISplit() method.
     * @see #shouldISplit()
     */
    public IntWrapper getNForSplit() {
        return new IntWrapper(this.n);
    }

    /**
     * Set the task index in a collection of tasks.
     * @param index given by the task generator
     */
    public void setTaskManagerIndex(int index) {
        this.index = index;
    }

    /**
     * As defined by the user, it returns the best results.
     * @param results an array of results.
     * @return the best user defined result.
     */
    public abstract Result gather(Result[] results);
}
