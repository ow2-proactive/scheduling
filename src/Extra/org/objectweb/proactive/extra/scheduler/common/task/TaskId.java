/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed, Concurrent
 * computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis Contact:
 * proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this library; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * Initial developer(s): The ProActive Team
 * http://proactive.inria.fr/team_members.htm Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.extra.scheduler.common.task;

import java.io.Serializable;

import org.objectweb.proactive.extra.scheduler.common.job.JobId;


/**
 * Definition of a task identification. For the moment, it is represented by an
 * integer.
 *
 * @author ProActive Team
 * @version 1.0, Jun 29, 2007
 * @since ProActive 3.2
 */
public final class TaskId implements Comparable<TaskId>, Serializable {

    /**
     * Multiplicatif factor for job id (taskId will be :
     * this_factor*jobID+taskID)
     */
    public static final int JOB_FACTOR = 1000;

    /** Serial version UID */
    private static final long serialVersionUID = -7367447876595953374L;

    /** the global id count */
    private static int currentId = 0;

    /** task id */
    private int id;

    /**
     * To reinitialize the initial id value
     */
    public static void initialize() {
        currentId = 0;
    }

    /**
     * Get the next id
     *
     * @return the next available id.
     */
    public static TaskId nextId(JobId jobId) {
        return new TaskId((jobId.getValue() * JOB_FACTOR) + (++currentId));
    }

    /**
     * ProActive empty constructor
     */
    public TaskId() {
    }

    /**
     * Default constructor. Just set the id of the task.
     *
     * @param id the task id to set.
     */
    private TaskId(int id) {
        this.id = id;
    }

    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(TaskId taskId) {
        return new Integer(id).compareTo(new Integer(taskId.id));
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof TaskId) {
            return ((TaskId) o).id == id;
        }
        return false;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return id;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "" + id;
    }
}
