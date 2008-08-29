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
package org.ow2.proactive.scheduler.core;

import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.ow2.proactive.scheduler.common.exception.SchedulerException;
import org.ow2.proactive.scheduler.common.job.Job;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobPriority;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.scheduler.SchedulerEvent;
import org.ow2.proactive.scheduler.common.scheduler.SchedulerEventListener;
import org.ow2.proactive.scheduler.common.scheduler.SchedulerInitialState;
import org.ow2.proactive.scheduler.common.scheduler.Stats;
import org.ow2.proactive.scheduler.common.scheduler.UserSchedulerInterface;
import org.ow2.proactive.scheduler.common.task.TaskResult;


/**
 * Scheduler user interface.
 * This class provides method to managed jobs for a user.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
public class UserScheduler implements UserSchedulerInterface {

    /** Scheduler proxy as an active object */
    protected SchedulerFrontend schedulerFrontend;

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.UserSchedulerInterface_#getJobResult(org.ow2.proactive.scheduler.common.job.JobId)
     */
    public JobResult getJobResult(JobId jobId) throws SchedulerException {
        return schedulerFrontend.getJobResult(jobId);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.UserSchedulerInterface_#remove(org.ow2.proactive.scheduler.common.job.JobId)
     */
    public void remove(JobId jobId) throws SchedulerException {
        schedulerFrontend.remove(jobId);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.UserSchedulerInterface#getTaskResult(org.ow2.proactive.scheduler.common.job.JobId, java.lang.String)
     */
    public TaskResult getTaskResult(JobId jobId, String taskName) throws SchedulerException {
        return schedulerFrontend.getTaskResult(jobId, taskName);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.UserSchedulerInterface#submit(org.ow2.proactive.scheduler.common.job.Job)
     */
    public JobId submit(Job job) throws SchedulerException {
        return schedulerFrontend.submit(job);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.UserSchedulerInterface_#listenLog(org.ow2.proactive.scheduler.common.job.JobId, java.lang.String, int)
     */
    public void listenLog(JobId jobId, String hostname, int port) throws SchedulerException {
        schedulerFrontend.listenLog(jobId, hostname, port);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.UserSchedulerInterface#addSchedulerEventListener(org.ow2.proactive.scheduler.common.scheduler.SchedulerEventListener, org.ow2.proactive.scheduler.common.scheduler.SchedulerEvent[])
     */
    public SchedulerInitialState<? extends Job> addSchedulerEventListener(
            SchedulerEventListener<? extends Job> sel, SchedulerEvent... events) throws SchedulerException {
        return schedulerFrontend.addSchedulerEventListener(sel, events);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.UserSchedulerInterface#removeSchedulerEventListener()
     */
    public void removeSchedulerEventListener() throws SchedulerException {
        schedulerFrontend.removeSchedulerEventListener();
    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.UserSchedulerInterface#disconnect()
     */
    public void disconnect() throws SchedulerException {
        schedulerFrontend.disconnect();
    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.UserSchedulerInterface_#pause(org.ow2.proactive.scheduler.common.job.JobId)
     */
    public BooleanWrapper pause(JobId jobId) throws SchedulerException {
        return schedulerFrontend.pause(jobId);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.UserSchedulerInterface_#resume(org.ow2.proactive.scheduler.common.job.JobId)
     */
    public BooleanWrapper resume(JobId jobId) throws SchedulerException {
        return schedulerFrontend.resume(jobId);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.UserSchedulerInterface_#kill(org.ow2.proactive.scheduler.common.job.JobId)
     */
    public BooleanWrapper kill(JobId jobId) throws SchedulerException {
        return schedulerFrontend.kill(jobId);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.UserSchedulerInterface_#changePriority(org.ow2.proactive.scheduler.common.job.JobId, org.ow2.proactive.scheduler.common.job.JobPriority)
     */
    public void changePriority(JobId jobId, JobPriority priority) throws SchedulerException {
        schedulerFrontend.changePriority(jobId, priority);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.UserSchedulerInterface#getStats()
     */
    public Stats getStats() throws SchedulerException {
        return schedulerFrontend.getStats();
    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.UserSchedulerInterface#isConnected()
     */
    public BooleanWrapper isConnected() {
        return schedulerFrontend.isConnected();
    }

}
