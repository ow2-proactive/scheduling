package org.objectweb.proactive.examples.nbody.barneshut;

import java.util.Vector;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.core.group.Group;
import org.objectweb.proactive.core.group.ProActiveGroup;
import org.objectweb.proactive.core.group.spmd.ProSPMD;
import org.objectweb.proactive.core.mop.ClassNotReifiableException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.examples.nbody.common.Displayer;


public class Start { 
    
    /**
     * Options should be "java Start xmlFile [-display] totalNbBodies maxIter"
     * @param -display, which is not compulsory, specifies whether a graphic display is to be created.
     * @param xmlFile is the xml deployment file..
     * @param totalNbBodies  The number of Planets in the System
     * @param maxIter The number of iterations before the program stops. 
     */
    public static void main(String[] args) {
        org.objectweb.proactive.examples.nbody.common.Start.main(args) ; 
    }
    
    public static void main(int totalNbBodies, int maxIter, Displayer displayer, Node[] nodes,
            org.objectweb.proactive.examples.nbody.common.Start killsupport) {
        System.out.println("RUNNING Barnes-Hut VERSION");
        
        // Create all the Domains, based on the tree structure
        QuadTree tree = new QuadTree(totalNbBodies);
        Vector info = tree.getInfo(); 
        int size = tree.size();
        
        Object [][] params = new Object [size][3]; // the parameters passed to the Domain constructors
        for (int  i = 0 ; i < size; i++)  {
            params[i][0] = new Integer(i);		      
            params[i][1] = (Info) info.get(i);	
            params[i][2] = killsupport;
        }
        Domain domainGroup = null;
        try {
            // Create all the Domains, inside an SPMD Group, to enable barriers
            domainGroup = (Domain) ProSPMD.newSPMDGroup( Domain.class.getName(), params, nodes);
        }
        catch (ClassNotReifiableException e) { killsupport.abort(e); }
        catch (ClassNotFoundException e) { killsupport.abort(e); }
        catch (ActiveObjectCreationException e) { killsupport.abort(e); } 
        catch (NodeException e) { killsupport.abort(e); } 
        
        System.out.println("[NBODY] " + size + " domains are deployed");
        
        // init Domains, with their possible neighbours, display, and quadtree
        Group group = ProActiveGroup.getGroup(domainGroup); 
        Domain [] domainArray = (Domain []) group.toArray(new Domain [] {}); 
        
        domainGroup.init(domainArray, displayer, tree, maxIter);
        // Within init, there are enough instructions to start computing the movement. 
    }
    
}