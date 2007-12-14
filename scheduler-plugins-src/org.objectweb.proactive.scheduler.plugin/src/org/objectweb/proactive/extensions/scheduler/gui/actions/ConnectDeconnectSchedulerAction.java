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
import org.eclipse.swt.widgets.Composite;
import org.objectweb.proactive.extensions.scheduler.gui.composite.StatusLabel;
import org.objectweb.proactive.extensions.scheduler.gui.data.JobsController;
import org.objectweb.proactive.extensions.scheduler.gui.data.SchedulerProxy;
import org.objectweb.proactive.extensions.scheduler.gui.dialog.SelectSchedulerDialog;
import org.objectweb.proactive.extensions.scheduler.gui.dialog.SelectSchedulerDialogResult;
import org.objectweb.proactive.extensions.scheduler.gui.views.SeparatedJobView;


/**
 * @author FRADJ Johann
 */
public class ConnectDeconnectSchedulerAction extends Action {
    public static final boolean ENABLED_AT_CONSTRUCTION = true;
    private static ConnectDeconnectSchedulerAction instance = null;
    private Composite parent = null;
    private boolean isConnected = false;

    private ConnectDeconnectSchedulerAction(Composite parent) {
        this.parent = parent;
        setDisconnectionMode();
        this.setEnabled(ENABLED_AT_CONSTRUCTION);
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
            int res = SchedulerProxy.getInstance()
                                    .connectToScheduler(dialogResult);

            if (res == SchedulerProxy.CONNECTED) {
                isConnected = true;

                // connection successful, so record "valid" url and login
                SelectSchedulerDialog.saveInformations();

                this.setText("Disconnect");
                this.setToolTipText("Disconnect from the ProActive Scheduler");
                this.setImageDescriptor(ImageDescriptor.createFromFile(
                        this.getClass(), "icons/disconnect.gif"));

                // active reference
                JobsController.getActiveView().init();

                // the call "JobsController.getActiveView().init();"
                // must be terminated here, before starting other call.
                SeparatedJobView.getPendingJobComposite().initTable();
                SeparatedJobView.getRunningJobComposite().initTable();
                SeparatedJobView.getFinishedJobComposite().initTable();

                ChangeViewModeAction.getInstance().setEnabled(true);
                KillSchedulerAction.getInstance().setEnabled(true);

                SeparatedJobView.setVisible(true);
            } else if (res == SchedulerProxy.LOGIN_OR_PASSWORD_WRONG) {
                MessageDialog.openError(parent.getShell(), "Couldn't connect",
                    "The login and/or the password are wrong !");
            } else {
                MessageDialog.openError(parent.getShell(), "Couldn't connect",
                    "Couldn't Connect to the scheduler based on : \n" +
                    dialogResult.getUrl());
            }
        }
    }

    private void disconnection() {
        StatusLabel.getInstance().disconnect();
        SeparatedJobView.clearOnDisconnection(true);
    }

    public void setDisconnectionMode() {
        isConnected = false;
        this.setText("Connect the ProActive Scheduler");
        this.setToolTipText("Connect the started ProActive Scheduler by its url");
        this.setImageDescriptor(ImageDescriptor.createFromFile(
                this.getClass(), "icons/connect.gif"));
    }

    public static ConnectDeconnectSchedulerAction newInstance(Composite parent) {
        instance = new ConnectDeconnectSchedulerAction(parent);
        return instance;
    }

    public static ConnectDeconnectSchedulerAction getInstance() {
        return instance;
    }
}
