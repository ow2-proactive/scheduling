package org.objectweb.proactive.examples.nbody.groupcom;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.group.ProActiveGroup;
import org.objectweb.proactive.core.mop.ClassNotReifiableException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.examples.nbody.common.Displayer;
import org.objectweb.proactive.examples.nbody.common.Rectangle;


/**
 * Starts the simulation running the groupcom example.
 */
public class Start {
    
    public static void main(String[] args) {  
        org.objectweb.proactive.examples.nbody.common.Start.main(args) ; 
    }
    
    /**
     * Called by common.Start if this version is selected.
     */
    public static void main(int totalNbBodies, int maxIter, Displayer displayer, Node[] nodes,
            org.objectweb.proactive.examples.nbody.common.Start killsupport) {
        
        System.out.println("RUNNING groupcom VERSION");
        
        int root = (int) Math.sqrt(totalNbBodies);
        int STEP_X = 200 / root , STEP_Y = 200 / root;  // to split the region in equal sized squares
        Object [][] params = new Object [totalNbBodies][3] ;
        for (int  i = 0 ; i < totalNbBodies ; i++) {
            params[i][0] = new Integer(i);		      
            params[i][1] = new Rectangle(STEP_X * (i % root), STEP_Y * (i / root) , STEP_X, STEP_Y);
            params[i][2] = killsupport ; 
        }
        Domain  domainGroup = null;
        try {
            // Create all the Domains as part of a Group
            domainGroup = (Domain) ProActiveGroup.newGroup ( Domain.class.getName(), params, nodes);
        } 
        catch (ClassNotReifiableException e) { killsupport.abort(e); }
        catch (ClassNotFoundException e) { killsupport.abort(e); }
        catch (ActiveObjectCreationException e) { killsupport.abort(e); } 
        catch (NodeException e) { killsupport.abort(e); }
        
        System.out.println("[NBODY] " + totalNbBodies + " Domains are deployed");
        
        Maestro maestro = null;
        try {
            // Supervizes the synchronisations
            maestro = (Maestro) ProActive.newActive(
                    Maestro.class.getName(), new Object[] {domainGroup, new Integer(maxIter), killsupport}, nodes[0]);
        } 
        catch (ActiveObjectCreationException e) {  killsupport.abort(e); }
        catch(NodeException e){  killsupport.abort(e); }
        
        
        // init workers
        domainGroup.init(domainGroup, displayer, maestro);
        
        // launch computation
        domainGroup.sendValueToNeighbours();
    }
    
}