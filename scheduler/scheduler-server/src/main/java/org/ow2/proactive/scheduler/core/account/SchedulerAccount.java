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

import lombok.Builder;


/**
 * This class represents an account, it contains information about the
 * activity of a Scheduler User.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 2.1
 */
@Builder
public final class SchedulerAccount implements Account {

    private String username;

    private int totalTaskCount;

    private long totalTaskDuration;

    private int totalJobCount;

    private long totalJobDuration;

    private int pendingTasksCount;

    private int currentTasksCount;

    private int pastTasksCount;

    private int pausedTasksCount;

    private int faultyTasksCount;

    private int failedTasksCount;

    private int inErrorTasksCount;

    private int pendingJobsCount;

    private int stalledJobsCount;

    private int runningJobsCount;

    private int pausedJobsCount;

    private int inErrorJobsCount;

    private int canceledJobsCount;

    private int failedJobsCount;

    private int killedJobsCount;

    private int finishedJobsCount;

    /**
     * The total count of tasks completed by the current user.
     * @return the total task count
     */
    public int getTotalTaskCount() {
        return this.totalTaskCount;
    }

    /**
     * The total count of pending tasks submitted by the current user.
     * @return the pending task count
     */
    public int getPendingTasksCount() {
        return this.pendingTasksCount;
    }

    /**
     * The total count of current tasks submitted by the current user.
     * @return the current task count
     */
    public int getCurrentTasksCount() {
        return this.currentTasksCount;
    }

    /**
     * The total count of past tasks submitted by the current user.
     * @return the past task count
     */
    public int getPastTasksCount() {
        return this.pastTasksCount;
    }

    /**
     * The total count of paused tasks submitted by the current user.
     * @return the paused task count
     */
    public int getPausedTasksCount() {
        return this.pausedTasksCount;
    }

    /**
     * The total count of failed tasks submitted by the current user.
     * @return the failed task count
     */
    public int getFailedTasksCount() {
        return this.failedTasksCount;
    }

    /**
     * The total count of faulty tasks submitted by the current user.
     * @return the faulty task count
     */
    public int getFaultyTasksCount() {
        return this.faultyTasksCount;
    }

    /**
     * The total count of in-error tasks submitted by the current user.
     * @return the  in-error task count
     */
    public int getInErrorTasksCount() {
        return this.inErrorTasksCount;
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

    /**
     * The total count of pending jobs submitted by the current user.
     * @return the pending jobs count
     */
    public int getPendingJobsCount() {
        return pendingJobsCount;
    }

    /**
     * The total count of stalled jobs submitted by the current user.
     * @return the stalled jobs count
     */
    public int getStalledJobsCount() {
        return stalledJobsCount;
    }

    /**
     * The total count of running jobs submitted by the current user.
     * @return the running jobs count
     */
    public int getRunningJobsCount() {
        return runningJobsCount;
    }

    /**
     * The total count of paused jobs submitted by the current user.
     * @return the paused jobs count
     */
    public int getPausedJobsCount() {
        return pausedJobsCount;
    }

    /**
     * The total count of in-error jobs submitted by the current user.
     * @return the in-error jobs count
     */
    public int getInErrorJobsCount() {
        return inErrorJobsCount;
    }

    /**
     * The total count of canceled jobs submitted by the current user.
     * @return the canceled jobs count
     */
    public int getCanceledJobsCount() {
        return canceledJobsCount;
    }

    /**
     * The total count of failed jobs submitted by the current user.
     * @return the failed jobs count
     */
    public int getFailedJobsCount() {
        return failedJobsCount;
    }

    /**
     * The total count of killed jobs submitted by the current user.
     * @return the killed jobs count
     */
    public int getKilledJobsCount() {
        return killedJobsCount;
    }

    /**
     * The total count of finished jobs submitted by the current user.
     * @return the finished jobs count
     */
    public int getFinishedJobsCount() {
        return finishedJobsCount;
    }
}
