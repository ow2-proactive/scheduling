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
package org.objectweb.proactive.infrastructuremanager.dialog;

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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.objectweb.proactive.core.Constants;
import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.objectweb.proactive.core.util.URIBuilder;
import org.objectweb.proactive.core.util.UrlBuilder;


public class IMDialogConnection extends Dialog {
    private Shell shell = null;
    private Text hostText;
    private Text portText;
    private Combo combo;
    private Button okButton;
    private Button cancelButton;
    private String url;
    private String nameView;
    private boolean accept = false;

    public IMDialogConnection(Shell parent) {
        // Pass the default styles here
        super(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);

        String initialHostValue = "localhost";
        String port = "";

        /* Get the machine's name */
        try {
            initialHostValue = URIBuilder.getLocalAddress().getHostName();
        } catch (UnknownHostException e) {
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
        shell.setText("Connect to an Infrastructure Manager");
        FormLayout layout = new FormLayout();
        layout.marginHeight = 5;
        layout.marginWidth = 5;
        shell.setLayout(layout);

        ////// group "Host to monitor"
        Group hostGroup = new Group(shell, SWT.NONE);
        hostGroup.setText("Infrastructure Manager to manage");
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
        if (port != null) {
            portText.setText(port);
        }
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
        combo = new Combo(hostGroup, SWT.DROP_DOWN);
        combo.add(Constants.RMI_PROTOCOL_IDENTIFIER);
        combo.add(Constants.XMLHTTP_PROTOCOL_IDENTIFIER);
        combo.add(Constants.IBIS_PROTOCOL_IDENTIFIER);
        combo.setText(Constants.RMI_PROTOCOL_IDENTIFIER);
        FormData protocolFormData = new FormData();
        protocolFormData.top = new FormAttachment(0, -1);
        protocolFormData.left = new FormAttachment(protocolLabel, 5);
        protocolFormData.right = new FormAttachment(100, 0);
        combo.setLayoutData(protocolFormData);

        // button "CANCEL"
        this.cancelButton = new Button(shell, SWT.NONE);
        cancelButton.setText("Cancel");
        cancelButton.addSelectionListener(new IMDialogConnectionButtonListener());
        FormData cancelFormData = new FormData();
        cancelFormData.top = new FormAttachment(hostGroup, 10);
        cancelFormData.left = new FormAttachment(25, 20);
        cancelFormData.right = new FormAttachment(50, -10);
        cancelButton.setLayoutData(cancelFormData);

        // button "OK"
        this.okButton = new Button(shell, SWT.NONE);
        okButton.setText("OK");
        okButton.addSelectionListener(new IMDialogConnectionButtonListener());
        FormData okFormData = new FormData();
        okFormData.top = new FormAttachment(hostGroup, 10);
        okFormData.left = new FormAttachment(50, 10);
        okFormData.right = new FormAttachment(75, -20);
        okButton.setLayoutData(okFormData);
        shell.setDefaultButton(okButton);

        shell.pack();
        shell.open();

        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
    }

    public boolean isAccept() {
        return accept;
    }

    public String getUrl() {
        return url;
    }

    public String getNameView() {
        return nameView;
    }

    private class IMDialogConnectionButtonListener extends SelectionAdapter {
        @Override
        public void widgetSelected(SelectionEvent e) {
            if (e.widget == okButton) {
                nameView = hostText.getText() + ":" + portText.getText();
                url = combo.getText().toLowerCase() + "://" + nameView;
                accept = true;
            }
            shell.close();
        }
    }
}
