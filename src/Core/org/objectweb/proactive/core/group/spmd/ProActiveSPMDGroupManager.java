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
package org.objectweb.proactive.core.group.spmd;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;


/**
 * @author The ProActive Team
 */
public class ProActiveSPMDGroupManager implements java.io.Serializable {

    /**
     *  The spmd group he object belongs to
     */
    private Object spmdGroup = null;

    /**
     * The current 'active' barrriers.
     * The requests tagged with at least one of those barriers
     * will be ignored until the barrier will be released.
     */
    private HashMap<String, BarrierState> currentBarriers = new HashMap<String, BarrierState>(4); // initial capacity is 4.

    /**
     * The tags joint to the requests.
     * The requests tagged with those barriers
     * will be ignored until all those barriers will be released.
     */
    private LinkedList<String> barrierTags = new LinkedList<String>();

    /**
     * Set the SPMD group for the active object.
     * @param o - the new SPMD group
     */
    public void setSPMDGroup(Object o) {
        this.spmdGroup = o;
    }

    /**
     * Returns the SPMD group of the active object.
     * @return the SPMD group of the active object
     */
    public Object getSPMDGroup() {
        return this.spmdGroup;
    }

    /**
     * Adds the barrier ID into the list of barrier ID used to tag messages.
     * @param barrierID the barrier name
     */
    public void addToBarrierTags(String barrierID) {
        if (!this.barrierTags.contains(barrierID)) {
            this.barrierTags.add(barrierID);
        }
    }

    /**
     * Return a BarrierState object representing the current state of a barrier.
     * @param barrierName the name of the barrier
     * @return the state of the specified barrier
     */
    public BarrierState getBarrierStateFor(String barrierName) {
        return this.currentBarriers.get(barrierName);
    }

    /**
     * Set the number of awaited barrier calls to release the specified barrier
     * @param barrierName the name of the barrier
     * @param nbCalls the number of awaited calls
     */
    public void setAwaitedBarrierCalls(String barrierName, int nbCalls) {
        BarrierState bs = this.currentBarriers.get(barrierName);
        if (bs == null) {
            // System.out.println("First barrier \"" + this.getIDName() + "\" encountered !");
            // build and add infos about new barrier
            bs = new BarrierState();
            this.addToCurrentBarriers(barrierName, bs);
        }
        bs.setAwaitedCalls(nbCalls);
    }

    /**
     * Set a BarrierState for the specified barrier
     * @param barrierName the name of the barrier
     * @param bs a state for the barrier
     */
    public void addToCurrentBarriers(String barrierName, BarrierState bs) {
        this.currentBarriers.put(barrierName, bs);
    }

    /**
     * Remove the informations (BarrierState and tag) of the specified barrier
     * (invoked when the barrier is over).
     * @param barrierName the name of the barrier
     */
    public void remove(String barrierName) {
        // stop tagging the outgoing message
        this.barrierTags.remove(barrierName);
        // remove the barrier from the current active barriers list
        this.currentBarriers.remove(barrierName);
    }

    /**
     * Check if the list of barrier tags is empty
     * @return true if the the list is empty, false if it is not
     */
    public boolean isTagsListEmpty() {
        return (this.barrierTags.size() == 0);
    }

    /**
     * Return the list of barrier tags
     * @return a LinkedList containing the barrier tags
     */
    public LinkedList<String> getBarrierTags() {
        return this.barrierTags;
    }

    /**
     * Check if the tags given in parameter contains none
     * of the tags of the current barriers
     * @param barrierTags a list of Tag
     * @return true if barrierTags contains no tags of the current barriers, false if barrierTags contains at least one tag of the current barriers
     */
    public boolean checkExecution(LinkedList<String> barrierTags) {
        if (barrierTags == null) {
            return true;
        }
        Iterator<String> it = barrierTags.iterator();
        while (it.hasNext()) {
            if (this.currentBarriers.get(it.next()) != null) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check if there is active barriers
     * @return true if there is no active barrier, false if there is at least one active barrier
     */
    public boolean isCurrentBarriersEmpty() {
        return (this.currentBarriers.size() == 0);
    }
}
