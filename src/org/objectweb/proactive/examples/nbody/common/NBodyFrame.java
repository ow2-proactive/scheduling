/*
 * Created on Jan 7, 2005
 */
package org.objectweb.proactive.examples.nbody.common;
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
    int count;

    private double scale = 1;

    public NBodyFrame(){}
    
    /**
     * @param title Title to set up for the window 
     * @throws java.awt.HeadlessException
     */
    public NBodyFrame(String title, int nb) throws HeadlessException {
        super(title);
        setSize(SIZE, SIZE);
        setLocation(400, 100);
        bodies = new int[nb][6];
        this.count = 0;
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
                int [] center = getCenter(); 
                for (int i=0;i<nbBodies;i++){
                    g.setColor(getColor(i));
                    int diameter = bodies[i][3];
                    int x = (int) (bodies[i][0]/scale)+center[0];
                    int y = (int) (bodies[i][1]/scale)+center[1];
                    g.fillOval(x, y,diameter,diameter);
                    g.setColor(Color.WHITE);
                    g.drawOval(x,y,diameter,diameter);
                    //g.drawLine(bodies[i][0],bodies[i][1],bodies[i][0]+bodies[i][4],bodies[i][1]+bodies[i][5]);
                    g.drawString("("+bodies[i][0]+","+bodies[i][1]+")",x+diameter,y);
                }
            }

        };
        setContentPane(panel);
      
        setVisible(true);
    }

    private int [] getCenter() {
        int MASS_INDEX = 2;
        int [] center = new int[] {0,0};
        int totalmass = 0;
        for (int i=0; i<nbBodies; i++)
            totalmass += bodies[i][MASS_INDEX];
        if (totalmass==0)
            totalmass = 1;
        for (int index = 0 ; index < 2 ; index ++) { // This index always goes with the dimension. In 2D, it's array.length=2
            for (int i=0; i<nbBodies; i++) 
                center[index] -= bodies[i][index] * bodies[i][MASS_INDEX];
            center[index] /=  totalmass;			// gives the center of mass
        }
        for (int index = 0 ; index < 2 ; index ++) {
            center[index] /= scale;
            center[index] +=  SIZE/2;	 // makes the center dependent on the size of the windows
        }
        return center;
    }

    public void drawBody (int x, int y, int vx, int vy, int weight, int diameter, int id) {
        this.bodies[id][0]=x;
        this.bodies[id][1]=y;
        this.bodies[id][2]=weight;
        this.bodies[id][3]=diameter;
        this.bodies[id][4]=vx;
        this.bodies[id][5]=vy;
        count ++; 
        if (count == nbBodies){ 
            repaint();
            count = 0;
            }
    }
    
    
    private Color getColor(int sel){
        assert sel >= 0;
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
