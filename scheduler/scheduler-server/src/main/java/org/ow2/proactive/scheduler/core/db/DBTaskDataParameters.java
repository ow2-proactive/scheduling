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

    private String tag;

    private long from;

    private long to;

    private int offset;

    private int limit;

    private String user;

    private Set<TaskStatus> status;

    private SortSpecifierContainer sortParams;

    public DBTaskDataParameters() {
    }

    DBTaskDataParameters(String user, String tag, long from, long to, int offset, int limit, Set<TaskStatus> statuses,
            SortSpecifierContainer sortParams) {
        this.user = user;
        this.tag = tag;
        this.from = from;
        this.to = to;
        this.offset = offset;
        this.limit = limit;
        this.status = statuses;
        this.sortParams = sortParams;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public boolean hasUser() {
        return !Strings.isNullOrEmpty(user);
    }

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

    public SortSpecifierContainer getSortParams() {
        return sortParams;
    }

    public void setSortParams(SortSpecifierContainer sortParams) {
        this.sortParams = sortParams;
    }

    public Set<TaskStatus> getStatus() {
        return status;
    }

    public void setStatus(Set<TaskStatus> status) {
        this.status = status;
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

}
