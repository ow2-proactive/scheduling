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
package org.objectweb.proactive.extra.scheduler.task;

import org.objectweb.proactive.extra.scheduler.common.task.ExecutableTask;


/**
 * This is the execution entry point for the native task.
 * The execute(TaskResult...) method will be override by the scheduler to launch the native process.
 * This class provide a getProcess method that will return the current running native process.
 *
 * @author ProActive Team
 * @version 1.0, Aug 21, 2007
 * @since ProActive 3.2
 */
public abstract class ExecutableNativeTask implements ExecutableTask {

    /**
     * Return the current nativ running process.
     * It is used by the scheduler to allow it to kill the process.
     *
     * @return the current nativ running process.
     */
    public abstract Process getProcess();
}
