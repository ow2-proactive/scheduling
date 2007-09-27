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
package org.objectweb.proactive.examples.philosophers;

import org.objectweb.proactive.core.config.ProActiveConfiguration;


public class AppletPhil extends org.objectweb.proactive.examples.StandardFrame {
    //  private javax.swing.JButton bStart;
    private String url;
    private DinnerLayout theLayout;
    private javax.swing.JPanel theLayoutPanel;

    public AppletPhil(String name, int width, int height) {
        super(name, width, height);
    }

    public static void main(String[] args) {
        ProActiveConfiguration.load();
        AppletPhil phil = new AppletPhil("Philosophers", 450, 300);
        phil.receiveMessage("Applet running...");
        if (args.length == 1) {
            phil.setURL(args[0]);
        }
        phil.go();
    }

    private void go() {
        try {
            /* le Layout est necessairement actif, puisqu'il est referenc? par tous les autres objets.
             */
            theLayout = (DinnerLayout) org.objectweb.proactive.api.ProActiveObject.turnActive(theLayout);
            if (url != null) {
                theLayout.setNode(url);
            }

            /*
             * Builds the active Table and Philosophers:
             */
            org.objectweb.proactive.api.ProFuture.waitFor(theLayout.init());
            theLayout.activateButtons();
            receiveMessage("Objects activated...");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void setURL(String url) {
        this.url = url;
    }

    /* Called by AppletWrapper before creating the toplevel Frame: */
    @Override
    protected void start() {
    }

    //
    // -- PROTECTED METHODS -----------------------------------------------
    //

    /* createRootPanel:
     * abstract method of AppletWrapper.
     * result: the JPanel to be inserted in the upper part of the Main Frame.
     */
    @Override
    protected javax.swing.JPanel createRootPanel() {
        // Get the images
        javax.swing.Icon[] imgArray = new javax.swing.Icon[5];
        try {
            ClassLoader c = this.getClass().getClassLoader();
            imgArray[0] = new javax.swing.ImageIcon(c.getResource(
                        "org/objectweb/proactive/examples/philosophers/think.gif"));
            imgArray[1] = new javax.swing.ImageIcon(c.getResource(
                        "org/objectweb/proactive/examples/philosophers/wait.gif"));
            imgArray[2] = new javax.swing.ImageIcon(c.getResource(
                        "org/objectweb/proactive/examples/philosophers/eat.gif"));
            imgArray[3] = new javax.swing.ImageIcon(c.getResource(
                        "org/objectweb/proactive/examples/philosophers/fork0.gif"));
            imgArray[4] = new javax.swing.ImageIcon(c.getResource(
                        "org/objectweb/proactive/examples/philosophers/fork1.gif"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        // the DinnerLayout constructor creates the graphical objects:
        theLayout = new DinnerLayout(imgArray);
        return theLayout.getDisplay();
    }
}
