package org.ow2.proactive.scheduler.core.db;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.hibernate.type.SerializableToBlobType;
import org.ow2.proactive.scheduler.common.task.ForkEnvironment;
import org.ow2.proactive.scheduler.common.task.ParallelEnvironment;
import org.ow2.proactive.scheduler.common.task.PropertyModifier;
import org.ow2.proactive.scheduler.common.task.RestartMode;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskInfo;
import org.ow2.proactive.scheduler.common.task.TaskState;
import org.ow2.proactive.scheduler.common.task.TaskStatus;
import org.ow2.proactive.scheduler.common.task.dataspaces.InputSelector;
import org.ow2.proactive.scheduler.common.task.dataspaces.OutputSelector;
import org.ow2.proactive.scheduler.common.task.flow.FlowBlock;
import org.ow2.proactive.scheduler.common.usage.TaskUsage;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.job.JobIdImpl;
import org.ow2.proactive.scheduler.task.TaskIdImpl;
import org.ow2.proactive.scheduler.task.TaskInfoImpl;
import org.ow2.proactive.scheduler.task.TaskStateImpl;
import org.ow2.proactive.scheduler.task.containers.ExecutableContainer;
import org.ow2.proactive.scheduler.task.containers.ScriptExecutableContainer;
import org.ow2.proactive.scheduler.task.internal.InternalForkedScriptTask;
import org.ow2.proactive.scheduler.task.internal.InternalScriptTask;
import org.ow2.proactive.scheduler.task.internal.InternalTask;
import org.ow2.proactive.scripting.InvalidScriptException;
import org.ow2.proactive.scripting.SelectionScript;
import org.ow2.proactive.scripting.TaskScript;
import org.ow2.proactive.topology.descriptor.ArbitraryTopologyDescriptor;
import org.ow2.proactive.topology.descriptor.BestProximityDescriptor;
import org.ow2.proactive.topology.descriptor.DifferentHostsExclusiveDescriptor;
import org.ow2.proactive.topology.descriptor.MultipleHostsExclusiveDescriptor;
import org.ow2.proactive.topology.descriptor.SingleHostDescriptor;
import org.ow2.proactive.topology.descriptor.SingleHostExclusiveDescriptor;
import org.ow2.proactive.topology.descriptor.ThresholdProximityDescriptor;
import org.ow2.proactive.topology.descriptor.TopologyDescriptor;


@Entity
@Table(name = "TASK_DATA"/*, uniqueConstraints = { @UniqueConstraint(columnNames = {
		"TASK_ID_JOB", "TASK_NAME" }) }*/)
public class TaskData {

    private static final String SCRIPT_TASK = "SCRIPT_TASK";

    private static final String FORKED_SCRIPT_TASK = "FORKED_SCRIPT_TASK";

    private DBTaskId id;

    private JobData jobData;

    private List<DBTaskId> dependentTasks;

    private List<DBTaskId> joinedBranches;

    private TaskData ifBranch;

    private Map<String, String> genericInformation;

    private List<SelectionScriptData> selectionScripts;

    private List<SelectorData> dataspaceSelectors;

    private ScriptData preScript;

    private ScriptData postScript;

    private ScriptData cleanScript;

    private ScriptData flowScript;

    private ScriptData script;

    private String taskName;

    private String tag;

    private String description;

    private long startTime;

    private long finishedTime;

    private long executionDuration;

    private TaskStatus taskStatus;

    private String executionHostName;

    private int maxNumberOfExecution;

    private boolean cancelJobOnError;

    private int numberOfExecutionLeft;

    private int numberOfExecutionOnFailureLeft;

    private String resultPreview;

    private boolean preciousResult;

    private boolean preciousLogs;

    private boolean runAsMe;

    private long wallTime;

    private int iteration;

    private int replication;

    private String matchingBlock;

    private String taskType;

    private int restartModeId;

    private FlowBlock flowBlock;

    private int parallelEnvNodesNumber;

    private String topologyDescriptor;

    private long topologyDescriptorThreshold;

    /* Fork environment parameters */

    private String javaHome;
    private List<String> jvmArguments;
    private List<String> additionalClasspath;
    private ScriptData envScript;
    private List<EnvironmentModifierData> envModifiers;
    private String workingDir;

    @Column(name = "JAVA_HOME", length = Integer.MAX_VALUE)
    @Lob
    public String getJavaHome() {
        return javaHome;
    }

    public void setJavaHome(String javaHome) {
        this.javaHome = javaHome;
    }

    @Column(name = "JVM_ARGUMENTS")
    @Type(type = "org.hibernate.type.SerializableToBlobType", parameters = @org.hibernate.annotations.Parameter(name = SerializableToBlobType.CLASS_NAME, value = "java.lang.Object"))
    public List<String> getJvmArguments() {
        return jvmArguments;
    }

    public void setJvmArguments(List<String> jvmArguments) {
        this.jvmArguments = jvmArguments;
    }

    @Column(name = "CLASSPATH")
    @Type(type = "org.hibernate.type.SerializableToBlobType", parameters = @org.hibernate.annotations.Parameter(name = SerializableToBlobType.CLASS_NAME, value = "java.lang.Object"))
    public List<String> getAdditionalClasspath() {
        return additionalClasspath;
    }

    public void setAdditionalClasspath(List<String> additionalClasspath) {
        this.additionalClasspath = additionalClasspath;
    }

    @Cascade(org.hibernate.annotations.CascadeType.ALL)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ENV_SCRIPT_ID")
    @ForeignKey(name = "none") // disable foreign key, to be able to remove runtime data
    @Index(name = "TASK_ENV_SCRIPT")
    public ScriptData getEnvScript() {
        return envScript;
    }

    public void setEnvScript(ScriptData envScript) {
        this.envScript = envScript;
    }

    @Cascade(org.hibernate.annotations.CascadeType.ALL)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SCRIPT_ID")
    @ForeignKey(name = "none") // disable foreign key, to be able to remove runtime data
    @Index(name = "TASK_SCRIPT")
    public ScriptData getScript() {
        return script;
    }

    public void setScript(ScriptData script) {
        this.script = script;
    }

    @Cascade(org.hibernate.annotations.CascadeType.ALL)
    @OneToMany(mappedBy = "taskData")
    @OnDelete(action = OnDeleteAction.CASCADE)
    public List<EnvironmentModifierData> getEnvModifiers() {
        return envModifiers;
    }

    public void setEnvModifiers(List<EnvironmentModifierData> envModifiers) {
        this.envModifiers = envModifiers;
    }

    @Column(name = "WORKING_DIR")
    public String getWorkingDir() {
        return workingDir;
    }

    public void setWorkingDir(String workingDir) {
        this.workingDir = workingDir;
    }

    public ForkEnvironment createForkEnvironment() throws InvalidScriptException {
        ForkEnvironment forkEnv = new ForkEnvironment();
        forkEnv.setJavaHome(javaHome);
        forkEnv.setWorkingDir(workingDir);

        List<String> additionalClasspath = getAdditionalClasspath();
        if (additionalClasspath != null) {
            for (String classpath : additionalClasspath) {
                forkEnv.addAdditionalClasspath(classpath);
            }
        }

        List<String> jvmArguments = getJvmArguments();
        if (jvmArguments != null) {
            for (String jvmArg : jvmArguments) {
                forkEnv.addJVMArgument(jvmArg);
            }
        }

        List<EnvironmentModifierData> envModifiers = getEnvModifiers();

        if (envModifiers != null) {
            for (EnvironmentModifierData envModifier : envModifiers) {
                forkEnv.addSystemEnvironmentVariable(envModifier.getName(), envModifier.getValue());
            }
        }
        if (envScript != null) {
            forkEnv.setEnvScript(envScript.createSimpleScript());
        }
        return forkEnv;
    }

    public ExecutableContainer createExecutableContainer() throws InvalidScriptException {
        return new ScriptExecutableContainer(new TaskScript(script.createSimpleScript()));
    }

    @Embeddable
    public static class DBTaskId implements Serializable {

        private long jobId;

        private long taskId;

        @Column(name = "TASK_ID_JOB")
        public long getJobId() {
            return jobId;
        }

        public void setJobId(long jobId) {
            this.jobId = jobId;
        }

        @Column(name = "TASK_ID_TASK")
        public long getTaskId() {
            return taskId;
        }

        public void setTaskId(long taskId) {
            this.taskId = taskId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            DBTaskId dbTaskId = (DBTaskId) o;

            if (jobId != dbTaskId.jobId) return false;
            return taskId == dbTaskId.taskId;
        }

        @Override
        public int hashCode() {
            int result = (int) (jobId ^ (jobId >>> 32));
            result = 31 * result + (int) (taskId ^ (taskId >>> 32));
            return result;
        }
    }

    private static final Map<Class<? extends TopologyDescriptor>, String> topologyDescMapping;

    static {
        topologyDescMapping = new HashMap<>();
        topologyDescMapping.put(ArbitraryTopologyDescriptor.class, "ARBITRARY");
        topologyDescMapping.put(BestProximityDescriptor.class, "BEST_PROXIMITY");
        topologyDescMapping.put(ThresholdProximityDescriptor.class, "THRESHOLD");
        topologyDescMapping.put(DifferentHostsExclusiveDescriptor.class, "DIFFERENT_HOSTS_EXCLUSIVE");
        topologyDescMapping.put(MultipleHostsExclusiveDescriptor.class, "MULTIPLE_HOSTS_EXCLUSIVE");
        topologyDescMapping.put(SingleHostDescriptor.class, "SINGLE_HOST");
        topologyDescMapping.put(SingleHostExclusiveDescriptor.class, "SINGLE_HOST_EXCLUSIVE");
    }

    public TaskData() {
    }

    private void convertTopologyDescriptor(TopologyDescriptor desc) {
        if (desc == null) {
            topologyDescriptor = null;
            return;
        }
        if (!topologyDescMapping.containsKey(desc.getClass())) {
            throw new IllegalArgumentException("Unsupported topology descriptor: " + desc);
        }
        topologyDescriptor = topologyDescMapping.get(desc.getClass());
        if (desc instanceof ThresholdProximityDescriptor) {
            topologyDescriptorThreshold = ((ThresholdProximityDescriptor) desc).getThreshold();
        }
    }

    private TopologyDescriptor convertTopologyDescriptor() {
        if (topologyDescriptor == null) {
            return null;
        }
        if (topologyDescriptor.equals("ARBITRARY")) {
            return TopologyDescriptor.ARBITRARY;
        } else if (topologyDescriptor.equals("BEST_PROXIMITY")) {
            return TopologyDescriptor.BEST_PROXIMITY;
        } else if (topologyDescriptor.equals("SINGLE_HOST")) {
            return TopologyDescriptor.SINGLE_HOST;
        } else if (topologyDescriptor.equals("SINGLE_HOST_EXCLUSIVE")) {
            return TopologyDescriptor.SINGLE_HOST_EXCLUSIVE;
        } else if (topologyDescriptor.equals("MULTIPLE_HOSTS_EXCLUSIVE")) {
            return TopologyDescriptor.MULTIPLE_HOSTS_EXCLUSIVE;
        } else if (topologyDescriptor.equals("DIFFERENT_HOSTS_EXCLUSIVE")) {
            return TopologyDescriptor.DIFFERENT_HOSTS_EXCLUSIVE;
        } else if (topologyDescriptor.equals("THRESHOLD")) {
            return new ThresholdProximityDescriptor(topologyDescriptorThreshold);
        } else {
            throw new IllegalStateException("Invalid TopologyDescriptor: " + topologyDescriptor);
        }
    }

    void updateMutableAttributes(InternalTask task) {
        setTaskName(task.getName());
        setStartTime(task.getStartTime());
        setFinishedTime(task.getFinishedTime());
        setIteration(task.getIterationIndex());
        setReplication(task.getReplicationIndex());
        setMatchingBlock(task.getMatchingBlock());
        setTaskStatus(task.getStatus());
        setExecutionDuration(task.getExecutionDuration());
    }

    static TaskData createTaskData(JobData jobRuntimeData, InternalScriptTask task) {
        TaskData taskData = new TaskData();

        TaskData.DBTaskId taskId = new DBTaskId();
        taskId.setJobId(jobRuntimeData.getId());
        taskId.setTaskId(task.getTaskInfo().getTaskId().longValue());

        taskData.setId(taskId);
        taskData.setDescription(task.getDescription());
        taskData.setTag(task.getTag());
        taskData.setParallelEnvironment(task.getParallelEnvironment());
        taskData.setFlowBlock(task.getFlowBlock());
        taskData.setRestartMode(task.getRestartTaskOnError());
        taskData.setPreciousLogs(task.isPreciousLogs());
        taskData.setPreciousResult(task.isPreciousResult());
        taskData.setRunAsMe(task.isRunAsMe());
        taskData.setWallTime(task.getWallTime());
        taskData.setCancelJobOnError(task.isCancelJobOnError());
        taskData.setMaxNumberOfExecution(task.getMaxNumberOfExecution());
        taskData.setJobData(jobRuntimeData);
        taskData.setNumberOfExecutionOnFailureLeft(PASchedulerProperties.NUMBER_OF_EXECUTION_ON_FAILURE
                .getValueAsInt());
        taskData.setNumberOfExecutionLeft(task.getMaxNumberOfExecution());
        taskData.setGenericInformation(task.getGenericInformations(false));
        taskData.updateMutableAttributes(task);

        if (task.getSelectionScripts() != null) {
            List<SelectionScriptData> scripts = new ArrayList<>(task.getSelectionScripts().size());
            for (SelectionScript selectionScript : task.getSelectionScripts()) {
                scripts.add(SelectionScriptData.createForSelectionScript(selectionScript, taskData));
            }
            taskData.setSelectionScripts(scripts);
        }
        if (task.getExecutableContainer() != null) {
            taskData.setScript(ScriptData.createForScript(
                    ((ScriptExecutableContainer) task.getExecutableContainer()).getScript(), taskData));
        }
        if (task.getPreScript() != null) {
            taskData.setPreScript(ScriptData.createForScript(task.getPreScript(), taskData));
        }
        if (task.getPostScript() != null) {
            taskData.setPostScript(ScriptData.createForScript(task.getPostScript(), taskData));
        }
        if (task.getCleaningScript() != null) {
            taskData.setCleanScript(ScriptData.createForScript(task.getCleaningScript(), taskData));
        }
        if (task.getFlowScript() != null) {
            taskData.setFlowScript(ScriptData.createForFlowScript(task.getFlowScript(), taskData));
        }

        List<SelectorData> selectorsData = new ArrayList<>();
        if (task.getInputFilesList() != null) {
            for (InputSelector selector : task.getInputFilesList()) {
                selectorsData.add(SelectorData.createForInputSelector(selector, taskData));
            }
        }
        if (task.getOutputFilesList() != null) {
            for (OutputSelector selector : task.getOutputFilesList()) {
                selectorsData.add(SelectorData.createForOutputSelector(selector, taskData));
            }
        }
        taskData.setDataspaceSelectors(selectorsData);

        ForkEnvironment forkEnvironment = task.getForkEnvironment();
        if (forkEnvironment != null) {
            taskData.setAdditionalClasspath(forkEnvironment.getAdditionalClasspath());
            taskData.setJavaHome(forkEnvironment.getJavaHome());
            taskData.setJvmArguments(forkEnvironment.getJVMArguments());
            taskData.setWorkingDir(forkEnvironment.getWorkingDir());

            if (forkEnvironment.getEnvScript() != null) {
                taskData.setEnvScript(ScriptData.createForScript(forkEnvironment.getEnvScript(), taskData));
            }

            Map<String, String> systemEnvironment = forkEnvironment.getSystemEnvironment();

            if (systemEnvironment != null) {
                List<EnvironmentModifierData> envModifiers =
                        new ArrayList<>(systemEnvironment.size());

                for (Map.Entry<String, String> entry : systemEnvironment.entrySet()) {
                    envModifiers.add(
                            EnvironmentModifierData.create(
                                    new PropertyModifier(entry.getKey(), entry.getValue()), taskData));
                }

                taskData.setEnvModifiers(envModifiers);
            }
        }

        taskData.initTaskType(task);

        return taskData;
    }

    TaskId createTaskId(InternalJob internalJob) {
        return TaskIdImpl.createTaskId(internalJob.getId(), getTaskName(), getId().getTaskId());
    }

    InternalTask toInternalTask(InternalJob internalJob) throws InvalidScriptException {
        TaskId taskId = createTaskId(internalJob);

        InternalTask internalTask;

        if (taskType.equals(SCRIPT_TASK)) {
            internalTask = new InternalScriptTask();
        } else if (taskType.equals(FORKED_SCRIPT_TASK)) {
            internalTask = new InternalForkedScriptTask();
        } else {
            throw new IllegalStateException("Unexpected stored task type: " + taskType);
        }

        internalTask.setId(taskId);
        internalTask.setDescription(getDescription());
        internalTask.setTag(this.getTag());
        internalTask.setStatus(getTaskStatus());
        internalTask.setJobInfo(internalJob.getJobInfo());
        internalTask.setName(getTaskName());
        internalTask.setExecutionDuration(getExecutionDuration());
        internalTask.setFinishedTime(getFinishedTime());
        internalTask.setStartTime(getStartTime());
        internalTask.setExecutionHostName(getExecutionHostName());
        internalTask.setCancelJobOnError(isCancelJobOnError());
        internalTask.setPreciousLogs(isPreciousLogs());
        internalTask.setPreciousResult(isPreciousResult());
        internalTask.setRunAsMe(isRunAsMe());
        internalTask.setWallTime(getWallTime());
        internalTask.setMaxNumberOfExecution(getMaxNumberOfExecution());
        internalTask.setRestartTaskOnError(getRestartMode());
        internalTask.setFlowBlock(getFlowBlock());
        internalTask.setIterationIndex(getIteration());
        internalTask.setReplicationIndex(getReplication());
        internalTask.setMatchingBlock(getMatchingBlock());

        ForkEnvironment forkEnv = new ForkEnvironment();
        forkEnv.setJavaHome(javaHome);

        List<String> additionalClasspath = getAdditionalClasspath();
        if (additionalClasspath != null) {
            for (String classpath : additionalClasspath) {
                forkEnv.addAdditionalClasspath(classpath);
            }
        }

        List<String> jvmArguments = getJvmArguments();
        if (jvmArguments != null) {
            for (String jvmArg : jvmArguments) {
                forkEnv.addJVMArgument(jvmArg);
            }
        }

        List<EnvironmentModifierData> envModifiers = getEnvModifiers();

        if (envModifiers != null) {
            for (EnvironmentModifierData envModifier : envModifiers) {
                forkEnv.addSystemEnvironmentVariable(envModifier.getName(), envModifier.getValue());
            }
        }

        if (envScript != null) {
            forkEnv.setEnvScript(envScript.createSimpleScript());
        }

        internalTask.setForkEnvironment(forkEnv);

        return internalTask;
    }

    @EmbeddedId
    public DBTaskId getId() {
        return id;
    }

    public void setId(DBTaskId taskId) {
        this.id = taskId;
    }

    @Column(name = "GENERIC_INFO", updatable = false)
    @Type(type = "org.ow2.proactive.scheduler.core.db.types.NonEmptyMapToBlobType", parameters = @Parameter(name = SerializableToBlobType.CLASS_NAME, value = "java.lang.Object"))
    public Map<String, String> getGenericInformation() {
        return genericInformation;
    }

    public void setGenericInformation(Map<String, String> genericInformation) {
        this.genericInformation = genericInformation;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @Index(name = "JOB_ID_TASK_DATA_INDEX")
    @JoinColumn(name = "JOB_ID", nullable = false, updatable = false)
    public JobData getJobData() {
        return jobData;
    }

    public void setJobData(JobData jobRuntimeData) {
        this.jobData = jobRuntimeData;
    }

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "TASK_DATA_DEPENDENCIES", joinColumns = {
            @JoinColumn(name = "JOB_ID", referencedColumnName = "TASK_ID_JOB"),
            @JoinColumn(name = "TASK_ID", referencedColumnName = "TASK_ID_TASK") })
    @BatchSize(size = 100)
    public List<DBTaskId> getDependentTasks() {
        return dependentTasks;
    }

    public void setDependentTasks(List<DBTaskId> dependentTasks) {
        this.dependentTasks = dependentTasks;
    }

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "TASK_DATA_JOINED_BRANCHES", joinColumns = {
            @JoinColumn(name = "JOB_ID", referencedColumnName = "TASK_ID_JOB"),
            @JoinColumn(name = "TASK_ID", referencedColumnName = "TASK_ID_TASK") })
    @BatchSize(size = 100)
    public List<DBTaskId> getJoinedBranches() {
        return joinedBranches;
    }

    public void setJoinedBranches(List<DBTaskId> joinedBranches) {
        this.joinedBranches = joinedBranches;
    }

    @OneToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @Index(name = "IF_BRANCH_TASK_DATA_INDEX")
    public TaskData getIfBranch() {
        return ifBranch;
    }

    public void setIfBranch(TaskData ifBranch) {
        this.ifBranch = ifBranch;
    }

    @Cascade(CascadeType.ALL)
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "taskData")
    @OnDelete(action = OnDeleteAction.CASCADE)
    public List<SelectionScriptData> getSelectionScripts() {
        return selectionScripts;
    }

    public void setSelectionScripts(List<SelectionScriptData> selectionScripts) {
        this.selectionScripts = selectionScripts;
    }

    @Cascade(CascadeType.ALL)
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "taskData")
    @OnDelete(action = OnDeleteAction.CASCADE)
    public List<SelectorData> getDataspaceSelectors() {
        return dataspaceSelectors;
    }

    public void setDataspaceSelectors(List<SelectorData> dataspaceSelectors) {
        this.dataspaceSelectors = dataspaceSelectors;
    }

    @Cascade(CascadeType.ALL)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PRE_SCRIPT_ID")
    @ForeignKey(name = "none") // disable foreign key, to be able to remove runtime data
    @Index(name = "TASK_PRE_SCRIPT")
    public ScriptData getPreScript() {
        return preScript;
    }

    public void setPreScript(ScriptData preScript) {
        this.preScript = preScript;
    }

    @Cascade(CascadeType.ALL)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "POST_SCRIPT_ID")
    @ForeignKey(name = "none") // disable foreign key, to be able to remove runtime data
    @Index(name = "TASK_POST_SCRIPT")
    public ScriptData getPostScript() {
        return postScript;
    }

    public void setPostScript(ScriptData postScript) {
        this.postScript = postScript;
    }

    @Cascade(CascadeType.ALL)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CLEAN_SCRIPT_ID")
    @ForeignKey(name = "none") // disable foreign key, to be able to remove runtime data
    @Index(name = "TASK_CLEAN_SCRIPT")
    public ScriptData getCleanScript() {
        return cleanScript;
    }

    public void setCleanScript(ScriptData cleanScript) {
        this.cleanScript = cleanScript;
    }

    @Cascade(CascadeType.ALL)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FLOW_SCRIPT_ID")
    @ForeignKey(name = "none") // disable foreign key, to be able to remove runtime data
    @Index(name = "TASK_FLOW_SCRIPT")
    public ScriptData getFlowScript() {
        return flowScript;
    }

    public void setFlowScript(ScriptData flowScript) {
        this.flowScript = flowScript;
    }

    @Column(nullable = false, name = "TASK_NAME")
    @Index(name = "TASK_NAME_TASK_DATA_INDEX")
    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    @Column(name = "TYPE", nullable = false, updatable = false)
    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    void initTaskType(InternalTask task) {
       if (task.getClass().equals(InternalForkedScriptTask.class)) {
            taskType = FORKED_SCRIPT_TASK;
        } else if (task.getClass().equals(InternalScriptTask.class)) {
            taskType = SCRIPT_TASK;
        } else {
            throw new IllegalArgumentException("Unexpected task type: " + task.getClass());
        }
    }

    @Column(name = "DESCRIPTION", updatable = false)
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Column(name = "TAG", updatable = false)
    public String getTag(){ return this.tag; }

    public void setTag(String tag) { this.tag = tag; }


    @Column(name = "MAX_NUMBER_OF_EXEC", updatable = false)
    public int getMaxNumberOfExecution() {
        return maxNumberOfExecution;
    }

    public void setMaxNumberOfExecution(int maxNumberOfExecution) {
        this.maxNumberOfExecution = maxNumberOfExecution;
    }

    @Column(name = "CANCEL_JOB_ON_ERROR", updatable = false)
    public boolean isCancelJobOnError() {
        return cancelJobOnError;
    }

    public void setCancelJobOnError(boolean cancelJobOnError) {
        this.cancelJobOnError = cancelJobOnError;
    }

    @Column(name = "RES_PREVIEW", length = 1000, updatable = false)
    public String getResultPreview() {
        return resultPreview;
    }

    public void setResultPreview(String resultPreview) {
        this.resultPreview = resultPreview;
    }

    @Column(name = "PRECIOUS_RES", updatable = false)
    public boolean isPreciousResult() {
        return preciousResult;
    }

    public void setPreciousResult(boolean preciousResult) {
        this.preciousResult = preciousResult;
    }

    @Column(name = "PRECIOUS_LOG", updatable = false)
    public boolean isPreciousLogs() {
        return preciousLogs;
    }

    public void setPreciousLogs(boolean preciousLogs) {
        this.preciousLogs = preciousLogs;
    }

    @Column(name = "RUN_AS_ME", updatable = false)
    public boolean isRunAsMe() {
        return runAsMe;
    }

    public void setRunAsMe(boolean runAsMe) {
        this.runAsMe = runAsMe;
    }

    @Column(name = "WALLTIME", updatable = false)
    public long getWallTime() {
        return wallTime;
    }

    public void setWallTime(long wallTime) {
        this.wallTime = wallTime;
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

    @Column(name = "EXEC_DURATION")
    public long getExecutionDuration() {
        return executionDuration;
    }

    public void setExecutionDuration(long executionDuration) {
        this.executionDuration = executionDuration;
    }

    @Column(name = "STATUS", nullable = false)
    @Index(name = "task_status_index")
    public TaskStatus getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(TaskStatus taskStatus) {
        this.taskStatus = taskStatus;
    }

    @Column(name = "EXEC_HOST_NAME")
    public String getExecutionHostName() {
        return executionHostName;
    }

    public void setExecutionHostName(String executionHostName) {
        this.executionHostName = executionHostName;
    }

    @Column(name = "NUMBER_OF_EXEC_LEFT")
    public int getNumberOfExecutionLeft() {
        return numberOfExecutionLeft;
    }

    public void setNumberOfExecutionLeft(int numberOfExecutionLeft) {
        this.numberOfExecutionLeft = numberOfExecutionLeft;
    }

    @Column(name = "NUMBER_OF_EXEC_ON_FAIL_LEFT")
    public int getNumberOfExecutionOnFailureLeft() {
        return numberOfExecutionOnFailureLeft;
    }

    public void setNumberOfExecutionOnFailureLeft(int numberOfExecutionOnFailureLeft) {
        this.numberOfExecutionOnFailureLeft = numberOfExecutionOnFailureLeft;
    }

    @Column(name = "ITERATION")
    public int getIteration() {
        return iteration;
    }

    public void setIteration(int iteration) {
        this.iteration = iteration;
    }

    @Column(name = "REPLICATION")
    public int getReplication() {
        return replication;
    }

    public void setReplication(int replication) {
        this.replication = replication;
    }

    @Column(name = "MATCH_BLOCK")
    public String getMatchingBlock() {
        return matchingBlock;
    }

    public void setMatchingBlock(String matchingBlock) {
        this.matchingBlock = matchingBlock;
    }

    @Column(name = "RESTART_MODE")
    public int getRestartModeId() {
        return restartModeId;
    }

    public void setRestartModeId(int restartModeId) {
        this.restartModeId = restartModeId;
    }

    @Column(name = "FLOW_BLOCK")
    public FlowBlock getFlowBlock() {
        return flowBlock;
    }

    public void setFlowBlock(FlowBlock flowBlock) {
        this.flowBlock = flowBlock;
    }

    @Column(name = "NODES_NUMBER")
    public int getParallelEnvNodesNumber() {
        return parallelEnvNodesNumber;
    }

    public void setParallelEnvNodesNumber(int parallelEnvNodesNumber) {
        this.parallelEnvNodesNumber = parallelEnvNodesNumber;
    }

    @Column(name = "TOPOLOGY")
    public String getTopologyDescriptor() {
        return topologyDescriptor;
    }

    public void setTopologyDescriptor(String topologyDescriptor) {
        this.topologyDescriptor = topologyDescriptor;
    }

    @Column(name = "TOPOLOGY_DESC_THRESHOLD")
    public long getTopologyDescriptorThreshold() {
        return topologyDescriptorThreshold;
    }

    public void setTopologyDescriptorThreshold(long topologyDescriptorThreshold) {
        this.topologyDescriptorThreshold = topologyDescriptorThreshold;
    }

    @Transient
    RestartMode getRestartMode() {
        switch (restartModeId) {
            case 1:
                return RestartMode.ANYWHERE;
            case 2:
                return RestartMode.ELSEWHERE;
            default:
                throw new IllegalStateException("Invalid restartModeId: " + restartModeId);
        }
    }

    void setRestartMode(RestartMode restartMode) {
        if (restartMode.equals(RestartMode.ANYWHERE)) {
            restartModeId = 1;
        } else if (restartMode.equals(RestartMode.ELSEWHERE)) {
            restartModeId = 2;
        } else {
            throw new IllegalArgumentException("Invalid restart mode: " + restartMode);
        }
    }

    @Transient
    ParallelEnvironment getParallelEnvironment() {
        if (parallelEnvNodesNumber == 0) {
            return null;
        }

        return new ParallelEnvironment(parallelEnvNodesNumber, convertTopologyDescriptor());
    }

    void setParallelEnvironment(ParallelEnvironment env) {
        if (env == null) {
            return;
        }
        parallelEnvNodesNumber = env.getNodesNumber();
        convertTopologyDescriptor(env.getTopologyDescriptor());
    }

    TaskUsage toTaskUsage(JobIdImpl jobId) {
        TaskId taskId = TaskIdImpl.createTaskId(jobId, getTaskName(), getId().getTaskId());

        return new TaskUsage(taskId.value(), getTaskName(), getStartTime(), getFinishedTime(),
            getExecutionDuration(), getParallelEnvironment() == null ? 1 : getParallelEnvironment()
                    .getNodesNumber());
    }
    

    TaskInfoImpl createTaskInfo(JobIdImpl jobId) {
        TaskId taskId = TaskIdImpl.createTaskId(jobId, getTaskName(), getId().getTaskId());
        TaskInfoImpl taskInfo = new TaskInfoImpl();
        taskInfo.setTaskId(taskId);
        taskInfo.setStatus(getTaskStatus());
        taskInfo.setStartTime(getStartTime());
        taskInfo.setProgress(0);
        taskInfo.setNumberOfExecutionOnFailureLeft(getNumberOfExecutionOnFailureLeft());
        taskInfo.setNumberOfExecutionLeft(getNumberOfExecutionLeft());
        taskInfo.setJobInfo(getJobData().toJobInfo());
        taskInfo.setJobId(jobId);
        taskInfo.setFinishedTime(getFinishedTime());
        taskInfo.setExecutionHostName(getExecutionHostName());
        taskInfo.setExecutionDuration(getExecutionDuration());
        return taskInfo;
    }
    
    public TaskInfo toTaskInfo() {
        JobIdImpl jobId = new JobIdImpl(getJobData().getId(), getJobData().getJobName());
        TaskInfoImpl taskInfo = createTaskInfo(jobId);
        System.out.println("jobName=" + getJobData().getJobName());
        return taskInfo;
    }
    
    public TaskState toTaskState() {
        TaskInfo taskInfo = toTaskInfo();
        TaskStateImpl taskState = new TaskStateImpl();
        taskState.update(taskInfo);
        taskState.setName(getTaskName());
        taskState.setDescription(getDescription());
        taskState.setTag(getTag());
        taskState.setIterationIndex(getIteration());
        taskState.setReplicationIndex(getReplication());
        taskState.setMaxNumberOfExecution(getMaxNumberOfExecution());
        taskState.setParallelEnvironment(getParallelEnvironment());
        
        System.out.println("getName()=" + taskState.getName());
        System.out.println("getDescription()=" + taskState.getDescription());
        System.out.println("getTag()=" + taskState.getTag());
        System.out.println("getIterationIndex()=" + taskState.getIterationIndex());
        System.out.println("getReplicationIndex()=" + taskState.getReplicationIndex());
        System.out.println("getMaxNumberOfExecution()=" + taskState.getMaxNumberOfExecution());
        System.out.println("getMaxNumberOfExecutionOnFailure()=" + taskState.getMaxNumberOfExecutionOnFailure());
        System.out.println("getTaskInfo()=" + taskState.getTaskInfo());
        System.out.println("getParallelEnvironment()=" + taskState.getParallelEnvironment());
        System.out.println("getNumberOfNodesNeeded()=" + taskState.getNumberOfNodesNeeded());        
        return taskState;
    }
    
    
}
