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
package org.ow2.proactive.scheduler.core.db;

import java.sql.SQLException;
import java.util.List;

import org.ow2.proactive.scheduler.common.job.JobEvent;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.task.TaskEvent;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.exception.DataBaseNotFoundException;
import org.ow2.proactive.scheduler.job.InternalJob;


/**
 * @author The ProActive Team
 */
public abstract class AbstractSchedulerDB {

    /**  */
    public static final String JOB_TABLE_NAME = "JOB_AND_JOB_EVENTS";
    /**  */
    public static final String TASK_TABLE_NAME = "TASK_EVENTS_AND_TASK_RESULTS";

    private static AbstractSchedulerDB instance = null;

    /**
     * Add a new Job to the database.
     *
     * @param internalJob the job to add.
     * @return true if success, false otherwise.
     */
    public abstract boolean addJob(InternalJob internalJob);

    /**
     * Remove a Job from the database.
     *
     * @param jobId the job to be removed.
     * @return true if success, false otherwise.
     */
    public abstract boolean removeJob(JobId jobId);

    /**
     * Update the JobEvent inside the corresponding Job.
     *
     * @param jobEvent the jobEvent to update.
     * @return true if success, false otherwise.
     */
    public abstract boolean setJobEvent(JobEvent jobEvent);

    /**
     * Update the taskEvent inside the corresponding Task.
     *
     * @param taskEvent the taskEvent to update.
     * @return true if success, false otherwise.
     */
    public abstract boolean setTaskEvent(TaskEvent taskEvent);

    /**
     * Update the JobEvent and some taskEvents inside the corresponding Job and tasks.
     *
     * @param jobEvent the jobEvent to update.
     * @param tasksEvents the taskEvents to update.
     * @return true if success, false otherwise.
     */
    public abstract boolean setJobAndTasksEvents(JobEvent jobEvent, List<TaskEvent> tasksEvents);

    /**
     * Add a result to the corresponding task.
     *
     * @param taskResult the result to be added.
     * @return true if success, false otherwise.
     */
    public abstract boolean addTaskResult(TaskResult taskResult);

    /**
     * Return the recoverable state that comes from the database.<br/>
     * This state is roughly build into this method. The one that used this result must perform its own process.
     *
     * @return The RecoverableState from the database.
     */
    public abstract RecoverableState getRecoverableState();

    /**
     * Return the result of the job corresponding to the given JobId.
     *
     * @param jobId the identification of the job on which you want the result.
     * @return The result of the designed job.
     */
    public abstract JobResult getJobResult(JobId jobId);

    /**
     * Return the result of the task corresponding to the given taskId.
     *
     * @param taskId the identification of the task on which you want the result.
     * @return The result of the designed task.
     */
    public abstract TaskResult getTaskResult(TaskId taskId);

    /**
     * Disconnect the database.
     *
     */
    public abstract void disconnect();

    /**
     * Delete the created database. If no database is linked to this object, nothing is performed.
     *
     */
    public abstract void delete();

    /**
     * Return the URL of the currently connected database.
     *
     * @return the URL of the currently connected database.
     */
    public abstract String getURL();

    /**
     * If the instance is null, this method create a new instance before
     * returning it. <br>The database instance will be conformed to the given configuration file.
     * If database instance does not exist for this configuration file, it will throw a DataBaseNotFoundException.
     * 
     * @param configFile the file that contains the description of the database.
     * @return the SchedulerDB instance.
     */
    public static AbstractSchedulerDB getInstance(String configFile) {
        if (instance == null) {
            try {
                instance = new SchedulerDB(configFile);
            } catch (SQLException e) {
                // The database doesn't exist
                throw new DataBaseNotFoundException("Database has not been found for this config file");
            }
        }

        return instance;
    }

    /**
     * Return the instance of database if it has already been created.<br>
     * If database instance has not been set yet, it will throw a DataBaseNotFoundException.
     * 
     * @return the instance of the created database.
     */
    public static AbstractSchedulerDB getInstance() {
        if (instance == null) {
            throw new DataBaseNotFoundException("Database instance has not been initialized !");
        }
        return instance;
    }

    /**
     * Set instance to null BUT BEFORE doing that, call the disconnect method !
     */
    public static void clearInstance() {
        if (instance != null) {
            instance.disconnect();
            instance = null;
        }
    }
}
