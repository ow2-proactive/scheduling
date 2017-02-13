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
package org.ow2.proactive.scheduler.common.util;

/**
 * Utility methods to ease pagination.
 *
 * @author ActiveEon Team
 */
public final class Pagination {

    /**
     * Maximum number of task items to retrieve.
     */
    public static final int TASKS_MAXIMUM_PAGE_SIZE = 256;

    public static PageBoundaries getTasksPageBoundaries(int offset, int limit, int defaultPageSize) {
        return getPageBoundaries(offset, limit, defaultPageSize, TASKS_MAXIMUM_PAGE_SIZE);
    }

    public static PageBoundaries getPageBoundaries(int offset, int limit, int defaultPageSize, int maximumPageSize) {
        if (offset < 0) {
            offset = 0;
        }

        if (limit <= 0) {
            limit = defaultPageSize;
        }

        if (limit > maximumPageSize) {
            limit = maximumPageSize;
        }

        return new PageBoundaries(offset, limit);
    }

}
