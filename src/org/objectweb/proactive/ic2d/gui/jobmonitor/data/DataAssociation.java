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
}

/*
 * Job -*-> VN -*-> Host -*-> JVM -*-> Node -*-> AO
 * 
 * But the world is not a simple tree.
 */

public class DataAssociation implements JobMonitorConstants {
	private Map asso;
	private Set[] sets;
	private int[][] specialCases = {
			{JOB, HOST},
			{JOB, JVM},
			{VN, JVM}
	};
	
	public DataAssociation() {
		clear();
	}
	
	private void addAsso(int fromKey, String fromValue, int toKey, String toValue) {
		if (sets[toKey] == null)
			sets[toKey] = new TreeSet();
		sets[toKey].add(toValue);
		
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
	
	/* Exemple : addChild(VN, "myVN", "camel.inria.fr") */
	public void addChild(int key, String lvalue, String rvalue) {
		addChild(key, lvalue, key + 1, rvalue);
	}

	private boolean isSpecialCase(int fromKey, int toKey) {
		if (toKey < fromKey) {
			int tmp = toKey;
			toKey = fromKey;
			fromKey = tmp;
		}
		
		for (int i = 0; i < specialCases.length; i++)
			if (fromKey == specialCases[i][0] && toKey == specialCases[i][1])
				return true;

		return false;
	} 
	
	private Set getAsso(int from, String name, int to) {
		AssoKey key = new AssoKey(from, to, name);
		Object res = asso.get(key);
		if (res == null)
			return new TreeSet();
		
		return (Set) res;
	}
	
	/*
	 * Exemple : getValues(VN, "myVN", KEY_AO) ==> {"Object1", "Object2"}
	 */
	public Set getValues(int from, String name, int to) {
		if (to == from) {
			Set res = new TreeSet();
			res.add(name);
			return res;
		}

		if (from == NO_KEY)
			return list(to);
		
		if (to == from + 1 || to == from - 1 || isSpecialCase(from, to))
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
		if (sets[key] == null)
			sets[key] = new TreeSet();
		
		return sets[key];
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
		if (sets[key] == null || !sets[key].remove(name))
			return;
		
		if (key != LAST_KEY) {
			Set desc = getValues(key, name, key + 1);
			Iterator iter = desc.iterator();
			while (iter.hasNext()) {
				String childName = (String) iter.next();
				removeItem(key + 1, childName);
			}
		}
		
		if (key != FIRST_KEY) {
			Set parent = getValues(key, name, key - 1);
			Iterator iter = parent.iterator();
			while (iter.hasNext()) {
				String parentName = (String) iter.next();
				removeChild(key - 1, parentName, name);
			}
		}
	}
}
