package org.ow2.proactive_grid_cloud_portal.scheduler.dto;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement
public class TaskIdsPage {

    private int size;

    private List<String> taskIds;

    public TaskIdsPage() {

    }

    public TaskIdsPage(List<String> taskIds) {
        this.taskIds = taskIds;
        this.size = taskIds.size();
    }

    public int getSize() {
        return size;
    }

    public void setSize(final int size) {
        this.size = size;
    }

    public List<String> getTaskIds() {
        return taskIds;
    }

    public void setTaskIds(List<String> taskIds) {
        this.taskIds = taskIds;
    }

    @Override
    public String toString() {
        return "TaskIdsPage{" + "size=" + size + ", tasksIds='" + taskIds + '\'' + '}';
    }

}
