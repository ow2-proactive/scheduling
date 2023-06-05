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
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.NamedQueries;
import org.hibernate.annotations.NamedQuery;
import org.ow2.proactive.scheduler.common.job.JobLabelInfo;


@Entity
@NamedQueries({ @NamedQuery(name = "getTotalJobsLabels", query = "select id, label from JobLabel"),
                @NamedQuery(name = "getLabelIdByLabel", query = "select id from JobLabel where label = :label"),
                @NamedQuery(name = "getLabelById", query = "select label from JobLabel where id = :labelId"),
                @NamedQuery(name = "updateLabel", query = "update JobLabel set label = :newLabel where id = :labelId"),
                @NamedQuery(name = "deleteLabel", query = "delete from JobLabel where id = :labelId"),
                @NamedQuery(name = "deleteAllLabel", query = "delete from JobLabel") })
@Table(name = "JOB_LABEL")
public class JobLabel implements Serializable {

    private Long id;

    private String label;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "JOBLABEL_ID_SEQUENCE")
    @SequenceGenerator(name = "JOBLABEL_ID_SEQUENCE", sequenceName = "JOBLABEL_ID_SEQUENCE", allocationSize = 1)
    @Column(name = "ID")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "LABEL", nullable = false)
    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    static JobLabel createJobLabel(String label) {

        JobLabel jobLabelData = new JobLabel();
        jobLabelData.setLabel(label);
        return jobLabelData;
    }

    JobLabelInfo toJobLabelInfo() {
        return new JobLabelInfo(getId(), getLabel());
    }
}
