/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds 
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
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
 * If needed, contact us to obtain a release under GPL Version 2 
 * or a different license than the GPL.
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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.CollectionOfElements;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.annotations.Proxy;
import org.objectweb.proactive.annotation.PublicAPI;
import org.ow2.proactive.scripting.Script;
import org.ow2.proactive.scripting.SimpleScript;


/**
 * Class representing a forked environment of a JVM created specifically for an execution of a Java Task.
 * A Java Task can be executed in the current JVM - then all Java Tasks are dependent on the same JVM (provider) and JVM options (like memory),
 * or can be executed in a dedicated JVM with additional options specified like javaHome, java Arguments, classpath, ...
 *
 * @author ProActive team
 *
 */
@PublicAPI
@Entity
@Table(name = "FORK_ENVIRONMENT")
@AccessType("field")
@Proxy(lazy = false)
@XmlAccessorType(XmlAccessType.FIELD)
public class ForkEnvironment implements Serializable {
    @Id
    @GeneratedValue
    @SuppressWarnings("unused")
    @XmlTransient
    private long hId;

    /**
     * Path to directory with Java installed, to this path '/bin/java' will be added.
     * If the path is null only 'java' command will be called
     */
    @Column(name = "JAVA_HOME", length = Integer.MAX_VALUE)
    @Lob
    private String javaHome = null;

    /**
     * Path to the working directory
     */
    @Column(name = "WORKING_DIR", length = Integer.MAX_VALUE)
    @Lob
    private String workingDir = null;

    /**
     * Base environment : used by internal constructor to set up
     * a base environment on which to apply client env
     */
    @Transient
    private transient Map<String, String> baseSystemProperties = null;

    /**
     * Arguments passed to Java (not an application) (example: memory settings or properties)
     */
    @OneToMany(targetEntity = PropertyModifier.class)
    @LazyCollection(value = LazyCollectionOption.FALSE)
    @Cascade(CascadeType.ALL)
    private List<PropertyModifier> systemProperties = null;

    /**
     * Arguments passed to Java (not an application) (example: memory settings or properties)
     */
    @CollectionOfElements
    @Cascade(CascadeType.ALL)
    @LazyCollection(value = LazyCollectionOption.FALSE)
    @JoinColumn(name = "JVM_ARGUMENTS")
    private List<String> jvmArguments = null;

    /**
     * Additional classpath when new JVM will be started
     */
    @CollectionOfElements
    @Cascade(CascadeType.ALL)
    @LazyCollection(value = LazyCollectionOption.FALSE)
    @JoinColumn(name = "CLASSPATH")
    private List<String> additionalClasspath = null;

    /**
     * EnvScript : can be used to initialize environment just before JVM fork.
     */
    @Cascade(CascadeType.ALL)
    @OneToOne(fetch = FetchType.EAGER, targetEntity = SimpleScript.class)
    private Script<?> script = null;

    public ForkEnvironment() {
    }

    /**
     * This constructor is used for internal stuff only.
     * It allows an internal subtype to create a ForkEnvironement with a base env.
     *
     * @param forkEnv the fork environment that should be decorated with a base env, if null, empty fork env will be used
     * @param baseEnv the environment on witch to base the user env.
     */
    protected ForkEnvironment(ForkEnvironment forkEnv, Map<String, String> baseEnv) {
        this.baseSystemProperties = baseEnv;
        if (forkEnv != null) {
            this.javaHome = forkEnv.javaHome;
            this.workingDir = forkEnv.workingDir;
            this.systemProperties = forkEnv.systemProperties;
            this.jvmArguments = forkEnv.jvmArguments;
            this.additionalClasspath = forkEnv.additionalClasspath;
            this.script = forkEnv.script;
        }
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
     * Return the working Directory
     *
     * @return the working Directory
     */
    public String getWorkingDir() {
        return workingDir;
    }

    /**
     * Set the working directory value to the given workingDir value
     *
     * @param workingDir the working directory to set
     */
    public void setWorkingDir(String workingDir) {
        this.workingDir = workingDir;
    }

    /**
     * Return a copy of the system properties, empty map if no properties.
     *
     * @return a copy of the system properties, empty map if no properties.
     */
    public Map<String, String> getSystemProperties() {
        if (this.systemProperties == null && this.baseSystemProperties == null) {
            return new HashMap<String, String>(0);
        }
        Map<String, String> props;
        if (baseSystemProperties == null) {
            props = new HashMap<String, String>();
        } else {
            props = new HashMap<String, String>(baseSystemProperties);
        }
        for (PropertyModifier pm : systemProperties) {
            pm.update(props);
        }
        return props;
    }

    /**
     * Add a new systemProperty value from its name and value.
     * The value can overwrite or be appended to a previous property value with the same name.<br/>
     * If the append boolean is true, the value will be appended to the old one or to a existing system property.
     * If not, the value will overwrite the old one.
     *
     * @param name the name of the property to add
     * @param value the the value associated to the given name
     * @param append true if this value must be appended to a previous one or a system one, false if overwrite.
     * @throws IllegalArgumentException if name is null
     */
    public void addSystemProperty(String name, String value, boolean append) {
        if (name == null) {
            throw new IllegalArgumentException("Name cannot be null");
        }
        if (this.systemProperties == null) {
            this.systemProperties = new ArrayList<PropertyModifier>(5);
        }
        this.systemProperties.add(new PropertyModifier(name, value, append));
    }

    /**
     * Add a new systemProperty value from its name and value.
     * The value will be appended to a previous property value with the same name using the given appendChar.<br/>
     * If this value is the first, no append character will be inserted.
     * Each time a new value is inserted, it appends the appendChar and the new value.
     *
     * @param name the name of the property to add
     * @param value the the value associated to the given name
     * @param appendChar The character used to append this value with a previous one.
     * @throws IllegalArgumentException if name is null
     */
    public void addSystemProperty(String name, String value, char appendChar) {
        if (name == null) {
            throw new IllegalArgumentException("Name cannot be null");
        }
        if (this.systemProperties == null) {
            this.systemProperties = new ArrayList<PropertyModifier>(5);
        }
        this.systemProperties.add(new PropertyModifier(name, value, appendChar));
    }

    /**
     * Get the system property value associated with the given name.
     *
     * @param name the name of the property value to get
     * @return the system property value associated with the given name, or null if the property does not exist.
     */
    public String getSystemProperty(String name) {
        if (this.systemProperties == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        boolean hadValue = false;
        if (baseSystemProperties != null) {
            String tmp = baseSystemProperties.get(name);
            if (tmp != null) {
                sb.append(tmp);
                hadValue = true;
            }
        }
        for (PropertyModifier pm : systemProperties) {
            if (pm.getName().equals(name)) {
                pm.update(sb);
                hadValue = true;
            }
        }
        return hadValue ? sb.toString() : null;
    }

    /**
     * Return a copy of the JVM arguments, empty list if no arguments.
     *
     * @return a copy of the JVM arguments, empty list if no arguments.
     */
    public List<String> getJVMArguments() {
        if (this.jvmArguments == null) {
            return new ArrayList<String>(0);
        }
        return new ArrayList<String>(this.jvmArguments);
    }

    /**
     * Add a new JVM argument value.
     *
     * @param value the value of the property to be added
     * @throws IllegalArgumentException if value is null
     */
    public void addJVMArgument(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Value cannot be null");
        }
        if (this.jvmArguments == null) {
            this.jvmArguments = new ArrayList<String>(5);
        }
        this.jvmArguments.add(value);
    }

    /**
     * Return a copy of the additional classpath, empty list if no arguments.
     *
     * @return a copy of the additional classpath, empty list if no arguments.
     */
    public List<String> getAdditionalClasspath() {
        if (this.additionalClasspath == null) {
            return new ArrayList<String>(0);
        }
        return new ArrayList<String>(this.additionalClasspath);
    }

    /**
     * Add a new additional Classpath value
     *
     * @param value the additional Classpath to add
     * @throws IllegalArgumentException if value is null
     */
    public void addAdditionalClasspath(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Value cannot be null");
        }
        if (this.additionalClasspath == null) {
            this.additionalClasspath = new ArrayList<String>(5);
        }
        this.additionalClasspath.add(value);
    }

    /**
     * Get the environment script
     *
     * @return the environment script
     */
    public Script<?> getEnvScript() {
        return script;
    }

    /**
     * Set the environment script value to the given script value.<br/>
     * This script allows the user to programaticaly set systemProperties, JVM arguments, additional classpath
     * Use the binding variable name <b>forkEnvironment</b> to fill this object in this given script.
     *
     * @param script the script to set
     */
    public void setEnvScript(Script<?> script) {
        this.script = script;
    }

}
