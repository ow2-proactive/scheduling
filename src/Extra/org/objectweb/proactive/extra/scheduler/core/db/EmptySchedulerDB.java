package org.objectweb.proactive.extra.scheduler.core.db;

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
public class EmptySchedulerDB extends AbstractSchedulerDB {

    /**
     * @see org.objectweb.proactive.extra.scheduler.core.db.AbstractSchedulerDB#addJob(org.objectweb.proactive.extra.scheduler.job.InternalJob)
     */
    @Override
    public boolean addJob(InternalJob internalJob) {
        return true;
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.core.db.AbstractSchedulerDB#addTaskResult(org.objectweb.proactive.extra.scheduler.common.task.TaskResult)
     */
    @Override
    public boolean addTaskResult(TaskResult taskResult) {
        return true;
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.core.db.AbstractSchedulerDB#disconnect()
     */
    @Override
    public void disconnect() {
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.core.db.AbstractSchedulerDB#getJobResult()
     */
    @Override
    public JobResult getJobResult() {
        return null;
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.core.db.AbstractSchedulerDB#getRecoverableState()
     */
    @Override
    public RecoverableState getRecoverableState() {
        return null;
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.core.db.AbstractSchedulerDB#getTaskResult()
     */
    @Override
    public TaskResult getTaskResult() {
        return null;
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.core.db.AbstractSchedulerDB#removeJob(org.objectweb.proactive.extra.scheduler.common.job.JobId)
     */
    @Override
    public boolean removeJob(JobId jobId) {
        return true;
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.core.db.AbstractSchedulerDB#setJobAndTasksEvents(org.objectweb.proactive.extra.scheduler.common.job.JobEvent,
     *      java.util.List)
     */
    @Override
    public boolean setJobAndTasksEvents(JobEvent jobEvent,
        List<TaskEvent> tasksEvents) {
        return true;
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.core.db.AbstractSchedulerDB#setJobEvent(org.objectweb.proactive.extra.scheduler.common.job.JobEvent)
     */
    @Override
    public boolean setJobEvent(JobEvent jobEvent) {
        return true;
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.core.db.AbstractSchedulerDB#setTaskEvent(org.objectweb.proactive.extra.scheduler.common.task.TaskEvent)
     */
    @Override
    public boolean setTaskEvent(TaskEvent taskEvent) {
        return true;
    }
}
