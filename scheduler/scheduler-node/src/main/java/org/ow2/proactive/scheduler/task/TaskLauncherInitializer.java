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
package org.ow2.proactive.scheduler.task;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.objectweb.proactive.extensions.dataspaces.core.naming.NamingService;
import org.ow2.proactive.scheduler.common.job.JobVariable;
import org.ow2.proactive.scheduler.common.task.ForkEnvironment;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskVariable;
import org.ow2.proactive.scheduler.common.task.dataspaces.InputSelector;
import org.ow2.proactive.scheduler.common.task.dataspaces.OutputSelector;
import org.ow2.proactive.scheduler.common.task.flow.FlowScript;
import org.ow2.proactive.scheduler.common.util.VariableSubstitutor;
import org.ow2.proactive.scheduler.signal.SignalApi;
import org.ow2.proactive.scheduler.synchronization.Synchronization;
import org.ow2.proactive.scripting.Script;

import com.google.common.collect.ImmutableMap;


/**
 * TaskLauncherInitializer is used to initialize the task launcher.<br>
 * It contains every information that can be used by the launcher. It's a kind of contract
 * so that each launcher must use its required information coming from this class. 
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 1.0
 */
public class TaskLauncherInitializer implements Serializable {

    /** The task identification */
    private TaskId taskId;

    /** The script executed before the task */
    private Script<?> pre;

    /** The script executed after the task */
    private Script<?> post;

    /** The script executed after the task */
    private FlowScript flowScript;

    /** The walltime defined for the task (it is considered as defined if it is &gt; 0) */
    private long walltime;

    private String jobOwner = "Unknown";

    private String schedulerRestUrl;

    private String schedulerRestPublicUrl;

    private String catalogRestUrl;

    private String catalogRestPublicUrl;

    private String cloudAutomationRestUrl;

    private String cloudAutomationRestPublicUrl;

    private String jobPlannerRestUrl;

    private String jobPlannerRestPublicUrl;

    private String notificationServiceRestUrl;

    private String notificationServiceRestPublicUrl;

    /** replication index: task was replicated in parallel */
    private int replicationIndex = 0;

    /** iteration index: task was replicated sequentially */
    private int iterationIndex = 0;

    private ImmutableMap<String, String> genericInformation;

    private boolean authorizedForkEnvironmentScript = true;

    /** DataSpaces needed parameter */
    private List<InputSelector> taskInputFiles = null;

    private List<OutputSelector> taskOutputFiles = null;

    private NamingService namingService;

    private boolean preciousLogs;

    private ImmutableMap<String, JobVariable> variables;

    private ImmutableMap<String, TaskVariable> taskVariables = ImmutableMap.of();

    private int pingPeriod;

    private int pingAttempts = 1;

    private ForkEnvironment forkEnvironment;

    private Synchronization synchronizationAPI;

    private SignalApi signalAPI;

    /**
     * Get the taskId
     *
     * @return the taskId
     */
    public TaskId getTaskId() {
        return taskId;
    }

    /**
     * Set the taskId value to the given taskId value
     * 
     * @param taskId the taskId to set
     */
    public void setTaskId(TaskId taskId) {
        this.taskId = taskId;
    }

    /**
     * Get the pre-script
     *
     * @return the pre-script
     */
    public Script<?> getPreScript() {
        return pre;
    }

    /**
     * Set the pre-script value to the given pre value
     * 
     * @param pre the pre-script to set
     */
    public void setPreScript(Script<?> pre) {
        this.pre = pre;
    }

    /**
     * Get the post-script
     *
     * @return the post-script
     */
    public Script<?> getPostScript() {
        return post;
    }

    /**
     * Set the control flow script value to the given flow value
     * 
     * @param flow the control flow script to set
     */
    public void setControlFlowScript(FlowScript flow) {
        flowScript = flow;
    }

    /**
     * Get the control flow script
     *
     * @return the post-script
     */
    public FlowScript getControlFlowScript() {
        return flowScript;
    }

    /**
     * Set the post-script value to the given post value
     * 
     * @param post the post-script to set
     */
    public void setPostScript(Script<?> post) {
        this.post = post;
    }

    /**
     * Set the walltime value to the given walltime value
     * 
     * @param walltime the walltime to set
     */
    public void setWalltime(long walltime) {
        this.walltime = walltime;
    }

    /**
     * Get the walltime of the task
     *
     * @return the walltime of the task
     */
    public long getWalltime() {
        return walltime;
    }

    /**
     * Get the namingService Stub
     *
     * @return the namingService
     */
    public NamingService getNamingService() {
        return namingService;
    }

    /**
     * Set the namingService Stub to the given namingService value
     *
     * @param namingService the namingService to set
     */
    public void setNamingService(NamingService namingService) {
        this.namingService = namingService;
    }

    /**
     * Get the taskInputFiles
     *
     * @return the taskInputFiles
     */
    public List<InputSelector> getTaskInputFiles() {
        return taskInputFiles;
    }

    /**
     * Set the taskInputFiles value to the given taskInputFiles value
     *
     * @param taskInputFiles the taskInputFiles to set
     */
    public void setTaskInputFiles(List<InputSelector> taskInputFiles) {
        this.taskInputFiles = taskInputFiles;
    }

    /**
     * Get the taskOutputFiles
     *
     * @return the taskOutputFiles
     */
    public List<OutputSelector> getTaskOutputFiles() {
        return taskOutputFiles;
    }

    /**
     * Set the taskOutputFiles value to the given taskOutputFiles value
     *
     * @param taskOutputFiles the taskOutputFiles to set
     */
    public void setTaskOutputFiles(List<OutputSelector> taskOutputFiles) {
        this.taskOutputFiles = taskOutputFiles;
    }

    /**
     * @param id the replication index: task was replicated in parallel 
     */
    public void setIterationIndex(int id) {
        this.iterationIndex = id;
    }

    /**
     * @return the replication index: task was replicated in parallel 
     */
    public int getIterationIndex() {
        return this.iterationIndex;
    }

    /**
     * @return the job owner
     */
    public String getJobOwner() {
        return jobOwner;
    }

    /**
     * @param id the iteration index: task was replicated sequentially 
     */
    public void setReplicationIndex(int id) {
        this.replicationIndex = id;
    }

    /**
     * @return the iteration index: task was replicated sequentially 
     */
    public int getReplicationIndex() {
        return this.replicationIndex;
    }

    /**
     * @param genericInformation the generic information of this task
     */
    public void setGenericInformation(Map<String, String> genericInformation) {
        this.genericInformation = ImmutableMap.copyOf(genericInformation);
    }

    /**
     * @return the generic information of this task
     */
    public ImmutableMap<String, String> getGenericInformation() {
        return genericInformation;
    }

    /**
     * @return the preciousLogs
     */
    public boolean isPreciousLogs() {
        return preciousLogs;
    }

    public void setJobOwner(String jobOwner) {
        this.jobOwner = jobOwner;
    }

    public void setSchedulerRestUrl(String schedulerRestUrl) {
        this.schedulerRestUrl = schedulerRestUrl;
    }

    public String getSchedulerRestUrl() {
        return schedulerRestUrl;
    }

    public void setSchedulerRestPublicUrl(String schedulerRestPublicUrl) {
        this.schedulerRestPublicUrl = schedulerRestPublicUrl;
    }

    public String getSchedulerRestPublicUrl() {
        return schedulerRestPublicUrl;
    }

    public void setCatalogRestUrl(String catalogRestUrl) {
        this.catalogRestUrl = catalogRestUrl;
    }

    public String getCatalogRestUrl() {
        return catalogRestUrl;
    }

    public void setCatalogRestPublicUrl(String catalogRestPublicUrl) {
        this.catalogRestPublicUrl = catalogRestPublicUrl;
    }

    public String getCatalogRestPublicUrl() {
        return catalogRestPublicUrl;
    }

    public String getCloudAutomationRestUrl() {
        return cloudAutomationRestUrl;
    }

    public void setCloudAutomationRestUrl(String cloudAutomationRestUrl) {
        this.cloudAutomationRestUrl = cloudAutomationRestUrl;
    }

    public String getCloudAutomationRestPublicUrl() {
        return cloudAutomationRestPublicUrl;
    }

    public void setCloudAutomationRestPublicUrl(String cloudAutomationRestPublicUrl) {
        this.cloudAutomationRestPublicUrl = cloudAutomationRestPublicUrl;
    }

    public String getJobPlannerRestUrl() {
        return jobPlannerRestUrl;
    }

    public void setJobPlannerRestUrl(String jobPlannerRestUrl) {
        this.jobPlannerRestUrl = jobPlannerRestUrl;
    }

    public String getJobPlannerRestPublicUrl() {
        return jobPlannerRestPublicUrl;
    }

    public void setJobPlannerRestPublicUrl(String jobPlannerRestPublicUrl) {
        this.jobPlannerRestPublicUrl = jobPlannerRestPublicUrl;
    }

    public String getNotificationServiceRestUrl() {
        return notificationServiceRestUrl;
    }

    public void setNotificationServiceRestUrl(String notificationServiceRestUrl) {
        this.notificationServiceRestUrl = notificationServiceRestUrl;
    }

    public String getNotificationServiceRestPublicUrl() {
        return notificationServiceRestPublicUrl;
    }

    public void setNotificationServiceRestPublicUrl(String notificationServiceRestPublicUrl) {
        this.notificationServiceRestPublicUrl = notificationServiceRestPublicUrl;
    }

    /**
     * @param preciousLogs the preciousLogs to set
     */
    public void setPreciousLogs(boolean preciousLogs) {
        this.preciousLogs = preciousLogs;
    }

    public void setJobVariables(Map<String, JobVariable> variables) {
        this.variables = ImmutableMap.copyOf(variables);
    }

    public ImmutableMap<String, JobVariable> getJobVariables() {
        return this.variables;
    }

    public void setTaskVariables(Map<String, TaskVariable> taskVariables) {
        this.taskVariables = ImmutableMap.copyOf(taskVariables);
    }

    public ImmutableMap<String, TaskVariable> getTaskVariables() {
        return this.taskVariables;
    }

    public void setPingPeriod(int pingPeriod) {
        this.pingPeriod = pingPeriod;
    }

    public int getPingPeriod() {
        return pingPeriod;
    }

    public void setPingAttempts(int pingAttempts) {
        this.pingAttempts = pingAttempts;
    }

    public int getPingAttempts() {
        return pingAttempts;
    }

    public List<InputSelector> getFilteredInputFiles(Map<String, Serializable> variables) {
        List<InputSelector> filteredTaskInputFiles = new ArrayList<>();
        if (taskInputFiles != null) {

            for (InputSelector is : taskInputFiles) {
                InputSelector filteredInputSelector = new InputSelector(is.getInputFiles(), is.getMode());
                Set<String> includes = filteredInputSelector.getInputFiles().getIncludes();
                Set<String> excludes = filteredInputSelector.getInputFiles().getExcludes();

                Set<String> filteredIncludes = filteredSelector(includes, variables);
                Set<String> filteredExcludes = filteredSelector(excludes, variables);

                filteredInputSelector.getInputFiles().setIncludes(filteredIncludes);
                filteredInputSelector.getInputFiles().setExcludes(filteredExcludes);
                filteredTaskInputFiles.add(filteredInputSelector);
            }
        }
        return filteredTaskInputFiles;
    }

    private Set<String> filteredSelector(Set<String> selectors, Map<String, Serializable> variables) {
        Set<String> filteredIncludes = new HashSet<>();
        if (selectors != null) {
            for (String include : selectors) {
                filteredIncludes.add(VariableSubstitutor.filterAndUpdate(include, variables));
            }
        }
        return filteredIncludes;
    }

    public List<OutputSelector> getFilteredOutputFiles(Map<String, Serializable> variables) {
        List<OutputSelector> filteredTaskOutputFiles = new ArrayList<>();
        if (taskOutputFiles != null) {

            for (OutputSelector is : taskOutputFiles) {
                OutputSelector filteredOutputSelector = new OutputSelector(is.getOutputFiles(), is.getMode());
                Set<String> includes = filteredOutputSelector.getOutputFiles().getIncludes();
                Set<String> excludes = filteredOutputSelector.getOutputFiles().getExcludes();

                Set<String> filteredIncludes = filteredSelector(includes, variables);
                Set<String> filteredExcludes = filteredSelector(excludes, variables);

                filteredOutputSelector.getOutputFiles().setIncludes(filteredIncludes);
                filteredOutputSelector.getOutputFiles().setExcludes(filteredExcludes);
                filteredTaskOutputFiles.add(filteredOutputSelector);
            }
        }
        return filteredTaskOutputFiles;
    }

    public ForkEnvironment getForkEnvironment() {
        return forkEnvironment;
    }

    public void setForkEnvironment(ForkEnvironment forkEnvironment) {
        this.forkEnvironment = forkEnvironment;
    }

    public boolean isAuthorizedForkEnvironmentScript() {
        return authorizedForkEnvironmentScript;
    }

    public void setAuthorizedForkEnvironmentScript(boolean authorizedForkEnvironmentScript) {
        this.authorizedForkEnvironmentScript = authorizedForkEnvironmentScript;
    }

    public Synchronization getSynchronizationAPI() {
        return synchronizationAPI;
    }

    public void setSynchronizationAPI(Synchronization synchronizationAPI) {
        this.synchronizationAPI = synchronizationAPI;
    }

    public SignalApi getSignalAPI() {
        return signalAPI;
    }

    public void setSignalAPI(SignalApi signalAPI) {
        this.signalAPI = signalAPI;
    }

}
