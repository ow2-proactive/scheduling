package org.objectweb.proactive.extra.scheduler.core.db;

import org.objectweb.proactive.extra.scheduler.common.job.JobResult;
import org.objectweb.proactive.extra.scheduler.common.task.TaskResult;


/**
 * @author FRADJ Johann
 */
public abstract class SchedulerDB {
    //TODO comments
    private static SchedulerDB instance = null;

    public abstract void addJob();

    public abstract void setJobStatus();

    public abstract void setTaskStatus();

    public abstract void addTaskResult();

    public abstract RecoverableState getRecoverableState();

    public abstract JobResult getJobResult();

    public abstract TaskResult getTaskResult();

    /**
     * If the instance is null, this method create a new instance before returning it.
     *
     * @return the SchedulerDB instance.
     */
    public static SchedulerDB getInstance() {
        if (instance == null) {
            instance = new SchedulerDBImpl();
        }
        return instance;
    }
}
