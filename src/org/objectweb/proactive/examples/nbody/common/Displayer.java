package org.objectweb.proactive.examples.nbody.common;


import java.io.Serializable;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;

public class Displayer implements Serializable, InitActive{

    private transient NBodyFrame nbf;
    // private int w,h;
    private int nbBodies;
    
    public Displayer(){}
    
    public Displayer(Integer nbBodies){
        this.nbBodies=nbBodies.intValue();
    }
    
    public void drawBody(int x, int y, int vx, int vy, int weight, int d, int id, String name){
        this.nbf.drawBody(x,y,vx,vy, weight,d,id, name);        
    }

    public void initActivity(Body body) {
        nbf = new NBodyFrame("ProActive N-Body", nbBodies);
		nbf.setVisible(true);
       // this.w=nbf.getWidth();
       // this.h=nbf.getHeight();
    }
   
}
