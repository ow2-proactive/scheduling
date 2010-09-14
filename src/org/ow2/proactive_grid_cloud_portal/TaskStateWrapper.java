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
    
    public String getName(){
    	return ts.getName();
    }
    
    public String getId(){
    	return ts.getId().value();
    }
    
    public String getStatus(){
    	return ts.getStatus().name();
    }
    
    public String getHostName(){
    	return ts.getExecutionHostName();
    }
    
    public long getExecutionDuration(){
    	return ts.getExecutionDuration();
    }
    
    public String getResultPreview(){
    	return ts.getResultPreview();
    }
    
}
