package org.ow2.proactive.scheduler.core.db;

import java.util.ArrayList;
import java.util.List;

import org.ow2.proactive.db.SortParameter;
import org.ow2.proactive.scheduler.common.TaskSortParameter;
import org.ow2.proactive.scheduler.common.task.TaskStatus;

/**
 * Utility container to pass multiple parameters for tasks requests.
 *
 */
public class DBTaskDataParameters {
    private String tag;
    private long from;
    private long to;
    private int offset;
    private int limit;
    private String user;
    private boolean pending;
    private boolean running;
    private boolean finished;
    private List<SortParameter<TaskSortParameter>> sortParameters;

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

    public List<TaskStatus> getStatuses() {
        List<TaskStatus> lStatuses = new ArrayList<TaskStatus>();

        if (pending) {
            lStatuses.add(TaskStatus.SUBMITTED);
            lStatuses.add(TaskStatus.PENDING);
            lStatuses.add(TaskStatus.NOT_STARTED);
        }

        if (running) {
            lStatuses.add(TaskStatus.PAUSED);
            lStatuses.add(TaskStatus.RUNNING);
            lStatuses.add(TaskStatus.WAITING_ON_ERROR);
            lStatuses.add(TaskStatus.WAITING_ON_FAILURE);
        }

        if (finished) {
            lStatuses.add(TaskStatus.FAILED);
            lStatuses.add(TaskStatus.NOT_RESTARTED);
            lStatuses.add(TaskStatus.ABORTED);
            lStatuses.add(TaskStatus.FAULTY);
            lStatuses.add(TaskStatus.FINISHED);
            lStatuses.add(TaskStatus.SKIPPED);
        }

        return lStatuses;
    }
}
