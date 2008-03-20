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
package org.objectweb.proactive.extensions.scheduler.core.db;

import java.sql.SQLException;
import java.util.List;

import org.objectweb.proactive.extensions.scheduler.common.job.JobEvent;
import org.objectweb.proactive.extensions.scheduler.common.job.JobId;
import org.objectweb.proactive.extensions.scheduler.common.job.JobResult;
import org.objectweb.proactive.extensions.scheduler.common.task.TaskEvent;
import org.objectweb.proactive.extensions.scheduler.common.task.TaskId;
import org.objectweb.proactive.extensions.scheduler.common.task.TaskResult;
import org.objectweb.proactive.extensions.scheduler.job.InternalJob;


/**
 * @author FRADJ Johann
 */
public abstract class AbstractSchedulerDB {
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

    /**
     * If the instance is null, this method create a new instance before
     * returning it.
     *
     * @return the SchedulerDB instance.
     */
    public static AbstractSchedulerDB getInstance() {
        if (instance == null) {
            try {
                instance = new SchedulerDB();
            } catch (SQLException e) {
                // The database doesn't exist
                System.out.println("[SCHEDULER-DATABASE] database not found !");
                instance = new EmptySchedulerDB();
            }
        }

        return instance;
    }

    /**
     * Set instance to null BUT BEFORE doing that, call the disconnect method !
     */
    public static void clearInstance() {
        instance.disconnect();
        instance = null;
    }
}
