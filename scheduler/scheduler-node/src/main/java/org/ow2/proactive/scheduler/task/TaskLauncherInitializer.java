/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2015 INRIA/University of
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
package org.ow2.proactive.scheduler.task;

import com.google.common.collect.ImmutableMap;
import org.objectweb.proactive.extensions.dataspaces.core.naming.NamingService;
import org.ow2.proactive.scheduler.common.task.ForkEnvironment;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.dataspaces.InputSelector;
import org.ow2.proactive.scheduler.common.task.dataspaces.OutputSelector;
import org.ow2.proactive.scheduler.common.task.flow.FlowScript;
import org.ow2.proactive.scheduler.common.util.VariableSubstitutor;
import org.ow2.proactive.scripting.Script;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


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

    private String restUrl;

    /** replication index: task was replicated in parallel */
    private int replicationIndex = 0;
    /** iteration index: task was replicated sequentially */
    private int iterationIndex = 0;

    private ImmutableMap<String, String> genericInformation;

    /** DataSpaces needed parameter */
    private List<InputSelector> taskInputFiles = null;
    private List<OutputSelector> taskOutputFiles = null;
    private NamingService namingService;
    private boolean preciousLogs;

    private ImmutableMap<String, String> variables;
    private int pingPeriod;
    private int pingAttempts = 1;

    private ForkEnvironment forkEnvironment;

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

    public void setRestUrl(String restUrl) {
        this.restUrl = restUrl;
    }

    public String getRestUrl() {
        return restUrl;
    }

    /**
     * @param preciousLogs the preciousLogs to set
     */
    public void setPreciousLogs(boolean preciousLogs) {
        this.preciousLogs = preciousLogs;
    }

    public void setVariables(Map<String, String> variables) {
        this.variables = ImmutableMap.copyOf(variables);
    }

    public ImmutableMap<String, String> getVariables() {
        return this.variables;
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

}
