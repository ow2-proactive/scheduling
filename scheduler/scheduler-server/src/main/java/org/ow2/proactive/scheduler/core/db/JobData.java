package org.ow2.proactive.scheduler.core.db;

import java.io.Serializable;
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

import org.hibernate.annotations.BatchSize;
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
import org.ow2.proactive.scheduler.common.task.OnTaskError;
import org.ow2.proactive.scheduler.common.usage.JobUsage;
import org.ow2.proactive.scheduler.common.usage.TaskUsage;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.job.InternalTaskFlowJob;
import org.ow2.proactive.scheduler.job.JobIdImpl;
import org.ow2.proactive.scheduler.job.JobInfoImpl;

import com.google.common.collect.Lists;


@Entity
@NamedQueries({ @NamedQuery(name = "checkJobExistence", query = "select id from JobData where id = :id"),
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
            "numberOfFailedTasks = :numberOfFailedTasks, " + "numberOfFaultyTasks = :numberOfFaultyTasks, " +
            "numberOfInErrorTasks = :numberOfInErrorTasks, " + "inErrorTime = :inErrorTime " +
            "where id = :jobId"),
        @NamedQuery(name = "updateJobDataRemovedTime", query = "update JobData set removedTime = :removedTime where id = :jobId"),
        @NamedQuery(name = "updateJobDataSetJobToBeRemoved", query = "update JobData set toBeRemoved = :toBeRemoved where id = :jobId"),
        @NamedQuery(name = "updateJobDataPriority", query = "update JobData set priority = :priority where id = :jobId"),
        @NamedQuery(name = "updateJobDataAfterTaskFinished", query = "update JobData set status = :status, " +
            "finishedTime = :finishedTime, numberOfPendingTasks = :numberOfPendingTasks, " +
            "numberOfFinishedTasks = :numberOfFinishedTasks, " +
            "numberOfRunningTasks = :numberOfRunningTasks, " +
            "numberOfFailedTasks = :numberOfFailedTasks, " + "numberOfFaultyTasks = :numberOfFaultyTasks, " +
            "numberOfInErrorTasks = :numberOfInErrorTasks " + " where id = :jobId"),
        @NamedQuery(name = "updateJobDataAfterWorkflowTaskFinished", query = "update JobData set status = :status, " +
            "finishedTime = :finishedTime, numberOfPendingTasks = :numberOfPendingTasks, " +
            "numberOfFinishedTasks = :numberOfFinishedTasks, " +
            "numberOfRunningTasks = :numberOfRunningTasks, " + "totalNumberOfTasks =:totalNumberOfTasks, " +
            "numberOfFailedTasks = :numberOfFailedTasks, " + "numberOfFaultyTasks = :numberOfFaultyTasks, " +
            "numberOfInErrorTasks = :numberOfInErrorTasks " + "where id = :jobId"),
        @NamedQuery(name = "updateJobDataTaskRestarted", query = "update JobData set status = :status, " +
            "numberOfPendingTasks = :numberOfPendingTasks, " +
            "numberOfRunningTasks = :numberOfRunningTasks, " +
            "numberOfFailedTasks = :numberOfFailedTasks, " + "numberOfFaultyTasks = :numberOfFaultyTasks, " +
            "numberOfInErrorTasks = :numberOfInErrorTasks " + "where id = :jobId"),
        @NamedQuery(name = "updateJobDataTaskStarted", query = "update JobData set status = :status, " +
            "startTime = :startTime, numberOfPendingTasks = :numberOfPendingTasks, " +
            "numberOfRunningTasks = :numberOfRunningTasks where id = :jobId") })
@Table(name = "JOB_DATA", indexes = { @Index(name = "JOB_DATA_FINISH_TIME", columnList = "FINISH_TIME"),
        @Index(name = "JOB_DATA_OWNER", columnList = "OWNER"),
        @Index(name = "JOB_DATA_REMOVE_TIME", columnList = "REMOVE_TIME"),
        @Index(name = "JOB_DATA_START_TIME", columnList = "START_TIME"),
        @Index(name = "JOB_DATA_STATUS", columnList = "STATUS"), })
public class JobData implements Serializable {

    private Long id;

    private List<TaskData> tasks;

    private Credentials credentials;

    private Map<String, String> genericInformation;

    private Map<String, String> variables;

    private String owner;

    private String jobName;

    private long submittedTime;

    private long startTime;

    private long inErrorTime;

    private long finishedTime;

    private long removedTime;

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
        if (isToBeRemoved()) {
            jobInfo.setToBeRemoved();
        }
        return jobInfo;
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
        internalJob.setVariables(getVariables());
        internalJob.setProjectName(getProjectName());
        internalJob.setOwner(getOwner());
        internalJob.setDescription(getDescription());
        internalJob.setInputSpace(getInputSpace());
        internalJob.setOutputSpace(getOutputSpace());
        internalJob.setGlobalSpace(getGlobalSpace());
        internalJob.setUserSpace(getGlobalSpace());
        internalJob.setMaxNumberOfExecution(getMaxNumberOfExecution());
        internalJob.setOnTaskError(OnTaskError.getInstance(this.onTaskErrorString));

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
        jobRuntimeData.setJobName(job.getName());
        jobRuntimeData.setDescription(job.getDescription());
        jobRuntimeData.setProjectName(job.getProjectName());
        jobRuntimeData.setInputSpace(job.getInputSpace());
        jobRuntimeData.setOutputSpace(job.getOutputSpace());
        jobRuntimeData.setGlobalSpace(job.getGlobalSpace());
        jobRuntimeData.setUserSpace(job.getUserSpace());
        jobRuntimeData.setGenericInformation(job.getGenericInformation());
        jobRuntimeData.setVariables(job.getVariables());
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

    @Column(name = "VARIABLES", length = Integer.MAX_VALUE)
    @Type(type = "org.hibernate.type.SerializableToBlobType", parameters = @Parameter(name = SerializableToBlobType.CLASS_NAME, value = "java.lang.Object"))
    public Map<String, String> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, String> variables) {
        this.variables = variables;
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

    @Column(name = "INPUT_SPACE", updatable = false)
    public String getInputSpace() {
        return inputSpace;
    }

    public void setInputSpace(String inputSpace) {
        this.inputSpace = inputSpace;
    }

    @Column(name = "OUT_SPACE", updatable = false)
    public String getOutputSpace() {
        return outputSpace;
    }

    public void setOutputSpace(String outputSpace) {
        this.outputSpace = outputSpace;
    }

    @Column(name = "GLOBAL_SPACE", updatable = false)
    public String getGlobalSpace() {
        return globalSpace;
    }

    public void setGlobalSpace(String globalSpace) {
        this.globalSpace = globalSpace;
    }

    @Column(name = "USER_SPACE", updatable = false)
    public String getUserSpace() {
        return userSpace;
    }

    public void setUserSpace(String userSpace) {
        this.userSpace = userSpace;
    }

    @Column(name = "DESCRIPTION", length = 1000, updatable = false)
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

    public void addJobContent(Job job) {
        JobContent content = new JobContent();
        content.setJobId(id);
        content.setJobData(this);
        content.setInitJobContent(job);
        getJobContent().add(content);
    }

    JobUsage toJobUsage() {
        JobIdImpl jobId = new JobIdImpl(getId(), getJobName());
        JobUsage jobUsage = new JobUsage(getOwner(), getProjectName(), jobId.value(), getJobName(),
            getFinishedTime() - getStartTime());
        for (TaskData taskData : getTasks()) {
            TaskUsage taskUsage = taskData.toTaskUsage(jobId);
            jobUsage.add(taskUsage);
        }
        return jobUsage;
    }
}
