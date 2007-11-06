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
package org.objectweb.proactive.extra.masterworker.interfaces.internal;

import java.io.Serializable;

import org.objectweb.proactive.extra.masterworker.TaskAlreadySubmittedException;
import org.objectweb.proactive.extra.masterworker.interfaces.Task;


/**
 * <i><font size="-1" color="#FF0000">**For internal use only** </font></i><br>
 * Interface for a repository which contains task objects, each task has an internal ID.
 * @author fviale
 *
 */
public interface TaskRepository<T extends Task<?extends Serializable>> {

    /**
     * Adds a new task to the repository
     * @param task the task to add
     * @param hashCode the hashCode of the task in the user address space
     * @return the id of this task
     * @throws TaskAlreadySubmittedException if the task has already been submitted
     */
    long addTask(T task, int hashCode) throws TaskAlreadySubmittedException;

    /**
     * returns the task associated with this id
     * @param id id of the task
     * @return the task which has this id
     */
    TaskIntern<?extends Serializable> getTask(long id);

    /**
     * Removes the task from the database, i.e. the task won't be used anymore by the system <br/>
     * (i.e. when the result of the task has been computed, the task is not needed anymore) <br/>
     * @param id if of the task to remove
     */
    void removeTask(long id);

    /**
     * Remove the given id from the system, the given id will be forgotten <br/>
     * (i.e. when the results have been given back to the user, the id must be deleted, along with the hashCode stored) <br/>
     * @param id the id to remove
     */
    void removeId(long id);

    /**
     * Asks the repository to save the task in a compressed format<br/>
     * This method is called when the task has been launched and is currently running on a slave. <br/>
     * There is still a chance that the slave will fail and the task will need to be rescheduled, but still most of the times, nothing will happen.<br/>
     * @param id id of the task to save
     */
    void saveTask(long id);

    /**
     * Terminates the repository activity
     * @return true if completion succeeded
     */
    boolean terminate();
}
