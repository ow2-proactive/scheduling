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
package org.ow2.proactive.scheduler.common.job;

import java.io.Serializable;

import org.objectweb.proactive.annotation.PublicAPI;


@PublicAPI
public class FilteredStatistics implements Serializable {

    private final long currentJobs;

    private final long runningJobs;

    private final long pausedJobs;

    private final long stalledJobs;

    private final long pendingJobs;

    private final long currentJobsWithoutIssues;

    private final long runningJobsWithoutIssues;

    private final long pausedJobsWithoutIssues;

    private final long stalledJobsWithoutIssues;

    private final long currentJobsWithIssues;

    private final long inErrorJobs;

    private final long runningJobsWithIssues;

    private final long pausedJobsWithIssues;

    private final long stalledJobsWithIssues;

    private final long pastJobsWithIssues;

    private final long canceledJobs;

    private final long killedJobs;

    private final long failedJobs;

    private final long finishedJobsWithIssues;

    private final long successfulJobs;

    private final long totalJobs;

    private final long successfulRate;

    public FilteredStatistics(long currentJobs, long runningJobs, long pausedJobs, long stalledJobs, long pendingJobs,
            long currentJobsWithoutIssues, long runningJobsWithoutIssues, long pausedJobsWithoutIssues,
            long stalledJobsWithoutIssues, long currentJobsWithIssues, long inErrorJobs, long runningJobsWithIssues,
            long pausedJobsWithIssues, long stalledJobsWithIssues, long pastJobsWithIssues, long canceledJobs,
            long killedJobs, long failedJobs, long finishedJobsWithIssues, long successfulJobs, long totalJobs,
            long successfulRate) {
        this.currentJobs = currentJobs;
        this.runningJobs = runningJobs;
        this.pausedJobs = pausedJobs;
        this.stalledJobs = stalledJobs;
        this.pendingJobs = pendingJobs;
        this.currentJobsWithoutIssues = currentJobsWithoutIssues;
        this.runningJobsWithoutIssues = runningJobsWithoutIssues;
        this.pausedJobsWithoutIssues = pausedJobsWithoutIssues;
        this.stalledJobsWithoutIssues = stalledJobsWithoutIssues;
        this.currentJobsWithIssues = currentJobsWithIssues;
        this.inErrorJobs = inErrorJobs;
        this.runningJobsWithIssues = runningJobsWithIssues;
        this.pausedJobsWithIssues = pausedJobsWithIssues;
        this.stalledJobsWithIssues = stalledJobsWithIssues;
        this.pastJobsWithIssues = pastJobsWithIssues;
        this.canceledJobs = canceledJobs;
        this.killedJobs = killedJobs;
        this.failedJobs = failedJobs;
        this.finishedJobsWithIssues = finishedJobsWithIssues;
        this.successfulJobs = successfulJobs;
        this.totalJobs = totalJobs;
        this.successfulRate = successfulRate;
    }

    public long getCurrentJobs() {
        return currentJobs;
    }

    public long getRunningJobs() {
        return runningJobs;
    }

    public long getPausedJobs() {
        return pausedJobs;
    }

    public long getStalledJobs() {
        return stalledJobs;
    }

    public long getPendingJobs() {
        return pendingJobs;
    }

    public long getCurrentJobsWithIssues() {
        return currentJobsWithIssues;
    }

    public long getInErrorJobs() {
        return inErrorJobs;
    }

    public long getRunningJobsWithIssues() {
        return runningJobsWithIssues;
    }

    public long getPausedJobsWithIssues() {
        return pausedJobsWithIssues;
    }

    public long getStalledJobsWithIssues() {
        return stalledJobsWithIssues;
    }

    public long getPastJobsWithIssues() {
        return pastJobsWithIssues;
    }

    public long getCanceledJobs() {
        return canceledJobs;
    }

    public long getKilledJobs() {
        return killedJobs;
    }

    public long getFailedJobs() {
        return failedJobs;
    }

    public long getFinishedJobsWithIssues() {
        return finishedJobsWithIssues;
    }

    public long getSuccessfulJobs() {
        return successfulJobs;
    }

    public long getTotalJobs() {
        return totalJobs;
    }

    public long getSuccessfulRate() {
        return successfulRate;
    }

    public long getCurrentJobsWithoutIssues() {
        return currentJobsWithoutIssues;
    }

    public long getRunningJobsWithoutIssues() {
        return runningJobsWithoutIssues;
    }

    public long getPausedJobsWithoutIssues() {
        return pausedJobsWithoutIssues;
    }

    public long getStalledJobsWithoutIssues() {
        return stalledJobsWithoutIssues;
    }
}
