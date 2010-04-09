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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Shell;
import org.ow2.proactive.scheduler.common.SchedulerStatus;
import org.ow2.proactive.scheduler.common.util.logforwarder.LogForwardingException;
import org.ow2.proactive.scheduler.gui.Activator;
import org.ow2.proactive.scheduler.gui.Internal;
import org.ow2.proactive.scheduler.gui.composite.StatusLabel;
import org.ow2.proactive.scheduler.gui.data.ActionsManager;
import org.ow2.proactive.scheduler.gui.data.JobsController;
import org.ow2.proactive.scheduler.gui.data.SchedulerProxy;
import org.ow2.proactive.scheduler.gui.dialog.SelectSchedulerDialog;
import org.ow2.proactive.scheduler.gui.dialog.SelectSchedulerDialogResult;
import org.ow2.proactive.scheduler.gui.views.SeparatedJobView;


/**
 * @author The ProActive Team
 */
public class ConnectDeconnectSchedulerAction extends SchedulerGUIAction {
    private Composite parent = null;
    private boolean isConnected = false;
    private Shell waitShell = null;

    public ConnectDeconnectSchedulerAction(Composite parent) {
        this.parent = parent;
        setDisconnectionMode();
    }

    @Override
    public void run() {
        if (isConnected) {
            disconnection();
        } else {
            connection();
        }
    }

    private void connection() {
        final SelectSchedulerDialogResult dialogResult = SelectSchedulerDialog.showDialog(parent.getShell());

        if (dialogResult != null) {
            try {
                int res = SchedulerProxy.getInstance().connectToScheduler(dialogResult);

                if (res == SchedulerProxy.CONNECTED) {

                    final ConnectDeconnectSchedulerAction btnConnect = this;
                    waitShell = new Shell(parent.getShell(), SWT.PRIMARY_MODAL);
                    waitShell.setText("Download scheduler state, please wait...");
                    GridLayout layout = new GridLayout();
                    int marginWidth = 50;
                    layout.marginHeight = 30;
                    layout.marginWidth = marginWidth;
                    waitShell.setLayout(layout);
                    Label jobNameLabel = new Label(waitShell, SWT.NONE);
                    jobNameLabel.setText("Download scheduler state, please wait...");

                    // Progress bar showing to the user that the application still running
                    final ProgressBar bar = new ProgressBar(waitShell, SWT.INDETERMINATE);
                    Label connectionCancel = new Label(waitShell, SWT.NONE);
                    connectionCancel.setText("Press Escape to cancel");
                    waitShell.pack();
                    Rectangle parentBounds = parent.getShell().getBounds();
                    int x = parentBounds.x + parentBounds.width / 2;
                    int y = parentBounds.y + parentBounds.height / 2;
                    waitShell.setLocation(x - waitShell.getSize().x / 2, y - waitShell.getSize().y / 2);
                    bar.setSize((waitShell.getSize().x) - marginWidth * 2, 20);
                    waitShell.open();

                    // Using thread because graphical thread is blocked by waitShell
                    new Thread(null, null, "ThreadConnection") {
                        public void run() {

                            // wait result for synchronous call
                            final boolean futurRes = JobsController.getActiveView().init();

                            // If the escape key was pressed, terminate thread
                            if (waitShell.isDisposed()) {
                                return;
                            }

                            // Get graphical thread for the progress bar
                            parent.getDisplay().syncExec(new Runnable() {

                                public void run() {

                                    //synchronous call ; wait futur
                                    if (futurRes) {
                                        bar.dispose();
                                        waitShell.close();

                                    }

                                    // the call "JobsController.getActiveView().init();"
                                    // must be terminated here, before starting other call.
                                    SeparatedJobView.getPendingJobComposite().initTable();
                                    SeparatedJobView.getRunningJobComposite().initTable();
                                    SeparatedJobView.getFinishedJobComposite().initTable();

                                    isConnected = true;
                                    ActionsManager.getInstance().setConnected(true);
                                    SelectSchedulerDialog.saveInformations();

                                    try {

                                        // start log server
                                        Activator.startLoggerServer();

                                        ActionsManager.getInstance().update();

                                        SeparatedJobView.setVisible(true);

                                        btnConnect.setText("Disconnect");
                                        btnConnect.setToolTipText("Disconnect from the ProActive Scheduler");
                                        btnConnect.setImageDescriptor(Activator.getDefault()
                                                .getImageRegistry().getDescriptor(Internal.IMG_DISCONNECT));

                                    } catch (LogForwardingException e) {
                                        errorConnect(e, dialogResult);
                                    }

                                }

                            });
                        }

                    }.start();

                } else if (res == SchedulerProxy.LOGIN_OR_PASSWORD_WRONG) {
                    MessageDialog.openError(parent.getShell(), "Could not connect",
                            "Incorrect username or password !");
                }

            } catch (Throwable t) {
                errorConnect(t, dialogResult);
            }

        }

    }

    private void errorConnect(Throwable e, SelectSchedulerDialogResult dialogResult) {
        e.printStackTrace();
        Activator.log(IStatus.ERROR, "Could not connect to the scheduler based on:" + dialogResult.getUrl(),
                e);
        MessageDialog.openError(parent.getShell(), "Couldn't connect",
                "Could not connect to the scheduler based on : " + dialogResult.getUrl() + "\n\nCause\n : " +
                    e.getMessage());
    }

    private void disconnection() {
        if (MessageDialog.openConfirm(parent.getShell(), "Confirm disconnection",
                "Are you sure you want to disconnect from the ProActive Scheduler ?")) {
            StatusLabel.getInstance().disconnect();
            // stop log server
            try {
                Activator.terminateLoggerServer();
            } catch (LogForwardingException e) {
                e.printStackTrace();
            }
            SeparatedJobView.clearOnDisconnection(true);
        }
    }

    public void setDisconnectionMode() {
        isConnected = false;
        this.setText("Connect the ProActive Scheduler");
        this.setToolTipText("Connect the started ProActive Scheduler by its url");
        this
                .setImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor(
                        Internal.IMG_CONNECT));
    }

    @Override
    public void setEnabled(boolean connected, SchedulerStatus chedulerStatus, boolean admin,
            boolean jobSelected, boolean owner, boolean jobInFinishQueue) {
        if (!connected)
            setDisconnectionMode();
    }
}
