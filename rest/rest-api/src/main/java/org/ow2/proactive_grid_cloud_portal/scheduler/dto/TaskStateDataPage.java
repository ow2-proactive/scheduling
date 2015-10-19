package org.ow2.proactive_grid_cloud_portal.scheduler.dto;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement
public class TaskStateDataPage {

    private int size;

    private List<TaskStateData> tasks;

    public TaskStateDataPage() {

    }

    public TaskStateDataPage(List<TaskStateData> tasks) {
        this.tasks = tasks;
        this.size = tasks.size();
    }

    public int getSize() {
        return size;
    }

    public void setSize(final int size) {
        this.size = size;
    }

    public List<TaskStateData> getTasks() {
        return tasks;
    }

    public void setTasks(List<TaskStateData> tasks) {
        this.tasks = tasks;
    }

    @Override
    public String toString() {
        return "TaskIdsPage{" + "size=" + size + ", tasks='" + tasks + '\'' + '}';
    }

}
