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

import java.rmi.AlreadyBoundException;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.migration.MigrationException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.core.util.UrlBuilder;
import org.objectweb.proactive.ic2d.console.Console;
import org.objectweb.proactive.ic2d.monitoring.Activator;
import org.objectweb.proactive.ic2d.monitoring.filters.FilterProcess;
import org.objectweb.proactive.ic2d.monitoring.spy.Spy;
import org.objectweb.proactive.ic2d.monitoring.spy.SpyEventListener;
import org.objectweb.proactive.ic2d.monitoring.spy.SpyEventListenerImpl;
import org.objectweb.proactive.ic2d.monitoring.spy.SpyListenerImpl;

public class NodeObject extends AbstractDataObject{

	/** The virtual node containing this node */
	private VNObject vnParent;

	/** The node name */
	private String key;
	/** A ProActive Node */
	private Node node;
	/** A spy */
	private Spy spy;
	private static String SPY_LISTENER_NODE_NAME = "SpyListenerNode";
	private static Node SPY_LISTENER_NODE;
	private SpyListenerImpl activeSpyListener;

	/** The url of the node */
	private String url;

	/** The node job ID */
	private String jobID;

	static {
		String currentHost;
		try {
			currentHost = UrlBuilder.getHostNameorIP(java.net.InetAddress.getLocalHost());
		} catch (java.net.UnknownHostException e) {
			currentHost = "localhost";
		}
		try {
			SPY_LISTENER_NODE = NodeFactory.createNode(UrlBuilder.buildUrlFromProperties(
					currentHost, SPY_LISTENER_NODE_NAME), true, null, null);
		} catch (NodeException e) {
			SPY_LISTENER_NODE = null;
		} catch (AlreadyBoundException e) {
			e.printStackTrace();
		}
	}
	//
	// -- CONSTRUCTORS -----------------------------------------------
	//

	/**
	 * Creates a new NodeObject
	 * @param parent The VMObject parent
	 * @param node The node to associate to this object
	 */
	public NodeObject(VMObject parent, Node node){
		super(parent);

		Comparator<String> comparator = new AOObject.AOComparator();
		monitoredChildren = new TreeMap<String , AbstractDataObject>(comparator);
		this.node = node;
		this.url = node.getNodeInformation().getURL();
		this.key = node.getNodeInformation().getName();
		this.jobID = node.getNodeInformation().getJobID();

		getWorld().addToMonitoredObject(this);
	}

	//
	// -- PUBLIC METHOD -----------------------------------------------
	//

	/**
	 * Explores itself, in order to find all active objects known by this one
	 */
	@Override
	public void explore(){
		VMObject parent = getTypedParent();
		List<List<Object>> activeObjects = null;
		try {
			activeObjects = parent.getProActiveRuntime().getActiveObjects(this.key);
		} catch (ProActiveException e) {
			notResponding();
			Console.getInstance(Activator.CONSOLE_NAME).debug(e);
		}
		if(activeObjects!=null)
			handleActiveObjects(activeObjects);
	}

	@Override
	public String getKey() {
		return this.key;
	}

	public String getURL(){
		return this.url;
	}

	@Override
	public String getFullName() {
		return "Node "+this.key;
	}

	/**
	 * Returns the protocol used by the virtual machine containing this node.
	 * @return The protocol
	 */
	public Protocol getParentProtocol() {
		VMObject vm = (VMObject)getParent();
		String url = vm.getProActiveRuntime().getURL();
		String protocol = UrlBuilder.getProtocol(url);
		return Protocol.getProtocolFromString(protocol);
	}

	@Override
	public String toString() {
		return this.getKey();
	}

	@Override
	public String getType() {
		return "node";
	}

	public Spy getSpy() {
		return this.spy;
	}

	public String getJobID() {
		return this.jobID;
	}

	/**
	 * Used to highlight this node, in a virtual node.
	 * @param highlighted true, or false
	 */
	public void setHighlight(boolean highlighted) {
		this.setChanged();
		if (highlighted)
			this.notifyObservers(State.HIGHLIGHTED);
		else
			this.notifyObservers(State.NOT_HIGHLIGHTED);
	}

	/**
	 * Returns the virtual node parent
	 * @return the virtual node parent
	 */
	public VNObject getVNParent() {
		return vnParent;
	}

	/**
	 * Changes the update frequence of the spy
	 * @param updateFrequence
	 */
	public void setSpyUpdateFrequence(long updateFrequence) {
		spy.setUpdateFrequence(updateFrequence);
	}

	@Override
	public void stopMonitoring(boolean log) {
		destroySpy();
		this.vnParent.removeChild(this);
		super.stopMonitoring(log);
	}

	/**
	 * Migrates an active object from this node to another.
	 * @param activeObjectID The UniqueID of the active object.
	 * @param nodeTargetURL The target node.
	 * @throws MigrationException
	 */
	public void migrateTo(UniqueID activeObjectID, String nodeTargetURL) throws MigrationException {
		try {
			spy.migrateTo(activeObjectID, nodeTargetURL);
		} catch (MigrationException e) {
			throw e;
		} catch (Exception e) {
			throw new MigrationException("Problem contacting the Spy", e);
		}
	}

	public void enableMonitoring(boolean enable){
		activeSpyListener.enableMonitoring(enable);
	}
	//
	// -- PROTECTED METHOD -----------------------------------------------
	//

	/**
	 * Adds a spy in this node
	 */
	protected void addSpy(){
		try {
			SpyEventListener spyEventListener = new SpyEventListenerImpl(this);
			SpyListenerImpl spyListener = new SpyListenerImpl(spyEventListener);
			this.activeSpyListener = (SpyListenerImpl) ProActive.turnActive(spyListener,SPY_LISTENER_NODE);
			this.spy = (Spy) ProActive.newActive(Spy.class.getName(), new Object[] { activeSpyListener }, node);
		}
		catch(NodeException e) {
			Console.getInstance(Activator.CONSOLE_NAME).logException(e);
		}
		catch(ActiveObjectCreationException e) {
			Console.getInstance(Activator.CONSOLE_NAME).debug("Cannot create the spy on node " + this.getURL());
		}
	}

	/**
	 * Destroys the spy of node.
	 */
	protected synchronized void destroySpy(){
		if ((this.spy != null) && (this.activeSpyListener != null)) {
			try {
				this.spy.terminate();
			}
			catch(Exception e){ /* Do noting */}
			this.activeSpyListener.terminate();
			this.spy = null;
			this.activeSpyListener = null;
		}
	}

	@Override
	protected void foundForTheFirstTime() {
		Console.getInstance(Activator.CONSOLE_NAME).log("NodeObject created based on node "+key);
		this.addSpy();
		HostObject host = (HostObject) getParent().getParent();
		String os = host.getOperatingSystem();
		if(os==null){
			if(host==null)
				System.out.println("NodeObject.foundForTheFirstTime() host is null");
			else if(spy==null){
				System.out.println("[ERROR]Spy cant' be created");
				Console.getInstance(Activator.CONSOLE_NAME).err("Spy cant' be created");
				return;
			}
			else if(this.spy.getSystemProperty(HostObject.OS_PROPERTY)==null)
				System.out.println("NodeObject.foundForTheFirstTime() system property is null");
			else 
				host.setOperatingSystem(this.spy.getSystemProperty(HostObject.OS_PROPERTY));
		}
		try {
			// Add a RequestQueueEventListener to the spy
			this.spy.sendEventsForAllActiveObjects();
		} catch (Exception e) {
			// TODO spy not responding
			this.notResponding();
			Console.getInstance(Activator.CONSOLE_NAME).debug(this.getFullName()+" not responding");
		}

		String vnName = null;
		try {
			vnName = getTypedParent().getRuntime().getVNName(node.getNodeInformation().getName());
		} catch (ProActiveException e) {
			Console.getInstance(Activator.CONSOLE_NAME).logException(e);
			e.printStackTrace();
		}
		if(vnName != null) {
			this.vnParent = getWorld().getVirtualNode(vnName);
			if(vnParent == null) {
				ProActiveRuntime pr = getTypedParent().getRuntime();
				String jobID = null;
				try {
					jobID = pr.getJobID(pr.getURL() + "/" + getKey());
				} catch (ProActiveException e) {
					// TODO
					Console.getInstance(Activator.CONSOLE_NAME).logException(e);
				}
				vnParent = new VNObject(vnName, jobID, getWorld());
			}
			if(FilterProcess.getInstance().filter(this))
				vnParent.skippedChildren.put(key, this);
			else {
				vnParent.putChild(this);
				this.setChanged();
				this.notifyObservers();
			}
		}
	}

	@Override
	protected void alreadyMonitored() {
		Console.getInstance(Activator.CONSOLE_NAME).log("NodeObject id="+key+" already monitored, ckeck for new active objects");

		try {
			// Add a RequestQueueEventListener to the spy
			this.spy.sendEventsForAllActiveObjects();
		} catch (Exception e) {
			// TODO spy not responding
			this.notResponding();
		}
	}

	//
	// -- PRIVATE METHOD -----------------------------------------------
	//

	/**
	 * Gets the typed parent
	 * @return the typed parent
	 */
	private VMObject getTypedParent() {
		return (VMObject) parent;
	}

	/**
	 * Handles a list of Proactive active objects in order to create the AOObjects associated.
	 * And explore this objects.
	 * @param activeObjects Names' list of active objects containing in this NodeObject
	 */
	private void handleActiveObjects(List<List<Object>> activeObjects){
		for (int i = 0, size = activeObjects.size(); i < size; ++i) {
			List<Object> aoWrapper = activeObjects.get(i);
			UniversalBody ub = (UniversalBody)aoWrapper.get(0);
			String className = (String) aoWrapper.get(1);

			AOObject ao = null;

			if (! getAllMonitoredObjects().containsKey(ub.getID().toString())) {
				ao = new AOObject(this,className.substring(className.lastIndexOf(".")+1), ub.getID(), ub.getJobID());
				exploreChild(ao);
			}
		}
	}
}
