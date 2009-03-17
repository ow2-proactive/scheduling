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
package org.ow2.proactive.scheduler.gui.composite;

import java.util.Vector;

import org.eclipse.swt.widgets.Composite;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.gui.data.JobsController;
import org.ow2.proactive.scheduler.gui.listeners.EventJobsListener;
import org.ow2.proactive.scheduler.gui.listeners.PendingJobsListener;


/**
 * This class represents the pending jobs
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
public class PendingJobComposite extends AbstractJobComposite implements PendingJobsListener,
        EventJobsListener {
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
    public PendingJobComposite(Composite parent, JobsController jobsController) {
        super(parent, "Pending", PENDING_TABLE_ID);
        jobsController.addPendingJobsListener(this);
        jobsController.addEventJobsListener(this);
    }

    // -------------------------------------------------------------------- //
    // ---------------------- extends JobComposite ------------------------ //
    // -------------------------------------------------------------------- //
    /**
     * @see org.ow2.proactive.scheduler.gui.composite.AbstractJobComposite#getJobs()
     */
    @Override
    public Vector<JobId> getJobs() {
        return JobsController.getLocalView().getPendingsJobs();
    }

    /**
     * @see org.ow2.proactive.scheduler.gui.composite.AbstractJobComposite#sortJobs()
     */
    @Override
    public void sortJobs() {
        JobsController.getLocalView().sortPendingsJobs();
    }

    /**
     * @see org.ow2.proactive.scheduler.gui.composite.AbstractJobComposite#clear()
     */
    @Override
    public void clear() {
        // Nothing to do
    }

    // -------------------------------------------------------------------- //
    // ----------------- implements PendingJobsListener ------------------- //
    // -------------------------------------------------------------------- //
    /**
     * @see org.ow2.proactive.scheduler.gui.listeners.PendingJobsListener#addPendingJob(org.objectweb.proactive.extra.scheduler.job.JobId)
     */
    public void addPendingJob(JobId jobId) {
        addJob(jobId);
    }

    /**
     * @see org.ow2.proactive.scheduler.gui.listeners.PendingJobsListener#removePendingJob(org.objectweb.proactive.extra.scheduler.job.JobId)
     */
    public void removePendingJob(JobId jobId) {
        removeJob(jobId);
    }

    // -------------------------------------------------------------------- //
    // ------------------- implements EventJobsListener ------------------- //
    // -------------------------------------------------------------------- //

    /**
     * @see org.ow2.proactive.scheduler.gui.listeners.EventJobsListener#pausedEvent(org.objectweb.proactive.extra.scheduler.job.JobInfo)
     */
    public void pausedEvent(JobInfo info) {
        stateUpdate(info);
    }

    /**
     * @see org.ow2.proactive.scheduler.gui.listeners.EventJobsListener#resumedEvent(org.objectweb.proactive.extra.scheduler.job.JobInfo)
     */
    public void resumedEvent(JobInfo info) {
        stateUpdate(info);
    }

    /**
     * @see org.ow2.proactive.scheduler.gui.listeners.EventJobsListener#priorityChangedEvent(org.ow2.proactive.scheduler.common.job.JobInfo)
     */
    public void priorityChangedEvent(JobInfo info) {
        JobId jobId = info.getJobId();
        if (getJobs().contains(jobId)) {
            super.priorityUpdate(jobId);
        }
    }

    // -------------------------------------------------------------------- //
    // ------------------- implements EventJobsListener ------------------- //
    // -------------------------------------------------------------------- //
    private void stateUpdate(JobInfo info) {
        JobId jobId = info.getJobId();
        if (getJobs().contains(jobId)) {
            super.stateUpdate(jobId);
        }
    }
}
