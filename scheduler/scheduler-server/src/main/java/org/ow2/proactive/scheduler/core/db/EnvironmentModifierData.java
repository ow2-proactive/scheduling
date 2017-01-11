package org.ow2.proactive.scheduler.core.db;

import javax.persistence.*;

import org.ow2.proactive.scheduler.common.task.PropertyModifier;


@Entity
@NamedQueries( {
        @NamedQuery(
                name = "deleteEnvironmentModifierData",
                query = "delete from EnvironmentModifierData where taskData.id.jobId = :jobId"
        ),
})
@Table(name = "ENVIRONMENT_MODIFIER_DATA", indexes = {
        @Index(name = "ENV_MODIFIER_DATA_JOB_ID", columnList = "JOB_ID"),
        @Index(name = "ENV_MODIFIER_DATA_TASK_ID", columnList = "TASK_ID")
})
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

