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
package org.ow2.proactive.resourcemanager.gui.dialog;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.ow2.proactive.resourcemanager.gui.data.model.Removable;


/**
 * This class allow to pop up a dialogue to remove a node.
 *
 * @author The ProActive Team
 */
public class RemoveNodeDialog extends Dialog {

    // -------------------------------------------------------------------- //
    // --------------------------- constructor ---------------------------- //
    // -------------------------------------------------------------------- //
    private RemoveNodeDialog(final Shell parent, final List<Removable> nodes) {

        // Pass the default styles here
        super(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);

        // Init the display
        Display display = parent.getDisplay();

        // Init the shell
        final Shell shell = new Shell(parent, SWT.BORDER | SWT.CLOSE);
        shell.setText("Remove nodes");
        FormLayout layout = new FormLayout();
        layout.marginHeight = 15;
        layout.marginWidth = 20;
        shell.setLayout(layout);

        // creation
        Label urlLabel = new Label(shell, SWT.NONE);
        final Button preemptCheck = new Button(shell, SWT.CHECK);
        preemptCheck.setSelection(true);
        final Button downCheck = new Button(shell, SWT.CHECK);
        Button okButton = new Button(shell, SWT.NONE);
        Button cancelButton = new Button(shell, SWT.NONE);

        String message;
        if (nodes.size() == 1) {
            message = "Confirm removal of node : \n\n" + nodes.get(0).getName();
        } else {
            message = "Confirm removal of " + Integer.toString(nodes.size()) + " nodes";
        }

        // label sourceName
        urlLabel.setText(message);
        FormData urlFormData = new FormData();
        urlFormData.top = new FormAttachment(0, 10);
        urlLabel.setLayoutData(urlFormData);

        // preempt check
        preemptCheck.setText("wait for tasks completion on busy nodes");
        FormData checkFormData = new FormData();
        checkFormData.top = new FormAttachment(urlLabel, 20, SWT.BOTTOM);
        preemptCheck.setLayoutData(checkFormData);

        // down check
        downCheck.setText("remove only if node is down");
        FormData downNodeFormData = new FormData();
        downNodeFormData.top = new FormAttachment(preemptCheck, 5, SWT.BOTTOM);
        downCheck.setLayoutData(downNodeFormData);

        // button "OK"
        okButton.setText("OK");
        okButton.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                boolean removeDownNodes = downCheck.getSelection();
                boolean preemptive = !preemptCheck.getSelection();
                for (Removable node : nodes) {
                    node.removeFromModel(preemptive, removeDownNodes);
                }
                shell.close();
            }
        });

        FormData okFormData = new FormData();
        okFormData.top = new FormAttachment(preemptCheck, 30);
        okFormData.left = new FormAttachment(25, 30);
        okFormData.width = 70;
        okButton.setLayoutData(okFormData);
        shell.setDefaultButton(okButton);

        // button "CANCEL"
        cancelButton.setText("Cancel");
        cancelButton.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                shell.close();
            }
        });

        FormData cancelFormData = new FormData();
        cancelFormData.top = new FormAttachment(preemptCheck, 30);
        cancelFormData.left = new FormAttachment(okButton, 10, SWT.RIGHT);
        cancelFormData.width = 70;
        cancelButton.setLayoutData(cancelFormData);

        shell.setMinimumSize(450, 200);
        shell.pack();
        shell.open();

        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
    }

    // -------------------------------------------------------------------- //
    // ------------------------------ public ------------------------------ //
    // -------------------------------------------------------------------- //
    /**
     * This method pop up a dialog for trying to connect a resource manager.
     *
     * @param parent
     *            the parent
     */
    public static void showDialog(Shell parent, List<Removable> nodes) {
        new RemoveNodeDialog(parent, nodes);
    }
}
