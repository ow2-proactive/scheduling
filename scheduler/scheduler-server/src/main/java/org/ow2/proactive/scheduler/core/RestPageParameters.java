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
package org.ow2.proactive.scheduler.core;

import org.ow2.proactive.scheduler.common.SortSpecifierContainer;
import org.ow2.proactive.scheduler.common.exception.NotConnectedException;
import org.ow2.proactive.scheduler.common.exception.PermissionException;


class RestPageParameters {

    private SchedulerFrontendState frontEnd;

    private String calledMethod;

    private long from;

    private long to;

    private boolean myTasksOnly;

    private boolean running;

    private boolean pending;

    private boolean finished;

    private int offset;

    private int limit;

    private String tag;

    private String sortAttribute;

    private boolean ascendingOrder;

    private SortSpecifierContainer sortParams;

    public RestPageParameters(SchedulerFrontendState frontEnd, String calledMethod, long from, long to,
            boolean myTasksOnly, boolean running, boolean pending, boolean finished, int offset, int limit, String tag,
            SortSpecifierContainer sortParams) {
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
        this.setSortAttribute(sortAttribute);
        this.setAscendingOrder(ascendingOrder);
        this.setSortParams(sortParams);
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
        } else
            return null;
    }

    public String getSortAttribute() {
        return sortAttribute;
    }

    public boolean isAscendingOrder() {
        return ascendingOrder;
    }

    public void setSortAttribute(String sortAttribute) {
        this.sortAttribute = sortAttribute;
    }

    public void setAscendingOrder(boolean ascendingOrder) {
        this.ascendingOrder = ascendingOrder;
    }

    public SortSpecifierContainer getSortParams() {
        return sortParams;
    }

    public void setSortParams(SortSpecifierContainer sortParams) {
        this.sortParams = sortParams;
    }
}
