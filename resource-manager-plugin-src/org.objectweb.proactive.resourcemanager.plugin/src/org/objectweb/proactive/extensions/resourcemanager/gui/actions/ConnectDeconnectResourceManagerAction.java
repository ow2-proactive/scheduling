/*
 * ################################################################
 * 
 * ProActive: The Java(TM) library for Parallel, Distributed, Concurrent
 * computing with Security and Mobility
 * 
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis Contact:
 * proactive@objectweb.org
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this library; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * Initial developer(s): The ProActive Team
 * http://proactive.inria.fr/team_members.htm Contributor(s):
 * 
 * ################################################################
 */
package org.objectweb.proactive.extensions.resourcemanager.gui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.objectweb.proactive.extensions.resourcemanager.gui.data.RMCoreListenerImpl;
import org.objectweb.proactive.extensions.resourcemanager.gui.data.RMStore;
import org.objectweb.proactive.extensions.resourcemanager.gui.data.ResourceManagerController;
import org.objectweb.proactive.extensions.resourcemanager.gui.dialog.SelectResourceManagerDialog;
import org.objectweb.proactive.extensions.resourcemanager.gui.dialog.SelectResourceManagerDialogResult;
import org.objectweb.proactive.extensions.resourcemanager.gui.views.ResourceExplorerView;


/**
 * @author The ProActive Team
 */
public class ConnectDeconnectResourceManagerAction extends Action {
    private static ConnectDeconnectResourceManagerAction instance = null;
    private Shell shell = null;
    private boolean isConnected = false;

    private ConnectDeconnectResourceManagerAction(Composite parent) {
        this.shell = parent.getShell();
        setDisconnectionMode();
    }

    /**
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run() {
        if (isConnected) {
            disconnection();
        } else {
            connection();
        }
    }

    private void connection() {
        SelectResourceManagerDialogResult dialogResult = SelectResourceManagerDialog.showDialog(shell);
        if (dialogResult != null) {
            try {
                RMStore.newInstance(dialogResult.getUrl());

                ResourceManagerController.getActiveView().init();

                isConnected = true;

                // connection successful, so record "valid" url
                SelectResourceManagerDialog.saveInformations();

                this.setText("Disconnect");
                this.setToolTipText("Disconnect from the Resource Manager");
                this.setImageDescriptor(ImageDescriptor.createFromFile(this.getClass(),
                        "icons/disconnect.gif"));

                ResourceExplorerView.init();
                ResourceManagerController.getLocalView().addCoreListener(new RMCoreListenerImpl(shell));
            } catch (Exception e) {
                MessageDialog.openError(shell, "Couldn't connect",
                        "Couldn't Connect to the resource manager based on : \n" + dialogResult.getUrl());
                e.printStackTrace();
            }
        } else {
            MessageDialog.openError(shell, "Couldn't connect",
                    "Couldn't Connect to the resource manager based on : \n" + dialogResult.getUrl());
        }
    }

    private void disconnection() {
        ResourceExplorerView.clearOnDisconnection();
        ResourceManagerController.getLocalView().removeCoreListener();
        setDisconnectionMode();
    }

    public void setDisconnectionMode() {
        isConnected = false;
        this.setText("Connect a Resource Manager");
        this.setToolTipText("Connect a started Resource Manager by its url");
        this.setImageDescriptor(ImageDescriptor.createFromFile(this.getClass(), "icons/connect.gif"));
    }

    public static ConnectDeconnectResourceManagerAction newInstance(Composite parent) {
        instance = new ConnectDeconnectResourceManagerAction(parent);
        return instance;
    }

    public static ConnectDeconnectResourceManagerAction getInstance() {
        return instance;
    }
}
