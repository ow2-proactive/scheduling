/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.common;

import javax.xml.bind.annotation.XmlRootElement;

import org.objectweb.proactive.annotation.PublicAPI;


/**
 * Status of the scheduler.
 * The status and what you can do with the scheduler according to the current status
 * are best described below.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
@PublicAPI
@XmlRootElement(name = "schedulerstatus")
public enum SchedulerStatus implements java.io.Serializable {

    /**
     * The scheduler is running. Jobs can be submitted.
     * Get the jobs results is possible.
     * It can be paused, stopped or shutdown.
     */
    STARTED("Started"),
    /**
     * The scheduler is stopped. Jobs cannot be submitted anymore.
     * It will terminate every submitted jobs.
     * Get the jobs results is possible.
     * It can be started or shutdown.
     */
    STOPPED("Stopped"),
    /**
     * The scheduler is in freeze mode.
     * It means that every running tasks will be terminated,
     * but the running jobs will wait for the scheduler to resume.
     * It can be resumed, stopped, paused or shutdown.
     */
    FROZEN("Frozen"),
    /**
     * The scheduler is paused.
     * It means that every running jobs will be terminated.
     * It can be resumed, stopped, frozen or shutdown.
     */
    PAUSED("Paused"),
    /**
     * The scheduler is shutting down,
     * It will terminate all running jobs (during this time, get jobs results is possible),
     * then it will serialize every remaining jobs results that still are in the finished queue.
     * Finally, it will shutdown the scheduler.
     */
    SHUTTING_DOWN("Shutting down"),
    /**
     * The scheduler is unlinked with RM,
     * This can be due to the crash of the resource manager.
     * This status will block every called to the scheduler except the terminate one
     * and the call to reconnect to a new Resource Manager.
     */
    UNLINKED("Unlinked from RM"),
    /**
     * The scheduler has been killed, nothing can be done anymore.
     * (Similar to Ctrl-C)
     */
    KILLED("Killed"),
    /**
     * The scheduler has been killed due to a db disconnection, nothing can be done anymore.
     */
    DB_DOWN("Killed (DB down)");

    /** The textual definition of the status */
    private String definition;

    /**
     * Default constructor.
     * @param def the textual definition of the status.
     */
    SchedulerStatus(String def) {
        definition = def;
    }

    /**
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString() {
        return definition;
    }

    /**
     * Return true if the scheduler is killed
     *
     * @return true if the scheduler is killed
     */
    public boolean isKilled() {
        return this == KILLED || this == DB_DOWN;
    }

    /**
     * Return true if the scheduler is currently shutting down
     *
     * @return true if the scheduler is currently shutting down
     */
    public boolean isShuttingDown() {
        return isKilled() || this == SHUTTING_DOWN;
    }

    /**
     * Return true if the scheduler is NOT usable in its current state
     *
     * @return true if the scheduler is NOT usable in its current state
     */
    public boolean isUnusable() {
        return this == UNLINKED || isKilled();
    }

    /**
     * Return true if the scheduler is not currently UP
     *
     * @return true if the scheduler is not currently UP
     */
    public boolean isDown() {
        return this == UNLINKED || isShuttingDown();
    }

    /**
     * Return true if the scheduler can be stopped in its current state.
     *
     * @return true if the scheduler can be stopped in its current state.
     */
    public boolean isStoppable() {
        return !isDown() && this != STOPPED;
    }

    /**
     * Return true if the scheduler is startable
     *
     * @return true if the scheduler is startable
     */
    public boolean isStartable() {
        return !isDown() && this == STOPPED;
    }

    /**
     * Return true if the scheduler is freezable in its current state
     *
     * @return true if the scheduler is freezable in its current state
     */
    public boolean isFreezable() {
        return !isDown() && (this == PAUSED || this == STARTED);
    }

    /**
     * Return true if the scheduler is pausable in its current state
     *
     * @return true if the scheduler is pausable in its current state
     */
    public boolean isPausable() {
        return !isDown() && (this == FROZEN || this == STARTED);

    }

    /**
     * Return true if the scheduler is resumable in its current state
     *
     * @return true if the scheduler is resumable in its current state
     */
    public boolean isResumable() {
        return !isDown() && (this == PAUSED || this == FROZEN);
    }

    /**
     * Return true if a job can be submitted to scheduler
     *
     * @return true if a job can be submitted to scheduler
     */
    public boolean isSubmittable() {
        return !isShuttingDown() && this != STOPPED;
    }

}