package org.objectweb.proactive.examples.nbody.groupoospmd;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.group.spmd.ProSPMD;
import org.objectweb.proactive.core.mop.ClassNotReifiableException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.examples.nbody.common.Displayer;
import org.objectweb.proactive.examples.nbody.common.Rectangle;


/**
 * Starts the simulation running the groupoospmd example.
 */
public class Start{
    
    public static void main(String[] args) {  
        org.objectweb.proactive.examples.nbody.common.Start.main(args) ; 
    }
    
    public static void main(int totalNbBodies, int maxIter, Displayer displayer, Node[] nodes,
            org.objectweb.proactive.examples.nbody.common.Start killsupport) {
        
        System.out.println("RUNNING group oo-spmd VERSION");
        
        int root = (int) Math.sqrt(totalNbBodies);
        int STEP_X = 200 / root , STEP_Y = 200 / root;
        Object [][] params = new Object [totalNbBodies][2] ;
        for (int  i = 0 ; i < totalNbBodies ; i++) {
            params[i][0] = new Integer(i);		      
            // coordinates between -100,-100 and 100,100
            params[i][1] = new Rectangle(STEP_X * (i % root)-100, STEP_Y * (i / root) -100, STEP_X, STEP_Y);
        }
        Domain domainGroup = null; 
        try {
            domainGroup = (Domain) ProSPMD.newSPMDGroup( Domain.class.getName(), params, nodes);
        }
        catch (NodeException e) { killsupport.abort(e);} 
        catch (ActiveObjectCreationException e) { killsupport.abort(e); } 
        catch (ClassNotReifiableException e) { killsupport.abort(e); }
        catch (ClassNotFoundException e) { killsupport.abort(e); }

        System.out.println("[NBODY] " + totalNbBodies + " Planets are deployed");
        
        // init workers
        domainGroup.init(displayer, maxIter, killsupport);
    }
    
}
