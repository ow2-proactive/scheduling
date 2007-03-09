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
package org.objectweb.proactive.ic2d.gui.jobmonitor.data;

import java.util.Iterator;
import java.util.TreeMap;


public class MonitoredObjectSet implements Cloneable {
    private TreeMap map;
    private BasicMonitoredObject parent;

    public MonitoredObjectSet(BasicMonitoredObject parent) {
        this.map = new TreeMap();
        this.parent = parent;
    }

    public MonitoredObjectSet() {
        this(null);
    }

    public BasicMonitoredObject add(BasicMonitoredObject o) {
        BasicMonitoredObject orig = (BasicMonitoredObject) map.get(o);
        if (orig == null) {
            map.put(o, o);
            o.addReference(this);
        } else {
            o.copyInto(orig);
            o = orig;
        }
        return o;
    }

    public BasicMonitoredObject get(BasicMonitoredObject o) {
        return (BasicMonitoredObject) map.get(o);
    }

    public boolean contains(BasicMonitoredObject o) {
        return map.containsKey(o);
    }

    public void addAll(MonitoredObjectSet c) {
        Iterator i = c.iterator();
        while (i.hasNext()) {
            add((BasicMonitoredObject) i.next());
        }
    }

    public Iterator iterator() {
        return map.keySet().iterator();
    }

    public boolean isEmpty() {
        return map.keySet().isEmpty();
    }

    public void remove(BasicMonitoredObject o) {
        o = (BasicMonitoredObject) map.get(o);
        if (o != null) {
            map.remove(o);
            o.removeReference(this);
        }
    }

    public BasicMonitoredObject getParent() {
        return parent;
    }

    public BasicMonitoredObject firstElement() {
        Iterator iter = iterator();
        BasicMonitoredObject first = null;
        if (iter.hasNext()) {
            first = (BasicMonitoredObject) iter.next();
        }

        return first;
    }

    @Override
    public Object clone() {
        MonitoredObjectSet copy = new MonitoredObjectSet(parent);
        copy.addAll(this);
        return copy;
    }
}
