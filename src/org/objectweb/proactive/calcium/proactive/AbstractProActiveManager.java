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
package org.objectweb.proactive.calcium.proactive;

import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.calcium.ResourceManager;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;

public abstract class AbstractProActiveManager extends ResourceManager{
	protected VirtualNode vnode;
	protected Node nodes[];
	
	private AbstractProActiveManager(Node nodes[], VirtualNode vn, String descriptorPath, String virtualNodeName){

		if(descriptorPath !=null && virtualNodeName !=null){
			ProActiveDescriptor pad = null;
			try {
				pad = ProActive.getProactiveDescriptor(descriptorPath);
			} catch (ProActiveException e) {
				logger.error("Error, unable to load ProActive descriptor: "+descriptorPath);
				e.printStackTrace();
				return;
			}
			vn = pad.getVirtualNode(virtualNodeName); 
		}
		
		if(vn !=null){
			vnode=vn;
			vnode.activate();
			try {
				this.nodes = vnode.getNodes();
			} catch (NodeException e) {
				logger.error("Error, unable to get nodes from virtual node: "+vn.getName());
				e.printStackTrace();
				return;
			}
		}
	}
	
	public AbstractProActiveManager(Node nodes[]){		
		this( nodes, null, null,null);
	}
	
	public AbstractProActiveManager(VirtualNode vn){
		this(null,vn,null,null);
	}
	
	public AbstractProActiveManager(String descriptorPath, String virtualNodeName){
		this(null,null,descriptorPath,virtualNodeName);
	}
	
	@Override
	public void shutdown(){
		if(vnode != null){
			vnode.killAll(false);
		}
	}
}
