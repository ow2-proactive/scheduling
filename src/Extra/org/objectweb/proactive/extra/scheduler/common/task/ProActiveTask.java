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
 * Definition of a ProActive task for the user.
 * This allows you to create a ProActive task.
 * You have to specify the number of nodes you need during the execution using the {@link #setNumberOfNodesNeeded(int)} method.
 * You can also specify arguments to give to the task using the {@link #setArguments(Map)}
 *
 * @author jlscheef - ProActiveTeam
 * @version 1.0, Sept 14, 2007
 * @since ProActive 3.2
 */
public class ProActiveTask extends Task {

    /** Serial version UID */
    private static final long serialVersionUID = 4400146311851234189L;

    /** Task as an instance */
    private ProActiveExecutable taskInstance = null;

    /** or as a class */
    private Class<ProActiveExecutable> taskClass = null;

    /** Arguments of the task as a map */
    private Map<String, String> args = new HashMap<String, String>();

    /**
     * Empty constructor.
     */
    public ProActiveTask() {
    }

    /**
     * Should be never used in this context.
     */
    @Override
    public void addDependence(Task task) {
        throw new RuntimeException(
            "ProActiveTask.addDependence(Task) Should be never used in this context !");
    }

    /**
     * Should be never used in this context.
     */
    @Override
    public void addDependences(List<Task> tasks) {
        throw new RuntimeException(
            "ProActiveTask.addDependences(List<Task>) Should be never used in this context !");
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
     * To set the executable task instance.
     * It may be an instance that extends {@link ProActiveExecutable}.
     *
     * @param taskInstance the task Instance to set.
     */
    public void setTaskInstance(ProActiveExecutable taskInstance) {
        this.taskInstance = taskInstance;
        this.taskClass = null;
    }

    /**
     * Return the task arguments list as an hash map.
     *
     * @return the arguments list.
     */
    public Map<String, String> getArguments() {
        return args;
    }

    /**
     * Set the task arguments list to this task.
     *
     * @param the arguments list to set
     */
    public void setArguments(Map<String, String> args) {
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
