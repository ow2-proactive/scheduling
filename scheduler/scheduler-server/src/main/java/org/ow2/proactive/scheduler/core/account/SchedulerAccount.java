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
package org.ow2.proactive.scheduler.core.account;

import org.ow2.proactive.account.Account;


/**
 * This class represents an account, it contains information about the
 * activity of a Scheduler User.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 2.1
 */
public final class SchedulerAccount implements Account {

    private String username;

    private int totalTaskCount;

    private long totalTaskDuration;

    private int totalJobCount;

    private long totalJobDuration;

    public SchedulerAccount() {
    }

    public SchedulerAccount(String username, int totalTaskCount, long totalTaskDuration, int totalJobCount,
            long totalJobDuration) {
        this.username = username;
        this.totalTaskCount = totalTaskCount;
        this.totalTaskDuration = totalTaskDuration;
        this.totalJobCount = totalJobCount;
        this.totalJobDuration = totalJobDuration;
    }

    /**
     * The total count of tasks completed by the current user. 
     * @return the total task count
     */
    public int getTotalTaskCount() {
        return this.totalTaskCount;
    }

    /**
     * The total time duration in milliseconds of tasks completed by the current user. 
     * @return the total task duration in milliseconds
     */
    public long getTotalTaskDuration() {
        return this.totalTaskDuration;
    }

    /**
     * The total count of jobs completed by the current user. 
     * @return the total jobs count
     */
    public int getTotalJobCount() {
        return this.totalJobCount;
    }

    /**
     * The total time duration in milliseconds of jobs completed by the current user. 
     * @return the total job duration in milliseconds
     */
    public long getTotalJobDuration() {
        return this.totalJobDuration;
    }

    /**
     * Returns the username of this account. 
     * @return the username of this account
     */
    public String getName() {
        return this.username;
    }
}