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

import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.UniversalBody;

/**
 * Data structure which stores futures and corresponding automatic continuation to do.
 * This map is like :
 * [creatorID --> [sequenceID --> {list of futures to update, list of bodies for AC}]]
 * @see FuturePool
 * @see FutureProxy 
 */
public class FutureMap extends Object implements java.io.Serializable {

	// main map
	private java.util.HashMap indexedByBodyID;

	//
	// -- CONSTRUCTORS -----------------------------------------------
	//

	public FutureMap() {
		indexedByBodyID = new java.util.HashMap();
	}

	/**
	 * Add an AC to do for bodyDest when the futurPool will receive the value of the future 
	 * indexed by (id, creatorID)
	 * @param id sequence id of the future 
	 * @param creatorID UniqueID of the creator body of the future
	 * @param bodyDest body which receives the future (id, bodyID)
	 */
	public synchronized void addAutomaticContinuation(long id, UniqueID creatorID, UniversalBody bodyDest) {
		java.util.HashMap indexedByID = (java.util.HashMap) (indexedByBodyID.get(creatorID));
		if (indexedByID == null)
			throw new ProActiveRuntimeException("There is no map for creatorID " + creatorID);
		java.util.ArrayList[] listes = (java.util.ArrayList[]) (indexedByID.get(new Long(id)));
		// add bodyDest to the list of dest for future (id, bodyID)
		if (listes != null)
			listes[1].add(bodyDest);
		else
			throw new ProActiveRuntimeException("There is no list for future " + id);
	}

	/**
	 * Add a future (id, creatorID) in the map. The entry for this key could already
	 * exists, because a body can have copies of a future.
	 * @param id sequence id of the future 
	 * @param creatorID UniqueID of the creator body of the future
	 * @param futureObject future to register
	 */
	public synchronized void receiveFuture(Future futureObject) {
		long id = futureObject.getID();
		UniqueID creatorID = futureObject.getCreatorID();
		java.util.HashMap indexedByID = (java.util.HashMap) (indexedByBodyID.get(creatorID));

		// entry does not exist
		if (indexedByID == null) {

			//sub-map
			java.util.HashMap newIndexedByID = new java.util.HashMap();
			//list of futures
			java.util.ArrayList futures = new java.util.ArrayList();
			futures.add(futureObject);
			//list of ACs (ie bodies destination)
			java.util.ArrayList dests = new java.util.ArrayList();

			java.util.ArrayList[] listes = new java.util.ArrayList[2];
			listes[0] = futures;
			listes[1] = dests;
			newIndexedByID.put(new Long(id), listes);
			indexedByBodyID.put(creatorID, newIndexedByID);
		}
		// entry for creatorID exists, but there is no sub-entry for id
		else if (indexedByID.get(new Long(id)) == null) {

			//list of futures
			java.util.ArrayList futures = new java.util.ArrayList();
			futures.add(futureObject);
			//list of ACs
			java.util.ArrayList dests = new java.util.ArrayList();

			java.util.ArrayList[] listes = new java.util.ArrayList[2];
			listes[0] = futures;
			listes[1] = dests;
			indexedByID.put(new Long(id), listes);
		}
		// one copy of an existing future
		else {
			(((java.util.ArrayList[]) (indexedByID.get(new Long(id))))[0]).add(futureObject);
		}
	}

	/**
	 * Return the list of futures corresponding to (id,bodyID) if any, null otherwise.
	 * @param id sequence id of the future 	
	 * @param creatorID UniqueID of the creator body of the future
	 */
	public synchronized java.util.ArrayList getFuturesToUpdate(long id, UniqueID creatorID) {
		java.util.HashMap indexedByID = (java.util.HashMap) (indexedByBodyID.get(creatorID));
		java.util.ArrayList resultat = null;

		if (indexedByID != null) {
			java.util.ArrayList[] listes = (java.util.ArrayList[]) (indexedByID.get(new Long(id)));

			if (listes != null) {
				java.util.ArrayList futures = listes[0];
				resultat = futures;
			}
		}
		// one of these two lists could be null : it's not an error ! Futures could have been already
		// updated. We must not throw exception in this case. 
		// Could be optimized...
		return resultat;

	}

	/**
	 * Return the list of ACs to (ie bodies destination) corresponding to (id,bodyID) if any, null otherwise.
	 * @param id sequence id of the future 	
	 * @param creatorID UniqueID of the creator body of the future
	 */
	public synchronized java.util.ArrayList getAutomaticContinuation(long id, UniqueID bodyID) {
		java.util.ArrayList resultat = null;
		java.util.HashMap indexedByID = (java.util.HashMap) (indexedByBodyID.get(bodyID));
		if (indexedByID != null) {
			java.util.ArrayList[] listes = (java.util.ArrayList[]) (indexedByID.get(new Long(id)));
			if (listes != null) {
				resultat = listes[1];
			}
		}
		return resultat;

	}

	/**
	 * Remove entry corresponding to (id, creatorID) in the futureMap.
	 * @param id sequence id of the future 	
	 * @param creatorID UniqueID of the creator body of the future
	 */
	public synchronized void removeFutures(long id, UniqueID creatorID) {
		java.util.HashMap indexedByID = (java.util.HashMap) (indexedByBodyID.get(creatorID));
		if (indexedByID != null) {
			java.util.ArrayList[] listes = (java.util.ArrayList[]) (indexedByID.remove(new Long(id)));
		}
	}



	/*
	 * Unset the migration tag in all futures of the map.
	 * @see FutureProxy
	 */
	public synchronized void unsetMigrationTag() {
		java.util.Collection c1 = indexedByBodyID.values();
		java.util.Iterator it1 = c1.iterator();

		while (it1.hasNext()) {
			java.util.Collection c2 = ((java.util.HashMap) (it1.next())).values();
			java.util.Iterator it2 = c2.iterator();
			while (it2.hasNext()) {
				java.util.ArrayList[] listes = (java.util.ArrayList[]) (it2.next());
				java.util.ArrayList futures = listes[0];
				java.util.Iterator itFutures = futures.iterator();
				while (itFutures.hasNext()) {
					FutureProxy p = (FutureProxy) itFutures.next();
					p.unsetMigrationTag();
				}
			}
		}
	}

	/**
	 * Set the migration tag in all futures of the map.
	 * @see FutureProxy
	 */
	public synchronized void setMigrationTag() {
		java.util.Collection c1 = indexedByBodyID.values();
		java.util.Iterator it1 = c1.iterator();

		while (it1.hasNext()) {
			java.util.Collection c2 = ((java.util.HashMap) (it1.next())).values();
			java.util.Iterator it2 = c2.iterator();
			while (it2.hasNext()) {
				java.util.ArrayList[] listes = (java.util.ArrayList[]) (it2.next());
				java.util.ArrayList futures = listes[0];
				java.util.Iterator itFutures = futures.iterator();
				while (itFutures.hasNext()) {
					FutureProxy p = (FutureProxy) itFutures.next();
					p.setMigrationTag();
				}
			}
		}
	}
	

}
