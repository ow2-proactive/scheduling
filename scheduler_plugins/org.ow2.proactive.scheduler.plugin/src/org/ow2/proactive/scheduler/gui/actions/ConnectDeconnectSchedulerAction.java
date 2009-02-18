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

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.ow2.proactive.scheduler.common.SchedulerState;
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
        SelectSchedulerDialogResult dialogResult = SelectSchedulerDialog.showDialog(parent.getShell());

        if (dialogResult != null) {
            int res = SchedulerProxy.getInstance().connectToScheduler(dialogResult);

            if (res == SchedulerProxy.CONNECTED) {
                isConnected = true;
                ActionsManager.getInstance().setConnected(true);

                // connection successful, so record "valid" url and login
                SelectSchedulerDialog.saveInformations();
                this.setText("Disconnect");
                this.setToolTipText("Disconnect from the ProActive Scheduler");
                this.setImageDescriptor(ImageDescriptor.createFromFile(this.getClass(),
                        "icons/disconnect.gif"));
                waitShell = new Shell(parent.getShell(), SWT.PRIMARY_MODAL);
                FormLayout layout = new FormLayout();
                layout.marginHeight = 20;
                layout.marginWidth = 20;
                waitShell.setLayout(layout);
                Label jobNameLabel = new Label(waitShell, SWT.NONE);
                jobNameLabel.setText("Download scheduler state, please wait...");
                waitShell.pack();
                Rectangle parentBounds = parent.getShell().getBounds();
                int x = parentBounds.x + parentBounds.width / 2;
                int y = parentBounds.y + parentBounds.height / 2;
                waitShell.setLocation(x, y);
                waitShell.pack();
                waitShell.open();

                // wait result for synchronous call
                boolean futurRes = JobsController.getActiveView().init();

                //synchronous call ; wait futur 
                if (futurRes) {
                    waitShell.close();
                }

                // the call "JobsController.getActiveView().init();"
                // must be terminated here, before starting other call.
                SeparatedJobView.getPendingJobComposite().initTable();
                SeparatedJobView.getRunningJobComposite().initTable();
                SeparatedJobView.getFinishedJobComposite().initTable();

                ActionsManager.getInstance().update();

                SeparatedJobView.setVisible(true);
            } else if (res == SchedulerProxy.LOGIN_OR_PASSWORD_WRONG) {
                MessageDialog.openError(parent.getShell(), "Couldn't connect",
                        "The login and/or the password are wrong !");
            } else {
                MessageDialog.openError(parent.getShell(), "Couldn't connect",
                        "Couldn't Connect to the scheduler based on : \n" + dialogResult.getUrl());
            }
        }
    }

    private void disconnection() {
        if (MessageDialog.openConfirm(parent.getShell(), "Confirm disconnection",
                "Are you sure you want to disconnect from the ProActive Scheduler ?")) {
            StatusLabel.getInstance().disconnect();
            SeparatedJobView.clearOnDisconnection(true);
        }
    }

    public void setDisconnectionMode() {
        isConnected = false;
        this.setText("Connect the ProActive Scheduler");
        this.setToolTipText("Connect the started ProActive Scheduler by its url");
        this.setImageDescriptor(ImageDescriptor.createFromFile(this.getClass(), "icons/connect.gif"));
    }

    @Override
    public void setEnabled(boolean connected, SchedulerState schedulerState, boolean admin,
            boolean jobSelected, boolean owner, boolean jobInFinishQueue) {
        if (!connected)
            setDisconnectionMode();
    }
}
