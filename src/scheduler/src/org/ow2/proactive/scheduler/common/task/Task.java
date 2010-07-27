/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
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
 * If needed, contact us to obtain a release under GPL Version 2 
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.common.task;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.AnyMetaDef;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.annotations.ManyToAny;
import org.hibernate.annotations.MetaValue;
import org.hibernate.annotations.Proxy;
import org.objectweb.proactive.annotation.PublicAPI;
import org.ow2.proactive.scheduler.common.SchedulerConstants;
import org.ow2.proactive.scheduler.common.task.dataspaces.FileSelector;
import org.ow2.proactive.scheduler.common.task.dataspaces.InputAccessMode;
import org.ow2.proactive.scheduler.common.task.dataspaces.InputSelector;
import org.ow2.proactive.scheduler.common.task.dataspaces.OutputAccessMode;
import org.ow2.proactive.scheduler.common.task.dataspaces.OutputSelector;
import org.ow2.proactive.scripting.Script;
import org.ow2.proactive.scripting.SelectionScript;
import org.ow2.proactive.scripting.SimpleScript;


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
@MappedSuperclass
@Table(name = "TASK")
@AccessType("field")
@Proxy(lazy = false)
public abstract class Task extends CommonAttribute {

    /** Number of nodes asked by the user. */
    @Column(name = "NEEDED_NODES")
    protected int numberOfNodesNeeded = 1;

    /** Name of the task. */
    @Column(name = "NAME")
    protected String name = SchedulerConstants.TASK_DEFAULT_NAME;

    /** Description of the task. */
    @Column(name = "DESCRIPTION", length = Integer.MAX_VALUE)
    @Lob
    protected String description = null;

    /** Description of the result of the task */
    @Column(name = "RESULTPREVIEW", length = Integer.MAX_VALUE)
    @Lob
    protected String resultPreview;

    /** DataSpace inputFiles */
    @OneToMany
    @LazyCollection(value = LazyCollectionOption.FALSE)
    @Cascade(CascadeType.ALL)
    protected List<InputSelector> inputFiles = null;

    /** DataSpace outputFiles */
    @OneToMany
    @LazyCollection(value = LazyCollectionOption.FALSE)
    @Cascade(CascadeType.ALL)
    protected List<OutputSelector> outputFiles = null;

    /**
     * selection script : can be launched before getting a node in order to
     * verify some computer specificity.
     */
    @ManyToAny(metaColumn = @Column(name = "S_SCRIPT", length = 5))
    @AnyMetaDef(idType = "long", metaType = "string", metaValues = { @MetaValue(targetEntity = SelectionScript.class, value = "SS") })
    @JoinTable(joinColumns = @JoinColumn(name = "SS_ID"), inverseJoinColumns = @JoinColumn(name = "DEPEND_ID"))
    @LazyCollection(value = LazyCollectionOption.FALSE)
    @Cascade(CascadeType.ALL)
    protected List<SelectionScript> sScripts;

    /**
     * PreScript : can be used to launch script just before the task
     * execution.
     */
    @Cascade(CascadeType.ALL)
    @OneToOne(fetch = FetchType.EAGER, targetEntity = SimpleScript.class)
    protected Script<?> preScript;

    /**
     * PostScript : can be used to launch script just after the task
     */
    @Cascade(CascadeType.ALL)
    @OneToOne(fetch = FetchType.EAGER, targetEntity = SimpleScript.class)
    protected Script<?> postScript;

    /**
     * CleaningScript : can be used to launch script just after the task or the postScript (if set)
     * Started even if a problem occurs.
     */
    @Cascade(CascadeType.ALL)
    @OneToOne(fetch = FetchType.EAGER, targetEntity = SimpleScript.class)
    protected Script<?> cScript;

    /** Tell whether this task has a precious result or not. */
    @Column(name = "PRECIOUS_RESULT")
    protected boolean preciousResult;

    /** List of dependences if necessary. */
    @ManyToAny(metaColumn = @Column(name = "TASK_TYPE", length = 5))
    @AnyMetaDef(idType = "long", metaType = "string", metaValues = {
            @MetaValue(targetEntity = JavaTask.class, value = "IJT"),
            @MetaValue(targetEntity = NativeTask.class, value = "INT") })
    @JoinTable(joinColumns = @JoinColumn(name = "TASK_ID"), inverseJoinColumns = @JoinColumn(name = "DEPEND_ID"))
    @LazyCollection(value = LazyCollectionOption.FALSE)
    @Cascade(CascadeType.ALL)
    private List<Task> dependences = null;

    /** maximum execution time of the task (in milliseconds), the variable is only valid if isWallTime is true */
    @Column(name = "WALLTIME")
    protected long wallTime = 0;

    /**
     * Add a dependence to the task. <font color="red">Warning : the dependence order is very
     * important.</font><br>
     * In fact, it is in this order that you will get back the result in the children task.<br>
     * For example : if you add to the task t3, the dependences t1 then t2, the parents of t3 will be t1 and t2 in this order
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
     * Return the result preview of this task.
     *
     * @return the result preview of this task.
     */
    public String getResultPreview() {
        return resultPreview;
    }

    /**
     * Set the result preview of this task.
     *
     * @param resultPreview
     *            the result preview  to set.
     */
    public void setResultPreview(String resultPreview) {
        this.resultPreview = resultPreview;
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
        if (name != null && name.length() > 255) {
            throw new IllegalArgumentException("The name is too long, it must have 255 chars length max : " +
                name);
        }
        this.name = name;
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
     * To set the postScript of this task.
     *
     * @param postScript
     *            the postScript to set.
     */
    public void setPostScript(Script<?> postScript) {
        this.postScript = postScript;
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
     * To get the list of dependences of the task.
     *
     * @return the the list of dependences of the task.
     */
    public List<Task> getDependencesList() {
        return dependences;
    }

    /**
     * Get the number of nodes needed for this task. (by default : 1)
     *
     * @return the number Of Nodes Needed
     */
    public int getNumberOfNodesNeeded() {
        return numberOfNodesNeeded;
    }

    /**
     * @return the walltime
     */
    public long getWallTime() {
        return wallTime;
    }

    /**
     * Set the wall time to the task in millisecond.
     * 
     * @param walltime the walltime to set in millisecond.
     */
    public void setWallTime(long walltime) {
        if (walltime < 0) {
            throw new IllegalArgumentException("The walltime must be a positive or nul integer value (>=0) !");
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
     * Set the number of nodes needed for this task.<br />
     * This number represents the total number of nodes that you need. You may remember that
     * (Default number is 1)
     *
     * @param numberOfNodesNeeded the number Of Nodes Needed to set.
     */
    public void setNumberOfNeededNodes(int numberOfNodesNeeded) {
        if (this.numberOfNodesNeeded < 1) {
            this.numberOfNodesNeeded = 1;
        }
        this.numberOfNodesNeeded = numberOfNodesNeeded;
    }

    /**
     * Add the files value to the given files value
     * according to the provided access mode.<br />
     * mode define the way the files will be bring to LOCAL space.
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
     * according to the provided access mode.<br />
     * mode define the way the files will be send to OUTPUT space.
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
     * according to the provided access mode.<br />
     * mode define the way the files will be bring to LOCAL space.
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
        inputFiles.add(new InputSelector(new FileSelector(new String[] { filesToInclude }), mode));
    }

    /**
     * Add the files to the given filesToInclude value
     * according to the provided access mode.<br />
     * mode define the way the files will be send to OUTPUT space.
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
        outputFiles.add(new OutputSelector(new FileSelector(new String[] { filesToInclude }), mode));
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

}
