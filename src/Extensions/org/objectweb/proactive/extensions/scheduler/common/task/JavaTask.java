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
import java.util.Map;
import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.extensions.scheduler.common.job.TaskFlowJob;
import org.objectweb.proactive.extensions.scheduler.common.task.executable.JavaExecutable;
import org.objectweb.proactive.extensions.scheduler.common.task.util.TaskConstructorTools;
import org.objectweb.proactive.extensions.scheduler.task.ForkEnvironment;


/**
 * Use this class to build a java task that will use a {@link JavaExecutable} and be integrated in a {@link TaskFlowJob}.<br>
 * A java task includes an {@link JavaExecutable} that can be set as a .class file or instance.<br>
 * It also provides method to personalize it.
 *
 * @author The ProActive Team
 * @version 3.9, Sept 14, 2007
 * @since ProActive 3.9
 */
@PublicAPI
public class JavaTask extends Task {

    /** Classname of the executable */
    private String executableClassName = null;

    /** Arguments of the task as a map */
    private Map<String, String> args = new HashMap<String, String>();

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
        this.executableClassName = executableClassName;
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
     * Add an argument to the list of arguments.
     *
     * @param name the name of the argument to add.
     * @param value the associated value to add.
     */
    public void addArgument(String name, String value) {
        args.put(name, value);
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
