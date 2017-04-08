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
import java.util.Set;

import org.ow2.proactive.scheduler.common.SortSpecifierContainer;
import org.ow2.proactive.scheduler.common.task.TaskStatus;

import com.google.common.base.Strings;


/**
 * Utility container to pass multiple parameters for tasks requests.
 *
 * @author ActiveEon Team
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

    private final SortSpecifierContainer sortParams;

    private final Set<TaskStatus> status;

    DBTaskDataParameters(String tag, long from, long to, int offset, int limit, String user, boolean pending,
            boolean running, boolean finished, SortSpecifierContainer sortParams) {
        this.tag = tag;
        this.from = from;
        this.to = to;
        this.offset = offset;
        this.limit = limit;
        this.user = user;
        this.pending = pending;
        this.running = running;
        this.finished = finished;
        this.sortParams = sortParams;

        Set<TaskStatus> newStatus = new HashSet<>();

        if (pending) {
            newStatus.addAll(SchedulerDBManager.PENDING_TASKS);
        }

        if (running) {
            newStatus.addAll(SchedulerDBManager.RUNNING_TASKS);
        }

        if (finished) {
            newStatus.addAll(SchedulerDBManager.FINISHED_TASKS);
        }

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

    public boolean hasDateFrom() {
        return from > 0;
    }

    public boolean hasDateTo() {
        return to > 0;
    }

    public boolean hasTag() {
        return !Strings.isNullOrEmpty(tag);
    }

    public boolean hasUser() {
        return !Strings.isNullOrEmpty(user);
    }

    public boolean isRunning() {
        return running;
    }

    public boolean isPending() {
        return pending;
    }

    public boolean isFinished() {
        return finished;
    }

    public Set<TaskStatus> getStatuses() {
        return status;
    }

    public SortSpecifierContainer getSortParams() {
        return sortParams;
    }

    public static class Builder {

        private String tag;

        private String user;

        private long from;

        private long to;

        private int offset;

        private int limit;

        private boolean pending = true;

        private boolean running = true;

        private boolean finished = true;

        private SortSpecifierContainer sortParams;

        private Builder() {
            sortParams = new SortSpecifierContainer();
        }

        public static Builder create() {
            return new Builder();
        }

        public Builder setTag(String tag) {
            this.tag = tag;
            return this;
        }

        public Builder setFrom(long from) {
            this.from = from;
            return this;
        }

        public Builder setTo(long to) {
            this.to = to;
            return this;
        }

        public Builder setUser(String user) {
            this.user = user;
            return this;
        }

        public Builder setOffset(int offset) {
            this.offset = offset;
            return this;
        }

        public Builder setLimit(int limit) {
            this.limit = limit;
            return this;
        }

        public Builder setPending(boolean pending) {
            this.pending = pending;
            return this;
        }

        public Builder setRunning(boolean running) {
            this.running = running;
            return this;
        }

        public Builder setFinished(boolean finished) {
            this.finished = finished;
            return this;
        }

        public Builder setSortParams(SortSpecifierContainer sortParams) {
            this.sortParams = sortParams;
            return this;
        }

        public Builder but() {
            return create().setTag(tag)
                           .setFrom(from)
                           .setTo(to)
                           .setOffset(offset)
                           .setLimit(limit)
                           .setPending(pending)
                           .setRunning(running)
                           .setFinished(finished)
                           .setSortParams(sortParams);
        }

        public DBTaskDataParameters build() {
            return new DBTaskDataParameters(tag, from, to, offset, limit, user, pending, running, finished, sortParams);
        }
    }

}
