/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
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

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.Constants;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.LocalBodyStore;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.future.Future;
import org.objectweb.proactive.core.body.future.FutureID;
import org.objectweb.proactive.core.body.future.FuturePool;
import org.objectweb.proactive.core.body.future.MethodCallResult;
import org.objectweb.proactive.core.mop.MOP;
import org.objectweb.proactive.core.mop.StubObject;
import org.objectweb.proactive.core.util.converter.ByteToObjectConverter;
import org.objectweb.proactive.core.util.converter.ObjectToByteConverter;
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
    protected String executableClassName = null;

    /** Arguments of the task as a map */
    private final Map<String, byte[]> serializedArguments = new HashMap<String, byte[]>();

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
     * Return a copy of all the task arguments as a hash map.
     *
     * @return a copy of the arguments list.
     * @throws IOException if the copy of the value cannot be performed.
     * @throws ClassNotFoundException if the value's class cannot be loaded.
     */
    public Map<String, Serializable> getArguments() throws IOException, ClassNotFoundException {
        final Set<String> allNames = this.getArgumentsNames();
        final Map<String, Serializable> deserialized = new HashMap<String, Serializable>(allNames.size());
        for (String name : allNames) {
            deserialized.put(name, this.getArgument(name));
        }
        return deserialized;
    }

    /**
     * Return a map containing all the task arguments serialized as byte[].
     *
     * @return the serialized arguments map.
     */
    public Map<String, byte[]> getSerializedArguments() {
        return new HashMap<String, byte[]>(this.serializedArguments);
    }

    /**
     * Add an argument to the list of arguments. Note that the value is serialized and stored
     * in the JavaTask.
     *
     * @param name the name of the argument to add.
     * @param value the associated value to add.
     * @throws IllegalArgumentException if the value cannot be serialized and stored in the task.
     */
    public void addArgument(String name, Serializable value) {
        if (name != null && name.length() > 255) {
            throw new IllegalArgumentException("Key is too long, it must have 255 chars length max : " + name);
        } else {
            byte[] serialized = null;
            try {
                serialized = ObjectToByteConverter.ObjectStream.convert(value);
                this.serializedArguments.put(name, serialized);
            } catch (IOException e) {
                throw new IllegalArgumentException("Cannot add argument " + name + " in task " + this.name, e);
            }
        }
    }

    /**
     * Return a copy of the value of the specified argument.
     * @param name the name of the specified argument.
     * @return a copy of the value of the specified argument.
     * @throws IOException if the copy of the value cannot be performed.
     * @throws ClassNotFoundException if the value's class cannot be loaded.
     */
    public Serializable getArgument(String name) throws IOException, ClassNotFoundException {
        byte[] serializedValue = this.serializedArguments.get(name);
        if (serializedValue != null) {
            return (Serializable) ByteToObjectConverter.ObjectStream.convert(serializedValue);
        } else {
            return null;
        }
    }

    /**
     * Return the set of all the argument names for this task.
     * @return Return the set of all the argument names for this task.
     */
    public Set<String> getArgumentsNames() {
        return this.serializedArguments.keySet();
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
