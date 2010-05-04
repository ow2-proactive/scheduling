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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.core.account;

import java.lang.ref.WeakReference;

import org.ow2.proactive.account.Account;
import org.ow2.proactive.scheduler.common.job.UserIdentification;


/**
 * This class represents an account, it contains information about the
 * activity of a Scheduler User.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 2.1
 */
public final class SchedulerAccount implements Account {
    String username;
    WeakReference<UserIdentification> ref;
    int totalTaskCount;
    long totalTaskDuration;
    int totalJobCount;
    long totalJobDuration;

    public boolean isConnected() {
        return this.ref != null && this.ref.get() != null;
    }

    public long getConnectionTime() {
        if (this.isConnected()) {
            return this.ref.get().getConnectionTime();
        }
        return 0;
    }

    public long getLastSubmitTime() {
        if (this.isConnected()) {
            return this.ref.get().getLastSubmitTime();
        }
        return 0;
    }

    public int getSubmitCount() {
        if (this.isConnected()) {
            return this.ref.get().getSubmitNumber();
        }
        return 0;
    }

    public String getHostname() {
        if (this.isConnected()) {
            return this.ref.get().getHostName();
        }
        return "";
    }

    public int getTotalTaskCount() {
        return this.totalTaskCount;
    }

    public long getTotalTaskDuration() {
        return this.totalTaskDuration;
    }

    public int getTotalJobCount() {
        return this.totalJobCount;
    }

    public long getTotalJobDuration() {
        return this.totalJobDuration;
    }

    public String getName() {
        return this.username;
    }
}