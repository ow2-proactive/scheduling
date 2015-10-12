/*
 *  *
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
 *  * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.ow2.proactive.utils;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import org.ow2.proactive.process_tree_killer.ProcessTree;
import org.apache.log4j.Logger;


public class CookieBasedProcessTreeKiller {

    private static final Logger logger = Logger.getLogger(CookieBasedProcessTreeKiller.class);

    private static final String PROCESS_KILLER_COOKIE = "PROCESS_KILLER_COOKIE";

    private final String cookieName;
    private final String cookieValue;

    private CookieBasedProcessTreeKiller(String cookieNameSuffix) {
        cookieName = PROCESS_KILLER_COOKIE + cookieNameSuffix;
        cookieValue = UUID.randomUUID().toString();
    }

    /**
     * A subsequent kill will terminate all children of the current process.
     */
    public static CookieBasedProcessTreeKiller createAllChildrenKiller(String cookieNameSuffix) {
        CookieBasedProcessTreeKiller killer = new CookieBasedProcessTreeKiller(cookieNameSuffix);
        Environment.setenv(killer.cookieName, killer.cookieValue, true);
        return killer;
    }

    /**
     * A subsequent kill will terminate only children of the process whose environment is passed as a parameter.
     */
    public static CookieBasedProcessTreeKiller createProcessChildrenKiller(String cookieNameSuffix,
            Map<String, String> processEnvironment) {
        CookieBasedProcessTreeKiller killer = new CookieBasedProcessTreeKiller(cookieNameSuffix);
        processEnvironment.put(killer.cookieName, killer.cookieValue);
        return killer;
    }

    public void kill() {
        Environment.unsetenv(cookieName); // avoid suicide
        try {
            ProcessTree.get().killAll(Collections.singletonMap(cookieName, cookieValue));
        } catch (Exception e) {
            logger.warn("Failed to kill child processes using cookie: " + cookieName, e);
        }
    }

    /**
     * Register a shutdown hook to kill children processes
     */
    public static void registerKillChildProcessesOnShutdown(String cookieNameSuffix) {
        final CookieBasedProcessTreeKiller processKiller = createAllChildrenKiller(cookieNameSuffix);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                processKiller.kill();
            }
        });
    }

}
