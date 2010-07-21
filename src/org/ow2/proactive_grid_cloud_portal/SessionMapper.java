package org.ow2.proactive_grid_cloud_portal;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.objectweb.proactive.core.security.crypto.Session;
import org.ow2.proactive.scheduler.common.Scheduler;

public class SessionMapper {

    private Map<String,Scheduler> sessions;
    private static SessionMapper sessionMapper;
    private long currentSessionid = 0l;
    private SessionMapper() {
        sessions = Collections.synchronizedMap(new HashMap<String,Scheduler>()); 
    }
    
    public static synchronized SessionMapper getInstance() {
        if (sessionMapper == null) {
            sessionMapper = new SessionMapper();
        }
        return sessionMapper;
    }
    
    public long add (Scheduler s) {
        long id = ++currentSessionid;
        sessions.put(""+id, s);
        return  id;
    }
    
    public Map<String,Scheduler> getSessionsMap() {
        return sessions;
    }
}
