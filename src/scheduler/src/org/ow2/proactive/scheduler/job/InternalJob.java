/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of 
 * 						   Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
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
 * If needed, contact us to obtain a release under GPL Version 2. 
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package org.ow2.proactive.scheduler.job;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.log4j.Logger;
import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.AnyMetaDef;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.annotations.ManyToAny;
import org.hibernate.annotations.MapKeyManyToMany;
import org.hibernate.annotations.MetaValue;
import org.hibernate.annotations.Proxy;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.dataspaces.core.naming.NamingService;
import org.ow2.proactive.scheduler.common.db.annotation.Alterable;
import org.ow2.proactive.scheduler.common.job.JobDescriptor;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobPriority;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.JobStatus;
import org.ow2.proactive.scheduler.common.task.TaskDescriptor;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskInfo;
import org.ow2.proactive.scheduler.common.task.TaskState;
import org.ow2.proactive.scheduler.common.task.TaskStatus;
import org.ow2.proactive.scheduler.core.annotation.TransientInSerialization;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.task.TaskIdImpl;
import org.ow2.proactive.scheduler.task.internal.InternalForkedJavaTask;
import org.ow2.proactive.scheduler.task.internal.InternalJavaTask;
import org.ow2.proactive.scheduler.task.internal.InternalNativeTask;
import org.ow2.proactive.scheduler.task.internal.InternalTask;
import org.ow2.proactive.scheduler.util.SchedulerDevLoggers;


/**
 * Internal and global description of a job.
 * This class contains all informations about the job to launch.
 * It also provides method to manage the content regarding the scheduling process.<br/>
 * Specific internal job may extend this abstract class.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
@MappedSuperclass
@Table(name = "INTERNAL_JOB")
@AccessType("field")
@Proxy(lazy = false)
public abstract class InternalJob extends JobState {
    public static final Logger logger_dev = ProActiveLogger.getLogger(SchedulerDevLoggers.CORE);

    /** Owner of the job */
    @Column(name = "OWNER")
    private String owner = "";

    /** List of every tasks in this job. */
    @ManyToAny(metaColumn = @Column(name = "ITASK_TYPE", length = 5))
    @AnyMetaDef(idType = "long", metaType = "string", metaValues = {
            @MetaValue(targetEntity = InternalJavaTask.class, value = "IJT"),
            @MetaValue(targetEntity = InternalNativeTask.class, value = "INT"),
            @MetaValue(targetEntity = InternalForkedJavaTask.class, value = "IFJT") })
    @JoinTable(joinColumns = @JoinColumn(name = "ITASK_ID"), inverseJoinColumns = @JoinColumn(name = "DEPEND_ID"))
    @LazyCollection(value = LazyCollectionOption.FALSE)
    @Cascade(CascadeType.ALL)
    @MapKeyManyToMany(targetEntity = TaskIdImpl.class)
    protected Map<TaskId, InternalTask> tasks = new HashMap<TaskId, InternalTask>();

    /** Informations (that can be modified) about job execution */
    @Alterable
    @Cascade(CascadeType.ALL)
    @OneToOne(fetch = FetchType.EAGER, targetEntity = JobInfoImpl.class)
    protected JobInfoImpl jobInfo = new JobInfoImpl();

    /** Job descriptor for dependences management */
    //Not DB managed, created once needed.
    @Transient
    @TransientInSerialization
    private JobDescriptor jobDescriptor;

    /** DataSpace application manager for this job */
    //Not DB managed, created once needed.
    @Transient
    @TransientInSerialization
    private JobDataSpaceApplication jobDataSpaceApplication;

    /** Job result */
    @Cascade(CascadeType.ALL)
    @OneToOne(fetch = FetchType.EAGER, targetEntity = JobResultImpl.class)
    @TransientInSerialization
    private JobResult jobResult;

    /** Initial waiting time for a task before restarting in millisecond */
    @Column(name = "RESTART_TIMER")
    @TransientInSerialization
    private long restartWaitingTimer = PASchedulerProperties.REEXECUTION_INITIAL_WAITING_TIME.getValueAsInt();

    /** Hibernate default constructor */
    public InternalJob() {
    }

    /**
     * Create a new Job with the given parameters. It provides methods to add or
     * remove tasks.
     *
     * @param name the current job name.
     * @param priority the priority of this job between 1 and 5.
     * @param cancelJobOnError true if the job has to run until its end or an user intervention.
     * @param description a short description of the job and what it will do.
     */

    public InternalJob(String name, JobPriority priority, boolean cancelJobOnError, String description) {
        this.name = name;
        this.jobInfo.setPriority(priority);
        this.setCancelJobOnError(cancelJobOnError);
        this.description = description;
    }

    /**
     * This method will do two things :<br />
     * First, it will update the job with the informations contained in the given taskInfo<br />
     * Then, it will update the proper task using the same given taskInfo.
     *
     * @param info a taskInfo containing new information about the task.
     */
    @Override
    public synchronized void update(TaskInfo info) {
        //ensure that is a JobInfoImpl
        //if not, we are in client side and client brings its own JobInfo Implementation
        if (!getId().equals(info.getJobId())) {
            throw new IllegalArgumentException(
                "This job info is not applicable for this job. (expected id is '" + getId() + "' but was '" +
                    info.getJobId() + "'");
        }
        jobInfo = (JobInfoImpl) info.getJobInfo();
        try {
            tasks.get(info.getTaskId()).update(info);
        } catch (NullPointerException e) {
            throw new IllegalArgumentException("This task info is not applicable in this job. (task id '" +
                info.getTaskId() + "' not found)");
        }
    }

    /**
     * To update the content of this job with a jobInfo.
     *
     * @param info the JobInfo to set
     */
    @Override
    public synchronized void update(JobInfo info) {
        if (!getId().equals(info.getJobId())) {
            throw new IllegalArgumentException(
                "This job info is not applicable for this job. (expected id is '" + getId() + "' but was '" +
                    info.getJobId() + "'");
        }
        //update job info
        this.jobInfo = (JobInfoImpl) info;
        //update task status if needed
        if (this.jobInfo.getTaskStatusModify() != null) {
            for (TaskId id : tasks.keySet()) {
                tasks.get(id).setStatus(this.jobInfo.getTaskStatusModify().get(id));
            }
        }
        //update task finished time if needed
        if (this.jobInfo.getTaskFinishedTimeModify() != null) {
            for (TaskId id : tasks.keySet()) {
                if (this.jobInfo.getTaskFinishedTimeModify().containsKey(id)) {
                    //a null send to a long setter throws a NullPointerException so, here is the fix
                    tasks.get(id).setFinishedTime(this.jobInfo.getTaskFinishedTimeModify().get(id));
                }
            }
        }
    }

    /**
     * @see org.ow2.proactive.scheduler.common.job.JobState#getJobInfo()
     */
    @Override
    public JobInfo getJobInfo() {
        return jobInfo;
    }

    /**
     * Append a task to this job.
     *
     * @param task the task to add.
     * @return true if the task has been correctly added to the job, false if
     *         not.
     */
    public boolean addTask(InternalTask task) {
        task.setJobId(getId());

        task.setId(TaskIdImpl.nextId(getId(), task.getName()));

        boolean result = (tasks.put(task.getId(), task) == null);

        if (result) {
            jobInfo.setTotalNumberOfTasks(jobInfo.getTotalNumberOfTasks() + 1);
        }

        return result;
    }

    /**
     * Start a new task will set some count and update dependencies if necessary.
     *
     * @param td the task which has just been started.
     */
    public void startTask(InternalTask td) {
        logger_dev.debug(" ");
        setNumberOfPendingTasks(getNumberOfPendingTasks() - 1);
        setNumberOfRunningTasks(getNumberOfRunningTasks() + 1);

        if (getStatus() == JobStatus.STALLED) {
            setStatus(JobStatus.RUNNING);
        }

        getJobDescriptor().start(td.getId());
        td.setStatus(TaskStatus.RUNNING);
        td.setStartTime(System.currentTimeMillis());
        td.setFinishedTime(-1);
        td.setExecutionHostName(td.getExecuterInformations().getHostName() + " (" +
            td.getExecuterInformations().getNodeName() + ")");
    }

    /**
     * Start dataspace configuration and application
     */
    public void startDataSpaceApplication(NamingService namingService, String namingServiceURL) {
        if (jobDataSpaceApplication == null) {
            long appId = getJobInfo().getJobId().hashCode();
            jobDataSpaceApplication = new JobDataSpaceApplication(appId, namingService, namingServiceURL);
        }
        jobDataSpaceApplication.startDataSpaceApplication(getInputSpace(), getOutputSpace(), getOwner());
    }

    /**
     * Updates count for running to pending event.
     */
    public void newWaitingTask() {
        logger_dev.debug(" ");
        setNumberOfPendingTasks(getNumberOfPendingTasks() + 1);
        setNumberOfRunningTasks(getNumberOfRunningTasks() - 1);
        if (getNumberOfRunningTasks() == 0) {
            setStatus(JobStatus.STALLED);
        }
    }

    /**
     * Set this task in restart mode, it will set the task to pending status and change task count.
     *
     * @param task the task which has to be restarted.
     */
    public void reStartTask(InternalTask task) {
        logger_dev.debug(" ");
        getJobDescriptor().reStart(task.getId());

        if (getStatus() == JobStatus.PAUSED) {
            task.setStatus(TaskStatus.PAUSED);
            HashMap<TaskId, TaskStatus> hts = new HashMap<TaskId, TaskStatus>();
            hts.put(task.getId(), task.getStatus());
            getJobDescriptor().update(hts);
        } else {
            task.setStatus(TaskStatus.PENDING);
        }
    }

    /**
     * Terminate a task, change status, managing dependences
     *
     * @param errorOccurred has an error occurred for this termination
     * @param taskId the task to terminate.
     * @return the taskDescriptor that has just been terminated.
     */
    public InternalTask terminateTask(boolean errorOccurred, TaskId taskId) {
        logger_dev.debug(" ");
        InternalTask descriptor = tasks.get(taskId);
        descriptor.setFinishedTime(System.currentTimeMillis());
        descriptor.setStatus(errorOccurred ? TaskStatus.FAULTY : TaskStatus.FINISHED);
        setNumberOfRunningTasks(getNumberOfRunningTasks() - 1);
        setNumberOfFinishedTasks(getNumberOfFinishedTasks() + 1);

        if ((getStatus() == JobStatus.RUNNING) && (getNumberOfRunningTasks() == 0)) {
            setStatus(JobStatus.STALLED);
        }

        //terminate this task
        getJobDescriptor().terminate(taskId);

        //creating list of status for the jobDescriptor
        HashMap<TaskId, TaskStatus> hts = new HashMap<TaskId, TaskStatus>();

        for (InternalTask td : tasks.values()) {
            hts.put(td.getId(), td.getStatus());
        }

        //updating job descriptor for eligible task
        getJobDescriptor().update(hts);

        return descriptor;
    }

    /**
     * Simulate that a task have been started and terminated.
     * Used only by the recovery method in scheduler core.
     *
     * @param id the id of the task to start and terminate.
     */
    public void simulateStartAndTerminate(TaskId id) {
        logger_dev.debug(" ");
        getJobDescriptor().start(id);
        getJobDescriptor().terminate(id);
    }

    /**
     * Failed this job due to the given task failure or job has been killed
     *
     * @param taskId the task that has been the cause to failure. Can be null if the job has been killed
     * @param jobStatus type of the failure on this job. (failed/canceled/killed)
     */
    public void failed(TaskId taskId, JobStatus jobStatus) {
        logger_dev.debug(" ");
        if (jobStatus != JobStatus.KILLED) {
            InternalTask descriptor = tasks.get(taskId);
            if (descriptor.getStartTime() > 0) {
                descriptor.setFinishedTime(System.currentTimeMillis());
                setNumberOfFinishedTasks(getNumberOfFinishedTasks() + 1);
            }
            descriptor.setStatus((jobStatus == JobStatus.FAILED) ? TaskStatus.FAILED : TaskStatus.FAULTY);
            //terminate this job descriptor
            getJobDescriptor().failed();
        }
        //set the new status of the job
        setFinishedTime(System.currentTimeMillis());
        setNumberOfPendingTasks(0);
        setNumberOfRunningTasks(0);
        setStatus(jobStatus);

        //creating list of status
        HashMap<TaskId, TaskStatus> hts = new HashMap<TaskId, TaskStatus>();
        HashMap<TaskId, Long> htl = new HashMap<TaskId, Long>();

        for (InternalTask td : tasks.values()) {
            if (!td.getId().equals(taskId)) {
                if (td.getStatus() == TaskStatus.RUNNING) {
                    td.setStatus(TaskStatus.ABORTED);
                    td.setFinishedTime(System.currentTimeMillis());
                } else if (td.getStatus() == TaskStatus.WAITING_ON_ERROR ||
                    td.getStatus() == TaskStatus.WAITING_ON_FAILURE) {
                    td.setStatus(TaskStatus.NOT_RESTARTED);
                } else if (td.getStatus() != TaskStatus.FINISHED && td.getStatus() != TaskStatus.FAILED &&
                    td.getStatus() != TaskStatus.FAULTY) {
                    td.setStatus(TaskStatus.NOT_STARTED);
                }
            }

            htl.put(td.getId(), td.getFinishedTime());
            hts.put(td.getId(), td.getStatus());
        }

        setTaskStatusModify(hts);
        setTaskFinishedTimeModify(htl);

        if (jobDataSpaceApplication != null) {
            jobDataSpaceApplication.terminateDataSpaceApplication();
        }
    }

    /**
     * Get a task descriptor that is in the running task queue.
     * 
     * @param id the id of the task descriptor to retrieve.
     * @return the task descriptor associated to this id, or null if not running.
     */
    public TaskDescriptor getRunningTaskDescriptor(TaskId id) {
        return getJobDescriptor().GetRunningTaskDescriptor(id);
    }

    /**
     * Set all properties following a job submitting.
     */
    public void submitAction() {
        logger_dev.debug(" ");
        setSubmittedTime(System.currentTimeMillis());
        setStatus(JobStatus.PENDING);
    }

    /**
     * Prepare tasks in order to be ready to be scheduled.
     * The task may have a consistent id and job info.
     */
    public synchronized void prepareTasks() {
        logger_dev.debug(" ");
        //get tasks
        ArrayList<InternalTask> sorted = getITasks();
        //re-init taskId count
        TaskIdImpl.initialize();
        //sort task according to the ID
        Collections.sort(sorted);
        tasks.clear();
        for (InternalTask td : sorted) {
            TaskId newId = TaskIdImpl.nextId(getId(), td.getName());
            td.setId(newId);
            td.setJobInfo(getJobInfo());
            tasks.put(newId, td);
        }
    }

    /**
     * Set all properties in order to start the job.
     * After this method and for better performances you may have to
     * set the taskStatusModify to "null" : setTaskStatusModify(null);
     */
    public void start() {
        logger_dev.debug(" ");
        setStartTime(System.currentTimeMillis());
        setNumberOfPendingTasks(getTotalNumberOfTasks());
        setNumberOfRunningTasks(0);
        setStatus(JobStatus.RUNNING);

        HashMap<TaskId, TaskStatus> taskStatus = new HashMap<TaskId, TaskStatus>();

        for (InternalTask td : getITasks()) {
            td.setStatus(TaskStatus.PENDING);
            taskStatus.put(td.getId(), TaskStatus.PENDING);
        }

        setTaskStatusModify(taskStatus);
    }

    /**
     * Set all properties in order to terminate the job.
     */
    public void terminate() {
        logger_dev.debug(" ");
        setStatus(JobStatus.FINISHED);
        setFinishedTime(System.currentTimeMillis());
        if (jobDataSpaceApplication != null) {
            jobDataSpaceApplication.terminateDataSpaceApplication();
        }
    }

    /**
     * Paused every running and submitted tasks in this pending job.
     * After this method and for better performances you may have to
     * set the taskStatusModify to "null" : setTaskStatusModify(null);
     * 
     * @return true if the job has correctly been paused, false if not.
     */
    public boolean setPaused() {
        logger_dev.debug(" ");
        if (jobInfo.getStatus() == JobStatus.PAUSED) {
            return false;
        }

        jobInfo.setStatus(JobStatus.PAUSED);

        HashMap<TaskId, TaskStatus> hts = new HashMap<TaskId, TaskStatus>();

        for (InternalTask td : tasks.values()) {
            if ((td.getStatus() != TaskStatus.FINISHED) && (td.getStatus() != TaskStatus.RUNNING)) {
                td.setStatus(TaskStatus.PAUSED);
            }

            hts.put(td.getId(), td.getStatus());
        }

        getJobDescriptor().update(hts);
        setTaskStatusModify(hts);

        return true;
    }

    /**
     * Status of every paused tasks becomes pending or submitted in this pending job.
     * After this method and for better performances you may have to
     * set the taskStatusModify to "null" : setTaskStatusModify(null);
     * 
     * @return true if the job has correctly been unpaused, false if not.
     */
    public boolean setUnPause() {
        logger_dev.debug(" ");
        if (jobInfo.getStatus() != JobStatus.PAUSED) {
            return false;
        }

        if ((getNumberOfPendingTasks() + getNumberOfRunningTasks() + getNumberOfFinishedTasks()) == 0) {
            jobInfo.setStatus(JobStatus.PENDING);
        } else if (getNumberOfRunningTasks() == 0) {
            jobInfo.setStatus(JobStatus.STALLED);
        } else {
            jobInfo.setStatus(JobStatus.RUNNING);
        }

        HashMap<TaskId, TaskStatus> hts = new HashMap<TaskId, TaskStatus>();

        for (InternalTask td : tasks.values()) {
            if (jobInfo.getStatus() == JobStatus.PENDING) {
                td.setStatus(TaskStatus.SUBMITTED);
            } else if ((jobInfo.getStatus() == JobStatus.RUNNING) ||
                (jobInfo.getStatus() == JobStatus.STALLED)) {
                if ((td.getStatus() != TaskStatus.FINISHED) && (td.getStatus() != TaskStatus.RUNNING)) {
                    td.setStatus(TaskStatus.PENDING);
                }
            }

            hts.put(td.getId(), td.getStatus());
        }

        getJobDescriptor().update(hts);
        setTaskStatusModify(hts);

        return true;
    }

    /**
     * @see org.ow2.proactive.scheduler.common.job.Job#setPriority(org.ow2.proactive.scheduler.common.job.JobPriority)
     */
    @Override
    public void setPriority(JobPriority priority) {
        jobInfo.setPriority(priority);
        if (jobDescriptor != null) {
            ((JobDescriptorImpl) jobDescriptor).setPriority(priority);
        }
    }

    /**
     * @see org.ow2.proactive.scheduler.common.job.JobState#getTasks()
     */
    @Override
    public ArrayList<TaskState> getTasks() {
        return new ArrayList<TaskState>(tasks.values());
    }

    /**
     * @see org.ow2.proactive.scheduler.common.job.JobState#getHMTasks()
     */
    @Override
    public Map<TaskId, TaskState> getHMTasks() {
        Map<TaskId, TaskState> tmp = new HashMap<TaskId, TaskState>();
        for (Entry<TaskId, InternalTask> e : tasks.entrySet()) {
            tmp.put(e.getKey(), e.getValue());
        }
        return tmp;
    }

    /**
     * To get the tasks as an array list.
     *
     * @return the tasks
     */
    public ArrayList<InternalTask> getITasks() {
        return new ArrayList<InternalTask>(tasks.values());
    }

    /**
     * To get the tasks as a hash map.
     *
     * @return the tasks
     */
    public Map<TaskId, InternalTask> getIHMTasks() {
        return tasks;
    }

    /**
     * To set the taskStatusModify
     *
     * @param taskStatusModify the taskStatusModify to set
     */
    public void setTaskStatusModify(Map<TaskId, TaskStatus> taskStatusModify) {
        jobInfo.setTaskStatusModify(taskStatusModify);
    }

    /**
     * To set the taskFinishedTimeModify
     *
     * @param taskFinishedTimeModify the taskFinishedTimeModify to set
     */
    public void setTaskFinishedTimeModify(Map<TaskId, Long> taskFinishedTimeModify) {
        jobInfo.setTaskFinishedTimeModify(taskFinishedTimeModify);
    }

    /**
     * To set the id
     *
     * @param id the id to set
     */
    public void setId(JobId id) {
        jobInfo.setJobId(id);
    }

    /**
     * To set the finishedTime
     *
     * @param finishedTime
     *            the finishedTime to set
     */
    public void setFinishedTime(long finishedTime) {
        jobInfo.setFinishedTime(finishedTime);
    }

    /**
     * To set the startTime
     *
     * @param startTime
     *            the startTime to set
     */
    public void setStartTime(long startTime) {
        jobInfo.setStartTime(startTime);
    }

    /**
     * To set the submittedTime
     *
     * @param submittedTime
     *            the submittedTime to set
     */
    public void setSubmittedTime(long submittedTime) {
        jobInfo.setSubmittedTime(submittedTime);
    }

    /**
     * To set the removedTime
     *
     * @param removedTime
     *            the removedTime to set
     */
    public void setRemovedTime(long removedTime) {
        jobInfo.setRemovedTime(removedTime);
    }

    /**
     * To set the numberOfFinishedTasks
     *
     * @param numberOfFinishedTasks the numberOfFinishedTasks to set
     */
    public void setNumberOfFinishedTasks(int numberOfFinishedTasks) {
        jobInfo.setNumberOfFinishedTasks(numberOfFinishedTasks);
    }

    /**
     * To set the numberOfPendingTasks
     *
     * @param numberOfPendingTasks the numberOfPendingTasks to set
     */
    public void setNumberOfPendingTasks(int numberOfPendingTasks) {
        jobInfo.setNumberOfPendingTasks(numberOfPendingTasks);
    }

    /**
     * To set the numberOfRunningTasks
     *
     * @param numberOfRunningTasks the numberOfRunningTasks to set
     */
    public void setNumberOfRunningTasks(int numberOfRunningTasks) {
        jobInfo.setNumberOfRunningTasks(numberOfRunningTasks);
    }

    /**
     * To get the jobDescriptor
     *
     * @return the jobDescriptor
     */
    public JobDescriptorImpl getJobDescriptor() {
        if (jobDescriptor == null) {
            jobDescriptor = new JobDescriptorImpl(this);
        }
        return (JobDescriptorImpl) jobDescriptor;
    }

    /**
     * Set the job Descriptor
     *
     * @param jobD the JobDescriptor to set.
     */
    public void setJobDescriptor(JobDescriptor jobD) {
        this.jobDescriptor = jobD;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(JobStatus status) {
        jobInfo.setStatus(status);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.job.JobState#getOwner()
     */
    @Override
    public String getOwner() {
        return owner;
    }

    /**
     * To set the owner of this job.
     *
     * @param owner the owner to set.
     */
    public void setOwner(String owner) {
        this.owner = owner;
    }

    /**
     * @see org.ow2.proactive.scheduler.common.job.JobState#getJobResult()
     */
    public JobResult getJobResult() {
        return jobResult;
    }

    /**
     * Sets the jobResult to the given jobResult value.
     *
     * @param jobResult the jobResult to set.
     */
    public void setJobResult(JobResult jobResult) {
        this.jobResult = jobResult;
    }

    /**
     * Get the next restart waiting time in millis.
     * 
     * @return the next restart waiting time in millis.
     */
    public long getNextWaitingTime(int executionNumber) {
        if (executionNumber <= 0) {
            //execution number is 0 or less, restart with the minimal amount of time
            return restartWaitingTimer;
        } else if (executionNumber > 10) {
            //execution timer exceed 10, restart after 60 seconds
            return 60 * 1000;
        } else {
            //else restart according to this function
            return (getNextWaitingTime(executionNumber - 1) + executionNumber * 1000);
        }
    }

    /**
     * Set this job to the state toBeRemoved.
     */
    public void setToBeRemoved() {
        jobInfo.setToBeRemoved();
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof InternalJob) {
            return getId().equals(((InternalJob) o).getId());
        }

        return false;
    }

    /**
     * Get the jobDataSpaceApplication
     *
     * @return the jobDataSpaceApplication
     */
    public JobDataSpaceApplication getJobDataSpaceApplication() {
        return jobDataSpaceApplication;
    }

    //********************************************************************
    //************************* SERIALIZATION ****************************
    //********************************************************************

    /**
     * <b>IMPORTANT : </b><br />
     * Using hibernate does not allow to have java transient fields that is inserted in database anyway.<br />
     * Hibernate defined @Transient annotation meaning the field won't be inserted in database.
     * If the java transient modifier is set for a field, so the hibernate @Transient annotation becomes
     * useless and the field won't be inserted in DB anyway.<br />
     * For performance reason, some field must be java transient but not hibernate transient.
     * These fields are annotated with @TransientInSerialization.
     * The @TransientInSerialization describe the fields that won't be serialized by java since the two following
     * methods describe the serialization process.
     */

    private void writeObject(ObjectOutputStream out) throws IOException {
        try {
            Map<String, Object> toSerialize = new HashMap<String, Object>();
            Field[] fields = InternalJob.class.getDeclaredFields();
            for (Field f : fields) {
                if (!f.isAnnotationPresent(TransientInSerialization.class) &&
                    !Modifier.isStatic(f.getModifiers())) {
                    toSerialize.put(f.getName(), f.get(this));
                }
            }
            out.writeObject(toSerialize);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        try {
            Map<String, Object> map = (Map<String, Object>) in.readObject();
            for (Entry<String, Object> e : map.entrySet()) {
                InternalJob.class.getDeclaredField(e.getKey()).set(this, e.getValue());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
