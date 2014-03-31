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
package org.ow2.proactive_grid_cloud_portal.common;

import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.utils.Sleeper;
import org.apache.log4j.Logger;


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
            new Sleeper(CLEAN_PERIOD).sleep();
        }
        logger.info(Thread.currentThread().getName() + " terminated");
    }

    public void stop() {
        stop = true;
    }

}
