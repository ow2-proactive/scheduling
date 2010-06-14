/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
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
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $ACTIVEEON_INITIAL_DEV$
 */
package org.ow2.proactive.scheduler.common.backwardcompatibility;

import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.ow2.proactive.scheduler.common.Scheduler;
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


/**
 * SchedulerUserAdapter is temporarily used to force compatibility with previous version !
 * This class reproduce the previous User interface behavior !
 *
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 2.0
 */
public class SchedulerUserAdapter implements UserSchedulerInterface {

    /**  */
	private static final long serialVersionUID = 21L;
	protected Scheduler frontend;

    public SchedulerUserAdapter(Scheduler frontend) {
        this.frontend = frontend;
    }

    /**
     * {@inheritDoc}
     */
    public void addEventListener(SchedulerEventListener sel, boolean myEventsOnly, SchedulerEvent... events)
            throws SchedulerException {
        frontend.addEventListener(sel, myEventsOnly, events);
    }

    /**
     * {@inheritDoc}
     */
    public SchedulerState addEventListener(SchedulerEventListener sel, boolean myEventsOnly,
            boolean getInitialState, SchedulerEvent... events) throws SchedulerException {
        return frontend.addEventListener(sel, myEventsOnly, getInitialState, events);
    }

    /**
     * {@inheritDoc}
     */
    public SchedulerState addSchedulerEventListener(SchedulerEventListener sel, boolean myEventsOnly,
            SchedulerEvent... events) throws SchedulerException {
        return frontend.addEventListener(sel, myEventsOnly, true, events);
    }

    /**
     * {@inheritDoc}
     */
    public void changePriority(String jobId, JobPriority priority) throws SchedulerException {
        frontend.changeJobPriority(jobId, priority);
    }

    /**
     * {@inheritDoc}
     */
    public void disconnect() throws SchedulerException {
        frontend.disconnect();
    }

    /**
     * {@inheritDoc}
     */
    public JobResult getJobResult(String jobId) throws SchedulerException {
        return frontend.getJobResult(jobId);
    }

    /**
     * {@inheritDoc}
     */
    public JobState getJobState(String jobId) throws SchedulerException {
        return frontend.getJobState(jobId);
    }

    /**
     * {@inheritDoc}
     */
    public SchedulerStatus getSchedulerStatus() throws SchedulerException {
        return frontend.getStatus();
    }

    /**
     * {@inheritDoc}
     */
    public SchedulerStatus getStatus() throws SchedulerException {
        return frontend.getStatus();
    }

    /**
     * {@inheritDoc}
     */
    public TaskResult getTaskResult(String jobId, String taskName) throws SchedulerException {
        return frontend.getTaskResult(jobId, taskName);
    }

    /**
     * {@inheritDoc}
     */
    public BooleanWrapper isConnected() {
        return new BooleanWrapper(frontend.isConnected());
    }

    /**
     * {@inheritDoc}
     */
    public BooleanWrapper kill(String jobId) throws SchedulerException {
        return new BooleanWrapper(frontend.killJob(jobId));
    }

    /**
     * {@inheritDoc}
     */
    public void listenLog(String jobId, AppenderProvider appenderProvider) throws SchedulerException {
        frontend.listenJobLogs(jobId, appenderProvider);
    }

    /**
     * {@inheritDoc}
     */
    public BooleanWrapper pause(String jobId) throws SchedulerException {
        return new BooleanWrapper(frontend.pause());
    }

    /**
     * {@inheritDoc}
     */
    public void remove(String jobId) throws SchedulerException {
        frontend.removeJob(jobId);
    }

    /**
     * {@inheritDoc}
     */
    public void removeEventListener() throws SchedulerException {
        frontend.removeEventListener();
    }

    /**
     * {@inheritDoc}
     */
    public void removeSchedulerEventListener() throws SchedulerException {
        frontend.removeEventListener();
    }

    /**
     * {@inheritDoc}
     */
    public BooleanWrapper resume(String jobId) throws SchedulerException {
        return new BooleanWrapper(frontend.resumeJob(jobId));
    }

    /**
     * {@inheritDoc}
     */
    public JobId submit(Job job) throws SchedulerException {
        return frontend.submit(job);
    }

    /**
     * {@inheritDoc}
     */
    public void changePriority(JobId jobId, JobPriority priority) throws SchedulerException {
        frontend.changeJobPriority(jobId, priority);
    }

    /**
     * {@inheritDoc}
     */
    public JobResult getJobResult(JobId jobId) throws SchedulerException {
        return frontend.getJobResult(jobId);
    }

    /**
     * {@inheritDoc}
     */
    public JobState getJobState(JobId jobId) throws SchedulerException {
        return frontend.getJobState(jobId);
    }

    /**
     * {@inheritDoc}
     */
    public SchedulerState getSchedulerState() throws SchedulerException {
        return frontend.getState();
    }

    /**
     * {@inheritDoc}
     */
    public TaskResult getTaskResult(JobId jobId, String taskName) throws SchedulerException {
        return frontend.getTaskResult(jobId, taskName);
    }

    /**
     * {@inheritDoc}
     */
    public BooleanWrapper kill(JobId jobId) throws SchedulerException {
        return new BooleanWrapper(frontend.killJob(jobId));
    }

    /**
     * {@inheritDoc}
     */
    public void listenLog(JobId jobId, AppenderProvider appenderProvider) throws SchedulerException {
        frontend.listenJobLogs(jobId, appenderProvider);
    }

    /**
     * {@inheritDoc}
     */
    public BooleanWrapper pause(JobId jobId) throws SchedulerException {
        return new BooleanWrapper(frontend.pauseJob(jobId));
    }

    /**
     * {@inheritDoc}
     */
    public void remove(JobId jobId) throws SchedulerException {
        frontend.removeJob(jobId);
    }

    /**
     * {@inheritDoc}
     */
    public BooleanWrapper resume(JobId jobId) throws SchedulerException {
        return new BooleanWrapper(frontend.resumeJob(jobId));
    }

}
