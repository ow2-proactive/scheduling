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
 * $ACTIVEEON_INITIAL_DEV$
 */
package org.ow2.proactive.scheduler.util.classloading;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Proxy;
import org.hibernate.annotations.Type;
import org.ow2.proactive.db.Condition;
import org.ow2.proactive.db.ConditionComparator;
import org.ow2.proactive.scheduler.core.db.DatabaseManager;


/**
 * This class stores job classpathes directly in DB through {@link JobClasspathEntry} struct.
 * No classpath is supposed to retain in memory.
 */
public class JobClasspathManager {

    // cache for known CRC
    private final Set<Long> knownCRC = new HashSet<Long>();

    /**
     * Return a jobclasspath
     * @param crc the key for this classpath
     * @return a {@link JobClasspathEntry} for this crc
     */
    public JobClasspathEntry get(long crc) {
        List<JobClasspathEntry> results = DatabaseManager.getInstance().recover(JobClasspathEntry.class,
                new Condition("crc", ConditionComparator.EQUALS_TO, crc));
        if (results.size() == 1) {
            this.knownCRC.add(crc);
            return results.get(0);
        } else if (results.size() != 0) {
            throw new IllegalStateException("CRC " + crc +
                " is replicated in DB. Please consider cleaning DB.");
        } else {
            return null;
        }
    }

    /**
     * Add a new job classpath in the DB
     * @param crc the key for this classpath
     * @param classpathContent the classpath content
     * @param containsJarFiles true if the the classpath contains jar files, false otherwhise
     */
    public void put(long crc, byte[] classpathContent, boolean containsJarFiles) {
        if (!this.contains(crc)) {
            DatabaseManager.getInstance().register(
                    new JobClasspathEntry(crc, classpathContent, containsJarFiles));
            this.knownCRC.add(crc);
        } else {
            throw new IllegalStateException("CRC " + crc +
                " is replicated in DB. Please consider cleaning DB.");
        }

    }

    /**
     * Returns true if the jobclasspath identified by crc is stored, false otherwhise
     * @return true if the jobclasspath identified by crc is stored, false otherwhise
     */
    public boolean contains(long crc) {
        // loading the classpath entry to know if it exists is costly but
        // it does not rely on SQL request.
        return this.knownCRC.contains(crc) ? true : get(crc) != null;

    }

    /**
     * Simple hibernatizable job classpath
     */
    @Entity
    @Table(name = "JOBCP_ENTRIES")
    @AccessType("field")
    @Proxy(lazy = false)
    public static class JobClasspathEntry {

        @Id
        @GeneratedValue
        @Column(name = "ID")
        @SuppressWarnings("unused")
        private long hId;

        @Column(name = "CRC", unique = true)
        public long crc;

        @Column(name = "CP_CONTENT", updatable = false, length = Integer.MAX_VALUE)
        @Type(type = "org.ow2.proactive.scheduler.core.db.schedulerType.BinaryLargeOBject")
        @Lob
        public byte[] classpathContent;

        @Column(name = "CONTAINS_JAR")
        public boolean containsJarFiles;

        /**
         * TODO
         * @param crc
         * @param classpathContent
         * @param containsJarFiles
         */
        JobClasspathEntry(long crc, byte[] classpathContent, boolean containsJarFiles) {
            this.crc = crc;
            this.classpathContent = classpathContent;
            this.containsJarFiles = containsJarFiles;
        }

        // for Hibernate...
        JobClasspathEntry() {
        }

    }

}
