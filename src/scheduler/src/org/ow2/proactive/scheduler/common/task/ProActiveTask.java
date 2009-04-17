/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
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
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.common.task;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.proactive.annotation.PublicAPI;
import org.ow2.proactive.scheduler.common.job.ProActiveJob;
import org.ow2.proactive.scheduler.common.task.executable.ProActiveExecutable;


/**
 * Use this class to build a ProActive task that will use a {@link ProActiveExecutable} and be integrated in a {@link ProActiveJob}.<br>
 * You have to specify the number of nodes you need during the execution using the {@link #setNumberOfNodesNeeded(int)} method.<br>
 * You can also specify arguments to give to the task using the {@link #setArguments(Map)} as the java task does it.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
@PublicAPI
public class ProActiveTask extends Task {
    /**
     *
     */
    private static final long serialVersionUID = 10L;

    /** Classname of the executable */
    private String executableClassName = null;

    /** Arguments of the task as a map */
    private Map<String, String> arguments = new HashMap<String, String>();

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
        throw new RuntimeException("ProActiveTask.addDependence(Task) Should be never used in this context !");
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
        if (this.numberOfNodesNeeded < 1) {
            this.numberOfNodesNeeded = 1;
        }

        this.numberOfNodesNeeded = numberOfNodesNeeded;
    }

    /**
     * To get the executable task class name.
     *
     * @return the task Class name.
     */
    public String getExecutableClassName() {
        return executableClassName;
    }

    /**
     * To set the executable task class name.
     * It may be a class that extends {@link ProActiveExecutable}.
     *
     * @param executableClassName the task Class to set.
     */
    public void setExecutableClassName(String executableClassName) {
        this.executableClassName = executableClassName;
    }

    /**
     * Return the task arguments list as an hash map.
     *
     * @return the arguments list.
     */
    public Map<String, String> getArguments() {
        return this.arguments;
    }

    /**
     * Set the task arguments list to this task.
     *
     * @param args the arguments list to set
     */
    public void setArguments(Map<String, String> args) {
        this.arguments = args;
    }

    /**
     * Add an argument to the list of arguments.
     *
     * @param name the name of the argument to add.
     * @param value the associated value to add.
     */
    public void addArgument(String name, String value) {
        if (name != null && name.length() > 255) {
            throw new IllegalArgumentException("Key is too long, it must have 255 chars length max : " + name);
        }
        this.arguments.put(name, value);
    }
}
