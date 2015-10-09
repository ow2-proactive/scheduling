/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2015 INRIA/University of
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
package org.ow2.proactive.scheduler.job;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.ow2.proactive.scheduler.common.SchedulerConstants;
import org.ow2.proactive.scheduler.common.job.JobId;
import com.google.common.primitives.Longs;


/**
 * Definition of a job identification, this will be used during scheduling to identify your job.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
@XmlRootElement(name = "jobid")
@XmlAccessorType(XmlAccessType.FIELD)
public final class JobIdImpl implements JobId {

    /** Default job name */
    public static final String DEFAULT_JOB_NAME = SchedulerConstants.JOB_DEFAULT_NAME;

    /** current instance id */
    @XmlElement(name = "id")
    private long id;

    /** Human readable name */
    private String readableName = DEFAULT_JOB_NAME;

    /**
     * Default Job id constructor
     *
     * @param id the id to put in the jobId
     */
    private JobIdImpl(long id) {
        this.id = id;
    }

    /**
     * Default Job id constructor
     *
     * @param id the id to put in the jobId
     * @param readableName the human readable name associated with this jobid
     */
    public JobIdImpl(long id, String readableName) {
        this(id);
        this.readableName = readableName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getReadableName() {
        return this.readableName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value() {
        return String.valueOf(this.id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long longValue() {
        return this.id;
    }

    /**
     * Make a new JobId with the given arguments.
     *
     * @param str the string on which to base the id.
     * @return the new jobId
     */
    public static JobId makeJobId(String str) {
        return new JobIdImpl(Long.parseLong(str.trim()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(JobId jobId) {
        return Longs.compare(id, ((JobIdImpl) jobId).id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JobIdImpl jobId = (JobIdImpl) o;

        if (id != jobId.id) return false;

        return true;
    }

    /**
     * <font color="red"><b>Do not use this method to get the value as an INTEGER.<br />
     * It does not ensure that this integer value is the real Job ID Value.</b></font><br />
     * Use the {@link org.ow2.proactive.scheduler.common.job.JobId#value()} method instead.
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return (int) this.id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return this.value();
    }

}
