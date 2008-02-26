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

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PADeployment;
import org.objectweb.proactive.api.PALifeCycle;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.api.PAActiveObject;

public class Main{
	private static VirtualNode[] deploy(String descriptor)
	{
		ProActiveDescriptor pad;
		try {
			pad = PADeployment.getProactiveDescriptor(descriptor);
			//active all Virtual Nodes
			pad.activateMappings();
			//get the first Node available in the first Virtual Node 
			//specified in the descriptor file
			return pad.getVirtualNodes();	
		}
		catch (NodeException nodeExcep){
			System.err.println(nodeExcep.getMessage());
		}
		catch(ProActiveException proExcep){
			System.err.println(proExcep.getMessage());
		}
		return null;
	}
	public static void main(String args[])
	{
		try{
			VirtualNode[] listOfVN = deploy(args[0]);
			//create the active object on the first node on
			//the first virtual node available
			//start the master
			PrimeManager master = (PrimeManager)PAActiveObject.newActive(
					PrimeManager.class.getName(),
		            new Object [] {},
		            listOfVN[0].getNode());

			//iterate through all the nodes and deploy
			//a worker on the first node on each VN available
			
			PrimeWorker worker;
			VirtualNode workers=listOfVN[0];
			for (Node node : workers.getNodes()) 
			{
				//deploy
				worker = (PrimeWorker)PAActiveObject.newActive(
						PrimeWorker.class.getName(),
			            new Object [] {},
			            node);
				master.addWorker(worker);
			}
			//tell the master to start
			master.startComputation(Long.parseLong(args[1]));
			//listOfVN[0].killAll(true);
		}
		catch (NodeException nodeExcep){
			System.err.println(nodeExcep.getMessage());
		}
		catch (ActiveObjectCreationException aoExcep){
			System.err.println(aoExcep.getMessage());
		}
		catch (NumberFormatException nrExcep){
			System.out.println("Number format is wrong !");
			System.err.println(nrExcep.getMessage());
			return;
		}
		//quitting
		PALifeCycle.exitSuccess();
	}
}