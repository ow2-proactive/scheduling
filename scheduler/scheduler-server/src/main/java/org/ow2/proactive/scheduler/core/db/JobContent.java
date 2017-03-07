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
package org.ow2.proactive.scheduler.core.db;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.log4j.Logger;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.ow2.proactive.scheduler.common.job.Job;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.util.ByteCompressionUtils;


/**
 * JobContent Entity class, store workflow content to database
 *
 * @author ActiveEon team
 *
 */
@Entity
@NamedQueries({ @NamedQuery(name = "deleteJobContentInBulk", query = "delete from JobContent where id in :jobIdList"),
                @NamedQuery(name = "loadJobContent", query = "from JobContent as content where content.jobId = :id"),
                @NamedQuery(name = "countJobContent", query = "select count (*) from JobContent") })
@Table(name = "JOB_CONTENT", indexes = { @Index(name = "INITIAL_JOB_INDEX", columnList = "JOB_ID") })
public class JobContent implements Serializable {

    private static final Logger LOGGER = Logger.getLogger(JobContent.class);

    @Lob
    @Column(name = "CONTENT", length = Integer.MAX_VALUE)
    private byte[] jobContentAsByteArray;

    @Id
    @Column(name = "JOB_ID", unique = true, nullable = false)
    @GeneratedValue(generator = "keyGenerator")
    @GenericGenerator(name = "keyGenerator", strategy = "foreign", parameters = { @Parameter(value = "jobData", name = "property") })
    private Long jobId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "JOB_ID", referencedColumnName = "ID")
    @MapsId
    private JobData jobData;

    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    public byte[] getJobContentAsByteArray() {
        return jobContentAsByteArray;
    }

    public JobData getJobData() {
        return jobData;
    }

    public void setJobData(JobData jobData) {
        this.jobData = jobData;
    }

    public void setJobContentAsByteArray(byte[] jobContentAsByteArray) {
        this.jobContentAsByteArray = jobContentAsByteArray;
    }

    @Transient
    public TaskFlowJob getInitJobContent() {
        try {
            byte[] deCompressed = ByteCompressionUtils.decompress(jobContentAsByteArray);
            return SerializationUtils.deserialize(deCompressed);
        } catch (Exception e) {
            LOGGER.error(e);
        }
        return SerializationUtils.deserialize(jobContentAsByteArray);
    }

    public void setInitJobContent(Job job) {
        byte[] jobByte = SerializationUtils.serialize(job);
        try {
            this.jobContentAsByteArray = ByteCompressionUtils.compress(jobByte);
        } catch (Exception e) {
            LOGGER.error(e);
            this.jobContentAsByteArray = jobByte;
        }
    }

    @Override
    public int hashCode() {
        return jobId.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        JobContent other = (JobContent) obj;
        if (other.getJobId().equals(this.getJobId()))
            return false;
        return true;
    }
}
