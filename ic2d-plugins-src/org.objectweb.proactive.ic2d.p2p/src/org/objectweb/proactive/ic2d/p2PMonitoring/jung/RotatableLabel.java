package org.objectweb.proactive.ic2d.p2PMonitoring.jung;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JLabel;


public class RotatableLabel extends JLabel {
    protected double angle;

    public RotatableLabel() {
        super();
    }

    public RotatableLabel(String s, double angle) {
        super(s);
        this.angle = angle;
        setPreferredSize(new Dimension(100, 100));
        setMinimumSize(new Dimension(100, 100));
    }

    public void paintComponent(Graphics g) {
        System.out.println(this.getText() + " " + angle);
        Graphics2D g2d = (Graphics2D) g;
        //           g2d.translate(this.getWidth(), this.getHeight());
        g2d.translate(10, 10);
        // g2d.rotate( 90 - angle*360);
        g2d.rotate((3 * Math.PI) / 2, 0, 0);
        g2d.drawRect(this.getX(), this.getY(), this.getHeight(), this.getWidth());
        g2d.drawString(this.getText(), 0, 0);
    }
}
