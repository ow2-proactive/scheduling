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

import javax.persistence.*;

import org.ow2.proactive.scheduler.common.job.JobVariable;


@Entity
@NamedQueries({ @NamedQuery(name = "deleteJobDataVariable", query = "delete from JobDataVariable where jobData.id = :jobId"),
                @NamedQuery(name = "deleteJobDataVariableInBulk", query = "delete from JobDataVariable where jobData.id in :jobIdList"),
                @NamedQuery(name = "countJobDataVariable", query = "select count (*) from JobDataVariable") })
@Table(name = "JOB_DATA_VARIABLE", indexes = { @Index(name = "JOB_DATA_VARIABLE_JOB_ID", columnList = "JOB_ID") })
public class JobDataVariable {

    private long id;

    private String name;

    private String value;

    private String model;

    private JobData jobData;

    static JobDataVariable create(String variableName, JobVariable jobVariable, JobData jobData) {
        JobDataVariable jobDataVariable = new JobDataVariable();
        jobDataVariable.setName(variableName);
        jobDataVariable.setValue(jobVariable.getValue());
        jobDataVariable.setModel(jobVariable.getModel());
        jobDataVariable.setJobData(jobData);
        return jobDataVariable;
    }

    @Id
    @GeneratedValue
    @Column(name = "ID")
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "JOB_ID", nullable = false, updatable = false)
    public JobData getJobData() {
        return jobData;
    }

    public void setJobData(JobData jobData) {
        this.jobData = jobData;
    }

    @Column(name = "VARIABLE_NAME")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "VARIABLE_VALUE", length = Integer.MAX_VALUE)
    @Lob
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Column(name = "VARIABLE_MODEL", length = Integer.MAX_VALUE)
    @Lob
    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

}
