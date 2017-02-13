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
package org.ow2.proactive.scheduler.job;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import org.ow2.proactive.scheduler.common.exception.UnknownTaskException;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.task.TaskResult;


/**
 * Class representing a job result. A job result is a map of task result. The
 * key of the map is the name of the task on which to get the result. To
 * identify the job result, it provides the id of the job in the scheduler and
 * the job name.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class JobResultImpl implements JobResult {

    /** List of every result */
    private Map<String, TaskResult> allResults = null;

    /** List of precious results */
    private Map<String, TaskResult> preciousResults = null;

    /** Info of the job at the end */
    private JobInfo jobInfo;

    /**
     * ProActive empty constructor
     */
    public JobResultImpl() {
        allResults = new HashMap<>();
        preciousResults = new HashMap<>();
    }

    /**
     * @see org.ow2.proactive.scheduler.common.job.JobResult#getJobId()
     */
    public JobId getJobId() {
        return jobInfo.getJobId();
    }

    /**
     * @see org.ow2.proactive.scheduler.common.job.JobResult#getName()
     */
    public String getName() {
        return getJobId().getReadableName();
    }

    /**
     * @see org.ow2.proactive.scheduler.common.job.JobResult#getJobInfo()
     */
    public JobInfo getJobInfo() {
        return jobInfo;
    }

    /**
     * Set the job info for this result
     *
     * @param jobInfo the current job info to set
     */
    public void setJobInfo(JobInfo jobInfo) {
        this.jobInfo = jobInfo;
    }

    /**
     * <font color="red">-- For internal use only --</font>
     * Add a new task result to this job result.<br>
     * Used by the scheduler to fill your job result.
     *
     * @param taskName user define name (in XML) of the task.
     * @param taskResult the corresponding result of the task.
     * @param isPrecious true if this taskResult is a precious one. It will figure out in the precious result list.
     */
    public void addTaskResult(String taskName, TaskResult taskResult, boolean isPrecious) {
        //add to all Results
        allResults.put(taskName, taskResult);
        //add to precious results if needed
        if (isPrecious) {
            preciousResults.put(taskName, taskResult);
        }
    }

    /**
     * Add this new (from replication) task result to the list of known tasks.
     * 
     * @param taskName the name of the new task to add
     */
    public void addToAllResults(String taskName) {
        allResults.put(taskName, null);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.job.JobResult#getAllResults()
     */
    public Map<String, TaskResult> getAllResults() {
        return filterNullResults(allResults);
    }

    /**
     * Remove every key that have null value
     *
     * @param trs the map to filter
     * @return a new filtered map
     */
    private Map<String, TaskResult> filterNullResults(Map<String, TaskResult> trs) {
        Map<String, TaskResult> tmp = new HashMap<>();
        for (Entry<String, TaskResult> e : trs.entrySet()) {
            if (e.getValue() != null) {
                tmp.put(e.getKey(), e.getValue());
            }
        }
        return tmp;
    }

    /**
     * @see org.ow2.proactive.scheduler.common.job.JobResult#getExceptionResults()
     */
    public Map<String, TaskResult> getExceptionResults() {
        Map<String, TaskResult> exceptions = new HashMap<>();
        for (Entry<String, TaskResult> e : allResults.entrySet()) {
            if (e.getValue() != null && e.getValue().hadException()) {
                exceptions.put(e.getKey(), e.getValue());
            }
        }
        return exceptions;
    }

    /**
     * @see org.ow2.proactive.scheduler.common.job.JobResult#getPreciousResults()
     */
    public Map<String, TaskResult> getPreciousResults() {
        return preciousResults;
    }

    /**
     * @see org.ow2.proactive.scheduler.common.job.JobResult#getResult(java.lang.String)
     */
    public TaskResult getResult(String taskName) throws UnknownTaskException {
        if (!allResults.containsKey(taskName)) {
            throw new UnknownTaskException(taskName + " does not exist in this job (jobId=" + getJobId() + ")");
        }
        return allResults.get(taskName);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.job.JobResult#hadException()
     */
    public boolean hadException() {
        return getExceptionResults().size() > 0;
    }

    /**
     * @see org.ow2.proactive.scheduler.common.job.JobResult#removeResult(java.lang.String)
     */
    public void removeResult(String taskName) throws UnknownTaskException {
        if (!allResults.containsKey(taskName)) {
            throw new UnknownTaskException(taskName + " does not exist in this job (jobId=" + getJobId() + ")");
        }
        allResults.put(taskName, null);
        preciousResults.remove(taskName);
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder toReturn = new StringBuilder("\n");
        boolean hasResult = false;
        for (TaskResult res : allResults.values()) {
            if (res != null) {
                hasResult = true;
                toReturn.append("\t" + res.getTaskId().getReadableName() + " : ");
                try {
                    toReturn.append(res.value() + "\n");
                } catch (Throwable e) {
                    toReturn.append(res.getException().getMessage() + "\n");
                }
            }
        }

        if (!hasResult) {
            return "No result available in this job !";
        } else {
            return toReturn.toString();
        }
    }

}
