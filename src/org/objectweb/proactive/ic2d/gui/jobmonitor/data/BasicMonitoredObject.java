package org.objectweb.proactive.ic2d.gui.jobmonitor.data;

import org.objectweb.proactive.ic2d.gui.jobmonitor.JobMonitorConstants;

import java.util.GregorianCalendar;
import java.util.Map;


public class BasicMonitoredObject implements JobMonitorConstants,
    Comparable {
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

    private BasicMonitoredObject(int key) {
    	this(key, null);
    }
    
    public static BasicMonitoredObject createRoot(int key) {
    	return new BasicMonitoredObject(key);
    }

    public boolean equals(Object value) {
        return compareTo(value) == 0;
    }

    public int compareTo(Object o) {
        BasicMonitoredObject mo = (BasicMonitoredObject) o;

        if (key != mo.key) {
            return key - mo.key;
        }

        return fullname.compareTo(mo.fullname);
    }

    public boolean isDeleted() {
        return deletedSince != null;
    }

    public void setDeleted(boolean deleted) {
        if (deleted) {
            if (deletedSince == null) {
                deletedSince = new GregorianCalendar();
            }
        } else {
            deletedSince = null;
        }
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
