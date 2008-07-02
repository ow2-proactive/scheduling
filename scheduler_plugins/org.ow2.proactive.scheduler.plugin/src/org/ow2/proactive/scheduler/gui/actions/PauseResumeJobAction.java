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
package org.ow2.proactive.scheduler.gui.actions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.scheduler.SchedulerState;
import org.ow2.proactive.scheduler.gui.data.JobsController;
import org.ow2.proactive.scheduler.gui.data.SchedulerProxy;
import org.ow2.proactive.scheduler.gui.data.TableManager;
import org.ow2.proactive.scheduler.job.InternalJob;


/**
 * @author The ProActive Team
 */
public class PauseResumeJobAction extends SchedulerGUIAction {

    private List<JobId> jobsId = null;
    private boolean resume;

    public PauseResumeJobAction() {
        jobsId = new ArrayList<JobId>();
        this.setEnabled(false);
    }

    @Override
    public void run() {
        if (resume)
            for (JobId jobId : jobsId)
                SchedulerProxy.getInstance().resume(jobId);
        else
            for (JobId jobId : jobsId)
                SchedulerProxy.getInstance().pause(jobId);
    }

    private void setPauseMode() {
        this.setText("Pause job");
        this.setToolTipText("To pause this job (this will finish all running tasks)");
    }

    private void setResumeMode() {
        this.setText("Resume job");
        this.setToolTipText("To resume this job (this will restart all paused tasks)");
    }

    private void setPauseResumeMode() {
        this.setText("Pause/Resume job");
        this.setToolTipText("To pause or resume a job");
        this
                .setImageDescriptor(ImageDescriptor.createFromFile(this.getClass(),
                        "icons/job_pause_resume.gif"));
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (enabled) {
            int count = 0;
            jobsId = TableManager.getInstance().getJobsIdOfSelectedItems();
            List<InternalJob> jobs = JobsController.getLocalView().getJobsByIds(jobsId);
            for (InternalJob job : jobs) {
                switch (job.getState()) {
                    case PAUSED:
                        count++;
                        break;
                    case CANCELLED:
                    case FAILED:
                    case FINISHED:
                        enabled = false;
                        break;
                    default:
                        break;
                }
            }

            if (enabled) {
                if (count > (jobsId.size() / 2)) {
                    resume = true;
                    setResumeMode();
                } else {
                    resume = false;
                    setPauseMode();
                }
            }
        }
        if (!enabled) {
            setPauseResumeMode();
        }
        super.setEnabled(enabled);
    }

    @Override
    public void setEnabled(boolean connected, SchedulerState schedulerState, boolean admin,
            boolean jobSelected, boolean owner, boolean jobInFinishQueue) {
        setEnabled(connected && jobSelected && !jobInFinishQueue && (admin || owner));
    }
}
