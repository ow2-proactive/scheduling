package org.ow2.proactive_grid_cloud_portal;

import java.io.File;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.Logger;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.utils.Sleeper;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.scheduler.common.SchedulerState;
import org.ow2.proactive.scheduler.common.util.CachingSchedulerProxyUserInterface;
import org.ow2.proactive.scheduler.common.util.SchedulerLoggers;

public class SchedulerStateCaching {
    private static Logger logger = ProActiveLogger.getLogger(SchedulerLoggers.PREFIX + ".rest.caching");
    
    private static CachingSchedulerProxyUserInterface scheduler;
    private static SchedulerState localState;
    private static long schedulerRevision;
    private static int refreshInterval;
    private static boolean kill = false;
    private static Thread t;
    
    
    public static CachingSchedulerProxyUserInterface getScheduler() {
        return scheduler;
    }

    public static void setScheduler(CachingSchedulerProxyUserInterface scheduler) {
        SchedulerStateCaching.scheduler = scheduler;
    } 
    
    public static void init() {
        
        refreshInterval = Integer.parseInt(PortalConfiguration.getProperties().getProperty(PortalConfiguration.scheduler_cache_refreshrate));
        String url = PortalConfiguration.getProperties().getProperty(PortalConfiguration.scheduler_url);
        String cred_path = PortalConfiguration.getProperties().getProperty(PortalConfiguration.scheduler_cache_credential);
        
        while (scheduler == null ) {
        try {
            
            if (scheduler == null) {
            
            scheduler = PAActiveObject.newActive(
                         CachingSchedulerProxyUserInterface.class, new Object[] {});

          
            // check is we use a credential file 
            
           
            File f = new File(cred_path);
            
            if (f.exists()) {
                Credentials credential = Credentials.getCredentials(cred_path);
                scheduler.init(url, credential);
            } else {
                String login =  PortalConfiguration.getProperties().getProperty(PortalConfiguration.scheduler_cache_login);
                String password =  PortalConfiguration.getProperties().getProperty(PortalConfiguration.scheduler_cache_password);           
                scheduler.init(url, login, password);
            }
            
            
            }
           } catch (Exception e) {
               logger.warn("no scheduler found on " + url + "retrying in 8 seconds", e);
               scheduler = null;
               new Sleeper(8 * 1000).sleep();
               continue;
           }
        }
        
    }

    
    public static void start() {
        t = new Thread(new Runnable() {
            public void run() {
               while (!kill) {
  
                long currentSchedulerStateRevision = scheduler.getSchedulerStateRevision();
                
                if (currentSchedulerStateRevision != schedulerRevision) {
                    Entry<AtomicLong, SchedulerState> tmp = scheduler.getRevisionVersionAndSchedulerState().entrySet().iterator().next();
                    localState = tmp.getValue();
                    schedulerRevision = tmp.getKey().longValue();
                    logger.debug("updated scheduler state revision at " + schedulerRevision);
                }

                new Sleeper(refreshInterval).sleep();
               }
            }
        },"State Updater Thread");
        
        t.start();
    }
    
    public static SchedulerState getLocalState() {
        return localState;
    }

    public static void setLocalState(SchedulerState localState) {
        SchedulerStateCaching.localState = localState;
    }

    public static long getSchedulerRevision() {
        return schedulerRevision;
    }

    public static void setSchedulerRevision(long schedulerRevision) {
        SchedulerStateCaching.schedulerRevision = schedulerRevision;
    }

    public static int getRefreshInterval() {
        return refreshInterval;
    }

    public static void setRefreshInterval(int refreshInterval) {
        SchedulerStateCaching.refreshInterval = refreshInterval;
    }

    public static boolean isKill() {
        return kill;
    }

    public static void setKill(boolean kill) {
        SchedulerStateCaching.kill = kill;
    }
    
}
