package org.ow2.proactive_grid_cloud_portal;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.objectweb.proactive.core.security.crypto.Session;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.scheduler.common.Scheduler;


public class RMSessionMapper {

    private Map<String, ResourceManager> sessions;
    private static RMSessionMapper sessionMapper;
    private long currentSessionid = 0l;

    private RMSessionMapper() {
        sessions = Collections.synchronizedMap(new HashMap<String, ResourceManager>());
    }

    public static synchronized RMSessionMapper getInstance() {
        if (sessionMapper == null) {
            sessionMapper = new RMSessionMapper();
        }
        return sessionMapper;
    }

    public long add(ResourceManager rm) {
        long id = ++currentSessionid;
        sessions.put("" + id, rm);
        return id;
    }

    public Map<String, ResourceManager> getSessionsMap() {
        return sessions;
    }
}
