/* 
* ################################################################
* 
* ProActive: The Java(TM) library for Parallel, Distributed, 
*            Concurrent computing with Security and Mobility
* 
* Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis
* Contact: proactive-support@inria.fr
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
package org.objectweb.proactive.core.body.future;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.Constants;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.mop.Utils;
import org.objectweb.proactive.core.util.ProActiveProperties;
import org.objectweb.proactive.core.body.reply.ReplyImpl;
import org.objectweb.proactive.core.body.reply.Reply;
import org.objectweb.proactive.core.body.LocalBodyStore;


public class FuturePool extends Object implements java.io.Serializable {

	protected boolean newState;
	private FutureMap futures;
	
	// ID of the body corresponding to this futurePool
	private UniqueID ownerBody;
	
	// Active queue of AC services
	private transient ActiveACQueue queueAC;
	
	// toggle for enabling or disabling automatic continuation 
	private boolean acEnabled;


	//
	// -- CONSTRUCTORS -----------------------------------------------
	//

	public FuturePool() {
		futures = new FutureMap();
		this.newState = false;
		if (ProActiveProperties.getACState().equals("enable"))
			this.acEnabled = true;
		else
			this.acEnabled = false;
		if (acEnabled) {
			queueAC = new ActiveACQueue();
			queueAC.start();
		}
	}

	//
	// -- STATIC ------------------------------------------------------
	//

	// this table is used to register destination before sending.
	// So, a future could retreive its destination during serialization
	// this table indexed by the thread which perform the registration.
	static private java.util.HashMap bodyDest;

	// to register in the table
	static private synchronized void registerBodyDest(UniversalBody dest) {
		bodyDest.put(Thread.currentThread(), dest);
	}

	// to clear an entry in the table
	static private synchronized void resetBodyDest() {
		bodyDest.remove(Thread.currentThread());
	}

	// to get a destination
	static public synchronized UniversalBody getBodyDest() {
		return (UniversalBody) (bodyDest.get(Thread.currentThread()));
	}

	// static init block
	static {
		bodyDest = new java.util.HashMap();
	}

	//
	// -- PUBLIC METHODS -----------------------------------------------
	//

	/**
	 * Setter of the ID of the body corresonding to this FuturePool
	 * @param i ID of the owner body.
	 */
	public void setOwnerBody(UniqueID i) {
		ownerBody = i;
	}

	/**	
	 * Getter of the ID of the body corresonding to this FuturePool
	 */
	public UniqueID getOwnerBody() {
		return ownerBody;
	}

	/**
	 * To enable the automatic continuation behaviour for all futures in
	 * this FuturePool
	 * */
	public void enableAC() {
		this.acEnabled = true;
	}

	/**
	 * To disable the automatic continuation behaviour for all futures in
	 * this FuturePool
	 * */
	public void disableAC() {
		this.acEnabled = false;
	}

	/**
	 * Method called when a reply is recevied, ie a value is available for a future.
	 * This method perform local futures update, and put an ACService in the activeACqueue.
	 * @param id sequence id of the future to update
	 * @param creatorID ID of the body creator of the future to update
	 * @param result value to "give" to the futures 
	 */
	public void receiveFutureValue(long id, UniqueID creatorID, Object result) throws java.io.IOException {

		// 1) Update futures
		java.util.ArrayList futuresToUpdate = getFuturesToUpdate(id, creatorID);

		if (futuresToUpdate != null) {
			Future future = (Future) (futuresToUpdate.get(0));
			if (future != null) {
				// Sets the result into the future
				future.receiveReply(result);
			}
			// if there are more than one future to update, we "give" deep copy
			// of the result to the other futures to respect ProActive model
			// We use here the migration tag to perform a simple serialization (ie 
			// without continuation side-effects)
			setMigrationTag();
			for (int i = 1; i < futuresToUpdate.size(); i++) {
				Future otherFuture = (Future) (futuresToUpdate.get(i));
				otherFuture.receiveReply(Utils.makeDeepCopy(result));
			}
			unsetMigrationTag();
			synchronizedStateChange();
		}

		// 2) create and put ACservices
		if (acEnabled) {
			java.util.ArrayList bodiesToContinue = getAutomaticContinuation(id, creatorID);
			if (bodiesToContinue != null)
				queueAC.addACRequest(new ACService(bodiesToContinue, new ReplyImpl(creatorID, id, null, result)));
		}
		
		// 3) Remove futures from the futureMap
		futures.removeFutures(id,creatorID);
	}

	// stateChange must be called in a synchronized method !
	private synchronized void synchronizedStateChange() {
		stateChange();
	}

	// to get list of futures to update in the futurMap		
	private java.util.ArrayList getFuturesToUpdate(long id, UniqueID bodyID) {
		return futures.getFuturesToUpdate(id, bodyID);
	}

	// to get list of Acs to perform 
	private java.util.ArrayList getAutomaticContinuation(long id, UniqueID bodyID) {
		return futures.getAutomaticContinuation(id, bodyID);
	}


	/**
	 * To put a future in the FutureMap
	 * @param id sequence id of the future
	 * @param creatorID UniqueID of the body which creates futureObject
	 * @param futureObject future to register
	 */
	public void receiveFuture(long id, UniqueID creatorID, Future futureObject) {
		futureObject.setSenderID(ownerBody);
		futures.receiveFuture(id, creatorID, futureObject);
		synchronizedStateChange();
	}

	/**
	 * To add an automatic contiunation, ie a destination body, for a particular future.
	 * @param id sequence id of the corresponding future
	 * @param creatorID UniqueID of the body which creates futureObject
	 * @param bodyDest body destination of this continuation
	 */
	public void addAutomaticContinuation(long id, UniqueID creatorID, UniversalBody bodyDest) {
		futures.addAutomaticContinuation(id, creatorID, bodyDest);
		synchronizedStateChange();
	}



	public synchronized void waitForReply() {
		this.newState = false;
		while (!newState) {
			try {
				wait();
			} catch (InterruptedException e) {
			}
		}

	}

	public void unsetMigrationTag() {
		futures.unsetMigrationTag();
	}


	/**
	 * Set the continuation tag for all futures in the futureMap.
	 * Register body destination in bodyDest static table 
	 */
	public void setContinuationTag(UniversalBody bodyDestination) {
		if (acEnabled) {
			this.registerBodyDest(bodyDestination);
			futures.setContinuationTag();
		}
	}

	/**
	 * Unset the continuation tag for all futures in the futureMap.
	 * Remove body destination in bodyDest static table.
	 */
	public void unsetContinuationTag() {
		if (acEnabled) {
			this.resetBodyDest();
			futures.unsetContinuationTag();
		}
	}
	
	//
	// -- PRIVATE METHODS -----------------------------------------------
	//

	private void stateChange() {
		this.newState = true;
		notifyAll();
	}

	private void setMigrationTag() {
		futures.setMigrationTag();
	}

	//
	// -- PRIVATE METHODS FOR SERIALIZATION -----------------------------------------------
	//

	private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
		setMigrationTag();
		out.defaultWriteObject();
		if (acEnabled) {
			// send the queue of AC requests
			out.writeObject(queueAC.getQueue());
			// stop the ActiveQueue thread 
			queueAC.killMe();
		}
	}

	private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
		in.defaultReadObject();
		unsetMigrationTag();
		if (acEnabled) {
			// create a new ActiveACQueue
			java.util.ArrayList queue = (java.util.ArrayList) (in.readObject());
			queueAC = new ActiveACQueue(queue);
			queueAC.start();
		}
	}

	//--------------------------------INNER CLASS------------------------------------//

	/**
	 * Active Queue for AC. This queue has his own thread to perform ACservices
	 * available in the queue. This thread is compliant with migration by using
	 * the threadStore of the body correponding to this FutureMap.
	 * Note that the ACServices are served in FIFO manner.
	 * @see ACservice
	 */
	private class ActiveACQueue extends Thread {

		private java.util.ArrayList queue;
		private int counter;
		private boolean kill;

		//
		// -- CONSTRUCTORS -----------------------------------------------
		//

		public ActiveACQueue() {
			queue = new java.util.ArrayList();
			counter = 0;
			kill = false;
			this.setName("Thread for AC");
		}

		public ActiveACQueue(java.util.ArrayList queue) {
			this.queue = queue;
			counter = queue.size();
			kill = false;
			this.setName("Thread for AC");
		}

		//
		// -- PUBLIC METHODS -----------------------------------------------
		//

		/**
		 * return the current queue of ACServices to perform
		 */
		public java.util.ArrayList getQueue() {
			return queue;
		}

		/**
		 * Add a ACservice in the active queue.
		 */
		public synchronized void addACRequest(ACService r) {
			queue.add(r);
			counter++;
			notifyAll();
		}

		/**
		 * To stop the thread.
		 */
		public synchronized void killMe() {
			kill = true;
			notifyAll();
		}


		public void run() {
			// get a reference on the owner body
			// try until it's not null because deserialization of the body 
			// may be not finished when we restart the thread.
			Body owner = null;
			while (owner == null) {
				owner = LocalBodyStore.getInstance().getLocalBody(ownerBody);
				// it's a halfbody...
				if (owner == null)
					owner = LocalBodyStore.getInstance().getLocalHalfBody(ownerBody);
			}

			while (true) {
				// if there is no AC to do, wait...
				while ((counter == 0) && !kill) {
					waitForAC();
				}
				if (kill)
					break;
				// there are ACs to do !
				try {
					// now we have it
					// enter in the threadStore 
					owner.enterInThreadStore();

					// if body has migrated, kill the thread
					if (kill)
						break;

					ACService toDo = (ACService) (queue.get(0));
					if (toDo != null)
						toDo.doAutomaticContinuation();

					// request is done, we can remove it
					queue.remove(0);
					counter--;

					// exit from the threadStore
					owner.exitFromThreadStore();
					// allows other actions to be done
					// Thread.yield();

				} catch (Exception e2) {
					// to unblock active object
					owner.exitFromThreadStore();
					throw new ProActiveRuntimeException("Error while sending reply for AC ", e2);
				}
			}
		}

		// synchronized wait
		private synchronized void waitForAC() {
			try {
				wait();
			} catch (InterruptedException e) {
			}
		}

	} 



	/**
	 * A simple object for a request for an automatic continuation
	 * @see ActiveACQueue
	 */
	private class ACService implements java.io.Serializable {

		// bodies that have to be updated	
		private java.util.ArrayList dests;
		// reply to send
		private Reply reply;

		//
		// -- CONSTRUCTORS -----------------------------------------------
		//

		public ACService(java.util.ArrayList dests, Reply reply) {
			this.dests = dests;
			this.reply = reply;
		}

		//
		// -- PUBLIC METHODS -----------------------------------------------
		//

		public void doAutomaticContinuation() throws java.io.IOException {
			if (dests != null) {
				setContinuationTag(null);
				for (int i = 0; i < dests.size(); i++) {
					UniversalBody dest = (UniversalBody) (dests.get(i));
					FuturePool.registerBodyDest(dest);
					dest.receiveReply(reply);
				}
				FuturePool.resetBodyDest();
				unsetContinuationTag();
			}
		}
	} //ACService

}
