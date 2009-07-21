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
package org.ow2.proactive.scheduler.common.job;

import java.io.IOException;
import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Proxy;
import org.hibernate.annotations.Type;
import org.objectweb.proactive.annotation.PublicAPI;
import org.ow2.proactive.scheduler.common.db.annotation.Unloadable;
import org.ow2.proactive.scheduler.common.util.JarUtils;


/**
 * This class contains all the elements needed for executing the executable
 * contained in a job, as the classpath for Java executables.
 * @author The ProActive team
 */
@PublicAPI
@Entity
@Table(name = "JOB_ENVIRONMENT")
@AccessType("field")
@Proxy(lazy = false)
public class JobEnvironment implements Serializable {
    @Id
    @GeneratedValue
    @SuppressWarnings("unused")
    private long hibernateId;

    // job classpath
    // used for resolving classes only on user side !
    @Column(name = "CLASSPATH", length = Integer.MAX_VALUE)
    @Type(type = "org.ow2.proactive.scheduler.core.db.schedulerType.CharacterLargeOBject")
    @Lob
    private String[] jobClasspath;

    // jar file containing the job classpath
    @Unloadable
    @Column(name = "CLASSPATH_CONTENT", length = Integer.MAX_VALUE)
    @Type(type = "org.ow2.proactive.scheduler.core.db.schedulerType.BinaryLargeOBject")
    @Lob
    private byte[] jobClasspathContent;

    // true if the classpath contains jar files
    @Column(name = "CONTAINS_JAR")
    private boolean containsJarFile;

    /**
     * Return the byte[] representation of the jar file containing the job classpath.
     *
     * @return the byte[] representation of the jar file containing the job classpath.
     */
    public byte[] getJobClasspathContent() {
        return jobClasspathContent;
    }

    /**
     * Return the job classPath.
     * 
     * @return the job classPath.
     */
    public String[] getJobClasspath() {
        return jobClasspath;
    }

    /**
     * Add the classPath of your task to the job. Every needed classes used in your executable must
     * be in this classPath.
     *
     * @param jobClasspath the jobClasspath to set
     * @throws IOException if the classpath cannot be built
     */
    public void setJobClasspath(String[] jobClasspath) throws IOException {
        this.jobClasspath = jobClasspath;
        for (String pathElement : jobClasspath) {
            if (pathElement.endsWith(".jar")) {
                this.containsJarFile = true;
                break;
            }
        }
        // TODO cdelbe : define version and cp of the jar classpath ?
        this.jobClasspathContent = JarUtils.jarDirectoriesAndFiles(jobClasspath, "1.0", null, null);
    }

    /**
     * return true if the jobclasspath contains a jar file, false otherwise.
     * @return true if the jobclasspath contains a jar file, false otherwise.
     */
    public boolean containsJarFile() {
        return containsJarFile;
    }
}
