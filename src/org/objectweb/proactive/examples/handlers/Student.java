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

package org.objectweb.proactive.examples.handlers;

// Exceptions and handlers
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.exceptions.communication.*;
import org.objectweb.proactive.core.exceptions.creation.*;
import org.objectweb.proactive.core.exceptions.group.*;
import org.objectweb.proactive.core.exceptions.migration.*;
import org.objectweb.proactive.core.exceptions.security.*;
import org.objectweb.proactive.core.exceptions.service.*;
import org.objectweb.proactive.core.exceptions.handler.*;

// Package importation
import java.awt.*; 
import java.awt.event.*;
import javax.swing.*;
import javax.swing.JTabbedPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JFrame;
import java.io.*;

public class Student extends JFrame {

    // Server reference
    Computer computer;

    // Constructor 
    public Student(String[] args) {

	// Add JComponents to window
	super("Exceptions generator");

	// Panels Creation
	JTabbedPane tabbedPane = new JTabbedPane();
	getContentPane().add(tabbedPane);
	JPanel panelAppUp = new JPanel();
	JPanel panelAppMiddle = new JPanel(new GridLayout(3,2));
	JPanel panelAppDown = new JPanel();
	panelAppUp.setBorder(BorderFactory.createRaisedBevelBorder());
	panelAppMiddle.setBorder(BorderFactory.createRaisedBevelBorder());
	panelAppDown.setBorder(BorderFactory.createRaisedBevelBorder());
	JPanel panelApp = new JPanel();
	panelApp.setLayout(new BoxLayout(panelApp, BoxLayout.Y_AXIS));
	panelApp.add(panelAppUp);
	panelApp.add(panelAppMiddle);
	panelApp.add(panelAppDown);

	JPanel panelGen = new JPanel();	
	panelGen.setLayout(new BoxLayout(panelGen, BoxLayout.Y_AXIS));

	JPanel panelHandlers = new JPanel();
	panelHandlers.setLayout(new BoxLayout(panelHandlers, BoxLayout.Y_AXIS));

	tabbedPane.addTab("Application", panelApp);
	tabbedPane.addTab("Generator", panelGen);	
	tabbedPane.addTab("Handlers", panelHandlers);

	JLabel labelTitle = new JLabel("Res. of second degree equation");
	JLabel labelResult = new JLabel("Delta =. --- X1 =. --- X2 =.");
	panelAppUp.add(labelTitle);

	panelAppMiddle.setLayout(new GridLayout(3,2));
	final JTextField aTextField = new JTextField("0", 10);
	final JTextField bTextField = new JTextField("0", 10);
	final JTextField cTextField = new JTextField("0", 10);
	panelAppMiddle.add(new JLabel("       A = "));
	panelAppMiddle.add(aTextField);
	panelAppMiddle.add(new JLabel("       B = "));
	panelAppMiddle.add(bTextField);
	panelAppMiddle.add(new JLabel("       C = "));
	panelAppMiddle.add(cTextField);

	JButton computeButton = new JButton("Compute results !");
	computeButton.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		try {
		    int a = java.lang.Integer.parseInt(aTextField.getText());
		    int b = java.lang.Integer.parseInt(bTextField.getText());
		    int c = java.lang.Integer.parseInt(cTextField.getText());
		    compute(a, b, c);
		} catch (NumberFormatException ex) {
		    System.out.println("\n*** Error : all number must be correct integers");
		}	
	    }
	});
	panelAppDown.add(computeButton);

	// Create strings containing important exceptions
	final String[] generExceptions = {"ProActiveCommunicationException", "ProActiveCreationException", "ProActiveGroupException", 
					  "ProActiveMigrationException", "ProActiveSecurityException", "ProActiveServiceException" };
	final String[] commuExceptions = {"ReceiveCommunicationException", "ReceiveReplyCommunicationException", "ReceiveRequestCommunicationException", 
					  "SendCommunicationException", "SendReplyCommunicationException", "SendRequestCommunicationException"};
	final String[] creatExceptions = {"ActiveObjectCreationException", "FutureCreationException", "ReifyObjectCreationException"};
	final String[] groupExceptions = {"CreationGroupException", "ReceiveGroupException", "ReceiveReplyGroupException", "ReceiveRequestGroupException", 
					  "SendGroupException", "SendReplyGroupException", "SendRequestGroupException"};
	final String[] migraExceptions = {"OnArrivalMigrationException", "OnDepartureMigrationException", "SerializationMigrationException"};
	final String[] securExceptions = {"ActiveObjectCreationSecurityException", "AuthentificationSecurityException", "DecryptionSecurityException", 
					  "IntegritySecurityException", "MigrationSecurityException", "ReceiveReplySecurityException", "ReceiveRequestSecurityException", 
					  "ReceiveSecurityException", "SendReplySecurityException", "SendRequestSecurityException", "SendSecurityException"};
	final String[] serviExceptions = {"ServiceFailedServiceException"};

        // Create combo boxes
	JLabel genLabel = new JLabel("Choose an exception to raise");
	genLabel.setAlignmentX(CENTER_ALIGNMENT);
        final JComboBox generList = new JComboBox(generExceptions);
	final JComboBox speciList = new JComboBox(commuExceptions);
	
	// Add combo boxes to generator panel
	panelGen.add(Box.createRigidArea(new Dimension(10, 7)));
	panelGen.add(genLabel);
	panelGen.add(Box.createRigidArea(new Dimension(10, 8)));
	panelGen.add(generList);
	panelGen.add(speciList);
	panelGen.add(Box.createRigidArea(new Dimension(10, 8)));
	generList.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JComboBox cb = (JComboBox) e.getSource();
                int index = cb.getSelectedIndex();
		speciList.removeAllItems();
		switch (index) {
		case 0 : for (int i=0; i<commuExceptions.length; i++) speciList.addItem(commuExceptions[i]); break;
		case 1 : for (int i=0; i<creatExceptions.length; i++) speciList.addItem(creatExceptions[i]); break;
		case 2 : for (int i=0; i<groupExceptions.length; i++) speciList.addItem(groupExceptions[i]); break;
		case 3 : for (int i=0; i<migraExceptions.length; i++) speciList.addItem(migraExceptions[i]); break;
		case 4 : for (int i=0; i<securExceptions.length; i++) speciList.addItem(securExceptions[i]); break;
		case 5 : for (int i=0; i<serviExceptions.length; i++) speciList.addItem(serviExceptions[i]); break;
		}
            }
        });

	// Add generate button to generator panel
	JButton genButton = new JButton("Generate !");
	genButton.setAlignmentX(CENTER_ALIGNMENT);
	panelGen.add(genButton);
	panelGen.add(Box.createRigidArea(new Dimension(10, 7)));
	genButton.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent action) {

		String ex = (String) speciList.getSelectedItem();
		System.out.println("\n*** Generate ProActiveException");
		System.out.println("             |_ " + (String) generList.getSelectedItem());
		System.out.println("                |_ " + ex);

		switch (generList.getSelectedIndex()) {
		case 0 : ex = "org.objectweb.proactive.core.exceptions.communication." + ex; break;
		case 1 : ex = "org.objectweb.proactive.core.exceptions.creation." + ex; break;
		case 2 : ex = "org.objectweb.proactive.core.exceptions.group." + ex; break;
		case 3 : ex = "org.objectweb.proactive.core.exceptions.migration." + ex; break;
		case 4 : ex = "org.objectweb.proactive.core.exceptions.security." + ex; break;
		case 5 : ex = "org.objectweb.proactive.core.exceptions.service." + ex; break;
		}
		BufferedWriter out = null;
		try {
		    out = new BufferedWriter(new FileWriter("bench"));
		} catch (IOException e) {
		    e.printStackTrace();
		}
		HandlerCommunicationException h = new HandlerCommunicationException();
		int nbIteration = 1;
		int nbIterMax = 1;
		int nbIterBegin = 0;

		while (nbIterBegin++ < nbIterMax) {

		    long t1 = System.currentTimeMillis();
		    
		    for (int i=0; i<nbIteration; i++) {
			try {
			    raiseException(ex);
			} catch (ProActiveException e) {
			    h.handle(e);
			}
		    }
		    
		    long t2 = System.currentTimeMillis();
		    
		    // Exception Handling
		    for (int i=0; i<nbIteration; i++) {
			try {
			    raiseException(ex);
			} catch (ProActiveException e) {
			    
			    HandlerInterface hi = null;

			    // Search for an appropriate handler first at VM level and then at default level
			    if ((hi = ProActive.searchExceptionHandler(ProActive.VMLevel, e)) != null) {
				System.out.println("*** Handler " + hi.toString() + " used at VM level");
				continue;
			    }
			    
			    if ((hi = ProActive.searchExceptionHandler(ProActive.defaultLevel, e)) != null) {
				System.out.println("*** Handler " + hi.toString() + " used at default level");
				continue;
			    }
				    
			    // When no handlers has been found
			    System.out.println("*** No Handler has been found for " + e.toString());
			}
		    }
		    
		    long t3 = System.currentTimeMillis();
		    try {
			out.write("" + nbIteration + " \t " + (t2 - t1) +" \t " + (t3 - t2));
			out.newLine();
		    } catch (IOException e) {
			e.printStackTrace();
		    }
		    System.out.println("Benchs for " + nbIteration + " iterations are over");
		    nbIteration *= 2;
		}
		try {
		    out.close();
		} catch (IOException e) {
		    e.printStackTrace();
		}
	    }
	});

	// Configure handler panel
	JLabel handlersLabel = new JLabel("Add an handler dynamically");
	handlersLabel.setAlignmentX(CENTER_ALIGNMENT);
	final JComboBox handlersCombo1 = new JComboBox(generExceptions);
	final JComboBox handlersCombo2 = new JComboBox(commuExceptions);
	JButton handlerButton = new JButton("Add handler to VM level");
	handlerButton.setAlignmentX(CENTER_ALIGNMENT);
	panelHandlers.add(Box.createRigidArea(new Dimension(10, 7)));
	panelHandlers.add(handlersLabel);
	panelHandlers.add(Box.createRigidArea(new Dimension(10, 8)));
	panelHandlers.add(handlersCombo1);
	panelHandlers.add(handlersCombo2);
	panelHandlers.add(Box.createRigidArea(new Dimension(10, 8)));
	panelHandlers.add(handlerButton);
	panelHandlers.add(Box.createRigidArea(new Dimension(10, 7)));

	// ActionListener to choose exception handlers
	handlersCombo1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JComboBox cb = (JComboBox) e.getSource();
                int index = cb.getSelectedIndex();
		handlersCombo2.removeAllItems();
		switch (index) {
		case 0 : for (int i=0; i<commuExceptions.length; i++) handlersCombo2.addItem(commuExceptions[i]); break;
		case 1 : for (int i=0; i<creatExceptions.length; i++) handlersCombo2.addItem(creatExceptions[i]); break;
		case 2 : for (int i=0; i<groupExceptions.length; i++) handlersCombo2.addItem(groupExceptions[i]); break;
		case 3 : for (int i=0; i<migraExceptions.length; i++) handlersCombo2.addItem(migraExceptions[i]); break;
		case 4 : for (int i=0; i<securExceptions.length; i++) handlersCombo2.addItem(securExceptions[i]); break;
		case 5 : for (int i=0; i<serviExceptions.length; i++) handlersCombo2.addItem(serviExceptions[i]); break;
		}
            }
        });
	
	// ActionListener to add new exception handlers
	handlerButton.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent action) {
		String exname = (String) handlersCombo2.getSelectedItem();
		String hname = "org.objectweb.proactive.core.exceptions.handler.Handler";
		switch (handlersCombo1.getSelectedIndex()) {
		case 0 : exname = "org.objectweb.proactive.core.exceptions.communication." + exname; 
		    hname += "CommunicationException";
		    break;
		case 1 : exname = "org.objectweb.proactive.core.exceptions.creation." + exname; 
		    hname += "CreationException";
		    break;
		case 2 : exname = "org.objectweb.proactive.core.exceptions.group." + exname; 
		    hname += "GroupException";
		    break;
		case 3 : exname = "org.objectweb.proactive.core.exceptions.migration." + exname;
 		    hname += "MigrationException";
		    break;
		case 4 : exname = "org.objectweb.proactive.core.exceptions.security." + exname; 
		    hname += "SecurityException";
		    break;
		case 5 : exname = "org.objectweb.proactive.core.exceptions.service." + exname; 
		    hname += "ServiceException";
		    break;
		}
		try {
		    ProActive.setExceptionHandler(ProActive.VMLevel, hname, exname);
		} catch (Exception e) {
		    if (e instanceof ClassNotFoundException)
			System.out.println("*** Error : Cannot find class " + exname + " or " + hname);
		    else
			e.printStackTrace();
		}
	    }
	});
	// Action listener for the quit event
	addWindowListener(new WindowAdapter() {
	    public void windowClosing(WindowEvent e) {System.exit(0);}
	});

	// Configure window appearence
	setSize(320, 240);
	pack();
	setVisible(true);
	
	// Initialize server
	initialize(args);
    }

    public void initialize(String[] args) {
	
	computer = null;

	try {
	    
	    // checks the URL of the server
	    if (args.length == 0) {
		
		// There is no url to the server, so create an active server within this VM
		computer = (Computer) org.objectweb.proactive.ProActive.newActive(Computer.class.getName(), null);
		java.net.InetAddress localhost = java.net.InetAddress.getLocalHost();
		org.objectweb.proactive.ProActive.register(computer, "//" + localhost.getHostName() + "/Computer");
	    } else {
		// Lookups the server object
		System.out.println("Server is located on " + args[0]);
		computer = (Computer) org.objectweb.proactive.ProActive.lookupActive(Computer.class.getName(), args[0]);
	    }

	} catch (Exception e) {
	    System.err.println("Could not reach or create the server");
	    e.printStackTrace();
	    System.exit(1);
	}
    }

    public void compute(int a, int b, int c) {

	// Invokes a remote method on this object to get the message
	if (computer != null) {
	    if (computer.secondDegreeEquation(a, b, c))
		System.out.println("\n" + computer.getLastSolution());
	    else
		System.out.println("\n*** Error : check coefficients");
	}
    }

    public void raiseException(String classOfException) throws ProActiveException {

	// System.out.println("\n*** Load and raise " + classOfException);

	// We load dynamically the class of the exception
	ProActiveException ex = null;
	try {
	    ex = (ProActiveException) Class.forName(classOfException).newInstance();
	} catch (Exception e) {
	    if (e instanceof ClassNotFoundException)
		System.out.println("*** Error : Cannot find class " + ex);
	    else
		e.printStackTrace();
	}

	// We throw the exception
	throw ex;
    }


    // Main program
    public static void main(String[] args) {
 
	Student student = new Student(args);
    }
}
