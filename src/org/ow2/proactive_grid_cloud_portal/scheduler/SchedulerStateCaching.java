/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.ow2.proactive_grid_cloud_portal.scheduler;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.Logger;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.utils.Sleeper;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.scheduler.common.SchedulerState;
import org.ow2.proactive.scheduler.common.exception.NotConnectedException;
import org.ow2.proactive.scheduler.common.exception.PermissionException;
import org.ow2.proactive.scheduler.common.util.CachingSchedulerProxyUserInterface;
import org.ow2.proactive.scheduler.common.util.SchedulerLoggers;
import org.ow2.proactive_grid_cloud_portal.webapp.PortalConfiguration;


/**
 * This class keeps a cache of the scheduler state and periodically refresh it.
 * This prevents to directly call the active object CachingSchedulerProxyUserInterface and thus
 * to have the scheduler state being copied each time.
 * the refresh rate can be configured using @see {org.ow2.proactive_grid_cloud_portal.PortalConfiguration.scheduler_cache_refreshrate}
 * 
 *
 */
public class SchedulerStateCaching {
    private static Logger logger = ProActiveLogger.getLogger(SchedulerLoggers.PREFIX + ".rest.caching");

    private static MyCachingSchedulerProxyUserInterface scheduler;
    private static SchedulerState localState;
    private static LightSchedulerState lightLocalState;
    private static long schedulerRevision;
    private static int refreshInterval;
    private static volatile boolean kill = false;

    /*
     * thread that updates the scheduler state
     */
    private static Thread cachedSchedulerStateThreadUpdater;

    /*
     * thread that renews the lease of the object that caches the scheduler state
     * as by default it does not perform any action.
     */
    private static Thread leaseRenewerThreadUpdater;

    protected static HashMap<AtomicLong, LightSchedulerState> revisionAndLightSchedulerState;

    private static int leaseRenewRate;

    public static CachingSchedulerProxyUserInterface getScheduler() {
        return scheduler;
    }

    public static void setScheduler(MyCachingSchedulerProxyUserInterface scheduler) {
        SchedulerStateCaching.scheduler = scheduler;
    }

    public synchronized static void init() {
        // here we need a thread to free the calling thread
        // i.e. not locking it when the scheduler is unavailable

        new Thread(new Runnable() {
            public void run() {
                revisionAndLightSchedulerState = new HashMap<AtomicLong, LightSchedulerState>();
                revisionAndLightSchedulerState.put(new AtomicLong(-1), new LightSchedulerState());

                init_();
                start_();
            }
        }).start();
    }

    private synchronized static void init_() {
        leaseRenewRate = Integer.parseInt(PortalConfiguration.getProperties().getProperty(
                PortalConfiguration.lease_renew_rate));
        refreshInterval = Integer.parseInt(PortalConfiguration.getProperties().getProperty(
                PortalConfiguration.scheduler_cache_refreshrate));
        String url = PortalConfiguration.getProperties().getProperty(PortalConfiguration.scheduler_url);
        String cred_path = PortalConfiguration.getProperties().getProperty(
                PortalConfiguration.scheduler_cache_credential);

        while (scheduler == null) {
            try {

                if (scheduler == null) {

                    scheduler = PAActiveObject.newActive(MyCachingSchedulerProxyUserInterface.class,
                            new Object[] {});

                    // check is we use a credential file 

                    File f = new File(cred_path);

                    if (f.exists()) {
                        Credentials credential = Credentials.getCredentials(cred_path);
                        scheduler.init(url, credential);
                    } else {
                        String login = PortalConfiguration.getProperties().getProperty(
                                PortalConfiguration.scheduler_cache_login);
                        String password = PortalConfiguration.getProperties().getProperty(
                                PortalConfiguration.scheduler_cache_password);
                        scheduler.init(url, login, password);
                    }

                }
            } catch (Exception e) {
                logger.warn("no scheduler found on " + url + " retrying in 8 seconds", e);
                PAActiveObject.terminateActiveObject(scheduler, true);
                scheduler = null;
                new Sleeper(8 * 1000).sleep();
                continue;
            }
        }

    }

    private static void start_() {
        cachedSchedulerStateThreadUpdater = new Thread(new Runnable() {
            public void run() {
                while (!kill) {

                    long currentSchedulerStateRevision = scheduler.getSchedulerStateRevision();
                    try {
                        if (currentSchedulerStateRevision != schedulerRevision) {
                            long begin = System.currentTimeMillis();
                            HashMap<AtomicLong, LightSchedulerState> schedStateTmp = scheduler
                                    .getLightSchedulerState();
                            PAFuture.waitFor(schedStateTmp);
                            long end = System.currentTimeMillis();
                            revisionAndLightSchedulerState = schedStateTmp;
                            Entry<AtomicLong, LightSchedulerState> tmp = revisionAndLightSchedulerState
                                    .entrySet().iterator().next();
                            lightLocalState = tmp.getValue();
                            schedulerRevision = tmp.getKey().longValue();
                            logger.debug("updated scheduler state revision at " + schedulerRevision +
                                " ,took " + (end - begin) + " msecs for " + lightLocalState.getJobs().size() +
                                " jobs");
                        }
                    } catch (Throwable t) {
                        logger
                                .info(
                                        "exception thrown when updating scheduler caching, cache not updated, connection resetted",
                                        t);
                        init_();
                    }
                    new Sleeper(refreshInterval).sleep();
                }
            }
        }, "State Updater Thread");

        cachedSchedulerStateThreadUpdater.setDaemon(true);
        cachedSchedulerStateThreadUpdater.start();

        leaseRenewerThreadUpdater = new Thread(new Runnable() {
            public void run() {
                if ((scheduler != null) && (!kill)) {
                    try {
                        scheduler.renewSession();
                    } catch (Exception e) {
                        logger
                                .info("leaseRenewerThread was not able to call the renewSession method, exception message is " +
                                    e.getMessage());
                        init_();
                    }
                }
                new Sleeper(leaseRenewRate).sleep();
            }
        }, "Lease Renewer Thread");
        leaseRenewerThreadUpdater.setDaemon(true);
        leaseRenewerThreadUpdater.start();

    }

    public static SchedulerState getLocalState() throws NotConnectedException, PermissionException {
        return scheduler.getState();
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

    public static Map<AtomicLong, LightSchedulerState> getRevisionAndLightSchedulerState() {
        return revisionAndLightSchedulerState;
    }

    public static Map<AtomicLong, SchedulerState> getRevisionAndSchedulerState() {
        return scheduler.getRevisionVersionAndSchedulerState();
    }
}
