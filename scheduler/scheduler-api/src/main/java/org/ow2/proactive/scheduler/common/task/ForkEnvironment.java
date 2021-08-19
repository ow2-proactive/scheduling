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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import org.objectweb.proactive.annotation.PublicAPI;
import org.ow2.proactive.scheduler.common.exception.ExecutableCreationException;
import org.ow2.proactive.scripting.InvalidScriptException;
import org.ow2.proactive.scripting.Script;
import org.ow2.proactive.scripting.SimpleScript;

import com.google.common.base.Preconditions;


/**
 * Class representing a forked environment of a JVM created specifically for an execution of a Java Task.
 * A Java Task can be executed in the current JVM - then all Java Tasks are dependent on the same JVM (provider) and JVM options (like memory),
 * or can be executed in a dedicated JVM with additional options specified like {@code javaHome}, {@code javaArguments}, {@code classpath}, ...
 *
 * @author ProActive team
 */
@PublicAPI
@XmlAccessorType(XmlAccessType.FIELD)
public class ForkEnvironment implements Serializable {

    public static final String DOCKER_FORK_WINDOWS2LINUX = "pa.scheduler.task.docker.windows2linux";

    /**
     * Path to directory with Java installed, to this path '/bin/java' will be added.
     * If the path is null only 'java' command will be called.
     */
    private String javaHome;

    /**
     * Path to the working directory.
     */
    private String workingDir;

    /**
     * User custom system environment.
     */
    private Map<String, String> systemEnvironment;

    /**
     * Arguments passed to Java (not an application) (example: memory settings or properties).
     */
    private List<String> jvmArguments;

    /**
     * Additional classpath when new JVM will be started.
     */
    private List<String> additionalClasspath;

    /**
     * EnvScript: can be used to initialize environment just before JVM fork.
     */
    private Script<?> script;

    /**
     * Command and parameters to add before java executable
     */
    private List<String> preJavaCommand;

    /**
     * Does the current fork environment aims at running a linux docker container on a windows host?
     */
    private boolean isDockerWindowsToLinux = false;

    public ForkEnvironment() {
        additionalClasspath = new ArrayList<>();
        jvmArguments = new ArrayList<>();
        systemEnvironment = new HashMap<>();
        preJavaCommand = new ArrayList<>();
    }

    /**
     * Copy constructor.
     * 
     * @param forkEnvironment the object to copy
     */
    public ForkEnvironment(ForkEnvironment forkEnvironment) {
        this();

        if (forkEnvironment.javaHome != null) {
            this.javaHome = forkEnvironment.javaHome;
        }

        if (forkEnvironment.workingDir != null) {
            this.workingDir = forkEnvironment.workingDir;
        }

        if (forkEnvironment.systemEnvironment != null) {
            this.systemEnvironment.putAll(forkEnvironment.systemEnvironment);
        }

        if (forkEnvironment.jvmArguments != null) {
            for (String entry : forkEnvironment.jvmArguments) {
                this.addJVMArgument(entry);
            }
        }

        if (forkEnvironment.additionalClasspath != null) {
            for (String entry : forkEnvironment.additionalClasspath) {
                this.addAdditionalClasspath(entry);
            }
        }

        if (forkEnvironment.preJavaCommand != null) {
            this.setPreJavaCommand(forkEnvironment.preJavaCommand);
        }

        if (forkEnvironment.script != null) {
            this.script = new SimpleScript(forkEnvironment.script);
        }
    }

    public ForkEnvironment(String workingDir) {
        this();
        this.workingDir = workingDir;
    }

    /**
     * Returns the javaHome.
     *
     * @return the javaHome.
     */
    public String getJavaHome() {
        return javaHome;
    }

    /**
     * Sets the javaHome to the given javaHome value.
     *
     * @param javaHome the javaHome to set.
     */
    public void setJavaHome(String javaHome) {
        this.javaHome = javaHome;
    }

    /**
     * Return the working Directory.
     *
     * @return the working Directory.
     */
    public String getWorkingDir() {
        return workingDir;
    }

    /**
     * Set the working directory value to the given workingDir value.
     *
     * @param workingDir the working directory to set
     */
    public void setWorkingDir(String workingDir) {
        this.workingDir = workingDir;
    }

    /**
     * Return a copy of the system environment, empty map if no variables.
     *
     * @return a copy of the system environment, empty map if no variables.
     */
    public Map<String, String> getSystemEnvironment() {
        if (systemEnvironment == null) {
            return new HashMap<>(0);
        }

        return new HashMap<>(systemEnvironment);
    }

    /**
     * Get the system environment variable value associated with the given name.
     *
     * @param name the name of the variable value to get
     * @return the system variable value associated with the given name, or {@code null} if the variable does not exist.
     */
    public String getSystemEnvironmentVariable(String name) {
        if (systemEnvironment == null) {
            return null;
        }

        return systemEnvironment.get(name);
    }

    /**
     * Add a new system environment variables value from its name and value.
     *
     * @param name the name of the variable to add
     * @param value the the value associated to the given name
     * @throws IllegalArgumentException if name is null
     *
     * @return the previous value associated to the system environment variable.
     */
    public String addSystemEnvironmentVariable(String name, String value) {
        if (name == null) {
            throw new IllegalArgumentException("Name cannot be null");
        }

        return systemEnvironment.put(name, value);
    }

    /**
     * Return a copy of the JVM arguments, empty list if no arguments.
     *
     * @return a copy of the JVM arguments, empty list if no arguments.
     */
    public List<String> getJVMArguments() {
        if (jvmArguments == null) {
            return new ArrayList<>(0);
        }

        return new ArrayList<>(jvmArguments);
    }

    /**
     * Add a new JVM argument value. (-Dname=value, -Xmx=.., -server)
     *
     * @param value the value of the property to be added.
     * @throws IllegalArgumentException if value is null.
     */
    public void addJVMArgument(String value) {
        Preconditions.checkNotNull(value);
        jvmArguments.add(value);
    }

    /**
     * Return a copy of the additional classpath, empty list if no arguments.
     *
     * @return a copy of the additional classpath, empty list if no arguments.
     */
    public List<String> getAdditionalClasspath() {
        if (additionalClasspath == null) {
            return new ArrayList<>(0);
        }

        return new ArrayList<>(additionalClasspath);
    }

    /**
     * Add one or more classpath entries to the forked JVM classpath
     *
     * @param values one or more classpath entries
     */
    public void addAdditionalClasspath(String... values) {
        for (String value : values) {
            addAdditionalClasspath(value);
        }
    }

    /**
     * Add a new additional Classpath value.
     *
     * @param value the additional Classpath to add.
     * @throws IllegalArgumentException if value is null.
     */
    public void addAdditionalClasspath(String value) {
        Preconditions.checkNotNull(value);
        this.additionalClasspath.add(value);
    }

    /**
     * Returns the list of (command + argument) which will be prepended to the java command
     * e.g. ["docker", "run", "--rm"]
     * @return a list containing the command + arguments
     */
    public List<String> getPreJavaCommand() {
        return preJavaCommand;
    }

    /**
     * Sets the list of (command + argument) which will be prepended to the java command
     * @param preJavaCommand a list containing the command + arguments, e.g. ["docker", "run", "--rm"]
     */
    public void setPreJavaCommand(List<String> preJavaCommand) {
        this.preJavaCommand = preJavaCommand;
    }

    /**
     * Add an item to the list of (command + argument) which will be prepended to the java command
     * @param commandOrParameter the command (e.g. "docker") or an argument (e.g. "run")
     */
    public void addPreJavaCommand(String commandOrParameter) {
        this.preJavaCommand.add(commandOrParameter);
    }

    /**
     * Get the environment script.
     *
     * @return the environment script.
     */
    public Script<?> getEnvScript() {
        return script;
    }

    /**
     * Set the environment script value to the given script value.
     * <p>
     * This script allows the user to programmatically set system variables, JVM arguments, additional classpath, etc.
     * Use the binding variable name {@code forkEnvironment} to fill this object in this given script.
     *
     * @param script the script to set
     */
    public void setEnvScript(Script<?> script) {
        this.script = script;
    }

    /**
     * Returns true if the current fork environment aims at running a linux docker container on a windows host
     * @return isDockerWindowsToLinux
     */
    public boolean isDockerWindowsToLinux() {
        return isDockerWindowsToLinux;
    }

    /**
     * Set true if the current fork environment aims at running a linux docker container on a windows host
     * @param dockerWindowsToLinux
     */
    public void setDockerWindowsToLinux(boolean dockerWindowsToLinux) {
        isDockerWindowsToLinux = dockerWindowsToLinux;
    }

    public static String convertToLinuxPath(String windowsPath) {
        if (windowsPath.matches("[a-zA-Z]:.*")) {
            return "/" + windowsPath.charAt(0) + windowsPath.substring(2).replace("\\", "/");
        } else {
            return windowsPath.replace("\\", "/");
        }
    }

    public static String convertToLinuxPathInJVMArgument(String jvmArgument) {
        if (jvmArgument.startsWith("-D") && jvmArgument.contains("=")) {
            int equalSignPos = jvmArgument.indexOf("=");
            return jvmArgument.substring(0, equalSignPos + 1) +
                   convertToLinuxClassPath(jvmArgument.substring(equalSignPos + 1));
        } else {
            return jvmArgument;
        }
    }

    public static String convertToLinuxClassPath(String windowsClassPath) {
        List<String> linuxClassPathEntries = new LinkedList<>();
        for (String windowsPath : windowsClassPath.split(";")) {
            if (windowsPath.matches("[a-zA-Z]:.*")) {
                linuxClassPathEntries.add("/" + windowsPath.charAt(0) + windowsPath.substring(2).replace("\\", "/"));
            } else {
                linuxClassPathEntries.add(windowsPath.replace("\\", "/"));
            }
        }
        return linuxClassPathEntries.stream().collect(Collectors.joining(":"));
    }

    @Override
    public String toString() {
        String nl = System.lineSeparator();
        return "ForkEnvironment {" + nl + "\tjavaHome = '" + javaHome + '\'' + nl + "\tisDockerWindowsToLinux = '" +
               isDockerWindowsToLinux + '\'' + nl + "\tworkingDir = '" + workingDir + '\'' + nl +
               "\tsystemEnvironment = " + systemEnvironment + nl + "\tjvmArguments = " + jvmArguments + nl +
               "\tadditionalClasspath = " + additionalClasspath + nl + "\tscript = " +
               (script != null ? script.display() : null) + nl + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        ForkEnvironment that = (ForkEnvironment) o;

        if (isDockerWindowsToLinux != that.isDockerWindowsToLinux)
            return false;
        if (javaHome != null ? !javaHome.equals(that.javaHome) : that.javaHome != null)
            return false;
        if (workingDir != null ? !workingDir.equals(that.workingDir) : that.workingDir != null)
            return false;
        if (systemEnvironment != null ? !systemEnvironment.equals(that.systemEnvironment)
                                      : that.systemEnvironment != null)
            return false;
        if (jvmArguments != null ? !jvmArguments.equals(that.jvmArguments) : that.jvmArguments != null)
            return false;
        if (additionalClasspath != null ? !additionalClasspath.equals(that.additionalClasspath)
                                        : that.additionalClasspath != null)
            return false;
        if (script != null ? !script.equals(that.script) : that.script != null)
            return false;
        return preJavaCommand != null ? preJavaCommand.equals(that.preJavaCommand) : that.preJavaCommand == null;
    }

    @Override
    public int hashCode() {
        int result = javaHome != null ? javaHome.hashCode() : 0;
        result = 31 * result + (workingDir != null ? workingDir.hashCode() : 0);
        result = 31 * result + (systemEnvironment != null ? systemEnvironment.hashCode() : 0);
        result = 31 * result + (jvmArguments != null ? jvmArguments.hashCode() : 0);
        result = 31 * result + (additionalClasspath != null ? additionalClasspath.hashCode() : 0);
        result = 31 * result + (script != null ? script.hashCode() : 0);
        result = 31 * result + (preJavaCommand != null ? preJavaCommand.hashCode() : 0);
        result = 31 * result + (isDockerWindowsToLinux ? 1 : 0);
        return result;
    }
}
