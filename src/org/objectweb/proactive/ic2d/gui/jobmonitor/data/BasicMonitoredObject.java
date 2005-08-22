package org.objectweb.proactive.ic2d.gui.jobmonitor.data;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.objectweb.proactive.ic2d.gui.jobmonitor.JobMonitorConstants;


/*
 * BasicMonitoredObject is the base class for objects shown in the job monitor.
 * There is some boilerplate code that should be present in every derived classes.
 * The prettyNames and lastID stuff is because each element can have a prettyName which is
 * composed of a prefix and a number unique to the derived class.
 *
 * A typical derived class looks like this:
 *
 * package org.objectweb.proactive.ic2d.gui.jobmonitor.data;
 *
 * import ...;
 *
 * public class MonitoredFoo extends BasicMonitoredObject {
 *     static protected int lastID = 0;
 *     static protected Map prettyNames = new HashMap();
 *     private type someOtherAttributes;
 *
 *     protected int incLastID() {
 *         return ++lastID;
 *     }
 *
 *     protected Map getPrettyNames() {
 *         return prettyNames;
 *     }
 *
 *     public MonitoredFoo(constructor parameters) {
 *         super(FOO, prefix, fullname); // FOO must be a registered key in JobMonitorConstants
 *         other initializations ...
 *     }
 *
 *     // This method must be overriden is attributes are added to the class
 *     public void copyInto(BasicMonitoredObject o) {
 *         super.copyInto(o);
 *         MonitoredFoo fooObject = (MonitoredFoo) o;
 *         fooObject.attributes = attributes ...;
 *     }
 * }
 *
 */
public class BasicMonitoredObject implements JobMonitorConstants, Comparable {

    /* HOST or JVM or NODE or AO or JOB or VN */
    protected int key;

    /* Displayed name */
    protected String prettyName;

    /* Unique name, shown in the panel on the right */
    protected String fullname;

    /* Timestamp to keep track of unjoinable elements */
    private GregorianCalendar deletedSince;

    /*
     *  During a traversal, every element are deleted, this is boolean is
     * to keep track of really deleted elements
     */
    private boolean reallyDeleted;

    /* Set of MonitoredObjectSet that contain this object */
    private Set references;

    protected BasicMonitoredObject(int key, String fullname) {
        this(key, fullname, fullname);
    }

    protected BasicMonitoredObject(int key, String prettyPrefix, String fullname) {
        this.key = key;
        this.fullname = fullname;
        this.deletedSince = null;
        this.reallyDeleted = false;
        this.references = new HashSet();
        computePrettyName(prettyPrefix);
    }

    protected int incLastID() {
        return 0;
    }

    protected Map getPrettyNames() {
        return null;
    }

    private void computePrettyName(String prefix) {
        Map prettyNames = getPrettyNames();
        if (prettyNames == null) {

            /* Special case for the root element */
            prettyName = null;
            return;
        }

        if (prefix == null) {
            prettyName = fullname;
            return;
        }

        prettyName = (String) prettyNames.get(fullname);
        if (prettyName == null) {
            int id = incLastID();
            if (getKey() == HOST) {
                prettyName = prefix;
            } else {
                prettyName = prefix + "#" + id;
            }
            prettyNames.put(fullname, prettyName);
        }
    }

    /* More than a reference count, we keep track of the owner too */
    public void addReference(MonitoredObjectSet set) {
        references.add(set);
    }

    public void removeReference(MonitoredObjectSet set) {
        references.remove(set);
    }

    /* When an object is removed we also have to remove it in the sets that contain it */
    public List removeInReferences() {

        /*
         * The list is copied because, as we are removing the object in sets, the
         * list is updated, which would cause ConcurrentModificationException.
         */
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

        /*
         * We return the sets we have emptied to
         * delete the parents that have no more children.
         */
        return clearedSets;
    }

    /*
     * We want only one instance of every element, when we traverse the network
     * we rebuild elements but we want to use the old instances because they
     * are already referenced in various data structures.
     */
    public void copyInto(BasicMonitoredObject o) {
        o.key = key;
        o.prettyName = prettyName;
        o.fullname = fullname;
        o.deletedSince = deletedSince;
        o.references.addAll(references);
    }

    /* Constructor for the root element */
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
