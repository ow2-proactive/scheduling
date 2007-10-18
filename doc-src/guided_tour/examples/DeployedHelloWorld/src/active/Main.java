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
 *  Contributor(s):	Vasile Jureschi
 *
 * ################################################################
 */
package active;
import java.io.IOException;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.api.ProDeployment;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.api.ProActiveObject;

public class Main{
	//deployment method
	private static Node deploy(String descriptor)	{
		ProActiveDescriptor pad;
		Node node;
		try {
			//create object representation of the deployment file
			pad = ProDeployment.getProactiveDescriptor(descriptor);
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
	public static void main(String args[]){
		try{
		 	InitializedHelloWorld ao = (InitializedHelloWorld)ProActiveObject.newActive(
		 								InitializedHelloWorld.class.getName(),
		 								new Object [] {}, deploy(args[0]));
			//say hello 
			System.out.println(ao.sayHello()); //possible wait-by-necessity
			ao.terminate();
		}
		catch (NodeException nodeExcep){
			System.err.println(nodeExcep.getMessage());
		}
		catch (ActiveObjectCreationException aoExcep){
			System.err.println(aoExcep.getMessage());
		}
		catch(IOException ioExcep){
			System.err.println(ioExcep.getMessage());
		}
		//quitting
		ProActive.exitSuccess();
	}
}