package org.objectweb.proactive.ic2d.gui.jobmonitor.data;

import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.objectweb.proactive.core.util.UrlBuilder;


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

    private void extractPort() {
    	Pattern p = Pattern.compile("(.*//)?([^:]+):?([0-9]*)/(.+)");
        Matcher m = p.matcher(fullname);
        if (!m.matches()) {
            port = Registry.REGISTRY_PORT;
        }

        String portStr = m.group(3);
        try {
        	port = Integer.parseInt(portStr);
        } catch (Exception e) {
        	port = Registry.REGISTRY_PORT;
        }
    }
    
    public MonitoredJVM(String url, int depth) {
        super(JVM, "JVM",  url);
        this.depth = depth;
        //extractPort();
        this.port = UrlBuilder.getPortFromUrl(url);
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
