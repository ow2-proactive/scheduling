/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 *              Nice-Sophia Antipolis/ActiveEon
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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
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
    private int res = 0;

    public ConnectAction() {

        this.setText("&Connect");
        this.setToolTipText("Connect the started ProActive Scheduler by its url");
        this
                .setImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor(
                        Internal.IMG_CONNECT));

    }

    @Override
    public void run() {
        connection();
    }

    private void connection() {
        final SelectSchedulerDialogResult dialogResult = SelectSchedulerDialog.showDialog(getParent()
                .getShell());

        if (dialogResult != null) {

            // Create a temporary shell with a progress bar during the downloading of the RM state
            final Shell waitShell = new Shell(getParent().getShell(), SWT.APPLICATION_MODAL);
            // Disable the escape key
            waitShell.addListener(SWT.Traverse, new Listener() {
                public void handleEvent(Event e) {
                    if (e.detail == SWT.TRAVERSE_ESCAPE) {
                        e.doit = false;
                    }
                }
            });

            GridLayout layout = new GridLayout();
            int marginWidth = 50;
            layout.marginHeight = 30;
            layout.marginWidth = marginWidth;
            layout.verticalSpacing = 15;
            waitShell.setLayout(layout);
            final Label jobNameLabel = new Label(waitShell, SWT.NONE);
            jobNameLabel.setText("Downloading Scheduler state, please wait...");

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
            Rectangle parentBounds = getParent().getShell().getBounds();
            int x = parentBounds.x + parentBounds.width / 2;
            int y = parentBounds.y + parentBounds.height / 2;
            waitShell.setLocation(x - waitShell.getSize().x / 2, y - waitShell.getSize().y / 2);
            bar.setSize((waitShell.getSize().x) - marginWidth * 2, 20);
            waitShell.open();

            new Thread() {
                public void run() {

                    try {
                        // Connection to the scheduler
                        res = 0;
                        res = SchedulerProxy.getInstance().connectToScheduler(dialogResult);
                    } catch (Throwable t) {
                        errorConnect(t, dialogResult);
                    }

                    if (res == 0/*init val*/) {
                        getParent().getDisplay().syncExec(new Runnable() {
                            public void run() {
                                if (!waitShell.isDisposed()) {
                                    bar.dispose();
                                    waitShell.dispose();
                                }
                            }
                        });
                        return;
                    } else if (res == SchedulerProxy.LOGIN_OR_PASSWORD_WRONG) {
                        getParent().getDisplay().syncExec(new Runnable() {
                            public void run() {
                                if (!waitShell.isDisposed()) {
                                    bar.dispose();
                                    waitShell.dispose();
                                }
                                MessageDialog.openError(getParent().getShell(), "Could not connect",
                                        "Incorrect username or password !");
                            }
                        });
                        return;
                    } else if (res == SchedulerProxy.CONNECTED) {
                        // wait result for synchronous call
                        final boolean futurRes = JobsController.getActiveView().init();

                        // If the escape key was pressed, terminate thread
                        if (waitShell.isDisposed()) {
                            //SchedulerProxy.getInstance().disconnect();                                    
                            return;
                        }

                        // Get graphical thread for the progress bar
                        getParent().getDisplay().syncExec(new Runnable() {

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

                                ActionsManager.getInstance().setConnected(true);
                                SelectSchedulerDialog.saveInformations();

                                try {
                                    // start log server
                                    Activator.startLoggerServer();

                                    ActionsManager.getInstance().update();

                                    SeparatedJobView.setVisible(true);

                                } catch (LogForwardingException e) {
                                    errorConnect(e, dialogResult);
                                }
                            }
                        });

                    } else if (res == SchedulerProxy.LOGIN_OR_PASSWORD_WRONG) {
                        getParent().getDisplay().syncExec(new Runnable() {
                            public void run() {
                                if (!waitShell.isDisposed()) {
                                    bar.dispose();
                                    waitShell.dispose();
                                }
                                MessageDialog.openError(getParent().getShell(), "Could not connect",
                                        "Incorrect username or password !");
                            }
                        });
                        return;
                    }
                }
            }.start();
        }
    }

    private void errorConnect(final Throwable e, final SelectSchedulerDialogResult dialogResult) {
        e.printStackTrace();
        Activator.log(IStatus.ERROR, "Could not connect to the scheduler based on:" + dialogResult.getUrl(),
                e);
        getParent().getDisplay().asyncExec(new Runnable() {
            public void run() {
                MessageDialog.openError(getParent().getShell(), "Couldn't connect",
                        "Could not connect to the scheduler based on : " + dialogResult.getUrl() +
                            "\n\nCause\n : " + e.getMessage());
            }
        });
    }

    @Override
    public void setEnabled(boolean connected, SchedulerStatus chedulerStatus, boolean admin,
            boolean jobSelected, boolean owner, boolean jobInFinishQueue) {
        super.setEnabled(!connected);
    }
}
