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
package org.objectweb.proactive.ic2d.security.widgets;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;


/**
 * @author The ProActive Team
 * Dialog for adding virtual entity i.e. security entity described by their
 * name within the scope of an application
 */
public class VirtualEntityDialog extends Dialog {
    private static final int RESET_ID = IDialogConstants.NO_TO_ALL_ID + 1;
    private Text nameField;
    private EntityTableComposite entityTable;
    private Button okButton;
    private Button cancelButton;
    private Shell shell;
    private Combo entityType;

    public VirtualEntityDialog(Shell parentShell, EntityTableComposite fromTable) {
        super(parentShell, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);

        this.entityTable = fromTable;

        Display display = getParent().getDisplay();
        this.shell = new Shell(getParent(), SWT.BORDER | SWT.CLOSE);
        shell.setText("Adding a new virtual entity");
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        layout.marginHeight = 5;
        layout.marginWidth = 5;

        shell.setSize(320, 200);
        shell.setLayout(layout);

        new Label(shell, SWT.NULL).setText("Type:");

        entityType = new Combo(shell, SWT.NULL);

        entityType.setItems(new String[] { "Domain", "Runtime", "Node" });

        entityType.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));

        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        Label passwordLabel = new Label(shell, SWT.RIGHT);
        passwordLabel.setText("Name: ");

        nameField = new Text(shell, SWT.SINGLE);
        nameField.setText("CN=");
        data = new GridData(GridData.FILL_HORIZONTAL);
        nameField.setLayoutData(data);

        // button "OK"
        okButton = new Button(shell, SWT.NONE);
        okButton.setText("OK");
        okButton.addSelectionListener(new MonitorNewHostListener());
        //        FormData okFormData = new FormData();
        //        okFormData.top = new FormAttachment( /*depthLabel2*/
        //                passwordLabel, 20);
        //        okFormData.left = new FormAttachment(25, 20);
        //        okFormData.right = new FormAttachment(50, -10);
        //        okButton.setLayoutData(okFormData);
        shell.setDefaultButton(okButton);

        // button "CANCEL"
        cancelButton = new Button(shell, SWT.NONE);
        cancelButton.setText("Cancel");
        cancelButton.addSelectionListener(new MonitorNewHostListener());
        //        FormData cancelFormData = new FormData();
        //        cancelFormData.top = new FormAttachment( /*depthLabel2*/
        //                passwordLabel, 20);
        //        cancelFormData.left = new FormAttachment(50, 10);
        //        cancelFormData.right = new FormAttachment(75, -20);
        //        cancelButton.setLayoutData(cancelFormData);
        shell.pack();
        shell.open();

        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
    }

    //	protected void createButtonsForButtonBar(Composite parent) {
    //        super.createButtonsForButtonBar(parent);
    //        createButton(parent, RESET_ID, "Reset All", false);
    //    }
    protected void buttonPressed(int buttonId) {
        if (buttonId == RESET_ID) {
            nameField.setText("");
        }
    }

    //
    // -- INNER CLASS -----------------------------------------------
    //
    private class MonitorNewHostListener extends SelectionAdapter {
        @Override
        public void widgetSelected(SelectionEvent e) {
            if (e.widget == okButton) {
                entityTable.add(nameField.getText());
            }
            shell.close();
        }
    }
}
