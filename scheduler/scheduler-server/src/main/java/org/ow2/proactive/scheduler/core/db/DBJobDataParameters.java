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
import org.ow2.proactive.scheduler.common.job.JobStatus;


/**
 * Utility container to pass multiple parameters for jobs requests.
 *
 */
public class DBJobDataParameters {
    private final int offset;

    private final int limit;

    private final String user;

    private final boolean pending;

    private final boolean running;

    private final boolean finished;

    private final List<SortParameter<JobSortParameter>> sortParameters;

    private final Set<JobStatus> status;

    DBJobDataParameters(int offset, int limit, String user, boolean pending, boolean running, boolean finished,
            List<SortParameter<JobSortParameter>> sortParameters) {
        this.offset = offset;
        this.limit = limit;
        this.user = user;
        this.pending = pending;
        this.running = running;
        this.finished = finished;
        this.sortParameters = sortParameters;

        Set<JobStatus> newStatus = new HashSet<JobStatus>();
        if (pending) {
            newStatus.addAll(SchedulerDBManager.PENDING_JOB_STATUSES);
        }
        if (running) {
            newStatus.addAll(SchedulerDBManager.RUNNING_JOB_STATUSES);
        }
        if (finished) {
            newStatus.addAll(SchedulerDBManager.FINISHED_JOB_STATUSES);
        }
        this.status = Collections.unmodifiableSet(newStatus);

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

    public List<SortParameter<JobSortParameter>> getSortParameters() {
        return sortParameters;
    }

    public Set<JobStatus> getStatuses() {
        return status;
    }
}
