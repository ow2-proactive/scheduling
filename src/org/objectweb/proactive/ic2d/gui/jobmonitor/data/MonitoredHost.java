package org.objectweb.proactive.ic2d.gui.jobmonitor.data;

import java.util.Map;

public class MonitoredHost extends BasicMonitoredObject {
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


