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
package org.objectweb.proactive.benchmarks.timit.util.observing;

import java.util.Vector;

import org.objectweb.proactive.benchmarks.timit.util.EventStatistics;


/**
 * This class represents several StatDatas provided by observers of a subject.
 *
 * @author Brian Amedro, Vladimir Bodnartchouk
 *
 */
public class EventDataBag implements java.io.Serializable {

    /**
     *
     */

    /** The rank that identifies the subject ie the worker in a group */
    private int subjectRank;

    /** The vector of StatDatas */
    private Vector<EventData> bag;

    public EventDataBag() {
    }

    /** Creates a new instance of StatDataBag */
    public EventDataBag(int rank) {
        this.subjectRank = rank;
        this.bag = null;
    }

    public int getSubjectRank() {
        return this.subjectRank;
    }

    public void setBag(Vector<EventData> bag) {
        this.bag = bag;
    }

    public Vector<EventData> getBag() {
        return this.bag;
    }

    public EventData getEventData(int index) {
        return this.bag.get(index);
    }

    public int size() {
        return this.bag.size();
    }

    public EventStatistics getStats() {
        String[] counterName = new String[this.size()];
        Object[] value = new Object[this.size()];

        for (int i = 0; i < this.size(); i++) {
            counterName[i] = this.getEventData(i).getName();
            value[i] = this.getEventData(i);
        }

        return new EventStatistics(counterName, value, this.size(), this);
    }

    @Override
    public String toString() {
        String res = "";
        for (int i = 0; i < this.bag.size(); i++) {
            res += (this.bag.get(i).toString() + "\n");
        }

        return res;
    }
}
