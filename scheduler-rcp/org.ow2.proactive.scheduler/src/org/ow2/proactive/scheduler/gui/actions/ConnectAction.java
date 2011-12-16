/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.gui.actions;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.UIJob;
import org.ow2.proactive.scheduler.Activator;
import org.ow2.proactive.scheduler.common.SchedulerStatus;
import org.ow2.proactive.scheduler.common.util.logforwarder.LogForwardingException;
import org.ow2.proactive.scheduler.gui.Internal;
import org.ow2.proactive.scheduler.gui.data.ActionsManager;
import org.ow2.proactive.scheduler.gui.data.JobsController;
import org.ow2.proactive.scheduler.gui.data.SchedulerProxy;
import org.ow2.proactive.scheduler.gui.dialog.SelectSchedulerDialog;
import org.ow2.proactive.scheduler.gui.dialog.SelectSchedulerDialogResult;
import org.ow2.proactive.scheduler.gui.views.SeparatedJobView;


public class ConnectAction extends SchedulerGUIAction {
    private static boolean connDialogUp;

    public ConnectAction() {
        this.setText("&Connect");
        this.setToolTipText("Connect the started ProActive Scheduler by its url");
        this
                .setImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor(
                        Internal.IMG_CONNECT));
    }

    @Override
    public void run() {
        if (!connDialogUp)
            connection();
    }

    private void connection() {
        // poor design led to the connection being impossible if the JobView is not visible...
        try {
            PlatformUI.getWorkbench().getActiveWorkbenchWindow().getPages()[0].showView(SeparatedJobView.ID,
                    null, IWorkbenchPage.VIEW_ACTIVATE);
        } catch (PartInitException e1) {
            e1.printStackTrace();
            return;
        }

        connDialogUp = true;
        final SelectSchedulerDialogResult dialogResult = SelectSchedulerDialog.showDialog(getParent()
                .getShell());
        connDialogUp = false;

        if (dialogResult == null) {
            getParent().getDisplay().asyncExec(new Runnable() {
                public void run() {
                    connection();
                }
            });
        } else if (!dialogResult.isCanceled()) {

            Job job = new Job(
                    "Downloading the Orchestration & Scheduling State, this might take a several minutes.") {
                @Override
                public IStatus run(IProgressMonitor monitor) {
                    try {
                        setName("Downloading the Orchestration & Scheduling State, this might take a several minutes.");

                        boolean connected = SchedulerProxy.getInstance().connectToScheduler(dialogResult);
                        if (connected) {
                            //getting the scheduler state here, in this job (non UI).
                            JobsController.getLocalView().init(SchedulerProxy.getInstance());
                            postConnect(dialogResult.getUrl());
                        } else {
                            errorConnect(
                                    new Exception("Authentication failed: invalid username or password "),
                                    dialogResult.getUrl());
                        }

                        return Status.OK_STATUS;
                    } catch (Throwable t) {
                        errorConnect(t, dialogResult.getUrl());
                        // Status.WARNING used (instead of Status.ERROR) to avoid the appearance of an eclipse's error dialog
                        return new Status(Status.WARNING, "scheduler.rcp",
                            "Could not connect to the Scheduler ", t);
                    }
                }

            };

            job.setUser(true);
            job.schedule();
        }
    }

    /**
     * operations to be performed just after a connection has been established with the scheduler
     * these operation will be executed in a UIJob as updates to the UI will be performed
     *
     * @param schedulerURL the URL of the scheduler we are connected to. May be used for displaying messages
     */
    private void postConnect(final String schedulerURL) {

        UIJob uiJob = new UIJob(getParent().getDisplay(), "Scheduler post connect job") {
            @Override
            public IStatus runInUIThread(IProgressMonitor monitor) {
                SeparatedJobView.getPendingJobComposite().initTable();
                SeparatedJobView.getRunningJobComposite().initTable();
                SeparatedJobView.getFinishedJobComposite().initTable();
                ActionsManager.getInstance().setConnected(true);
                SelectSchedulerDialog.saveInformations();
                try {
                    // start log server
                    Activator.startLoggerServer();
                    ActionsManager.getInstance().update();
                    SeparatedJobView.setVisible(true);
                    return Status.OK_STATUS;
                } catch (LogForwardingException e) {
                    errorConnect(e, schedulerURL);
                    return new Status(Status.WARNING, "scheduler.rcp", "Unable to download Scheduler state",
                        e);
                }
            }
        };

        uiJob.setUser(false);
        uiJob.schedule();

    }

    private void errorConnect(final Throwable e, final String schedURL) {
        e.printStackTrace();
        Activator.log(IStatus.ERROR, "Could not connect to the scheduler based on:" + schedURL, e);
        UIJob uiJob = new UIJob(getParent().getDisplay(), "Display connect error message ") {
            @Override
            public IStatus runInUIThread(IProgressMonitor monitor) {

                getParent().getDisplay().syncExec(new Runnable() {
                    public void run() {
                        String cause = "";
                        if (e.getMessage() != null)
                            cause += "\n\nCause\n : " + e.getMessage();
                        MessageDialog.openError(getParent().getShell(), "Couldn't connect",
                                "Could not connect to the scheduler based on : " + schedURL + cause);
                    }
                });
                return Status.OK_STATUS;
            }
        };

        uiJob.setUser(false);
        uiJob.schedule();
    }

    @Override
    public void setEnabled(boolean connected, SchedulerStatus chedulerStatus, boolean admin,
            boolean jobSelected, boolean owner, boolean jobInFinishQueue) {
        super.setEnabled(!connected);
    }
}
