package org.objectweb.proactive.ic2d.gui.jobmonitor.data;

import org.objectweb.proactive.ic2d.gui.jobmonitor.JobMonitorConstants;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;


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
    private static final int[][] CHILDREN_KEYS = new int[NB_KEYS][];
    private static final MonitoredObjectSet EMPTY_MONITORED_SET = new MonitoredObjectSet();

    public DataAssociation() {
        asso = new TreeMap();
        sets = new MonitoredObjectSet[NB_KEYS];
        for (int i = 0; i < sets.length; i++)
            sets[i] = new MonitoredObjectSet();
    }

    private static int[] getChildrenKeys(int key) {
        int index = KEY2INDEX[key];
        if (CHILDREN_KEYS[index] != null) {
            return CHILDREN_KEYS[index];
        }

        int[] keys;
        switch (key) {
        case AO:
            keys = new int[] {  };
            break;
        case VN:
            keys = new int[] { NODE };
            break;
        case JOB:
            keys = new int[] { JVM, VN, NODE, AO };
            break;
        default:
            keys = new int[] { key + 1 };
            break;
        }

        CHILDREN_KEYS[index] = keys;
        return keys;
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
            res = new MonitoredObjectSet(from);
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
            res = new MonitoredObjectSet(from);
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
            return EMPTY_MONITORED_SET;
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

    interface Traversal {
        public void doSomething(BasicMonitoredObject child);
    }

    private void traverse(BasicMonitoredObject object, Traversal t) {
        int key = object.getKey();

        MonitoredObjectSet set = getSetForKey(key);
        if (!set.contains(object)) {
            return;
        }

        t.doSomething(object);

        int[] childrenKeys = getChildrenKeys(key);

        for (int i = 0; i < childrenKeys.length; i++) {
            MonitoredObjectSet desc = getWritableAsso(object, childrenKeys[i]);
            Iterator iter = desc.iterator();
            while (iter.hasNext()) {
                BasicMonitoredObject childObject = (BasicMonitoredObject) iter.next();
                t.doSomething(childObject);
            }
        }
    }

    /* Mark as deleted */
    public void deleteItem(BasicMonitoredObject object) {
        Traversal t = new Traversal() {
                public void doSomething(BasicMonitoredObject child) {
                    child.setDeleted(true);
                }
            };

        traverse(object, t);
    }

    public void removeItem(BasicMonitoredObject object) {
        final List assoKeysToRemove = new ArrayList();
        final List childrenToRemove = new ArrayList();
        Traversal t = new Traversal() {
                public void doSomething(BasicMonitoredObject child) {
                    childrenToRemove.add(child);
                    int[] childrenKeys = getChildrenKeys(child.getKey());
                    for (int i = 0; i < childrenKeys.length; i++) {
                        AssoKey removeKey = new AssoKey(child, childrenKeys[i]);
                        assoKeysToRemove.add(removeKey);
                    }
                }
            };

        traverse(object, t);

        Iterator iter = assoKeysToRemove.iterator();
        while (iter.hasNext()) {
            AssoKey assoKey = (AssoKey) iter.next();
            asso.remove(assoKey);
        }

        iter = childrenToRemove.iterator();
        Set clearedSets = new HashSet();
        while (iter.hasNext()) {
            BasicMonitoredObject child = (BasicMonitoredObject) iter.next();
            clearedSets.addAll(child.removeInReferences());
        }

        iter = clearedSets.iterator();
nextCleared: 
        while (iter.hasNext()) {
            MonitoredObjectSet set = (MonitoredObjectSet) iter.next();
            BasicMonitoredObject parent = set.getParent();
            if (parent == null) {
                continue;
            }

            int[] childrenKeys = getChildrenKeys(parent.getKey());
            for (int i = 0; i < childrenKeys.length; i++) {
                MonitoredObjectSet childrenSet = getWritableAsso(parent,
                        childrenKeys[i]);
                if (!childrenSet.isEmpty()) {
                    continue nextCleared;
                }
            }
            removeItem(parent);
        }
    }

    public void clearDeleted() {
        List toDelete = new ArrayList();

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

    public void updateReallyDeleted() {
        for (int i = 0; i < sets.length; i++) {
            Iterator iter = sets[i].iterator();
            while (iter.hasNext()) {
                BasicMonitoredObject object = (BasicMonitoredObject) iter.next();
                object.updateReallyDeleted();
            }
        }
    }
}
