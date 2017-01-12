package org.ow2.proactive.scheduler.core.db;

import org.ow2.proactive.scheduler.common.task.TaskVariable;

import javax.persistence.*;


@Entity
@NamedQueries({ @NamedQuery(name = "deleteTaskDataVariable", query = "delete from TaskDataVariable where taskData.id.jobId = :jobId"), })
@Table(name = "TASK_DATA_VARIABLE", indexes = {
        @Index(name = "TASK_DATA_VARIABLE_JOB_ID", columnList = "JOB_ID"),
        @Index(name = "TASK_DATA_VARIABLE_TASK_ID", columnList = "TASK_ID")
})
public class TaskDataVariable {

    private long id;
    
    private String name;
    
    private String value;
    
    private boolean jobInherited;
    
    private String model;

    private TaskData taskData;

    static TaskDataVariable create(String variableName, TaskVariable taskVariable, TaskData taskData) {
        TaskDataVariable taskDataVariable = new TaskDataVariable();
        taskDataVariable.setName(variableName);
        taskDataVariable.setValue(taskVariable.getValue());
        taskDataVariable.setModel(taskVariable.getModel());
        taskDataVariable.setJobInherited(taskVariable.isJobInherited());
        taskDataVariable.setTaskData(taskData);
        return taskDataVariable;
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
    @JoinColumns(value = {
            @JoinColumn(name = "JOB_ID", referencedColumnName = "TASK_ID_JOB"),
            @JoinColumn(name = "TASK_ID", referencedColumnName = "TASK_ID_TASK") })
    public TaskData getTaskData() {
        return taskData;
    }

    public void setTaskData(TaskData taskData) {
        this.taskData = taskData;
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

    @Column(name = "VARIABLE_JOB_INHERITED")
    public boolean isJobInherited() {
        return jobInherited;
    }

    public void setJobInherited(boolean jobInherited) {
        this.jobInherited = jobInherited;
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
