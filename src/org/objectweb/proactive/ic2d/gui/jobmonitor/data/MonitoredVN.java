package org.objectweb.proactive.ic2d.gui.jobmonitor.data;

import java.util.Map;

public class MonitoredVN extends BasicMonitoredObject {
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


