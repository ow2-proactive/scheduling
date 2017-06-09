/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.ow2.proactive_grid_cloud_portal.rm;

import java.io.File;

import org.apache.log4j.Logger;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.utils.Sleeper;
import org.ow2.proactive.authentication.crypto.CredData;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.resourcemanager.common.event.RMInitialState;
import org.ow2.proactive.resourcemanager.common.util.RMProxyUserInterface;
import org.ow2.proactive_grid_cloud_portal.webapp.PortalConfiguration;


/**
 * Periodically request {@link RMProxyUserInterface#getMonitoring()} and store it locally.
 * <p>
 * The {@link RMInitialState} fetched from {@link RMProxyUserInterface} is a large object
 * that is long to serialize, but is always the same for every client.
 * <p>
 * Use this class to start a thread that will periodically get this object
 * using a watcher account, making the cached version available to any client instantly.
 * <p>
 * Refresh rate can be configured using {@link PortalConfiguration#RM_CACHE_REFRESHRATE}
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
     * Thread frequency can be customized using {@link PortalConfiguration#RM_CACHE_REFRESHRATE}.
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
        refreshInterval = PortalConfiguration.RM_CACHE_REFRESHRATE.getValueAsInt();

        while (rm == null) {
            String url = PortalConfiguration.RM_URL.getValueAsString();
            String cred_path = PortalConfiguration.RM_CACHE_CREDENTIALS.getValueAsStringOrNull();

            try {
                if (rm == null) {
                    rm = PAActiveObject.newActive(RMProxyUserInterface.class, new Object[] {});

                    if (cred_path != null && !(new File(cred_path)).exists()) {
                        logger.error("Credentials path set in " + PortalConfiguration.RM_CACHE_CREDENTIALS.getKey() +
                                     " but file " + cred_path + " does not exist");
                    }

                    if (cred_path != null && new File(cred_path).exists()) {
                        Credentials cred = Credentials.getCredentials(cred_path);
                        rm.init(url, cred);
                    } else {
                        String login = PortalConfiguration.RM_CACHE_LOGIN.getValueAsString();
                        String password = PortalConfiguration.RM_CACHE_PASSWORD.getValueAsString();
                        rm.init(url, new CredData(login, password));
                    }
                }
            } catch (Exception e) {
                logger.warn("Could not connect to resource manager at " + url + " retrying in 8 seconds", e);
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
                boolean isDebugEnabled = logger.isDebugEnabled();
                long startTime = 0;

                while (!kill) {
                    try {
                        if (isDebugEnabled) {
                            startTime = System.currentTimeMillis();
                        }

                        state = PAFuture.getFutureValue(rm.getRMInitialState());

                        if (isDebugEnabled) {
                            logger.debug("Updated RM initial state in " + (System.currentTimeMillis() - startTime) +
                                         "ms");
                        }
                    } catch (Throwable t) {
                        logger.error("Exception occurrend while updating RM state cache, connection reset", t);
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
