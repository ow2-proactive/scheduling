package org.objectweb.proactive.examples.nbody.groupdistrib;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Vector;

import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.group.Group;
import org.objectweb.proactive.core.group.ProActiveGroup;
import org.objectweb.proactive.examples.nbody.common.Displayer;
import org.objectweb.proactive.examples.nbody.common.Rectangle;

public class Domain implements Serializable{
    
    private class Carrier {
        Planet planet;
        int iter;
        
        Carrier (Planet plan, int iter) {
            this.planet = plan;
            this.iter = iter;
        }
    }
    
    private Displayer display;								// If we want some graphical interface
    private int identification;                         	// unique domain identifier
    private String hostName = "unknown";					// to display on which host we're running
    private Domain neighbours;								// The Group containing all the other Domains
    private Planet info;									// the body information
    private Force currentForce ;							// the sum of the forces already worked out 
    
    private int nbvalues, nbReceived=0;						// iteration related
    private int iter, maxIter;
    private Vector prematureValues;							// if values arrive too early, put them here.
    
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
        this.prematureValues = new Vector(); 
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
    public void init(Domain domainGroup, Displayer dp, int maxIter) {
        this.display=dp;
        this.maxIter = maxIter;
        this.neighbours = domainGroup;
        Group g = ProActiveGroup.getGroup(neighbours);
        g.remove(ProActive.getStubOnThis()); // no need to send information to self
        this.nbvalues = g.size();			// number of expected values to receive.
        reset();
    }
    
    
    /**
     * Move the Planet contained, applying the force computed. 
     */
    public void moveBody() {
        this.info.moveWithForce(currentForce);    
        sendValueToNeighbours();
    }
    
    /**
     * Called by a distant Domain, this method adds the inf contribution to the force applied on the local Planet
     * @param inf the distant Planet which adds its contribution.
     * @param receivedIter the distant iteration, to make sure we're synchronized 
     */
    public void setValue(Planet inf, int receivedIter) {
        if (this.iter == receivedIter) {
            this.currentForce.add(info, inf);
            this.nbReceived ++ ;
            if (this.nbReceived > this.nbvalues)
                System.err.println(identification +  " : Too many answers " + this.nbReceived + "/" + this.nbvalues);
            if (this.nbReceived == this.nbvalues) 
                moveBody();
        }
        else { 
            if (this.iter > receivedIter)
                throw new NullPointerException("Value arrives too late!");
            this.prematureValues.add(new Carrier (inf, receivedIter));
        }
    }
    
    /**
     * Triggers the emission of the local Planet to all the other Domains.
     */
    public void sendValueToNeighbours() {
        reset();
        this.iter++;
        if (this.iter < this.maxIter) {	  
            neighbours.setValue(this.info, this.iter);
            if (this.display == null) {// if no display, only the first Domain outputs message to say recompute is going on
                if (this.identification==0 && this.iter % 50 == 0 ) 
                    System.out.println("Compute movement." + this.iter);
            }
            else 
                this.display.drawBody((int)this.info.x, (int)this.info.y, (int)this.info.vx, (int)this.info.vy, 
                        (int)this.info.mass, (int)this.info.diameter, this.identification, this.hostName);
            treatPremature();
        }
        else
            if (this.identification==0) // only need one quit signal man!
                org.objectweb.proactive.examples.nbody.common.Start.quit();
            
    }    
    
    /**
     * Resends the premature information, which is probably up-to-date now
     */
    private void treatPremature() {
        int size = this.prematureValues.size() ;
        for (int i = 0 ; i < size ; i++) {
            Carrier c = (Carrier) this.prematureValues.remove(0);
            setValue(c.planet , c.iter); // works even if c.iter > iter
        }
    }
    
    /**
     * Empties iteration-specific variables.
     *
     */
    private void reset() {
        this.nbReceived = 0 ;
        this.currentForce = new Force();
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
