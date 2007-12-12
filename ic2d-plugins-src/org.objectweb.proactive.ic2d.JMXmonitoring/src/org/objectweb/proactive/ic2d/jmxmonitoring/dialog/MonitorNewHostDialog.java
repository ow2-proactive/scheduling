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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
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
import org.objectweb.proactive.core.config.PAProperties;
import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.objectweb.proactive.core.util.ProActiveInet;
import org.objectweb.proactive.core.util.URIBuilder;
import org.objectweb.proactive.ic2d.console.Console;
import org.objectweb.proactive.ic2d.jmxmonitoring.Activator;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.WorldObject;


public class MonitorNewHostDialog extends Dialog {
    private Shell shell = null;

    //private Shell parent = null;
    private Combo hostCombo;
    private Text portText;
    private Combo protocolCombo;
    private Text depthText;
    private Button okButton;
    private Button cancelButton;

    /** The World */
    private WorldObject world;

    /** Host name */
    String initialHostValue = "localhost";

    // Name of the file
    String file = ".urls";
    // <hostName,url>
    Map<String, String> urls = new HashMap<String, String>();

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //
    public MonitorNewHostDialog(Shell parent, WorldObject world) {
        // Pass the default styles here
        super(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);

        //this.parent = parent;
        this.world = world;

        String port = "";

        /* Get the machine's name */
        initialHostValue = ProActiveInet.getInstance().getHostname();

        /* Load the proactive default configuration */
        ProActiveConfiguration.load();

        /* Get the machine's port */
        port = PAProperties.PA_RMI_PORT.getValue();

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
        hostCombo = new Combo(hostGroup, SWT.BORDER);
        FormData hostFormData = new FormData();
        hostFormData.top = new FormAttachment(0, -1);
        hostFormData.left = new FormAttachment(hostLabel, 5);
        hostFormData.right = new FormAttachment(50, -5);
        hostCombo.setLayoutData(hostFormData);
        hostCombo.addSelectionListener(new SelectionListener() {
                public void widgetSelected(SelectionEvent e) {
                    String hostName = hostCombo.getText();
                    String url = urls.get(hostName);
                    Integer port = URIBuilder.getPortNumber(url);
                    String protocol = URIBuilder.getProtocol(url);
                    portText.setText(port.toString());
                    protocolCombo.setText(protocol);
                }

                public void widgetDefaultSelected(SelectionEvent e) {
                    String text = hostCombo.getText();
                    if (hostCombo.indexOf(text) < 0) { // Not in the list yet.
                        hostCombo.add(text);

                        // Re-sort
                        String[] items = hostCombo.getItems();
                        Arrays.sort(items);
                        hostCombo.setItems(items);
                        hostCombo.setText(text);
                    }
                }
            });

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
        protocolCombo = new Combo(hostGroup, SWT.DROP_DOWN);
        protocolCombo.add(Constants.RMI_PROTOCOL_IDENTIFIER);
        protocolCombo.add(Constants.XMLHTTP_PROTOCOL_IDENTIFIER);
        protocolCombo.add(Constants.IBIS_PROTOCOL_IDENTIFIER);
        protocolCombo.setText(Constants.RMI_PROTOCOL_IDENTIFIER);
        FormData protocolFormData = new FormData();
        protocolFormData.top = new FormAttachment(0, -1);
        protocolFormData.left = new FormAttachment(protocolLabel, 5);
        protocolFormData.right = new FormAttachment(100, 0);
        protocolCombo.setLayoutData(protocolFormData);

        // Load Urls
        loadUrls();

        // label depth
        Label depthLabel = new Label(shell, SWT.NONE);
        depthLabel.setText(
            "Hosts will be recursively searched up to a depth of :");
        FormData depthFormData = new FormData();
        depthFormData.top = new FormAttachment(hostGroup, 20);
        depthFormData.left = new FormAttachment(15, 0);
        depthLabel.setLayoutData(depthFormData);

        // text depth
        this.depthText = new Text(shell, SWT.BORDER);
        depthText.setText(Integer.toString(world.getDepth()));
        FormData depthFormData2 = new FormData();
        depthFormData2.top = new FormAttachment(hostGroup, 17);
        depthFormData2.left = new FormAttachment(depthLabel, 5);
        depthFormData2.right = new FormAttachment(85, 0);
        depthText.setLayoutData(depthFormData2);

        // label set depth control
        /*Label depthLabel2 = new Label(shell, SWT.CENTER);
        depthLabel2.setText("You can change it there or from menu \"Control -> Set depth control\"");
        FormData depthFormData3 = new FormData();
        depthFormData3.top = new FormAttachment(depthLabel, 5);
        depthFormData3.left = new FormAttachment(8, 0);
        //depthFormData3.right = new FormAttachment(85, 0);
        depthLabel2.setLayoutData(depthFormData3);*/

        // button "OK"
        this.okButton = new Button(shell, SWT.NONE);
        okButton.setText("OK");
        okButton.addSelectionListener(new MonitorNewHostListener());
        FormData okFormData = new FormData();
        okFormData.top = new FormAttachment( /*depthLabel2*/
                depthLabel, 20);
        okFormData.left = new FormAttachment(25, 20);
        okFormData.right = new FormAttachment(50, -10);
        okButton.setLayoutData(okFormData);
        shell.setDefaultButton(okButton);

        // button "CANCEL"
        this.cancelButton = new Button(shell, SWT.NONE);
        cancelButton.setText("Cancel");
        cancelButton.addSelectionListener(new MonitorNewHostListener());
        FormData cancelFormData = new FormData();
        cancelFormData.top = new FormAttachment( /*depthLabel2*/
                depthLabel, 20);
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

        //display.dispose(); TODO ???
    }

    //
    // -- PRIVATE METHODS -----------------------------------------------
    //

    /**
     * Logs in the IC2D's console, and show a pop-up.
     * @param message
     */

    //    private void displayMessage(final String message) {
    //        System.out.println("MonitorNewHostDialog.displayMessage()");
    //        // Print the message in the UI Thread in async mode
    //        Display.getDefault().asyncExec(new Runnable() {
    //                public void run() {
    //                    Console.getInstance("IC2D").warn(message);
    //                    MessageBox mb = new MessageBox(parent);
    //                    mb.setMessage(message);
    //                    mb.open();
    //                }
    //            });
    //    }

    /**
     * Load Urls
     */
    @SuppressWarnings("unchecked")
    private void loadUrls() {
        try {
            BufferedReader reader = null;
            String url = null;
            reader = new BufferedReader(new FileReader(file));
            try {
                Set<String> hostNames = new TreeSet<String>();
                String lastNameUsed = null;
                Integer lastPortUsed = null;
                String lastProtocolUsed = null;

                while ((url = reader.readLine()) != null) {
                    if ((url == null) || url.equals("")) {
                        url = URIBuilder.buildURIFromProperties(initialHostValue,
                                "").toString();
                    }
                    lastNameUsed = URIBuilder.getHostNameFromUrl(url);
                    lastPortUsed = URIBuilder.getPortNumber(url);
                    lastProtocolUsed = URIBuilder.getProtocol(url);
                    hostNames.add(lastNameUsed);
                    urls.put(lastNameUsed, url);
                }

                String[] t = { "" };
                String[] hosts = null;
                if (hostNames.isEmpty()) {
                    url = URIBuilder.buildURIFromProperties(initialHostValue, "")
                                    .toString();
                    lastNameUsed = URIBuilder.getHostNameFromUrl(url);
                    lastPortUsed = URIBuilder.getPortNumber(url);
                    lastProtocolUsed = URIBuilder.getProtocol(url);
                    hostNames.add(lastNameUsed);
                }
                hosts = (new ArrayList<String>(hostNames)).toArray(t);
                Arrays.sort(hosts);
                hostCombo.setItems(hosts);
                hostCombo.setText(lastNameUsed);
                portText.setText(lastPortUsed.toString());
                protocolCombo.setText(lastProtocolUsed);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    Console.getInstance(Activator.CONSOLE_NAME).logException(e);
                }
            }
        } catch (FileNotFoundException e) {
            hostCombo.add(initialHostValue);
            hostCombo.setText(initialHostValue);
            String defaultURL = URIBuilder.buildURIFromProperties(initialHostValue,
                    "").toString();
            urls.put(initialHostValue, defaultURL);
            recordUrl(defaultURL);
        }
    }

    /**
     * Record an url
     * @param url
     */
    private void recordUrl(String url) {
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(file, false));
            PrintWriter pw = new PrintWriter(bw, true);
            String host = URIBuilder.getHostNameFromUrl(url);
            if (urls.size() > 1) {
                urls.remove(host);
            }

            // Record urls
            Iterator<String> it = urls.values().iterator();
            while (it.hasNext()) {
                pw.println(it.next());
            }
            // Record the last URL used at the end of the file
            // in order to find it easily for the next time
            pw.println(url);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                bw.close();
            } catch (IOException e) {
                e.printStackTrace();
                Console.getInstance(Activator.CONSOLE_NAME).logException(e);
            }
        }
    }

    //
    // -- INNER CLASS -----------------------------------------------
    //
    private class MonitorNewHostListener extends SelectionAdapter {
        String hostname;
        int port;
        String protocol;

        @Override
        public void widgetSelected(SelectionEvent e) {
            if (e.widget == okButton) {
                hostname = hostCombo.getText();
                port = Integer.parseInt(portText.getText());
                protocol = protocolCombo.getText();
                final String url = URIBuilder.buildURI(hostname, "", protocol,
                        port).toString();
                recordUrl(url);
                world.setDepth(Integer.parseInt(depthText.getText()));
                //				new Thread() {
                //					public void run() {
                world.addHost(url);
                //					}
                //				}.start();
            }
            shell.close();
        }
    }
}
