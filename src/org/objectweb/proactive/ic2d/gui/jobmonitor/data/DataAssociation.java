package org.objectweb.proactive.ic2d.gui.jobmonitor.data;

import java.util.*;

import org.objectweb.proactive.ic2d.gui.jobmonitor.JobMonitorConstants;

class AssoKey implements Comparable {
	private int lkey;
	private int rkey;
	private String name;
	
	public AssoKey(int lkey, int rkey, String name) {
		this.lkey = lkey;
		this.rkey = rkey;
		this.name = name;
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
	
	private void addToSet(int key, String value) {
		int index = KEY2INDEX[key];
		if (getSetForKey(key) == null)
			makeSet(key);
		
		getSetForKey(key).add(value);
	}
	
	private void addAsso(int fromKey, String fromValue, int toKey, String toValue) {
		addToSet(toKey, toValue);
		
		AssoKey key = new AssoKey(fromKey, toKey, fromValue);
		Object res = asso.get(key);
		if (res == null) {
			res = new TreeSet();
			asso.put(key, res);
		}
		
		Set l = (Set) res;
		l.add(toValue);
	}

	public void addChild(int fromKey, String fromValue, int toKey, String toValue) {
		addAsso(fromKey, fromValue, toKey, toValue);
		addAsso(toKey, toValue, fromKey, fromValue);
	}

	private boolean isSpecialKey(int key) {
		return key == VN || key == JOB;
	}
	
	private Set getWritableAsso(int from, String name, int to) {
		AssoKey key = new AssoKey(from, to, name);
		Object res = asso.get(key);
		if (res == null) {
			res = new TreeSet();
			asso.put(key, res);
		}
		
		return (Set) res;
	}
	
	private boolean isValid(int key, String name, Map constraints) {
		Iterator iterKeys = constraints.keySet().iterator();
		while (iterKeys.hasNext()) {
			Integer testKey = (Integer) iterKeys.next();
			String testValue = (String) constraints.get(testKey);
			Set test = getValues(key, name, testKey.intValue(), null);
			if (!test.contains(testValue))
				return false;
		}

		return true;
	}
	
	private Set filter(int key, Set set, Map constraints) {
		if (constraints == null || constraints.isEmpty() || set.isEmpty())
			return set;
		
		Set res = new TreeSet();
		Iterator iter = set.iterator();
		while (iter.hasNext()) {
			String name = (String) iter.next();
			if (isValid(key, name, constraints))
				res.add(name);
		}
		
		return res;
	}
	
	private Set getAsso(int from, String name, int to, Map constraints) {
		Set res = getWritableAsso(from, name, to);
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
	
	private Set handleSpecialPath(int from, String name, int to, Map constraints) {		
		if (isPermutation(from, to, JOB, VN))
			return getAsso(from, name, to, constraints);
			
		if (isPermutation(from, to, JOB, HOST)) {
			int[] steps = {JVM, NODE, VN, AO};
			Set res = new TreeSet();
			for (int i = 0; i < steps.length; i++)
				res.addAll(getValues(from, name, to, constraints, steps[i]));
			return res;
		}
		
		if ((from == VN || to == VN) && !isPermutation(from, to, JOB, VN) && !isPermutation(from, to, NODE, VN))
			return getValues(from, name, to, constraints, NODE);

		return getAsso(from, name, to, constraints);
	}
	
	private Set getValues(int from, String name, int to, Map constraints, int step) {
		Set stepSet = getValues(from, name, step, constraints);
		if (stepSet.isEmpty())
			return new TreeSet();
		
		Iterator iter = stepSet.iterator();
		Set res = new TreeSet();
		while (iter.hasNext()) {
			String stepName = (String) iter.next();
			Set temp = getValues(step, stepName, to, constraints);
			res.addAll(temp);
		}
		
		return res;
	}
	
	/*
	 * Exemple : getValues(VN, "myVN", AO) ==> {"Object1", "Object2"}
	 */
	public Set getValues(int from, String name, int to, Map constraints) {
		if (to == from) {
			Set res = new TreeSet();
			res.add(name);
			return res;
		}

		if (from == NO_KEY)
			return list(to, constraints);
		
		if (isSpecialKey(from) || isSpecialKey(to))
			return handleSpecialPath(from, name, to, constraints);
		
		if (to == from + 1 || to == from - 1)
			return getAsso(from, name, to, constraints);

		int inc = (to > from) ? 1 : -1;
		int step = to - inc;
		return getValues(from, name, to, constraints, step);
	}
	
	private Set list(int key, Map constraints) {
		if (getSetForKey(key) == null)
			makeSet(key);
		
		Set res = getSetForKey(key); 
		res = filter(key, res, constraints);
		return res;
	}
	
	public void clear() {
		asso = new TreeMap();
		sets = new Set[NB_KEYS];
	}
	
	private void removeChild(int parentKey, String parentName, String childName) {
		Set res = getWritableAsso(parentKey, parentName, parentKey + 1);
		res.remove(childName);
		if (res.isEmpty())
			removeItem(parentKey, parentName);
	}
	
	public void removeItem(int key, String name) {
		if (getSetForKey(key) == null || !getSetForKey(key).remove(name))
			return;
		
		if  (isSpecialKey(key))
			/* If we had a reference count : associatedNode.ref-- */
			return;
		
		Set desc = getValues(key, name, key + 1, null);
		Iterator iter = desc.iterator();
		while (iter.hasNext()) {
			String childName = (String) iter.next();
			removeItem(key + 1, childName);
		}
		
		Set parent = getValues(key, name, key - 1, null);
		iter = parent.iterator();
		while (iter.hasNext()) {
			String parentName = (String) iter.next();
			removeChild(key - 1, parentName, name);
		}
	}
	
	public void dumpMap() {
		Iterator keys = asso.keySet().iterator();
		while (keys.hasNext()) {
			AssoKey key = (AssoKey) keys.next();
			System.out.println(key);
			Iterator values = ((Set) asso.get(key)).iterator();
			while (values.hasNext()) {
				System.out.println(" " + values.next());
			}
		}
	}
}
