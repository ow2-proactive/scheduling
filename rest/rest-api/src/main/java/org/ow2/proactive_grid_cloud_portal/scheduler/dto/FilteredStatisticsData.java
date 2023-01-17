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
package org.ow2.proactive_grid_cloud_portal.scheduler.dto;

public class FilteredStatisticsData {

    private long currentJobs;

    private long runningJobs;

    private long pausedJobs;

    private long stalledJobs;

    private long pendingJobs;

    private long currentJobsWithoutIssues;

    private long runningJobsWithoutIssues;

    private long pausedJobsWithoutIssues;

    private long stalledJobsWithoutIssues;

    private long currentJobsWithIssues;

    private long inErrorJobs;

    private long runningJobsWithIssues;

    private long pausedJobsWithIssues;

    private long stalledJobsWithIssues;

    private long pastJobsWithIssues;

    private long canceledJobs;

    private long killedJobs;

    private long failedJobs;

    private long finishedJobsWithIssues;

    private long successfulJobs;

    private long totalJobs;

    private long successfulRate;

    public long getCurrentJobs() {
        return currentJobs;
    }

    public void setCurrentJobs(long currentJobs) {
        this.currentJobs = currentJobs;
    }

    public long getRunningJobs() {
        return runningJobs;
    }

    public void setRunningJobs(long runningJobs) {
        this.runningJobs = runningJobs;
    }

    public long getPausedJobs() {
        return pausedJobs;
    }

    public void setPausedJobs(long pausedJobs) {
        this.pausedJobs = pausedJobs;
    }

    public long getStalledJobs() {
        return stalledJobs;
    }

    public void setStalledJobs(long stalledJobs) {
        this.stalledJobs = stalledJobs;
    }

    public long getPendingJobs() {
        return pendingJobs;
    }

    public void setPendingJobs(long pendingJobs) {
        this.pendingJobs = pendingJobs;
    }

    public long getCurrentJobsWithIssues() {
        return currentJobsWithIssues;
    }

    public void setCurrentJobsWithIssues(long currentJobsWithIssues) {
        this.currentJobsWithIssues = currentJobsWithIssues;
    }

    public long getInErrorJobs() {
        return inErrorJobs;
    }

    public void setInErrorJobs(long inErrorJobs) {
        this.inErrorJobs = inErrorJobs;
    }

    public long getRunningJobsWithIssues() {
        return runningJobsWithIssues;
    }

    public void setRunningJobsWithIssues(long runningJobsWithIssues) {
        this.runningJobsWithIssues = runningJobsWithIssues;
    }

    public long getPausedJobsWithIssues() {
        return pausedJobsWithIssues;
    }

    public void setPausedJobsWithIssues(long pausedJobsWithIssues) {
        this.pausedJobsWithIssues = pausedJobsWithIssues;
    }

    public long getStalledJobsWithIssues() {
        return stalledJobsWithIssues;
    }

    public void setStalledJobsWithIssues(long stalledJobsWithIssues) {
        this.stalledJobsWithIssues = stalledJobsWithIssues;
    }

    public long getPastJobsWithIssues() {
        return pastJobsWithIssues;
    }

    public void setPastJobsWithIssues(long pastJobsWithIssues) {
        this.pastJobsWithIssues = pastJobsWithIssues;
    }

    public long getCanceledJobs() {
        return canceledJobs;
    }

    public void setCanceledJobs(long canceledJobs) {
        this.canceledJobs = canceledJobs;
    }

    public long getKilledJobs() {
        return killedJobs;
    }

    public void setKilledJobs(long killedJobs) {
        this.killedJobs = killedJobs;
    }

    public long getFailedJobs() {
        return failedJobs;
    }

    public void setFailedJobs(long failedJobs) {
        this.failedJobs = failedJobs;
    }

    public long getFinishedJobsWithIssues() {
        return finishedJobsWithIssues;
    }

    public void setFinishedJobsWithIssues(long finishedJobsWithIssues) {
        this.finishedJobsWithIssues = finishedJobsWithIssues;
    }

    public long getSuccessfulJobs() {
        return successfulJobs;
    }

    public void setSuccessfulJobs(long successfulJobs) {
        this.successfulJobs = successfulJobs;
    }

    public long getTotalJobs() {
        return totalJobs;
    }

    public void setTotalJobs(long totalJobs) {
        this.totalJobs = totalJobs;
    }

    public long getSuccessfulRate() {
        return successfulRate;
    }

    public void setSuccessfulRate(long successfulRate) {
        this.successfulRate = successfulRate;
    }

    public long getCurrentJobsWithoutIssues() {
        return currentJobsWithoutIssues;
    }

    public void setCurrentJobsWithoutIssues(long currentJobsWithoutIssues) {
        this.currentJobsWithoutIssues = currentJobsWithoutIssues;
    }

    public long getRunningJobsWithoutIssues() {
        return runningJobsWithoutIssues;
    }

    public void setRunningJobsWithoutIssues(long runningJobsWithoutIssues) {
        this.runningJobsWithoutIssues = runningJobsWithoutIssues;
    }

    public long getPausedJobsWithoutIssues() {
        return pausedJobsWithoutIssues;
    }

    public void setPausedJobsWithoutIssues(long pausedJobsWithoutIssues) {
        this.pausedJobsWithoutIssues = pausedJobsWithoutIssues;
    }

    public long getStalledJobsWithoutIssues() {
        return stalledJobsWithoutIssues;
    }

    public void setStalledJobsWithoutIssues(long stalledJobsWithoutIssues) {
        this.stalledJobsWithoutIssues = stalledJobsWithoutIssues;
    }

    @Override
    public String toString() {
        return "FilteredStatisticsData{" + "currentJobs=" + currentJobs + ", runningJobs=" + runningJobs +
               ", pausedJobs=" + pausedJobs + ", stalledJobs=" + stalledJobs + ", pendingJobs=" + pendingJobs +
               ", currentJobsWithoutIssues=" + currentJobsWithoutIssues + ", runningJobsWithoutIssues=" +
               runningJobsWithoutIssues + ", pausedJobsWithoutIssues=" + pausedJobsWithoutIssues +
               ", stalledJobsWithoutIssues=" + stalledJobsWithoutIssues + ", currentJobsWithIssues=" +
               currentJobsWithIssues + ", inErrorJobs=" + inErrorJobs + ", runningJobsWithIssues=" +
               runningJobsWithIssues + ", pausedJobsWithIssues=" + pausedJobsWithIssues + ", stalledJobsWithIssues=" +
               stalledJobsWithIssues + ", pastJobsWithIssues=" + pastJobsWithIssues + ", canceledJobs=" + canceledJobs +
               ", killedJobs=" + killedJobs + ", failedJobs=" + failedJobs + ", finishedJobsWithIssues=" +
               finishedJobsWithIssues + ", successfulJobs=" + successfulJobs + ", totalJobs=" + totalJobs +
               ", successfulRate=" + successfulRate + '}';
    }
}
