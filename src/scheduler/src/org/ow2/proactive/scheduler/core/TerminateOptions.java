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
 * $ACTIVEEON_INITIAL_DEV$
 */
package org.ow2.proactive.scheduler.core;

/**
 * TerminateOptions is used to give options to the terminate task method in SchedulerCore.
 * A task can be :
 * <ul>
 * 	<li>normal : default value.(like restarted but using default system delay) </li>
 * 	<li>preempted : execution stopped. Will be re-scheduled after the given delay.</li>
 * 	<li>restarted : execution terminated. Will be re-scheduled after the given delay.<br/>
 * 					Task number of execution left is decreased.</li>
 *  <li>killed : execution stopped, task is killed, no re-execution will be scheduled.</li>
 * </ul>
 * Use provided method to filter the option.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 3.0
 */
final class TerminateOptions {

    /** Used as default, normal scheduling behavior */
    static int NORMAL = 1 << 0;
    /**
     * Use this value to preempt a task, ie. terminate it without any effect.
     * The task will be restarted after the default or given delay.
     */
    static int PREEMPT = 1 << 1;
    /**
     * Use this value to restart a task, ie. terminate it but like it was terminated normally.
     * It terminates the task with an exception, if max number of execution is reached,
     * the task won't be restarted wand will be faulty. If not, number of execution left will be increased.
     * The task will be restarted after the default or given delay.
     */
    static int RESTART = 1 << 2;
    /**
     * Use this value to kill a task, ie. terminate task and do not re-execute it.
     * The task will be faulty without conditions.
     * The delay is useless with this option.
     */
    static int KILL = 1 << 3;

    /** Several options can be stored in this integer */
    private int options;

    /** Restart task delay */
    private int restartDelay;

    /**
     * Create a new instance of TerminateOptions
     * This instance is a normal termination instance.
     *
     * To be used when node call terminate on core.
     */
    TerminateOptions() {
        this.options = NORMAL;
    }

    /**
     * Create a new instance of TerminateOptions with the given options.
     *
     * @param options
     */
    TerminateOptions(int options) {
        this.options = options;
    }

    /**
     * Create a new instance of TerminateOptions with the given options and restart delay.
     *
     * @param options options to be used
     * @param restartDelay time to wait before restarting the task (in sec)
     */
    TerminateOptions(int options, int restartDelay) {
        this(options);
        this.restartDelay = restartDelay;
    }

    /**
     * Return the delay (in sec) associated with this options
     *
     * @return the delay (in sec) associated with this options
     */
    int getDelay() {
        return this.restartDelay;
    }

    /**
     * Return the options value
     *
     * @return the options value
     */
    int getOptions() {
        return this.options;
    }

    /**
     * Return true if the options represents a normal termination.
     * A normal termination is a call to terminate method by a worker node (taskLauncher).
     *
     * @param options the options to check
     * @return true if the options represents a normal termination.
     */
    boolean isNormalTermination() {
        return (options & NORMAL) > 0;
    }

    /**
     * Return true if the options represents a normal restart.
     * A normal restart is a restart that will increase execution count.
     *
     * @param options the options to check
     * @return true if the options represents a normal restart.
     */
    boolean isNormalRestart() {
        return (options & (NORMAL | RESTART)) > 0;
    }

    /**
     * Return true if the options represents a kill termination.
     * A kill termination will kill the task and won't restart it.
     *
     * @param options the options to check
     * @return true if the options represents a kill termination.
     */
    boolean isKilled() {
        return (options & KILL) > 0;
    }

    /**
     * Return true if the options represents a kill termination.
     * A kill termination will kill the task and won't restart it.
     *
     * @param options the options to check
     * @return true if the options represents a kill termination.
     */
    boolean isPreempt() {
        return (options & PREEMPT) > 0;
    }

    /**
     * Return true if the options can represents a termination with delay.
     *
     * @param options the options to check
     * @return true if the options can represents a termination with delay.
     */
    boolean hasDelay() {
        return (options & (PREEMPT | RESTART)) > 0;
    }

}
