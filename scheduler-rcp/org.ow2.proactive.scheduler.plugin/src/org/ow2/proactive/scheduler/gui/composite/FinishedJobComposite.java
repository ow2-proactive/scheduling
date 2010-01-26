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
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.gui.composite;

import java.util.Vector;

import org.eclipse.swt.widgets.Composite;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.gui.data.JobsController;
import org.ow2.proactive.scheduler.gui.listeners.FinishedJobsListener;


/**
 * This class represents the finished jobs
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
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
     * @see org.ow2.proactive.scheduler.gui.composite.AbstractJobComposite#getJobs()
     */
    @Override
    public Vector<JobId> getJobs() {
        return JobsController.getLocalView().getFinishedJobs();
    }

    /**
     * @see org.ow2.proactive.scheduler.gui.composite.AbstractJobComposite#sortJobs()
     */
    @Override
    public void sortJobs() {
        JobsController.getLocalView().sortFinishedJobs();
    }

    /**
     * @see org.ow2.proactive.scheduler.gui.composite.AbstractJobComposite#clear()
     */
    @Override
    public void clear() {
        // Nothing to do
    }

    // -------------------------------------------------------------------- //
    // ----------------- implements FinishedJobsListener ------------------ //
    // -------------------------------------------------------------------- //
    /**
     * @see org.ow2.proactive.scheduler.gui.listeners.FinishedJobsListener#addFinishedJob(org.objectweb.proactive.extra.scheduler.job.JobId)
     */
    public void addFinishedJob(JobId jobId) {
        addJob(jobId);
    }

    /**
     * @see org.ow2.proactive.scheduler.gui.listeners.FinishedJobsListener#removeFinishedJob(org.objectweb.proactive.extra.scheduler.job.JobId)
     */
    public void removeFinishedJob(JobId jobId) {
        removeJob(jobId);
    }
}
