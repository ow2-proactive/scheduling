/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.extra.scheduler.common.task;


/**
 * The status of each task submitted by the user
 *
 * @author ProActive Team
 * @version 1.1, Jun 28, 2007
 * @since ProActive 3.2
 */
public enum Status implements java.io.Serializable {
    /**
     *
     * The task has just been submitted by the user
     */
    SUBMITTED("Submitted"),
    /**
     * The task is in the scheduler pending queue
     */
    PENDING("Pending"),
    /**
     * The task is paused.
     */
    PAUSED("Paused"),
    /**
     * The task is executing
     */
    RUNNNING("Running"),
    /**
     * The task is failed
     */
    FAILED("Failed"),
    /**
     * The task could not be started.
     * Warning, it means that the task could not be started due to
     * dependences failure.
     */
    NOT_STARTED("Could not start"),
    /**
     * The task has finished execution
     */
    CANCELLED("Cancelled"),
    /**
     * The task has been aborted by an exception on an other task
     */
    ABORTED("Aborted"),
    /**
     * The task has finished execution
     */
    FINISHED("Finished");
    /** The name of the current status. */
    private String name;

    /**
     * Implicit constructor of a status.
     *
     * @param name the name of the status.
     */
    Status(String name) {
        this.name = name;
    }

    /**
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString() {
        return name;
    }
}
