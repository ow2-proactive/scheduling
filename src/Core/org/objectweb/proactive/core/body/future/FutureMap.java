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
package org.objectweb.proactive.core.body.future;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.UniversalBody;


/**
 * Data structure which stores futures and corresponding automatic continuation to do.
 * This map is like :
 * [creatorID --> [sequenceID --> FuturesAndACs]]
 * @see FuturePool
 * @see FutureProxy
 */
public class FutureMap extends Object implements java.io.Serializable {
    // main map
    private Map<UniqueID, HashMap<Long, FuturesAndACs>> indexedByBodyID;

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //
    public FutureMap() {
        indexedByBodyID = new java.util.HashMap<UniqueID, HashMap<Long, FuturesAndACs>>();
    }

    /**
     * Add an AC to do for bodyDest when the futurePool will receive the value of the future
     * indexed by (id, creatorID)
     * @param id sequence id of the future
     * @param creatorID UniqueID of the creator body of the future
     * @param bodyDest body which receives the future (id, bodyID)
     */
    public synchronized void addAutomaticContinuation(long id,
        UniqueID creatorID, UniversalBody bodyDest) {
        java.util.HashMap<Long, FuturesAndACs> indexedByID = (indexedByBodyID.get(creatorID));
        if (indexedByID == null) {
            throw new ProActiveRuntimeException(
                "There is no map for creatorID " + creatorID);
        }
        FuturesAndACs listes = indexedByID.get(new Long(id));

        // add bodyDest to the list of dest for future (id, bodyID)
        if (listes != null) {
            listes.addDestinationsAC(bodyDest);
        } else {
            throw new ProActiveRuntimeException("There is no list for future " +
                id);
        }
    }

    /**
     * Add a future (id, creatorID) in the map. The entry for this key could already
     * exists, because a body can have multiple copies of the same future.
     * @param futureObject future to register
     */
    public synchronized void receiveFuture(Future futureObject) {
        long id = futureObject.getID();
        UniqueID creatorID = futureObject.getCreatorID();
        java.util.HashMap<Long, FuturesAndACs> indexedByID = indexedByBodyID.get(creatorID);

        // entry does not exist
        if (indexedByID == null) {
            //sub-map
            java.util.HashMap<Long, FuturesAndACs> newIndexedByID = new java.util.HashMap<Long, FuturesAndACs>();

            FuturesAndACs newf = new FuturesAndACs();
            newf.addFuture(futureObject);
            newIndexedByID.put(new Long(id), newf);
            indexedByBodyID.put(creatorID, newIndexedByID);
        }
        // entry for creatorID exists, but there is no sub-entry for id
        else if (indexedByID.get(new Long(id)) == null) {
            //list of futures
            FuturesAndACs newf = new FuturesAndACs();
            newf.addFuture(futureObject);
            indexedByID.put(new Long(id), newf);
        }
        // one copy of an existing future
        else {
            ((indexedByID.get(new Long(id)))).addFuture(futureObject);
        }
    }

    /**
     * Return the list of futures corresponding to (id,bodyID) if any, null otherwise.
     * @param id sequence id of the future
     * @param creatorID UniqueID of the creator body of the future
     */
    public synchronized ArrayList<Future> getFuturesToUpdate(long id,
        UniqueID creatorID) {
        java.util.HashMap<Long, FuturesAndACs> indexedByID = (indexedByBodyID.get(creatorID));
        ArrayList<Future> result = null;

        if (indexedByID != null) {
            FuturesAndACs listes = indexedByID.get(new Long(id));
            if (listes != null) {
                result = listes.getFutures();
            }
        }
        return result;
    }

    /**
     * Return the list of ACs to (ie bodies destination) corresponding to (id,bodyID) if any, null otherwise.
     * @param id sequence id of the future
     * @param bodyID UniqueID of the creator body of the future
     */
    public synchronized ArrayList<UniversalBody> getAutomaticContinuation(
        long id, UniqueID bodyID) {
        java.util.HashMap<Long, FuturesAndACs> indexedByID = (indexedByBodyID.get(bodyID));
        ArrayList<UniversalBody> result = null;
        if (indexedByID != null) {
            FuturesAndACs listes = indexedByID.get(new Long(id));
            if (listes != null) {
                result = listes.getDestinationsAC();
            }
        }
        return result;
    }

    /**
     * Return true if some ACs are remaining is this futuremap.
     * @return true if some ACs are remaining is this futuremap, false otherwise.
     */
    public boolean remainingAC() {
        Iterator<UniqueID> itAll = this.indexedByBodyID.keySet().iterator();
        while (itAll.hasNext()) {
            Map<Long, FuturesAndACs> currentMap = this.indexedByBodyID.get(itAll.next());
            Iterator<Long> itCur = currentMap.keySet().iterator();
            while (itCur.hasNext()) {
                FuturesAndACs curFAC = currentMap.get(itCur.next());
                if (curFAC.getDestinationsAC() != null) {
                    if (curFAC.getDestinationsAC().size() != 0) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Remove entry corresponding to (id, creatorID) in the futureMap.
     * @param id sequence id of the future
     * @param creatorID UniqueID of the creator body of the future
     */
    public synchronized void removeFutures(long id, UniqueID creatorID) {
        java.util.HashMap<Long, FuturesAndACs> indexedByID = (indexedByBodyID.get(creatorID));
        if (indexedByID != null) {
            indexedByID.remove(new Long(id));
        }
    }

    /**
     * Set the copy tag in all futures of the map.
     * @see FutureProxy
     */
    public synchronized void setCopyMode(boolean mode) {
        Collection<HashMap<Long, FuturesAndACs>> c1 = indexedByBodyID.values();
        Iterator<HashMap<Long, FuturesAndACs>> it1 = c1.iterator();
        while (it1.hasNext()) {
            Collection<FuturesAndACs> c2 = (it1.next()).values();
            Iterator<FuturesAndACs> it2 = c2.iterator();
            while (it2.hasNext()) {
                FuturesAndACs listes = it2.next();
                Iterator<Future> itFutures = listes.getFutures().iterator();
                while (itFutures.hasNext()) {
                    Future f = itFutures.next();
                    f.setCopyMode(mode);
                }
            }
        }
    }

    /**
     * Simple container for futures and automatic continuations (i.e. destination bodies)
     * for a given future's unique id (i.e. [CreatorID,SequenceID])
     * @author cdelbe
     * @since 3.2
     */
    private class FuturesAndACs {
        // futures
        private ArrayList<Future> futures;

        // destinations of ACs if any
        private ArrayList<UniversalBody> destinationsAC;

        /**
         * Create a FuturesAndACs
         * @param isACEnabled true if AC are enabled
         */
        public FuturesAndACs(boolean isACEnabled) {
            futures = new ArrayList<Future>();
            destinationsAC = isACEnabled ? new ArrayList<UniversalBody>() : null;
        }

        /**
         * Create a FuturesAndACs
         */
        public FuturesAndACs() {
            this(true);
        }

        /**
         * Return the list of registred futures
         * @return the list of registred futures
         */
        public ArrayList<Future> getFutures() {
            return futures;
        }

        /**
         * Register a future
         * @param f the registred future
         */
        public void addFuture(Future f) {
            this.futures.add(f);
        }

        /**
         * Return the list of registred ACs, i.e. target bodies
         * @return the list of registred ACs, i.e. target bodies
         */
        public ArrayList<UniversalBody> getDestinationsAC() {
            return destinationsAC;
        }

        /**
         * Register an AC
         * @param f the target body for the registred AC
         */
        public void addDestinationsAC(UniversalBody d) {
            this.destinationsAC.add(d);
        }
    }
}
