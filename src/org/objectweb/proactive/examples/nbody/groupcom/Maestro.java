package org.objectweb.proactive.examples.nbody.groupcom;

import java.io.Serializable;

import org.objectweb.proactive.core.group.ProActiveGroup;

public class Maestro implements Serializable {

    private Domain domainGroup;
    private int nbFinished = 0, iter = 0, maxIter;
    private int size;
    
    public Maestro () {}
    
    /**
     * Create a new master for the simulation, which pilots all the domains given in parameter.
     * @param domainG the group of Domains which are to be controled by this Maestro.
     */
    public Maestro (Domain domainG, Integer max) {
        maxIter = max.intValue(); 
        domainGroup = domainG;
        size = ProActiveGroup.getGroup(domainGroup).size();
    }
    
    /**
     * Called by a Domain when computation is finished. 
     * This method counts the answers, and restarts all Domains when all have finished. 
     */
    public void notifyFinished() {
        nbFinished ++ ;
        if (nbFinished == size) {
            iter ++;
            if (iter == maxIter)  Start.quit ();
            nbFinished = 0 ;
            domainGroup.sendValueToNeighbours();
        }
    }
    
}
