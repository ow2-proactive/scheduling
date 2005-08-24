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
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.rmi.UnknownHostException;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.examples.c3d.C3DDispatcher;



/** 
 * A dialog with two text fields.
 * Handles incorrect entries.
 * Inspired from the java Swing Dialog tutorial
 */
public class NameAndHostDialog extends JDialog implements ActionListener, PropertyChangeListener {
    private String userName = null;
    private String hostName = null;
    private JTextField userTextField;
    private JTextField hostNameTextField;
    private JOptionPane optionPane;
    private String enterButtonString = "Enter";
    private String cancelButtonString = "Cancel";
    private C3DDispatcher c3dDispatcher;
    
    public NameAndHostDialog(String localHost) {
        super();
        setTitle("Welcome to the Collaborative 3D Environment.");
        
        userTextField = new JTextField(10);
        userTextField.setText("Bob");
        userTextField.addActionListener(this);
        
        hostNameTextField = new JTextField(10);
        hostNameTextField.setText(localHost);
        hostNameTextField.addActionListener(this);
        
        
        //Create an array of the text and components to be displayed.
        Object[] array = { "Please enter your name,", userTextField, "and the C3DDispatcher host", hostNameTextField };
        
        //Create an array specifying the number of dialog buttons and their text.
        Object[] options = { enterButtonString, cancelButtonString };
        
        //Create the JOptionPane.
        optionPane = new JOptionPane(
                array, JOptionPane.PLAIN_MESSAGE, JOptionPane.YES_NO_OPTION, null, options,
                options[0]);
        
        //Make this dialog display it.
        setContentPane(optionPane);
        
        //Handle window closing correctly.
        addWindowListener(
                new WindowAdapter() {
                    public void windowClosing(WindowEvent we) {
                        // handle closing behavior in propertyChange () 
                        optionPane.setValue(new Integer(JOptionPane.CLOSED_OPTION));
                    }
                });
        
        //Ensure the text field always gets the first focus.
        addComponentListener(
                new ComponentAdapter() {
                    public void componentShown(ComponentEvent ce) {
                        userTextField.requestFocusInWindow();
                    }
                });
        
        //Register an event handler that reacts to option pane state changes.
        optionPane.addPropertyChangeListener(this);
        pack(); // find optimal size
        setModal(true); // cannot play with other windows when this one is visible
        setVisible(true);
    }
    
    /** This method handles events for the text field. */
    public void actionPerformed(ActionEvent e) {
        optionPane.setValue(enterButtonString);
    }
    
    /** This method reacts to state changes in the option pane. */
    public void propertyChange(PropertyChangeEvent event) {
        String prop = event.getPropertyName();
        
        if (
                isVisible() && (event.getSource() == optionPane) &&
                (JOptionPane.VALUE_PROPERTY.equals(prop) ||
                        JOptionPane.INPUT_VALUE_PROPERTY.equals(prop))) {
            Object value = optionPane.getValue();
            
            if (value == JOptionPane.UNINITIALIZED_VALUE) {
                //ignore reset
                return;
            }
            
            //Reset the JOptionPane's value. If you don't do this, then if the user
            //presses the same button next time, no property change event will be fired.
            optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);
            
            if (enterButtonString.equals(value)) {
                this.userName = userTextField.getText();
                if (this.userName.equals("")) { //userName text was invalid
                    userName = "Bob";
                }
                this.hostName = hostNameTextField.getText() ;
                
                try {
                    this.c3dDispatcher = (C3DDispatcher) ProActive.lookupActive(
                            C3DDispatcher.class.getName(), 
                            "//" + hostName + "/Dispatcher"
                    );
                    setVisible(false);
                } catch (UnknownHostException exception) {
                    treatException(exception , 
                            "Sorry, could not connect to host \"" + hostName + 
                    "\".\nMaybe you should check for typos?");
                } catch (IOException exception) {
                    treatException(exception , 
                            "Sorry, could not find a registered C3DDispatcher on host \"" + hostName +
                    "\".\nMaybe you should create one, or try another host?");
                } catch (ActiveObjectCreationException exception) {
                    exception.printStackTrace();        // this exception is a problem, for sure!
                    treatException(exception , 
                            "Sorry, could not create stub for C3DDispatcher on host \"" + hostName + "\"." );
                }
            } else { //user closed dialog or clicked cancel
                userName = hostName = null;
                setVisible(false);
            }
        }
    }
    
    private void treatException(Exception exception, String message) {
        hostNameTextField.selectAll();
        JOptionPane.showMessageDialog(
                NameAndHostDialog.this,
                message + "\nError is \n " +  exception.getMessage(), 
                "Try again", 
                JOptionPane.ERROR_MESSAGE);
        hostName = null;
        hostNameTextField.requestFocusInWindow();
    }
    
    /** Always contains some characters, default value is Bob. */
    public String getValidatedUserName() {
        return userName;
    }
    
    /**
     * @return a dispatcher if information provided was correct, and null if coudn't find one.
     * It is up to the programmer to check for null values.
     */
    public C3DDispatcher getValidatedDispatcher() {
        return this.c3dDispatcher;
    }
}
