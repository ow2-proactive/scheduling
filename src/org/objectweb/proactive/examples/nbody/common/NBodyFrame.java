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
package org.objectweb.proactive.examples.nbody.common;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class NBodyFrame extends JFrame implements Serializable, ActionListener, ChangeListener, MouseListener {
    
    public final static int SIZE=800;
    public final static int MAX_HISTO_SIZE=100;
    
    // fonctional
    private String [] bodyname;
    int[][] bodies; //[index]-> [x,y,w,d,vx,vy]
    ArrayList names;
    int nbBodies;
    CircularPostionList[] historics;
    boolean showTrace = false;
    int histoSize = MAX_HISTO_SIZE;
    int zoomValue=1;
    int xCenter = SIZE/2;
    int yCenter = SIZE/2;
    
    // gui
    JButton kill;
    JComboBox listVMs, protocol;
    JCheckBox queue;
    JPanel main, gui, anim;
    JSlider zoom;
    
    
    public NBodyFrame(){}
    
    /**
     * @param title
     * @throws java.awt.HeadlessException
     */
    public NBodyFrame(String title, int nb, boolean displayft) throws HeadlessException {
        super(title);
        setSize(SIZE+11, SIZE+90);
        setLocation(500, 50);
        bodies = new int[nb][6];
        bodyname = new String[nb];
        historics = new CircularPostionList[nb];
        for (int i=0;i<nb;i++){
            historics[i]=new CircularPostionList(MAX_HISTO_SIZE);
        }
        names = new ArrayList(nb);
        for (int i=0;i<nb;i++){
            names.add(i," ");
            bodyname[i] = "";
        }
        
        this.nbBodies = nb;
        ClassLoader cl = this.getClass().getClassLoader();
        java.net.URL u = cl.getResource("org/objectweb/proactive/examples/nbody/common/fondnbody.jpg");
        final Image fond = getToolkit().getImage(u);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // panels
        this.main = new JPanel(new BorderLayout());
        
        
        // Animation panel
        this.anim = new JPanel () {
            int iter=0;
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                this.setSize(SIZE,SIZE);
                iter++;
                g.drawImage(fond, 0, 0, this);
                // draw historics
                if (showTrace){
                    for (int i=0;i<nbBodies;i++){
                        for (int j=0;j<histoSize;j++){
                            int diameter = (bodies[i][3]>10)?(bodies[i][3]):(6);
                            Color c = getColor(i);
                            g.setColor(c.darker().darker());
                            g.fillOval(historics[i].getX(j)+diameter/2, historics[i].getY(j)+diameter/2,6,6);
                            g.setColor(Color.DARK_GRAY);
                            g.drawOval(historics[i].getX(j)+diameter/2, historics[i].getY(j)+diameter/2,6,6);
                        }
                    }
                }
                
                g.setFont(g.getFont().deriveFont(Font.ITALIC,12));
                for (int i=0;i<nbBodies;i++){
                    g.setColor(getColor(i));
                    int diameter = bodies[i][3];
                    int zoomedX = getZoomedCoord(bodies[i][0])+xCenter;
                    int zoomedY = getZoomedCoord(bodies[i][1])+yCenter;
                    g.fillOval(zoomedX,zoomedY,diameter,diameter);
                    g.setColor(Color.WHITE);
                    g.drawOval(zoomedX,zoomedY,diameter,diameter);
                    g.drawString(bodyname[i],zoomedX+diameter,zoomedY); 
                    
                    //update histo
                    if (iter%8==0){
                        historics[i].addValues(zoomedX,zoomedY);
                    }
                }   
            }
        };
        this.anim.setBorder(BorderFactory.createLineBorder(Color.BLACK,3));
        this.anim.addMouseListener(this);
        
        
        // GUI panel
        this.gui = new JPanel(new GridLayout(1,2));
        
        JPanel killingPanel = new JPanel(new GridLayout(1,4));
        this.protocol = new JComboBox(new Object[]{"rsh","ssh"});
        this.listVMs = new JComboBox();
        this.listVMs.addActionListener(this);
        JLabel cmd = new JLabel(" killall java  ");
        this.kill = new JButton("Execute");
        this.kill.addActionListener(this);
        killingPanel.add(protocol);
        killingPanel.add(listVMs);
        killingPanel.add(cmd);
        killingPanel.add(kill);
        killingPanel.setBorder(BorderFactory.createTitledBorder("Execution control"));
        
        
        
        
        JPanel drawPanel = new JPanel(new FlowLayout());
        this.queue = new JCheckBox("Show trace", false);
        this.queue.addActionListener(this);
        JLabel sliderLabel = new JLabel("ZoomOut", JLabel.CENTER);      
        this.zoom = new JSlider(1,5,1);
        this.zoom.setSnapToTicks(true);
        this.zoom.setMinorTickSpacing(1);
        this.zoom.setPaintTicks(true);
        this.zoom.setValue(1);
        this.zoom.addChangeListener(this);
        drawPanel.add(sliderLabel);
        drawPanel.add(this.zoom);
        drawPanel.add(this.queue);
        drawPanel.setBorder(BorderFactory.createTitledBorder("Draw control"));
        
        if (displayft)
            this.gui.add(killingPanel);
        this.gui.add(drawPanel);
        //this.gui.setBorder(BorderFactory.createTitledBorder("Controls"));
        
        
        
        this.main.add(anim, BorderLayout.NORTH);
        this.main.add(gui, BorderLayout.SOUTH);
        setContentPane(main);       
        setVisible(true);
    }
    
    
    
    /// EVENT HANDLING
    
    public void actionPerformed(ActionEvent e) {
        if (e.getSource()==this.queue){
            this.showTrace = !showTrace;
        } else if (e.getSource()==this.kill){
            try {
                Runtime.getRuntime().exec(""+this.protocol.getSelectedItem() + " " + this.listVMs.getSelectedItem() + " killall -KILL java");               
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }
    
    
    public void stateChanged(ChangeEvent e) {
        if (e.getSource()==this.zoom){
            if (this.zoom.getValue()!=this.zoomValue){
                this.zoomValue = this.zoom.getValue();
                int xRef=0, yRef=0;
                for (int i=0;i<this.nbBodies;i++){
                    xRef+= getZoomedCoord(this.bodies[i][0]);
                    yRef+= getZoomedCoord(this.bodies[i][1]);
                }       
                this.xCenter = SIZE/2 - xRef/nbBodies;
                this.yCenter = SIZE/2 - yRef/nbBodies;
                this.clearTrace();
            }
        }
    }
    
    
    public void mouseClicked(MouseEvent e) {        
        int xRef=0, yRef=0;
        for (int i=0;i<this.nbBodies;i++){
            xRef+= getZoomedCoord(this.bodies[i][0]);
            yRef+= getZoomedCoord(this.bodies[i][1]);
        }       
        this.xCenter = e.getX()- xRef/nbBodies;
        this.yCenter = e.getY()- yRef/nbBodies;
        this.clearTrace();
        
    }
    
    public void mouseEntered(MouseEvent e) {       
    }
    
    public void mouseExited(MouseEvent e) {
    }
    
    public void mousePressed(MouseEvent e) { 
    }
    
    public void mouseReleased(MouseEvent e) {
    }
    
    
    
    
    
    
    public void drawBody (int x, int y, int vx, int vy, int weight, int d, int id, String name) {
        this.bodies[id][0]=x;
        this.bodies[id][1]=y;
        this.bodies[id][2]=weight;
        this.bodies[id][3]=d;
        this.bodies[id][4]=vx;
        this.bodies[id][5]=vy;        
        bodyname[id]=name;
        if (!names.contains(name)){
            this.names.remove(id);
            this.names.add(id,name);
            this.listVMs.addItem(name);           
        }
        repaint();
    }
    
    
    private int getZoomedCoord(int x){
        //return x/zoomValue + (SIZE/2-xCenter) + SIZE/2/this.zoomValue;
        return x/zoomValue;
    }
    
    
    
    private void clearTrace(){
        historics = new CircularPostionList[this.nbBodies];
        for (int i=0;i<this.nbBodies;i++){
            historics[i]=new CircularPostionList(MAX_HISTO_SIZE);
        }
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
    
    
    private class CircularPostionList {
        
        private int [][] list;
        private int currentIndex;
        private int size;
        
        public CircularPostionList (int size){
            this.size = size;
            this.list = new int[size][2];
            this.currentIndex = 0;
        }
        
        public void addValues(int x, int y){
            this.list[currentIndex][0]=x;
            this.list[currentIndex][1]=y;
            this.currentIndex++;
            if (this.currentIndex==size){
                this.currentIndex=0;
            }
        }
        
        
        public int getX(int position){
            return this.list[position][0];
        }
        
        public int getY(int position){
            return this.list[position][1];
        }
        
        public void setX(int x, int position){
            this.list[position][0]=x;
        }
        
        public void setY(int y, int position){
            this.list[position][1]=y;
        }
        
        public int getSize(){
            return this.size;
        }
        
        public int getCurrentIndex(){
            return this.currentIndex;
        }
        
    }
    
    
}
