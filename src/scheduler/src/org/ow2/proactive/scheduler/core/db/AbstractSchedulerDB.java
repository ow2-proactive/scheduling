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

    public static final String JOB_TABLE_NAME = "JOB_AND_JOB_EVENTS";
    public static final String TASK_TABLE_NAME = "TASK_EVENTS_AND_TASK_RESULTS";

    // TODO comments
    private static AbstractSchedulerDB instance = null;

    public abstract boolean addJob(InternalJob internalJob);

    public abstract boolean removeJob(JobId jobId);

    public abstract boolean setJobEvent(JobEvent jobEvent);

    public abstract boolean setTaskEvent(TaskEvent taskEvent);

    public abstract boolean setJobAndTasksEvents(JobEvent jobEvent, List<TaskEvent> tasksEvents);

    public abstract boolean addTaskResult(TaskResult taskResult);

    public abstract RecoverableState getRecoverableState();

    public abstract JobResult getJobResult(JobId jobId);

    public abstract TaskResult getTaskResult(TaskId taskId);

    public abstract void disconnect();

    public abstract void delete();

    public abstract String getURL();

    /**
     * If the instance is null, this method create a new instance before
     * returning it. <br>The database instance will be conformed to the given config file.
     * If database instance does not exist for this config file, it will throw a DataBaseNotFoundException.
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
