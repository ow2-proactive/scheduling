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
package org.ow2.proactive.scheduler.core.db;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Lob;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.apache.log4j.Logger;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.NamedQueries;
import org.hibernate.annotations.NamedQuery;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.hibernate.type.SerializableToBlobType;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.scheduler.common.job.Job;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobPriority;
import org.ow2.proactive.scheduler.common.job.JobStatus;
import org.ow2.proactive.scheduler.common.job.JobVariable;
import org.ow2.proactive.scheduler.common.task.OnTaskError;
import org.ow2.proactive.scheduler.common.task.util.SerializationUtil;
import org.ow2.proactive.scheduler.common.usage.JobUsage;
import org.ow2.proactive.scheduler.common.usage.TaskUsage;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.job.InternalTaskFlowJob;
import org.ow2.proactive.scheduler.job.JobIdImpl;
import org.ow2.proactive.scheduler.job.JobInfoImpl;

import com.google.common.collect.Lists;


@Entity
@NamedQueries({ @NamedQuery(name = "setJobForRemoval", query = "update JobData set scheduledTimeForRemoval = :timeForRemoval, toBeRemoved = :toBeRemoved where id = :jobId"),
                @NamedQuery(name = "deleteJobDataInBulk", query = "delete from JobData where id in (:jobIdList)"),
                @NamedQuery(name = "checkJobExistence", query = "select id from JobData where id = :id"),
                @NamedQuery(name = "countJobDataFinished", query = "select count (*) from JobData where status = 3"),
                @NamedQuery(name = "countJobData", query = "select count (*) from JobData"),
                @NamedQuery(name = "deleteJobData", query = "delete from JobData where id = :jobId"),
                @NamedQuery(name = "findUsersWithJobs", query = "select owner, count(owner), max(submittedTime) from JobData group by owner"),
                @NamedQuery(name = "getJobsNumberWithStatus", query = "select count(*) from JobData where status in (:status) and removedTime = -1"),
                @NamedQuery(name = "getJobSubmittedTime", query = "select submittedTime from JobData where id = :id"),
                @NamedQuery(name = "getMeanJobExecutionTime", query = "select avg(finishedTime - startTime) from JobData where startTime > 0 and finishedTime > 0"),
                @NamedQuery(name = "getMeanJobPendingTime", query = "select avg(startTime - submittedTime) from JobData where startTime > 0 and submittedTime > 0"),
                @NamedQuery(name = "getMeanJobSubmittingPeriod", query = "select count(*), min(submittedTime), max(submittedTime) from JobData"),
                @NamedQuery(name = "getTotalJobsCount", query = "select count(*) from JobData where removedTime = -1"),
                @NamedQuery(name = "loadInternalJobs", query = "from JobData as job where job.id in (:ids)"),
                @NamedQuery(name = "loadJobs", query = "select id from JobData where status in (:status) and removedTime = -1"),
                @NamedQuery(name = "loadJobsWithPeriod", query = "select id from JobData where status in (:status) and removedTime = -1 and submittedTime >= :minSubmittedTime"),
                @NamedQuery(name = "loadJobDataIfNotRemoved", query = "from JobData as job where job.id in (:ids) and job.removedTime = -1"),
                @NamedQuery(name = "readAccountJobs", query = "select count(*), sum(finishedTime) - sum(startTime) from JobData" +
                                                              " where owner = :username and finishedTime > 0"),
                @NamedQuery(name = "updateJobAndTasksState", query = "update JobData set status = :status, " +
                                                                     "numberOfFailedTasks = :numberOfFailedTasks, numberOfFaultyTasks = :numberOfFaultyTasks, " +
                                                                     "numberOfInErrorTasks = :numberOfInErrorTasks, inErrorTime = :inErrorTime, lastUpdatedTime = :lastUpdatedTime " +
                                                                     "where id = :jobId"),
                @NamedQuery(name = "updateJobDataRemovedTime", query = "update JobData set removedTime = :removedTime, lastUpdatedTime = :lastUpdatedTime where id = :jobId"),
                @NamedQuery(name = "updateJobDataRemovedTimeInBulk", query = "update JobData set removedTime = :removedTime, lastUpdatedTime = :lastUpdatedTime where id in :jobIdList"),
                @NamedQuery(name = "updateJobDataSetJobToBeRemoved", query = "update JobData set toBeRemoved = :toBeRemoved, lastUpdatedTime = :lastUpdatedTime where id = :jobId"),
                @NamedQuery(name = "updateJobDataPriority", query = "update JobData set priority = :priority, lastUpdatedTime = :lastUpdatedTime where id = :jobId"),
                @NamedQuery(name = "updateJobDataAfterTaskFinished", query = "update JobData set status = :status, " +
                                                                             "finishedTime = :finishedTime, numberOfPendingTasks = :numberOfPendingTasks, " +
                                                                             "numberOfFinishedTasks = :numberOfFinishedTasks, " +
                                                                             "numberOfRunningTasks = :numberOfRunningTasks, " +
                                                                             "numberOfFailedTasks = :numberOfFailedTasks, numberOfFaultyTasks = :numberOfFaultyTasks, " +
                                                                             "numberOfInErrorTasks = :numberOfInErrorTasks, lastUpdatedTime = :lastUpdatedTime, resultMap = :resultMap where id = :jobId"),
                @NamedQuery(name = "updateJobDataAfterWorkflowTaskFinished", query = "update JobData set status = :status, " +
                                                                                     "finishedTime = :finishedTime, numberOfPendingTasks = :numberOfPendingTasks, " +
                                                                                     "numberOfFinishedTasks = :numberOfFinishedTasks, " +
                                                                                     "numberOfRunningTasks = :numberOfRunningTasks, totalNumberOfTasks =:totalNumberOfTasks, " +
                                                                                     "numberOfFailedTasks = :numberOfFailedTasks, numberOfFaultyTasks = :numberOfFaultyTasks, " +
                                                                                     "numberOfInErrorTasks = :numberOfInErrorTasks, lastUpdatedTime = :lastUpdatedTime, resultMap = :resultMap " +
                                                                                     "where id = :jobId"),
                @NamedQuery(name = "updateJobDataTaskRestarted", query = "update JobData set status = :status, " +
                                                                         "numberOfPendingTasks = :numberOfPendingTasks, " +
                                                                         "numberOfRunningTasks = :numberOfRunningTasks, " +
                                                                         "numberOfFailedTasks = :numberOfFailedTasks, numberOfFaultyTasks = :numberOfFaultyTasks, " +
                                                                         "numberOfInErrorTasks = :numberOfInErrorTasks, lastUpdatedTime = :lastUpdatedTime where id = :jobId"),
                @NamedQuery(name = "updateJobDataTaskStarted", query = "update JobData set status = :status, " +
                                                                       "startTime = :startTime, numberOfPendingTasks = :numberOfPendingTasks, " +
                                                                       "numberOfRunningTasks = :numberOfRunningTasks, lastUpdatedTime = :lastUpdatedTime where id = :jobId") })
@Table(name = "JOB_DATA", indexes = { @Index(name = "JOB_DATA_FINISH_TIME", columnList = "FINISH_TIME"),
                                      @Index(name = "JOB_DATA_OWNER", columnList = "OWNER"),
                                      @Index(name = "JOB_DATA_REMOVE_TIME", columnList = "REMOVE_TIME"),
                                      @Index(name = "JOB_DATA_START_TIME", columnList = "START_TIME"),
                                      @Index(name = "JOB_DATA_STATUS", columnList = "STATUS"), })
public class JobData implements Serializable {

    private static final Logger logger = Logger.getLogger(JobData.class);

    private Long id;

    private List<TaskData> tasks;

    private Credentials credentials;

    private Map<String, String> genericInformation;

    private Map<String, JobDataVariable> variables;

    private Map<String, byte[]> resultMap;

    private String owner;

    private String jobName;

    private long submittedTime;

    private long startTime;

    private long inErrorTime;

    private long finishedTime;

    private long removedTime;

    private long scheduledTimeForRemoval;

    private int totalNumberOfTasks;

    private int numberOfPendingTasks;

    private int numberOfRunningTasks;

    private int numberOfFinishedTasks;

    /*
     * The next three fields are there to prevent expensive queries in order to display the number
     * of "Issues".
     */
    private int numberOfFailedTasks;

    private int numberOfFaultyTasks;

    private int numberOfInErrorTasks;

    private int maxNumberOfExecution;

    private String onTaskErrorString;

    private JobPriority priority;

    private JobStatus status;

    private boolean toBeRemoved;

    private String inputSpace;

    private String outputSpace;

    private String globalSpace;

    private String userSpace;

    private String description;

    private String projectName;

    private List<JobContent> jobContent = Lists.newArrayList();

    private long lastUpdatedTime;

    JobInfoImpl createJobInfo(JobId jobId) {
        JobInfoImpl jobInfo = new JobInfoImpl();
        jobInfo.setJobId(jobId);
        jobInfo.setJobOwner(getOwner());
        jobInfo.setStatus(getStatus());
        jobInfo.setTotalNumberOfTasks(getTotalNumberOfTasks());
        jobInfo.setNumberOfPendingTasks(getNumberOfPendingTasks());
        jobInfo.setNumberOfRunningTasks(getNumberOfRunningTasks());
        jobInfo.setNumberOfFinishedTasks(getNumberOfFinishedTasks());
        jobInfo.setNumberOfFailedTasks(getNumberOfFailedTasks());
        jobInfo.setNumberOfFaultyTasks(getNumberOfFaultyTasks());
        jobInfo.setNumberOfInErrorTasks(getNumberOfInErrorTasks());
        jobInfo.setPriority(getPriority());
        jobInfo.setRemovedTime(getRemovedTime());
        jobInfo.setStartTime(getStartTime());
        jobInfo.setInErrorTime(getInErrorTime());
        jobInfo.setFinishedTime(getFinishedTime());
        jobInfo.setSubmittedTime(getSubmittedTime());
        jobInfo.setRemovedTime(getRemovedTime());
        jobInfo.setLastUpdatedTime(getLastUpdatedTime());
        if (isToBeRemoved()) {
            jobInfo.setToBeRemoved();
        }
        jobInfo.setGenericInformation(getGenericInformation());
        jobInfo.setVariables(createVariablesStringMap());
        return jobInfo;
    }

    private Map<String, String> createVariablesStringMap() {
        Map<String, JobDataVariable> jobDataVariablesMap = getVariables();
        Map<String, String> stringVariablesMap = new HashMap<>(jobDataVariablesMap.size());
        for (JobDataVariable variable : getVariables().values()) {
            stringVariablesMap.put(variable.getName(), variable.getValue());
        }
        return stringVariablesMap;
    }

    JobInfo toJobInfo() {
        JobId jobIdInstance = new JobIdImpl(getId(), getJobName());
        JobInfoImpl jobInfo = createJobInfo(jobIdInstance);
        return jobInfo;
    }

    InternalJob toInternalJob() {
        JobId jobIdInstance = new JobIdImpl(getId(), getJobName());

        JobInfoImpl jobInfo = createJobInfo(jobIdInstance);

        InternalJob internalJob = new InternalTaskFlowJob();
        internalJob.setCredentials(getCredentials());
        internalJob.setJobInfo(jobInfo);
        internalJob.setGenericInformation(getGenericInformation());
        internalJob.setVariables(variablesToJobVariables());
        internalJob.setProjectName(getProjectName());
        internalJob.setOwner(getOwner());
        internalJob.setDescription(getDescription());
        internalJob.setInputSpace(getInputSpace());
        internalJob.setOutputSpace(getOutputSpace());
        internalJob.setGlobalSpace(getGlobalSpace());
        internalJob.setUserSpace(getGlobalSpace());
        internalJob.setMaxNumberOfExecution(getMaxNumberOfExecution());
        internalJob.setOnTaskError(OnTaskError.getInstance(this.onTaskErrorString));
        internalJob.setScheduledTimeForRemoval(getScheduledTimeForRemoval());
        try {
            internalJob.setResultMap(SerializationUtil.deserializeVariableMap(getResultMap()));
        } catch (IOException | ClassNotFoundException e) {
            logger.error("error when serializing result map variables " + e);
        }
        return internalJob;
    }

    static JobData createJobData(InternalJob job) {

        JobData jobRuntimeData = new JobData();
        jobRuntimeData.setMaxNumberOfExecution(job.getMaxNumberOfExecution());
        jobRuntimeData.setOnTaskErrorString(job.getOnTaskErrorProperty().getValue());
        jobRuntimeData.setSubmittedTime(job.getSubmittedTime());
        jobRuntimeData.setStartTime(job.getStartTime());
        jobRuntimeData.setInErrorTime(job.getInErrorTime());
        jobRuntimeData.setFinishedTime(job.getFinishedTime());
        jobRuntimeData.setRemovedTime(job.getRemovedTime());
        jobRuntimeData.setScheduledTimeForRemoval(job.getScheduledTimeForRemoval());
        jobRuntimeData.setJobName(job.getName());
        jobRuntimeData.setDescription(job.getDescription());
        jobRuntimeData.setProjectName(job.getProjectName());
        jobRuntimeData.setInputSpace(job.getInputSpace());
        jobRuntimeData.setOutputSpace(job.getOutputSpace());
        jobRuntimeData.setGlobalSpace(job.getGlobalSpace());
        jobRuntimeData.setUserSpace(job.getUserSpace());
        jobRuntimeData.setGenericInformation(job.getGenericInformation());
        Map<String, JobDataVariable> variables = new HashMap<>();
        for (Map.Entry<String, JobVariable> entry : job.getVariables().entrySet()) {
            variables.put(entry.getKey(), JobDataVariable.create(entry.getKey(), entry.getValue(), jobRuntimeData));
        }
        jobRuntimeData.setVariables(variables);
        jobRuntimeData.setStatus(job.getStatus());
        jobRuntimeData.setOwner(job.getOwner());
        jobRuntimeData.setCredentials(job.getCredentials());
        jobRuntimeData.setPriority(job.getPriority());
        jobRuntimeData.setNumberOfPendingTasks(job.getNumberOfPendingTasks());
        jobRuntimeData.setNumberOfRunningTasks(job.getNumberOfRunningTasks());
        jobRuntimeData.setNumberOfFinishedTasks(job.getNumberOfFinishedTasks());
        jobRuntimeData.setNumberOfFailedTasks(job.getNumberOfFailedTasks());
        jobRuntimeData.setNumberOfFaultyTasks(job.getNumberOfFaultyTasks());
        jobRuntimeData.setNumberOfInErrorTasks(job.getNumberOfInErrorTasks());
        jobRuntimeData.setTotalNumberOfTasks(job.getTotalNumberOfTasks());
        jobRuntimeData.addJobContent(job.getTaskFlowJob());
        jobRuntimeData.setLastUpdatedTime(job.getSubmittedTime());
        jobRuntimeData.setResultMap(SerializationUtil.serializeVariableMap(job.getResultMap()));

        return jobRuntimeData;
    }

    @Column(name = "GENERIC_INFO", length = Integer.MAX_VALUE)
    @Type(type = "org.hibernate.type.SerializableToBlobType", parameters = @Parameter(name = SerializableToBlobType.CLASS_NAME, value = "java.lang.Object"))
    public Map<String, String> getGenericInformation() {
        return genericInformation;
    }

    public void setGenericInformation(Map<String, String> genericInformation) {
        this.genericInformation = genericInformation;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "JOBID_SEQUENCE")
    @SequenceGenerator(name = "JOBID_SEQUENCE", sequenceName = "JOBID_SEQUENCE")
    @Column(name = "ID")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Cascade(org.hibernate.annotations.CascadeType.ALL)
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "jobData")
    @OnDelete(action = OnDeleteAction.CASCADE)
    public Map<String, JobDataVariable> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, JobDataVariable> variables) {
        this.variables = variables;
    }

    @Column(name = "RESULT_MAP", length = Integer.MAX_VALUE)
    @Type(type = "org.hibernate.type.SerializableToBlobType", parameters = @Parameter(name = SerializableToBlobType.CLASS_NAME, value = "java.lang.Object"))
    public Map<String, byte[]> getResultMap() {
        return resultMap;
    }

    public void setResultMap(Map<String, byte[]> resultMap) {
        this.resultMap = resultMap;
    }

    @Column(name = "MAX_NUMBER_OF_EXEC", updatable = false, nullable = false)
    public int getMaxNumberOfExecution() {
        return maxNumberOfExecution;
    }

    public void setMaxNumberOfExecution(int maxNumberOfExecution) {
        this.maxNumberOfExecution = maxNumberOfExecution;
    }

    @Column(name = "ON_TASK_ERROR", updatable = false, nullable = false, length = 25)
    public String getOnTaskErrorString() {
        return onTaskErrorString;
    }

    public void setOnTaskErrorString(OnTaskError onTaskError) {
        this.onTaskErrorString = onTaskError.toString();
    }

    public void setOnTaskErrorString(String onTaskError) {
        this.onTaskErrorString = onTaskError;
    }

    @Column(name = "JOB_NAME", nullable = false, updatable = false)
    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    @Lob
    @Column(name = "INPUT_SPACE", length = Integer.MAX_VALUE, updatable = false)
    @Type(type = "org.hibernate.type.MaterializedClobType")
    public String getInputSpace() {
        return inputSpace;
    }

    public void setInputSpace(String inputSpace) {
        this.inputSpace = inputSpace;
    }

    @Lob
    @Column(name = "OUT_SPACE", length = Integer.MAX_VALUE, updatable = false)
    @Type(type = "org.hibernate.type.MaterializedClobType")
    public String getOutputSpace() {
        return outputSpace;
    }

    public void setOutputSpace(String outputSpace) {
        this.outputSpace = outputSpace;
    }

    @Lob
    @Column(name = "GLOBAL_SPACE", length = Integer.MAX_VALUE, updatable = false)
    @Type(type = "org.hibernate.type.MaterializedClobType")
    public String getGlobalSpace() {
        return globalSpace;
    }

    public void setGlobalSpace(String globalSpace) {
        this.globalSpace = globalSpace;
    }

    @Lob
    @Column(name = "USER_SPACE", length = Integer.MAX_VALUE, updatable = false)
    @Type(type = "org.hibernate.type.MaterializedClobType")
    public String getUserSpace() {
        return userSpace;
    }

    public void setUserSpace(String userSpace) {
        this.userSpace = userSpace;
    }

    @Lob
    @Column(name = "DESCRIPTION", length = Integer.MAX_VALUE, updatable = false)
    @Type(type = "org.hibernate.type.MaterializedClobType")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Column(name = "PROJECT_NAME", updatable = false)
    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    @OneToMany(mappedBy = "jobData", fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    public List<TaskData> getTasks() {
        return tasks;
    }

    public void setTasks(List<TaskData> tasks) {
        this.tasks = tasks;
    }

    @Column(name = "SUBMIT_TIME")
    public long getSubmittedTime() {
        return submittedTime;
    }

    public void setSubmittedTime(long submittedTime) {
        this.submittedTime = submittedTime;
    }

    @Column(name = "START_TIME")
    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    @Column(name = "IN_ERROR_TIME")
    public long getInErrorTime() {
        return inErrorTime;
    }

    public void setInErrorTime(long inErrorTime) {
        this.inErrorTime = inErrorTime;
    }

    @Column(name = "FINISH_TIME")
    public long getFinishedTime() {
        return finishedTime;
    }

    public void setFinishedTime(long finishedTime) {
        this.finishedTime = finishedTime;
    }

    @Column(name = "REMOVE_TIME")
    public long getRemovedTime() {
        return removedTime;
    }

    public void setRemovedTime(long removedTime) {
        this.removedTime = removedTime;
    }

    @Column(name = "SCHEDULED_TIME_FOR_REMOVAL", nullable = false)
    public long getScheduledTimeForRemoval() {
        return scheduledTimeForRemoval;
    }

    public void setScheduledTimeForRemoval(long scheduledTimeForRemoval) {
        this.scheduledTimeForRemoval = scheduledTimeForRemoval;
    }

    @Column(name = "TOTAL_TASKS")
    public int getTotalNumberOfTasks() {
        return totalNumberOfTasks;
    }

    public void setTotalNumberOfTasks(int totalNumberOfTasks) {
        this.totalNumberOfTasks = totalNumberOfTasks;
    }

    @Column(name = "PENDING_TASKS")
    public int getNumberOfPendingTasks() {
        return numberOfPendingTasks;
    }

    public void setNumberOfPendingTasks(int numberOfPendingTasks) {
        this.numberOfPendingTasks = numberOfPendingTasks;
    }

    @Column(name = "RUNNING_TASKS")
    public int getNumberOfRunningTasks() {
        return numberOfRunningTasks;
    }

    public void setNumberOfRunningTasks(int numberOfRunningTasks) {
        this.numberOfRunningTasks = numberOfRunningTasks;
    }

    @Column(name = "FINISHED_TASKS")
    public int getNumberOfFinishedTasks() {
        return numberOfFinishedTasks;
    }

    public void setNumberOfFinishedTasks(int numberOfFinishedTasks) {
        this.numberOfFinishedTasks = numberOfFinishedTasks;
    }

    @Column(name = "FAILED_TASKS")
    public int getNumberOfFailedTasks() {
        return numberOfFailedTasks;
    }

    public void setNumberOfFailedTasks(int numberOfFailedTasks) {
        this.numberOfFailedTasks = numberOfFailedTasks;
    }

    @Column(name = "FAULTY_TASKS")
    public int getNumberOfFaultyTasks() {
        return numberOfFaultyTasks;
    }

    public void setNumberOfFaultyTasks(int numberOfFaultyTasks) {
        this.numberOfFaultyTasks = numberOfFaultyTasks;
    }

    @Column(name = "IN_ERROR_TASKS")
    public int getNumberOfInErrorTasks() {
        return numberOfInErrorTasks;
    }

    public void setNumberOfInErrorTasks(int numberOfInErrorTasks) {
        this.numberOfInErrorTasks = numberOfInErrorTasks;
    }

    @Column(name = "PRIORITY", nullable = false)
    public JobPriority getPriority() {
        return priority;
    }

    public void setPriority(JobPriority priority) {
        this.priority = priority;
    }

    @Column(name = "STATUS", nullable = false)
    public JobStatus getStatus() {
        return status;
    }

    public void setStatus(JobStatus status) {
        this.status = status;
    }

    @Column(name = "TO_BE_REMOVED")
    public boolean isToBeRemoved() {
        return toBeRemoved;
    }

    public void setToBeRemoved(boolean toBeRemoved) {
        this.toBeRemoved = toBeRemoved;
    }

    @Column(name = "OWNER", nullable = false)
    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    @Lob
    @Column(name = "CREDENTIALS", length = Integer.MAX_VALUE)
    public Credentials getCredentials() {
        return credentials;
    }

    public void setCredentials(Credentials credentials) {
        this.credentials = credentials;
    }

    // NOTE: the jobData and jobContent is basically a one to one association,
    // but hibernate doesn't support lazy fetch mode in an one to one
    // association. Consider about the application performance, the jobContent
    // should not be loaded along with the jobData every time jobData needed, so
    // the workaround is to make the association as one to many
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "jobData")
    @Fetch(FetchMode.SELECT)
    @BatchSize(size = 10)
    @MapKey(name = "jobId")
    @PrimaryKeyJoinColumn(name = "JOB_ID")
    public List<JobContent> getJobContent() {
        return jobContent;
    }

    public void setJobContent(List<JobContent> jobContent) {
        this.jobContent = jobContent;
    }

    @Column(name = "LAST_UPDATED_TIME")
    public long getLastUpdatedTime() {
        return lastUpdatedTime;
    }

    public void setLastUpdatedTime(long lastUpdatedTime) {
        this.lastUpdatedTime = lastUpdatedTime;
    }

    public void addJobContent(Job job) {
        JobContent content = new JobContent();
        content.setJobId(id);
        content.setJobData(this);
        content.setInitJobContent(job.getJobContent());
        getJobContent().add(content);
    }

    JobUsage toJobUsage() {
        JobIdImpl jobId = new JobIdImpl(getId(), getJobName());
        JobUsage jobUsage = new JobUsage(getOwner(),
                                         getProjectName(),
                                         jobId.value(),
                                         getJobName(),
                                         getFinishedTime() - getStartTime());
        for (TaskData taskData : getTasks()) {
            TaskUsage taskUsage = taskData.toTaskUsage(jobId);
            jobUsage.add(taskUsage);
        }
        return jobUsage;
    }

    private Map<String, JobVariable> variablesToJobVariables() {
        Map<String, JobVariable> jobVariables = new HashMap<>();
        for (JobDataVariable variable : getVariables().values()) {
            jobVariables.put(variable.getName(), jobDataVariableToTaskVariable(variable));
        }
        return jobVariables;
    }

    private static JobVariable jobDataVariableToTaskVariable(JobDataVariable jobDataVariable) {
        if (jobDataVariable == null) {
            return null;
        }

        JobVariable jobVariable = new JobVariable();
        jobVariable.setModel(jobDataVariable.getModel());
        jobVariable.setValue(jobDataVariable.getValue());
        jobVariable.setName(jobDataVariable.getName());

        return jobVariable;
    }
}
