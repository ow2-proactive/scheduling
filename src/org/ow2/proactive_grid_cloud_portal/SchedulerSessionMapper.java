/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 
 * or a different license than the GPL.
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

import org.ow2.proactive.scheduler.common.Scheduler;


public class SchedulerSessionMapper {

    private Map<String, Scheduler> sessions;
    private static SchedulerSessionMapper sessionMapper;
    private long currentSessionid = 0l;

    private SchedulerSessionMapper() {
        sessions = Collections.synchronizedMap(new HashMap<String, Scheduler>());
    }

    public static synchronized SchedulerSessionMapper getInstance() {
        if (sessionMapper == null) {
            sessionMapper = new SchedulerSessionMapper();
        }
        return sessionMapper;
    }

    public long add(Scheduler s) {
        long id = ++currentSessionid;
        sessions.put("" + id, s);
        return id;
    }

    public Map<String, Scheduler> getSessionsMap() {
        return sessions;
    }
}
