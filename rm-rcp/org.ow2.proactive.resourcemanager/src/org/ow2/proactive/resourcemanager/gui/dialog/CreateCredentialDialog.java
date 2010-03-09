/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 * 				Nice-Sophia Antipolis/ActiveEon
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
package org.ow2.proactive.resourcemanager.gui.dialog;

import java.security.KeyException;

import org.eclipse.core.runtime.IStatus;
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
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.ow2.proactive.authentication.Authentication;
import org.ow2.proactive.authentication.Connection;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.resourcemanager.Activator;


/**
 * This class allow to pop up a dialogue to remove a source node.
 *
 * @author The ProActive Team
 */
public class CreateCredentialDialog extends Dialog {

    private byte[] creds;

    // -------------------------------------------------------------------- //
    // --------------------------- constructor ---------------------------- //
    // -------------------------------------------------------------------- //
    public CreateCredentialDialog(Shell parent, String source) {

        // Pass the default styles here
        super(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);

        // Load the proactive default configuration
        ProActiveConfiguration.load();

        // Init the display
        Display display = parent.getDisplay();

        // Init the shell
        final Shell shell = new Shell(parent, SWT.BORDER | SWT.CLOSE);
        shell.setText("Create credential");
        FormLayout layout = new FormLayout();
        layout.marginHeight = 5;
        layout.marginWidth = 5;
        shell.setLayout(layout);

        // creation
        Label authNameLabel = new Label(shell, SWT.NONE);
        final Combo authEntityCombo = new Combo(shell, SWT.BORDER | SWT.READ_ONLY);
        Button okButton = new Button(shell, SWT.NONE);
        Button cancelButton = new Button(shell, SWT.NONE);

        // label sourceName
        authNameLabel.setText("Credential for ");
        FormData formData = new FormData();
        formData.top = new FormAttachment(0, 5);
        formData.width = 100;
        authNameLabel.setLayoutData(formData);

        formData = new FormData();
        formData.top = new FormAttachment(0, -1);
        formData.left = new FormAttachment(authNameLabel, 5);
        formData.right = new FormAttachment(100, -5);
        formData.width = 320;
        authEntityCombo.setLayoutData(formData);
        authEntityCombo.setItems(new String[] { "Resource Manager", "Scheduler" });
        authEntityCombo.select(0);

        Label urlLabel = new Label(shell, SWT.NONE);
        final Text urlText = new Text(shell, SWT.BORDER);
        urlLabel.setText("Url ");
        formData = new FormData();
        formData.top = new FormAttachment(authNameLabel, 15);
        formData.width = 100;
        urlLabel.setLayoutData(formData);

        formData = new FormData();
        formData.left = new FormAttachment(urlLabel, 5);
        formData.top = new FormAttachment(authEntityCombo, 5);
        formData.width = 320;
        urlText.setLayoutData(formData);

        // login
        Label loginLabel = new Label(shell, SWT.NONE);
        final Text loginText = new Text(shell, SWT.BORDER);
        loginLabel.setText("Login ");
        formData = new FormData();
        formData.top = new FormAttachment(urlLabel, 15);
        formData.width = 100;
        loginLabel.setLayoutData(formData);

        formData = new FormData();
        formData.top = new FormAttachment(urlText, 5);
        formData.left = new FormAttachment(loginLabel, 5);
        formData.width = 320;
        loginText.setLayoutData(formData);

        // password
        Label passwordLabel = new Label(shell, SWT.NONE);
        final Text passwordText = new Text(shell, SWT.BORDER | SWT.PASSWORD);
        passwordLabel.setText("Password ");
        formData = new FormData();
        formData.top = new FormAttachment(loginLabel, 15);
        formData.width = 100;
        passwordLabel.setLayoutData(formData);

        formData = new FormData();
        formData.top = new FormAttachment(loginText, 5);
        formData.left = new FormAttachment(passwordLabel, 5);
        formData.width = 320;
        passwordText.setLayoutData(formData);

        final Button saveCredentialCheck = new Button(shell, SWT.CHECK);
        saveCredentialCheck.setText("Save credential to the file");

        formData = new FormData();
        formData.top = new FormAttachment(passwordLabel, 5);
        formData.width = 320;
        saveCredentialCheck.setLayoutData(formData);

        final Text fileNameText = new Text(shell, SWT.BORDER);
        fileNameText.setEnabled(false);
        formData = new FormData();
        formData.top = new FormAttachment(saveCredentialCheck, 5);
        formData.width = 320;
        fileNameText.setLayoutData(formData);

        final Button chooseButton = new Button(shell, SWT.NONE);
        chooseButton.setEnabled(false);
        chooseButton.setText("Choose");
        chooseButton.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                FileDialog fileDialog = new FileDialog(shell, SWT.SAVE);
                String fileName = fileDialog.open();
                if (fileName != null)
                    fileNameText.setText(fileName);
            }
        });

        formData = new FormData();
        formData.left = new FormAttachment(fileNameText, 5);
        formData.top = new FormAttachment(saveCredentialCheck, 5);
        formData.width = 70;
        chooseButton.setLayoutData(formData);

        saveCredentialCheck.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                if (saveCredentialCheck.getSelection()) {
                    fileNameText.setEnabled(true);
                    chooseButton.setEnabled(true);
                } else {
                    fileNameText.setEnabled(false);
                    chooseButton.setEnabled(false);
                }
            }
        });

        // button "OK"
        okButton.setText("Create");
        okButton.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {

                String entity = authEntityCombo.getSelectionIndex() == 0 ? "RMAUTHENTICATION" : "SCHEDULER";
                String url = urlText.getText();
                String authAOUrl = Connection.normalize(url) + entity;

                try {
                    Authentication auth = (Authentication) PAActiveObject.lookupActive(Authentication.class
                            .getName(), authAOUrl);
                    Credentials credentials = Credentials.createCredentials(loginText.getText(), passwordText
                            .getText(), auth.getPublicKey());
                    creds = credentials.getBase64();
                    if (saveCredentialCheck.getSelection() && fileNameText.getText().length() > 0) {
                        Activator.log(IStatus.INFO, "Saving credential to " + fileNameText.getText(), null);
                        try {
                            credentials.writeToDisk(fileNameText.getText());
                        } catch (KeyException e) {
                            MessageDialog.openError(shell, "Credentials saving error",
                                    "Cannot save credentials. \n\n" + e.getMessage());
                        }
                    }
                } catch (Exception e) {
                    Activator.log(IStatus.ERROR, "Cannot create credentials" + urlText.getText(), e);
                    MessageDialog.openError(shell, "Credentials creation Error",
                            "Cannot create credentials. \n\n" + e.getMessage());
                }
                shell.close();
            }
        });

        formData = new FormData();
        formData.top = new FormAttachment(fileNameText, 5);
        formData.left = new FormAttachment(25, 20);
        formData.right = new FormAttachment(50, -10);
        okButton.setLayoutData(formData);
        shell.setDefaultButton(okButton);

        // button "CANCEL"
        cancelButton.setText("Cancel");
        cancelButton.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                shell.close();
            }
        });

        formData = new FormData();
        formData.top = new FormAttachment(fileNameText, 5);
        formData.left = new FormAttachment(50, 10);
        formData.right = new FormAttachment(75, -20);
        cancelButton.setLayoutData(formData);

        shell.pack();
        shell.open();

        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
    }

    public byte[] getCredentials() {
        return creds;
    }
}
