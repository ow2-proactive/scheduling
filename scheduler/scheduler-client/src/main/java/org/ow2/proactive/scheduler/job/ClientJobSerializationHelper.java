package org.ow2.proactive.scheduler.job;

import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskState;
import org.ow2.proactive.scheduler.task.ClientTaskState;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;


public class ClientJobSerializationHelper implements Serializable {
    public void serializeTasks(Map<TaskId, TaskState> tasks) throws IOException, ClassNotFoundException {
        for(TaskState task : tasks.values()) {
            if (task instanceof ClientTaskState) {
                ((ClientTaskState)task).restoreDependences(tasks);
            }
        }
    }
}
