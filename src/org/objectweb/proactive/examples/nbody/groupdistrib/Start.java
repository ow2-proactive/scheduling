package org.objectweb.proactive.examples.nbody.groupdistrib;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.core.group.ProActiveGroup;
import org.objectweb.proactive.core.mop.ClassNotReifiableException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.examples.nbody.common.Displayer;
import org.objectweb.proactive.examples.nbody.common.Rectangle;


public class Start {
    
    public static void main(String[] args) {
        // Set arguments as read on command line
        org.objectweb.proactive.examples.nbody.common.Start.main(args) ; 
    }
    
    public static void main(int totalNbBodies, int maxIter, Displayer displayer, Node[] nodes) {
        System.out.println("RUNNING groupdistrib VERSION");
        
        int root = (int) Math.sqrt(totalNbBodies);
        int STEP_X = 200 / root , STEP_Y = 200 / root;
        Object [][] params = new Object [totalNbBodies][2] ;
        
        for (int  i = 0 ; i < totalNbBodies ; i++) {
            params[i][0] = new Integer(i);		      
            params[i][1] = new Rectangle(STEP_X * (i % root), STEP_Y * (i / root) , STEP_X, STEP_Y);
        }
        
        Domain domainGroup = null;
        try {
            domainGroup = (Domain) ProActiveGroup.newGroup ( Domain.class.getName(), params, nodes);
        }
        catch (ClassNotReifiableException e) { org.objectweb.proactive.examples.nbody.common.Start.abort(e); }
        catch (ClassNotFoundException e) { org.objectweb.proactive.examples.nbody.common.Start.abort(e); }
        catch (ActiveObjectCreationException e) { org.objectweb.proactive.examples.nbody.common.Start.abort(e); } 
        catch (NodeException e) { org.objectweb.proactive.examples.nbody.common.Start.abort(e); } 

        System.out.println("[NBODY] " + totalNbBodies + " Planets are deployed");
        
        // init workers
        if (displayer != null)
            domainGroup.init(domainGroup, displayer, maxIter);
        else
            domainGroup.init(domainGroup, maxIter);
        
        // launch computation
        domainGroup.sendValueToNeighbours();
        
    }
    
    
}
