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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.objectweb.proactive.core.security.crypto.Session;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.scheduler.common.Scheduler;


public class RMSessionMapper {

    private Map<String, RMCachingProxyInterface> sessions;
    private static RMSessionMapper sessionMapper;
    private Map<String, Long> sessionsLastAccessToClient;
    private long currentSessionid = 0l;

    private RMSessionMapper() {
        sessions = Collections.synchronizedMap(new HashMap<String, RMCachingProxyInterface>());
        sessionsLastAccessToClient= Collections.synchronizedMap(new HashMap<String, Long>());
    }

    public static synchronized RMSessionMapper getInstance() {
        if (sessionMapper == null) {
            sessionMapper = new RMSessionMapper();
        }
        return sessionMapper;
    }

    public long add(RMCachingProxyInterface rm) {
        long id = ++currentSessionid;
        sessions.put("" + id, rm);
        sessionsLastAccessToClient.put("" + id,System.currentTimeMillis());
        return id;
    }

    public Map<String, RMCachingProxyInterface> getSessionsMap() {
        return sessions;
    }

    public Map<String, Long> getSessionsLastAccessToClient() {
        return sessionsLastAccessToClient;
    }
    
    public void remove (String key) {
        sessions.remove(key);
        sessionsLastAccessToClient.remove(key);
    }
}
