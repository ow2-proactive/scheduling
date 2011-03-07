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
 * 
 * @author acontes
 *
 */
public class SessionsCleaner implements Runnable {

    private Logger logger = ProActiveLogger.getLogger(SchedulerLoggers.PREFIX + ".rest.sessioncleaner");

    private volatile boolean stop = false;
    private SchedulerSessionMapper ssm;

    public SessionsCleaner(SchedulerSessionMapper ssm) {
        this.ssm = ssm;

    }

    public void run() {
        while (!stop) {
            Map<String, SchedulerProxyUserInterface> sessionMap = ssm.getSessionsMap();
            Map<String, String> usersMap = ssm.getUsernames();
            logger.info("cleaning session started, " + sessionMap.size() + " existing session(s) ");
            int removedSession = 0;
            List<Entry<String, SchedulerProxyUserInterface>> scheduledforRemoval = new ArrayList<Entry<String, SchedulerProxyUserInterface>>();
            synchronized (sessionMap) {
                Set<Entry<String, SchedulerProxyUserInterface>> entrySet = sessionMap.entrySet();
                Iterator<Entry<String, SchedulerProxyUserInterface>> it = entrySet.iterator();
                while (it.hasNext()) {
                    Entry<String, SchedulerProxyUserInterface> entry = it.next();
                    Scheduler s = entry.getValue();
                    try {

                        // isConnected does not reset the lease of the stub
                        boolean connected = false;
                        connected = s.isConnected();

                        // if not connected, removing it from the session map
                        // to clean 
                        if (!connected) {
                            logger.info("session " + entry.getKey() + " is scheduled for deletion, not connected");
                            scheduledforRemoval.add(entry);
                            removedSession++;
                        }
                    } catch (Throwable t) {
                        logger.info("session " + entry.getKey() + " is scheduled for deletion, connection issue");
                        scheduledforRemoval.add(entry);
                        removedSession++;
                    }

                }
                
                // effective deletion
                for (Entry<String, SchedulerProxyUserInterface> entry : scheduledforRemoval) {
                    sessionMap.remove(entry.getKey());
                    usersMap.remove(entry.getKey());
                }
                
            }
            // clean every 5 minutes
            logger.info("cleaning session ended, " + removedSession+ " session(s) removed");
            new Sleeper(5 * 60 * 1000).sleep();
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
