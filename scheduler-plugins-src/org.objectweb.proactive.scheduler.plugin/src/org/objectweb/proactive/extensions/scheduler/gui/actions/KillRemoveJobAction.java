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
package org.objectweb.proactive.extensions.scheduler.gui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.objectweb.proactive.extensions.scheduler.common.job.JobId;
import org.objectweb.proactive.extensions.scheduler.gui.data.SchedulerProxy;
import org.objectweb.proactive.extensions.scheduler.gui.data.TableManager;


/**
 * @author FRADJ Johann
 */
public class KillRemoveJobAction extends Action {
    public static final boolean ENABLED_AT_CONSTRUCTION = false;
    private static KillRemoveJobAction instance = null;
    private boolean killMode = false;
    private Shell shell = null;

    private KillRemoveJobAction(Shell shell) {
        this.shell = shell;
        this.setText("Kill job");
        this.setToolTipText("To kill a job (this will remove this job from the scheduler)");
        this.setImageDescriptor(ImageDescriptor.createFromFile(this.getClass(), "icons/job_kill.gif"));
        this.setEnabled(ENABLED_AT_CONSTRUCTION);
    }

    @Override
    public void run() {
        if (killMode) {
            if (MessageDialog
                    .openConfirm(shell, "Confirm please", "Are you sure you want to Kill this job ?")) {
                TableItem item = TableManager.getInstance().getLastSelectedItem();
                if (item != null) {
                    JobId jobId = (JobId) item.getData();
                    SchedulerProxy.getInstance().kill(jobId);
                }
            }
        } else {
            if (MessageDialog.openConfirm(shell, "Confirm please",
                    "Are you sure you want to Remove this job ?")) {
                TableItem item = TableManager.getInstance().getLastSelectedItem();
                if (item != null) {
                    JobId jobId = (JobId) item.getData();
                    SchedulerProxy.getInstance().getJobResult(jobId);
                }
            }
        }
    }

    public void setRemoveMode() {
        killMode = false;

        this.setText("Remove Job");
        this.setToolTipText("To remove definitely a job from the scheduler");
    }

    public void setKillMode() {
        killMode = true;

        this.setText("Kill job");
        this.setToolTipText("To kill a job (this will remove this job from the scheduler)");
    }

    public static KillRemoveJobAction newInstance(Shell shell) {
        instance = new KillRemoveJobAction(shell);
        return instance;
    }

    public static KillRemoveJobAction getInstance() {
        return instance;
    }
}
