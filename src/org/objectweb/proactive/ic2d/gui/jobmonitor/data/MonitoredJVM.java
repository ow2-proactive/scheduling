package org.objectweb.proactive.ic2d.gui.jobmonitor.data;

import java.util.HashMap;
import java.util.Map;


public class MonitoredJVM extends BasicMonitoredObject {
    static protected int lastID = 0;
    static protected Map prettyNames = new HashMap();
    private int depth;
    private int port;

    protected int incLastID() {
        return ++lastID;
    }

    protected Map getPrettyNames() {
        return prettyNames;
    }

    public MonitoredJVM(String url, int port, int depth) {
        super(JVM, "JVM",  url);
        this.depth = depth;
        this.port = port;
    }

    public void copyInto(BasicMonitoredObject o) {
        super.copyInto(o);
        MonitoredJVM jvmObject = (MonitoredJVM) o;
        jvmObject.depth = depth;
        jvmObject.port = port;
    }

    public int getDepth() {
        return depth;
    }
    
    public int getPort() {
    	return port;
    }
}
