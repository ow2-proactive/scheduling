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
package org.objectweb.proactive.examples.hello;

import org.objectweb.proactive.core.config.ProActiveConfiguration;


/**
 * An Applet to display the results of calls on a (distant) Hello Active Object.
 * Can connect to an already deployed Hello if specified.
 * Makes a nice demo.
 */
public class HelloApplet extends org.objectweb.proactive.examples.StandardFrame {

    /** The active Hello object */
    private Hello activeHello;

    /** The remote node locator if runnning over the network */
    private org.objectweb.proactive.core.node.Node node;

    /** The label  */
    private javax.swing.JTextArea lMessage;
    private boolean shouldRun = true;

    public HelloApplet(String name, int width, int height, String nodeURL) {
        super(name, width, height);
        if (nodeURL == null) {
            node = null; // We'll create the objects locally
        } else {
            try {
                node = org.objectweb.proactive.core.node.NodeFactory.getNode(nodeURL); // We'll create the objects on the specified node
            } catch (org.objectweb.proactive.core.node.NodeException e) {
                node = null;
            }
        }
    }

    public static void main(String[] args) {
        // checks for the server's URL
        String nodeURL = null;
        if (args.length > 0) {
            // Lookups the server object
            nodeURL = args[0];
        }
        ProActiveConfiguration.load();
        new HelloApplet("Hello applet", 300, 200, nodeURL);
    }

    @Override
    public void start() {
        receiveMessage("Applet creating active objects");
        receiveMessage("on node " +
            ((node == null) ? "local" : node.getNodeInformation().getURL()));
        try {
            activeHello = (Hello) org.objectweb.proactive.api.PAActiveObject.newActive(Hello.class.getName(),
                    null, node);
        } catch (Exception e) {
            // There has been a problem...
            receiveMessage("Error while initializing");
            lMessage.setText("Error... Did you set the security attributes?");
            e.printStackTrace(); // Print the exception to stderr
            return;
        }
        receiveMessage("Ok");
        Thread t = new Thread(new HelloTimer(), "Hello clock");
        t.start();
    }

    public void stop() {
        shouldRun = false;
        activeHello = null;
        lMessage.setText("Applet stopped");
    }

    @Override
    protected javax.swing.JPanel createRootPanel() {
        javax.swing.JPanel rootPanel = new javax.swing.JPanel();

        // Layout 
        rootPanel.setBackground(java.awt.Color.white);
        rootPanel.setForeground(java.awt.Color.blue);
        lMessage = new javax.swing.JTextArea("Please wait...........");
        rootPanel.add(lMessage);
        return rootPanel;
    }

    private class HelloTimer implements Runnable {
        public void run() {
            while (shouldRun) {
                lMessage.setText(activeHello.sayHello().toString());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }
            }
        }
    }
}
