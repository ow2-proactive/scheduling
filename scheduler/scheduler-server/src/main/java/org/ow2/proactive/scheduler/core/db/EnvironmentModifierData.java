package org.ow2.proactive.scheduler.core.db;

import org.ow2.proactive.scheduler.common.task.PropertyModifier;

import javax.persistence.*;


@Entity
@Table(name = "ENVIRONMENT_MODIFIER_DATA")
public class EnvironmentModifierData {

    private long id;

    private String name;

    private String value;

    private boolean append;

    private char appendChar;

    private ScriptTaskData taskData;

    static EnvironmentModifierData create(PropertyModifier propertyModifier, ScriptTaskData taskData) {
        EnvironmentModifierData data = new EnvironmentModifierData();
        data.setName(propertyModifier.getName());
        data.setValue(propertyModifier.getValue());
        data.setAppend(propertyModifier.isAppend());
        data.setAppendChar(propertyModifier.getAppendChar());
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
    @JoinColumn(name = "TASK_DATA_ID")
    public ScriptTaskData getTaskData() {
        return taskData;
    }

    public void setTaskData(ScriptTaskData taskData) {
        this.taskData = taskData;
    }

    @Column(name = "NAME", nullable = false)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "VALUE", nullable = false)
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Column(name = "APPEND")
    public boolean isAppend() {
        return append;
    }

    public void setAppend(boolean append) {
        this.append = append;
    }

    @Column(name = "APPEND_CHAR")
    public char getAppendChar() {
        return appendChar;
    }

    public void setAppendChar(char appendChar) {
        this.appendChar = appendChar;
    }

}
