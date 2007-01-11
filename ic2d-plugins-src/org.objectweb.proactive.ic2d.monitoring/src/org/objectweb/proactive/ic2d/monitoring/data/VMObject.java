/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2005 INRIA/University of Nice-Sophia Antipolis
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
package org.objectweb.proactive.ic2d.monitoring.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeImpl;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.core.util.UrlBuilder;
import org.objectweb.proactive.ic2d.console.Console;
import org.objectweb.proactive.ic2d.monitoring.Activator;
import org.objectweb.proactive.p2p.service.util.P2PConstants;


public class VMObject extends AbstractDataObject {

	/** The key of this object */
	private String key;

	/** The ProActive runtime corresponding to this JVM */
	private ProActiveRuntime runtime;

	/** The JVM job ID */
	private String jobID;

	//
	// -- CONSTRUCTORS -----------------------------------------------
	//

	public VMObject(HostObject parent, ProActiveRuntime runtime) {
		super(parent);

		this.runtime = runtime;
		this.key = this.runtime.getVMInformation()/*.getVMID().toString()*/.getName();
		this.runtime = runtime;
		this.jobID = runtime.getJobID();

		getWorld().addToMonitoredObject(this);
	}

	//
	// -- PUBLIC METHOD -----------------------------------------------
	//

	/**
	 * Explores a ProActiveRuntime, in order to find all nodes known by this one.
	 */
	@Override
	public void explore(){

		// Enable or not the P2P Node monitoring
		boolean hideP2PNode = new Boolean(System.getProperty(
				P2PConstants.HIDE_P2PNODE_MONITORING)).booleanValue();

		String[] namesOfNodes = null;
		try {
			namesOfNodes = runtime.getLocalNodeNames();
		} catch (ProActiveException e) {
			e.printStackTrace();
		}
		for (int i = 0; i < namesOfNodes.length; ++i) {
			String nodeName = namesOfNodes[i];
			// Enable or not the P2P Node monitoring
			if (hideP2PNode &&
					(nodeName.compareTo(P2PConstants.P2P_NODE_NAME) == 0)) {
				continue;
			}
			if (nodeName.indexOf("SpyListenerNode") == -1) {
				handleNode(nodeName);
			}
		}
	}

	@Override
	public String getKey() {
		return this.key;
	}

	@Override
	public String getFullName() {
		return key;
	}

	@Override
	public String toString() {
		return "JVM " + this.getKey();
	}

	@Override
	public String getType() {
		return "JVM";
	}

	/**
	 * Returns the ProActiveRuntime associated to this object
	 * @return A ProActiveRuntime
	 */
	public ProActiveRuntime getRuntime() {
		return this.runtime;
	}

	/**
	 * Retuns the job id
	 * @return the job id
	 */
	public String getJobID() {
		return jobID;
	}

	@Override
	public void notResponding() {
		if(isAlive) {
			Console.getInstance(Activator.CONSOLE_NAME).warn(getFullName()+" is not responding");
		}
		super.notResponding();
	}

	public void enableMonitoring(boolean enable){
		List<AbstractDataObject> childrenList = new ArrayList<AbstractDataObject>(monitoredChildren.values());
		for(int i=0, size=childrenList.size(); i<size; i++)
			((NodeObject)childrenList.get(i)).enableMonitoring(enable);
	}

	//
	// -- PROTECTED METHOD -----------------------------------------------
	//

	/**
	 * Returns the parent of the VM, that's to say the host where is the VM
	 * @return the host of this VM
	 */
	protected HostObject getTypedParent() {
		return (HostObject) parent;
	}

	/**
	 * Get the ProActiveRuntime associated with this VMObject
	 * @return The ProActiveRuntime associated with this VMObject
	 */
	protected ProActiveRuntime getProActiveRuntime(){
		return this.runtime;
	}

	@Override
	protected void foundForTheFirstTime() {
		Console.getInstance(Activator.CONSOLE_NAME).
		log("VMObject id="+key+" created based on ProActiveRuntime "+runtime.getURL());
	}

	@Override
	protected void alreadyMonitored() {
		Console.getInstance(Activator.CONSOLE_NAME).
		log("VMObject id="+key+" already monitored, check for new nodes");
	}

	/**
	 * Returns all ProActiveRuntimes known by this JVM.
	 * @return a list containing all ProActiveRuntimes known by this JVM.
	 */
	protected List<ProActiveRuntime> getKnownRuntimes() {		
		ProActiveRuntime[] registered = null;
		try {
			registered = runtime.getProActiveRuntimes();
		} catch (ProActiveException e) {
			Console.getInstance(Activator.CONSOLE_NAME).logException(e);
		}
		return new ArrayList<ProActiveRuntime>(Arrays.asList(registered));
	}

	//
	// -- PRIVATE METHOD -----------------------------------------------
	//

	/**
	 * Handles node, in order to create a NodeObject associated to this one.
	 * @param nodeName The name of the node.
	 */
	private void handleNode(String nodeName){
		HostObject parent = getTypedParent();
		String nodeUrl = UrlBuilder.buildUrl(parent.getHostName(), nodeName,
				parent.getProtocol().toString (), parent.getPort());
		Node node = null;
		try {
			node = new NodeImpl(runtime, nodeUrl,UrlBuilder.getProtocol(nodeUrl), runtime.getJobID(nodeUrl));
		} catch (ProActiveException e) {
			notResponding();
			Console.getInstance(Activator.CONSOLE_NAME).debug(e);
		}

		NodeObject nodeObject = null;

		String key = node.getNodeInformation().getName();
		if ((! monitoredChildren.containsKey(key))
				&&
				(! skippedChildren.containsKey(key))){
			nodeObject = new NodeObject(this, node);
		}
		this.exploreChild(nodeObject);
	}
}
