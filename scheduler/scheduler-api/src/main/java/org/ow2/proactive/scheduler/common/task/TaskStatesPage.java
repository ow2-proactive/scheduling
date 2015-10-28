package org.ow2.proactive.scheduler.common.task;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;


/**
 * This class is the super class of the every task that can be integrated in a job.<br>
 * A task contains some properties that can be set but also : <ul>
 * <li>A selection script that can be used to select a specific execution node for this task.</li>
 * <li>A preScript that will be launched before the real task (can be used to set environment vars).</li>
 * <li>A postScript that will be launched just after the end of the real task.
 * (this can be used to transfer files that have been created by the task).</li>
 * <li>A CleaningScript that will be launched by the resource manager to perform some cleaning. (deleting files or resources).</li>
 * </ul>
 * You will also be able to add dependences (if necessary) to
 * this task. The dependences mechanism are best describe below.
 * 
 * TODO
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
