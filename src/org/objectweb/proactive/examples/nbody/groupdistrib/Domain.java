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
    
    private Displayer display;
    private int identification;
    private String hostName = "unknown";
    private Domain neighbours;
    private Planet info;
    private Force currentForce ;
    
    private int nbvalues, nbReceived=0;
    private int iter, maxIter;
    private Vector prematureValues;
    
    public Domain (){}
    
    public Domain (Integer i, Rectangle r) {
        this.identification = i.intValue();
        this.prematureValues = new Vector(); 
        this.info = new Planet(r);
        try {
            this.hostName = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }
    
    public void init(Domain domainG, Displayer dp, int maxIter) {
        init(domainG, maxIter);
        this.display=dp;
    }
    
    /**
     * Initialize all necessary variables, namely the variables concerning neighborhood and forces.  
     * @param domainGroup the Group of all Domains within universe 
     * @param maxIter The number of iterations to compute before stoppping
     */
    
    public void init(Domain domainGroup, int maxIter) {
        this.maxIter = maxIter;
        this.neighbours = domainGroup;
        Group g = ProActiveGroup.getGroup(neighbours);
        g.remove(ProActive.getStubOnThis()); // no need to send information to self
        this.nbvalues = g.size();
        reset();
    }
    
    
    public void computeNewValue() {
        this.info.moveWithForce(currentForce);    
        sendValueToNeighbours();
    }
    
    public void setValue(Planet inf, int receivedIter) {
        if (this.iter == receivedIter) {
            this.currentForce.add(info, inf);
            this.nbReceived ++ ;
            if (this.nbReceived > this.nbvalues)
                System.err.println(identification +  " : Too many answers " + this.nbReceived + "/" + this.nbvalues);
            if (this.nbReceived == this.nbvalues) 
                computeNewValue();
        }
        else { 
            if (this.iter > receivedIter)
                throw new NullPointerException("Value arrives too late!");
            this.prematureValues.add(new Carrier (inf, receivedIter));
        }
    }
    
    
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
