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
package org.objectweb.proactive.ext.security.crypto;
import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.Security;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 *  This class provides a graphical interface for the Certificates Generation.
 *
 * @author     Vincent RIBAILLIER
 * <br>created    July 19, 2001
 */
public class CertificateGenerator {
	boolean packFrame = false;


	/**
	 *  Constructor for the CertificateGenerator object
	 *
	 * @since
	 */
	public CertificateGenerator() {
		CertificateGeneratorFrame frame = new CertificateGeneratorFrame();

		// Validate frames that have preset sizes
		// Pack frames that have useful preferred size info, e.g. from their layout
		if (packFrame) {
			frame.pack();
		}
		else {
			frame.validate();
		}

		// Center the window
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension frameSize = frame.getSize();

		if (frameSize.height > screenSize.height) {
			frameSize.height = screenSize.height;
		}

		if (frameSize.width > screenSize.width) {
			frameSize.width = screenSize.width;
		}

		frame.setLocation((screenSize.width - frameSize.width) / 2,
				(screenSize.height - frameSize.height) / 2);
		frame.setVisible(true);
	}


	/**
	 *  The main program for the CertificateGenerator class
	 *
	 * @param  args  The command line arguments
	 * @since
	 */
	public static void main(String[] args) {

        /*
	 * try {
	 * UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
	 * }
	 * catch(Exception e) {
	 * e.printStackTrace();
	 * }
	 */
		new CertificateGenerator();
	}

}


/**
 *  Description of the Class
 *
 * @author     Vincent RIBAILLIER
 * <br>created    July 19, 2001
 */
class CertificateGeneratorFrame extends JFrame {


	CertificateGeneratorPannel myPanel = new CertificateGeneratorPannel();
	JPanel contentPane;
	BorderLayout borderLayout1 = new BorderLayout();


	/**
	 *  Constructor for the CertificateGeneratorFrame object
	 *
	 * @since
	 */
	public CertificateGeneratorFrame() {
		enableEvents(AWTEvent.WINDOW_EVENT_MASK);

		try {
			jbInit();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}


	/**
	 *  Description of the Method
	 *
	 * @param  e  Description of Parameter
	 * @since
	 */
	protected void processWindowEvent(WindowEvent e) {
		super.processWindowEvent(e);

		if (e.getID() == WindowEvent.WINDOW_CLOSING) {
			System.exit(0);
		}
	}


	/**
	 *  Description of the Method
	 *
	 * @exception  Exception  Description of Exception
	 * @since
	 */
	private void jbInit() throws Exception {

		// setIconImage(Toolkit.getDefaultToolkit().createImage(FrameCertificateGenerator.class.getResource("[Your Icon]")));
		contentPane = (JPanel) this.getContentPane();

		contentPane.setLayout(borderLayout1);
		contentPane.add(myPanel, BorderLayout.CENTER);
		this.setSize(new Dimension(400, 300));
		this.setTitle("Certificate Generator");
	}

}


/**
 *  Description of the Class
 *
 * @author     Vincent RIBAILLIER
 * <br>created    July 19, 2001
 */
class CertificateGeneratorPannel extends JPanel {


	BorderLayout borderLayout1 = new BorderLayout();
	JPanel jPanel1 = new JPanel();
	GridLayout gridLayout1 = new GridLayout();
	JLabel jLabel1 = new JLabel();
	JTextField jDomain = new JTextField();
	JLabel jLabel2 = new JLabel();
	JTextField jValidity = new JTextField();
	JLabel jLabel3 = new JLabel();
	JTextField jPublic = new JTextField();
	JLabel jLabel4 = new JLabel();
	JTextField jPrivate = new JTextField();
	JButton jButton1 = new JButton();
	JLabel jLabel5 = new JLabel();
	JTextField jACPublic = new JTextField();
	JLabel jLabel6 = new JLabel();
	JTextArea jACPrivate = new JTextArea();


	/**
	 *  Constructor for the CertificateGeneratorPannel object
	 *
	 * @since
	 */
	public CertificateGeneratorPannel() {
		Provider myProvider =
				new org.bouncycastle.jce.provider.BouncyCastleProvider();

		Security.addProvider(myProvider);

		try {
			jbInit();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}


	/**
	 *  Description of the Method
	 *
	 * @param  e  Description of Parameter
	 * @since
	 */
	void jDomain_actionPerformed(ActionEvent e) {
	}


	/**
	 *  Description of the Method
	 *
	 * @param  e  Description of Parameter
	 * @since
	 */
	void jButton1_actionPerformed(ActionEvent e) {
		try {
			FileInputStream fin = new FileInputStream(jACPrivate.getText());
			ObjectInputStream in = new ObjectInputStream(fin);
			PrivateKey acPrivateKey = (PrivateKey) in.readObject();

			in.close();

			fin = new FileInputStream(jACPublic.getText());
			in = new ObjectInputStream(fin);

			PublicKey acPublicKey = (PublicKey) in.readObject();

			in.close();

			ProactiveCertificateFactory certif_factory =
					new ProactiveCertificateFactory(acPublicKey, acPrivateKey);
			PrivateCertificate certif_private =
					certif_factory.generatePrivateCertificate(jDomain.getText(),
					new Integer(jValidity.getText()).intValue());
			PublicCertificate certif_public =
					certif_private.get_PublicCertificate();
			FileOutputStream fout =
					new FileOutputStream(jPublic.getText());
			ObjectOutputStream out = new ObjectOutputStream(fout);

			out.writeObject(certif_public);
			out.flush();
			out.close();

			fout = new FileOutputStream(jPrivate.getText());
			out = new ObjectOutputStream(fout);

			out.writeObject(certif_private);
			out.flush();
			out.close();
			System.out.println("The Certificate Pair has been correctly generated and saved");
		}
		catch (Exception excp) {
			System.out.println("Exception in Certificate Generation : "
					 + excp);
		}
	}


	/**
	 *  Description of the Method
	 *
	 * @exception  Exception  Description of Exception
	 * @since
	 */
	private void jbInit() throws Exception {
		this.setLayout(borderLayout1);
		jPanel1.setBorder(BorderFactory.createLineBorder(Color.black));
		jPanel1.setPreferredSize(new Dimension(350, 117));
		jPanel1.setToolTipText("");
		jPanel1.setLayout(gridLayout1);
		gridLayout1.setRows(6);
		gridLayout1.setColumns(2);
		gridLayout1.setVgap(5);
		jLabel1.setText(" Domain Name :");
		jDomain.addActionListener(
			new java.awt.event.ActionListener() {

				/**
				 *  Description of the Method
				 *
				 * @param  e  Description of Parameter
				 * @since
				 */
				public void actionPerformed(ActionEvent e) {
					jDomain_actionPerformed(e);
				}

			});
		jLabel2.setText(" Validity (in days) :");
		jLabel3.setText(" Save Public Certificate to :");
		jLabel4.setToolTipText("");
		jLabel4.setText(" Save Private Certificate to :");
		jButton1.setPreferredSize(new Dimension(97, 50));
		jButton1.setToolTipText("");
		jButton1.setText("Generate !");
		jButton1.addActionListener(
			new java.awt.event.ActionListener() {

				/**
				 *  Description of the Method
				 *
				 * @param  e  Description of Parameter
				 * @since
				 */
				public void actionPerformed(ActionEvent e) {
					jButton1_actionPerformed(e);
				}

			});
		borderLayout1.setHgap(5);
		borderLayout1.setVgap(5);
		jLabel5.setText(" AC Public Key :");
		jACPublic.setText("acPublicKey");
		this.setMinimumSize(new Dimension(340, 147));
		this.setPreferredSize(new Dimension(350, 172));
		this.setToolTipText("");
		jLabel6.setText(" AC Private Key :");
		jACPrivate.setBorder(BorderFactory.createLineBorder(Color.black));
		jACPrivate.setText("acPrivateKey");
		this.add(jPanel1, BorderLayout.CENTER);
		jPanel1.add(jLabel1, null);
		jPanel1.add(jDomain, null);
		jPanel1.add(jLabel2, null);
		jPanel1.add(jValidity, null);
		jPanel1.add(jLabel3, null);
		jPanel1.add(jPublic, null);
		jPanel1.add(jLabel4, null);
		jPanel1.add(jPrivate, null);
		jPanel1.add(jLabel5, null);
		jPanel1.add(jACPublic, null);
		jPanel1.add(jLabel6, null);
		jPanel1.add(jACPrivate, null);
		this.add(jButton1, BorderLayout.SOUTH);
	}

}



