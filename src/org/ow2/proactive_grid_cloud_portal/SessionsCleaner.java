package org.ow2.proactive_grid_cloud_portal;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.Sleeper;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.util.SchedulerLoggers;

/**
 * 
 * @author acontes
 *
 */
public class SessionsCleaner implements Runnable{

    private Logger logger = ProActiveLogger.getLogger(SchedulerLoggers.PREFIX + ".rest.sessioncleaner");

    
    private volatile boolean stop = false;
    private SchedulerSessionMapper ssm;
    
    public SessionsCleaner(SchedulerSessionMapper ssm) {
        this.ssm = ssm;
        
    }
    
    public void run() {
        while (!stop) {
            Map<String, Scheduler> sessionMap = ssm.getSessionsMap();
            logger.info("start cleaning session");
            synchronized (sessionMap) {
                Set<Entry<String, Scheduler>> entrySet = sessionMap.entrySet();
                Iterator<Entry<String, Scheduler>> it = entrySet.iterator();
                while (it.hasNext()) {
                    Entry<String, Scheduler> entry = it.next();
                    Scheduler s = entry.getValue();
                    try {
                        
                    // isConnected does not reset the lease of the stub
                    boolean connected = s.isConnected();
                    
                    // if not connected, removing it from the session map
                    // to clean 
                    if (!connected) {
                        logger.info("cleaning session " + entry.getKey() + ", not connected");
                        sessionMap.remove(s);
                    }
                    } catch (Throwable t) {
                        logger.info("cleaning session " + entry.getKey() + ", connection issue");
                        sessionMap.remove(s);
                    }
                    
                }
                }
            // clean every 5 minutes
            new Sleeper(5*60*1000).sleep();
            }
            logger.info(Thread.currentThread().getName() + " terminated");   
        }
        

    public void stop() {
        stop = true;
        
    }

    public void start() {
        this.stop = false;
    }

    public boolean isStopped() {
        return this.stop;
    }
}
