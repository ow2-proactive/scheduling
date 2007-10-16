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
package org.objectweb.proactive.core.body.ft.message;

import java.io.Serializable;
import java.util.List;

import org.objectweb.proactive.core.UniqueID;


/**
 * This structure defines a part of a reception history. It is used to
 * update a ReceptionHistory object.
 * @author cdelbe
 * @since 3.0
 */
public class HistoryUpdater implements Serializable {

    /**
         *
         */
    private static final long serialVersionUID = 4942745847841355119L;

    /** Reception events, i.e. id of the senders */
    public List<UniqueID> elements;

    /** Reception index of the first element */
    public long base;

    /** Reception index of the last element */
    public long last;

    /** ID of the owner */
    public UniqueID owner;

    /** Index of the associated checkpoint */
    public int checkpointIndex;

    /** Incarnation number of the sender */
    public int incarnation;

    /**
     * Create an history updater.
     * @param elements Reception events, i.e. id of the senders
     * @param base Reception index of the first element
     * @param last Reception index of the last element
     * @param owner ID of the owner
     * @param checkpointIndex Index of the associated checkpoint
     * @param incarnation Incarnation number of the sender
     */
    public HistoryUpdater(List<UniqueID> elements, long base, long last,
        UniqueID owner, int checkpointIndex, int incarnation) {
        this.elements = elements;
        this.base = base;
        this.last = last;
        this.owner = owner;
        this.checkpointIndex = checkpointIndex;
        this.incarnation = incarnation;
    }
}
