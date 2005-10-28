/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive-support@inria.fr
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
package org.objectweb.proactive.examples.c3d.gui;

import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import org.objectweb.proactive.examples.c3d.UserLogic;
import org.objectweb.proactive.examples.c3d.geom.Vec;


/**
 * An implementation of User GUI, which handles all the generated events.
 */
public class UserGUIImpl extends UserGUI {
    private UserLogic c3dUser;

    /** Event handler, which transforms events into method calls */
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source == rightButton) { // Request 'rotate right' with button click
            c3dUser.rotateScene(new Vec(0, Math.PI / 4, 0));
        } else if (source == leftButton) {
            c3dUser.rotateScene(new Vec(0, -Math.PI / 4, 0));
        } else if (source == upButton) {
            c3dUser.rotateScene(new Vec(Math.PI / 4, 0, 0));
        } else if (source == downButton) {
            c3dUser.rotateScene(new Vec(-Math.PI / 4, 0, 0));
        } else if (source == spinRight) {
            c3dUser.rotateScene(new Vec(0, 0, Math.PI / 4));
        } else if (source == spinLeft) {
            c3dUser.rotateScene(new Vec(0, 0, -Math.PI / 4));
        } else if ((source == exitMenuItem)) {
            this.c3dUser.terminate();
            trash();
        } else if ((source == this.localMessageField) ||
                (source == this.sendMessageButton)) {
            String message = this.localMessageField.getText();
            if (message.length() > 0) {
                String recipient = (String) sendToComboBox.getSelectedItem();
                c3dUser.sendMessage(message, recipient);
                localMessageField.setText("");
            } else {
                localMessageField.setText("Enter text to send");
                localMessageField.selectAll();
            }
        } else if (source == this.addSphereButton) {
            c3dUser.addSphere();
        } else if (source == this.resetSceneButton) {
            c3dUser.resetScene();
        } else if (source == userInfoItem) {
            c3dUser.showUserInfo();
        } else if (source == listUsersMenuItem) {
            c3dUser.getUserList();
        } else if (source == clearMenuItem) {
            logArea.setText("");
        } else if (source == aboutMenuItem) {
            new DialogBox(this.mainFrame, "About ProActive",
                "The ProActive Grid Middleware",
                "http://ProActive.ObjectWeb.org/");
        } else {
            log("EVENT not handled : " + source);
        }
    }

    /** Constructor, which mostly adds a window Listener */
    public UserGUIImpl(String title, final UserLogic c3dUser, final int width,
        final int height) {
        super(title, width, height);
        this.c3dUser = c3dUser;
        mainFrame.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    c3dUser.terminate();
                    trash();
                }
            });
    }
}
