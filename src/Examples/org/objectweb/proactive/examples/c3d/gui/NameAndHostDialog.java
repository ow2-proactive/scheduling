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
package org.objectweb.proactive.examples.c3d.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.ProActiveObject;
import org.objectweb.proactive.core.Constants;
import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.objectweb.proactive.core.config.ProProperties;
import org.objectweb.proactive.core.util.URIBuilder;
import org.objectweb.proactive.examples.c3d.C3DDispatcher;
import org.objectweb.proactive.examples.c3d.Dispatcher;


/** A dialog with two text fields, which handles incorrect entries.
 * It is used to select a dispatcher host and a user name.
 * Inspired from the java Swing Dialog tutorial */
public class NameAndHostDialog extends JDialog implements ActionListener,
    PropertyChangeListener {
    private String userName = "Bob";
    private JTextField userTextField;
    protected JTextField hostNameTextField;
    private JOptionPane optionPane;
    private String enterButtonString = "Enter";
    private String cancelButtonString = "Cancel";
    protected Dispatcher c3dDispatcher;

    /** This is NOT an Active Object: constructor is configurable! */
    public NameAndHostDialog() {
        super();

        String localHostUrl = NameAndHostDialog.getLocalHostUrl();

        setTitle("Welcome to the Collaborative 3D Environment.");

        this.userTextField = new JTextField(this.userName, 10);
        this.userTextField.addActionListener(this);

        this.hostNameTextField = new JTextField(localHostUrl, 10);
        this.hostNameTextField.addActionListener(this);

        //Create an array of the text and components to be displayed.
        Object[] array = {
                "Please enter your name,", this.userTextField,
                "and the C3DDispatcher host", this.hostNameTextField
            };

        //Create an array specifying the number of dialog buttons and their text.
        Object[] options = { this.enterButtonString, this.cancelButtonString };

        //Create the JOptionPane.
        this.optionPane = new JOptionPane(array, JOptionPane.PLAIN_MESSAGE,
                JOptionPane.YES_NO_OPTION, null, options, options[0]);

        //Make this dialog display it.
        setContentPane(this.optionPane);

        //Handle window closing correctly.
        addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent we) {
                    // handle closing behavior in propertyChange ()
                    NameAndHostDialog.this.optionPane.setValue(new Integer(
                            JOptionPane.CLOSED_OPTION));
                }
            });

        //Ensure the text field always gets the first focus.
        addComponentListener(new ComponentAdapter() {
                @Override
                public void componentShown(ComponentEvent ce) {
                    NameAndHostDialog.this.userTextField.requestFocusInWindow();
                }
            });

        //Register an event handler that reacts to option pane state changes.
        this.optionPane.addPropertyChangeListener(this);
        pack(); // find optimal size
        setModal(true); // cannot play with other windows when this one is visible
        setVisible(true);
    }

    /** Handles events for the text field. */
    public void actionPerformed(ActionEvent e) {
        this.optionPane.setValue(this.enterButtonString);
    }

    /** Reacts to state changes in the option pane. */
    public void propertyChange(PropertyChangeEvent event) {
        String prop = event.getPropertyName();

        if (isVisible() && (event.getSource() == this.optionPane) &&
                (JOptionPane.VALUE_PROPERTY.equals(prop) ||
                JOptionPane.INPUT_VALUE_PROPERTY.equals(prop))) {
            Object value = this.optionPane.getValue();

            if (value == JOptionPane.UNINITIALIZED_VALUE) {
                //ignore reset
                return;
            }

            //Reset the JOptionPane's value. If you don't do this, then if the user
            //presses the same button next time, no property change event will be fired.
            this.optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);

            if (this.enterButtonString.equals(value)) {
                this.userName = this.userTextField.getText();

                if (this.userName.equals("")) { //userName text was invalid
                    this.userName = "Bob";
                }

                // OK, now we've received enough information from the user. Let's try the lookup.
                tryTheLookup();
            } else { //user closed dialog or clicked cancel
                this.userName = null;
                setVisible(false);
            }
        }
    }

    /** Really try to find a dispatcher, using the provided address in the hostNameTextField. */
    protected void tryTheLookup() {
        String url = this.hostNameTextField.getText();
        String hostName = null;
        String[] registeredObjects;

        this.c3dDispatcher = null;
        // First try with the provided url, if the user entered the exact url for the dispatcher
        try {
            this.c3dDispatcher = (Dispatcher) ProActiveObject.lookupActive(C3DDispatcher.class.getName(),
                    url);
            setVisible(false);
            return;
        } catch (Exception e) {
            //e.printStackTrace();
            // do nothing, it's just not a correct dispatcher url
        }

        // Second, check the url given does map to a machine, and get list of registered objects on it
        try {
            hostName = URIBuilder.getHostNameFromUrl(url);
            registeredObjects = ProActiveObject.listActive(url);
        } catch (IOException e) {
            treatException(e,
                "Sorry, could not find a registered C3DDispatcher on host \"" +
                hostName + "\".");
            return;
        }

        // third, for every registered object, try to save it as a dispatcher
        for (int i = 0; i < registeredObjects.length; i++) {
            String name = URIBuilder.getNameFromURI(registeredObjects[i]);

            if (name.equals("Dispatcher") && (name.indexOf("_VN") == -1)) { // replace by (java 1.5 String.contains)
                try {
                    this.c3dDispatcher = (Dispatcher) ProActiveObject.lookupActive(C3DDispatcher.class.getName(),
                            registeredObjects[i]);
                    setVisible(false);
                    return;
                } catch (ActiveObjectCreationException e) {
                    treatException(e,
                        "Sorry, could not create stub for C3DDispatcher on host \"" +
                        hostName + "\".");
                } catch (IOException e) {
                }
            }
        }

        treatException(new IOException(
                "No such Active Object in registered Active Objects."),
            "Sorry, could not find a registered Dispatcher on host \"" +
            hostName + "\".");
    }

    /** Take action against failed connections to Dispatcher. */
    protected void treatException(Exception exception, String message) {
        this.hostNameTextField.selectAll();
        JOptionPane.showMessageDialog(NameAndHostDialog.this,
            message + "\nError is \n " + exception.getMessage(), "Try again",
            JOptionPane.ERROR_MESSAGE);
        this.hostNameTextField.requestFocusInWindow();
    }

    /** Always contains some characters, default value is Bob. */
    public String getValidatedUserName() {
        return this.userName;
    }

    /** Get the dispatcher which was found on url provided by the user.
     * @return a dispatcher if information provided was correct, and null if one wasn't properly selected.
     * It is up to the programmer to check for null values. */
    public Dispatcher getValidatedDispatcher() {
        return this.c3dDispatcher;
    }

    /** Gets the name of the machine this is running on.
     * @return a url which is suitable for looking up active objects. */
    public static String getLocalHostUrl() {
        String localhost = "";

        try {
            int port = -1;
            String protocol = ProProperties.PA_COMMUNICATION_PROTOCOL.getValue();

            if (!protocol.equals(Constants.IBIS_PROTOCOL_IDENTIFIER)) {
                port = Integer.parseInt(ProActiveConfiguration.getInstance()
                                                              .getProperty("proactive." +
                            protocol + ".port"));
            }

            localhost = URIBuilder.buildURI(URIBuilder.getHostNameorIP(
                        URIBuilder.getLocalAddress()), null, null, port)
                                  .toString();
        } catch (UnknownHostException e) {
            localhost = "";
        }

        return localhost;
    }
}
