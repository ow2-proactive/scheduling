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
import org.ow2.proactive.resourcemanager.common.event.dto.RMStateDelta;
import org.ow2.proactive.resourcemanager.common.event.dto.RMStateFull;
import org.ow2.proactive.resourcemanager.common.util.RMProxyUserInterface;
import org.ow2.proactive_grid_cloud_portal.webapp.PortalConfiguration;


/**
 * Creates new AO {@link RMProxyUserInterface} in order to access filtered events.
 */
public class RMStateCaching {

    private static Logger logger = ProActiveLogger.getLogger(RMStateCaching.class);

    private static RMProxyUserInterface rm;

    /**
     * Start a thread that will periodically fetch {@link RMProxyUserInterface#getMonitoring()}.
     * <p>
     * Thread frequency can be customized using {@link PortalConfiguration#RM_CACHE_REFRESHRATE}.
     * <p>
     * Cached object can be retrieved using {@link #getRMStateDetla(long)}
     */
    public synchronized static void init() {
        init_();
    }

    private static void init_() {
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

    /**
     * @return cached RM State as returned by {@link RMProxyUserInterface#getRMStateDelta(long)}
     */
    public static RMStateDelta getRMStateDetla(long counter) {
        try {
            long startTime = System.currentTimeMillis();

            final RMStateDelta state = PAFuture.getFutureValue(rm.getRMStateDelta(counter));

            long time = System.currentTimeMillis() - startTime;

            logger.debug(String.format("Updated RM initial state in %d ms", time));

            return state;
        } catch (Exception e) {
            logger.error("Exception occurrend while updating RM state cache, connection reset", e);
            throw e;
        }
    }

    public static RMStateFull getRMStateFull() {
        try {
            long startTime = System.currentTimeMillis();

            final RMStateFull state = PAFuture.getFutureValue(rm.getRMStateFull());

            long time = System.currentTimeMillis() - startTime;

            logger.debug(String.format("Retrieved RM full state in %d ms", time));

            return state;
        } catch (Exception e) {
            logger.error("Exception occurrend while retrieving full RM state, connection reset", e);
            throw e;
        }
    }
}
