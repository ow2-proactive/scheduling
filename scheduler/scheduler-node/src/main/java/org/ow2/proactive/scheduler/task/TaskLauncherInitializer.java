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
package org.ow2.proactive.scheduler.task;

import org.objectweb.proactive.extensions.dataspaces.core.naming.NamingService;

import org.ow2.proactive.scheduler.common.task.ForkEnvironment;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.dataspaces.InputSelector;
import org.ow2.proactive.scheduler.common.task.dataspaces.OutputSelector;
import org.ow2.proactive.scheduler.common.task.flow.FlowScript;
import org.ow2.proactive.scheduler.task.utils.Substitutor;
import org.ow2.proactive.scripting.Script;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * TaskLauncherInitializer is used to initialize the different task launcher.<br>
 * It contains every information that can be used by the launchers. It's a kind of contract
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
    /** The walltime defined for the task (it is considered as defined if it is > 0) */
    private long walltime;
    /** policy content to be prepared before being sent to node */
    private String policyContent;
    /** log4j content to be prepared before being sent to node */
    private String log4JContent;
    /** PAConfiguration content to be prepared before being sent to node */
    private String paConfigContent;

    /** replication index: task was replicated in parallel */
    private int replicationIndex = 0;
    /** iteration index: task was replicated sequentially */
    private int iterationIndex = 0;

    /** DataSpaces needed parameter */
    private List<InputSelector> taskInputFiles = null;
    private List<OutputSelector> taskOutputFiles = null;
    private NamingService namingService;
    private String owner;
    private boolean preciousLogs;

    private Map<String, String> variables;
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
     * Set the policyContent value to the given policyContent value
     * 
     * @param policyContent the policyContent to set
     */
    public void setPolicyContent(String policyContent) {
        this.policyContent = policyContent;
    }

    /**
     * Get the policyContent
     *
     * @return the policyContent
     */
    public String getPolicyContent() {
        return policyContent;
    }

    /**
     * Get the log4JContent
     *
     * @return the log4JContent
     */
    public String getLog4JContent() {
        return log4JContent;
    }

    /**
     * Set the log4JContent value to the given log4jContent value
     *
     * @param log4jContent the log4JContent to set
     */
    public void setLog4JContent(String log4jContent) {
        log4JContent = log4jContent;
    }

    /**
     * Get the paConfiguration content
     *
     * @return the paConfiguration content
     */
    public String getPaConfigContent() {
        return paConfigContent;
    }

    /**
     * Set the paConfiguration content value to the given paConfigContent value
     *
     * @param paConfigContent content the paConfigContent to set
     */
    public void setPaConfigContent(String paConfigContent) {
        this.paConfigContent = paConfigContent;
    }

    /**
     * Get the owner
     *
     * @return the owner
     */
    public String getOwner() {
        return owner;
    }

    /**
     * Set the owner value to the given owner value
     *
     * @param owner the owner to set
     */
    public void setOwner(String owner) {
        this.owner = owner;
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
     * @return the preciousLogs
     */
    public boolean isPreciousLogs() {
        return preciousLogs;
    }

    /**
     * @param preciousLogs the preciousLogs to set
     */
    public void setPreciousLogs(boolean preciousLogs) {
        this.preciousLogs = preciousLogs;
    }

    public void setVariables(Map<String, String> variables) {
        this.variables = variables;
    }

    public Map<String, String> getVariables() {
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

            Map<String, String> replacements = Substitutor.buildSubstitutes(variables);
            for (InputSelector is : taskInputFiles) {
                InputSelector filteredInputSelector = new InputSelector(is.getInputFiles(), is.getMode());
                Set<String> includes = filteredInputSelector.getInputFiles().getIncludes();
                Set<String> excludes = filteredInputSelector.getInputFiles().getExcludes();

                Set<String> filteredIncludes = filteredSelector(includes, replacements);
                Set<String> filteredExcludes = filteredSelector(excludes, replacements);

                filteredInputSelector.getInputFiles().setIncludes(filteredIncludes);
                filteredInputSelector.getInputFiles().setExcludes(filteredExcludes);
                filteredTaskInputFiles.add(filteredInputSelector);
            }
        }
        return filteredTaskInputFiles;
    }

    private Set<String> filteredSelector(Set<String> selectors, Map<String, String> replacements) {
        Set<String> filteredIncludes = new HashSet<>();
        if (selectors != null) {
            for (String include : selectors) {
                filteredIncludes.add(Substitutor.replace(include, replacements));
            }
        }
        return filteredIncludes;
    }

    public List<OutputSelector> getFilteredOutputFiles(Map<String, Serializable> variables) {
        List<OutputSelector> filteredTaskOutputFiles = new ArrayList<>();
        if (taskOutputFiles != null) {

            Map<String, String> replacements = Substitutor.buildSubstitutes(variables);
            for (OutputSelector is : taskOutputFiles) {
                OutputSelector filteredOutputSelector = new OutputSelector(is.getOutputFiles(), is.getMode());
                Set<String> includes = filteredOutputSelector.getOutputFiles().getIncludes();
                Set<String> excludes = filteredOutputSelector.getOutputFiles().getExcludes();

                Set<String> filteredIncludes = filteredSelector(includes, replacements);
                Set<String> filteredExcludes = filteredSelector(excludes, replacements);

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
