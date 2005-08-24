package org.objectweb.proactive.examples.c3d.gui;

import java.awt.Button;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


/**
 * A Dialog box, with a title, displaying two lines of text.
 * Nothing fancy at all. Used to display the ProActive "about" window 
 */
public class DialogBox extends Dialog implements ActionListener, java.io.Serializable {
    
    
    public DialogBox(Frame parent,String frametitle, String line1, String line2) {
        super(parent, frametitle, true);
        
        GridBagLayout gb = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        setLayout(gb);
        
        // line 1 
        c.gridy = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        Label line1Label = new Label(line1, Label.CENTER);
        gb.setConstraints(line1Label, c);
        line1Label.setForeground(Color.blue);
        line1Label.setFont(new Font("arial", Font.BOLD | Font.ITALIC, 16));
        add(line1Label);
        
        // line 2
        c.gridy = 1;
        Label line2Label = new Label(line2, Label.CENTER);
        gb.setConstraints(line2Label, c);
        add(line2Label);
        
        //Button
        c.gridy = 2;
        c.fill = GridBagConstraints.NONE;
        Button okButton = new Button("OK");
        gb.setConstraints(okButton, c);
        okButton.addActionListener(this);
        add(okButton);
        
        setLocation(400, 200);
        pack();
        setVisible(true);
        toFront();
        
    }
    
    public void actionPerformed(ActionEvent e) {
        setVisible(false);
        dispose();
    }
}