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
package org.ow2.proactive_grid_cloud_portal.scheduler.dto;

public enum SchedulerStatusData {
    /**
     * The scheduler is running. Jobs can be submitted.
     * Get the jobs results is possible.
     * It can be paused, stopped or shutdown.
     */
    STARTED,
    /**
     * The scheduler is stopped. Jobs cannot be submitted anymore.
     * It will terminate every submitted jobs.
     * Get the jobs results is possible.
     * It can be started or shutdown.
     */
    STOPPED,
    /**
     * The scheduler is in freeze mode.
     * It means that every running tasks will be terminated,
     * but the running jobs will wait for the scheduler to resume.
     * It can be resumed, stopped, paused or shutdown.
     */
    FROZEN,
    /**
     * The scheduler is paused.
     * It means that every running jobs will be terminated.
     * It can be resumed, stopped, frozen or shutdown.
     */
    PAUSED,
    /**
     * The scheduler is shutting down,
     * It will terminate all running tasks.
     * During this time, get jobs results is possible.
     * Finally, it will terminate the scheduler.
     */
    SHUTTING_DOWN,
    /**
     * The scheduler is unlinked with RM,
     * This can be due to the crash of the resource manager.
     * This status will block every called to the scheduler except the terminate one
     * and the call to reconnect to a new Resource Manager.
     */
    UNLINKED,
    /**
     * The scheduler has been killed, nothing can be done anymore.
     * (Similar to Ctrl-C)
     */
    KILLED
}
