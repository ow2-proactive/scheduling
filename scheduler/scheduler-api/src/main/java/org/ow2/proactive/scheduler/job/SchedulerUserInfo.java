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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.job;

import java.io.Serializable;

import org.objectweb.proactive.annotation.PublicAPI;


@PublicAPI
public class SchedulerUserInfo implements Serializable {

    private static final long serialVersionUID = 60L;

    private final String hostName;

    private final String username;

    private final long connectionTime;

    private final long lastSubmitTime;

    private final int submitNumber;

    public SchedulerUserInfo(String hostName, String username, long connectionTime, long lastSubmitTime,
            int submitNumber) {
        this.hostName = hostName;
        this.username = username;
        this.connectionTime = connectionTime;
        this.lastSubmitTime = lastSubmitTime;
        this.submitNumber = submitNumber;
    }

    public String getHostName() {
        return hostName;
    }

    public String getUsername() {
        return username;
    }

    public long getConnectionTime() {
        return connectionTime;
    }

    public long getLastSubmitTime() {
        return lastSubmitTime;
    }

    public int getSubmitNumber() {
        return submitNumber;
    }

}
