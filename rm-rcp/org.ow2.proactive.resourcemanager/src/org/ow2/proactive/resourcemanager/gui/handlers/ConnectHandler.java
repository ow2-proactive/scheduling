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
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package org.ow2.proactive.resourcemanager.gui.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.HandlerEvent;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.progress.UIJob;
import org.ow2.proactive.resourcemanager.Activator;
import org.ow2.proactive.resourcemanager.gui.data.RMStatusBarItem;
import org.ow2.proactive.resourcemanager.gui.data.RMStore;
import org.ow2.proactive.resourcemanager.gui.dialog.SelectResourceManagerDialog;
import org.ow2.proactive.resourcemanager.gui.dialog.SelectResourceManagerDialogResult;


public class ConnectHandler extends AbstractHandler implements IHandler {

    public static final String COMMAND_ID = "org.ow2.proactive.resourcemanager.plugin.gui.connectCommand";

    private boolean previousState = true;
    /** A boolean value to know if the dialog box is already open */
    private boolean isDialogOpen;
    private static ConnectHandler thisHandler;

    public ConnectHandler() {
        thisHandler = this;
    }

    public static ConnectHandler getHandler() {
        return thisHandler;
    }

    @Override
    public boolean isEnabled() {
        //hack for toolbar menu, throws event if state changed
        //otherwise action stays disabled
        if (previousState != RMStore.isConnected()) {
            previousState = RMStore.isConnected();
            fireHandlerChanged(new HandlerEvent(this, true, false));
        }
        return !RMStore.isConnected();
    }

    public synchronized Object execute(ExecutionEvent event) throws ExecutionException {
        return execute(HandlerUtil.getActiveWorkbenchWindowChecked(event).getShell());
    }

    public synchronized Object execute(final Shell parent) {
        if (this.isDialogOpen || RMStore.isConnected()) {
            return null;
        }
        this.isDialogOpen = true;
        final SelectResourceManagerDialogResult dialogResult = SelectResourceManagerDialog.showDialog(parent);

        if (dialogResult == null) {
            parent.getDisplay().syncExec(new Runnable() {
                public void run() {
                    ConnectHandler.getHandler().execute(parent);
                }
            });
        } else if (!dialogResult.isCanceled()) {
            //perform connection in a new thread, non graphic
            Job job = new Job("Downloading the Cloud & Grid state, this might take several minutes.") {

                @Override
                protected IStatus run(IProgressMonitor monitor) {
                    try {
                        RMStore.newInstance(dialogResult.getUrl(), dialogResult.getLogin(), dialogResult
                                .getPassword(), dialogResult.getCredentials());

                        parent.getDisplay().syncExec(new Runnable() {
                            public void run() {
                                RMStatusBarItem.getInstance().setText("connected");
                                updateToolbarCommands();
                            }
                        });

                        return Status.OK_STATUS;

                    } catch (final Throwable t) {

                        // Status.WARNING used (instead of Status.ERROR) to avoid the appearance of an eclipse's error dialog
                        errorConnect(t, dialogResult.getUrl());
                        return new Status(Status.WARNING, "rm.rcp",
                            "Could not connect to the Resource Manager ", t);
                    }
                }

                @Override
                protected void canceling() {
                    // canceling connection
                }
            };

            job.setUser(true);
            job.schedule();
        }

        fireHandlerChanged(new HandlerEvent(this, true, false));
        this.isDialogOpen = false;
        return null;
    }

    private void updateToolbarCommands() {
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        ICommandService commandService = (ICommandService) window.getService(ICommandService.class);
        String commandIds[] = { AddNodesHandler.COMMAND_ID, ConnectHandler.COMMAND_ID,
                CreateNodeSourceHandler.COMMAND_ID, DisconnectHandler.COMMAND_ID,
                RemoveNodesHandler.COMMAND_ID, RemoveNodeSourceHandler.COMMAND_ID,
                ShowAccountHandler.COMMAND_ID, ShowRuntimeDataHandler.COMMAND_ID, ShutdownHandler.COMMAND_ID };
        for (String commandId : commandIds) {
            commandService.getCommand(commandId).getHandler().isEnabled();
        }
    }

    private void errorConnect(final Throwable t, final String rmUrl) {
        UIJob uiJob = new UIJob(Display.getDefault(), "Display connect error message") {
            @Override
            public IStatus runInUIThread(IProgressMonitor monitor) {
                RMStatusBarItem.getInstance().setText("disconnected");
                MessageDialog.openError(Display.getDefault().getActiveShell(),
                        "Couldn't connect to resource manager at " + rmUrl, t.getMessage());
                if (t != null) {
                    Activator.log(IStatus.ERROR, "Could not connect to the Resource Manager ", t);
                    t.printStackTrace();
                }
                // trying to disconnect in any case
                RMStore.getInstance().getResourceManager().disconnect();
                return Status.OK_STATUS;
            }
        };
        uiJob.setUser(false);
        uiJob.schedule();
    }

}
