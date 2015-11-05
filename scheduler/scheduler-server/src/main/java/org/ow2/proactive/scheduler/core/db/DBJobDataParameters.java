package org.ow2.proactive.scheduler.core.db;

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
    private int offset;
    private int limit;
    private String user;
    private boolean pending;
    private boolean running;
    private boolean finished;
    private List<SortParameter<JobSortParameter>> sortParameters;

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

        Set<JobStatus> status = new HashSet<JobStatus>();
        if (pending) {
            status.addAll(SchedulerDBManager.PENDING_JOB_STATUSES);
        }
        if (running) {
            status.addAll(SchedulerDBManager.RUNNING_JOB_STATUSES);
        }
        if (finished) {
            status.addAll(SchedulerDBManager.FINISHED_JOB_STATUSES);
        }

        return status;
    }
}
