/*
 * Created on Jan 12, 2005
 */
package org.objectweb.proactive.examples.nbody.barneshut;

import java.io.Serializable;

import org.objectweb.proactive.core.group.ProActiveGroup;

/**
 * @author irosenbe
 */
public class Maestro implements Serializable {
    
    private Domain domainGroup;
    private int nbFinished = 0, iter = 0, maxIter;
    private int size;
    
    public Maestro () {}
    
    /**
     * Create a new master for the simulation, which pilots all the domains given in parameter.
     * @param domainG the group of Domains which are to be controled by this Maestro.
     */
    public Maestro (Domain domainG) {
        domainGroup = domainG;
        size = ProActiveGroup.getGroup(domainGroup).size();
    }
    
    /**
     * Start the whole simulation, for a given number of iterations. 
     * @param max The number of iterations which should be computed before the end of the program. 
     */
    public void start(int max) {
        System.out.println("MAESTRO START ");
        maxIter = max; 
        domainGroup.sendValueToNeighbours();
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
            //System.out.println("Synchro " + iter );
            domainGroup.sendValueToNeighbours();
        }
    }
    
    
}
