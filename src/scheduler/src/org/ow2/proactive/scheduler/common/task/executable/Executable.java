/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
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
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.common.task.executable;

import java.io.Serializable;

import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.util.SchedulerDevLoggers;


/**
 * Extends this class if you want to create your own task.<br>
 * Executable is the superclass of every personal executable task that will be scheduled.<br>
 * Some classes are provide like :<ul>
 * <li>{@link JavaExecutable} : to make your own java task.</li>
 * <li>{@link ProActiveExecutable} : to make your own ProActive application task.</li></ul><br>
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
        this.killed = true;
    }

    /**
     * Returns true if the task has been killed, false otherwise.
     * 
     * @return true if the task has been killed, false otherwise.
     */
    public boolean isKilled() {
        return this.killed;
    }

}
