package org.objectweb.proactive.examples.nbody.simple;

import java.io.Serializable;

/**
 * Synchronization of the computation of the Domains
 */
public class Maestro implements Serializable {
    
    private int nbFinished = 0, iter = 0, maxIter;   // iteration related fields
    private Domain[] domainArray;					 // references on all the Active Domains
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
    public Maestro (Domain [] domainArray, Integer max, org.objectweb.proactive.examples.nbody.common.Start killsupport) {
        this.killsupport = killsupport;
        this.maxIter = max.intValue(); 
        this.domainArray = domainArray;
    }
    
    /**
     * Called by a Domain when computation is finished. 
     * This method counts the calls, and restarts all Domains only once all have finished. 
     */
    public void notifyFinished() {
        this.nbFinished ++ ;
        if (this.nbFinished == this.domainArray.length) {
            this.iter ++;
            if (this.iter == this.maxIter)  
                this.killsupport.quit();
            this.nbFinished = 0 ;
            for (int i = 0 ; i < domainArray.length ; i++)
                this.domainArray[i].sendValueToNeighbours();
        }
    }
    
}
