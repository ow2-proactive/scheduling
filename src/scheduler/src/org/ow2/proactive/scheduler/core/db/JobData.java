package org.ow2.proactive.scheduler.core.db;

import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Index;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.hibernate.type.SerializableToBlobType;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.scheduler.common.job.JobEnvironment;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobPriority;
import org.ow2.proactive.scheduler.common.job.JobStatus;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.job.InternalTaskFlowJob;
import org.ow2.proactive.scheduler.job.JobIdImpl;
import org.ow2.proactive.scheduler.job.JobInfoImpl;


@Entity
@Table(name = "JOB_DATA")
public class JobData {

    private Long id;

    private List<TaskData> tasks;

    private Credentials credentials;

    private Map<String, String> genericInformation;

    private String[] classpath;

    private String owner;

    private String jobName;

    private long submittedTime;

    private long startTime;

    private long finishedTime;

    private long removedTime;

    private int totalNumberOfTasks;

    private int numberOfPendingTasks;

    private int numberOfRunningTasks;

    private int numberOfFinishedTasks;

    private int maxNumberOfExecution;

    private boolean cancelJobOnError;

    private JobPriority priority;

    private JobStatus status;

    private boolean toBeRemoved;

    private String inputSpace;

    private String outputSpace;

    private String description;

    private String projectName;

    private long classpathCrc;

    JobInfoImpl createJobInfo(JobId jobId) {
        JobInfoImpl jobInfo = new JobInfoImpl();
        jobInfo.setJobId(jobId);
        jobInfo.setStatus(getStatus());
        jobInfo.setTotalNumberOfTasks(getTotalNumberOfTasks());
        jobInfo.setNumberOfPendingTasks(getNumberOfPendingTasks());
        jobInfo.setNumberOfRunningTasks(getNumberOfRunningTasks());
        jobInfo.setNumberOfFinishedTasks(getNumberOfFinishedTasks());
        jobInfo.setPriority(getPriority());
        jobInfo.setRemovedTime(getRemovedTime());
        jobInfo.setStartTime(getStartTime());
        jobInfo.setFinishedTime(getFinishedTime());
        jobInfo.setSubmittedTime(getSubmittedTime());
        jobInfo.setRemovedTime(getRemovedTime());
        if (isToBeRemoved()) {
            jobInfo.setToBeRemoved();
        }
        return jobInfo;
    }

    InternalJob toInternalJob() {
        JobId jobIdInstance = new JobIdImpl(getId(), getJobName());

        JobInfoImpl jobInfo = createJobInfo(jobIdInstance);

        InternalJob internalJob = new InternalTaskFlowJob();
        internalJob.setCredentials(getCredentials());
        internalJob.setJobInfo(jobInfo);
        internalJob.setGenericInformations(getGenericInformation());
        internalJob.setProjectName(getProjectName());
        internalJob.setOwner(getOwner());
        internalJob.setDescription(getDescription());
        internalJob.setInputSpace(getInputSpace());
        internalJob.setOutputSpace(getOutputSpace());
        internalJob.setMaxNumberOfExecution(getMaxNumberOfExecution());
        internalJob.setCancelJobOnError(isCancelJobOnError());

        return internalJob;
    }

    static JobData createJobData(InternalJob job) {

        JobData jobRuntimeData = new JobData();
        jobRuntimeData.setMaxNumberOfExecution(job.getMaxNumberOfExecution());
        jobRuntimeData.setCancelJobOnError(job.isCancelJobOnError());
        jobRuntimeData.setSubmittedTime(System.currentTimeMillis());
        jobRuntimeData.setJobName(job.getName());
        jobRuntimeData.setDescription(job.getDescription());
        jobRuntimeData.setProjectName(job.getProjectName());
        jobRuntimeData.setInputSpace(job.getInputSpace());
        jobRuntimeData.setOutputSpace(job.getOutputSpace());
        jobRuntimeData.setGenericInformation(job.getGenericInformations());
        jobRuntimeData.setStatus(job.getStatus());
        jobRuntimeData.setOwner(job.getOwner());
        jobRuntimeData.setCredentials(job.getCredentials());
        jobRuntimeData.setPriority(job.getPriority());
        jobRuntimeData.setNumberOfPendingTasks(job.getNumberOfPendingTasks());
        jobRuntimeData.setNumberOfRunningTasks(job.getNumberOfRunningTasks());
        jobRuntimeData.setNumberOfFinishedTasks(job.getNumberOfFinishedTasks());
        jobRuntimeData.setTotalNumberOfTasks(job.getTotalNumberOfTasks());
        jobRuntimeData.setRemovedTime(job.getRemovedTime());

        JobEnvironment jobEnv = job.getEnvironment();
        if (jobEnv != null && jobEnv.getJobClasspath() != null) {
            jobRuntimeData.setClasspath(jobEnv.getJobClasspath());
            jobRuntimeData.setClasspathCrc(jobEnv.getJobClasspathCRC());
        }

        return jobRuntimeData;
    }

    @Column(name = "JOB_CLASSPATH")
    @Type(type = "org.hibernate.type.SerializableToBlobType", parameters = @Parameter(name = SerializableToBlobType.CLASS_NAME, value = "java.lang.Object"))
    public String[] getClasspath() {
        return classpath;
    }

    public void setClasspath(String[] classpath) {
        this.classpath = classpath;
    }

    @Column(name = "GENERIC_INFO")
    @Type(type = "org.hibernate.type.SerializableToBlobType", parameters = @Parameter(name = SerializableToBlobType.CLASS_NAME, value = "java.lang.Object"))
    public Map<String, String> getGenericInformation() {
        return genericInformation;
    }

    public void setGenericInformation(Map<String, String> genericInformation) {
        this.genericInformation = genericInformation;
    }

    @Column(updatable = false, name = "CLASSPATH_CRC")
    public long getClasspathCrc() {
        return classpathCrc;
    }

    public void setClasspathCrc(long classpathCrc) {
        this.classpathCrc = classpathCrc;
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

    @Column(name = "CANCEL_ON_ERROR", updatable = false, nullable = false)
    public boolean isCancelJobOnError() {
        return cancelJobOnError;
    }

    public void setCancelJobOnError(boolean cancelJobOnError) {
        this.cancelJobOnError = cancelJobOnError;
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

    @Column(name = "PRIORITY", nullable = false)
    public JobPriority getPriority() {
        return priority;
    }

    public void setPriority(JobPriority priority) {
        this.priority = priority;
    }

    @Column(name = "STATUS", nullable = false)
    @Index(name = "job_status")
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
    @Index(name = "job_owner_index")
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

}
