package org.objectweb.proactive.examples.nbody.groupcom;

import java.io.Serializable;

import org.objectweb.proactive.core.group.ProActiveGroup;

/**
 * Synchronization of the computation of the Domains
 */
public class Maestro implements Serializable {
    
    private Domain domainGroup;
    private int nbFinished = 0, iter = 0, maxIter;
    private int size;
    private org.objectweb.proactive.examples.nbody.common.Start killsupport;
    
    /**
     * Required by ProActive Active Objects
     */
    public Maestro () {}
    
    /**
     * Create a new master for the simulation, which pilots all the domains given in parameter.
     * @param domainG the group of Domains which are to be controled by this Maestro.
     * @param max the total number of iterations that should be simulated
     */
    public Maestro (Domain domainG, Integer max, org.objectweb.proactive.examples.nbody.common.Start killsupport) {
        this.killsupport = killsupport;
        this.maxIter = max.intValue(); 
        this.domainGroup = domainG;
        this.size = ProActiveGroup.getGroup(domainGroup).size();
    }
    
    /**
     * Called by a Domain when computation is finished. 
     * This method counts the answers, and restarts all Domains when all have finished. 
     */
    public void notifyFinished() {
        this.nbFinished ++ ;
        if (this.nbFinished == this.size) {
            this.iter ++;
            if (this.iter == this.maxIter)  
                this.killsupport.quit ();
            this.nbFinished = 0 ;
            this.domainGroup.sendValueToNeighbours();
        }
    }
    
}
