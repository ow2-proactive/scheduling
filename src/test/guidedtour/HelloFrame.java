/*
 * HelloFrame.java
 *
 * Created on February 17, 2003, 2:16 PM
 */

package test.guidedtour;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * This class allows the creation of a graphical window 
 * with a text field
 * 
 */
public class HelloFrame extends javax.swing.JFrame {

	private javax.swing.JLabel jLabel1;

	/** Creates new form HelloFrame */
	public HelloFrame(String text) {
		initComponents();
		setText(text);
	}

	/** This method is called from within the constructor to
	 * initialize the form.
	 *	It will perform the initialization of the frame
	 */
	private void initComponents() {
		jLabel1 = new javax.swing.JLabel();
		addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent evt) {
				exitForm(evt);
			}
		});

		jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
		getContentPane().add(jLabel1, java.awt.BorderLayout.CENTER);

		pack();
	}

	/** Kill the frame */
	private void exitForm(java.awt.event.WindowEvent evt) {
		//        System.exit(0); would kill the VM !
		dispose(); // this way, the active object agentFrameController stays alive
	}

	/**
	 * sets the text of the label inside the frame
	 */
	private void setText(String text) {
		jLabel1.setText(text);
	}
}
