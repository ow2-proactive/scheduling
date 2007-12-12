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
package org.objectweb.proactive.extensions.scheduler.job;

import java.util.HashMap;

import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.extensions.scheduler.common.job.JobId;
import org.objectweb.proactive.extensions.scheduler.common.job.JobResult;
import org.objectweb.proactive.extensions.scheduler.common.task.TaskResult;


/**
 * Class representing a job result. A job result is a map of task result. The
 * key of the map is the name of the task on which to get the result. To
 * identify the job result, it provides the id of the job in the scheduler and
 * the job name.
 *
 * @author jlscheef - ProActiveTeam
 * @version 3.9, Jul 5, 2007
 * @since ProActive 3.9
 */
public class JobResultImpl implements JobResult {
    private JobId id = null;
    private String name = null;
    private HashMap<String, TaskResult> allResults = null;
    private HashMap<String, TaskResult> preciousResults = null;
    private HashMap<String, TaskResult> exceptionResults = null;

    /**
     * ProActive empty constructor
     */
    public JobResultImpl() {
    }

    /**
     * Instantiate a new JobResult with a jobId and a result
     *
     * @param id
     *            the jobId associated with this result
     * @param name
     *            the name of the job that has generate this result.
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
     * @see org.objectweb.proactive.extensions.scheduler.common.job.JobResult#addTaskResult(java.lang.String, org.objectweb.proactive.extensions.scheduler.common.task.TaskResult, boolean)
     */
    public void addTaskResult(String taskName, TaskResult taskResult,
        boolean isPrecious) {
        //allResults
        if (allResults == null) {
            allResults = new HashMap<String, TaskResult>();
        }

        allResults.put(taskName, taskResult);

        //preciousResult
        if (isPrecious) {
            if (preciousResults == null) {
                preciousResults = new HashMap<String, TaskResult>();
            }

            allResults.put(taskName, taskResult);
        }

        //exceptionResults
        if (!PAFuture.isAwaited(taskResult) && taskResult.hadException()) {
            if (exceptionResults == null) {
                exceptionResults = new HashMap<String, TaskResult>();
            }

            exceptionResults.put(taskName, taskResult);
        }
    }

    /**
     * @see org.objectweb.proactive.extensions.scheduler.common.job.JobResult#getAllResults()
     */
    public HashMap<String, TaskResult> getAllResults() {
        return allResults;
    }

    /**
         * @see org.objectweb.proactive.extensions.scheduler.common.job.JobResult#getExceptionResults()
         */
    public HashMap<String, TaskResult> getExceptionResults() {
        return exceptionResults;
    }

    /**
     * @see org.objectweb.proactive.extensions.scheduler.common.job.JobResult#getPreciousResults()
     */
    public HashMap<String, TaskResult> getPreciousResults() {
        return preciousResults;
    }

    /**
     * @see org.objectweb.proactive.extensions.scheduler.common.job.JobResult#hadException()
     */
    public boolean hadException() {
        return exceptionResults != null;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        if (allResults == null) {
            return "No result available in this job !";
        }

        StringBuilder toReturn = new StringBuilder("\n");

        for (TaskResult res : allResults.values()) {
            try {
                toReturn.append("\t" + res.value() + "\n");
            } catch (Throwable e) {
                toReturn.append("\t" + res.getException().getMessage() + "\n");
            }
        }

        return toReturn.toString();
    }
}
