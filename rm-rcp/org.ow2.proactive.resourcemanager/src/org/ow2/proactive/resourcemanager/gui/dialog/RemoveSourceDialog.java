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

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.ow2.proactive.resourcemanager.gui.data.RMStore;
import org.ow2.proactive.resourcemanager.gui.data.ResourceManagerProxy;


/**
 * This class allow to pop up a dialogue to remove a source node.
 *
 * @author The ProActive Team
 */
public class RemoveSourceDialog extends Dialog {

    // -------------------------------------------------------------------- //
    // --------------------------- constructor ---------------------------- //
    // -------------------------------------------------------------------- //
    private RemoveSourceDialog(Shell parent) {

        // Pass the default styles here
        super(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);

        // Load the proactive default configuration
        ProActiveConfiguration.load();

        // Init the display
        Display display = parent.getDisplay();

        // Init the shell
        final Shell shell = new Shell(parent, SWT.BORDER | SWT.CLOSE);
        shell.setText("Remove a node source");
        FormLayout layout = new FormLayout();
        layout.marginHeight = 5;
        layout.marginWidth = 5;
        shell.setLayout(layout);

        // creation
        Label urlLabel = new Label(shell, SWT.NONE);
        final Combo sourceNameCombo = new Combo(shell, SWT.READ_ONLY);
        final Button preemptCheck = new Button(shell, SWT.CHECK);
        Button okButton = new Button(shell, SWT.NONE);
        Button cancelButton = new Button(shell, SWT.NONE);

        // label sourceName
        urlLabel.setText("Node source :");
        FormData urlLabelFormData = new FormData();
        urlLabelFormData.top = new FormAttachment(sourceNameCombo, 0, SWT.CENTER);
        urlLabel.setLayoutData(urlLabelFormData);

        // combo sourceName
        FormData urlFormData = new FormData();
        urlFormData.top = new FormAttachment(0, -1);
        urlFormData.left = new FormAttachment(urlLabel, 5);
        urlFormData.right = new FormAttachment(100, -5);
        urlFormData.width = 320;
        sourceNameCombo.setLayoutData(urlFormData);
        sourceNameCombo.setItems(RMStore.getInstance().getModel().getSourcesNames(false));
        int i = sourceNameCombo.getItemCount() - 1;
        // Search for the index of the source selected in the tree (index=0 if nothing is selected then blank line in combo is used)
        while (i > 0 &&
            !sourceNameCombo.getItem(i).matches(RMStore.getInstance().getModel().getSourceToRemoveSelected())) {
            i--;
        }
        sourceNameCombo.select(i);
        RMStore.getInstance().getModel().setSourceToRemoveSelected("");

        // preempt check
        preemptCheck.setText("wait tasks end on busy nodes");
        preemptCheck.setSelection(true);
        FormData checkFormData = new FormData();
        checkFormData.top = new FormAttachment(sourceNameCombo, 5);
        checkFormData.left = new FormAttachment(50, -50);
        preemptCheck.setLayoutData(checkFormData);

        // button "OK"
        okButton.setText("OK");
        okButton.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                String src = sourceNameCombo.getText();
                if (src.equals(""))
                    MessageDialog.openError(shell, "Error", "You didn't choose a source to remove");
                else {
                    ResourceManagerProxy proxy = RMStore.getInstance().getResourceManager();
                    try {
                        if (!proxy.syncRemoveNodeSource(src, !preemptCheck.getSelection())) {
                            proxy.displayError("Unknown reason", "Error in node source removal");
                        } else {
                            shell.close();
                        }
                    } catch (Exception e) {
                        proxy.logAndDisplayError(e, "Error in node source removal");
                    }
                }
            }
        });

        FormData okFormData = new FormData();
        okFormData.top = new FormAttachment(preemptCheck, 5);
        okFormData.left = new FormAttachment(25, 20);
        okFormData.right = new FormAttachment(50, -10);
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
        cancelFormData.top = new FormAttachment(preemptCheck, 5);
        cancelFormData.left = new FormAttachment(50, 10);
        cancelFormData.right = new FormAttachment(75, -20);
        cancelButton.setLayoutData(cancelFormData);

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
     * @param parent the parent
     */
    public static void showDialog(Shell parent) {
        new RemoveSourceDialog(parent);
    }
}
