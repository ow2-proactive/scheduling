package org.objectweb.proactive.examples.nbody.common;

import java.io.IOException;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;


public class Start {

    private static ProActiveDescriptor staticPad; // is set as static, to use with quit() which calls killall
    /**
     * Options should be "java Start xmlFile [-display] totalNbBodies maxIter"
     * @param -display, which is not compulsory, specifies whether a graphic display is to be created.
     * @param xmlFile is the xml deployment file..
     * @param totalNbBodies  The number of Planets in the System.
     * @param maxIter The number of iterations before the program stops.
     */
    public static void main(String[] args) {
        int input = 0;
        boolean display = true;
        boolean displayft = false;
        int totalNbBodies = 4;
        int maxIter = 1000;          
        String xmlFileName ;
        // Set arguments as read on command line
        switch (args.length){
        case 0 :
            usage();
            System.out.println("No xml descriptor specified - aborting");
            quit();
        case 2 :
            if (args[1].equals("-display")){
                System.out.println("        Running with options set to 4 bodies, 1000 iterations, display on");
                break;
            }
            else if (args[1].equals("-displayft")){
                displayft=true;
                System.out.println("        Running with options set to 4 bodies, 1000 iterations, fault-tolerance display on");
                break;
            }
        case 3 :
            display=false;
            totalNbBodies =  Integer.parseInt(args[1]);
            maxIter =  Integer.parseInt(args[2]);
            break;
        case 4 :
            if (args[1].equals("-display")){
                totalNbBodies =  Integer.parseInt(args[2]);
                maxIter =  Integer.parseInt(args[3]);
                break;
            }
            else if (args[1].equals("-displayft")){
                displayft=true;
                totalNbBodies =  Integer.parseInt(args[2]);
                maxIter =  Integer.parseInt(args[3]);
                break;
            }
            // else : don't break, which means go to the default case
        default :
            usage();
        System.out.println("        Running with options set to 4 bodies, 1000 iterations, display on");
        }
        xmlFileName = args[0];
        
        System.out.println(" 1 : Simplest version, one-to-one communication and master");
        System.out.println(" 2 : group communication and master");
        System.out.println(" 3 : group communication, odd-even-synchronization");
        if (displayft){
            System.out.print("Choose which version you want to run [123] : ");
                try {
                while ( true ) {
                    // Read a character from keyboard
                    input  = System.in.read();
                    if ((input >= 49 && input <=51) ||input == -1)
                        break;
                }
            } catch (IOException ioe) {
                System.out.println( "IO error:" + ioe );
            }

        }else{
            System.out.println(" 4 : group communication, oospmd synchronization");
            System.out.println(" 5 : Barnes-Hut, and oospmd");
            System.out.print("Choose which version you want to run [12345] : ");
            try {
                while ( true ) {
                    // Read a character from keyboard
                    input  = System.in.read();
                    if ((input >= 49 && input <=53) ||input == -1)
                        break;
                }
            } catch (IOException ioe) {
                System.out.println( "IO error:" + ioe );
            }
        }
        System.out.println ("Thank you!");
        // If need be, create a displayer
        Displayer displayer = null;
        if (display) {
            try {
                displayer = (Displayer) (ProActive.newActive(
                        Displayer.class.getName(), new Object[] { new Integer(totalNbBodies) , new Boolean (displayft) }));
            }
            catch (ActiveObjectCreationException e) { abort (e);}
            catch (NodeException e) { abort (e);}
        }
        // Construct deployment-related variables: pad & nodes
        staticPad = null;
        VirtualNode vnode;
        try { staticPad = ProActive.getProactiveDescriptor(xmlFileName); }
        catch (ProActiveException e) { abort(e); }
        staticPad.activateMappings();
        vnode = staticPad.getVirtualNode("Workers");
        Node[] nodes = null;
        try { nodes = vnode.getNodes(); }
        catch (NodeException e) { abort(e); }
        switch (input) {
        case 49 :  org.objectweb.proactive.examples.nbody.simple.Start.main(totalNbBodies, maxIter, displayer, nodes); break;
        case 50 :  org.objectweb.proactive.examples.nbody.groupcom.Start.main(totalNbBodies, maxIter, displayer, nodes); break;
        case 51 :  org.objectweb.proactive.examples.nbody.groupdistrib.Start.main(totalNbBodies, maxIter, displayer, nodes); break;
        case 52 :  quit(); //org.objectweb.proactive.examples.nbody.groupoospmd.Start.main(totalNbBodies, maxIter, displayer, nodes); break;
        case 53 :  org.objectweb.proactive.examples.nbody.barneshut.Start.main(totalNbBodies, maxIter, displayer, nodes); break;
        }
    }
    /**
     * Shows what are the possible options to this program.
     */
    private static void usage() {
        String options = "[-display | -displayft] totalNbBodies maxIter";
        System.out.println("        Usage : nbody.[bat|sh] " + options);
        System.out.println("        from the command line, it would be   java Start xmlFile " + options);
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
     */       public static void quit (){
         System.out.println(" PROGRAM ENDS " + staticPad);
         try {
             staticPad.killall(true);            // FIXME why does this generate an exception?
         } catch (ProActiveException e) { e.printStackTrace(); }
         System.exit(0);
     }
}
