/*
 * Created on Jan 7, 2005
 */
package org.objectweb.proactive.examples.nbody.groupcom;


import org.objectweb.proactive.examples.nbody.common.Rectangle;
import java.io.Serializable;

import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.group.Group;
import org.objectweb.proactive.core.group.ProActiveGroup;

/**
 * @author irosenbe
 */
public class Domain implements Serializable{

    private int identification;
    private Domain neighbours;

    private Maestro maestro;
    private Displayer display;
    
    Planet info;
    Rectangle limits;
    private Planet [] values; // list of all the bodies within all the other domains
    private int nbvalues;
    
    
    public Domain (){}
    
    public Domain (Integer i, Rectangle r) {
        identification = i.intValue();
        limits = r;
    }

    public void init(Domain domainG, Displayer dp, Maestro master) {
        init(domainG,master);
        display=dp;
    }

    public void init(Domain domainGroup, Maestro master) {
        maestro = master;
        neighbours = domainGroup;
        Group gr = ProActiveGroup.getGroup(neighbours);
        gr.remove(ProActive.getStubOnThis ()); // domainGroup now contains all domains, excluding self
        
        values = new Planet[gr.size() +1]; // must add one slot for self, which stays null all the time
        clearValues();
        
        info = new Planet (limits);
    }

    public void clearValues(){
        for (int i = 0 ; i < values.length;  i++) 
            values[i] = null;
        nbvalues=0;
    } 
    
    public void computeNewValue() {
        // System.out.println("Domain " + identification + " starting mvt computation");
        Force force = new Force();  
        for (int i = 0 ; i < values.length ; i++) {
            force.add(info, values[i]); // adds the interaction of the distant body 
        }
        info.moveWithForce(force);
      
        clearValues();
        maestro.notifyFinished();
    }

    public Planet getValue() {
        return info;
    }

    public void setValue(Planet inf, int id) {
        //System.out.println(identification + " received value from " + id);
        values[id] = inf;
        nbvalues++;
        
        if (nbvalues == values.length - 1)
            computeNewValue();
    }

    public void sendValueToNeighbours() {
        //System.out.println(identification + " sends value to its neighbours");
        neighbours.setValue(info,identification);
        if (display == null) {// if no display, only the first Domain outputs message to say recompute is going on
            if (identification==0) 
                System.out.println("Compute movement.");
        }
        else 
            display.drawBody((int)info.x, (int)info.y, (int)info.vx, (int)info.vy, 
                    (int)info.mass, (int)info.diameter, this.identification);
    }

}
