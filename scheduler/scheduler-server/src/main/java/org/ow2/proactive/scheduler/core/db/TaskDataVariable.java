package org.ow2.proactive.scheduler.core.db;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;


@Entity
@Table(name = "TASK_VARIABLE")
public class TaskDataVariable {
    
    private String name;
    
    private String value;
    
    private boolean jobInherited;
    
    private String model;

    @Column(name = "VARIABLE_NAME")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "VARIABLE_VALUE")
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

    @Column(name = "VARIABLE_MODEL")
    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

}
