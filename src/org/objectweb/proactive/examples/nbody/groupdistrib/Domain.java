package org.objectweb.proactive.examples.nbody.groupdistrib;


import java.io.IOException;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.group.Group;
import org.objectweb.proactive.core.group.ProActiveGroup;
import org.objectweb.proactive.examples.nbody.common.Displayer;
import org.objectweb.proactive.examples.nbody.common.Rectangle;
import org.objectweb.proactive.ext.security.exceptions.RenegotiateSessionException;

public class Domain implements RunActive{
    
    private Displayer display;
    
    private int identification;
    private Domain neighbours;
    private Planet info;
    private Force currentForce ;
    
    private int nbvalues, nbReceived=0;
    private int iter, maxIter;
    
    public Domain (){}
    
    public Domain (Integer i, Rectangle r) {
        identification = i.intValue();
        info = new Planet(r);
    }
    
    public void init(Domain domainG, Displayer dp, int maxIter) {
        init(domainG, maxIter);
        display=dp;
    }
    
    /**
     * Initialize all necessary variables, namely the variables concerning neighborhood and forces.  
     * @param domainGroup the Group of all Domains within universe 
     * @param maxIter The number of iterations to compute before stoppping
     */
    
    public void init(Domain domainGroup, int maxIter) {
        this.maxIter = maxIter;
        neighbours = domainGroup;
        Group g = ProActiveGroup.getGroup(neighbours);
        g.remove(ProActive.getStubOnThis()); // no need to send information to self
        nbvalues = g.size();
        reset();
    }
    
    public void computeNewValue() {
        info.moveWithForce(currentForce);    
        sendValueToNeighbours();
    }
    
    /**
     * Update the value of a distant Domain.
     * @param inf The new value to take into account.
     * @param receivedIter The iteration of the distant Domain, to avoid desynchronization.
     */
    public void setValue(Planet inf, int receivedIter) {
        if (iter == receivedIter) {
            currentForce.add(info, inf);
            nbReceived ++ ;
            if (nbReceived > nbvalues)  // This is a bad sign!
                System.err.println("Domain " + identification + " received too many answers");
            if (nbReceived == nbvalues) 
                computeNewValue();
        }
        else 
            System.err.println("This should never happen, different iteration (on Domain" + identification +")");
    }
    
    
    /**
     * Broadcast the local planet to all neighbours.
     */
    public void sendValueToNeighbours() {
        reset();
        iter++;
        if (iter < maxIter) {	  
            neighbours.setValue(info, iter);
            if (display == null) {// if no display, only the first Domain outputs message to say recompute is going on
                if (identification==0) 
                    System.out.println("Compute movement." + iter);
            }
            else 
                display.drawBody((int)info.x, (int)info.y, (int)info.vx, (int)info.vy, 
                        (int)info.mass, (int)info.diameter, identification);
        }
        else
            if (identification==0) // only need one quit signal man!
                Start.quit();
            
    }    
    
    /**
     * Once a movement has been computed, clean up the variables related to an iteration.
     */
    private void reset() {
        nbReceived = 0 ;
        currentForce = new Force();
    }
    
    /**
     * Redefining this method allows us to reject requests that come in too early.
     * When incoming request has receivedIter > iter, the request is pushed to the end of the queue. 
     */
    public void runActivity(Body body) {
        org.objectweb.proactive.Service service = new org.objectweb.proactive.Service(body);
        while (body.isActive()) {
            Request r = service.blockingRemoveOldest();
            if (r.getMethodName().equals("setValue")) {
                int receivedIter = ((Integer)r.getParameter(2)).intValue();
                if (receivedIter != iter) {
                    try {
                        r.send(body);
                    } catch (IOException e) {e.printStackTrace(); }
                    catch (RenegotiateSessionException e) {e.printStackTrace();}
                    continue;	// don't serve the request, it's been put at the end of the queue!
                }
            }
            service.serve(r);
        }
    }
    
}
