package org.objectweb.proactive.examples.nbody.common;

import java.io.Serializable;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;

public class Displayer implements Serializable, InitActive{
    
    private transient NBodyFrame nbf;
    
    private boolean displayft;
    private int nbBodies;
    
    public Displayer(){}
    
    public Displayer(Integer nbBodies, Boolean displayft){
        this.nbBodies=nbBodies.intValue();
        this.displayft = displayft.booleanValue();
    }
    
    public void drawBody(int x, int y, int vx, int vy, int weight, int d, int id, String name){
        this.nbf.drawBody(x,y,vx,vy, weight,d,id, name);        
    }
    
    /* @deprecated please replace these occurences by replaced by
     *   drawBody(int, int, int, int, int, int, int, String).
     */
    public void drawBody(int x, int y, int vx, int vy, int weight, int d, int id){
        this.nbf.drawBody(x,y,vx,vy, weight,d,id, "");        
    }
    
    public void initActivity(Body body) {
        nbf = new NBodyFrame("ProActive N-Body", nbBodies, displayft);
        nbf.setVisible(true);
        // this.w=nbf.getWidth();
        // this.h=nbf.getHeight();
    }
    
}
