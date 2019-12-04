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

    private int pendingJobsCount;

    private int currentJobsCount;

    private int pastJobsCount;

    private int pendingTasksCount;

    private int currentTasksCount;

    private int pastTasksCount;

    private int pausedInErrorTasksCount;

    public SchedulerAccount() {
    }

    public SchedulerAccount(String username, int totalTaskCount, long totalTaskDuration, int totalJobCount,
            long totalJobDuration, int pendingJobsCount, int currentJobsCount, int pastJobsCount, int pendingTasksCount,
            int currentTasksCount, int pastTasksCount, int pausedInErrorTasksCount) {
        this.username = username;
        this.totalTaskCount = totalTaskCount;
        this.pendingTasksCount = pendingTasksCount;
        this.currentTasksCount = currentTasksCount;
        this.pastTasksCount = pastTasksCount;
        this.pausedInErrorTasksCount = pausedInErrorTasksCount;
        this.totalTaskDuration = totalTaskDuration;
        this.totalJobCount = totalJobCount;
        this.totalJobDuration = totalJobDuration;
        this.pendingJobsCount = pendingJobsCount;
        this.currentJobsCount = currentJobsCount;
        this.pastJobsCount = pastJobsCount;
    }

    /**
     * The total count of tasks completed by the current user.
     * @return the total task count
     */
    public int getTotalTaskCount() {
        return this.totalTaskCount;
    }

    /**
     * The total count of pending tasks submitted by the current user.
     * @return the total task count
     */
    public int getPendingTasksCount() {
        return this.pendingTasksCount;
    }

    /**
     * The total count of current tasks submitted by the current user.
     * @return the total task count
     */
    public int getCurrentTasksCount() {
        return this.currentTasksCount;
    }

    /**
     * The total count of past tasks submitted by the current user.
     * @return the total task count
     */
    public int getPastTasksCount() {
        return this.pastTasksCount;
    }

    /**
     * The total count of past tasks submitted by the current user.
     * @return the total task count
     */
    public int getPausedInErrorTasksCount() {
        return this.pausedInErrorTasksCount;
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
     * The total count of pending jobs submitted by the current user.
     * @return the total jobs count
     */
    public int getPendingJobsCount() {
        return pendingJobsCount;
    }

    /**
     * The total count of current jobs submitted by the current user.
     * @return the total jobs count
     */
    public int getCurrentJobsCount() {
        return currentJobsCount;
    }

    /**
     * The total count of past jobs submitted by the current user.
     * @return the total jobs count
     */
    public int getPastJobsCount() {
        return pastJobsCount;
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
