package org.objectweb.proactive.ic2d.gui.jobmonitor.data;

import java.util.*;

import org.objectweb.proactive.ic2d.gui.jobmonitor.JobMonitorConstants;

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
		
		if (lkey != a.lkey)
			return lkey - a.lkey;
		
		if (rkey != a.rkey)
			return rkey - a.rkey;
		
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
	private Set[] sets;
	
	public DataAssociation() {
		clear();
	}
	
	private Set getSetForKey(int key) {
		int index = KEY2INDEX[key];
		return sets[index];
	}
	
	private void makeSet(int key) {
		int index = KEY2INDEX[key];
		sets[index] = new TreeSet();
	}
	
	private void addToSet(BasicMonitoredObject value) {
		int key = value.getKey();
		int index = KEY2INDEX[key];
		if (getSetForKey(key) == null)
			makeSet(key);
		
		getSetForKey(key).add(value);
	}
	
	private void addAsso(BasicMonitoredObject from, BasicMonitoredObject to) {
		addToSet(to);
		
		AssoKey key = new AssoKey(from, to.getKey());
		Object res = asso.get(key);
		if (res == null) {
			res = new TreeSet();
			asso.put(key, res);
		}
		
		Set l = (Set) res;
		l.add(to);
	}

	public void addChild(BasicMonitoredObject from, BasicMonitoredObject to) {
		addAsso(from, to);
		addAsso(to, from);
	}

	private boolean isSpecialKey(int key) {
		return key == VN || key == JOB;
	}
	
	private Set getWritableAsso(BasicMonitoredObject from, int to) {
		AssoKey key = new AssoKey(from, to);
		Object res = asso.get(key);
		if (res == null) {
			res = new TreeSet();
			asso.put(key, res);
		}
		
		return (Set) res;
	}
	
	private boolean isValid(BasicMonitoredObject value, Set constraints) {
		Iterator iter= constraints.iterator();
		while (iter.hasNext()) {
			BasicMonitoredObject testValue = (BasicMonitoredObject) iter.next();
			Set test = getValues(value, testValue.getKey(), null);
			if (!test.contains(testValue))
				return false;
		}

		return true;
	}
	
	private Set filter(int key, Set set, Set constraints) {
		if (constraints == null || constraints.isEmpty() || set.isEmpty())
			return set;
		
		Set res = new TreeSet();
		Iterator iter = set.iterator();
		while (iter.hasNext()) {
			BasicMonitoredObject value= (BasicMonitoredObject) iter.next();
			if (isValid(value, constraints))
				res.add(value);
		}
		
		return res;
	}
	
	private Set getAsso(BasicMonitoredObject from, int to, Set constraints) {
		Set res = getWritableAsso(from, to);
		return filter(to, res, constraints);
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
		
		return a == aa && b == bb;
	}
	
	private Set handleSpecialPath(BasicMonitoredObject from, int to, Set constraints) {
		int key = from.getKey();
		
		if (isPermutation(key, to, JOB, HOST)) {
			int[] steps = {JVM, NODE, VN, AO};
			Set res = new TreeSet();
			for (int i = 0; i < steps.length; i++)
				res.addAll(getValues(from, to, constraints, steps[i]));
			return res;
		}
		
		if ((key == VN || to == VN) && !isPermutation(key, to, JOB, VN) && !isPermutation(key, to, NODE, VN))
			return getValues(from, to, constraints, NODE);

		return getAsso(from, to, constraints);
	}
	
	private Set getValues(BasicMonitoredObject from, int to, Set constraints, int step) {
		Set stepSet = getValues(from, step, constraints);
		if (stepSet.isEmpty())
			return new TreeSet();
		
		Iterator iter = stepSet.iterator();
		Set res = new TreeSet();
		while (iter.hasNext()) {
			BasicMonitoredObject stepValue= (BasicMonitoredObject) iter.next();
			Set temp = getValues(stepValue, to, constraints);
			res.addAll(temp);
		}
		
		return res;
	}
	
	/*
	 * Exemple : getValues(VN, "myVN", AO) ==> {"Object1", "Object2"}
	 */
	public Set getValues(BasicMonitoredObject from, int to, Set constraints) {
		int key = from.getKey();
		
		if (to == key) {
			Set res = new TreeSet();
			res.add(from);
			return res;
		}

		if (from.getFullName() == null)
			return list(to, constraints);
		
		if (isSpecialKey(key) || isSpecialKey(to))
			return handleSpecialPath(from, to, constraints);
		
		if (to == key + 1 || to == key - 1)
			return getAsso(from, to, constraints);

		int inc = (to > key) ? 1 : -1;
		int step = to - inc;
		return getValues(from, to, constraints, step);
	}
	
	private Set list(int key, Set constraints) {
		if (getSetForKey(key) == null)
			makeSet(key);
		
		Set res = getSetForKey(key); 
		res = filter(key, res, constraints);
		return res;
	}
	
	/* TODO : marquer deleted */
	public void clear() {
		asso = new TreeMap();
		sets = new Set[NB_KEYS];
	}
	
	private void removeChild(BasicMonitoredObject parent, String childName) {
		Set res = getWritableAsso(parent, parent.getKey() + 1);
		res.remove(childName);
		if (res.isEmpty())
			removeItem(parent);
	}
	
	public void removeItem(BasicMonitoredObject value) {
		int key = value.getKey();
		String name = value.getFullName();
		
		if (getSetForKey(key) == null || !getSetForKey(key).remove(name))
			return;
		
		if  (isSpecialKey(key))
			/* If we had a reference count : associatedNode.ref-- */
			return;
		
		Set desc = getValues(value, key + 1, null);
		Iterator iter = desc.iterator();
		while (iter.hasNext()) {
			BasicMonitoredObject childValue = (BasicMonitoredObject) iter.next();
			removeItem(childValue);
		}
		
		Set parent = getValues(value, key - 1, null);
		iter = parent.iterator();
		while (iter.hasNext()) {
			BasicMonitoredObject parentValue = (BasicMonitoredObject) iter.next();
			removeChild(parentValue, name);
		}
	}
}
