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
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package org.ow2.proactive.scheduler.common.task;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Proxy;
import org.objectweb.proactive.annotation.PublicAPI;


/**
 * Class representing a forked environment of a JVM created specifically for an execution of a Java Task.
 * A Java Task can be executed in the current JVM - then all Java Tasks are dependent on the same JVM (provider) and JVM options (like memory),
 * or can be executed in a dedicated JVM with additional options specified like javaHome, java Options, ...
 *
 * @author ProActive team
 *
 */
@PublicAPI
@Entity
@Table(name = "FORK_ENVIRONMENT")
@AccessType("field")
@Proxy(lazy = false)
public class ForkEnvironment implements Serializable {
    @Id
    @GeneratedValue
    @SuppressWarnings("unused")
    private long hibernateId;

    /**
     * Path to directory with Java installed, to this path '/bin/java' will be added.
     * If the path is null only 'java' command will be called
     */
    @Column(name = "JAVA_HOME", columnDefinition = "CLOB")
    private String javaHome = null;

    /**
     * Parameters passed to Java (not an application) (example: memory settings or properties)
     */
    @Column(name = "JVM_PARAMETERS", columnDefinition = "CLOB")
    private String jvmParameters = null;

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
     * Returns the jvmParameters.
     *
     * @return the jvmParameters.
     */
    public String getJVMParameters() {
        return jvmParameters;
    }

    /**
     * Sets the jvmParameters to the given jvmParameters value.
     *
     * @param jvmParameters the jvmParameters to set.
     */
    public void setJVMParameters(String jvmParameters) {
        this.jvmParameters = jvmParameters;
    }

}
