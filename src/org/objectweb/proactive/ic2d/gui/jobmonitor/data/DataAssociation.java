package org.objectweb.proactive.ic2d.gui.jobmonitor.data;

import org.objectweb.proactive.ic2d.gui.jobmonitor.JobMonitorConstants;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/*
 * The key used in the TreeMap association
 */
class AssoKey implements Comparable {
    private int rkey;
    private BasicMonitoredObject from;

    public AssoKey(BasicMonitoredObject from, int rkey) {
        this.rkey = rkey;
        this.from = from;
    }

    public int compareTo(Object o) {
        AssoKey a = (AssoKey) o;
        int lkey = from.getKey();
        int alkey = a.from.getKey();

        if (lkey != alkey) {
            return lkey - alkey;
        }

        if (rkey != a.rkey) {
            return rkey - a.rkey;
        }

        return from.getFullName().compareTo(a.from.getFullName());
    }
}


/*
 * This is the main data structure. Here we keep track of the relationship between
 * all the elements we have. We have lots of associations summed up in the following diagram.
 * The arrow means one to many. For example, a host can contain many jvms but a jvm is in
 * only one host. 
 * 
 * 
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
 * We traverse this data structure to find the associated elements of any other elements.
 * For example, to find out in which VN a JVM is, we find its nodes, and for each node
 * its VN.
 * All these informations are stored in a map : (BasicMonitoredObject, key) -> MonitoredObjectSet
 * For a given object, and a given key. The latter being a successor on the diagram, it is
 * associated with its children. We also associate the child with its parent, thus the graph
 * can be traversed in both directions.
 */
public class DataAssociation implements JobMonitorConstants {
    private Map asso;
    private MonitoredObjectSet[] sets; /* We also keep track of every element of each key */
    private static final int[][] CHILDREN_KEYS = new int[NB_KEYS][]; /* The graph */
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

    /* 
     * We add the object to our collection, but if we already had it, we return
     * the previous version which should replace the just created new version.
     */
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

    /*
     * Remove all children of a given key.
     */
    private void delAsso(BasicMonitoredObject from, int key) {
    	AssoKey assoKey = new AssoKey(from, key);
    	asso.remove(assoKey);
    }
    
    /*
     * Add a child to an association. The from.getKey() -*-> to.getKey() or its
     * opposite should exist in the graph.
     */
    private void addAsso(BasicMonitoredObject from, BasicMonitoredObject to) {
        MonitoredObjectSet l = getWritableAsso(from, to.getKey());
        l.add(to);
    }

    /*
     * Called when exploring the network. We add the association in both directions.
     */
    public void addChild(BasicMonitoredObject from, BasicMonitoredObject to) {
        from = addToSet(from);
        to = addToSet(to);

        /*
         * Remove the element in its previous parent
         */
        BasicMonitoredObject previousParent = getWritableAsso(to, from.getKey()).firstElement();
        if (previousParent != null) {
        	getWritableAsso(previousParent, to.getKey()).remove(to);
        }
        
        /*
         * First new child of 'from', remove the previous ones.
         */
        if (from.isDeleted())
        	delAsso(from, to.getKey());
        
        from.setDeleted(false);
        to.setDeleted(false);

        addAsso(from, to);
        
        /*
         * We now know the only one true parent of 'to'.
         */
        delAsso(to, from.getKey());
        addAsso(to, from);
    }

    /*
     * In the beginning the graph was a tree, now it is no more.
     */
    private static boolean isSpecialKey(int key) {
        return (key == VN) || (key == JOB);
    }

    /*
     * Get the children, we can write the to set, the modifications will be saved.
     */
    private MonitoredObjectSet getWritableAsso(BasicMonitoredObject from, int to) {
        AssoKey key = new AssoKey(from, to);
        Object res = asso.get(key);
        if (res == null) {
            res = new MonitoredObjectSet(from);
            asso.put(key, res);
        }

        return (MonitoredObjectSet) res;
    }

    /*
     * As the graph is not a tree when traversing we may encounter elements
     * which are not descendants of the previously traversed elements, so we
     * filter them out.
     */
    private boolean isValid(BasicMonitoredObject value, Set constraints) {
        Iterator iter = constraints.iterator();
        int key = value.getKey();
        
        while (iter.hasNext()) {
            BasicMonitoredObject testValue = (BasicMonitoredObject) iter.next();

            if (key == AO && testValue.getKey() == JOB)
            	/* Migration */
            	continue;

            MonitoredObjectSet test = getValues(value, testValue.getKey(), null);
            if (!test.contains(testValue)) {
            	return false;
            }
        }

        return true;
    }

    /*
     * We check every element against the constraints, see the above comment for the
     * rationale.
     */
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

    /*
     * When we are not simply traversing the tree, we end up there.
     * Lots of special cases ;-)
     */
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

        return getWritableAsso(from, to);
    }

    /*
     * Advance one step in a getValues() call
     */
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
    
    /*
     * Listing for the IC2D with big boxes
     */
    public MonitoredObjectSet getHosts() {
        return list(HOST, null);
    }
    
    public MonitoredObjectSet getNodes() {
        return list(NODE, null);
    }
    
    public MonitoredObjectSet getAOs() {
        return list(AO, null);
    }
    
    
    /*
     * Main entry point to extract data.
     */
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
            return filter(handleSpecialPath(from, to, null), constraints);
        }

        if ((to == (key + 1)) || (to == (key - 1))) {
            return filter(getWritableAsso(from, to), constraints);
        }

        int inc = (to > key) ? 1 : (-1);
        int step = to - inc;
        return filter(getValues(from, to, null, step), constraints);
    }

    private MonitoredObjectSet list(int key, Set constraints) {
        MonitoredObjectSet res = getSetForKey(key);
        res = filter(res, constraints);
        return res;
    }

    public MonitoredObjectSet getJVM() {
        return list(JVM, null);
    }

    /*
     * Before exploring the network, we clear everything, to have Mark-and-sweep
     * in some sort.
     */
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

    /*
     * Recursively traverse an element and its children
     */
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
                traverse(childObject, t);
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

    /*
     * Really remove the element and its children from our knowledge.
     * To avoid ConcurrentModificationException, we first get all the elements
     * we will remove.
     */
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

        /* Fill in assoKeysToRemove and childrenToRemove */
        traverse(object, t);

        /* Remove the associations */
        Iterator iter = assoKeysToRemove.iterator();
        while (iter.hasNext()) {
            AssoKey assoKey = (AssoKey) iter.next();
            asso.remove(assoKey);
        }

        /* Remove the children in their parents, keep track of emptied parents */
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
            /* If the parent has no more children, remove it */
            removeItem(parent);
        }
    }

    /*
     * Elements marked as deleted are really deleted.
     * Again we use a copy to avoid ConcurrentModificationException.
     */
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

    /* Sweep phase of the Mark-and-sweep */
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
