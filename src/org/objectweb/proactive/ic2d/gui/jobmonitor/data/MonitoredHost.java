package org.objectweb.proactive.ic2d.gui.jobmonitor.data;

import java.util.HashMap;
import java.util.Map;


public class MonitoredHost extends BasicMonitoredObject {
    static protected int lastID = 0;
    static protected Map prettyNames = new HashMap();
    private int port;
    
    protected int incLastID() {
        return ++lastID;
    }

    protected Map getPrettyNames() {
        return prettyNames;
    }

    public void copyInto(BasicMonitoredObject o) {
        super.copyInto(o);
        MonitoredHost hostObject = (MonitoredHost) o;
        hostObject.port = port;
    }
    
    public MonitoredHost(String hostname, int port) {
        super(HOST, hostname, hostname + ":" + port);
        this.port = port;
    }
    
    public int getPort() {
    	return port;
    }
 }
