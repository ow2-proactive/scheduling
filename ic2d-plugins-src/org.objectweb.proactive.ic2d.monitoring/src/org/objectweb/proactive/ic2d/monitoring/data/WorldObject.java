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
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.objectweb.proactive.ic2d.console.Console;
import org.objectweb.proactive.ic2d.monitoring.Activator;



/**
 * Holder class for all monitored hosts and virtual nodes
 */
public class WorldObject extends AbstractDataObject {

	public static boolean HIDE_P2PNODE_MONITORING = true;
	public static boolean DEFAULT_ENABLE_AUTO_RESET = false;

	public final static String ADD_VN_MESSAGE = "Add a virtual node";
	public final static String REMOVE_VN_MESSAGE = "Remove a virtual node";

	// 60 s
	public static int MAX_AUTO_RESET_TIME = 60;
	// 1 s
	public static int MIN_AUTO_RESET_TIME = 1;
	// 7 s
	public static int DEFAULT_AUTO_RESET_TIME = 7;

	private int currentAutoResetTime = DEFAULT_AUTO_RESET_TIME;
	private boolean enableAutoReset = DEFAULT_ENABLE_AUTO_RESET;

	/** The name of this world */
	private String name;

	private MonitorThread monitorThread;

	/** Contains all virtual nodes. */
	private Map<String, VNObject> vnChildren;

	public enum methodName { PUT_CHILD, REMOVE_CHILD, RESET_COMMUNICATIONS }

	/** Set of all the active object names */
	private Map<String, String> recorededFullNames = new Hashtable<String, String>();

	// This collection is used to optimize the research of an object.
	/** A collection of all the monitored objects */
	private Map<String, AbstractDataObject> allMonitoredObjects;

	private boolean hideP2P = HIDE_P2PNODE_MONITORING;

	//
	// -- CONSTRUCTORS -----------------------------------------------
	//

	/**
	 * Create a new WorldObject
	 */
	public WorldObject() {
		super(null);

		vnChildren = new ConcurrentHashMap<String, VNObject>();
		monitorThread = new MonitorThread(this);
		addObserver(monitorThread);

		// Record the model
		this.name = ModelRecorder.getInstance().addModel(this);

		this.allMonitoredObjects = new ConcurrentHashMap<String, AbstractDataObject>();
		addToMonitoredObject(this);
	}


	//
	// -- PUBLIC METHODS ---------------------------------------------
	//

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

	@Override
	public WorldObject getWorld() {
		return this;
	}

	@Override
	public Map<String, AbstractDataObject> getAllMonitoredObjects(){
		return this.allMonitoredObjects;
	}

	/**
	 * Add an object to the collection of monitored objects
	 * @param object 
	 */
	public void addToMonitoredObject(AbstractDataObject object){
		this.allMonitoredObjects.put(object.getKey(), object);
	}

	/**
	 * Remove from the collection of monitored objects an object
	 * @param object
	 */
	public void removeFromMonitoredObjects(AbstractDataObject object){
		this.allMonitoredObjects.remove(object.getKey());
	}

	/**
	 * Returns the MonitorThread associated to this object.
	 * @return The MonitorThread associated to this object.
	 */
	public MonitorThread getMonitorThread(){
		return monitorThread;
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
	 * Change the current auto reset time
	 * @param time The new time
	 */
	public void setAutoResetTime(int time){
		currentAutoResetTime = time;
	}

	/**
	 * Returns the current auto reset time
	 * @return The current auto reset time
	 */
	public int getAutoResetTime(){
		return this.currentAutoResetTime;
	}

	/**
	 * Enables the auto reset action
	 * @param enable
	 */
	public void setEnableAutoResetTime(boolean enable){
		enableAutoReset = enable;
	}

	/**
	 * Returns true if the auto reset time is enabled, false otherwise
	 * @return true if the auto reset time is enabled, false otherwise
	 */
	public boolean enableAutoResetTime(){
		return enableAutoReset;
	}

	public void enableMonitoring(boolean enable){
		List<AbstractDataObject> childrenList = new ArrayList<AbstractDataObject>(monitoredChildren.values());
		for(int i=0, size=childrenList.size(); i<size; i++)
			((HostObject)childrenList.get(i)).enableMonitoring(enable);
	}

	/**
	 * Use to hide or nor the p2p objects.
	 * @param hide true for hide the p2p object, false otherwise
	 */
	public void hideP2P(boolean hide){
		this.hideP2P = hide;
		getMonitorThread().forceRefresh();
	}

	/**
	 * Return true if the p2p objects ars hidden, false otherwise
	 * @return true if the p2p objects ars hidden, false otherwise
	 */
	public boolean isP2PHidden(){
		return this.hideP2P;
	}

	//
	// -- PROTECTED METHODS -----------------------------------------------
	//

	/**
	 * Add a host to this object 
	 * @param child the host added
	 */
	@Override
	protected void putChild(AbstractDataObject child) {
		monitoredChildren.put(child.getKey(), child);
		setChanged();
		if(monitoredChildren.size() == 1)
			notifyObservers(methodName.PUT_CHILD);
		notifyObservers();
	}

	/**
	 * Add a virtual node to this object
	 * @param vn
	 */
	protected void putVNChild(VNObject vn) {
		vnChildren.put(vn.getKey(), vn);
		setChanged();
		Hashtable<String, VNObject> data = new Hashtable<String, VNObject>();
		data.put(ADD_VN_MESSAGE, vn);
		notifyObservers(data);;
	}

	/**
	 * Remove a virtual node to this object
	 * @param vn
	 */
	protected void removeVNChild(VNObject vn){
		vnChildren.remove(vn.getKey());
		setChanged();
		Hashtable<String, VNObject> data = new Hashtable<String, VNObject>();
		data.put(REMOVE_VN_MESSAGE, vn);
		notifyObservers(data);
	}

	/**
	 * Returns a map of recorded full names
	 * @return A map of recorded full names
	 */
	protected Map<String, String> getRecordedFullNames(){
		return this.recorededFullNames;
	}

	/**
	 * Returns a virtual node.
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

}