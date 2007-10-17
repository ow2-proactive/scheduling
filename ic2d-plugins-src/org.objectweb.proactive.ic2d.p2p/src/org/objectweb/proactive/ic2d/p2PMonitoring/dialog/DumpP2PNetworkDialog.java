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
package org.objectweb.proactive.ic2d.p2PMonitoring.dialog;

import java.net.UnknownHostException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.objectweb.proactive.core.util.URIBuilder;
import org.objectweb.proactive.core.util.UrlBuilder;
import org.objectweb.proactive.ext.webservices.soap.ProActiveProvider;


public class DumpP2PNetworkDialog extends Dialog {
    private Shell shell = null;
    private Text hostText;
    private Text portText;
    private Text ttlText;
    private Button okButton;
    private Button cancelButton;
    private String URL;

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //
    public DumpP2PNetworkDialog(Shell parent) {
        // Pass the default styles here
        super(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
        //	System.out.println("DumpP2PNetworkDialog.DumpP2PNetworkDialog() " + parent);
        String initialHostValue = "localhost";
        String port = "";

        /* Get the machine's name */
        try {
            initialHostValue = URIBuilder.getHostNameorIP(URIBuilder.getLocalAddress());
        } catch (UnknownHostException e) {
            // TODO catch this exception, and do something
            e.printStackTrace();
        }

        /* Load the proactive default configuration */
        ProActiveConfiguration.load();

        /* Get the machine's port */
        port = System.getProperty("proactive.p2p.port");

        /* Init the display */
        Display display = getParent().getDisplay();
        shell = new Shell(getParent(), SWT.BORDER | SWT.CLOSE);
        shell.setText("Contacting P2P Network");
        shell.setSize(new Point(300, 200));

        FormLayout layout = new FormLayout();
        layout.marginHeight = 5;
        layout.marginWidth = 5;
        shell.setLayout(layout);

        Group hostGroup = new Group(shell, SWT.NONE);
        hostGroup.setText("Peer to contact");
        FormLayout hostLayout = new FormLayout();
        hostLayout.marginHeight = 5;
        hostLayout.marginWidth = 5;
        hostGroup.setLayout(hostLayout);
        FormData hostFormData1 = new FormData();
        hostFormData1.left = new FormAttachment(0, 0);
        hostFormData1.right = new FormAttachment(100, 0);
        hostGroup.setLayoutData(hostFormData1);

        Label hostLabel = new Label(hostGroup, SWT.NONE);
        hostLabel.setText("Name or IP :");

        this.hostText = new Text(hostGroup, SWT.BORDER);
        hostText.setText(initialHostValue);
        FormData hostFormData = new FormData();
        hostFormData.top = new FormAttachment(0, -1);
        hostFormData.left = new FormAttachment(hostLabel, 5);
        hostFormData.right = new FormAttachment(70, -10);
        hostText.setLayoutData(hostFormData);

        Label portLabel = new Label(hostGroup, SWT.NONE);
        portLabel.setText("Port :");
        FormData portFormData = new FormData();
        portFormData.left = new FormAttachment(70, 10);
        portLabel.setLayoutData(portFormData);

        this.portText = new Text(hostGroup, SWT.BORDER);
        if (port != null) {
            portText.setText(port);
        }
        FormData portFormData2 = new FormData();
        portFormData2.top = new FormAttachment(0, -1);
        portFormData2.left = new FormAttachment(portLabel, 5);
        portFormData2.right = new FormAttachment(100, 0);
        portText.setLayoutData(portFormData2);

        Label ttlLabel = new Label(shell, SWT.NONE);
        ttlLabel.setText("TTL of request :");
        FormData ttlFormData = new FormData();
        ttlFormData.top = new FormAttachment(hostGroup, 20);
        ttlFormData.left = new FormAttachment(0, 20);
        ttlLabel.setLayoutData(ttlFormData);

        this.ttlText = new Text(shell, SWT.BORDER);
        //	depthText.setText(MonitorThread.getInstance().getDepth()+"");
        FormData ttlFormData2 = new FormData();
        ttlFormData2.top = new FormAttachment(hostGroup, 17);
        ttlFormData2.left = new FormAttachment(ttlLabel, 5);
        ttlFormData2.right = new FormAttachment(100, -20);
        ttlText.setText("5");
        ttlText.setLayoutData(ttlFormData2);

        //		Label depthLabel2 = new Label(shell, SWT.CENTER);
        //	//	depthLabel2.setText("You can change it there or from menu \"Control -> Set depth control\"");
        //		FormData depthFormData3 = new FormData();
        //		depthFormData3.top = new FormAttachment(depthLabel, 5);
        //		depthLabel2.setLayoutData(depthFormData3);
        this.okButton = new Button(shell, SWT.NONE);
        okButton.setText("OK");
        okButton.addSelectionListener(new MonitorNewHostListener(this));
        FormData okFormData = new FormData();
        okFormData.top = new FormAttachment(ttlLabel, 20);
        okFormData.left = new FormAttachment(25, 20);
        okFormData.right = new FormAttachment(50, -10);
        okButton.setLayoutData(okFormData);

        this.cancelButton = new Button(shell, SWT.NONE);
        cancelButton.setText("Cancel");
        cancelButton.addSelectionListener(new MonitorNewHostListener(this));
        FormData cancelFormData = new FormData();
        cancelFormData.top = new FormAttachment(ttlLabel, 20);
        cancelFormData.left = new FormAttachment(50, 10);
        cancelFormData.right = new FormAttachment(75, -20);
        cancelButton.setLayoutData(cancelFormData);

        center(display, shell);

        shell.pack();
        shell.open();

        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }

        //display.dispose(); TODO ???
    }

    //
    // -- PRIVATE METHODS -----------------------------------------------
    //
    private static void center(Display display, Shell shell) {
        Rectangle rect = display.getClientArea();
        Point size = shell.getSize();
        int x = (rect.width - size.x) / 2;
        int y = (rect.height - size.y) / 2;
        shell.setLocation(new Point(x, y));
    }

    //
    // -- INNER CLASS -----------------------------------------------
    //
    private class MonitorNewHostListener extends SelectionAdapter {
        protected DumpP2PNetworkDialog dialog;

        public MonitorNewHostListener(DumpP2PNetworkDialog d) {
            this.dialog = d;
        }

        public void widgetSelected(SelectionEvent e) {
            if (e.widget == okButton) {
                // this.dialog.getUrl();
                this.dialog.buildURL();
                shell.close();
            } else if (e.widget == cancelButton) {
                shell.close();
            }
        }
    }

    protected void buildURL() {
        try {
            this.URL = "//" + hostText.getText() + ":" +
                Integer.parseInt(portText.getText());
        } catch (NumberFormatException e) {
            this.URL = "";
        }
    }

    public String getUrl() {
        return this.URL;
    }
}
