/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of 
 * 						   Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org
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
 * If needed, contact us to obtain a release under GPL Version 2. 
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.gui.actions;

import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.ow2.proactive.scheduler.common.SchedulerStatus;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.gui.Activator;
import org.ow2.proactive.scheduler.gui.Internal;
import org.ow2.proactive.scheduler.gui.data.SchedulerProxy;
import org.ow2.proactive.scheduler.gui.data.TableManager;


/**
 * @author The ProActive Team
 */
public class KillRemoveJobAction extends SchedulerGUIAction {
    private boolean killMode = false;
    private Shell shell = null;

    public KillRemoveJobAction(Shell shell) {
        this.shell = shell;
        this.setText("Kill job");
        this.setToolTipText("Kill selected job (this will remove this job from the scheduler)");
        this
                .setImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor(
                        Internal.IMG_JOBKILL));
        this.setEnabled(false);
    }

    @Override
    public void run() {
        if (killMode) {
            if (MessageDialog
                    .openConfirm(shell, "Confirm please", "Are you sure you want to Kill this job ?")) {
                List<JobId> jobsId = TableManager.getInstance().getJobsIdOfSelectedItems();
                for (JobId jobId : jobsId) {
                    SchedulerProxy.getInstance().kill(jobId);
                }
            }
        } else {
            if (MessageDialog.openConfirm(shell, "Confirm please",
                    "Are you sure you want to Remove this job ?")) {
                List<JobId> jobsId = TableManager.getInstance().getJobsIdOfSelectedItems();
                for (JobId jobId : jobsId) {
                    SchedulerProxy.getInstance().remove(jobId);
                }
            }
        }
    }

    public void setRemoveMode() {
        killMode = false;

        this.setText("Remove Job");
        this.setToolTipText("Remove definitely a job from the scheduler");
    }

    public void setKillMode() {
        killMode = true;

        this.setText("Kill job");
        this.setToolTipText("Kill selected job (this will remove this job from the scheduler)");
    }

    @Override
    public void setEnabled(boolean connected, SchedulerStatus schedulerStatus, boolean admin,
            boolean jobSelected, boolean owner, boolean jobInFinishQueue) {
        if (connected && jobSelected && (owner || admin))
            setEnabled(true);
        else
            setEnabled(false);

        if (jobInFinishQueue)
            setRemoveMode();
        else
            setKillMode();
    }
}
