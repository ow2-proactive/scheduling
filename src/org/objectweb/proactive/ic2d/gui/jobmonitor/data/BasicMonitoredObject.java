package org.objectweb.proactive.ic2d.gui.jobmonitor.data;

import org.objectweb.proactive.ic2d.gui.jobmonitor.JobMonitorConstants;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


public class BasicMonitoredObject implements JobMonitorConstants, Comparable {
    protected int key;
    protected String prettyName;
    protected String fullname;
    private GregorianCalendar deletedSince;
    private boolean reallyDeleted;
    private Set references;

    protected BasicMonitoredObject(int key, String fullname) {
        this.key = key;
        this.fullname = fullname;
        this.prettyName = fullname;
        this.deletedSince = null;
        this.reallyDeleted = false;
        this.references = new HashSet();
    }

    public void addReference(MonitoredObjectSet set) {
        references.add(set);
    }

    public void removeReference(MonitoredObjectSet set) {
        references.remove(set);
    }

    public List removeInReferences() {
        List copy = new ArrayList();
        copy.addAll(references);
        Iterator iter = copy.iterator();

        List clearedSets = new ArrayList();
        while (iter.hasNext()) {
            MonitoredObjectSet set = (MonitoredObjectSet) iter.next();
            set.remove(this);
            if (set.isEmpty()) {
                clearedSets.add(set);
            }
        }

        return clearedSets;
    }

    public void copyInto(BasicMonitoredObject o) {
        o.key = key;
        o.prettyName = prettyName;
        o.fullname = fullname;
        o.deletedSince = deletedSince;
        o.references.addAll(references);
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
        return reallyDeleted;
    }

    public void setDeleted(boolean deleted) {
        reallyDeleted = false;
        if (deleted) {
            if (deletedSince == null) {
                deletedSince = new GregorianCalendar();
            }
        } else {
            deletedSince = null;
        }
    }

    public void updateReallyDeleted() {
        reallyDeleted = deletedSince != null;
    }

    private GregorianCalendar getDeletedSince() {
        return reallyDeleted ? deletedSince : null;
    }

    public String getDeletedTime() {
        Calendar d1 = getDeletedSince();
        if (d1 == null) {
            return null;
        }

        Calendar d2 = new GregorianCalendar();

        long d1Sec = d1.getTimeInMillis() / 1000;
        long d2Sec = d2.getTimeInMillis() / 1000;

        long diff = d2Sec - d1Sec;
        if (diff < 120) {
            return diff + " seconds";
        }

        diff /= 60;
        if (diff < 120) {
            return diff + " minutes";
        }

        diff /= 60;
        if (diff < 120) {
            return diff + " hours";
        }

        diff /= 24;
        return diff + " days";
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
