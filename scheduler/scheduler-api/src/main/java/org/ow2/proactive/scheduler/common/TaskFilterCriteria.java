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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.ow2.proactive.db.SortParameter;


/**
 * Default values for the embedded criterias are the following:
 * <ul>
 *     <li>no tag filtering</li>
 *     <li>no dates filtering</li>
 *     <li>no pagination</li>
 *     <li>all status are selected</li>
 *     <li>no sort parameter</li>
 * </ul>
 */
public class TaskFilterCriteria implements Serializable {

    private String tag = null;

    private long from = 0;

    private long to = 0;

    private int offset = 0;

    private int limit = 0;

    private String user = null;

    private boolean running = true;

    private boolean pending = true;

    private boolean finished = true;

    private ArrayList<SortSpecifierContainer> sortParameters = null;

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public long getFrom() {
        return from;
    }

    public void setFrom(long from) {
        this.from = from;
    }

    public long getTo() {
        return to;
    }

    public void setTo(long to) {
        this.to = to;
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

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public List<SortSpecifierContainer> getSortParameters() {
        if (sortParameters != null)
            return Collections.unmodifiableList(sortParameters);
        else
            return null;
    }

    public void setSortParameters(List<SortSpecifierContainer> sortParameters) {
        if (sortParameters != null)
            this.sortParameters = new ArrayList<SortSpecifierContainer>(sortParameters);
    }

    public static class TFCBuilder {

        private TaskFilterCriteria criterias = null;

        private TFCBuilder() {
            criterias = new TaskFilterCriteria();
        }

        public static TFCBuilder newInstance() {
            return new TFCBuilder();
        }

        /**
         * Default value is <code>null</code> (no task tag filtering)
         */
        public TFCBuilder tag(String tag) {
            criterias.setTag(tag);
            return this;
        }

        /**
         * Default value is <code>0L</code> (no date filtering)
         */
        public TFCBuilder from(long from) {
            criterias.setFrom(from);
            return this;
        }

        /**
         * Default value is <code>0L</code> (no date filtering)
         */
        public TFCBuilder to(long to) {
            criterias.setTo(to);
            return this;
        }

        /**
         * Default value is <code>0</code> (no pagination)
         */
        public TFCBuilder offset(int offset) {
            criterias.setOffset(offset);
            return this;
        }

        /**
         * Default value is <code>0</code> (no pagination)
         */
        public TFCBuilder limit(int limit) {
            criterias.setLimit(limit);
            return this;
        }

        /**
         * Default value is <code>true</code> (fetch all tasks)
         */
        public TFCBuilder running(boolean running) {
            criterias.setRunning(running);
            return this;
        }

        /**
         * Default value is <code>true</code> (fetch all tasks)
         */
        public TFCBuilder pending(boolean pending) {
            criterias.setPending(pending);
            return this;
        }

        /**
         * Default value is <code>true</code> (fetch all tasks)
         */
        public TFCBuilder finished(boolean finished) {
            criterias.setFinished(finished);
            return this;
        }

        /**
         * Default value is <code>null</code> (no user specific filtering)
         */
        public TFCBuilder user(String user) {
            criterias.setUser(user);
            return this;
        }

        /**
         * Default value is <code>null</code> (no sort parameters)
         */
        public TFCBuilder sortParameters(List<SortSpecifierContainer> sortParameters) {
            criterias.setSortParameters(sortParameters);
            return this;
        }

        public TaskFilterCriteria criterias() {
            return criterias;
        }

    }

}
