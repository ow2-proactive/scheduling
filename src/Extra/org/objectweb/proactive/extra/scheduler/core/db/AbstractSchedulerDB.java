package org.objectweb.proactive.extra.scheduler.core.db;

import java.sql.SQLException;
import java.util.List;

import org.objectweb.proactive.extra.scheduler.common.job.JobEvent;
import org.objectweb.proactive.extra.scheduler.common.job.JobId;
import org.objectweb.proactive.extra.scheduler.common.job.JobResult;
import org.objectweb.proactive.extra.scheduler.common.task.TaskEvent;
import org.objectweb.proactive.extra.scheduler.common.task.TaskResult;
import org.objectweb.proactive.extra.scheduler.job.InternalJob;


/**
 * @author FRADJ Johann
 */
public abstract class AbstractSchedulerDB {
    // TODO comments
    private static AbstractSchedulerDB instance = null;

    public abstract boolean addJob(InternalJob internalJob);

    public abstract boolean removeJob(JobId jobId);

    public abstract boolean setJobEvent(JobEvent jobEvent);

    public abstract boolean setTaskEvent(TaskEvent taskEvent);

    public abstract boolean setJobAndTasksEvents(JobEvent jobEvent,
        List<TaskEvent> tasksEvents);

    public abstract boolean addTaskResult(TaskResult taskResult);

    public abstract RecoverableState getRecoverableState();

    public abstract JobResult getJobResult();

    public abstract TaskResult getTaskResult();

    public abstract void disconnect();

    /**
     * If the instance is null, this method create a new instance before
     * returning it.
     *
     * @return the SchedulerDB instance.
     */
    public static AbstractSchedulerDB getInstance() {
        if (instance == null) {
            try {
                instance = new SchedulerDB();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return instance;
    }

    /**
     * Set instance to null BUT BEFORE doing that, call the disconnect method !
     */
    public static void clearInstance() {
        instance.disconnect();
        instance = null;
    }
}
