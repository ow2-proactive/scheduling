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
package org.ow2.proactive_grid_cloud_portal.common;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.utils.Sleeper;


/**
 * This class takes care of removing sessions that have been not used during a given
 * period of time.
 *
 */
public class SessionsCleaner implements Runnable {

    private static final Logger logger = ProActiveLogger.getLogger(SessionsCleaner.class);

    // run the cleaner every 10 minutes
    private static final int CLEAN_PERIOD = 5 * 60 * 1000;

    // clean when no action since TIMEOUT_DELAY
    // every 60 min
    private static final int TIMEOUT_DELAY = 60 * 60 * 1000;

    private volatile boolean stop = false;

    private SessionStore sessionStore;

    public SessionsCleaner(SessionStore sessionStore) {
        this.sessionStore = sessionStore;
    }

    public void run() {
        while (!stop) {
            logger.info("cleaning session started, " + sessionStore.size() + " existing session(s) ");
            int removedSession = sessionStore.terminateExpiredSessions(TIMEOUT_DELAY);

            // clean every 5 minutes
            logger.info("cleaning session ended, " + removedSession + " session(s) removed");
            new Sleeper(CLEAN_PERIOD, logger).sleep();
        }
        logger.info(Thread.currentThread().getName() + " terminated");
    }

    public void stop() {
        stop = true;
    }

}
