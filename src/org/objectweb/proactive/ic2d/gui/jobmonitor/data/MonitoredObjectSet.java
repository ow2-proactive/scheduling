package org.objectweb.proactive.ic2d.gui.jobmonitor.data;

import java.util.*;


public class MonitoredObjectSet {
    private TreeMap map;

    public MonitoredObjectSet() {
        map = new TreeMap();
    }

    public BasicMonitoredObject add(BasicMonitoredObject o) {
        BasicMonitoredObject orig = (BasicMonitoredObject) map.get(o);
        if (orig == null) {
            map.put(o, o);
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
        }
    }
}
