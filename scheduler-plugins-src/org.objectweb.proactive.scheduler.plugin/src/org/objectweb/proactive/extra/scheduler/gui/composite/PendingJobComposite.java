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
package org.objectweb.proactive.extra.scheduler.gui.composite;

import java.util.Vector;

import org.eclipse.swt.widgets.Composite;
import org.objectweb.proactive.extra.scheduler.common.job.JobEvent;
import org.objectweb.proactive.extra.scheduler.common.job.JobId;
import org.objectweb.proactive.extra.scheduler.common.job.JobState;
import org.objectweb.proactive.extra.scheduler.gui.actions.KillRemoveJobAction;
import org.objectweb.proactive.extra.scheduler.gui.actions.ObtainJobOutputAction;
import org.objectweb.proactive.extra.scheduler.gui.actions.PauseResumeJobAction;
import org.objectweb.proactive.extra.scheduler.gui.actions.PriorityHighJobAction;
import org.objectweb.proactive.extra.scheduler.gui.actions.PriorityHighestJobAction;
import org.objectweb.proactive.extra.scheduler.gui.actions.PriorityIdleJobAction;
import org.objectweb.proactive.extra.scheduler.gui.actions.PriorityJobAction;
import org.objectweb.proactive.extra.scheduler.gui.actions.PriorityLowJobAction;
import org.objectweb.proactive.extra.scheduler.gui.actions.PriorityLowestJobAction;
import org.objectweb.proactive.extra.scheduler.gui.actions.PriorityNormalJobAction;
import org.objectweb.proactive.extra.scheduler.gui.data.EventJobsListener;
import org.objectweb.proactive.extra.scheduler.gui.data.JobsController;
import org.objectweb.proactive.extra.scheduler.gui.data.PendingJobsListener;
import org.objectweb.proactive.extra.scheduler.gui.data.SchedulerProxy;
import org.objectweb.proactive.extra.scheduler.job.InternalJob;


/**
 * This class represents the pending jobs
 *
 * @author ProActive Team
 * @version 1.0, Jul 12, 2007
 * @since ProActive 3.2
 */
public class PendingJobComposite extends AbstractJobComposite
    implements PendingJobsListener, EventJobsListener {
    // -------------------------------------------------------------------- //
    // --------------------------- constructor ---------------------------- //
    // -------------------------------------------------------------------- //
    /**
     * This is the default constructor.
     *
     * @param parent
     * @param title
     * @param jobsController
     */
    public PendingJobComposite(Composite parent, String title,
        JobsController jobsController) {
        super(parent, title, PENDING_TABLE_ID);
        jobsController.addPendingJobsListener(this);
        jobsController.addEventJobsListener(this);
    }

    // -------------------------------------------------------------------- //
    // ---------------------- extends JobComposite ------------------------ //
    // -------------------------------------------------------------------- //
    /**
     * @see org.objectweb.proactive.extra.scheduler.gui.composites.AbstractJobComposite#getJobs()
     */
    @Override
    public Vector<JobId> getJobs() {
        return JobsController.getLocalView().getPendingsJobs();
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.gui.composites.AbstractJobComposite#sortJobs()
     */
    @Override
    public void sortJobs() {
        JobsController.getLocalView().sortPendingsJobs();
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.gui.composites.AbstractJobComposite#jobSelected(org.objectweb.proactive.extra.scheduler.job.Job)
     */
    @Override
    public void jobSelected(InternalJob job) {
        // enabling/disabling button permitted with this job
        boolean enabled = SchedulerProxy.getInstance().isItHisJob(job.getOwner());
        PauseResumeJobAction pauseResumeJobAction = PauseResumeJobAction.getInstance();

        switch (JobsController.getSchedulerState()) {
        case SHUTTING_DOWN:
        case KILLED:
            PriorityJobAction.getInstance().setEnabled(false);
            PriorityIdleJobAction.getInstance().setEnabled(false);
            PriorityLowestJobAction.getInstance().setEnabled(false);
            PriorityLowJobAction.getInstance().setEnabled(false);
            PriorityNormalJobAction.getInstance().setEnabled(false);
            PriorityHighJobAction.getInstance().setEnabled(false);
            PriorityHighestJobAction.getInstance().setEnabled(false);

            pauseResumeJobAction.setEnabled(false);
            pauseResumeJobAction.setPauseResumeMode();
            break;
        default:
            PriorityJobAction.getInstance().setEnabled(enabled);
            PriorityIdleJobAction.getInstance().setEnabled(enabled);
            PriorityLowestJobAction.getInstance().setEnabled(enabled);
            PriorityLowJobAction.getInstance().setEnabled(enabled);
            PriorityNormalJobAction.getInstance().setEnabled(enabled);
            PriorityHighJobAction.getInstance().setEnabled(enabled);
            PriorityHighestJobAction.getInstance().setEnabled(enabled);

            pauseResumeJobAction.setEnabled(enabled);
            JobState jobState = job.getState();
            if (jobState.equals(JobState.PAUSED)) {
                pauseResumeJobAction.setResumeMode();
            } else if (jobState.equals(JobState.RUNNING) ||
                    jobState.equals(JobState.PENDING)) {
                pauseResumeJobAction.setPauseMode();
            } else {
                pauseResumeJobAction.setPauseResumeMode();
            }
        }

        ObtainJobOutputAction.getInstance().setEnabled(enabled);

        KillRemoveJobAction killRemoveJobAction = KillRemoveJobAction.getInstance();
        killRemoveJobAction.setKillMode();
        killRemoveJobAction.setEnabled(enabled);
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.gui.composite.AbstractJobComposite#clear()
     */
    @Override
    public void clear() {
        // Nothing to do
    }

    // -------------------------------------------------------------------- //
    // ----------------- implements PendingJobsListener ------------------- //
    // -------------------------------------------------------------------- //
    /**
     * @see org.objectweb.proactive.extra.scheduler.gui.data.PendingJobsListener#addPendingJob(org.objectweb.proactive.extra.scheduler.job.JobId)
     */
    @Override
    public void addPendingJob(JobId jobId) {
        addJob(jobId);
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.gui.data.PendingJobsListener#removePendingJob(org.objectweb.proactive.extra.scheduler.job.JobId)
     */
    @Override
    public void removePendingJob(JobId jobId) {
        removeJob(jobId);
    }

    // -------------------------------------------------------------------- //
    // ------------------- implements EventJobsListener ------------------- //
    // -------------------------------------------------------------------- //
    /**
     * @see org.objectweb.proactive.extra.scheduler.gui.data.EventJobsListener#killedEvent(org.objectweb.proactive.extra.scheduler.job.JobId)
     */
    @Override
    public void killedEvent(JobId jobId) {
        // Do nothing
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.gui.data.EventJobsListener#pausedEvent(org.objectweb.proactive.extra.scheduler.job.JobEvent)
     */
    @Override
    public void pausedEvent(JobEvent event) {
        stateUpdate(event);
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.gui.data.EventJobsListener#resumedEvent(org.objectweb.proactive.extra.scheduler.job.JobEvent)
     */
    @Override
    public void resumedEvent(JobEvent event) {
        stateUpdate(event);
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.gui.data.EventJobsListener#priorityChangedEvent(org.objectweb.proactive.extra.scheduler.job.JobEvent)
     */
    @Override
    public void priorityChangedEvent(JobEvent event) {
        JobId jobId = event.getJobId();
        if (getJobs().contains(jobId)) {
            super.priorityUpdate(jobId);
        }
    }

    // -------------------------------------------------------------------- //
    // ------------------- implements EventJobsListener ------------------- //
    // -------------------------------------------------------------------- //
    private void stateUpdate(JobEvent event) {
        JobId jobId = event.getJobId();
        if (getJobs().contains(jobId)) {
            super.stateUpdate(jobId);
        }
    }
}
