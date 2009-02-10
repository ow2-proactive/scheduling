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
package org.ow2.proactive.scheduler.common.task;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Proxy;
import org.objectweb.proactive.annotation.PublicAPI;
import org.ow2.proactive.scheduler.common.job.JobEvent;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.core.db.annotation.Alterable;


/**
 * Informations about the task that is able to change.<br>
 * These informations are not in the {@link Task} class in order to permit
 * the scheduler listener to send this class as event.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
@PublicAPI
@Entity
@Table(name = "TASK_EVENT")
@AccessType("field")
@Proxy(lazy = false)
public class TaskEvent implements Serializable {
    @Id
    @GeneratedValue
    @SuppressWarnings("unused")
    private long hibernateId;

    /** id of the task */
    @Cascade(CascadeType.ALL)
    @OneToOne(fetch = FetchType.EAGER, targetEntity = TaskId.class)
    private TaskId taskId = null;

    /** informations about the job */
    @Cascade(CascadeType.ALL)
    @OneToOne(fetch = FetchType.EAGER, targetEntity = JobEvent.class)
    private JobEvent jobEvent = null;

    /** task started time */
    @Alterable
    @Column(name = "START_TIME")
    private long startTime = -1;

    /** task finished time : DEFAULT HAS TO BE SET TO -1 */
    @Alterable
    @Column(name = "FINISHED_TIME")
    private long finishedTime = -1;

    /** Current taskState of the task */
    @Alterable
    @Column(name = "TASK_STATE")
    private TaskState taskState = TaskState.SUBMITTED;

    /** name of the host where the task is executed */
    @Alterable
    @Column(name = "EXEC_HOSTNAME")
    private String executionHostName;

    /** Number of executions left */
    @Alterable
    @Column(name = "NB_EXEC_LEFT")
    private int numberOfExecutionLeft = 1;

    /** Number of execution left for this task in case of failure (node down) */
    @Alterable
    @Column(name = "NB_EXEC_ON_FAILURE_LEFT")
    private int numberOfExecutionOnFailureLeft = 1;

    /** Hibernate default constructor */
    public TaskEvent() {
    }

    /**
     * To get the jobEvent
     *
     * @return the jobEvent
     */
    public JobEvent getJobEvent() {
        return jobEvent;
    }

    /**
     * To set the jobEvent
     *
     * @param jobEvent the jobEvent to set
     */
    public void setJobEvent(JobEvent jobEvent) {
        this.jobEvent = jobEvent;
    }

    /**
     * To get the finishedTime
     *
     * @return the finishedTime
     */
    public long getFinishedTime() {
        return finishedTime;
    }

    /**
     * To set the finishedTime
     *
     * @param finishedTime the finishedTime to set
     */
    public void setFinishedTime(long finishedTime) {
        this.finishedTime = finishedTime;
    }

    /**
     * To get the jobId
     *
     * @return the jobId
     */
    public JobId getJobId() {
        if (jobEvent != null) {
            return jobEvent.getJobId();
        }

        return null;
    }

    /**
     * To set the jobId
     *
     * @param jobId the jobId to set
     */
    public void setJobId(JobId jobId) {
        if (jobEvent != null) {
            jobEvent.setJobId(jobId);
        }
    }

    /**
     * To get the startTime
     *
     * @return the startTime
     */
    public long getStartTime() {
        return startTime;
    }

    /**
     * To set the startTime
     *
     * @param startTime the startTime to set
     */
    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    /**
     * To get the taskId
     *
     * @return the taskId
     */
    public TaskId getTaskId() {
        return taskId;
    }

    /**
     * To set the taskId
     * 
     * @param taskId The taskId to be set.
     *
     */
    public void setTaskId(TaskId taskId) {
        this.taskId = taskId;
    }

    /**
     * To get the taskState
     *
     * @return the taskState
     */
    public TaskState getStatus() {
        return taskState;
    }

    /**
     * To set the taskState
     *
     * @param taskState the taskState to set
     */
    public void setStatus(TaskState taskState) {
        this.taskState = taskState;
    }

    /**
     * To get the executionHostName
     *
     * @return the executionHostName
     */
    public String getExecutionHostName() {
        return executionHostName;
    }

    /**
     * To set the executionHostName
     *
     * @param executionHostName the executionHostName to set
     */
    public void setExecutionHostName(String executionHostName) {
        this.executionHostName = executionHostName;
    }

    /**
     * Get the number of execution left.
     *
     * @return the number of execution left.
     */
    public int getNumberOfExecutionLeft() {
        return numberOfExecutionLeft;
    }

    /**
     * Set the number of execution left.
     *
     * @param numberOfExecutionLeft the number of execution left to set.
     */
    public void setNumberOfExecutionLeft(int numberOfExecutionLeft) {
        this.numberOfExecutionLeft = numberOfExecutionLeft;
    }

    /**
     * Get the numberOfExecutionOnFailureLeft value.
     * 
     * @return the numberOfExecutionOnFailureLeft value.
     */
    public int getNumberOfExecutionOnFailureLeft() {
        return numberOfExecutionOnFailureLeft;
    }

    /**
     * Decrease the number of execution left.
     */
    public void decreaseNumberOfExecutionLeft() {
        numberOfExecutionLeft--;
    }

    /**
     * Set the initial number of execution on failure left.
     * 
     * @param numberOfExecutionOnFailureLeft the new number of execution to be set.
     */
    public void setNumberOfExecutionOnFailureLeft(int numberOfExecutionOnFailureLeft) {
        this.numberOfExecutionOnFailureLeft = numberOfExecutionOnFailureLeft;
    }

    /**
     * Decrease the number of execution on failure left.
     */
    public void decreaseNumberOfExecutionOnFailureLeft() {
        numberOfExecutionOnFailureLeft--;
    }

}
