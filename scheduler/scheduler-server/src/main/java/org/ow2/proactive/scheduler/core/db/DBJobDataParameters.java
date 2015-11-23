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

    DBJobDataParameters(int offset, int limit, String user,
            boolean pending, boolean running, boolean finished,
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
