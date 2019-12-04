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
package org.ow2.proactive.scheduler.core.jmx.mbean;

/**
 * MBean interface representing the attributes of an account.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 2.1
 */
public interface MyAccountMBean {

    /**
     * The total count of jobs completed by the current user.
     * @return the total job count
     */
    int getTotalJobCount();

    /**
     * The total count of pending jobs submitted by the current user.
     * @return the total jobs count
     */
    int getPendingJobsCount();

    /**
     * The total count of current jobs submitted by the current user.
     * @return the total jobs count
     */
    int getCurrentJobsCount();

    /**
     * The total count of finished jobs submitted by the current user.
     * @return the total jobs count
     */
    int getPastJobsCount();

    /**
     * The total time duration in milliseconds of jobs completed by the current user.
     * @return the total job duration in milliseconds
     */
    long getTotalJobDuration();

    /**
     * The total count of tasks completed by the current user.
     * @return the total task count
     */
    int getTotalTaskCount();

    /**
     * The total count of pending tasks submitted by the current user.
     * @return the total task count
     */
    int getPendingTasksCount();

    /**
     * The total count of current tasks submitted by the current user.
     * @return the total task count
     */
    int getCurrentTasksCount();

    /**
     * The total count of past tasks submitted by the current user.
     * @return the total task count
     */
    int getPastTasksCount();

    /**
     * The total count of paused and in-error tasks submitted by the current user.
     * @return the total task count
     */
    int getPausedInErrorTasksCount();

    /**
     * The total time duration in milliseconds of tasks completed by the current user.
     * @return the total task duration in milliseconds
     */
    long getTotalTaskDuration();
}
