/*
 * Created on Jan 31, 2005
 */
package org.objectweb.proactive.examples.nbody.barneshut;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.group.Group;
import org.objectweb.proactive.core.group.ProActiveGroup;
import org.objectweb.proactive.core.mop.ClassNotReifiableException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.examples.nbody.common.Displayer;


/**
 * @author irosenbe
 */
public class Start { 
    
    private static ProActiveDescriptor staticPad; // is set as static so that Maestro can call quit() which calls killall
    
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
    
    /**
     * Options should be "java Start xmlFile [-display] totalNbBodies maxIter"
     * @param -display, which is not compulsory, specifies whether a graphic display is to be created.
     * @param xmlFile is the xml deployment file..
     * @param totalNbBodies  The number of Planets in the System
     * @param maxIter The number of iterations before the program stops. 
     */
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
        
        // If need be, create a displayer
        Displayer displayer = null;
        if (display) {
            try {
                displayer = (Displayer) (ProActive.newActive(
                        Displayer.class.getName(), new Object[] { new Integer(totalNbBodies) }));
            } 
            catch (ActiveObjectCreationException e) { abort (e);}
            catch (NodeException e) { abort (e);}
        }
        
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
        
        sleep (2);
        
        // Create all the Domains, based on the tree structure
        QuadTree tree = new QuadTree(totalNbBodies);
        System.err.println(tree);
        int size = tree.size();
        Object [][] params = new Object [size][1];
        for (int  i = 0 ; i < size; i++) 
            params[i][0] = new Integer(i);		      
        Domain domainGroup = null;
        try {
            domainGroup = (Domain) ProActiveGroup.newGroup ( Domain.class.getName(), params, nodes);
        }
        catch (ClassNotReifiableException e) { abort(e); }
        catch (ClassNotFoundException e) { abort(e); }
        catch (ActiveObjectCreationException e) { abort(e); } 
        catch (NodeException e) { abort(e); } 
        
        System.out.println("[NBODY] " + size + " domains are deployed");
        
        // Create the master which synchronizes all activities 
        Maestro maestro = null;
        try {
            maestro = (Maestro) ProActive.newActive(Maestro.class.getName(), new Object[] {domainGroup});
        } 
        catch (ActiveObjectCreationException e) { abort(e);}
        catch(NodeException e){	abort(e);}
        
        // init Domains, with their neighbours, display, master, and quadtree
        
        Group group = ProActiveGroup.getGroup(domainGroup); 
        Domain [] domainArray = new Domain [size];
        for (int  i = 0 ; i < size; i++) {
            Domain dom = (Domain) group.get(i);
            domainArray[i] = dom; 
        }
        if (displayer != null)
            domainGroup.init(domainGroup, domainArray, displayer, maestro, tree);
        else
            domainGroup.init(domainGroup, domainArray,maestro,tree);
        
        sleep(4);
        // launch computation
        maestro.start(maxIter);
        
        
    }
    
    /**
     * Waits for te given time, before continuing the code.
     * May be useful for debug, synchronization should use barriers.
     * @param time the number of seconds to wait
     */
    public static void sleep(int time) {
        /*
        try {
            for (int i=0; i < time ; i++) {
                Thread.sleep(1000);
                System.out.print("SLEEP. ");
            }
        } catch (InterruptedException e) { abort (e); }
        */
    }
    
    /**
     * Stop with an error.
     * @param e the Exception which triggered the abrupt end of the program
     */
    public static void abort (Exception e) {
        e.printStackTrace();
        System.exit(-1);
    }
    
}