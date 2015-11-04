package org.ow2.proactive.scheduler.core;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Locale;

import org.ow2.proactive.scheduler.common.exception.NotConnectedException;
import org.ow2.proactive.scheduler.common.exception.PermissionException;


class RestPageParameters {

    private DateFormat df;
    private SchedulerFrontendState frontEnd;
    private String calledMethod;
    private String from;
    private String to;
    private boolean myTasksOnly;
    private boolean running;
    private boolean pending;
    private boolean finished;
    private int offset;
    private int limit;
    private String tag;

    public RestPageParameters(SchedulerFrontendState frontEnd, String calledMethod, String from, String to,
            boolean myTasksOnly, boolean running, boolean pending, boolean finished, int offset, int limit,
            String tag) {
        df = DateFormat.getDateInstance(DateFormat.SHORT, Locale.US);
        this.frontEnd = frontEnd;
        this.calledMethod = calledMethod;
        this.setFrom(from);
        this.setTo(to);
        this.myTasksOnly = myTasksOnly;
        this.setRunning(running);
        this.setPending(pending);
        this.setFinished(finished);
        this.setOffset(offset);
        this.setLimit(limit);
        this.setTag(tag);
    }

    public long getFrom() {
        long dateFrom = 0;
        if (from != null && "".compareTo(from) != 0) {
            try {
                dateFrom = df.parse(from).getTime();
            } catch (ParseException e) {
                dateFrom = 0;
            }
        }
        return dateFrom;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public long getTo() {
        long dateTo = 0;
        if (to != null && "".compareTo(to) != 0) {
            try {
                dateTo = df.parse(to).getTime();
            } catch (ParseException e) {
                dateTo = 0;
            }
        }
        return dateTo;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public boolean isPending() {
        return pending;
    }

    public void setPending(boolean pending) {
        this.pending = pending;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    /**
     * Returns the user name if the we want to return only the user's tasks, if we want all tasks, it
     * returns null.
     * Throws exceptions if not connected to the Scheduler or if the user doesn't have permission to.
     */
    public String getUserName() throws NotConnectedException, PermissionException {
        if (myTasksOnly) {
        return frontEnd.checkPermission(calledMethod, "You do not have permission to use " + calledMethod)
                .getUsername();
        }
        else return null;
    }

}