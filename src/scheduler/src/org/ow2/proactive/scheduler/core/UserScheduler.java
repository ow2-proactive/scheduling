/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
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
 * If needed, contact us to obtain a release under GPL Version 2 
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.core;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.ow2.proactive.scheduler.common.SchedulerEvent;
import org.ow2.proactive.scheduler.common.SchedulerEventListener;
import org.ow2.proactive.scheduler.common.SchedulerState;
import org.ow2.proactive.scheduler.common.SchedulerStatus;
import org.ow2.proactive.scheduler.common.UserSchedulerInterface;
import org.ow2.proactive.scheduler.common.exception.SchedulerException;
import org.ow2.proactive.scheduler.common.job.Job;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobPriority;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.util.logforwarder.AppenderProvider;
import org.ow2.proactive.utils.Tools;


/**
 * Scheduler user interface.
 * This class provides method to managed jobs for a user.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
public class UserScheduler implements UserSchedulerInterface {

    /**  */
    private static final long serialVersionUID = 200;
    /** Scheduler proxy as an active object */
    public SchedulerFrontend schedulerFrontend;

    /**
     * @see org.ow2.proactive.scheduler.common.UserSchedulerInterface_#getJobResult(org.ow2.proactive.scheduler.common.job.JobId)
     */
    public JobResult getJobResult(JobId jobId) throws SchedulerException {
        return schedulerFrontend.getJobResult(jobId);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.UserSchedulerInterface#getTaskResult(org.ow2.proactive.scheduler.common.job.JobId, java.lang.String)
     */
    public TaskResult getTaskResult(JobId jobId, String taskName) throws SchedulerException {
        return schedulerFrontend.getTaskResult(jobId, taskName);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.UserSchedulerInterface#getJobResult(java.lang.String)
     */
    public JobResult getJobResult(String jobId) throws SchedulerException {
        return schedulerFrontend.getJobResult(jobId);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.UserSchedulerInterface#getTaskResult(java.lang.String, java.lang.String)
     */
    public TaskResult getTaskResult(String jobId, String taskName) throws SchedulerException {
        return schedulerFrontend.getTaskResult(jobId, taskName);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.UserSchedulerInterface_#remove(org.ow2.proactive.scheduler.common.job.JobId)
     */
    public void remove(JobId jobId) throws SchedulerException {
        schedulerFrontend.remove(jobId);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.UserSchedulerInterface#submit(org.ow2.proactive.scheduler.common.job.Job)
     */
    public JobId submit(Job job) throws SchedulerException {
        return schedulerFrontend.submit(job);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.UserSchedulerInterface_#listenLog(org.ow2.proactive.scheduler.common.job.JobId, org.ow2.proactive.scheduler.common.util.logforwarder.AppenderProvider)
     */
    public void listenLog(JobId jobId, AppenderProvider appenderProvider) throws SchedulerException {
        schedulerFrontend.listenLog(jobId, appenderProvider);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.UserSchedulerInterface#listenLog(java.lang.String, org.ow2.proactive.scheduler.common.util.logforwarder.AppenderProvider)
     */
    public void listenLog(String jobId, AppenderProvider appenderProvider) throws SchedulerException {
        schedulerFrontend.listenLog(jobId, appenderProvider);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.UserSchedulerInterface#getSchedulerState()
     */
    public SchedulerState getSchedulerState() throws SchedulerException {
        return schedulerFrontend.getSchedulerState();
    }

    /**
     * @see org.ow2.proactive.scheduler.common.UserSchedulerInterface#getStatus()
     *
     * @deprecated {@link UserScheduler#getSchedulerStatus()}
     */
    @Deprecated
    public SchedulerStatus getStatus() throws SchedulerException {
        return schedulerFrontend.getStatus();
    }

    /**
     * @see org.ow2.proactive.scheduler.common.UserSchedulerInterface#getSchedulerStatus()
     *
     */
    public SchedulerStatus getSchedulerStatus() throws SchedulerException {
        return schedulerFrontend.getSchedulerStatus();
    }

    /**
     * @deprecated {@link UserScheduler#addEventListener(SchedulerEventListener, boolean, SchedulerEvent...)}
     */
    @Deprecated
    public SchedulerState addSchedulerEventListener(SchedulerEventListener sel, boolean myEventsOnly,
            SchedulerEvent... events) throws SchedulerException {
        return schedulerFrontend.addSchedulerEventListener(sel, myEventsOnly, events);
    }

    /**
     * @deprecated {@link UserScheduler#removeEventListener()}
     */
    @Deprecated
    public void removeSchedulerEventListener() throws SchedulerException {
        schedulerFrontend.removeSchedulerEventListener();
    }

    /**
     * @see org.ow2.proactive.scheduler.common.UserSchedulerInterface#addEventListener(org.ow2.proactive.scheduler.common.SchedulerEventListener, boolean, org.ow2.proactive.scheduler.common.SchedulerEvent[])
     */
    public void addEventListener(SchedulerEventListener sel, boolean myEventsOnly, SchedulerEvent... events)
            throws SchedulerException {
        schedulerFrontend.addEventListener(sel, myEventsOnly, events);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.UserSchedulerInterface#addEventListener(org.ow2.proactive.scheduler.common.SchedulerEventListener, boolean, boolean, org.ow2.proactive.scheduler.common.SchedulerEvent[])
     */
    public SchedulerState addEventListener(SchedulerEventListener sel, boolean myEventsOnly,
            boolean getInitialState, SchedulerEvent... events) throws SchedulerException {
        return schedulerFrontend.addEventListener(sel, myEventsOnly, getInitialState, events);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.UserSchedulerInterface#removeEventListener()
     */
    public void removeEventListener() throws SchedulerException {
        schedulerFrontend.removeEventListener();
    }

    /**
     * @see org.ow2.proactive.scheduler.common.UserSchedulerInterface#disconnect()
     */
    public void disconnect() throws SchedulerException {
        schedulerFrontend.disconnect();
    }

    /**
     * @see org.ow2.proactive.scheduler.common.UserSchedulerInterface_#pause(org.ow2.proactive.scheduler.common.job.JobId)
     */
    public BooleanWrapper pause(JobId jobId) throws SchedulerException {
        return schedulerFrontend.pause(jobId);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.UserSchedulerInterface_#resume(org.ow2.proactive.scheduler.common.job.JobId)
     */
    public BooleanWrapper resume(JobId jobId) throws SchedulerException {
        return schedulerFrontend.resume(jobId);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.UserSchedulerInterface_#kill(org.ow2.proactive.scheduler.common.job.JobId)
     */
    public BooleanWrapper kill(JobId jobId) throws SchedulerException {
        return schedulerFrontend.kill(jobId);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.UserSchedulerInterface_#changePriority(org.ow2.proactive.scheduler.common.job.JobId, org.ow2.proactive.scheduler.common.job.JobPriority)
     */
    public void changePriority(JobId jobId, JobPriority priority) throws SchedulerException {
        schedulerFrontend.changePriority(jobId, priority);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.UserSchedulerInterface_#getJobState(org.ow2.proactive.scheduler.common.job.JobId)
     */
    public JobState getJobState(JobId jobId) throws SchedulerException {
        return schedulerFrontend.getJobState(jobId);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.UserSchedulerInterface#kill(java.lang.String)
     */
    public BooleanWrapper kill(String jobId) throws SchedulerException {
        return schedulerFrontend.kill(jobId);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.UserSchedulerInterface#pause(java.lang.String)
     */
    public BooleanWrapper pause(String jobId) throws SchedulerException {
        return schedulerFrontend.pause(jobId);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.UserSchedulerInterface#remove(java.lang.String)
     */
    public void remove(String jobId) throws SchedulerException {
        schedulerFrontend.remove(jobId);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.UserSchedulerInterface#resume(java.lang.String)
     */
    public BooleanWrapper resume(String jobId) throws SchedulerException {
        return schedulerFrontend.resume(jobId);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.UserSchedulerInterface#changePriority(java.lang.String, org.ow2.proactive.scheduler.common.job.JobPriority)
     */
    public void changePriority(String jobId, JobPriority priority) throws SchedulerException {
        schedulerFrontend.changePriority(jobId, priority);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.UserSchedulerInterface#getJobState(java.lang.String)
     */
    public JobState getJobState(String jobId) throws SchedulerException {
        return schedulerFrontend.getJobState(jobId);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.UserSchedulerInterface#isConnected()
     */
    public BooleanWrapper isConnected() {
        return schedulerFrontend.isConnected();
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return getClass().getName() + "@" +
            Tools.getHostURL(PAActiveObject.getActiveObjectNodeUrl(schedulerFrontend));
    }

}
