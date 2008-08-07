/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
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
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

/**
 * GUI of the JSR223 patch for the ProActive Scheduler
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
public class J5JSR223Patch extends JFrame implements HyperlinkListener {
	
	
	private static final long serialVersionUID = 1L;
	private JPanel jContentPane = null;
	private JPanel jPanel = null;
	private JButton jButton = null;
	private JTextField jTextField = null;
	private JTextPane jTextPane = null;
	private JPanel jPanel1 = null;
	private JLabel jLabel = null;
	public J5JSR223Patch() throws HeadlessException {
		super();
		initialize();
	}
	
	/**
	 * This method initializes jPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel() {
		if (jPanel == null) {
			BorderLayout borderLayout1 = new BorderLayout();
			borderLayout1.setHgap(0);
			jPanel = new JPanel();
			jPanel.setLayout(borderLayout1);
			jPanel.setPreferredSize(new Dimension(0, 25));
			jPanel.add(getJButton(), BorderLayout.EAST);
			jPanel.add(getJTextField(), BorderLayout.CENTER);
		}
		return jPanel;
	}

	/**
	 * This method initializes jButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJButton() {
		if (jButton == null) {
			jButton = new JButton();
			jButton.setText("Browse...");
			final J5JSR223Patch th =  this;
			jButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					JFileChooser chooser = new JFileChooser();
				   chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				   chooser.setDialogTitle("Select JSR-223 jars directory");
				   chooser.setApproveButtonText("OK");
				   if(chooser.showOpenDialog(th) == JFileChooser.APPROVE_OPTION) {
				    	File f = chooser.getSelectedFile();
				    	jTextField.setForeground(Color.BLACK);
				    	jTextField.setText(f.getAbsolutePath());
				    	Result result = Patch.patch(f.getAbsolutePath());
				    	boolean continu = true;
				    	if (result.isForce()){
				    		continu = false;
				    		int res = JOptionPane.showConfirmDialog(th,result.getMsg(),"Force patching ?",JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE);
				    		if (res == JOptionPane.OK_OPTION){
				    			continu = true;
				    			result = Patch.patch(f.getAbsolutePath(),true);
				    		}
				    	}
				    	if (continu){
					    	if (result.isSuccess()){
					    		JOptionPane.showConfirmDialog(th,result.getMsg(),"Successfully patched !!",JOptionPane.CLOSED_OPTION,JOptionPane.INFORMATION_MESSAGE);
					    		startApplication();
					    	} else {
					    		JOptionPane.showConfirmDialog(th,result.getMsg(),"Error !!",JOptionPane.CLOSED_OPTION,JOptionPane.ERROR_MESSAGE);
					    	}
				    	}
				   }
				}

				private void startApplication() {
					String os = System.getProperty("os.name");
					Runtime r = Runtime.getRuntime();
					try {
						if (os.matches(".*Windows.*"))
							r.exec("Scheduler.exe -clean");
						else
							r.exec("./Scheduler -clean");
					}
					catch (IOException ex) {}
				}
			});
		}
		return jButton;
	}

	/**
	 * This method initializes jTextField	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJTextField() {
		if (jTextField == null) {
			jTextField = new JTextField();
			jTextField.setText("Select a directory...");
		}
		return jTextField;
	}

	/**
	 * This method initializes jTextPane	
	 * 	
	 * @return javax.swing.JTextPane	
	 */
	private JTextPane getJTextPane() {
		if (jTextPane == null) {
			jTextPane = new JTextPane();
			jTextPane.setContentType("text/html");
			jTextPane.setEditable(false);
			jTextPane.setBackground(new Color(196, 255, 200));
			jTextPane.setText(
					"<html>" +
					"<head></head><body>" +
					"This patch adds the JSR-223 script language support library into the ProActive Scheduler RCP Application for Java 1.5.<br /><br />" +
					"If you are using a <b>Java 1.6</b> version or greater, this patch is <b>not applicable</b> (JSR-223 is included in the JRE since 1.6).<br />" +
					"<font color=\"red\"><b><u>IMPORTANT</u></b></font> : You must first <i><b>download the JSR-223 jar files</b></i> from <a href=\"http://jcp.org/aboutJava/communityprocess/final/jsr223/index.html\">http://jcp.org/aboutJava/communityprocess/final/jsr223/index.html</a> (script-api.jar, script-js.jar and js.jar)." +
					"</body>" +
					"</html>");
			jTextPane.addHyperlinkListener(this);
		}
		return jTextPane;
	}

	/**
	 * This method initializes jPanel1	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel1() {
		if (jPanel1 == null) {
			BorderLayout borderLayout2 = new BorderLayout();
			jLabel = new JLabel();
			jLabel.setText("Select the directory that contains the 3 JSR-223 jars files");
			jLabel.setPreferredSize(new Dimension(322, 20));
			jLabel.setBackground(new Color(239, 239, 239));
			jLabel.setHorizontalAlignment(SwingConstants.CENTER);
			jPanel1 = new JPanel();
			jPanel1.setLayout(borderLayout2);
			jPanel1.setPreferredSize(new Dimension(0, 0));
			jPanel1.add(getJTextPane(), BorderLayout.CENTER);
			jPanel1.add(jLabel, BorderLayout.SOUTH);
		}
		return jPanel1;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				J5JSR223Patch thisClass = new J5JSR223Patch();
				thisClass.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				thisClass.setVisible(true);
			}
		});
	}
	
	
	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setContentPane(getJContentPane());
		this.setTitle("Java 5 - JSR-223 Patch");
		this.setBounds(new Rectangle(200, 200, 450, 260));
	}
	
	
	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			BorderLayout borderLayout = new BorderLayout();
			borderLayout.setHgap(0);
			borderLayout.setVgap(0);
			jContentPane = new JPanel();
			jContentPane.setLayout(borderLayout);
			jContentPane.add(getJPanel(), BorderLayout.SOUTH);
			jContentPane.add(getJPanel1(), BorderLayout.CENTER);
		}
		return jContentPane;
	}

	public void hyperlinkUpdate(HyperlinkEvent arg0) {
		if (arg0.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
			String os = System.getProperty("os.name");
			Runtime r = Runtime.getRuntime();
			try {
				if (os.matches(".*Windows.*"))
					r.exec("cmd /c start "+arg0.getURL());
				else
					r.exec("htmlview "+arg0.getURL());
			}
			catch (IOException ex) {}
		}
	}
	
}
