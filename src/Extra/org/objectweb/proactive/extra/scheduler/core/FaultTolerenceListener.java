/**
 *
 */
package org.objectweb.proactive.extra.scheduler.core;

import org.objectweb.proactive.extra.scheduler.common.job.Job;
import org.objectweb.proactive.extra.scheduler.common.job.JobEvent;
import org.objectweb.proactive.extra.scheduler.common.job.JobId;
import org.objectweb.proactive.extra.scheduler.common.scheduler.SchedulerEventListener;
import org.objectweb.proactive.extra.scheduler.common.task.TaskEvent;


/**
 * FaultTolerenceListener ...
 *
 * @author jlscheef - ProActiveTeam
 * @date 19 oct. 07
 * @version 3.2
 *
 */
public class FaultTolerenceListener implements SchedulerEventListener {

    /**
     * @see org.objectweb.proactive.extra.scheduler.common.scheduler.SchedulerEventListener#changeJobPriorityEvent(org.objectweb.proactive.extra.scheduler.common.job.JobEvent)
     */
    @Override
    public void changeJobPriorityEvent(JobEvent event) {
        // TODO Auto-generated method stub
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.common.scheduler.SchedulerEventListener#jobKilledEvent(org.objectweb.proactive.extra.scheduler.common.job.JobId)
     */
    @Override
    public void jobKilledEvent(JobId jobId) {
        // TODO Auto-generated method stub
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.common.scheduler.SchedulerEventListener#jobPausedEvent(org.objectweb.proactive.extra.scheduler.common.job.JobEvent)
     */
    @Override
    public void jobPausedEvent(JobEvent event) {
        // TODO Auto-generated method stub
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.common.scheduler.SchedulerEventListener#jobResumedEvent(org.objectweb.proactive.extra.scheduler.common.job.JobEvent)
     */
    @Override
    public void jobResumedEvent(JobEvent event) {
        // TODO Auto-generated method stub
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.common.scheduler.SchedulerEventListener#newPendingJobEvent(org.objectweb.proactive.extra.scheduler.common.job.Job)
     */
    @Override
    public void newPendingJobEvent(Job job) {
        // TODO Auto-generated method stub
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.common.scheduler.SchedulerEventListener#pendingToRunningJobEvent(org.objectweb.proactive.extra.scheduler.common.job.JobEvent)
     */
    @Override
    public void pendingToRunningJobEvent(JobEvent event) {
        // TODO Auto-generated method stub
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.common.scheduler.SchedulerEventListener#pendingToRunningTaskEvent(org.objectweb.proactive.extra.scheduler.common.task.TaskEvent)
     */
    @Override
    public void pendingToRunningTaskEvent(TaskEvent event) {
        // TODO Auto-generated method stub
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.common.scheduler.SchedulerEventListener#removeFinishedJobEvent(org.objectweb.proactive.extra.scheduler.common.job.JobEvent)
     */
    @Override
    public void removeFinishedJobEvent(JobEvent event) {
        // TODO Auto-generated method stub
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.common.scheduler.SchedulerEventListener#runningToFinishedJobEvent(org.objectweb.proactive.extra.scheduler.common.job.JobEvent)
     */
    @Override
    public void runningToFinishedJobEvent(JobEvent event) {
        // TODO Auto-generated method stub
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.common.scheduler.SchedulerEventListener#runningToFinishedTaskEvent(org.objectweb.proactive.extra.scheduler.common.task.TaskEvent)
     */
    @Override
    public void runningToFinishedTaskEvent(TaskEvent event) {
        // TODO Auto-generated method stub
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.common.scheduler.SchedulerEventListener#schedulerImmediatePausedEvent()
     */
    @Override
    public void schedulerImmediatePausedEvent() {
        // TODO Auto-generated method stub
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.common.scheduler.SchedulerEventListener#schedulerKilledEvent()
     */
    @Override
    public void schedulerKilledEvent() {
        // TODO Auto-generated method stub
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.common.scheduler.SchedulerEventListener#schedulerPausedEvent()
     */
    @Override
    public void schedulerPausedEvent() {
        // TODO Auto-generated method stub
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.common.scheduler.SchedulerEventListener#schedulerResumedEvent()
     */
    @Override
    public void schedulerResumedEvent() {
        // TODO Auto-generated method stub
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.common.scheduler.SchedulerEventListener#schedulerShutDownEvent()
     */
    @Override
    public void schedulerShutDownEvent() {
        // TODO Auto-generated method stub
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.common.scheduler.SchedulerEventListener#schedulerShuttingDownEvent()
     */
    @Override
    public void schedulerShuttingDownEvent() {
        // TODO Auto-generated method stub
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.common.scheduler.SchedulerEventListener#schedulerStartedEvent()
     */
    @Override
    public void schedulerStartedEvent() {
        // TODO Auto-generated method stub
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.common.scheduler.SchedulerEventListener#schedulerStoppedEvent()
     */
    @Override
    public void schedulerStoppedEvent() {
        // TODO Auto-generated method stub
    }
}
