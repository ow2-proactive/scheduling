package org.ow2.proactive.scheduler.common.task;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;


/**
 * This class holds a paginated list of <code>TaskState</code>
 * server-wise and the total number of tasks.
 * 
 */
@XmlRootElement
public class TaskStatesPage {

    private int size;
    
    private List<TaskState> taskStates;
    
    public TaskStatesPage() {
        
    }
    
    public TaskStatesPage(List<TaskState> taskStates, int size) {
        this.taskStates = taskStates;
        this.size = size;
    }
    
    public int getSize() {
        return size;
    }
    
    public void setSize(final int size) {
        this.size = size;
    }
    
    public List<TaskState> getTaskStates() {
        return taskStates;
    }
    
    public void setTaskStates(List<TaskState> taskStates) {
        this.taskStates = taskStates;
    }
    
    @Override
    public String toString() {
        return "TaskStatePage{" + "size=" + size + ", taskStates='" + taskStates + '\'' + '}';
    }
}
