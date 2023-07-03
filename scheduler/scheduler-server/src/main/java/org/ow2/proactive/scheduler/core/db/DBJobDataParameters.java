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
package org.ow2.proactive.scheduler.core.db;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ow2.proactive.db.SortParameter;
import org.ow2.proactive.scheduler.common.JobSortParameter;
import org.ow2.proactive.scheduler.common.job.JobState;


/**
 * Utility container to pass multiple parameters for jobs requests.
 *
 */
public class DBJobDataParameters {
    private final int offset;

    private final int limit;

    private final String user;

    private final String tenant;

    private final boolean isExplicitTenantFilter;

    private final boolean pending;

    private final boolean running;

    private final boolean finished;

    private final boolean withIssuesOnly;

    private final boolean childJobs;

    private final String jobName;

    private final String projectName;

    private final String bucketName;

    private final Long parentId;

    private final String submissionMode;

    private final String label;

    private final List<SortParameter<JobSortParameter>> sortParameters;

    private final Set<Integer> statusRanks;

    private final long submittedTimeLessThan;

    private final long submittedTimeGreater;

    DBJobDataParameters(int offset, int limit, String user, String tenant, boolean isExplicitTenantFilter,
            boolean pending, boolean running, boolean finished, boolean withIssuesOnly, boolean childJobs,
            String jobName, String projectName, String bucketName, Long parentId, String submissionMode, String label,
            List<SortParameter<JobSortParameter>> sortParameters, long submittedTimeLessThan,
            long submittedTimeGreater) {
        this.offset = offset;
        this.limit = limit;
        this.user = user;
        this.tenant = tenant;
        this.isExplicitTenantFilter = isExplicitTenantFilter;
        this.pending = pending;
        this.running = running;
        this.finished = finished;
        this.withIssuesOnly = withIssuesOnly;
        this.childJobs = childJobs;
        this.jobName = jobName;
        this.projectName = projectName;
        this.bucketName = bucketName;
        this.submissionMode = submissionMode;
        this.label = label;
        this.parentId = parentId;
        this.sortParameters = sortParameters;
        this.submittedTimeLessThan = submittedTimeLessThan;
        this.submittedTimeGreater = submittedTimeGreater;

        Set<Integer> newStatusRanks = new HashSet<Integer>();
        if (pending) {
            newStatusRanks.add(JobState.PENDING_RANK);
        }
        if (running) {
            newStatusRanks.add(JobState.RUNNING_RANK);
        }
        if (finished) {
            newStatusRanks.add(JobState.FINISHED_RANK);
        }
        this.statusRanks = Collections.unmodifiableSet(newStatusRanks);

    }

    public int getOffset() {
        return offset;
    }

    public int getLimit() {
        return limit;
    }

    public String getUser() {
        return user;
    }

    public String getTenant() {
        return tenant;
    }

    public boolean isExplicitTenantFilter() {
        return isExplicitTenantFilter;
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

    public Long getParentId() {
        return parentId;
    }

    public List<SortParameter<JobSortParameter>> getSortParameters() {
        return sortParameters;
    }

    public Set<Integer> getStatusRanks() {
        return statusRanks;
    }

    public boolean isWithIssuesOnly() {
        return withIssuesOnly;
    }

    public String getSubmissionMode() {
        return submissionMode;
    }

    public String getLabel() {
        return label;
    }

    public long getSubmittedTimeLessThan() {
        return submittedTimeLessThan;
    }

    public long getSubmittedTimeGreater() {
        return submittedTimeGreater;
    }
}
