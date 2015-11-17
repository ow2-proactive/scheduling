/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2015 INRIA/University of
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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive_grid_cloud_portal.rm;

import java.io.File;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.utils.Sleeper;
import org.ow2.proactive.authentication.crypto.CredData;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.resourcemanager.common.event.RMInitialState;
import org.ow2.proactive.resourcemanager.common.util.RMProxyUserInterface;
import org.ow2.proactive_grid_cloud_portal.webapp.PortalConfiguration;
import org.apache.log4j.Logger;


/**
 * Periodically request {@link RMProxyUserInterface#getMonitoring()} and store it locally.
 * <p>
 * The {@link RMInitialState} fetched from {@link RMProxyUserInterface} is a large object
 * that is long to serialize, but is always the same for every client.
 * <p>
 * Use this class to start a thread that will periodically get this object
 * using a watcher account, making the cached version available to any client instantly.
 * <p>
 * Refresh rate can be configured using {@link PortalConfiguration#rm_cache_refreshrate}
 */
public class RMStateCaching {

    private static Logger logger = ProActiveLogger.getLogger(RMStateCaching.class);

    private static RMProxyUserInterface rm;
    private static RMInitialState state;

    private static Thread rmUpdater;
    private static int refreshInterval;

    private static boolean kill = false;

    /**
     * Start a thread that will periodically fetch {@link RMProxyUserInterface#getMonitoring()}.
     * <p>
     * Thread frequency can be customized using {@link PortalConfiguration#rm_cache_refreshrate}.
     * <p>
     * Cached object can be retrieved using {@link #getRMInitialState()}.
     * <p>
     * Stop this thread by calling {@link #kill()}.
     */
    public synchronized static void init() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                state = new RMInitialState();
                init_();
                run_();
            }
        }).start();
    }

    private static void init_() {
        refreshInterval = Integer.parseInt(PortalConfiguration.getProperties().getProperty(
                PortalConfiguration.rm_cache_refreshrate));

        while (rm == null) {
            String url = PortalConfiguration.getProperties().getProperty(PortalConfiguration.rm_url);
            String cred_path = PortalConfiguration.getProperties().getProperty(
                    PortalConfiguration.rm_cache_credential);

            try {
                if (rm == null) {
                    rm = PAActiveObject.newActive(RMProxyUserInterface.class, new Object[] {});
                    if (cred_path != null && new File(cred_path).exists()) {
                        Credentials cred = Credentials.getCredentials(cred_path);
                        rm.init(url, cred);
                    } else {
                        String login = PortalConfiguration.getProperties().getProperty(
                                PortalConfiguration.rm_cache_login);
                        String password = PortalConfiguration.getProperties().getProperty(
                                PortalConfiguration.rm_cache_password);
                        rm.init(url, new CredData(login, password));
                    }
                }
            } catch (Exception e) {
                if (rm != null) {
                    PAActiveObject.terminateActiveObject(rm, true);
                    rm = null;
                }
                new Sleeper(8 * 1000, logger).sleep();
                continue;
            }
        }
    }

    private static void run_() {
        rmUpdater = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!kill) {
                    try {
                        long t1 = System.currentTimeMillis();
                        state = PAFuture.getFutureValue(rm.getRMInitialState());
                        long t2 = System.currentTimeMillis();
                        logger.debug("Updated RM initial state in " + (t2 - t1) + "ms");
                    } catch (Throwable t) {
                        logger
                                .error("Exception occurrend while updating RM state cache, connection reset",
                                        t);
                        init_();
                    }

                    new Sleeper(refreshInterval, logger).sleep();
                }
            }
        });
        rmUpdater.setName("RM Initial State Cache Updater");
        rmUpdater.setDaemon(true);
        rmUpdater.start();
    }

    /**
     * @return cached RM State as returned by {@link RMProxyUserInterface#getMonitoring()}
     */
    public static RMInitialState getRMInitialState() {
        return state;
    }

    /**
     * Stop the RM State polling thread.
     */
    public static void kill() {
        RMStateCaching.kill = true;
    }

}
