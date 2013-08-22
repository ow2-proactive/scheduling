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
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.common.job;

import java.io.IOException;
import java.io.Serializable;
import java.util.zip.CRC32;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import org.objectweb.proactive.annotation.PublicAPI;
import org.ow2.proactive.scheduler.common.util.JarUtils;


/**
 * This class contains all the elements needed for executing the executable
 * contained in a job, as the classpath for Java executables.
 * @author The ProActive team
 */
@PublicAPI
@XmlAccessorType(XmlAccessType.FIELD)
public class JobEnvironment implements Serializable {

    // job classpath
    // used for resolving classes only on user side !
    private String[] jobClasspath;

    // jar file containing the job classpath
    // handle manually in the core
    // @see JobClasspathManager
    private byte[] jobClasspathContent;

    // true if the classpath contains jar files
    private boolean containsJarFile;

    // CRC32 of the classpath content
    private long crc;

    public JobEnvironment() {
    }

    public JobEnvironment(String[] jobClasspath, byte[] jobClasspathContent, boolean containsJarFile, long crc) {
        this.jobClasspath = jobClasspath;
        this.jobClasspathContent = jobClasspathContent;
        this.containsJarFile = containsJarFile;
        this.crc = crc;
    }

    /**
     * Return the byte[] representation of the jar file containing the job classpath.
     *
     * @return the byte[] representation of the jar file containing the job classpath.
     */
    public byte[] getJobClasspathContent() {
        return jobClasspathContent;
    }

    /**
     * Return and delete the jobclasspath content. Used for memory saving.
     * @return the jobclasspath content.
     */
    public byte[] clearJobClasspathContent() {
        byte[] jcpc = this.jobClasspathContent;
        this.jobClasspathContent = null;
        return jcpc;
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
     * Return the CRC of the jobclasspath content
     *
     * @return the CRC of the jobclasspath content
     */
    public long getJobClasspathCRC() {
        return this.crc;
    }

    /**
     * Add the classPath of your task to the job. Every needed classes used in your executable must
     * be in this classPath.
     *
     * @param jobClasspath the jobClasspath to set.
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
        final CRC32 crc32 = new CRC32();
        // TODO cdelbe : define version and cp of the jar classpath ?
        this.jobClasspathContent = JarUtils.jarDirectoriesAndFiles(jobClasspath, "1.0", null, null, crc32);
        this.crc = crc32.getValue();

    }

    /**
     * return true if the jobclasspath contains a jar file, false otherwise.
     * @return true if the jobclasspath contains a jar file, false otherwise.
     */
    public boolean containsJarFile() {
        return containsJarFile;
    }
}
