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
    	if (iter.hasNext())
    		first = (BasicMonitoredObject) iter.next();
    	
    	return first;
    }
    
    public Object clone() {
    	MonitoredObjectSet copy = new MonitoredObjectSet(parent);
    	copy.addAll(this);
    	return copy;
    }
}
