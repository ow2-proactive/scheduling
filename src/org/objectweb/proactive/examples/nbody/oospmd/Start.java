package org.objectweb.proactive.examples.nbody.oospmd;


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

    public static void main(int totalNbBodies, int maxIter, Displayer displayer, Node[] nodes) {

        System.out.println("RUNNING oospmd VERSION");
        // Create all the Domains, based on the tree structure
        QuadTree tree = new QuadTree(totalNbBodies);
        Vector info = tree.getInfo(); 
        System.err.println(tree);
        int size = tree.size();
        
        Object [][] params = new Object [size][2]; // the parameters passed to the Domain constructors
        for (int  i = 0 ; i < size; i++)  {
            params[i][0] = new Integer(i);		      
            params[i][1] = (Info) info.get(i);		
        }
        Domain domainGroup = null;
        try {
            domainGroup = (Domain) ProSPMD.newSPMDGroup( Domain.class.getName(), params, nodes);
        }
        catch (ClassNotReifiableException e) { org.objectweb.proactive.examples.nbody.common.Start.abort(e); }
        catch (ClassNotFoundException e) { org.objectweb.proactive.examples.nbody.common.Start.abort(e); }
        catch (ActiveObjectCreationException e) { org.objectweb.proactive.examples.nbody.common.Start.abort(e); } 
        catch (NodeException e) { org.objectweb.proactive.examples.nbody.common.Start.abort(e); } 
        
        System.out.println("[NBODY] " + size + " domains are deployed");
        
        // init Domains, with their possible neighbours, display, and quadtree
        Group group = ProActiveGroup.getGroup(domainGroup); 
        //	Domain [] domainArray = (Domain []) group.toArray(); FIXME : this should work, man!
        Domain [] domainArray = new Domain [size];
        for (int  i = 0 ; i < size; i++) {
            Domain dom = (Domain) group.get(i);
            domainArray[i] = dom; 
        }
        
        if (displayer != null)
            domainGroup.init(domainArray, displayer, tree, maxIter);
        else
            domainGroup.init(domainArray, tree, maxIter);
        
        // Within init, there are enough instructions to start computing the movement. 
    }
    
}