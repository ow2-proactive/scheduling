/* * Created on Jan 31, 2005
 */
package org.objectweb.proactive.examples.nbody.barneshut;

import java.io.Serializable;

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


/**
 * @author irosenbe
 */
public class Glou implements Serializable{ 
    
    private static ProActiveDescriptor staticPad; // is set as static so that Maestro can call quit() which calls killall
    private int ident ;     
    /**
     * End the program, removing extra JVM that have been created with the deployment of the Domains
     */   
    
    public Glou () {}
    public Glou (Integer i) {
        ident = i.intValue();
    }
    
    public static void quit (){
        System.out.println(" PROGRAM ENDS ");
        try {
            staticPad.killall(true);        
        } catch (ProActiveException e) { e.printStackTrace(); }
        System.exit(0);
    }
    
    
    
    /**
     * Options should be "java Glou xmlFile [-display] totalNbBodies maxIter"
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
            String options = "xmlFile [-display] totalNbBodies maxIter";
        System.err.println("Usage : nboby.[bat|sh] " + options);
        System.err.println("from the command line, it would be   java Glou " + options);
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
        
        
        int size = 2;
        Object [][] params = new Object [size][1];
        for (int  i = 0 ; i < size; i++) 
            params[i][0] = new Integer(i);		      
        Glou [] glou;
        glou = new Glou [size];
        try {
            for (int i = 0 ; i < size ; i++)
                glou[i] = (Glou) ProActive.newActive(Glou.class.getName(), params[i]);
        } 
        catch (ActiveObjectCreationException e) { abort(e);}
        catch(NodeException e){	abort(e);}
        
        glou[0].sendSelf(glou[1]);
        Start.sleep (3);
        
        size = 5;
        params = new Object [size][1];
        for (int  i = 0 ; i < size; i++) 
            params[i][0] = new Integer(i);		      
        Glou glouGroup = null;
        try {
            glouGroup = (Glou) ProActiveGroup.newGroup ( Glou.class.getName(), params, nodes);
        }
        catch (ClassNotReifiableException e) { abort(e); }
        catch (ClassNotFoundException e) { abort(e); }
        catch (ActiveObjectCreationException e) { abort(e); } 
        catch (NodeException e) { abort(e); } 
        
        Group group = ProActiveGroup.getGroup (glouGroup);
        glou = new Glou [size]; 
        for (int i = 0 ; i < size ; i++)
            glou [i] = (Glou) group.get(i);
        
        glou[2].display(glou);
    }
    
    public void display(Glou[] glou) {
        for (int i = 0 ; i < glou.length; i++) {
            glou[i].echo(new SerString ("index : "  + i + " "));
        }
    }
    public void echo(SerString s) {
        System.out.println(s + "" + ident);        
    }
    public void sendSelf(Glou glou) {
        System.out.println(ident +" sending self to other ");
        glou.setNeighbour((Glou) ProActive.getStubOnThis());
    }
    
    
    public void setNeighbour(Glou glou) {
        System.out.println(ident +" setting new neighbour.");
        Glou neighbour = glou;
        SerString hello = new SerString("HELLO");
        System.out.println(ident +" sending " + hello + " to neighbour");
        neighbour.output(hello);
    }
    
    public void output(SerString hello) {
        System.out.println ( ident + " received " + hello );
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