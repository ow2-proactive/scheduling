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
package active;
import java.io.IOException;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.api.ProDeployment;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.api.ProActiveObject;

public class Main{
	public static void main(String args[])
	{
		String path = new String(System.getProperty("user.dir") + 
								"/src/active/deployment.xml");
		ProActiveDescriptor pad;
		VirtualNode remoteNode; 
		Node node;
		try {
			pad = ProDeployment.getProactiveDescriptor(path);
			remoteNode = pad.getVirtualNode("remoteNode");
			// Returns the VirtualNode Workers described
			// in the xml file as a java object
			remoteNode.activate();
			// Activates the VirtualNode
			node = remoteNode.getNode();
			// Returns the first node available among nodes
			// mapped to the VirtualNode
			//shutdown hook
			
			InitializedHelloWorld ao = (InitializedHelloWorld)ProActiveObject.newActive(
					InitializedHelloWorld.class.getName(),
		            new Object [] {},
		            node);
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
		catch(ProActiveException proExcep){
			System.err.println(proExcep.getMessage());
		}
		//quitting
		ProActive.exitSuccess();
	}
}