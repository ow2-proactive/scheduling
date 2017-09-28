/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.ow2.proactive.scheduler.common.task;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.objectweb.proactive.annotation.PublicAPI;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.util.AllObjects2BytesConverterHandler;


/**
 * Use this class to build a java task that will use a
 * {@link org.ow2.proactive.scheduler.common.task.executable.JavaExecutable} and be integrated in a
 * {@link TaskFlowJob}.
 * <p>
 * A java task includes an {@link org.ow2.proactive.scheduler.common.task.executable.JavaExecutable} that can be set
 * as a .class file or instance.
 * <p>
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
    // WARNING: this field is accessed by reflection from InternalJobFactory
    private final HashMap<String, byte[]> serializedArguments = new HashMap<>();

    /** For internal use: name of the field that stores task arguments */
    public static final String ARGS_FIELD_NAME = "serializedArguments";

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
     * It must be a class that extends {@link org.ow2.proactive.scheduler.common.task.executable.JavaExecutable}.
     *
     * @param executableClassName the task Class to set.
     */
    public void setExecutableClassName(String executableClassName) {
        if (executableClassName == null) {
            throw new IllegalArgumentException("Executable class name must be set for JavaTask: " + this.name);
        }
        if (executableClassName.length() > 255) {
            throw new IllegalArgumentException("Class name is too long, it must have 255 chars length max: " +
                                               executableClassName);
        }
        this.executableClassName = executableClassName;
    }

    /**
     * Return an unmodifiable copy of all the task arguments as a hash map.
     *
     * @return an unmodifiable copy of the arguments list.
     * @throws IOException if the copy of the value cannot be performed.
     * @throws ClassNotFoundException if the value's class cannot be loaded.
     */
    public Map<String, Serializable> getArguments() throws IOException, ClassNotFoundException {
        final Set<String> allNames = this.serializedArguments.keySet();
        final Map<String, Serializable> deserialized = new HashMap<String, Serializable>(allNames.size());
        for (String name : allNames) {
            deserialized.put(name, this.getArgument(name));
        }
        return Collections.unmodifiableMap(deserialized);
    }

    public HashMap<String, byte[]> getSerializedArguments() {
        return new HashMap<>(serializedArguments);
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
        byte[] serialized = null;
        try {
            serialized = AllObjects2BytesConverterHandler.convertObject2Byte(name, value);
            this.serializedArguments.put(name, serialized);
        } catch (Exception e) {
            throw new IllegalArgumentException("Cannot add argument " + name + " in task " + this.name, e);
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
            return (Serializable) AllObjects2BytesConverterHandler.convertByte2Object(serializedValue);
        } else {
            return null;
        }
    }

    /**
     * Remove the specified argument from the argument map.
     * @param name the name of the specified argument.
     * @return a copy of the value of the specified argument.
     * @throws IOException if the copy of the value cannot be performed.
     * @throws ClassNotFoundException if the value's class cannot be loaded.
     */
    public Serializable removeArgument(String name) throws IOException, ClassNotFoundException {
        byte[] serializedValue = this.serializedArguments.remove(name);
        if (serializedValue != null) {
            return (Serializable) AllObjects2BytesConverterHandler.convertByte2Object(serializedValue);
        } else {
            return null;
        }
    }

    /**
     * @return true if the task will be executed in a separate JVM
     */
    public boolean isFork() {
        return this.forkEnvironment != null || super.isWallTimeSet() || super.isRunAsMe();
    }

    @Override
    public String display() {
        String nl = System.lineSeparator();
        String answer = super.display();
        return answer + nl + "\tExecutableClassName = '" + executableClassName + '\'' + nl + "\tArguments = " +
               serializedArguments.keySet() + nl + "\tForkEnvironment = " + forkEnvironment;
    }

}
