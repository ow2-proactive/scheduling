package org.ow2.proactive.scheduler.core.db;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ow2.proactive.db.SortParameter;
import org.ow2.proactive.scheduler.common.TaskSortParameter;
import org.ow2.proactive.scheduler.common.task.TaskStatus;

/**
 * Utility container to pass multiple parameters for tasks requests.
 *
 */
public class DBTaskDataParameters {
    private final String tag;
    private final long from;
    private final long to;
    private final int offset;
    private final int limit;
    private final String user;
    private final boolean pending;
    private final boolean running;
    private final boolean finished;
    private final List<SortParameter<TaskSortParameter>> sortParameters;
    private final Set<TaskStatus> status;

    DBTaskDataParameters(String tag, long from, long to, int offset, int limit, String user,
            boolean pending, boolean running, boolean finished,
            List<SortParameter<TaskSortParameter>> sortParameters) {
        this.tag = tag;
        this.from = from;
        this.to = to;
        this.offset = offset;
        this.limit = limit;
        this.user = user;
        this.pending = pending;
        this.running = running;
        this.finished = finished;
        this.sortParameters = sortParameters;
        
        Set<TaskStatus> newStatus = new HashSet<TaskStatus>();

        if (pending) newStatus.addAll(SchedulerDBManager.PENDING_TASKS);
        if (running) newStatus.addAll(SchedulerDBManager.RUNNING_TASKS);
        if (finished) newStatus.addAll(SchedulerDBManager.PENDING_TASKS);
        
        this.status = Collections.unmodifiableSet(newStatus);
        
    }

    public String getTag() {
        return tag;
    }

    public long getFrom() {
        return from;
    }

    public long getTo() {
        return to;
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

    public List<SortParameter<TaskSortParameter>> getSortParameters() {
        return sortParameters;
    }

    public Set<TaskStatus> getStatuses() {
        return status;
    }
}
