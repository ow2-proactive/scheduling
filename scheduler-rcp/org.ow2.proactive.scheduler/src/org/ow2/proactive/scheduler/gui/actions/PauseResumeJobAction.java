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
package org.ow2.proactive.scheduler.gui.actions;

import java.util.ArrayList;
import java.util.List;

import org.ow2.proactive.scheduler.Activator;
import org.ow2.proactive.scheduler.common.SchedulerStatus;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.gui.Internal;
import org.ow2.proactive.scheduler.gui.data.JobsController;
import org.ow2.proactive.scheduler.gui.data.SchedulerProxy;
import org.ow2.proactive.scheduler.gui.data.TableManager;


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
        if (resume) {
            for (JobId jobId : jobsId) {
                SchedulerProxy.getInstance().resumeJob(jobId);
            }
        } else {
            for (JobId jobId : jobsId) {
                SchedulerProxy.getInstance().pauseJob(jobId);
            }
        }
    }

    private void setPauseMode() {
        this.setText("Pause job");
        this.setToolTipText("Pause this job (this will finish all running tasks)");
    }

    private void setResumeMode() {
        this.setText("Resume job");
        this.setToolTipText("Resume this job (this will restart all paused tasks)");
    }

    private void setPauseResumeMode() {
        this.setText("Pause/Resume job");
        this.setToolTipText("Pause or resume a job");
        this.setImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor(
                Internal.IMG_JOBPAUSERESUME));
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (enabled) {
            int count = 0;
            jobsId = TableManager.getInstance().getJobsIdOfSelectedItems();
            List<JobState> jobs = JobsController.getLocalView().getJobsByIds(jobsId);
            for (JobState job : jobs) {
                switch (job.getStatus()) {
                    case PAUSED:
                        count++;
                        break;
                    case CANCELED:
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
    public void setEnabled(boolean connected, SchedulerStatus schedulerStatus, boolean admin,
            boolean jobSelected, boolean owner, boolean jobInFinishQueue) {
        setEnabled(connected && jobSelected && !jobInFinishQueue && (admin || owner));
    }
}
