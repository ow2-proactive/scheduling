/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
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
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.job;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.annotations.Proxy;
import org.objectweb.proactive.api.PAFuture;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.task.TaskResultImpl;


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
public class JobResultImpl implements JobResult {
    @Id
    @GeneratedValue
    @SuppressWarnings("unused")
    private long hibernateId;

    /** Referenced JobId */
    @Cascade(CascadeType.ALL)
    @OneToOne(fetch = FetchType.EAGER, targetEntity = JobIdImpl.class)
    private JobId id = null;

    /** Temporary used to store proActive futur result. */
    @Transient
    private Map<String, TaskResult> futurResults = new HashMap<String, TaskResult>();

    /** List of every result */
    @OneToMany(cascade = javax.persistence.CascadeType.ALL, targetEntity = TaskResultImpl.class)
    @Cascade(CascadeType.ALL)
    @LazyCollection(value = LazyCollectionOption.FALSE)
    @JoinColumn(name = "ALL_RESULTS")
    private Map<String, TaskResult> allResults = null;

    /** List of precious result */
    @OneToMany(cascade = javax.persistence.CascadeType.ALL, targetEntity = TaskResultImpl.class)
    @Cascade(CascadeType.ALL)
    @LazyCollection(value = LazyCollectionOption.FALSE)
    @JoinColumn(name = "PRECIOUS_RESULTS")
    private Map<String, TaskResult> preciousResults = null;

    /** List of result that ends with an exception */
    @OneToMany(cascade = javax.persistence.CascadeType.ALL, targetEntity = TaskResultImpl.class)
    @Cascade(CascadeType.ALL)
    @LazyCollection(value = LazyCollectionOption.FALSE)
    @JoinColumn(name = "EXCEPTION_RESULTS")
    private Map<String, TaskResult> exceptionResults = null;

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
     */
    public JobResultImpl(JobId id) {
        this.id = id;
        this.allResults = new HashMap<String, TaskResult>();
        this.preciousResults = new HashMap<String, TaskResult>();
        this.exceptionResults = new HashMap<String, TaskResult>();
    }

    /**
     * To get the id
     *
     * @return the id
     */
    public JobId getJobId() {
        return id;
    }

    /**
     * To get the name of the job that has generate this result.
     *
     * @return the name
     */
    public String getName() {
        return getJobId().getReadableName();
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

        //allResults
        allResults.put(taskName, taskResult);

        //preciousResult
        if (isPrecious)
            preciousResults.put(taskName, taskResult);

        //exceptionResults
        if (!PAFuture.isAwaited(taskResult) && taskResult.hadException()) {
            exceptionResults.put(taskName, taskResult);
        }
    }

    /**
     * @see org.ow2.proactive.scheduler.common.job.JobResult#getAllResults()
     */
    public Map<String, TaskResult> getAllResults() {
        return allResults;
    }

    /**
     * @see org.ow2.proactive.scheduler.common.job.JobResult#getExceptionResults()
     */
    public Map<String, TaskResult> getExceptionResults() {
        return exceptionResults;
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
    public TaskResult getResult(String taskName) {
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
     *
     * @param taskName the task name of the result to get.
     * @return the result if it exists, null if not.
     */
    public TaskResult getAnyResult(String taskName) {
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
        return exceptionResults.size() > 0;
    }

    /**
     * @see org.ow2.proactive.scheduler.common.job.JobResult#removeResult(java.lang.String)
     */
    public void removeResult(String task) {
        allResults.remove(task);
        exceptionResults.remove(task);
        preciousResults.remove(task);
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        if (allResults.size() == 0) {
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
