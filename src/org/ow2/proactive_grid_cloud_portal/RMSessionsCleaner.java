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
package org.ow2.proactive_grid_cloud_portal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.utils.Sleeper;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.util.SchedulerLoggers;
import org.ow2.proactive.scheduler.common.util.SchedulerProxyUserInterface;

/**
 * The ressource manager does not disconnect clients by itself, there is no session timeout.
 * This class takes care of removing rm proxies that have been not used during a given
 * period of time.
 *
 */
public class RMSessionsCleaner implements Runnable {

    private Logger logger = ProActiveLogger.getLogger(SchedulerLoggers.PREFIX + ".rest.sessioncleaner");

    private volatile boolean stop = false;
    private RMSessionMapper rmsm;

    // run the cleaner every 10 minutes
    protected int cleanPeriod = 5 * 60 * 1000;

    // clean when no action since timeoutDelay
    // every 60 min
    protected int timeoutDelay = 60 * 60 * 1000;

    public RMSessionsCleaner(RMSessionMapper rmsm) {
        this.rmsm = rmsm;

    }

    public void run() {
        while (!stop) {
            Map<String, RMCachingProxyInterface> sessionMap = rmsm.getSessionsMap();
            Map<String, Long> timestamps = rmsm.getSessionsLastAccessToClient();
            logger.info("cleaning session started, " + sessionMap.size() + " existing session(s) ");
            int removedSession = 0;
            List<Entry<String, RMCachingProxyInterface>> scheduledforRemoval = new ArrayList<Entry<String, RMCachingProxyInterface>>();
            synchronized (sessionMap) {
                Set<Entry<String, RMCachingProxyInterface>> entrySet = sessionMap.entrySet();
                Iterator<Entry<String, RMCachingProxyInterface>> it = entrySet.iterator();

                long currentTimeStamp = System.currentTimeMillis();

                while (it.hasNext()) {
                    Entry<String, RMCachingProxyInterface> entry = it.next();
                    RMProxy rmproxy = entry.getValue();
                    try {

                        // isConnected does not reset the lease of the stub

                        Long clientTimestamp = RMSessionMapper.getInstance().getSessionsLastAccessToClient()
                                .get(entry.getKey());

                        if (clientTimestamp != null) {
                            long clientts = clientTimestamp.longValue();
                            if (currentTimeStamp - clientts > timeoutDelay) {
                                // if not connected, removing it from the session map
                                // to clean
                                logger.info("RM session " + entry.getKey() +
                                " is scheduled for deletion, timeout reached");

                                rmproxy.disconnect(); 
                                
                                scheduledforRemoval.add(entry);
                                removedSession++;
                            }
                        }
                    } catch (Throwable t) {
                        logger.info("RM session " + entry.getKey() +
                            " is scheduled for deletion, connection issue");
                        scheduledforRemoval.add(entry);
                        removedSession++;
                    }

                }

                // effective deletion
                for (Entry<String, RMCachingProxyInterface> entry : scheduledforRemoval) {
                    sessionMap.remove(entry.getKey());
                    timestamps.remove(entry.getKey());
                }

            }
            // clean every 5 minutes
            logger.info("cleaning RM session ended, " + removedSession + " session(s) removed");
            new Sleeper(cleanPeriod).sleep();
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
