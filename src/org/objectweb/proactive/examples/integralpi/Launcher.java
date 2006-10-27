/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2006 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.examples.integralpi;

import com.sun.org.apache.bcel.internal.verifier.statics.LONG_Upper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.group.ProActiveGroup;
import org.objectweb.proactive.core.group.spmd.ProSPMD;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.wrapper.DoubleWrapper;

/**
 * This simple program approximates pi by computing<br>
 * pi = integral from 0 to 1 of 4/(1+x*x)dx<br>
 * which is approximated by<br>
 * sum from k=1 to N of 4 / ((1 + (k-1/2)**2 ).
 * The only input data required is N.<br>
 * <br>
 * This example is not intended to be the fastest.<br>
 *
 * @author Brian Amedro, Vladimir Bodnartchouk
 *
 */
public class Launcher {
    
    private static ProActiveDescriptor pad ;
    
    /** The main method, not used by TimIt */
    public static void main(String[] args) {
        
        try {
            // The number of workers
            int np = Integer.valueOf(args[1]).intValue();
            
            Object[] param = new Object[] {};
            Object[][] params = new Object[np][];
            for (int i = 0; i < np; i++) {
                params[i] = param;
            }
            
            Worker workers = (Worker) ProSPMD.newSPMDGroup(
                    Worker.class.getName(), params, provideNodes(args[0]));
            
            String input = "";
            long numOfIterations = 1;
            double result, error;
            
            while( numOfIterations > 0 ){
                
                // Prompt the user
                System.out.print( "\nEnter the number of iterations (0 to exit) : " );
                
                try {
                    // Read a line of text from the user.
                    input = stdin.readLine();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                
                numOfIterations = Long.parseLong(input);
                
                if ( numOfIterations <= 0 ) break;
                
                // Workers starts their job and return a group of Futures
                DoubleWrapper results = workers.start( numOfIterations );
                
                result = ((DoubleWrapper)ProActiveGroup.getGroup(results).get(0)).doubleValue();
                error =  result - Math.PI;
                
                System.out.println( "\nCalculated PI is " + result + " error is " + error );
            }
            
            finish();
            
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    private static BufferedReader stdin =
            new BufferedReader( new InputStreamReader( System.in ) );
    
    private static Node[] provideNodes( String descriptorUrl ){
        try {
            // Common stuff about ProActive deployement
            pad = ProActive.getProactiveDescriptor( descriptorUrl );
            
            pad.activateMappings();
            VirtualNode vnode = pad.getVirtualNodes()[0];
            
            Node[] nodes = vnode.getNodes();
            
            System.out.println(nodes.length + " nodes found");
            
            return nodes;
        } catch (NodeException ex) {
            ex.printStackTrace();
        } catch (ProActiveException ex) {
            ex.printStackTrace();
        }
        return null;
    }
    
    private static void finish(){
        try {
            pad.killall(false);
            ProActive.exitSuccess();
        } catch (ProActiveException ex) {
            ex.printStackTrace();
            ProActive.exitFailure();
        }
    }
}