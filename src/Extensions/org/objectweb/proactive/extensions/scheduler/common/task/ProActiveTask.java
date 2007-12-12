/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.extensions.scheduler.common.task;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.extensions.scheduler.common.job.ProActiveJob;
import org.objectweb.proactive.extensions.scheduler.common.task.executable.ProActiveExecutable;
import org.objectweb.proactive.extensions.scheduler.common.task.util.TaskConstructorTools;


/**
 * Use this class to build a ProActive task that will use a {@link ProActiveExecutable} and be integrated in a {@link ProActiveJob}.<br>
 * You have to specify the number of nodes you need during the execution using the {@link #setNumberOfNodesNeeded(int)} method.<br>
 * You can also specify arguments to give to the task using the {@link #setArguments(Map)} as the java task does it.
 *
 * @author jlscheef - ProActiveTeam
 * @version 3.9, Sept 14, 2007
 * @since ProActive 3.9
 */
@PublicAPI
public class ProActiveTask extends Task {

    /** Task as an instance */
    private ProActiveExecutable taskInstance = null;

    /** or as a class */
    private Class<ProActiveExecutable> taskClass = null;

    /** Arguments of the task as a map */
    private Map<String, Object> args = new HashMap<String, Object>();

    /**
     * Empty constructor.
     */
    public ProActiveTask() {
    }

    /**
     * <font color="red">Should be never used in this context.</font>
     */
    @Override
    public void addDependence(Task task) {
        throw new RuntimeException(
            "ProActiveTask.addDependence(Task) Should be never used in this context !");
    }

    /**
     * <font color="red">Should be never used in this context.</font>
     */
    @Override
    public void addDependences(List<Task> tasks) {
        throw new RuntimeException(
            "ProActiveTask.addDependences(List<Task>) Should be never used in this context !");
    }

    /**
     * Set the number of nodes needed for this task.<br />
     * This number represents the total number of nodes that you need. You may remember that
     * one node is used to start your task. So if you ask for 11 nodes, 10 would be given to your
     * ProActive executable task.
     * (Default number is 1)
     *
     * @param numberOfNodesNeeded the number Of Nodes Needed to set.
     */
    public void setNumberOfNodesNeeded(int numberOfNodesNeeded) {
        if (numberOfNodesNeeded < 1) {
            numberOfNodesNeeded = 1;
        }

        this.numberOfNodesNeeded = numberOfNodesNeeded;
    }

    /**
     * To get the executable task as a class.
     *
     * @return the task Class.
     */
    public Class<ProActiveExecutable> getTaskClass() {
        return taskClass;
    }

    /**
     * To set the executable task class.
     * It may be a class that extends {@link ProActiveExecutable}.
     *
     * @param taskClass the task Class to set.
     */
    public void setTaskClass(Class<ProActiveExecutable> taskClass) {
        if (!TaskConstructorTools.hasEmptyConstructor(taskClass)) {
            throw new RuntimeException("WARNING : The executable class '" +
                taskClass + "' must have a public no parameter constructor !");
        }
        this.taskClass = taskClass;
        this.taskInstance = null;
    }

    /**
     * To get the executable task as an instance.
     *
     * @return the task Instance.
     */
    public ProActiveExecutable getTaskInstance() {
        return taskInstance;
    }

    /**
     * To set the executable task instance.<br>
     * It may be an instance that extends {@link ProActiveExecutable}.
     *
     * @param taskInstance the task Instance to set.
     */
    public void setTaskInstance(ProActiveExecutable taskInstance) {
        if (!TaskConstructorTools.hasEmptyConstructor(taskInstance.getClass())) {
            throw new RuntimeException("WARNING : The executable class '" +
                taskInstance.getClass() +
                "' must have a public no parameter constructor !");
        }
        this.taskInstance = taskInstance;
        this.taskClass = null;
    }

    /**
     * Return the task arguments list as an hash map.
     *
     * @return the arguments list.
     */
    public Map<String, Object> getArguments() {
        return args;
    }

    /**
     * Set the task arguments list to this task.
     *
     * @param the arguments list to set
     */
    public void setArguments(Map<String, Object> args) {
        this.args = args;
    }

    /**
     * Add an argument to the list of arguments.
     *
     * @param name the name of the argument to add.
     * @param value the associated value to add.
     */
    public void addArgument(String name, String value) {
        args.put(name, value);
    }
}
