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

import java.util.Comparator;
import java.util.Map;

import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.migration.MigrationException;
import org.objectweb.proactive.ic2d.console.Console;
import org.objectweb.proactive.ic2d.monitoring.Activator;
import org.objectweb.proactive.ic2d.monitoring.filters.FilterProcess;

public class AOObject extends AbstractDataObject {

	/**
	 * Counter used to add a number after the name of the object if counter=3,
	 * and if the next object's name is "ao", then it's fullname will be "ao#3"
	 */
	private static int counter = 0;

	private java.util.Set<AOObject> communications;

	//private java.util.Set<AOObject> communicationsOld;

	private Thread dispatchThread;

	/** Time To Sleep (in seconds) */
	private float tts = 0.2f;

	//private float comm_fade = 3.0f;

	/** State of the object (ex: WAITING_BY_NECESSITY) */
	private State state = State.UNKNOWN;

	/** Old state of the object * */
	private State stateOld = State.UNKNOWN;

	/** the object's name (ex: ao) */
	private String name;

	/** the object's fullname (ex: ao#3) */
	private String fullName;

	/** id used to identify the active object globally, even in case of migration */
	private UniqueID id;

	/** the jobID of the active object */
	private String jobID;

	/** request queue length */
	private int requestQueueLength = -1; // -1 = not known

	private int requestQueueLengthOld = -1;

	//
	// -- CONSTRUCTORS -----------------------------------------------
	//

	/**
	 * Creates a new AOObject
	 * @param parent The Node containing the active object
	 * @param name The active object's name
	 * @param id The active object's id
	 */
	public AOObject(NodeObject parent, String name, UniqueID id, String jobID) {
		super(parent);

		if (name == null)
			name = this.getClass().getName();
		this.name = name;

		Map<String, String> fullNames = getWorld().getRecordedFullNames();
		String recordedName = fullNames.get(id.toString());

		communications = java.util.Collections
		.synchronizedSet(new java.util.HashSet<AOObject>());
		//communicationsOld = new java.util.HashSet<AOObject>();

		// If a name is already associated to this object
		if (recordedName != null) {
			this.fullName = recordedName;
		} else {
			// We shouldn't display this object, therefore we don't associate a number
			if (!FilterProcess.getInstance().filter(this))
				this.fullName = name + "#" + counter();
			else
				this.fullName = name;
			fullNames.put(id.toString(), fullName);
		}
		this.id = id;
		this.jobID = jobID;
		this.requestQueueLength = -1;

		this.allMonitoredObjects.put(getKey(), this);

		/* For the communication thread */
		this.dispatchThread = new Thread(new AOObjectRefresher());
		this.dispatchThread.setName(this.fullName);
		this.dispatchThread.start();

	}

	//
	// -- PUBLIC METHODS ---------------------------------------------
	//

	/**
	 * Returns the id of the active object.
	 * @return the id of the active object.
	 */
	public UniqueID getID() {
		return this.id;
	}

	/**
	 * Returns the job id of the active object.
	 * @return
	 */
	public String getJobID() {
		return this.jobID;
	}

	/**
	 * Returns the object's key. It is an unique identifier.
	 * @return the object's key
	 * @see AbstractDataObject#getKey()
	 */
	@Override
	public String getKey() {
		return this.id.toString();
	}

	/**
	 * Returns the object's name. (ex: ao)
	 * @return the object's name
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Returns the object's full name. (ex: ao#3)
	 * @return the object's full name.
	 * @see AbstractDataObject#getFullName()
	 */
	@Override
	public String getFullName() {
		return fullName;
	}

	/**
	 * Change the current state
	 * @param newState
	 */
	public void setState(State newState) {
		this.state = newState;
		// setChanged();
		// notifyObservers(this.state);
	}

	public State getState() {
		return this.state;
	}

	/**
	 * Add a communication between this active object and the active object
	 * destination.
	 * @param destination The destination active object
	 */
	public void addCommunication(AOObject destination) {
		synchronized (communications) {
			if (!communications.contains(destination)) {
				communications.add(destination);
			}
		}
		// setChanged();
		// notifyObservers(destination);
	}

	@Override
	public String toString() {
		return this.getFullName();
	}

	/**
	 * Returns a string representing the type ActiveObject : "ao"
	 * @return ao
	 * @see AbstractDataObject#getType()
	 */
	@Override
	public String getType() {
		return "ao";
	}

	/**
	 * Changes the request queue length
	 * @param length
	 * @see #getRequestQueueLength()
	 */
	public void setRequestQueueLength(int length) {
		requestQueueLength = length;
	}

	/**
	 * Returns the request queue length
	 * @return the request queue lenght
	 * @see #setRequestQueueLength(int)
	 */
	public int getRequestQueueLength() {
		return requestQueueLength;
	}

	/**
	 * Migrates this object to another node.
	 * @param nodeTargetURL
	 * @return true if it has successfully migrated, false otherwise.
	 */
	public boolean migrateTo(String nodeTargetURL) {
		Console console = Console.getInstance(Activator.CONSOLE_NAME);
		try {
			((NodeObject) getParent()).migrateTo(id, nodeTargetURL);
			console.log("Successfully migrated " + fullName + " to "
					+ nodeTargetURL);
			setState(State.MIGRATING);
			return true;
		} catch (MigrationException e) {
			console
			.err("Couldn't migrate " + fullName + " to "
					+ nodeTargetURL);
			console.logException(e);
			return false;
		}
	}

	@Override
	public void explore() {/* Do nothing */}

	@Override
	public void resetCommunications() {
		communications.clear();
		setChanged();
		notifyObservers(new java.util.HashSet<AOObject>(communications));
		// notifyObservers(State.NOT_MONITORED);
	}

	/**
	 * TODO
	 */
	public synchronized void dispatch() {

		if (super.isAlive) {

			java.util.HashSet<AOObject> communicationsNew = new java.util.HashSet<AOObject>(
					communications);
			communications.clear();

			// communicationsNew.removeAll(communicationsOld);

			if (!communicationsNew.isEmpty()) {
				setChanged();
				notifyObservers(communicationsNew);
				// communicationsOld = communicationsNew;
			}
			if (!state.equals(stateOld)) {
				setChanged();
				notifyObservers(state);
				stateOld = state;
			}
			if (requestQueueLength != requestQueueLengthOld) {
				setChanged();
				notifyObservers(requestQueueLength);
				requestQueueLengthOld = requestQueueLength;
			}
		}
	}

	//
	// -- PROTECTED METHODS ---------------------------------------------
	//

	@Override
	protected void finalize() {
		this.dispatchThread.stop();
	}
	
	@Override
	protected void foundForTheFirstTime() {
		// Add a MessageEventListener to the spy
		try {
			((NodeObject) this.parent).getSpy()
			.addMessageEventListener(this.id);
		} catch (Exception e) {
			this.parent.notResponding();
		}

		Console.getInstance(Activator.CONSOLE_NAME).log(
				"AOObject " + fullName + " created based on ActiveObject "
				+ id.toString());
	}

	@Override
	protected void alreadyMonitored() {
		Console.getInstance(Activator.CONSOLE_NAME).log(
				"AOObject " + fullName + " already monitored");
		AOObject.counter--;
	}

	//
	// -- PRIVATE METHODS ---------------------------------------------
	//

	/**
	 * Returns the next number to use for the 'fullname' of the object.
	 */
	private static synchronized int counter() {
		return ++counter;
	}

	// -- INNER CLASS -----------------------------------------------
	
	public static class AOComparator implements Comparator<String> {

		/**
		 * Compare two active objects. (For Example: ao#3 and ao#5 give -1
		 * because ao#3 has been discovered before ao#5.)
		 * 
		 * @return -1, 0, or 1 as the first argument is less than, equal to, or
		 *         greater than the second.
		 */
		public int compare(String ao1, String ao2) {
			String ao1Name = ao1;
			String ao2Name = ao2;
			return -(ao1Name.compareTo(ao2Name));
		}
	}

	private class AOObjectRefresher implements Runnable {

		public void run() {
			Long oldtime = System.currentTimeMillis();
			Long newtime = oldtime;
			while (true) {
				newtime = System.currentTimeMillis();
				if (newtime - oldtime > tts * 1000) {
					dispatch();
					oldtime = System.currentTimeMillis();
				} else {
					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
					}
					Thread.yield();
				}
			}
		}
	}
}
