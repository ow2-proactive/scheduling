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
package org.objectweb.proactive.extra.scheduler.job;

import java.util.HashMap;

import org.objectweb.proactive.extra.logforwarder.BufferedAppender;
import org.objectweb.proactive.extra.scheduler.common.job.JobId;
import org.objectweb.proactive.extra.scheduler.common.job.JobLogs;
import org.objectweb.proactive.extra.scheduler.common.job.JobResult;
import org.objectweb.proactive.extra.scheduler.common.task.TaskResult;


/**
 * Class representing a job result.
 * A job result is a map of task result.
 * The key of the map is the name of the task on which to get the result.
 * To identify the job result, it provides the id of the job in the scheduler and the job name.
 *
 * @author ProActive Team
 * @version 1.0, Jul 5, 2007
 * @since ProActive 3.2
 */
public class JobResultImpl implements JobResult {

    /** Serial version UID */
    private static final long serialVersionUID = 6287355063616273677L;
    private JobId id = null;
    private String name = null;
    private HashMap<String, TaskResult> taskResults = null;
    private JobLogs jobOutput;

    /**
     * ProActive empty constructor
     */
    public JobResultImpl() {
    }

    /**
     * Instanciate a new JobResult with a jobId and a result
     *
     * @param id the jobId associated with this result
     * @param result the result associated with this result
     * @param name the name of the job that has generate this result.
     */
    public JobResultImpl(JobId id, String name) {
        this.id = id;
        this.name = name;
    }

    /**
     * To get the id
     *
     * @return the id
     */
    public JobId getId() {
        return id;
    }

    /**
     * To get the name of the job that has generate this result.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Add a new task result to this job result.
     *
     * @param taskName user define name (in XML) of the task.
     * @param taskResult the corresponding result of the task.
     */
    public void addTaskResult(String taskName, TaskResult taskResult) {
        if (taskResults == null) {
            taskResults = new HashMap<String, TaskResult>();
        }
        taskResults.put(taskName, taskResult);
    }

    /**
     * Return the task results of this job as a mapping between
     * user task name (in XML jo description) and its task result.
     * User that wants to get a specific result may get this map and ask for a specific mapping.
     *
     * @return the task result as a map.
     */
    public HashMap<String, TaskResult> getTaskResults() {
        return taskResults;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        if (taskResults == null) {
            return "No result available in this job !";
        }
        StringBuilder toReturn = new StringBuilder("\n");
        for (TaskResult res : taskResults.values()) {
            try {
                toReturn.append("\t" + res.value() + "\n");
            } catch (Throwable e) {
                toReturn.append("\t" + res.getException().getMessage() + "\n");
            }
        }
        return toReturn.toString();
    }

    @Override
    public JobLogs getOutput() {
        return this.jobOutput;
    }

    @Override
    public void setOutput(JobLogs op) {
        assert (this.jobOutput == null);
        this.jobOutput = op;
    }
}
