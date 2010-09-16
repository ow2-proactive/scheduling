package org.ow2.proactive_grid_cloud_portal;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.ow2.proactive.scheduler.common.Scheduler;


public class SchedulerSessionMapper {

    private Map<String, Scheduler> sessions;
    private static SchedulerSessionMapper sessionMapper;
    private long currentSessionid = 0l;

    private SchedulerSessionMapper() {
        sessions = Collections.synchronizedMap(new HashMap<String, Scheduler>());
    }

    public 
    static synchronized SchedulerSessionMapper getInstance() {
        if (sessionMapper == null) {
            sessionMapper = new SchedulerSessionMapper();
        }
        return sessionMapper;
    }

    public long add(Scheduler s) {
        long id = ++currentSessionid;
        sessions.put("" + id, s);
        return id;
    }

    public Map<String, Scheduler> getSessionsMap() {
        return sessions;
    }
}
