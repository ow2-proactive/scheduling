
package org.objectweb.proactive.ic2d.gui.jobmonitor.data;

import java.util.GregorianCalendar;
import java.util.Map;

import org.objectweb.proactive.ic2d.gui.jobmonitor.JobMonitorConstants;

public abstract class BasicMonitoredObject implements JobMonitorConstants, Comparable {
	protected int key;
	protected String prettyName;
	protected String fullname;
	private GregorianCalendar deletedSince;
	
	protected BasicMonitoredObject(int key, String fullname) {
		this.key = key;
		this.fullname = fullname;
		this.prettyName = fullname;
		this.deletedSince = null;
	}
	
	public void copyInto(BasicMonitoredObject o) {
		o.key = key;
		o.prettyName = prettyName;
		o.fullname = fullname;
		o.deletedSince = deletedSince;
	}
	
	public static BasicMonitoredObject create(int key, String fullname) {
		switch (key) {
		case JOB:
			return new MonitoredJob(fullname);
		
		case VN:
			return new MonitoredVN(fullname);
			
		case HOST:
			return new MonitoredHost(fullname);
			
		case JVM:
			return new MonitoredJVM(fullname);
			
		case NODE:
			return new MonitoredNode(fullname);
			
		case AO:
			return new MonitoredAO(fullname);

		default:
			return null;
		}
	}
	
	public boolean equals(Object value) {
		return compareTo(value) == 0;
	}
	
	public int compareTo(Object o) {
		BasicMonitoredObject mo = (BasicMonitoredObject) o;

		if (key != mo.key)
			return key - mo.key;

		return fullname.compareTo(mo.fullname);
	}
	
	public boolean isDeleted() {
		return deletedSince != null;
	}
	
	public void setDeleted(boolean deleted) {
		if (deleted) {
			if (deletedSince == null)
				deletedSince = new GregorianCalendar();
		} else
			deletedSince = null;
	}
	
	public GregorianCalendar getDeletedSince() {
		return deletedSince;
	}
	
	public String getFullName() {
		return fullname;
	}

	public String getPrettyName() {
		return prettyName;
	}
	
	public int getKey() {
		return key;
	}
	
	public void setKey(int key) {
		this.key = key;
	}

	public boolean isRoot() {
		return fullname == null;
	}
}

class MonitoredJob extends BasicMonitoredObject {
	static protected int lastID = 0;
	static protected Map prettyNames;
	
	protected int incLastID() {
		return ++lastID;
	}
	
	protected Map getPrettyNames() {
		return prettyNames;
	}
	
	public MonitoredJob(String fullname) {
		super(JOB, fullname);
	}
}

class MonitoredVN extends BasicMonitoredObject {
	static protected int lastID = 0;
	static protected Map prettyNames;
	
	protected int incLastID() {
		return ++lastID;
	}
	
	protected Map getPrettyNames() {
		return prettyNames;
	}
	
	public MonitoredVN(String fullname) {
		super(VN, fullname);
	}
}

class MonitoredHost extends BasicMonitoredObject {
	static protected int lastID = 0;
	static protected Map prettyNames;
	
	protected int incLastID() {
		return ++lastID;
	}
	
	protected Map getPrettyNames() {
		return prettyNames;
	}
	
	public MonitoredHost(String fullname) {
		super(HOST, fullname);
	}
}

class MonitoredJVM extends BasicMonitoredObject {
	static protected int lastID = 0;
	static protected Map prettyNames;
	
	protected int incLastID() {
		return ++lastID;
	}
	
	protected Map getPrettyNames() {
		return prettyNames;
	}
	
	public MonitoredJVM(String fullname) {
		super(JVM, fullname);
	}
}

class MonitoredNode extends BasicMonitoredObject {
	static protected int lastID = 0;
	static protected Map prettyNames;
	
	protected int incLastID() {
		return ++lastID;
	}
	
	protected Map getPrettyNames() {
		return prettyNames;
	}
	
	public MonitoredNode(String fullname) {
		super(NODE, fullname);
	}
}

class MonitoredAO extends BasicMonitoredObject {
	static protected int lastID = 0;
	static protected Map prettyNames;
	
	protected int incLastID() {
		return ++lastID;
	}
	
	protected Map getPrettyNames() {
		return prettyNames;
	}
	
	public MonitoredAO(String fullname) {
		super(AO, fullname);
	}
}
