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
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.objectweb.proactive.ic2d.console.Console;
import org.objectweb.proactive.ic2d.monitoring.Activator;



/**
 * Holder class for all monitored hosts and virtual nodes
 */
public class WorldObject extends AbstractDataObject {
	
	private static WorldObject instance;
	
	/** The name of this world */
	private String name;
	
	private MonitorThread monitorThread;
	
	/** Contains all virtual nodes. */
	private Map<String, VNObject> vnChildren;
	
	public enum methodName { PUT_CHILD, REMOVE_CHILD, RESET_COMMUNICATIONS }
	
	/** Set of all the active object names */
	private Map<String, String> recorededFullNames = new Hashtable<String, String>();
	
	//
    // -- CONSTRUCTORS -----------------------------------------------
    //
    
	/**
	 * Create a new WorldObject
	 */
	public WorldObject() {
        super(null);
        vnChildren = new HashMap<String, VNObject>();
        monitorThread = new MonitorThread(this);
        addObserver(monitorThread);
        
        // Record the model
        this.name = ModelRecorder.getInstance().addModel(this);
    }
	
	
    //
    // -- PUBLIC METHODS ---------------------------------------------
    //
	
	//TODO It is used by the Job monitoring, so resolve and delete this methode.
	public static WorldObject getInstance() {
		if(instance == null)
			instance = new WorldObject();
		return instance;
	}
	
	@Override
	public String getKey() {
		// A WorldObject doesn't need a key because it is the only son of IC2DObject.
		return "WorldObject";
	}
	
	@Override
	public String getFullName(){
		return "WorldObject";
	}

	@Override
	public void explore() {
		List<AbstractDataObject> childrenList = new ArrayList<AbstractDataObject>(monitoredChildren.values());
		for(int i=0, size=childrenList.size(); i<size; i++)
			((HostObject)childrenList.get(i)).explore();
	}
	
	@Override
	public String getType() {
		return "world";
	}

	
	public List<AbstractDataObject> getVNChildren() {
		return new ArrayList<AbstractDataObject>(vnChildren.values());
	}

	
	/**
	 * @see AbstractDataObject#stopMonitoring(boolean)
	 */
	@Override
	public void stopMonitoring(boolean log) {
		if(log)
			Console.getInstance(Activator.CONSOLE_NAME).log("Stop monitoring the " + getType() + " " + getFullName());
		Iterator<AbstractDataObject> iterator = monitoredChildren.values().iterator();
		while (iterator.hasNext()) {
			AbstractDataObject child = iterator.next();
			child.stopMonitoring(false);
		}
	}
	
	/**
	 * Returns the name of this world.
	 * @return The name of this world.
	 */
	public String getName(){
		return name;
	}
    //
    // -- PROTECTED METHODS -----------------------------------------------
    //
	
	/**
	 * Add a host to this object 
	 * @param child the host added
	 */
	@Override
	protected synchronized void putChild(AbstractDataObject child) {
		monitoredChildren.put(child.getKey(), child);
		setChanged();
		if(monitoredChildren.size() == 1)
			notifyObservers(methodName.PUT_CHILD);
		notifyObservers();
	}
    
	/**
	 * 
	 * @param vn
	 */
	protected synchronized void putVNChild(VNObject vn) {
		vnChildren.put(vn.getKey(), vn);
		setChanged();
		notifyObservers(vn);
	}
	
	/**
	 * Stop monitoring the host specified.
	 * @param child the host to stop monitoring
	 */
	@Override
	public void removeChild(AbstractDataObject child) {
		monitoredChildren.remove(child.getKey());
		setChanged();
		if(monitoredChildren.size() == 0)
			notifyObservers(methodName.REMOVE_CHILD);
		notifyObservers();
	}
	
	
    /**
     * Returns a map of recorded full names
     * @return A map of recorded full names
     */
    protected Map<String, String> getRecordedFullNames(){
    	return this.recorededFullNames;
    }
	
	/**
	 * 
	 * @param name
	 * @return
	 */
	protected VNObject getVirtualNode(String name) {
		return vnChildren.get(name);
	}
	
	
	@Override
	protected void alreadyMonitored() {/* Do nothing */}


	@Override
	protected void foundForTheFirstTime() {/* Do nothing */}


	@Override
	public WorldObject getWorld() {
		return this;
	}
	
	public MonitorThread getMonitorThread(){
		return monitorThread;
	}
}