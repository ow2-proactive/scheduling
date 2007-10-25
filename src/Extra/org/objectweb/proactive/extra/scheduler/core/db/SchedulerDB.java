package org.objectweb.proactive.extra.scheduler.core.db;

import org.objectweb.proactive.extra.scheduler.common.job.JobResult;
import org.objectweb.proactive.extra.scheduler.common.task.TaskResult;


/**
 * @author FRADJ Johann
 */
public class SchedulerDB extends AbstractSchedulerDB {

    /**
     * @see org.objectweb.proactive.extra.scheduler.core.db.AbstractSchedulerDB#addJob()
     */
    @Override
    public void addJob() {
        // TODO Auto-generated method stub
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.core.db.AbstractSchedulerDB#addTaskResult()
     */
    @Override
    public void addTaskResult() {
        // TODO Auto-generated method stub
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.core.db.AbstractSchedulerDB#getJobResult()
     */
    @Override
    public JobResult getJobResult() {
        // TODO a implementer
        throw new RuntimeException("Not yet implemented");
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.core.db.AbstractSchedulerDB#getTaskResult()
     */
    @Override
    public TaskResult getTaskResult() {
        // TODO a implementer
        throw new RuntimeException("Not yet implemented");
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.core.db.AbstractSchedulerDB#setJobStatus()
     */
    @Override
    public void setJobStatus() {
        // TODO Auto-generated method stub
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.core.db.AbstractSchedulerDB#setTaskStatus()
     */
    @Override
    public void setTaskStatus() {
        // TODO Auto-generated method stub
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.core.db.AbstractSchedulerDB#getRecoverableState()
     */
    @Override
    public RecoverableState getRecoverableState() {
        // TODO Auto-generated method stub
        return null;
    }
}
