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

import java.io.Serializable;

import org.objectweb.proactive.annotation.PublicAPI;


@PublicAPI
public class JobFilterCriteria implements Serializable {

    private final boolean myJobsOnly;

    private final boolean pending;

    private final boolean running;

    private final boolean finished;

    private final boolean withIssuesOnly;

    private final boolean childJobs;

    private final String jobName;

    private final String projectName;

    private final String bucketName;

    private final String userName;

    private final String tenant;

    private final Long parentId;

    private final String submissionMode;

    public JobFilterCriteria(JobFilterCriteriaBuilder builder) {
        this.myJobsOnly = builder.isMyJobsOnly();
        this.pending = builder.isPending();
        this.running = builder.isRunning();
        this.finished = builder.isFinished();
        this.withIssuesOnly = builder.isWithIssuesOnly();
        this.childJobs = builder.isChildJobs();
        this.jobName = builder.getJobName();
        this.projectName = builder.getProjectName();
        this.bucketName = builder.getBucketName();
        this.userName = builder.getUserName();
        this.tenant = builder.getTenant();
        this.parentId = builder.getParentId();
        this.submissionMode = builder.getSubmissionMode();
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
}
