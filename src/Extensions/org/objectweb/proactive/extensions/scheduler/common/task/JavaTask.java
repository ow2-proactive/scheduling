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

    /** Task as an instance */
    private JavaExecutable taskInstance = null;

    /** or as a class */
    private Class<JavaExecutable> taskClass = null;

    /** Arguments of the task as a map */
    private Map<String, String> args = new HashMap<String, String>();

    /** if the task will be executed in a separate JVM */
    private boolean fork;

    /* Path to directory with Java installed, to this path '/bin/java' will be added. 
     * If the path is null only 'java' command will be called
     */
    private String javaHome = null;

    /* options passed to Java (not an application) (example: memory settings or properties) */
    private String javaOptions = null;

    /**
     * Empty constructor.
     */
    public JavaTask() {
    }

    /**
     * To get the executable task as a class.
     *
     * @return the task Class.
     */
    public Class<JavaExecutable> getTaskClass() {
        return taskClass;
    }

    /**
     * To set the executable task class.
     * It may be a class that extends {@link JavaExecutable}.
     *
     * @param taskClass the task Class to set.
     */
    public void setTaskClass(Class<JavaExecutable> taskClass) {
        if (!TaskConstructorTools.hasEmptyConstructor(taskClass)) {
            throw new RuntimeException("WARNING : The executable class '" + taskClass +
                "' must have a public no parameter constructor !");
        }
        this.taskClass = taskClass;
        this.taskInstance = null;
    }

    /**
     * To get the executable task as an instance.
     *
     * @return the task Instance.
     */
    public JavaExecutable getTaskInstance() {
        return taskInstance;
    }

    /**
     * To set the executable task instance.
     * It may be an instance that extends {@link JavaExecutable}.
     *
     * @param taskInstance the task Instance to set.
     */
    public void setTaskInstance(JavaExecutable taskInstance) {
        if (!TaskConstructorTools.hasEmptyConstructor(taskInstance.getClass())) {
            throw new RuntimeException("WARNING : The executable class '" + taskInstance.getClass() +
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
     * @return the javaHome
     */
    public String getJavaHome() {
        return javaHome;
    }

    /**
     * @param javaHome the javaHome to set
     */
    public void setJavaHome(String javaHome) {
        this.javaHome = javaHome;
    }

    /**
     * @return the javaOptions
     */
    public String getJavaOptions() {
        return javaOptions;
    }

    /**
     * @param javaOptions the javaOptions to set
     */
    public void setJavaOptions(String javaOptions) {
        this.javaOptions = javaOptions;
    }
}
