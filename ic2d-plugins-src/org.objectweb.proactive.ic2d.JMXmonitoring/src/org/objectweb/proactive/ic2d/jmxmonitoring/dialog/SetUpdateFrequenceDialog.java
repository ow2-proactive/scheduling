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
package org.objectweb.proactive.ic2d.jmxmonitoring.dialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.NodeObject;


public class SetUpdateFrequenceDialog extends Dialog {
    //private NodeObject node;
    private Shell shell = null;
    private Text text;
    private Button okButton;
    private Button cancelButton;

    public SetUpdateFrequenceDialog(Shell parent, NodeObject node) {
        super(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);

        //this.node = node;

        /* Init the display */
        Display display = getParent().getDisplay();

        /* Init the shell */
        shell = new Shell(getParent(), SWT.BORDER | SWT.CLOSE);
        shell.setText("Spy Update Frequence");
        FormLayout layout = new FormLayout();
        layout.marginHeight = 5;
        layout.marginWidth = 5;
        shell.setLayout(layout);

        Label titleLabel = new Label(shell, SWT.NONE);
        titleLabel.setText("Please enter the new value for the frequence of the update for");
        FormData titleLabelFormData = new FormData();
        titleLabelFormData.left = new FormAttachment(0, 0);
        titleLabel.setLayoutData(titleLabelFormData);

        Label nameLabel = new Label(shell, SWT.NONE);
        nameLabel.setText(node.getName());
        FormData secondsLabelFormData = new FormData();
        secondsLabelFormData.top = new FormAttachment(titleLabel, 8);
        secondsLabelFormData.left = new FormAttachment(30, 0);
        nameLabel.setLayoutData(secondsLabelFormData);

        this.text = new Text(shell, SWT.BORDER);
        // TODO A faire ?
        /*text.setText(node.getSpy().getUpdateFrequence()+"");*/
        FormData textFormData = new FormData();
        textFormData.top = new FormAttachment(nameLabel, 5);
        textFormData.left = new FormAttachment(45, 0);
        textFormData.right = new FormAttachment(55, 0);
        text.setLayoutData(textFormData);

        // button "OK"
        this.okButton = new Button(shell, SWT.NONE);
        okButton.setText("OK");
        okButton.addSelectionListener(new SetUpdateFrequenceListener());
        FormData okFormData = new FormData();
        okFormData.top = new FormAttachment(text, 20);
        okFormData.left = new FormAttachment(25, 20);
        okFormData.right = new FormAttachment(50, -10);
        okButton.setLayoutData(okFormData);
        shell.setDefaultButton(okButton);

        // button "CANCEL"
        this.cancelButton = new Button(shell, SWT.NONE);
        cancelButton.setText("Cancel");
        cancelButton.addSelectionListener(new SetUpdateFrequenceListener());
        FormData cancelFormData = new FormData();
        cancelFormData.top = new FormAttachment(text, 20);
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

    //
    // -- INNER CLASS -----------------------------------------------
    //
    private class SetUpdateFrequenceListener extends SelectionAdapter {
        public void widgetSelected(SelectionEvent e) {
            if (e.widget == okButton) {
                // long updateFrequence = Long.parseLong(text.getText());

                // TODO A faire ?                
                /*node.getSpy().setUpdateFrequence(updateFrequence);*/
            }
            shell.close();
        }
    }
}
