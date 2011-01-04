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
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;
import org.ow2.proactive.resourcemanager.Activator;
import org.ow2.proactive.resourcemanager.gui.data.RMStatusBarItem;
import org.ow2.proactive.resourcemanager.gui.data.RMStore;
import org.ow2.proactive.resourcemanager.gui.dialog.SelectResourceManagerDialog;
import org.ow2.proactive.resourcemanager.gui.dialog.SelectResourceManagerDialogResult;


public class ConnectHandler extends AbstractHandler implements IHandler {

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

        if (dialogResult != null && !dialogResult.isCanceled()) {
            // Create a temporary shell with a progress bar during the downloading of the RM state
            final Shell waitShell = new Shell(parent.getDisplay(), SWT.APPLICATION_MODAL);
            // Disable the escape key
            waitShell.addListener(SWT.Traverse, new Listener() {
                public void handleEvent(Event e) {
                    if (e.detail == SWT.TRAVERSE_ESCAPE) {
                        e.doit = false;
                    }
                }
            });

            final GridLayout layout = new GridLayout();
            final int marginWidth = 50;
            layout.marginHeight = 30;
            layout.verticalSpacing = 15;
            layout.marginWidth = marginWidth;
            waitShell.setLayout(layout);
            final Label jobNameLabel = new Label(waitShell, SWT.NONE);
            jobNameLabel.setText("Downloading RM state, please wait...");

            // Progress bar showing to the user that the application still running
            final ProgressBar bar = new ProgressBar(waitShell, SWT.INDETERMINATE);

            final Button cancelButton = new Button(waitShell, SWT.PUSH);
            cancelButton.setText("Cancel");
            cancelButton.setToolTipText("Cancel the downloading and exit");
            cancelButton.setLayoutData(new GridData(SWT.CENTER, SWT.END, false, true));
            cancelButton.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent e) {
                    System.exit(0);
                }
            });
            waitShell.setDefaultButton(cancelButton);

            // Useless without the escape key use
            //Label connectionCancel = new Label(waitShell, SWT.NONE);
            //connectionCancel.setText("Press Escape to cancel");
            waitShell.pack();
            Rectangle parentBounds = parent.getShell().getBounds();
            int x = parentBounds.x + parentBounds.width / 2;
            int y = parentBounds.y + parentBounds.height / 2;
            waitShell.setLocation(x - waitShell.getSize().x / 2, y - waitShell.getSize().y / 2);
            bar.setSize((waitShell.getSize().x) - marginWidth * 2, 20);
            waitShell.open();

            new Thread() {
                public void run() {
                    //perform connection in a new thread, non graphic
                    try {
                        RMStore.newInstance(dialogResult.getUrl(), dialogResult.getLogin(), dialogResult
                                .getPassword());
                        parent.getDisplay().syncExec(new Runnable() {
                            public void run() {
                                if (!waitShell.isDisposed()) {
                                    bar.dispose();
                                    waitShell.dispose();
                                }
                                RMStatusBarItem.getInstance().setText("connected");
                            }
                        });
                    } catch (final Throwable t) {
                        parent.getDisplay().syncExec(new Runnable() {
                            public void run() {
                                if (!waitShell.isDisposed()) {
                                    bar.dispose();
                                    waitShell.dispose();
                                }
                                RMStatusBarItem.getInstance().setText("disconnected");
                                MessageDialog.openError(Display.getDefault().getActiveShell(),
                                        "Couldn't connect to resource manager", t.getMessage());
                                Activator.log(IStatus.ERROR, "Could not connect to the Resource Manager ", t);
                                t.printStackTrace();

                                try {
                                    // trying to disconnect in any case
                                    RMStore.getInstance().getResourceManager().disconnect();
                                } catch (Throwable thr) {
                                }
                            }
                        });
                    }
                }
            }.start();

        }
        fireHandlerChanged(new HandlerEvent(this, true, false));
        this.isDialogOpen = false;
        return null;
    }
}
