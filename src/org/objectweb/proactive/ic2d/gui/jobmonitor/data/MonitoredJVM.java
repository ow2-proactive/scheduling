package org.objectweb.proactive.ic2d.gui.jobmonitor.data;

import java.util.Map;


public class MonitoredJVM extends BasicMonitoredObject {
    static protected int lastID = 0;
    static protected Map prettyNames;
    private int depth;

    protected int incLastID() {
        return ++lastID;
    }

    protected Map getPrettyNames() {
        return prettyNames;
    }

    public MonitoredJVM(String host, String name, int depth) {
        super(JVM, host + "/" + name);
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
