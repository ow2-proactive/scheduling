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
package org.ow2.proactive.scheduler.common;

public class JobFilterCriteriaBuilder {

    private boolean myJobsOnly;

    private boolean pending;

    private boolean running;

    private boolean finished;;

    private boolean withIssuesOnly;

    private boolean childJobs;

    private String jobName;

    private String projectName;

    private String bucketName;

    private String label;

    private String userName;

    private String tenant;

    private Long parentId;

    private String submissionMode;

    private long submittedTimeLessThan;

    private long submittedTimeGreater;

    public JobFilterCriteriaBuilder() {
    }

    public boolean isMyJobsOnly() {
        return myJobsOnly;
    }

    public boolean isPending() {
        return pending;
    }

    public boolean isRunning() {
        return running;
    }

    public boolean isFinished() {
        return finished;
    }

    public boolean isWithIssuesOnly() {
        return withIssuesOnly;
    }

    public boolean isChildJobs() {
        return childJobs;
    }

    public String getJobName() {
        return jobName;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getBucketName() {
        return bucketName;
    }

    public String getLabel() {
        return label;
    }

    public String getUserName() {
        return userName;
    }

    public String getTenant() {
        return tenant;
    }

    public Long getParentId() {
        return parentId;
    }

    public String getSubmissionMode() {
        return submissionMode;
    }

    public long getSubmittedTimeLessThan() {
        return submittedTimeLessThan;
    }

    public long getSubmittedTimeGreater() {
        return submittedTimeGreater;
    }

    public JobFilterCriteriaBuilder myJobsOnly(boolean myJobsOnly) {
        this.myJobsOnly = myJobsOnly;
        return this;
    }

    public JobFilterCriteriaBuilder pending(boolean pending) {
        this.pending = pending;
        return this;
    }

    public JobFilterCriteriaBuilder running(boolean running) {
        this.running = running;
        return this;
    }

    public JobFilterCriteriaBuilder finished(boolean finished) {
        this.finished = finished;
        return this;
    }

    public JobFilterCriteriaBuilder withIssuesOnly(boolean withIssuesOnly) {
        this.withIssuesOnly = withIssuesOnly;
        return this;
    }

    public JobFilterCriteriaBuilder childJobs(boolean childJobs) {
        this.childJobs = childJobs;
        return this;
    }

    public JobFilterCriteriaBuilder jobName(String jobName) {
        this.jobName = jobName;
        return this;
    }

    public JobFilterCriteriaBuilder projectName(String projectName) {
        this.projectName = projectName;
        return this;
    }

    public JobFilterCriteriaBuilder bucketName(String bucketName) {
        this.bucketName = bucketName;
        return this;
    }

    public JobFilterCriteriaBuilder label(String label) {
        this.label = label;
        return this;
    }

    public JobFilterCriteriaBuilder userName(String userName) {
        this.userName = userName;
        return this;
    }

    public JobFilterCriteriaBuilder tenant(String tenant) {
        this.tenant = tenant;
        return this;
    }

    public JobFilterCriteriaBuilder parentId(Long parentId) {
        this.parentId = parentId;
        return this;
    }

    public JobFilterCriteriaBuilder submissionMode(String submissionMode) {
        this.submissionMode = submissionMode;
        return this;
    }

    public JobFilterCriteriaBuilder submittedTimeLessThan(long submittedTimeLessThan) {
        this.submittedTimeLessThan = submittedTimeLessThan;
        return this;
    }

    public JobFilterCriteriaBuilder submittedTimeGreater(long submittedTimeGreater) {
        this.submittedTimeGreater = submittedTimeGreater;
        return this;
    }

    public JobFilterCriteria build() {
        return new JobFilterCriteria(this);
    }

}
