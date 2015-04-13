package org.ow2.proactive.scheduler.core.db;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.ow2.proactive.scheduler.common.task.PropertyModifier;


@Entity
@Table(name = "ENVIRONMENT_MODIFIER_DATA")
public class EnvironmentModifierData {

    private long id;

    private String name;

    private String value;

    private boolean append;

    private char appendChar;

    private ForkedJavaTaskData taskData;

    static EnvironmentModifierData create(PropertyModifier propertyModifier, ForkedJavaTaskData taskData) {
        EnvironmentModifierData data = new EnvironmentModifierData();
        data.setName(propertyModifier.getName());
        data.setValue(propertyModifier.getValue());
        data.setAppend(propertyModifier.isAppend());
        data.setAppendChar(propertyModifier.getAppendChar());
        data.setTaskData(taskData);
        return data;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ENV_MOD_DATA_ID_SEQUENCE")
    @SequenceGenerator(name = "ENV_MOD_DATA_ID_SEQUENCE", sequenceName = "ENV_MOD_DATA_ID_SEQUENCE")
    @Column(name = "ID")
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TASK_DATA_ID")
    public ForkedJavaTaskData getTaskData() {
        return taskData;
    }

    public void setTaskData(ForkedJavaTaskData taskData) {
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
