/* 
* ################################################################
* 
* ProActive: The Java(TM) library for Parallel, Distributed, 
*            Concurrent computing with Security and Mobility
* 
* Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis
* Contact: proactive-support@inria.fr
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
package org.objectweb.proactive.examples.nbody.groupoospmd;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.core.group.spmd.ProSPMD;
import org.objectweb.proactive.core.mop.ClassNotReifiableException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.examples.nbody.common.Displayer;
import org.objectweb.proactive.examples.nbody.common.Planet;
import org.objectweb.proactive.examples.nbody.common.Rectangle;

/**
 * Starts the simulation running the groupoospmd example.
 */
public class Start{
    
    public static void main(String[] args) {  
        org.objectweb.proactive.examples.nbody.common.Start.main(args) ; 
    }
    
    public static void main(int totalNbBodies, int maxIter, Displayer displayer, Node[] nodes,
            org.objectweb.proactive.examples.nbody.common.Start killsupport) {
        
        System.out.println("RUNNING group oo-spmd VERSION");
        
        Rectangle universe = new Rectangle(-100 , -100 , 100 , 100);
        Object [][] constructorParams = new Object [totalNbBodies][2] ;
        for (int  i = 0 ; i < totalNbBodies ; i++) {
            constructorParams[i][0] = new Integer(i);		      
            constructorParams[i][1] = new Planet (universe);
        }
        Domain domainGroup = null; 
        try {
            domainGroup = (Domain) ProSPMD.newSPMDGroup( Domain.class.getName(), constructorParams, nodes);
        }
        catch (NodeException e) { killsupport.abort(e);} 
        catch (ActiveObjectCreationException e) { killsupport.abort(e); } 
        catch (ClassNotReifiableException e) { killsupport.abort(e); }
        catch (ClassNotFoundException e) { killsupport.abort(e); }
        
        System.out.println("[NBODY] " + totalNbBodies + " Planets are deployed");
        
        // init workers
        domainGroup.init(displayer, maxIter, killsupport);
    }
    
}
