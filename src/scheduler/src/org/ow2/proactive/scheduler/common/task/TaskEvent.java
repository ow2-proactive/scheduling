/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
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
package org.ow2.proactive.scheduler.common.task;

import java.io.Serializable;

import org.objectweb.proactive.annotation.PublicAPI;
import org.ow2.proactive.scheduler.common.job.JobEvent;
import org.ow2.proactive.scheduler.common.job.JobId;


/**
 * Informations about the task that is able to change.<br>
 * These informations are not in the {@link Task} class in order to permit
 * the scheduler listener to send this class as event.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
@PublicAPI
public interface TaskEvent extends Serializable {

    /**
     * To get the jobEvent
     *
     * @return the jobEvent
     */
    public JobEvent getJobEvent();

    /**
     * To get the finishedTime
     *
     * @return the finishedTime
     */
    public long getFinishedTime();

    /**
     * To get the jobId
     *
     * @return the jobId
     */
    public JobId getJobId();

    /**
     * To get the startTime
     *
     * @return the startTime
     */
    public long getStartTime();

    /**
     * To get the taskId
     *
     * @return the taskId
     */
    public TaskId getTaskId();

    /**
     * To get the taskState
     *
     * @return the taskState
     */
    public TaskState getStatus();

    /**
     * To get the executionHostName
     *
     * @return the executionHostName
     */
    public String getExecutionHostName();

    /**
     * Get the number of execution left.
     *
     * @return the number of execution left.
     */
    public int getNumberOfExecutionLeft();

    /**
     * Get the numberOfExecutionOnFailureLeft value.
     * 
     * @return the numberOfExecutionOnFailureLeft value.
     */
    public int getNumberOfExecutionOnFailureLeft();

}
