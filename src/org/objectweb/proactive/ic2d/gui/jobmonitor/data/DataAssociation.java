package org.objectweb.proactive.ic2d.gui.jobmonitor.data;

import org.objectweb.proactive.ic2d.gui.jobmonitor.JobMonitorConstants;

import java.util.*;


class AssoKey implements Comparable {
    private int lkey;
    private int rkey;
    private String name;

    public AssoKey(BasicMonitoredObject from, int rkey) {
        this.lkey = from.getKey();
        this.rkey = rkey;
        this.name = from.getFullName();
    }

    public int compareTo(Object o) {
        AssoKey a = (AssoKey) o;

        if (lkey != a.lkey) {
            return lkey - a.lkey;
        }

        if (rkey != a.rkey) {
            return rkey - a.rkey;
        }

        return name.compareTo(a.name);
    }

    public String getName() {
        return name;
    }

    public String toString() {
        return name + "(" + lkey + "->" + rkey + ")";
    }
}


/*
 *           Job Job-*->VN     Job
 *            |         |       |
 *            *         *       *
 *            |         |       |
 *            v         v       v
 * Host -*-> JVM -*-> Node -*-> AO
 *                      ^
 *                      |
 *                      *
 *                      |
 *                     Job
 *
 */
public class DataAssociation implements JobMonitorConstants {
    private Map asso;
    private MonitoredObjectSet[] sets;

    public DataAssociation() {
        asso = new TreeMap();
        sets = new MonitoredObjectSet[NB_KEYS];
        for (int i = 0; i < sets.length; i++)
            sets[i] = new MonitoredObjectSet();
    }

    private MonitoredObjectSet getSetForKey(int key) {
        int index = KEY2INDEX[key];
        return sets[index];
    }

    private BasicMonitoredObject addToSet(BasicMonitoredObject value) {
        int key = value.getKey();
        MonitoredObjectSet set = getSetForKey(key);
        BasicMonitoredObject orig = set.get(value);
        if (orig == null) {
            set.add(value);
            orig = value;
        }

        return orig;
    }

    private void addAsso(BasicMonitoredObject from, BasicMonitoredObject to) {
        AssoKey key = new AssoKey(from, to.getKey());
        Object res = asso.get(key);
        if (res == null) {
            res = new MonitoredObjectSet();
            asso.put(key, res);
        }

        MonitoredObjectSet l = (MonitoredObjectSet) res;
        l.add(to);
    }

    public void addChild(BasicMonitoredObject from, BasicMonitoredObject to) {
        from = addToSet(from);
        to = addToSet(to);

        from.setDeleted(false);
        to.setDeleted(false);

        addAsso(from, to);
        addAsso(to, from);
    }

    private static boolean isSpecialKey(int key) {
        return (key == VN) || (key == JOB);
    }

    private MonitoredObjectSet getWritableAsso(BasicMonitoredObject from, int to) {
        AssoKey key = new AssoKey(from, to);
        Object res = asso.get(key);
        if (res == null) {
            res = new MonitoredObjectSet();
            asso.put(key, res);
        }

        return (MonitoredObjectSet) res;
    }

    private boolean isValid(BasicMonitoredObject value, Set constraints) {
        Iterator iter = constraints.iterator();
        while (iter.hasNext()) {
            BasicMonitoredObject testValue = (BasicMonitoredObject) iter.next();
            if (isSpecialKey(value.getKey()) ||
                    isSpecialKey(testValue.getKey())) {
                MonitoredObjectSet test = getValues(value, testValue.getKey(),
                        null);
                if (!test.contains(testValue)) {
                    return false;
                }
            }
        }

        return true;
    }

    private MonitoredObjectSet filter(MonitoredObjectSet set, Set constraints) {
        if ((constraints == null) || constraints.isEmpty() || set.isEmpty()) {
            return set;
        }

        MonitoredObjectSet res = new MonitoredObjectSet();
        Iterator iter = set.iterator();
        while (iter.hasNext()) {
            BasicMonitoredObject value = (BasicMonitoredObject) iter.next();
            if (isValid(value, constraints)) {
                res.add(value);
            }
        }

        return res;
    }

    private MonitoredObjectSet getAsso(BasicMonitoredObject from, int to,
        Set constraints) {
        MonitoredObjectSet res = getWritableAsso(from, to);
        return filter(res, constraints);
    }

    private static boolean isPermutation(int a, int b, int aa, int bb) {
        if (a > b) {
            a += b;
            b = a - b;
            a -= b;
        }

        if (aa > bb) {
            aa += bb;
            bb = aa - bb;
            aa -= bb;
        }

        return (a == aa) && (b == bb);
    }

    private MonitoredObjectSet handleSpecialPath(BasicMonitoredObject from,
        int to, Set constraints) {
        int key = from.getKey();

        if (isPermutation(key, to, JOB, HOST)) {
            int[] steps = { JVM, NODE, VN, AO };
            MonitoredObjectSet res = new MonitoredObjectSet();
            for (int i = 0; i < steps.length; i++)
                res.addAll(getValues(from, to, constraints, steps[i]));
            return res;
        }

        if (((key == VN) || (to == VN)) && !isPermutation(key, to, JOB, VN) &&
                !isPermutation(key, to, NODE, VN)) {
            return getValues(from, to, constraints, NODE);
        }

        return getAsso(from, to, constraints);
    }

    private MonitoredObjectSet getValues(BasicMonitoredObject from, int to,
        Set constraints, int step) {
        MonitoredObjectSet stepSet = getValues(from, step, constraints);
        if (stepSet.isEmpty()) {
            return new MonitoredObjectSet();
        }

        Iterator iter = stepSet.iterator();
        MonitoredObjectSet res = new MonitoredObjectSet();
        while (iter.hasNext()) {
            BasicMonitoredObject stepValue = (BasicMonitoredObject) iter.next();
            MonitoredObjectSet temp = getValues(stepValue, to, constraints);
            res.addAll(temp);
        }

        return res;
    }

    public MonitoredObjectSet getValues(BasicMonitoredObject from, int to,
        Set constraints) {
        int key = from.getKey();

        if (to == key) {
            MonitoredObjectSet res = new MonitoredObjectSet();
            res.add(from);
            return res;
        }

        if (from.isRoot()) {
            return list(to, constraints);
        }

        if (isSpecialKey(key) || isSpecialKey(to)) {
            return handleSpecialPath(from, to, constraints);
        }

        if ((to == (key + 1)) || (to == (key - 1))) {
            return getAsso(from, to, constraints);
        }

        int inc = (to > key) ? 1 : (-1);
        int step = to - inc;
        return getValues(from, to, constraints, step);
    }

    private MonitoredObjectSet list(int key, Set constraints) {
        MonitoredObjectSet res = getSetForKey(key);
        res = filter(res, constraints);
        return res;
    }
    
    public MonitoredObjectSet getJVM() {
    	return list(JVM, null);
    }
    
    public void clear() {
        for (int j = 0; j < sets.length; j++)
            if (sets[j] != null) {
                for (Iterator iter = sets[j].iterator(); iter.hasNext();) {
                    BasicMonitoredObject object = (BasicMonitoredObject) iter.next();
                    object.setDeleted(true);
                }
            }
    }

    /* Mark as deleted */
    public void deleteItem(BasicMonitoredObject value) {
        int key = value.getKey();

        MonitoredObjectSet set = getSetForKey(key);
        if (!set.contains(value)) {
            return;
        }

        value.setDeleted(true);

        if (isSpecialKey(key)) {
            // If we had a reference count : associatedNode.ref--
            return;
        }

        MonitoredObjectSet desc = getWritableAsso(value, key + 1);
        Iterator iter = desc.iterator();
        while (iter.hasNext()) {
            BasicMonitoredObject childValue = (BasicMonitoredObject) iter.next();
            deleteItem(childValue);
        }
    }

    public void removeItem(BasicMonitoredObject value) {
        int key = value.getKey();

        MonitoredObjectSet set = getSetForKey(key);
        if (!set.contains(value)) {
            return;
        }

        set.remove(value);

        if (isSpecialKey(key)) {
            // If we had a reference count : associatedNode.ref--
            return;
        }

        int[] keys = new int[] { key + 1, key - 1, JOB, VN };
        for (int i = 0; i < keys.length; i++) {
            AssoKey removeKey = new AssoKey(value, keys[i]);
            asso.remove(removeKey);
        }
    }

    public void clearDeleted() {
        List toDelete = new LinkedList();

        for (int i = 0; i < sets.length; i++) {
            Iterator iter = sets[i].iterator();
            while (iter.hasNext()) {
                BasicMonitoredObject object = (BasicMonitoredObject) iter.next();
                if (object.isDeleted()) {
                    toDelete.add(object);
                }
            }
        }

        Iterator iter = toDelete.iterator();
        while (iter.hasNext()) {
            removeItem((BasicMonitoredObject) iter.next());
        }
    }
}
