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
package org.objectweb.proactive.extra.scheduler.common.task;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Definition of an application task for the user.
 *
 * @author ProActive Team
 * @version 1.0, Sept 14, 2007
 * @since ProActive 3.2
 */
public class ApplicationTask extends Task {

    /** Serial version UID */
    private static final long serialVersionUID = 4400146311851234189L;

    /** Task as an instance */
    private ExecutableApplicationTask taskInstance = null;

    /** or as a class */
    private Class<ExecutableApplicationTask> taskClass = null;

    /** Arguments of the task as a map */
    private Map<String, Object> args = new HashMap<String, Object>();

    /**
     * Empty constructor.
     */
    public ApplicationTask() {
    }

    /**
     * Should be never used in this context.
     */
    @Override
    public void addDependence(Task task) {
        throw new RuntimeException(
            "ApplicationTask.addDependence(Task) Should be never used in this context !");
    }

    /**
     * Should be never used in this context.
     */
    @Override
    public void addDependences(List<Task> tasks) {
        throw new RuntimeException(
            "ApplicationTask.addDependences(List<Task>) Should be never used in this context !");
    }

    /**
     * Set the number of nodes needed for this task.
     * (by default : 1)
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
    public Class<ExecutableApplicationTask> getTaskClass() {
        return taskClass;
    }

    /**
     * To set the executable task class.
     * It may be a class that extends {@link ExecutableApplicationTask}.
     *
     * @param taskClass the task Class to set.
     */
    public void setTaskClass(Class<ExecutableApplicationTask> taskClass) {
        this.taskClass = taskClass;
        this.taskInstance = null;
    }

    /**
     * To get the executable task as an instance.
     *
     * @return the task Instance.
     */
    public ExecutableApplicationTask getTaskInstance() {
        return taskInstance;
    }

    /**
     * To set the executable task instance.
     * It may be an instance that extends {@link ExecutableApplicationTask}.
     *
     * @param taskInstance the task Instance to set.
     */
    public void setTaskInstance(ExecutableApplicationTask taskInstance) {
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
    public void addArgument(String name, Object value) {
        args.put(name, value);
    }
}
