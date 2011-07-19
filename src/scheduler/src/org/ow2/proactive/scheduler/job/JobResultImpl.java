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
package org.ow2.proactive.scheduler.job;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.annotations.Proxy;
import org.objectweb.proactive.api.PAFuture;
import org.ow2.proactive.scheduler.common.exception.UnknownTaskException;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.task.TaskResultImpl;
import org.ow2.proactive.scheduler.task.internal.InternalTask;


/**
 * Class representing a job result. A job result is a map of task result. The
 * key of the map is the name of the task on which to get the result. To
 * identify the job result, it provides the id of the job in the scheduler and
 * the job name.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
@Entity
@Table(name = "JOB_RESULT_IMPL")
@AccessType("field")
@Proxy(lazy = false)
@XmlAccessorType(XmlAccessType.FIELD)
public class JobResultImpl implements JobResult {
    /**  */
    private static final long serialVersionUID = 31L;

    @Id
    @GeneratedValue
    @SuppressWarnings("unused")
    @XmlTransient
    private long hId;

    /** Referenced JobId */
    @Cascade(CascadeType.ALL)
    @OneToOne(fetch = FetchType.EAGER, targetEntity = JobIdImpl.class)
    private JobId id = null;

    /** Temporary used to store proActive future result. */
    @Transient
    private Map<String, TaskResult> futurResults = new HashMap<String, TaskResult>();

    /** List of every result */
    @OneToMany(cascade = javax.persistence.CascadeType.ALL, targetEntity = TaskResultImpl.class)
    @Cascade(CascadeType.ALL)
    @LazyCollection(value = LazyCollectionOption.FALSE)
    @JoinColumn(name = "ALL_RESULTS")
    private Map<String, TaskResult> allResults = null;

    /** List of precious results */
    @OneToMany(cascade = javax.persistence.CascadeType.ALL, targetEntity = TaskResultImpl.class)
    @Cascade(CascadeType.ALL)
    @LazyCollection(value = LazyCollectionOption.FALSE)
    @JoinColumn(name = "PRECIOUS_RESULTS")
    private Map<String, TaskResult> preciousResults = null;

    /** Info of the job at the end */
    @Transient
    private JobInfo jobInfo;

    /**
     * ProActive empty constructor
     */
    public JobResultImpl() {
    }

    /**
     * Instantiate a new JobResult with a jobId and a result
     *
     * @param id the jobId associated with this result
     */
    public JobResultImpl(InternalJob job) {
        this.id = job.getId();
        this.allResults = new HashMap<String, TaskResult>(job.getTasks().size());
        this.preciousResults = new HashMap<String, TaskResult>();
        for (InternalTask it : job.getIHMTasks().values()) {
            this.allResults.put(it.getName(), null);
        }
    }

    /**
     * @see org.ow2.proactive.scheduler.common.job.JobResult#getJobId()
     */
    public JobId getJobId() {
        return id;
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
     * Used to store the futur result.
     * It must be temporary in a Hibernate transient map to avoid empty ID Object.
     *
     * @param taskName user define name (in XML) of the task.
     * @param taskResult the corresponding result of the task.
     */
    public void storeFuturResult(String taskName, TaskResult taskResult) {
        futurResults.put(taskName, taskResult);
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
        //remove futur Result
        futurResults.remove(taskName);
        //add to all Results
        allResults.put(taskName, taskResult);
        //add to precious results if needed
        if (isPrecious) {
            preciousResults.put(taskName, taskResult);
        }
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
        Map<String, TaskResult> tmp = new HashMap<String, TaskResult>();
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
        Map<String, TaskResult> exceptions = new HashMap<String, TaskResult>();
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
            throw new UnknownTaskException(taskName + " does not exist in this job (jobId=" + id + ")");
        }
        TaskResult tr = futurResults.get(taskName);
        if (tr == null) {
            return allResults.get(taskName);
        } else if (!PAFuture.isAwaited(tr)) {
            return tr;
        } else {
            return null;
        }
    }

    /**
     * Return any result even if it is awaited futur.
     * Null is returned only if the given taskName is unknown
     * <u>For internal Use.</u>
     *
     * @param taskName the task name of the result to get.
     * @return the result if it exists, null if not.
     */
    public TaskResult getAnyResult(String taskName) throws UnknownTaskException {
        TaskResult tr = futurResults.get(taskName);
        if (tr == null) {
            return allResults.get(taskName);
        } else {
            return tr;
        }
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
            throw new UnknownTaskException(taskName + " does not exist in this job (jobId=" + id + ")");
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
