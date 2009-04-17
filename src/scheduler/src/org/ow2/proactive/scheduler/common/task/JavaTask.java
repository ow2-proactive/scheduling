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
import java.util.Map;

import org.objectweb.proactive.annotation.PublicAPI;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.executable.JavaExecutable;


/**
 * Use this class to build a java task that will use a {@link JavaExecutable} and be integrated in a {@link TaskFlowJob}.<br>
 * A java task includes an {@link JavaExecutable} that can be set as a .class file or instance.<br>
 * It also provides method to personalize it.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
@PublicAPI
public class JavaTask extends Task {

    /** Classname of the executable */
    private String executableClassName = null;

    /** Arguments of the task as a map */
    private Map<String, String> arguments = new HashMap<String, String>();

    /** if the task will be executed in a separate JVM */
    private boolean fork;

    /** Environment of a new dedicated JVM */
    private ForkEnvironment forkEnvironment = null;

    /**
     * Empty constructor.
     */
    public JavaTask() {
    }

    /**
     * To get the executable task classname.
     *
     * @return the task Class name.
     */
    public String getExecutableClassName() {
        return executableClassName;
    }

    /**
     * To set the executable task class name.
     * It may be a class that extends {@link JavaExecutable}.
     *
     * @param executableClassName the task Class to set.
     */
    public void setExecutableClassName(String executableClassName) {
        if (executableClassName == null) {
            throw new IllegalArgumentException("Executable class name must be set for JavaTask : " +
                this.name);
        }
        if (executableClassName.length() > 255) {
            throw new IllegalArgumentException(
                "Class name is too long, it must have 255 chars length max : " + executableClassName);
        }
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

    /**
     * @return the fork if the task will be executed in a separate JVM 
     */
    public boolean isFork() {
        return fork;
    }

    /**
     * @param fork the fork to set - if the task will be executed in a separate JVM 
     */
    public void setFork(boolean fork) {
        this.fork = fork;
    }

    /**
     * Returns the forkEnvironment.
     *
     * @return the forkEnvironment.
     */
    public ForkEnvironment getForkEnvironment() {
        return forkEnvironment;
    }

    /**
     * Sets the forkEnvironment to the given forkEnvironment value.
     *
     * @param forkEnvironment the forkEnvironment to set.
     */
    public void setForkEnvironment(ForkEnvironment forkEnvironment) {
        this.forkEnvironment = forkEnvironment;
    }

}
