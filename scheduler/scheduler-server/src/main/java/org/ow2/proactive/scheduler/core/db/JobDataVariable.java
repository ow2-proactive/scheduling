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

import org.hibernate.annotations.Type;
import org.ow2.proactive.scheduler.common.job.JobVariable;


@Entity
@NamedQueries({ @NamedQuery(name = "deleteJobDataVariable", query = "delete from JobDataVariable where jobData.id in :ids"),
                @NamedQuery(name = "deleteJobDataVariableInBulk", query = "delete from JobDataVariable where jobData.id in :jobIdList"),
                @NamedQuery(name = "countJobDataVariable", query = "select count (*) from JobDataVariable") })
@Table(name = "JOB_DATA_VARIABLE", indexes = { @Index(name = "JOB_DATA_VARIABLE_JOB_ID", columnList = "JOB_ID") })
public class JobDataVariable {

    private long id;

    private String name;

    private String value;

    private String model;

    private String description;

    private String group;

    private Boolean advanced;

    private Boolean hidden;

    private JobData jobData;

    static JobDataVariable create(String variableName, JobVariable jobVariable, JobData jobData) {
        JobDataVariable jobDataVariable = new JobDataVariable();
        jobDataVariable.setName(variableName);
        jobDataVariable.setValue(jobVariable.getValue());
        jobDataVariable.setModel(jobVariable.getModel());
        jobDataVariable.setDescription(jobVariable.getDescription());
        jobDataVariable.setGroup(jobVariable.getGroup());
        jobDataVariable.setAdvanced(jobVariable.isAdvanced());
        jobDataVariable.setHidden(jobVariable.isHidden());
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
    @Type(type = "org.hibernate.type.MaterializedClobType")
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Column(name = "VARIABLE_MODEL", length = Integer.MAX_VALUE)
    @Lob
    @Type(type = "org.hibernate.type.MaterializedClobType")
    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    @Column(name = "VARIABLE_DESCRIPTION", length = Integer.MAX_VALUE)
    @Lob
    @Type(type = "org.hibernate.type.MaterializedClobType")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Column(name = "VARIABLE_GROUP")
    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    @Column(name = "VARIABLE_ADVANCED")
    public Boolean getAdvanced() {
        if (advanced == null) {
            return false;
        } else {
            return advanced;
        }
    }

    public void setAdvanced(Boolean advanced) {
        this.advanced = advanced;
    }

    @Column(name = "VARIABLE_HIDDEN")
    public Boolean getHidden() {
        if (hidden == null) {
            return false;
        } else {
            return hidden;
        }
    }

    public void setHidden(Boolean hidden) {
        this.hidden = hidden;
    }
}
