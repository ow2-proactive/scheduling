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
import org.objectweb.proactive.extensions.scheduler.common.job.JobId;
import org.objectweb.proactive.extensions.scheduler.gui.data.JobsController;
import org.objectweb.proactive.extensions.scheduler.gui.listeners.FinishedJobsListener;


/**
 * This class represents the finished jobs
 *
 * @author The ProActive Team
 * @version 1.0, Jul 12, 2007
 * @since ProActive 3.2
 */
public class FinishedJobComposite extends AbstractJobComposite implements FinishedJobsListener {
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
    public FinishedJobComposite(Composite parent, JobsController jobsController) {
        super(parent, "Finished", FINISHED_TABLE_ID);
        jobsController.addFinishedJobsListener(this);
    }

    // -------------------------------------------------------------------- //
    // ---------------------- extends JobComposite ------------------------ //
    // -------------------------------------------------------------------- //
    /**
     * @see org.objectweb.proactive.extensions.scheduler.gui.composites.AbstractJobComposite#getJobs()
     */
    @Override
    public Vector<JobId> getJobs() {
        return JobsController.getLocalView().getFinishedJobs();
    }

    /**
     * @see org.objectweb.proactive.extensions.scheduler.gui.composites.AbstractJobComposite#sortJobs()
     */
    @Override
    public void sortJobs() {
        JobsController.getLocalView().sortFinishedJobs();
    }

    /**
     * @see org.objectweb.proactive.extensions.scheduler.gui.composite.AbstractJobComposite#clear()
     */
    @Override
    public void clear() {
        // Nothing to do
    }

    // -------------------------------------------------------------------- //
    // ----------------- implements FinishedJobsListener ------------------ //
    // -------------------------------------------------------------------- //
    /**
     * @see org.objectweb.proactive.extensions.scheduler.gui.listeners.FinishedJobsListener#addFinishedJob(org.objectweb.proactive.extra.scheduler.job.JobId)
     */
    public void addFinishedJob(JobId jobId) {
        addJob(jobId);
    }

    /**
     * @see org.objectweb.proactive.extensions.scheduler.gui.listeners.FinishedJobsListener#removeFinishedJob(org.objectweb.proactive.extra.scheduler.job.JobId)
     */
    public void removeFinishedJob(JobId jobId) {
        removeJob(jobId);
    }
}
