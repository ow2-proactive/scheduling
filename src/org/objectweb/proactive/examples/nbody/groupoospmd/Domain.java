package org.objectweb.proactive.examples.nbody.groupoospmd;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.group.Group;
import org.objectweb.proactive.core.group.ProActiveGroup;
import org.objectweb.proactive.core.group.spmd.ProSPMD;
import org.objectweb.proactive.examples.nbody.common.Displayer;
import org.objectweb.proactive.examples.nbody.common.Force;
import org.objectweb.proactive.examples.nbody.common.Planet;
import org.objectweb.proactive.examples.nbody.common.Rectangle;


public class Domain implements Serializable{
    
    private Displayer display;								// If we want some graphical interface
    private int identification;                         	// unique domain identifier
    private String hostName = "unknown";					// to display on which host we're running
    private Domain neighbours;								// The Group containing all the other Domains
    private Planet info;									// the body information
    private Force currentForce ;							// the sum of the forces already worked out 
    
    private int nbvalues, nbReceived=0;						// iteration related
    private int iter, maxIter;
    
    private org.objectweb.proactive.examples.nbody.common.Start killsupport;
    
    private Planet [] values; // list of all the bodies within all the other domains
    
    
    /**
     * Required by ProActive Active Objects
     */
    public Domain (){}
    
    /**
     * Constructor
     * @param i the unique identifier
     * @param r the boundaries containing the Planet at the begining of the simulation
     */
    public Domain (Integer i, Rectangle r) {
        this.identification = i.intValue();
        this.info = new Planet(r);
        try {this.hostName = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {e.printStackTrace();}
    }
    
    /**
     * Initialize all necessary variables, namely the variables concerning neighborhood, the display, and forces. 
     * @param domainGroup the Group of all Domains within universe 
     * @param dp The Displayer used to show on screen the movement of the objects.
     * @param maxIter The number of iterations to compute before stoppping
     */
public Domain getNeigh() {
    return this.neighbours;
} 
    public void init(Displayer dp, int maxIter, org.objectweb.proactive.examples.nbody.common.Start killsupport) {
        this.killsupport = killsupport;
        this.display=dp;
        this.maxIter = maxIter;
        this.neighbours = (Domain) ProSPMD.getSPMDGroup();
        this.nbvalues = ProActiveGroup.size(this.neighbours);
        this.values = new Planet [nbvalues];
        this.values[this.identification]=null;
        
        System.out.println("Waiting on barrier");
        ProSPMD.barrier("INIT");
        ((Domain)ProActive.getStubOnThis()).sendValueToNeighbours();
    }
    
    /**
     * Move the Planet contained, applying the force computed. 
     */
    public void moveBody() {
        Force force = new Force();  
        for (int i = 0 ; i < values.length ; i++) {
            force.add(info, values[i]); // adds the interaction of the distant body 
        }
        this.info.moveWithForce(force);
        this.iter++;
        clearValues();
        ProSPMD.barrier("BARRIER : " + this.iter );
        if (this.iter < this.maxIter) {
            ((Domain)ProActive.getStubOnThis()).sendValueToNeighbours();
        }
    }
    
    /**
     * Called by a distant Domain, this method adds the inf contribution to the force applied on the local Planet
     * @param inf the distant Planet which adds its contribution.
     * @param receivedIter the distant iteration, to make sure we're synchronized 
     */
    public void setValue(Planet inf, int id) {
        this.nbReceived ++ ;
        if (id != this.identification) 
            this.values[id] = inf;
        if (this.nbReceived == this.nbvalues) 
            moveBody();
    }
    
    
    /**
     * Triggers the emission of the local Planet to all the other Domains.
     */
    // FIXME DEBUG : remove this go, it stands to no use
    public void sendValueToNeighbours() {
        this.neighbours.setValue(this.info, this.identification);
        if (this.display == null) {// if no display, only the first Domain outputs message to say recompute is going on
            if (this.identification==0) 
                System.out.println("Compute movement.");
        }
        else 
            this.display.drawBody((int)this.info.x, (int)info.y, (int)info.vx, (int)info.vy, 
                    (int)info.mass, (int)info.diameter, this.identification, this.hostName);
    }
    
    /**    
     * Empties iteration-specific variables.
     *
     */
    public void clearValues(){
        this.nbReceived = 0 ;
    } 
    
    /**
     * Method called when the object is redeployed on a new Node (Fault recovery, or migration).
     */
    private void readObject(java.io.ObjectInputStream in) 
    throws java.io.IOException, ClassNotFoundException {
        in.defaultReadObject();
        try {
            this.hostName = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            hostName="unknown";
            e.printStackTrace();
        }
        
    }
    
    
}
