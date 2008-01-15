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
package org.objectweb.proactive.extensions.resourcemanager.gui.dialog;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.objectweb.proactive.api.PADeployment;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.extensions.resourcemanager.exception.RMException;
import org.objectweb.proactive.extensions.resourcemanager.gui.data.RMStore;

/**
 * This class allow to pop up a dialogue to create a static source node.
 * 
 * @author FRADJ Johann
 */
public class CreateStaticSourceDialog extends Dialog {

	// -------------------------------------------------------------------- //
	// --------------------------- constructor ---------------------------- //
	// -------------------------------------------------------------------- //
	private CreateStaticSourceDialog(Shell parent) {

		// Pass the default styles here
		super(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);

		// Load the proactive default configuration
		ProActiveConfiguration.load();

		// Init the display
		Display display = parent.getDisplay();

		// Init the shell
		final Shell shell = new Shell(parent, SWT.BORDER | SWT.CLOSE);
		shell.setText("Create a static node source");
		FormLayout layout = new FormLayout();
		layout.marginHeight = 5;
		layout.marginWidth = 5;
		shell.setLayout(layout);

		// creation
		Label nameLabel = new Label(shell, SWT.NONE);
		final Text nameText = new Text(shell, SWT.SINGLE | SWT.BORDER);
		Label ddLabel = new Label(shell, SWT.NONE);
		final Text ddText = new Text(shell, SWT.SINGLE | SWT.BORDER | SWT.READ_ONLY);
		Button chooseButton = new Button(shell, SWT.NONE);
		Button okButton = new Button(shell, SWT.NONE);
		Button cancelButton = new Button(shell, SWT.NONE);

		// label sourceName
		nameLabel.setText("Name :");
		FormData urlLabelFormData = new FormData();
		urlLabelFormData.top = new FormAttachment(nameText, 0, SWT.CENTER);
		nameLabel.setLayoutData(urlLabelFormData);

		// combo sourceName
		FormData urlFormData = new FormData();
		urlFormData.top = new FormAttachment(0, 0);
		urlFormData.left = new FormAttachment(nameLabel, 5);
		urlFormData.right = new FormAttachment(100, -5);
		urlFormData.width = 320;
		nameText.setLayoutData(urlFormData);

		// label sourceName
		ddLabel.setText("Deployement descriptor :");
		FormData ddLabelFormData = new FormData();
		ddLabelFormData.top = new FormAttachment(ddText, 0, SWT.CENTER);
		ddLabel.setLayoutData(ddLabelFormData);

		// combo sourceName
		FormData ddFormData = new FormData();
		ddFormData.top = new FormAttachment(nameText, 5);
		ddFormData.left = new FormAttachment(ddLabel, 5);
		ddFormData.width = 200;
		ddText.setLayoutData(ddFormData);

		// button "Choose file"
		chooseButton.setText("Choose file");
		chooseButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				FileDialog fileDialog = new FileDialog(shell, SWT.OPEN);
				fileDialog.setFilterExtensions(new String[] { "*.xml" });
                fileDialog.setText("Choose a deployement descriptor");
				String fileName = fileDialog.open();
				ddText.setText(fileName);
			}
		});

		FormData chooseFormData = new FormData();
		chooseFormData.top = new FormAttachment(ddText, 0, SWT.CENTER);
		chooseFormData.left = new FormAttachment(ddText, 5);
		chooseFormData.right = new FormAttachment(100, -5);
		chooseButton.setLayoutData(chooseFormData);

		// button "OK"
		okButton.setText("OK");
		okButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				if (nameText.getText().equals(""))
					MessageDialog.openError(shell, "Error", "You didn't enter a name");
				else {
					try {
						List<ProActiveDescriptor> padList = new ArrayList<ProActiveDescriptor>();
						if (!ddText.getText().equals("")) {
							ProActiveDescriptor pad = PADeployment.getProactiveDescriptor(ddText.getText());
							padList.add(pad);
						}
						RMStore.getInstance().getRMAdmin()
								.createStaticNodesource(nameText.getText(), padList);
						shell.close();
					} catch (ProActiveException e) {
						MessageDialog.openError(shell, "Error",
								"You didn't choose a good deployement descriptor file");
					} catch (RMException e) {
						MessageDialog.openError(shell, "Error", "Node Source name already existing");
					}
				}
			}
		});

		FormData okFormData = new FormData();
		okFormData.top = new FormAttachment(ddText, 5);
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
		cancelFormData.top = new FormAttachment(ddText, 5);
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
		new CreateStaticSourceDialog(parent);
	}
}
