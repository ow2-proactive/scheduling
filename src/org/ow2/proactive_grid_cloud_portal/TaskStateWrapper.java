package org.ow2.proactive_grid_cloud_portal;

import java.util.Map;

import javax.xml.bind.annotation.XmlTransient;

import org.ow2.proactive.scheduler.common.task.TaskState;

public class TaskStateWrapper  {

    @XmlTransient
    private TaskState ts;
    
    public TaskStateWrapper(TaskState ts) {
        this.ts = ts;
    }
    
    public String getDescription() {
        return ts.getDescription();
    }
    
    public Map<String, String> getGenericInformations() {
        return ts.getGenericInformations();
    }
    
    public long getStartTime() {
        return ts.getStartTime();
    }
    
    public long getFinishedTime() {
        return ts.getFinishedTime();
    }
    
    
}
