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
 *         Host        VN
 *           |         |
 *           *         *
 *           |         |
 *           v         v
 * Job -*-> JVM -*-> Node -*-> AO
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

	/* Example : addChild(HOST, "camel.inria.fr", "PA_JVM_0123456798") */
	public void addChild(int key, String lvalue, String rvalue) {
		if (getSpecialPath(key) != key)
			throw new RuntimeException("This key does not have any child");
		else
			addChild(key, lvalue, key + 1, rvalue);
	}

	private int getSpecialPath(int key) {
		for (int i = 0; i < SPECIAL_PATHS.length; i++)
			if (key == SPECIAL_PATHS[i][0])
				return SPECIAL_PATHS[i][1];

		return key;
	} 
	
	private Set getAsso(int from, String name, int to) {
		AssoKey key = new AssoKey(from, to, name);
		Object res = asso.get(key);
		if (res == null) {
			res = new TreeSet();
			asso.put(key, res);
		}
		
		return (Set) res;
	}
	
	private Set handleSpecialPath(int from, String name, int to) {
		int fromStep = getSpecialPath(from);
		int toStep = getSpecialPath(to);
		
		if (to == fromStep || from == toStep)
			return getAsso(from, name, to);
		
		int step = (fromStep != from) ? fromStep : toStep;

		Set stepValues = getValues(from, name, step);
		Set res = new TreeSet();
		Iterator iter = stepValues.iterator();
		while (iter.hasNext()) {
			String stepName = (String) iter.next();
			Set temp = getValues(step, stepName, to);
			res.addAll(temp);
		}
		return res;
	}
	
	/*
	 * Exemple : getValues(VN, "myVN", AO) ==> {"Object1", "Object2"}
	 */
	public Set getValues(int from, String name, int to) {
		if (to == from) {
			Set res = new TreeSet();
			res.add(name);
			return res;
		}

		if (from == NO_KEY)
			return list(to);
		
		if (getSpecialPath(from) != from || getSpecialPath(to) != to)
			return handleSpecialPath(from, name, to);
		
		if (to == from + 1 || to == from - 1)
			return getAsso(from, name, to);

		int inc = (to > from) ? 1 : -1;
		Set step = getValues(from, name, to - inc);
		if (step.isEmpty())
			return step;
		
		Iterator iter = step.iterator();
		Set res = new TreeSet();
		while (iter.hasNext()) {
			String stepName = (String) iter.next();
			Set temp = getValues(to - inc, stepName, to);
			res.addAll(temp);
		}
		
		return res;
	}
	
	private Set list(int key) {
		if (getSetForKey(key) == null)
			makeSet(key);
		
		return getSetForKey(key);
	}
	
	public void clear() {
		asso = new TreeMap();
		sets = new Set[NB_KEYS];
	}
	
	private void removeChild(int parentKey, String parentName, String childName) {
		Set res = getValues(parentKey, parentName, parentKey + 1);
		res.remove(childName);
		if (res.isEmpty())
			removeItem(parentKey, parentName);
	}
	
	public void removeItem(int key, String name) {
		if (getSetForKey(key) == null || !getSetForKey(key).remove(name))
			return;
		
		if  (getSpecialPath(key) != key)
			/* If we had a reference count : associatedNode.ref-- */
			return;
		
		Set desc = getValues(key, name, key + 1);
		Iterator iter = desc.iterator();
		while (iter.hasNext()) {
			String childName = (String) iter.next();
			removeItem(key + 1, childName);
		}
		
		Set parent = getValues(key, name, key - 1);
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
