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
package org.ow2.proactive.resourcemanager.gui.dialog;

import java.util.Vector;

import org.eclipse.jface.dialogs.MessageDialog;
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
import org.eclipse.swt.widgets.Text;
import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.ow2.proactive.resourcemanager.exception.RMException;
import org.ow2.proactive.resourcemanager.gui.data.RMStore;


/**
 * This class allow to pop up a dialogue to create a dynamic source node.
 * 
 * @author The ProActive Team
 */
public class CreateDynamicSourceDialog extends Dialog {

    // -------------------------------------------------------------------- //
    // --------------------------- constructor ---------------------------- //
    // -------------------------------------------------------------------- //
    private CreateDynamicSourceDialog(Shell parent) {

        // Pass the default styles here
        super(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);

        // Load the proactive default configuration
        ProActiveConfiguration.load();

        // Init the display
        Display display = parent.getDisplay();

        // Init the shell
        final Shell shell = new Shell(parent, SWT.BORDER | SWT.CLOSE);
        shell.setText("Create a dynamic node source");
        FormLayout layout = new FormLayout();
        layout.marginHeight = 5;
        layout.marginWidth = 5;
        shell.setLayout(layout);

        // creation
        Label urlLabel = new Label(shell, SWT.NONE);
        final Text url = new Text(shell, SWT.SINGLE | SWT.BORDER);
        Label nameLabel = new Label(shell, SWT.NONE);
        final Text name = new Text(shell, SWT.SINGLE | SWT.BORDER);
        Label nbLabel = new Label(shell, SWT.NONE);
        final Text nb = new Text(shell, SWT.SINGLE | SWT.BORDER);
        Label ttrLabel = new Label(shell, SWT.NONE);
        final Text ttr = new Text(shell, SWT.SINGLE | SWT.BORDER);
        Label niceLabel = new Label(shell, SWT.NONE);
        final Text nice = new Text(shell, SWT.SINGLE | SWT.BORDER);
        Button okButton = new Button(shell, SWT.NONE);
        Button cancelButton = new Button(shell, SWT.NONE);

        // label url
        urlLabel.setText("Url :");
        FormData urlLabelFormData = new FormData();
        urlLabelFormData.top = new FormAttachment(url, 0, SWT.CENTER);
        urlLabel.setLayoutData(urlLabelFormData);

        // text url
        FormData urlFormData = new FormData();
        urlFormData.top = new FormAttachment(0, 0);
        urlFormData.left = new FormAttachment(urlLabel, 5);
        urlFormData.right = new FormAttachment(100, -5);
        urlFormData.width = 320;
        url.setLayoutData(urlFormData);

        // label name
        nameLabel.setText("Name :");
        FormData nameLabelFormData = new FormData();
        nameLabelFormData.top = new FormAttachment(name, 0, SWT.CENTER);
        nameLabel.setLayoutData(nameLabelFormData);

        // text name
        FormData nameFormData = new FormData();
        nameFormData.top = new FormAttachment(url, 5);
        nameFormData.left = new FormAttachment(nameLabel, 5);
        nameFormData.right = new FormAttachment(100, -5);
        nameFormData.width = 320;
        name.setLayoutData(nameFormData);

        // label nb
        nbLabel.setText("Max number of nodes the node source has to provide :");
        FormData nbLabelFormData = new FormData();
        nbLabelFormData.top = new FormAttachment(nb, 0, SWT.CENTER);
        nbLabel.setLayoutData(nbLabelFormData);

        // text nb
        FormData nbFormData = new FormData();
        nbFormData.top = new FormAttachment(name, 5);
        nbFormData.left = new FormAttachment(nbLabel, 5);
        nbFormData.right = new FormAttachment(100, -5);
        nbFormData.width = 50;
        nb.setLayoutData(nbFormData);

        // label ttr
        ttrLabel.setText("Time during the node will be kept by the node source (in sec) :");
        FormData ttrLabelFormData = new FormData();
        ttrLabelFormData.top = new FormAttachment(ttr, 0, SWT.CENTER);
        ttrLabel.setLayoutData(ttrLabelFormData);

        // text ttr
        FormData ttrFormData = new FormData();
        ttrFormData.top = new FormAttachment(nb, 5);
        ttrFormData.left = new FormAttachment(ttrLabel, 5);
        ttrFormData.right = new FormAttachment(100, -5);
        ttrFormData.width = 50;
        ttr.setLayoutData(ttrFormData);

        // label nice
        niceLabel.setText("Time to wait between a node remove and a new node acquisition (in sec) :");
        FormData niceLabelFormData = new FormData();
        niceLabelFormData.top = new FormAttachment(nice, 0, SWT.CENTER);
        niceLabel.setLayoutData(niceLabelFormData);

        // text nice
        FormData niceFormData = new FormData();
        niceFormData.top = new FormAttachment(ttr, 5);
        niceFormData.left = new FormAttachment(niceLabel, 5);
        niceFormData.right = new FormAttachment(100, -5);
        niceFormData.width = 50;
        nice.setLayoutData(niceFormData);

        // button "OK"
        okButton.setText("OK");
        okButton.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                int nb2 = 0;
                int ttr2 = 0;
                int nice2 = 0;

                if (url.getText().equals("")) {
                    MessageDialog.openError(shell, "Error", "You didn't enter an url for you node source");
                    return;
                }
                if (name.getText().equals("")) {
                    MessageDialog.openError(shell, "Error", "You didn't enter a name for you node source");
                    return;
                }
                try {
                    nb2 = new Integer(nb.getText());
                } catch (NumberFormatException e) {
                    MessageDialog.openError(shell, "Error",
                            "You didn't enter an integer for the number of nodes");
                    return;
                }
                try {
                    ttr2 = new Integer(ttr.getText());
                } catch (NumberFormatException e) {
                    MessageDialog.openError(shell, "Error",
                            "You didn't enter an integer for the time to release");
                    return;
                }
                try {
                    nice2 = new Integer(nice.getText());
                } catch (NumberFormatException e) {
                    MessageDialog.openError(shell, "Error", "You didn't enter an integer for the nice time");
                    return;
                }

                Vector<String> tmp = new Vector<String>();
                tmp.add(url.getText());
                try {
                    RMStore.getInstance().getRMAdmin().createDynamicNodeSource(name.getText(), nb2,
                            nice2 * 1000, ttr2 * 1000, tmp);
                    shell.close();
                } catch (RMException e) {
                    MessageDialog.openError(shell, "Error", "Node Source name already existing...");
                }
            }
        });

        FormData okFormData = new FormData();
        okFormData.top = new FormAttachment(nice, 5);
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
        cancelFormData.top = new FormAttachment(nice, 5);
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
        new CreateDynamicSourceDialog(parent);
    }
}
