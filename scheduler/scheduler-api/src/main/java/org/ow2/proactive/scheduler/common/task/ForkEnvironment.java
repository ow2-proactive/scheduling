/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package org.ow2.proactive.scheduler.common.task;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public ForkEnvironment() {
        additionalClasspath = new ArrayList<>();
        jvmArguments = new ArrayList<>();
        systemEnvironment = new HashMap<>();
    }

    /**
     * Copy constructor.
     * 
     * @param forkEnvironment the object to copy
     * @throws ExecutableCreationException script copy failed
     */
    public ForkEnvironment(ForkEnvironment forkEnvironment) throws ExecutableCreationException {
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

        if (forkEnvironment.script != null) {
            try {
                this.script = new SimpleScript(forkEnvironment.script);
            } catch (InvalidScriptException e) {
                throw new ExecutableCreationException("Failed to copy ForkEnvironment script", e);
            }
        }
    }

    public ForkEnvironment(String workingDir) {
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
     * Get the environment script.
     *
     * @return the environment script.
     */
    public Script<?> getEnvScript() {
        return script;
    }

    /**
     * Set the environment script value to the given script value.<br/>
     * This script allows the user to programmatically set system variables, JVM arguments, additional classpath
     * Use the binding variable name <b>forkEnvironment</b> to fill this object in this given script.
     *
     * @param script the script to set
     */
    public void setEnvScript(Script<?> script) {
        this.script = script;
    }

    @Override
    public String toString() {
        String nl = System.lineSeparator();
        return "ForkEnvironment {" + nl + "\tjavaHome = '" + javaHome + '\'' + nl + "\tworkingDir = '" +
            workingDir + '\'' + nl +
            "\tsystemEnvironment = " + systemEnvironment + nl + "\tjvmArguments = " + jvmArguments + nl +
            "\tadditionalClasspath = " + additionalClasspath + nl + "\tscript = " +
            (script != null ? script.display() : null) + nl + '}';
    }

}
