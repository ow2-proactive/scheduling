/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.common.task.executable;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.objectweb.proactive.annotation.PublicAPI;
import org.ow2.proactive.scheduler.common.task.TaskResult;


/**
 * Extends this class if you want to create your own task.<br>
 * Executable is the superclass of every personal executable task that will be scheduled.<br>
 * Each java executable and native executable may have to implements the {@link #execute(TaskResult...)} method.<br>
 * Only the ProActive executable will implement its own execute.
 * In this last case, this {@link #execute(TaskResult...)} method will be forgot.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
@PublicAPI
public abstract class Executable {

    /** Executable state. True if the executable has been killed */
    private volatile boolean killed = false;

    /** execution progress value (between 0 and 100) */
    private final AtomicInteger progress = new AtomicInteger(0);

    private Map<String, Serializable> propagatedVariables;

    /**
     * The content of this method will be executed once after being scheduled.<br>
     * This may generate a result as an {@link Object}. It can be whatever you want.<br>
     * The results list order correspond to the order in the dependence list.
     *
     * @param results the results (as a taskResult) from parent tasks.
     * @throws Throwable any exception thrown by the user's code
     * @return any serializable object from the user.
     */
    public abstract Serializable execute(TaskResult... results) throws Throwable;

    /**
     * Kill executable, terminate preemptively its execution.
     * Should be overridden to kill subprocesses if any. 
     */
    public void kill() {
        this.killed = true; // TODO unsupported
    }

    /**
     * Returns true if the task has been killed, false otherwise.
     * 
     * @return true if the task has been killed, false otherwise.
     */
    public boolean isKilled() {
        return this.killed;
    }

    /*
     * <B>This method has no effect in Scheduling 2.2.0.</B>
     */
    /**
     * Set the progress value for this Executable. Progress value must be ranged
     * between 0 and 100.
     * @param newValue the new progress value
     * @return the previous progress value
     * @throws IllegalArgumentException if the value is not ranged between 0 and 100.
     */
    protected final int setProgress(int newValue) throws IllegalArgumentException {
        if (newValue < 0 || newValue > 100) {
            throw new IllegalArgumentException("Progress value must be ranged between 0 and 100");
        }
        return this.progress.getAndSet(newValue);
    }

    /*
     * <B>This method always returns 0 in Scheduling 2.2.0</B>
     */
    /**
     * Return the current progress value for this executable, ranged between 0 and 100.
     *
     * @return the current progress value for this executable.
     */
    public int getProgress() {
        return this.progress.get();
    }

    public Map<String, Serializable> getVariables() {
        return this.propagatedVariables;
    }

    public void setVariables(Map<String, Serializable> propagatedVariables) {
        this.propagatedVariables = propagatedVariables;
    }

}
