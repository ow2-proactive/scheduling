/*
 * Created on Jan 7, 2005
 */
package org.objectweb.proactive.examples.nbody;


import java.io.Serializable;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;

/**
 * @author irosenbe
 */
public class Displayer implements Serializable, InitActive{
    
    private transient NBodyFrame nbf;
    private int w,h;
    private int centralX,centralY;
    private int reference;
    private int radiusReference;
    private int nbBodies;
    
    public Displayer(){}
    
    public Displayer(Integer nbBodies){
        this.nbBodies=nbBodies.intValue();
    }
    
    public void drawBody(int x, int y, int vx, int vy, int weight, int d, int id){
        if (reference==-1){
            this.reference=id;
            this.radiusReference = weight/2000+1;
        }
        
        if (id==reference){
            // reference
            this.centralX=x+radiusReference;
            this.centralY=y+radiusReference;
        }
        int dx = x+(w/2)-centralX;
        int dy = y+(h/2)-centralY;
        
        this.nbf.drawBody(dx,dy,vx,vy, weight,d,id);
        
    }
    
    
    public void initActivity(Body body) {
        nbf = new NBodyFrame("ProActive N-Body", nbBodies);
        nbf.setVisible(true);
        //this.nbf = nbf;
        this.w=nbf.getWidth();
        this.h=nbf.getHeight();
        this.centralX=w/2;
        this.centralY=h/2;
        this.reference =-1;
    }
    
}