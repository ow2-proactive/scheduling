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
package org.ow2.proactive.scheduler.common.task;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.extensions.dataspaces.vfs.selector.FileSelector;
import org.ow2.proactive.scheduler.common.SchedulerConstants;
import org.ow2.proactive.scheduler.common.job.Job;
import org.ow2.proactive.scheduler.common.task.dataspaces.InputAccessMode;
import org.ow2.proactive.scheduler.common.task.dataspaces.InputSelector;
import org.ow2.proactive.scheduler.common.task.dataspaces.OutputAccessMode;
import org.ow2.proactive.scheduler.common.task.dataspaces.OutputSelector;
import org.ow2.proactive.scheduler.common.task.flow.FlowAction;
import org.ow2.proactive.scheduler.common.task.flow.FlowBlock;
import org.ow2.proactive.scheduler.common.task.flow.FlowScript;
import org.ow2.proactive.scripting.Script;
import org.ow2.proactive.scripting.SelectionScript;

import com.google.common.base.Joiner;


/**
 * This class is the super class of the every task that can be integrated in a job.<br>
 * A task contains some properties that can be set but also : <ul>
 * <li>A selection script that can be used to select a specific execution node for this task.</li>
 * <li>A preScript that will be launched before the real task (can be used to set environment vars).</li>
 * <li>A postScript that will be launched just after the end of the real task.
 * (this can be used to transfer files that have been created by the task).</li>
 * <li>A CleaningScript that will be launched by the resource manager to perform some cleaning. (deleting files or resources).</li>
 * </ul>
 * You will also be able to add dependences (if necessary) to
 * this task. The dependences mechanism are best describe below.
 *
 * @see #addDependence(Task)
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
@PublicAPI
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "task")
public abstract class Task extends CommonAttribute {

    /** Name of the task. */
    protected String name = SchedulerConstants.TASK_DEFAULT_NAME;

    /** block declaration : syntactic scopes used for workflows
     * string representation of a FlowBlock enum */
    protected String flowBlock = FlowBlock.NONE.toString();

    /** Description of the task. */
    protected String description = null;

    /** tag of the task */
    protected String tag = null;

    /** DataSpace inputFiles */
    protected List<InputSelector> inputFiles = null;

    /** DataSpace outputFiles */
    protected List<OutputSelector> outputFiles = null;

    /**
     * The parallel environment of the task included number of nodes and topology definition
     * required to execute this task.
     */
    protected ParallelEnvironment parallelEnvironment = null;

    /**
     * selection script : can be launched before getting a node in order to
     * verify some computer specificity.
     */
    protected List<SelectionScript> sScripts;

    /**
     * PreScript : can be used to launch script just before the task
     * execution.
     */
    protected Script<?> preScript;

    /**
     * PostScript : can be used to launch script just after the task
     */
    protected Script<?> postScript;

    /**
     * CleaningScript : can be used to launch script just after the task or the postScript (if set)
     * Started even if a problem occurs.
     */
    protected Script<?> cScript;

    /**
     * FlowScript: Control Flow Action Script executed after the task,
     * returns a {@link FlowAction} which will perform flow actions on the job
     */
    protected FlowScript flowScript;

    /** Tell whether this task has a precious result or not. */
    protected boolean preciousResult;

    /** If true, the stdout/err of this tasks are stored in a file in LOCALSPACE and copied back to USERSPACE */
    protected boolean preciousLogs;

    protected boolean runAsMe;

    /** List of dependences if necessary. */
    @XmlTransient
    private List<Task> dependences = null;

    /** maximum execution time of the task (in milliseconds), the variable is only valid if isWallTime is true */
    protected long wallTime = 0;

    protected ForkEnvironment forkEnvironment;

    /** A map to hold task variables */
    protected ConcurrentHashMap<String, TaskVariable> variables = new ConcurrentHashMap<>();

    /**
     * Add a dependence to the task. <font color="red">Warning : the dependence order is very
     * important.</font>
     * <p>
     * In fact, it is in this order that you will get back the result in the children task.
     * <p>
     * For example: if you add to the task t3, the dependences t1 then t2, the parents of t3 will be t1 and t2 in this order
     * and the parameters of t3 will be the results of t1 and t2 in this order.
     *
     * @param task
     *            the parent task to add to this task.
     */
    public void addDependence(Task task) {
        if (dependences == null) {
            dependences = new ArrayList<Task>();
        }
        dependences.add(task);
    }

    /**
     * Same as the {@link #addDependence(Task)} method, but for a list of dependences.
     *
     * @param tasks
     *            the parent list of tasks to add to this task.
     */
    public void addDependences(List<Task> tasks) {
        if (dependences == null) {
            dependences = new ArrayList<Task>();
        }
        for (Task t : tasks) {
            addDependence(t);
        }
    }

    /**
     * To get the description of this task.
     *
     * @return the description of this task.
     */
    public String getDescription() {
        return description;
    }

    /**
     * To set the description of this task.
     *
     * @param description
     *            the description to set.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * To know if the result of this task is precious.
     *
     * @return true if the result is precious, false if not.
     */
    public boolean isPreciousResult() {
        return preciousResult;
    }

    /**
     * Set if the result of this task is precious.
     *
     * @param preciousResult true if the result of this task is precious, false if not.
     */
    public void setPreciousResult(boolean preciousResult) {
        this.preciousResult = preciousResult;
    }

    /**
     * To know if the logs of this task are precious.
     *
     * @return true if the logs are precious, false if not.
     */
    public boolean isPreciousLogs() {
        return preciousLogs;
    }

    /**
     * Set if the logs of this task are precious.
     *
     * @param preciousLogs true if the logs of this task are precious, false if not.
     */
    public void setPreciousLogs(boolean preciousLogs) {
        this.preciousLogs = preciousLogs;
    }

    /**
     * To know if the task will be executed under the user identity or not.
     *
     * @return true if the task will be executed as the user identity.
     */
    public boolean isRunAsMe() {
        return this.runAsMe;
    }

    /**
     * Set if this task will be run under user identity.
     * This could impact the performance.
     *
     * @param runAsMe true if this task will be run under user identity.
     */
    public void setRunAsMe(boolean runAsMe) {
        this.runAsMe = runAsMe;
    }

    /**
     * To get the name of this task.
     *
     * @return the name of this task.
     */
    public String getName() {
        return name;
    }

    /**
     * To set the name of this task.
     *
     * @param name
     *            the name to set.
     */
    public void setName(String name) {
        if (name == null) {
            return;
        }
        if (name.length() > 255) {
            throw new IllegalArgumentException(
                "The name is too long, it must have 255 chars length max : " + name);
        }
        this.name = name;
    }

    /**
     * Get the tag of this task.
     * Return null if this task has no tag.
     *
     * @return the tag of this task
     */
    public String getTag() {
        return this.tag;
    }

    /**
     * Set the tag of this task.
     * @param tag tag value
     */
    public void setTag(String tag) {
        this.tag = tag;
    }

    /**
     * Defining Control Flow Blocks with pairs of {@link FlowBlock#START} and {@link FlowBlock#END}
     * is a semantic requirement to be able to create specific control flow constructs.
     * <p>
     * Refer to the documentation for detailed instructions.
     * 
     * @return the FlowBlock attribute of this task indicating if it is the beginning or the ending
     *  of a new block scope
     */
    public FlowBlock getFlowBlock() {
        return FlowBlock.parse(this.flowBlock);
    }

    /**
     * Defining Control Flow Blocks using {@link FlowBlock#START} and {@link FlowBlock#END}
     * is a semantic requirement to be able to create specific control flow constructs.
     * <p>
     * Refer to the documentation for detailed instructions.
     * 
     * @param fb the FlowBlock attribute of this task indicating if it is the beginning 
     *  or the ending of a new block scope
     */
    public void setFlowBlock(FlowBlock fb) {
        this.flowBlock = fb.toString();
    }

    /**
     * To get the preScript of this task.
     *
     * @return the preScript of this task.
     */
    public Script<?> getPreScript() {
        return preScript;
    }

    /**
     * To set the preScript of this task.
     *
     * @param preScript
     *            the preScript to set.
     */
    public void setPreScript(Script<?> preScript) {
        this.preScript = preScript;
        this.preScript.overrideDefaultScriptName("PreScript");
    }

    /**
     * To get the postScript of this task.
     *
     * @return the postScript of this task.
     */
    public Script<?> getPostScript() {
        return postScript;
    }

    /**
     * To perform complex Control Flow Actions in a TaskFlowJob,
     * a FlowScript needs to be attached to specific tasks.
     * <p>
     * Some Control Flow constructs need to be used within the bounds of
     * a {@link FlowBlock}, which is defined a the level of the Task using
     * {@link Task#setFlowBlock(FlowBlock)}.
     * <p>
     * Refer to the user documentation for further instructions.
     * 
     * @return the Control Flow Script used for workflow actions
     */
    public FlowScript getFlowScript() {
        return flowScript;
    }

    /**
     * To perform complex Control Flow Actions in a TaskFlowJob,
     * a FlowScript needs to be attached to specific tasks.
     * <p>
     * Some Control Flow constructs need to be used within the bounds of
     * a {@link FlowBlock}, which is defined a the level of the Task using
     * {@link Task#setFlowBlock(FlowBlock)}.
     * <p>
     * Refer to the user documentation for further instructions.
     * 
     * @param s the Control Flow Script used for workflow actions
     */
    public void setFlowScript(FlowScript s) {
        this.flowScript = s;
    }

    /**
     * To set the postScript of this task.
     *
     * @param postScript
     *            the postScript to set.
     */
    public void setPostScript(Script<?> postScript) {
        this.postScript = postScript;
        this.postScript.overrideDefaultScriptName("PostScript");
    }

    /**
     * To get the cleaningScript of this task.
     *
     * @return the cleaningScript of this task.
     */
    public Script<?> getCleaningScript() {
        return cScript;
    }

    /**
     * To set the cleaningScript of this task.
     *
     * @param cleaningScript
     *            the cleaningScript to set.
     */
    public void setCleaningScript(Script<?> cleaningScript) {
        this.cScript = cleaningScript;
        this.cScript.overrideDefaultScriptName("CleanScript");
    }

    /**
     * Checks if the task is parallel.
     * @return true if the task is parallel, false otherwise.
     */
    public boolean isParallel() {
        return parallelEnvironment != null && parallelEnvironment.getNodesNumber() >= 1;
    }

    /**
     * Returns the parallel environment of the task.
     * @return the parallel environment of the task.
     */
    public ParallelEnvironment getParallelEnvironment() {
        return parallelEnvironment;
    }

    /**
     * Sets new parallel environment of the task.
     * @param parallelEnvironment new parallel environment of the task.
     */
    public void setParallelEnvironment(ParallelEnvironment parallelEnvironment) {
        this.parallelEnvironment = parallelEnvironment;
    }

    /**
     * To get the number of execution for this task.
     *
     * @return the number of times this task can be executed.
     */

    /**
     * To get the selection script. This is the script that will select a node.
     *
     * @return the selection Script.
     */
    public List<SelectionScript> getSelectionScripts() {
        if (sScripts == null || sScripts.size() == 0) {
            return null;
        } else {
            return sScripts;
        }
    }

    /**
     * Set a selection script. It is the script that will be in charge of selecting a node.
     */
    public void setSelectionScript(SelectionScript selScript) {
        if (selScript == null) {
            throw new IllegalArgumentException("The given selection script cannot be null !");
        }
        List<SelectionScript> selScriptsList = new ArrayList<SelectionScript>();
        selScriptsList.add(selScript);
        setSelectionScripts(selScriptsList);
    }

    /**
     * Set a list of selection scripts. These are the scripts that will be in charge of selecting a node.
     */
    public void setSelectionScripts(List<SelectionScript> selScriptsList) {
        this.sScripts = selScriptsList;
    }

    /**
     * To add a selection script to the list of selection script.
     *
     * @param selectionScript
     *            the selectionScript to add.
     */
    public void addSelectionScript(SelectionScript selectionScript) {
        if (selectionScript == null) {
            throw new IllegalArgumentException("The given selection script cannot be null !");
        }
        if (this.sScripts == null) {
            this.sScripts = new ArrayList<SelectionScript>();
        }
        this.sScripts.add(selectionScript);
    }

    /**
     * To get the list of dependencies of the task.
     *
     * @return the the list of dependencies of the task.
     */
    @XmlTransient
    public List<Task> getDependencesList() {
        return dependences;
    }

    /**
     * Get the number of nodes needed for this task (by default: 1).
     *
     * @return the number of Nodes Needed
     */
    public int getNumberOfNodesNeeded() {
        return isParallel() ? getParallelEnvironment().getNodesNumber() : 1;
    }

    /**
     * @return the walltime
     */
    public long getWallTime() {
        return wallTime;
    }

    /**
     * Return the working Directory.
     *
     * @return the working Directory.
     */
    public String getWorkingDir() {
        if (forkEnvironment == null) {
            return null;
        }

        return forkEnvironment.getWorkingDir();
    }

    /**
     * Set the wall time to the task in millisecond.
     * 
     * @param walltime the walltime to set in millisecond.
     */
    public void setWallTime(long walltime) {
        if (walltime < 0) {
            throw new IllegalArgumentException(
                "The walltime must be a positive or nul integer value (>=0) !");
        }
        this.wallTime = walltime;
    }

    /**
     * Return true if wallTime is set.
     * 
     * @return the isWallTime
     */
    public boolean isWallTimeSet() {
        return wallTime > 0;
    }

    /**
     * Set the number of nodes needed for this task.
     * <p>
     * This number represents the total number of nodes that you need. You may remember that
     * default number is 1.
     *
     * @param numberOfNodesNeeded the number Of Nodes Needed to set.
     */
    @Deprecated
    public void setNumberOfNeededNodes(int numberOfNodesNeeded) {
        if (parallelEnvironment != null) {
            throw new IllegalStateException(
                "Cannot set numberOfNodesNeeded as it could be inconsistent with the parallel environment");
        }
        this.parallelEnvironment = new ParallelEnvironment(numberOfNodesNeeded);
    }

    /**
     * Add the files value to the given files value
     * according to the provided access mode.
     * <p>
     * Mode define the way the files will be bring to LOCAL space.
     *
     * @param files the input Files to add
     * @param mode the way to provide files to LOCAL space
     */
    public void addInputFiles(FileSelector files, InputAccessMode mode) {
        if (files == null) {
            throw new IllegalArgumentException("Argument files is null");
        }

        if (inputFiles == null) {
            inputFiles = new ArrayList<InputSelector>();
        }

        inputFiles.add(new InputSelector(files, mode));
    }

    /**
     * Add the files value to the given files value
     * according to the provided access mode.
     * <p>
     * Mode define the way the files will be send to OUTPUT space.
     *
     * @param files the output Files to add
     * @param mode the way to send files to OUTPUT space
     */
    public void addOutputFiles(FileSelector files, OutputAccessMode mode) {
        if (files == null) {
            throw new IllegalArgumentException("Argument files is null");
        }

        if (outputFiles == null) {
            outputFiles = new ArrayList<OutputSelector>();
        }

        outputFiles.add(new OutputSelector(files, mode));
    }

    /**
     * Add the files to the given filesToInclude value
     * according to the provided access mode.
     * <p>
     * Mode define the way the files will be bring to LOCAL space.
     * filesToInclude can represent one file or many files defined by a regular expression.
     * @see FileSelector for details
     *
     * @param filesToInclude the input files to add
     * @param mode the way to provide files to LOCAL space
     */
    public void addInputFiles(String filesToInclude, InputAccessMode mode) {
        if (filesToInclude == null) {
            throw new IllegalArgumentException("Argument filesToInclude is null");
        }
        if (inputFiles == null) {
            inputFiles = new ArrayList<InputSelector>();
        }
        inputFiles.add(new InputSelector(new FileSelector(filesToInclude), mode));
    }

    /**
     * Add the files to the given filesToInclude value
     * according to the provided access mode.
     * <p>
     * Mode define the way the files will be send to OUTPUT space.
     * filesToInclude can represent one file or many files defined by a regular expression.
     * @see FileSelector for details
     *
     * @param filesToInclude the output files to add
     * @param mode the way to send files to OUTPUT space
     */
    public void addOutputFiles(String filesToInclude, OutputAccessMode mode) {
        if (filesToInclude == null) {
            throw new IllegalArgumentException("Argument filesToInclude is null");
        }
        if (outputFiles == null) {
            outputFiles = new ArrayList<OutputSelector>();
        }
        outputFiles.add(new OutputSelector(new FileSelector(filesToInclude), mode));
    }

    /**
     * Get the input file selectors list.
     * This list represents every couple of input FileSelector and its associated access mode
     * The first element is the first added couple.<br>
     * This method returns null if nothing was added to the inputFiles.
     *
     * @return the input file selectors list
     */
    public List<InputSelector> getInputFilesList() {
        return inputFiles;
    }

    /**
     * Get the output file selectors list.
     * This list represents every couple of output FileSelector and its associated access mode
     * The first element is the first added couple.<br>
     * This method returns null if nothing was added to the outputFiles.
     *
     * @return the output file selectors list
     */
    public List<OutputSelector> getOutputFilesList() {
        return outputFiles;
    }

    /**
     * Sets the variable map for this task.
     * 
     * @param variables the variables map
     */
    public void setVariables(Map<String, TaskVariable> variables) {
        this.variables = new ConcurrentHashMap<String, TaskVariable>(variables);
    }

    /**
     * Returns the variable map of this task.
     * 
     * @return a variable map
     */
    public Map<String, TaskVariable> getVariables() {
        return this.variables;
    }

    /**
     * Returns the variable map of this task.
     * 
     * @return a variable map
     */
    public Map<String, String> getVariablesOverriden(Job job) {
        Map<String, String> taskVariables = new HashMap<>();
        if (job != null){
            taskVariables.putAll(job.getVariables());
        }
        for (TaskVariable variable: getVariables().values()){
            if (!variable.isJobInherited()){
                taskVariables.put(variable.getName(), variable.getValue());
            }
        }
        return taskVariables;
    }

    @Override
    public String toString() {
        return name;
    }

    public String display() {
        String nl = System.lineSeparator();

        return "Task \'" + name + "\' : " + nl + "\tDescription = '" + description + "'" + nl +
            (restartTaskOnError.isSet()
                    ? "\trestartTaskOnError = '" + restartTaskOnError.getValue() + '\'' + nl : "") +
            (onTaskError.isSet() ? "\tonTaskError = '" + onTaskError.getValue() + '\'' + nl : "") +
            (maxNumberOfExecution.isSet() ? "\tmaxNumberOfExecution = '" +
                maxNumberOfExecution.getValue().getIntegerValue() + '\'' + nl : "") +
            "\ttag = " + tag + nl + "\tgenericInformation = {" + nl +
            Joiner.on('\n').withKeyValueSeparator("=").join(genericInformation) + nl + "}" + nl +
            "\tInputFiles = " + inputFiles + nl + "\tOutputFiles = " + outputFiles + nl +
            "\tParallelEnvironment = " + parallelEnvironment + nl + "\tFlowBlock = '" + flowBlock + "'" + nl +
            "\tSelectionScripts = " + displaySelectionScripts() + nl + "\tForkEnvironment = " +
            forkEnvironment + nl + "\tPreScript = " + ((preScript != null) ? preScript.display() : null) +
            nl + "\tPostScript = " + ((postScript != null) ? postScript.display() : null) + nl +
            "\tCleanScript = " + ((cScript != null) ? cScript.display() : null) + nl + "\tFlowScript = " +
            ((flowScript != null) ? flowScript.display() : null) + nl + "\tPreciousResult = " +
            preciousResult + nl + "\tPreciousLogs = " + preciousLogs + nl + "\tRunAsMe = " + runAsMe + nl +
            "\tWallTime = " + wallTime + nl + "\tDependences = " + dependences;
    }

    private String displaySelectionScripts() {
        if (sScripts == null)
            return null;
        String answer = "{ ";
        for (int i = 0; i < sScripts.size(); i++) {
            SelectionScript ss = sScripts.get(i);
            if (ss != null) {
                answer += ss.display() + ((i < sScripts.size() - 1) ? " , " : "");
            }
        }
        answer += " }";
        return answer;
    }

    public ForkEnvironment getForkEnvironment() {
        return forkEnvironment;
    }

    public void setForkEnvironment(ForkEnvironment forkEnvironment) {
        this.forkEnvironment = forkEnvironment;
    }

}
