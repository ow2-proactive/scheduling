/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2005 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.ic2d.monitoring.dialog;

import java.net.UnknownHostException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.objectweb.proactive.core.util.UrlBuilder;
import org.objectweb.proactive.ic2d.console.Console;
import org.objectweb.proactive.ic2d.monitoring.data.HostObject;
import org.objectweb.proactive.ic2d.monitoring.data.Protocol;
import org.objectweb.proactive.ic2d.monitoring.data.WorldObject;
import org.objectweb.proactive.ic2d.monitoring.exceptions.HostAlreadyExistsException;


public class MonitorNewHostDialog extends Dialog {

	private Shell shell = null;
	private Shell parent =null;

	private Text hostText;
	private Text portText;
	private Combo combo;
	private Text depthText;
	private Button okButton;
	private Button cancelButton;
	
	/** The World */
	private WorldObject world;
	
	//
	// -- CONSTRUCTORS -----------------------------------------------
	//

	public MonitorNewHostDialog(Shell parent, WorldObject world) {
		// Pass the default styles here
		super(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);

		this.parent = parent;
		this.world = world;
		
		String initialHostValue = "localhost";
		String port = "";

		/* Get the machine's name */
		try {
			initialHostValue = UrlBuilder.getHostNameorIP(java.net.InetAddress.getLocalHost());
		} catch (UnknownHostException e) {
			// TODO catch this exception, and do something
			e.printStackTrace();
		}

		/* Load the proactive default configuration */
		ProActiveConfiguration.load();

		/* Get the machine's port */
		port = System.getProperty("proactive.rmi.port");

		/* Init the display */
		Display display = getParent().getDisplay();
		
		/* Init the shell */
		shell = new Shell(getParent(), SWT.BORDER | SWT.CLOSE);
		shell.setText("Adding host and depth to monitor");
		FormLayout layout = new FormLayout();
		layout.marginHeight = 5;
		layout.marginWidth = 5;
		shell.setLayout(layout);

		////// group "Host to monitor"
		Group hostGroup = new Group(shell, SWT.NONE);
		hostGroup.setText("Host to monitor");
		FormLayout hostLayout = new FormLayout();
		hostLayout.marginHeight = 5;
		hostLayout.marginWidth = 5;
		hostGroup.setLayout(hostLayout);
		FormData hostFormData1 = new FormData();
		hostFormData1.left = new FormAttachment(0, 0);
		hostFormData1.right = new FormAttachment(100, 0);
		hostGroup.setLayoutData(hostFormData1);

		// label "Name or IP"
		Label hostLabel = new Label(hostGroup, SWT.NONE);
		hostLabel.setText("Name or IP :");
		
		// text hostname or IP
		this.hostText = new Text(hostGroup, SWT.BORDER);
		hostText.setText(initialHostValue);
		FormData hostFormData = new FormData();
		hostFormData.top = new FormAttachment(0, -1);
		hostFormData.left = new FormAttachment(hostLabel, 5);
		hostFormData.right = new FormAttachment(50, -5);
		hostText.setLayoutData(hostFormData);

		// label "Port"
		Label portLabel = new Label(hostGroup, SWT.NONE);
		portLabel.setText("Port :");
		FormData portFormData = new FormData();
		portFormData.left = new FormAttachment(50, 5);
		portLabel.setLayoutData(portFormData);

		// text port
		this.portText = new Text(hostGroup, SWT.BORDER);
		if(port != null) portText.setText(port);
		FormData portFormData2 = new FormData();
		portFormData2.top = new FormAttachment(0, -1);
		portFormData2.left = new FormAttachment(portLabel, 5);
		portFormData2.right = new FormAttachment(70, 0);
		portText.setLayoutData(portFormData2);

		// label "Protocol"
		Label protocolLabel = new Label(hostGroup, SWT.NONE);
		protocolLabel.setText("Protocol :");
		FormData protocolFormData1 = new FormData();
		protocolFormData1.left = new FormAttachment(70, 5);
		protocolLabel.setLayoutData(protocolFormData1);
		
		// combo protocols
		combo = new Combo(hostGroup,SWT.DROP_DOWN);
		combo.add(Protocol.RMI.toString().toUpperCase());
		combo.add(Protocol.HTTP.toString().toUpperCase());
		combo.add(Protocol.IBIS.toString().toUpperCase());
		combo.add(Protocol.JINI.toString().toUpperCase());
		combo.setText("RMI");
		FormData protocolFormData = new FormData();
		protocolFormData.top = new FormAttachment(0, -1);
		protocolFormData.left = new FormAttachment(protocolLabel, 5);
		protocolFormData.right = new FormAttachment(100, 0);
		combo.setLayoutData(protocolFormData);

		// label depth
		Label depthLabel = new Label(shell, SWT.NONE);
		depthLabel.setText("Hosts will be recursively searched up to a depth of :");
		FormData depthFormData = new FormData();
		depthFormData.top = new FormAttachment(hostGroup, 20);
		depthFormData.left = new FormAttachment(15, 0);
		depthLabel.setLayoutData(depthFormData);

		// text depth
		this.depthText = new Text(shell, SWT.BORDER);
		depthText.setText(world.getMonitorThread().getDepth()+"");
		FormData depthFormData2 = new FormData();
		depthFormData2.top = new FormAttachment(hostGroup, 17);
		depthFormData2.left = new FormAttachment(depthLabel, 5);
		depthFormData2.right = new FormAttachment(85, 0);
		depthText.setLayoutData(depthFormData2);

		// label set depth control
		Label depthLabel2 = new Label(shell, SWT.CENTER);
		depthLabel2.setText("You can change it there or from menu \"Control -> Set depth control\"");
		FormData depthFormData3 = new FormData();
		depthFormData3.top = new FormAttachment(depthLabel, 5);
		depthFormData3.left = new FormAttachment(8, 0);
		//depthFormData3.right = new FormAttachment(85, 0);
		depthLabel2.setLayoutData(depthFormData3);

		// button "OK"
		this.okButton = new Button(shell, SWT.NONE);
		okButton.setText("OK");
		okButton.addSelectionListener(new MonitorNewHostListener());
		FormData okFormData = new FormData();
		okFormData.top = new FormAttachment(depthLabel2, 20);
		okFormData.left = new FormAttachment(25, 20);
		okFormData.right = new FormAttachment(50, -10);
		okButton.setLayoutData(okFormData);
		shell.setDefaultButton(okButton);
		
		// button "CANCEL"
		this.cancelButton = new Button(shell, SWT.NONE);
		cancelButton.setText("Cancel");
		cancelButton.addSelectionListener(new MonitorNewHostListener());
		FormData cancelFormData = new FormData();
		cancelFormData.top = new FormAttachment(depthLabel2, 20);
		cancelFormData.left = new FormAttachment(50, 10);
		cancelFormData.right = new FormAttachment(75, -20);
		cancelButton.setLayoutData(cancelFormData);

		shell.pack();
		shell.open();


		while(!shell.isDisposed()) {
			if(!display.readAndDispatch())
				display.sleep();
		}

		//display.dispose(); TODO ???
	}


	//
	// -- PRIVATE METHODS -----------------------------------------------
	//

	/**
	 * Logs in the IC2D's console, and show a pop-up.
	 * @param message
	 */
	private void displayMessage(final String message) {
		System.out.println("MonitorNewHostDialog.displayMessage()");
		// Print the message in the UI Thread in async mode
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				Console.getInstance("IC2D").warn(message);
				MessageBox mb = new MessageBox(parent);
				mb.setMessage(message);
				mb.open();
			}});

	}

	//
	// -- INNER CLASS -----------------------------------------------
	//

	private class MonitorNewHostListener extends SelectionAdapter {
		String hostname;
		int port ;
		Protocol protocol;

		public void widgetSelected(SelectionEvent e) {
			if(e.widget == okButton) {
				hostname = hostText.getText();
				port = Integer.parseInt(portText.getText());
				protocol = Protocol.getProtocolFromString((combo.getText()));
				world.getMonitorThread().setDepth(Integer.parseInt(depthText.getText()));
				new Thread(){
					public void run(){
						try {
							new HostObject(hostname, port, protocol, world);
						} catch (HostAlreadyExistsException e) {
							displayMessage(e.getMessage());
						}
					}
				}.start();
			}
			shell.close();
		}
	}
}
