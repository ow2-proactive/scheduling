package org.objectweb.proactive.ic2d.gui.jobmonitor.data;

import java.util.HashMap;
import java.util.Map;


public class MonitoredJVM extends BasicMonitoredObject {
    static protected int lastID = 0;
    static protected Map prettyNames = new HashMap();
    private int depth;

    protected int incLastID() {
        return ++lastID;
    }

    protected Map getPrettyNames() {
        return prettyNames;
    }

    public MonitoredJVM(String url, int depth) {
        super(JVM, "JVM",  url);
        this.depth = depth;
    }

    public void copyInto(BasicMonitoredObject o) {
        super.copyInto(o);
        MonitoredJVM jvmObject = (MonitoredJVM) o;
        jvmObject.depth = depth;
    }

    public int getDepth() {
        return depth;
    }
}
