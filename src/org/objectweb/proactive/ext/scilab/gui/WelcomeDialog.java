package org.objectweb.proactive.ext.scilab.gui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;

import javax.swing.JDialog;
import javax.swing.JPanel;

public class WelcomeDialog extends JDialog {
	private JPanel panelLogo = null ;
	private Image logo;
	public WelcomeDialog() {
		super();
		
		logo = Toolkit.getDefaultToolkit().getImage(getClass().getResource("img/logo.jpg"));
		panelLogo = new JPanel (){
			public void paintComponent ( Graphics g )
			{
				super.paintComponent(g) ;

				g.drawImage(logo, 0, 0, this);
			}
		};
		
		this.setSize(510, 300) ;
		this.setResizable(false);
		this.setTitle("Welcome");
		this.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		this.setModal(false) ;
		this.setUndecorated(true) ;
		this.getContentPane().add(this.panelLogo) ;
		this.center() ;
		
	}

	public void center() {
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension f = this.getSize();
        int x = (d.width - f.width) / 2;
        int y = (d.height - f.height) / 2;
        this.setBounds(x, y, f.width, f.height );
	}

}
