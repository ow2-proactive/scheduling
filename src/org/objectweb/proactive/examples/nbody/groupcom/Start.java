package org.objectweb.proactive.examples.nbody.groupcom;


import org.objectweb.proactive.examples.nbody.common.Rectangle;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.group.ProActiveGroup;
import org.objectweb.proactive.core.mop.ClassNotReifiableException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;

/*
 * Created on Jan 7, 2005
 */

/**
 * @author irosenbe
 **/


public class Start {

    private static ProActiveDescriptor staticPad; // is set as static so that Maestro can call quit() which calls killall
   
    public static void main(String[] args) {
        
        boolean display = false; 
        int totalNbBodies = 4;
        int maxIter = 1000;   
        String xmlFileName = "";
                
        // Set arguments as read on command line
        switch (args.length){
        case 3 : 
            xmlFileName = args[0];
            totalNbBodies =  Integer.parseInt(args[1]);
            maxIter =  Integer.parseInt(args[2]);
            break;
        case 4 : 
            if (args[1].equals("-display")){
                display=true;
                xmlFileName = args[0];
                totalNbBodies =  Integer.parseInt(args[2]);
                maxIter =  Integer.parseInt(args[3]);
                break;
            }
            // else : don't break, which means go to the default case
        default :
            String options = "[-display] totalNbBodies maxIter";
            System.err.println("Usage : nbody.[bat|sh] " + options);
        	 System.err.println("from the command line, it would be   java Start xmlFile " + options);
        	 System.exit(1);
        }
	
        Displayer displayer = null;
        if (display) {
            try {
                displayer = (Displayer) (ProActive.newActive(
                        Displayer.class.getName(), new Object[] { new Integer(totalNbBodies) }));
            } catch (ActiveObjectCreationException e2) {
                e2.printStackTrace();
            } catch (NodeException e2) {
                e2.printStackTrace();
            }
        }
        try {
			
            // Init deployment-related variables: pad & nodes
            staticPad = null;
            VirtualNode vnode;
            try { staticPad = ProActive.getProactiveDescriptor(xmlFileName); }
            catch (ProActiveException e) { abort(e); }
            staticPad.activateMappings();
            vnode = staticPad.getVirtualNode("Workers"); 
            Node[] nodes = null;
            try { nodes = vnode.getNodes(); }
            catch (NodeException e) { abort(e); }
            
            			
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
 		       maestro = (Maestro) ProActive.newActive(Maestro.class.getName(), new Object[] {domainGroup});
			    } 
 		    catch (ActiveObjectCreationException e) { e.printStackTrace();   }
			catch(NodeException ex){  	ex.printStackTrace();    }
		
			
			// init workers
		    if (display)
		        domainGroup.init(domainGroup, displayer, maestro);
		    else
		        domainGroup.init(domainGroup,maestro);
        
		    // launch computation
		    // domainGroup.sendValueToNeighbours();
		    maestro.start(maxIter);
        }
			
        catch (NodeException e) { e.printStackTrace(); } 
        catch (ActiveObjectCreationException e) { e.printStackTrace(); } 
        catch (ClassNotReifiableException e1) { e1.printStackTrace(); }
        catch (ClassNotFoundException e1) { e1.printStackTrace(); }
    }

    /**
     * Stop with an error.
     * @param e the Exception which triggered the abrupt end of the program
     */
    public static void abort (Exception e) {
        e.printStackTrace();
        System.exit(-1);
    }

    /**
     * End the program, removing extra JVM that have been created with the deployment of the Domains
     */    
    public static void quit (){
        System.out.println(" PROGRAM ENDS ");
        try {
            staticPad.killall(true);            // FIXME why does this generate an exception?
        } catch (ProActiveException e) { e.printStackTrace(); }
        System.exit(0);
    }

}
