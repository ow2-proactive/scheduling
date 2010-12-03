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
import org.hibernate.annotations.Type;
import org.objectweb.proactive.annotation.PublicAPI;
import org.ow2.proactive.scheduler.task.TaskResultImpl;
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
     * Arguments passed to Java (not an application) (example: memory settings or properties)
     */
    @CollectionOfElements
    @Cascade(CascadeType.ALL)
    @LazyCollection(value = LazyCollectionOption.FALSE)
    @JoinColumn(name = "SYSTEM_PROPS")
    private Map<String, String> systemProperties = null;

    /**
     * Arguments passed to Java (not an application) (example: memory settings or properties)
     */
    @Column(name = "JVM_ARGUMENTS", columnDefinition = "BLOB")
    @Type(type = "org.ow2.proactive.scheduler.core.db.schedulerType.CharacterLargeOBject")
    private String[] jvmArguments = null;

    /**
     * Additional classpath when new JVM will be started
     */
    @Column(name = "CLASSPATH", columnDefinition = "BLOB")
    @Type(type = "org.ow2.proactive.scheduler.core.db.schedulerType.CharacterLargeOBject")
    private String[] additionalClasspath = null;

    /**
     * PreScript : can be used to launch script just before the task
     * execution.
     */
    @Cascade(CascadeType.ALL)
    @OneToOne(fetch = FetchType.EAGER, targetEntity = SimpleScript.class)
    protected Script<?> script = null;

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
     * Get the systemProperties
     *
     * @return the systemProperties
     */
    public Map<String, String> getSystemProperties() {
        return systemProperties;
    }

    /**
     * Set the systemProperties value to the given systemProperties value
     *
     * @param systemProperties the systemProperties to set
     */
    public void setSystemProperties(Map<String, String> systemProperties) {
        this.systemProperties = systemProperties;
    }

    /**
     * Returns the jvmArguments.
     *
     * @return the jvmArguments.
     */
    public String[] getJVMArguments() {
        return jvmArguments;
    }

    /**
     * Sets the jvmArguments to the given jvmArguments value.
     *
     * @param jvmArguments the jvmArguments to set.
     */
    public void setJVMArguments(String[] jvmArguments) {
        this.jvmArguments = jvmArguments;
    }

    /**
     * Get the additional Classpath
     *
     * @return the additional Classpath
     */
    public String[] getAdditionalClasspath() {
        return additionalClasspath;
    }

    /**
     * Set the additional Classpath value to the given additionalClasspath value
     *
     * @param additionalClasspath the additional Classpath to set
     */
    public void setAdditionalClasspath(String[] additionalClasspath) {
        this.additionalClasspath = additionalClasspath;
    }

    /**
     * Get the environment script
     *
     * @return the environment script
     */
    public Script<?> getScript() {
        return script;
    }

    /**
     * Set the environment script value to the given script value.<br/>
     * This script allows the user to programmatically set systemProperties, JVM arguments, additional classpath
     * Use TODO(Utils) methods to set each desired element.
     *
     * @param script the script to set
     */
    public void setScript(Script<?> script) {
        this.script = script;
    }

}
