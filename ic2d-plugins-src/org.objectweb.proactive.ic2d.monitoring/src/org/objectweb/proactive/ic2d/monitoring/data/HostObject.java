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

import java.util.List;

import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.core.runtime.VMInformation;
import org.objectweb.proactive.core.util.UrlBuilder;
import org.objectweb.proactive.ic2d.console.Console;
import org.objectweb.proactive.ic2d.monitoring.Activator;
import org.objectweb.proactive.ic2d.monitoring.exceptions.HostAlreadyExistsException;
import org.objectweb.proactive.ic2d.monitoring.finder.HostRTFinder;
import org.objectweb.proactive.ic2d.monitoring.finder.HostRTFinderFactory;


/**
 * Holder class for the host data representation
 */
public class HostObject extends AbstractDataObject {

	/** Name of this Host */
	private String hostname;

	/** Number of the port */
	private int port;

	/** Name of Operating System */
	private String os;// = "OS undefined";

	/** Host's protocol */
	private Protocol protocol;

	//
	// -- CONSTRUCTORS -----------------------------------------------
	//

	/**
	 * Creates a new HostObject
	 * @parent hostname machine's name
	 * @param port
	 * @param protocol to use
	 * @param world The World
	 * @throws HostAlreadyExistsException 
	 */
	public HostObject(String hostname, int port, Protocol protocol, WorldObject world) throws HostAlreadyExistsException{
		super(world);
		this.hostname = hostname;
		this.port = port;
		this.protocol = protocol;

		HostObject hostAlreadyExists = (HostObject) this.parent.monitoredChildren.get(this.getKey());
		if(hostAlreadyExists != null)
			throw new HostAlreadyExistsException(hostAlreadyExists);

		this.parent.putChild(this);
		
		this.allMonitoredObjects.put(getKey(), this);
	}

	//
	// -- PUBLIC METHODS -----------------------------------------------
	//


	/**
	 * Explores the host, in order to find all VMs known by this one.
	 */
	@Override
	public void explore() {
		HostRTFinder runtimeFinder = HostRTFinderFactory.createHostRTFinder(this.protocol);
		List<ProActiveRuntime> foundRuntimes = runtimeFinder.findPARuntime(this);

		for (int i = 0; i < foundRuntimes.size(); ++i) {
			ProActiveRuntime proActiveRuntime = foundRuntimes.get(i);
			handleProActiveRuntime(proActiveRuntime, getWorld().getMonitorThread().getDepth());
		}
		if(monitoredChildren.size() == 0) { //we didn't find any child
			Console.getInstance(Activator.CONSOLE_NAME).warn("No ProActiveRuntimes were found on host "+getKey());
		}
	}

	/*@Override
	public void stopMonitoring(boolean log) {
		if(log)
			Console.getInstance(Activator.CONSOLE_NAME).log("Stop monitoring the " + getType() + " " + getFullName());
		((WorldObject)(this.parent)).removeChild(this);

		setChanged();
		notifyObservers(State.NOT_MONITORED);

		Iterator<AbstractDataObject> iterator = monitoredChildren.values().iterator();
		while (iterator.hasNext()) {
			AbstractDataObject child = iterator.next();
			child.stopMonitoring(false);
		}
	}*/

	@Override
	public String getKey() {
		return hostname+":"+port;
	}

	@Override
	public String getFullName(){
		if(os==null)
			return hostname+":"+port+":OS undefined";
		else
			return hostname+":"+port+":"+os;
	}


	/**
	 * @return Name of this Host
	 */
	public String getHostName(){
		return hostname;
	}

	/**
	 * @return Number of the port
	 */
	public int getPort(){
		return this.port;
	}

	/**
	 * Return the host's operating system
	 * @return a string representation of the host's operating system
	 */
	public String getOperatingSystem() {
		return os;
	}

//	protected void setOperatingSystem(String os){
//		this.os = os;
//		System.out.println("HostObject.setOperatingSystem()====>Os="+os);
//		setChanged();
//		notifyObservers(this.os);
//	}
	
	/**
	 * Returns the host's protocol
	 * @return The host's protocol
	 */
	public Protocol getProtocol(){
		return this.protocol;
	}

	/**
	 * Returns a string representing this host
	 */
	public String toString() {
		return "Host " + hostname;
	}

	@Override
	public String getType() {
		return "host";
	}


	//
	// -- PROTECTED METHOD -----------------------------------------------
	//


	/**
	 * Returns the parent with the real type
	 */
	protected WorldObject getTypedParent() {
		return (WorldObject) parent;
	}

	//
	// -- PRIVATE METHOD -----------------------------------------------
	//

	/**
	 * TODO
	 */
	private void handleProActiveRuntime(ProActiveRuntime runtime, int depth){
		VMObject vm = new VMObject(this, runtime);

		/*if (os == null) {
			os = vm.getParent().getSystemProperty("os.name");
			}
		 */
		exploreChild(vm);

		if(depth > 0) {
			List<ProActiveRuntime> knownRuntimes = vm.getKnownRuntimes();
			for(int i=0, size=knownRuntimes.size() ; i<size ; i++) {
				ProActiveRuntime pr = knownRuntimes.get(i);
				VMInformation infos = pr.getVMInformation();
				String hostname = infos.getHostName();
				String url = pr.getURL();
				int port = UrlBuilder.getPortFromUrl(url);
				String pro = UrlBuilder.getProtocol(url);
				Protocol protocol = Protocol.getProtocolFromString(pro.substring(0, pro.length()-1).toUpperCase());
				HostObject host;
				try {
					host = new HostObject(hostname, port, protocol, getWorld());
				} catch (HostAlreadyExistsException e) {
					continue;
				}
				host.handleProActiveRuntime(pr, depth-1);
			}
		}
	}

	@Override
	protected void alreadyMonitored() {/* Do nothing */}

	@Override
	protected void foundForTheFirstTime() {/* Do nothing */}

}
