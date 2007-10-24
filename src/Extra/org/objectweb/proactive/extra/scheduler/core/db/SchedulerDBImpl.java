package org.objectweb.proactive.extra.scheduler.core.db;

import org.objectweb.proactive.extra.scheduler.common.job.JobResult;
import org.objectweb.proactive.extra.scheduler.common.task.TaskResult;


/**
 * @author FRADJ Johann
 */
public class SchedulerDBImpl extends SchedulerDB {

    /**
     * @see org.objectweb.proactive.extra.scheduler.core.db.SchedulerDB#addJob()
     */
    @Override
    public void addJob() {
        // TODO Auto-generated method stub
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.core.db.SchedulerDB#addTaskResult()
     */
    @Override
    public void addTaskResult() {
        // TODO Auto-generated method stub
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.core.db.SchedulerDB#getJobResult()
     */
    @Override
    public JobResult getJobResult() {
        // TODO a implementer
        throw new RuntimeException("Not yet implemented");
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.core.db.SchedulerDB#getTaskResult()
     */
    @Override
    public TaskResult getTaskResult() {
        // TODO a implementer
        throw new RuntimeException("Not yet implemented");
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.core.db.SchedulerDB#setJobStatus()
     */
    @Override
    public void setJobStatus() {
        // TODO Auto-generated method stub
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.core.db.SchedulerDB#setTaskStatus()
     */
    @Override
    public void setTaskStatus() {
        // TODO Auto-generated method stub
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.core.db.SchedulerDB#getRecoverableState()
     */
    @Override
    public RecoverableState getRecoverableState() {
        // TODO Auto-generated method stub
        return null;
    }
}
