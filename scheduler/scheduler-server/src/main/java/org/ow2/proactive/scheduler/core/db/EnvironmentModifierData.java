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

import org.ow2.proactive.scheduler.common.task.PropertyModifier;


@Entity
@NamedQueries({ @NamedQuery(name = "deleteEnvironmentModifierData", query = "delete from EnvironmentModifierData where taskData.id.jobId = :jobId"),
                @NamedQuery(name = "deleteEnvironmentModifierDataInBulk", query = "delete from EnvironmentModifierData where taskData.id.jobId in :jobIdList"),
                @NamedQuery(name = "countEnvironmentModifierData", query = "select count (*) from EnvironmentModifierData") })
@Table(name = "ENVIRONMENT_MODIFIER_DATA", indexes = { @Index(name = "ENV_MODIFIER_DATA_JOB_ID", columnList = "JOB_ID"),
                                                       @Index(name = "ENV_MODIFIER_DATA_TASK_ID", columnList = "TASK_ID") })
public class EnvironmentModifierData {

    private long id;

    private String name;

    private String value;

    private TaskData taskData;

    static EnvironmentModifierData create(PropertyModifier propertyModifier, TaskData taskData) {
        EnvironmentModifierData data = new EnvironmentModifierData();
        data.setName(propertyModifier.getName());
        data.setValue(propertyModifier.getValue());
        data.setTaskData(taskData);
        return data;
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
    @JoinColumns(value = { @JoinColumn(name = "JOB_ID", referencedColumnName = "TASK_ID_JOB"),
                           @JoinColumn(name = "TASK_ID", referencedColumnName = "TASK_ID_TASK") })
    public TaskData getTaskData() {
        return taskData;
    }

    public void setTaskData(TaskData taskData) {
        this.taskData = taskData;
    }

    @Column(name = "NAME", nullable = false)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "VALUE", nullable = false, length = Integer.MAX_VALUE)
    @Lob
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
