package org.objectweb.proactive.examples.nbody.groupdistrib;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.core.group.ProActiveGroup;
import org.objectweb.proactive.core.mop.ClassNotReifiableException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.examples.nbody.common.Displayer;
import org.objectweb.proactive.examples.nbody.common.Planet;
import org.objectweb.proactive.examples.nbody.common.Rectangle;


public class Start {
    
    public static void main(String[] args) {
        // Set arguments as read on command line
        org.objectweb.proactive.examples.nbody.common.Start.main(args) ; 
    }
    
    public static void main(int totalNbBodies, int maxIter, Displayer displayer, Node[] nodes,
            org.objectweb.proactive.examples.nbody.common.Start killsupport) {
        System.out.println("RUNNING groupdistrib VERSION");
        
        Object [][] constructorParams = new Object [totalNbBodies][3] ;
        
        Rectangle universe = new Rectangle (-100 , -100 , 100 , 100);
        for (int  i = 0 ; i < totalNbBodies ; i++) {
            constructorParams[i][0] = new Integer(i);		      
            // coordinates between -100,-100 and 100,100
            constructorParams[i][1] = new Planet (universe); 
            constructorParams[i][2] = killsupport;
        }
        
        Domain domainGroup = null;
        try {
            // Create a group containing all the Domain in the simulation 
            domainGroup = (Domain) ProActiveGroup.newGroup ( Domain.class.getName(), constructorParams, nodes);
        }
        catch (ClassNotReifiableException e) { killsupport.abort(e); }
        catch (ClassNotFoundException e) { killsupport.abort(e); }
        catch (ActiveObjectCreationException e) { killsupport.abort(e); } 
        catch (NodeException e) { killsupport.abort(e); } 
        
        System.out.println("[NBODY] " + totalNbBodies + " Planets are deployed");
        
        // init workers
        domainGroup.init(domainGroup, displayer, maxIter, killsupport);
        
        // launch computation
        domainGroup.sendValueToNeighbours();
        
    }
    
    
}
