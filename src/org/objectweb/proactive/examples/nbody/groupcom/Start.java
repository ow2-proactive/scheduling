package org.objectweb.proactive.examples.nbody.groupcom;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.group.ProActiveGroup;
import org.objectweb.proactive.core.mop.ClassNotReifiableException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.examples.nbody.common.Displayer;
import org.objectweb.proactive.examples.nbody.common.Rectangle;


public class Start {
    
    public static void main(String[] args) {  
        org.objectweb.proactive.examples.nbody.common.Start.main(args) ; 
    }
    
    public static void main(int totalNbBodies, int maxIter, Displayer displayer, Node[] nodes) {
        
        System.out.println("RUNNING groupcom VERSION");
        
        try {
            
            int root = (int) Math.sqrt(totalNbBodies);
            int STEP_X = 200 / root , STEP_Y = 200 / root;
            Object [][] params = new Object [totalNbBodies][2] ;
            for (int  i = 0 ; i < totalNbBodies ; i++) {
                params[i][0] = new Integer(i);		      
                params[i][1] = new Rectangle(STEP_X * (i % root), STEP_Y * (i / root) , STEP_X, STEP_Y);
            }
            Domain  domainGroup = (Domain) ProActiveGroup.newGroup ( Domain.class.getName(), params, nodes);
            
            System.out.println("[NBODY] " + totalNbBodies + " Planets are deployed");
            
            Maestro maestro = null;
            try {
                maestro = (Maestro) ProActive.newActive(
                        Maestro.class.getName(), new Object[] {domainGroup, new Integer(maxIter)});
            } 
            catch (ActiveObjectCreationException e) { e.printStackTrace();   }
            catch(NodeException ex){  	ex.printStackTrace();    }
            
            
            // init workers
            if (displayer != null)
                domainGroup.init(domainGroup, displayer, maestro);
            else
                domainGroup.init(domainGroup,maestro);
            
            // launch computation
            domainGroup.sendValueToNeighbours();
            
        }
        
        catch (NodeException e) { e.printStackTrace(); } 
        catch (ActiveObjectCreationException e) { e.printStackTrace(); } 
        catch (ClassNotReifiableException e) { e.printStackTrace(); }
        catch (ClassNotFoundException e1) { e1.printStackTrace(); }
    }
    
}
