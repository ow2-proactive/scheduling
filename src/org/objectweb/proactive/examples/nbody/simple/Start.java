package org.objectweb.proactive.examples.nbody.simple;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.examples.nbody.common.Displayer;
import org.objectweb.proactive.examples.nbody.common.Rectangle;


public class Start {
    
    public static void main(String[] args) {  
        org.objectweb.proactive.examples.nbody.common.Start.main(args) ; 
    }
    
    public static void main(int totalNbBodies, int maxIter, Displayer displayer, Node[] nodes,
            org.objectweb.proactive.examples.nbody.common.Start killsupport) {
        System.out.println("RUNNING simplest VERSION");
        
        int root = (int) Math.sqrt(totalNbBodies);
        int STEP_X = 200 / root , STEP_Y = 200 / root;
        Domain [] domainArray = new Domain [totalNbBodies];
        for (int  i = 0 ; i < totalNbBodies ; i++) {
            Object [] params = new Object [] {
                    new Integer(i), 
                    new Rectangle(STEP_X * (i % root)-100, STEP_Y * (i / root) -100, STEP_X, STEP_Y),
            	};
            
            try {
                // Create all the Domains used in the simulation 
                domainArray[i] = (Domain) ProActive.newActive(
                        Domain.class.getName(), 
                        params, 
                        nodes[(i+1) % nodes.length]
                );
            }
            catch (ActiveObjectCreationException e) { killsupport.abort(e); } 
            catch (NodeException e) { killsupport.abort(e); }
        }
        
        System.out.println("[NBODY] " + totalNbBodies + " Planets are deployed");
        
        // Create a maestro, which will orchestrate the whole simulation, synchronizing the computations of the Domains
        Maestro maestro = null;
        try {
            maestro = (Maestro) ProActive.newActive (
                    Maestro.class.getName(), 
                    new Object[] {domainArray, new Integer(maxIter), killsupport} , 
                    nodes[0]
            );
        } 
        catch (ActiveObjectCreationException e) { killsupport.abort(e); } 
        catch (NodeException e) { killsupport.abort(e); }
        
        // init workers
        for (int i=0 ; i < totalNbBodies ; i ++)
            domainArray[i].init(domainArray, displayer, maestro);
        
    }
    
}
