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
import java.util.Set;

import org.objectweb.proactive.annotation.PublicAPI;


@PublicAPI
public class JobFilterCriteria implements Serializable {

    private final boolean myJobsOnly;

    private final boolean pending;

    private final boolean running;

    private final boolean finished;

    private final boolean childJobs;

    private final String jobName;

    private final String projectName;

    private final Set<String> tags;

    private final String userName;

    private final String tenant;

    private final Long parentId;

    public JobFilterCriteria(boolean myJobsOnly, boolean pending, boolean running, boolean finished, boolean childJobs,
            String jobName, String projectName, Set<String> tags, String userName, String tenant, Long parentId) {
        this.myJobsOnly = myJobsOnly;
        this.pending = pending;
        this.running = running;
        this.finished = finished;
        this.childJobs = childJobs;
        this.jobName = jobName;
        this.projectName = projectName;
        this.tags = tags;
        this.userName = userName;
        this.tenant = tenant;
        this.parentId = parentId;
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

    public boolean isChildJobs() {
        return childJobs;
    }

    public String getJobName() {
        return jobName;
    }

    public String getProjectName() {
        return projectName;
    }

    public Set<String> getTags() {
        return tags;
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
}
