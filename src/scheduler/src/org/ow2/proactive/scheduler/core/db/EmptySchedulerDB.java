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
package org.ow2.proactive.scheduler.core.db;

import java.util.List;

import org.ow2.proactive.scheduler.common.job.JobEvent;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.task.TaskEvent;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.job.InternalJob;


/**
 * @author The ProActive Team
 */
public class EmptySchedulerDB extends AbstractSchedulerDB {

    /**
     * @see org.ow2.proactive.scheduler.core.db.AbstractSchedulerDB#addJob(org.ow2.proactive.scheduler.job.InternalJob)
     */
    @Override
    public boolean addJob(InternalJob internalJob) {
        return true;
    }

    /**
     * @see org.ow2.proactive.scheduler.core.db.AbstractSchedulerDB#addTaskResult(org.ow2.proactive.scheduler.common.task.TaskResult)
     */
    @Override
    public boolean addTaskResult(TaskResult taskResult) {
        return true;
    }

    /**
     * @see org.ow2.proactive.scheduler.core.db.AbstractSchedulerDB#disconnect()
     */
    @Override
    public void disconnect() {
    }

    /**
     * @see org.ow2.proactive.scheduler.core.db.AbstractSchedulerDB#getJobResult()
     */
    @Override
    public JobResult getJobResult(JobId jobId) {
        return null;
    }

    /**
     * @see org.ow2.proactive.scheduler.core.db.AbstractSchedulerDB#getRecoverableState()
     */
    @Override
    public RecoverableState getRecoverableState() {
        return null;
    }

    /**
     * @see org.ow2.proactive.scheduler.core.db.AbstractSchedulerDB#getTaskResult()
     */
    @Override
    public TaskResult getTaskResult(TaskId taskId) {
        return null;
    }

    /**
     * @see org.ow2.proactive.scheduler.core.db.AbstractSchedulerDB#removeJob(org.ow2.proactive.scheduler.common.job.JobId)
     */
    @Override
    public boolean removeJob(JobId jobId) {
        return true;
    }

    /**
     * @see org.ow2.proactive.scheduler.core.db.AbstractSchedulerDB#setJobAndTasksEvents(org.ow2.proactive.scheduler.common.job.JobEvent,
     *      java.util.List)
     */
    @Override
    public boolean setJobAndTasksEvents(JobEvent jobEvent, List<TaskEvent> tasksEvents) {
        return true;
    }

    /**
     * @see org.ow2.proactive.scheduler.core.db.AbstractSchedulerDB#setJobEvent(org.ow2.proactive.scheduler.common.job.JobEvent)
     */
    @Override
    public boolean setJobEvent(JobEvent jobEvent) {
        return true;
    }

    /**
     * @see org.ow2.proactive.scheduler.core.db.AbstractSchedulerDB#setTaskEvent(org.ow2.proactive.scheduler.common.task.TaskEvent)
     */
    @Override
    public boolean setTaskEvent(TaskEvent taskEvent) {
        return true;
    }

    /**
     * @see org.ow2.proactive.scheduler.core.db.AbstractSchedulerDB#getURL()
     */
    @Override
    public String getURL() {
        return null;
    }

    /**
     * @see org.ow2.proactive.scheduler.core.db.AbstractSchedulerDB#delete()
     */
    @Override
    public void delete() {
    }
}
