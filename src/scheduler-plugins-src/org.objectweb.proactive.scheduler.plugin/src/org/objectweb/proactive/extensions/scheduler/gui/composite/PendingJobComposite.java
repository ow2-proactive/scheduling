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
package org.objectweb.proactive.extensions.scheduler.gui.composite;

import java.util.Vector;

import org.eclipse.swt.widgets.Composite;
import org.objectweb.proactive.extensions.scheduler.common.job.JobEvent;
import org.objectweb.proactive.extensions.scheduler.common.job.JobId;
import org.objectweb.proactive.extensions.scheduler.gui.data.JobsController;
import org.objectweb.proactive.extensions.scheduler.gui.listeners.EventJobsListener;
import org.objectweb.proactive.extensions.scheduler.gui.listeners.PendingJobsListener;


/**
 * This class represents the pending jobs
 *
 * @author The ProActive Team
 * @version 1.0, Jul 12, 2007
 * @since ProActive 3.2
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
     * @see org.objectweb.proactive.extensions.scheduler.gui.composites.AbstractJobComposite#getJobs()
     */
    @Override
    public Vector<JobId> getJobs() {
        return JobsController.getLocalView().getPendingsJobs();
    }

    /**
     * @see org.objectweb.proactive.extensions.scheduler.gui.composites.AbstractJobComposite#sortJobs()
     */
    @Override
    public void sortJobs() {
        JobsController.getLocalView().sortPendingsJobs();
    }

    /**
     * @see org.objectweb.proactive.extensions.scheduler.gui.composite.AbstractJobComposite#clear()
     */
    @Override
    public void clear() {
        // Nothing to do
    }

    // -------------------------------------------------------------------- //
    // ----------------- implements PendingJobsListener ------------------- //
    // -------------------------------------------------------------------- //
    /**
     * @see org.objectweb.proactive.extensions.scheduler.gui.listeners.PendingJobsListener#addPendingJob(org.objectweb.proactive.extra.scheduler.job.JobId)
     */
    public void addPendingJob(JobId jobId) {
        addJob(jobId);
    }

    /**
     * @see org.objectweb.proactive.extensions.scheduler.gui.listeners.PendingJobsListener#removePendingJob(org.objectweb.proactive.extra.scheduler.job.JobId)
     */
    public void removePendingJob(JobId jobId) {
        removeJob(jobId);
    }

    // -------------------------------------------------------------------- //
    // ------------------- implements EventJobsListener ------------------- //
    // -------------------------------------------------------------------- //
    /**
     * @see org.objectweb.proactive.extensions.scheduler.gui.listeners.EventJobsListener#killedEvent(org.objectweb.proactive.extra.scheduler.job.JobId)
     */
    public void killedEvent(JobId jobId) {
        // Do nothing
    }

    /**
     * @see org.objectweb.proactive.extensions.scheduler.gui.listeners.EventJobsListener#pausedEvent(org.objectweb.proactive.extra.scheduler.job.JobEvent)
     */
    public void pausedEvent(JobEvent event) {
        stateUpdate(event);
    }

    /**
     * @see org.objectweb.proactive.extensions.scheduler.gui.listeners.EventJobsListener#resumedEvent(org.objectweb.proactive.extra.scheduler.job.JobEvent)
     */
    public void resumedEvent(JobEvent event) {
        stateUpdate(event);
    }

    /**
     * @see org.objectweb.proactive.extensions.scheduler.gui.listeners.EventJobsListener#priorityChangedEvent(org.objectweb.proactive.extra.scheduler.job.JobEvent)
     */
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
