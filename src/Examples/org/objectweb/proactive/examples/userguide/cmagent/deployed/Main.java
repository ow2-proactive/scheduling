/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 */
//@snippet-start cma_deploy_full
package org.objectweb.proactive.examples.userguide.cmagent.deployed;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PADeployment;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.examples.userguide.cmagent.initialized.CMAgentInitialized;
import org.objectweb.proactive.ActiveObjectCreationException;


public class Main {
	//deployment method
	//@snippet-start cma_deploy_method
	private static Node deploy(String descriptor)	{
		ProActiveDescriptor pad;
		Node node;
		try {
			//create object representation of the deployment file
			pad = PADeployment.getProactiveDescriptor(descriptor);
			//active all Virtual Nodes
			pad.activateMappings();
			//get the first Node available in the first Virtual Node 
			//specified in the descriptor file
			node = pad.getVirtualNodes()[0].getNode();
			return node;
		}
		catch (NodeException nodeExcep){
			System.err.println(nodeExcep.getMessage());
		}
		catch(ProActiveException proExcep){
			System.err.println(proExcep.getMessage());
		}
		return null;
	}
	//@snippet-end cma_deploy_method
    public static void main(String args[]) {
        try {
            String currentState = new String();
            //create the active oject
           //@snippet-start cma_deploy_object
            CMAgentInitialized ao = (CMAgentInitialized) 
            	PAActiveObject.newActive(CMAgentInitialized.class.getName(), 
            			new Object [] {}, deploy(args[0]));
            //@snippet-end cma_deploy_object
            currentState = ao.getCurrentState().toString();
            System.out.println(currentState);
            PAActiveObject.terminateActiveObject(ao, false);
        } catch (NodeException nodeExcep) {
            System.err.println(nodeExcep.getMessage());
        } catch (ActiveObjectCreationException aoExcep) {
            System.err.println(aoExcep.getMessage());
        }
    }
}
//@snippet-end cma_deploy_full