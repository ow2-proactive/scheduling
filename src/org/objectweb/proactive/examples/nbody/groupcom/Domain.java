package org.objectweb.proactive.examples.nbody.groupcom;


import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.group.Group;
import org.objectweb.proactive.core.group.ProActiveGroup;
import org.objectweb.proactive.examples.nbody.common.Displayer;
import org.objectweb.proactive.examples.nbody.common.Rectangle;

public class Domain implements Serializable{
    
    private int identification;
    private Domain neighbours;
    
    private Maestro maestro;
    private Displayer display;
    
    Planet info;
    Rectangle limits;
    private Planet [] values; // list of all the bodies within all the other domains
    private int nbvalues, nbReceived=0;
    
    private String hostName;
    
    public Domain (){}
    
    public Domain (Integer i, Rectangle r) {
        identification = i.intValue();
        limits = r;
        info = new Planet(r);
        try {
            this.hostName = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }
    
    public void init(Domain domainG, Displayer dp, Maestro master) {
        init(domainG,master);
        display=dp;
    }
    
    public void init(Domain domainGroup, Maestro master) {
        maestro = master;
        neighbours = domainGroup;
        Group g = ProActiveGroup.getGroup(neighbours);
        g.remove(ProActive.getStubOnThis()); // no need to send information to self
        nbvalues = g.size();
        values = new Planet [nbvalues + 1] ; // leave empty slot for self
    }
    
    public void clearValues(){
        nbReceived = 0 ;
    } 
    
    public void computeNewValue() {
        // System.out.println("Domain " + identification + " starting mvt computation");
        Force force = new Force();  
        for (int i = 0 ; i < values.length ; i++) {
            force.add(info, values[i]); // adds the interaction of the distant body 
        }
        info.moveWithForce(force);
        
        clearValues();
    }
    
    public Planet getValue() {
        return info;
    }
    
    public void setValue(Planet inf, int id) {
        values [id] = inf;
        nbReceived ++ ;
        if (nbReceived > nbvalues)  // This is a bad sign!
            System.err.println("Domain " + identification + " received too many answers");
        if (nbReceived == nbvalues) {
            maestro.notifyFinished();
            computeNewValue();
        }
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
                    (int)info.mass, (int)info.diameter, this.identification, hostName);
    }
    
}
