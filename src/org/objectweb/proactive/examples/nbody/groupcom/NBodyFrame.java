/*
 * Created on Jan 7, 2005
 */
package org.objectweb.proactive.examples.nbody.groupcom;


import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.HeadlessException;
import java.awt.Image;
import java.io.Serializable;

import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * @author irosenbe
 */
public class NBodyFrame extends JFrame implements Serializable {

    public final static int SIZE=600;
    
    int[][] bodies; //[index]-> [x,y,w,d,vx,vy]
    int nbBodies;

    public NBodyFrame(){}
    
    /**
     * @param title
     * @throws java.awt.HeadlessException
     */
    public NBodyFrame(String title, int nb) throws HeadlessException {
        super(title);
        setSize(SIZE, SIZE);
        setLocation(400, 100);
        bodies = new int[nb][6];
        this.nbBodies = nb;
        final Image fond = getToolkit().getImage("/user/cdelbe/home/fondnbody.jpg");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel panel = new JPanel () {
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(fond, 0, 0, this);
                //g.setColor(Color.WHITE);
                //g.fillRect(0,0,SIZE,SIZE);
                g.setFont(g.getFont().deriveFont(Font.BOLD,8));
                for (int i=0;i<nbBodies;i++){
                    g.setColor(getColor(i));
                    int diameter = bodies[i][3];
                    g.fillOval(bodies[i][0],bodies[i][1],diameter,diameter);
                    g.setColor(Color.WHITE);
                    g.drawOval(bodies[i][0],bodies[i][1],diameter,diameter);
                    //g.drawLine(bodies[i][0],bodies[i][1],bodies[i][0]+bodies[i][4],bodies[i][1]+bodies[i][5]);
                    g.drawString("("+bodies[i][0]+","+bodies[i][1]+")",bodies[i][0]+diameter,bodies[i][1]);
                }
            }
        };
        setContentPane(panel);
      
        setVisible(true);
    }

    public void drawBody (int x, int y, int vx, int vy, int weight, int d, int id) {
        this.bodies[id][0]=x;
        this.bodies[id][1]=y;
        this.bodies[id][2]=weight;
        this.bodies[id][3]=d;
        this.bodies[id][4]=vx;
        this.bodies[id][5]=vy;
        repaint();
    }
    
    
    private Color getColor(int sel){
        switch (sel) {
        case 0:
            return Color.RED;
        case 1:
            return Color.BLUE;          
        case 2:
            return Color.CYAN;
        case 3:
            return Color.GREEN;
        case 4 :
            return Color.DARK_GRAY;
        case 5 :
            return Color.MAGENTA;
        case 6 :
            return Color.ORANGE;
        case 7 :
            return Color.PINK;
        case 8 :
            return Color.BLACK;
        default:
            return getColor(sel-8);
        }
    }

}
