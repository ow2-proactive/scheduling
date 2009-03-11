package functionaltests.monitor;

import org.ow2.proactive.scheduler.common.SchedulerEvent;
import org.ow2.proactive.scheduler.common.SchedulerEventListener;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.UserIdentification;
import org.ow2.proactive.scheduler.common.task.TaskInfo;


/**
 * Scheduler event receiver for functional tests
 * Receives all scheduler events and
 * forward them a SchedulerMonitorsHandler object to handle them.
 *
 * @author ProActive team
 *
 */
public class MonitorEventReceiver implements SchedulerEventListener {

    private SchedulerMonitorsHandler monitorsHandler;

    /**
     * ProActive Empty constructor
     */
    public MonitorEventReceiver() {
    }

    /**
     * @param monitor SchedulerMonitorsHandler object which is notified
     * of Schedulers events.
     */
    public MonitorEventReceiver(SchedulerMonitorsHandler monitor) {
        this.monitorsHandler = monitor;
    }

    //---------------------------------------------------------------//
    //Methods inherited form SchedulerEventListener
    //---------------------------------------------------------------//

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.SchedulerEventListener#jobPendingToRunningEvent(org.ow2.proactive.scheduler.common.job.JobEvent)
     */
    public void jobPendingToRunningEvent(JobInfo jInfo) {
        monitorsHandler.handleJobEvent(SchedulerEvent.JOB_PENDING_TO_RUNNING, jInfo);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.SchedulerEventListener#jobRemoveFinishedEvent(org.ow2.proactive.scheduler.common.job.JobEvent)
     */
    public void jobRemoveFinishedEvent(JobInfo jInfo) {
        monitorsHandler.handleJobEvent(SchedulerEvent.JOB_REMOVE_FINISHED, jInfo);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.SchedulerEventListener#jobRunningToFinishedEvent(org.ow2.proactive.scheduler.common.job.JobEvent)
     */
    public void jobRunningToFinishedEvent(JobInfo jInfo) {
        monitorsHandler.handleJobEvent(SchedulerEvent.JOB_RUNNING_TO_FINISHED, jInfo);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.SchedulerEventListener#jobSubmittedEvent(org.ow2.proactive.scheduler.common.job.Job)
     */
    public void jobSubmittedEvent(JobState jState) {
        monitorsHandler.handleJobEvent(SchedulerEvent.JOB_SUBMITTED, jState);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.SchedulerEventListener#taskPendingToRunningEvent(org.ow2.proactive.scheduler.common.task.TaskEvent)
     */
    public void taskPendingToRunningEvent(TaskInfo tInfo) {
        monitorsHandler.handleTaskEvent(SchedulerEvent.TASK_PENDING_TO_RUNNING, tInfo);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.SchedulerEventListener#taskRunningToFinishedEvent(org.ow2.proactive.scheduler.common.task.TaskEvent)
     */
    public void taskRunningToFinishedEvent(TaskInfo tInfo) {
        monitorsHandler.handleTaskEvent(SchedulerEvent.TASK_RUNNING_TO_FINISHED, tInfo);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.SchedulerEventListener#taskWaitingForRestart(org.ow2.proactive.scheduler.common.task.TaskEvent)
     */
    public void taskWaitingForRestart(TaskInfo tInfo) {
        monitorsHandler.handleTaskEvent(SchedulerEvent.TASK_WAITING_FOR_RESTART, tInfo);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.SchedulerEventListener#jobChangePriorityEvent(org.ow2.proactive.scheduler.common.job.JobEvent)
     */
    public void jobChangePriorityEvent(JobInfo jInfo) {
        monitorsHandler.handleJobEvent(SchedulerEvent.JOB_CHANGE_PRIORITY, jInfo);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.SchedulerEventListener#jobPausedEvent(org.ow2.proactive.scheduler.common.job.JobEvent)
     */
    public void jobPausedEvent(JobInfo jInfo) {
        monitorsHandler.handleJobEvent(SchedulerEvent.JOB_PAUSED, jInfo);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.SchedulerEventListener#jobResumedEvent(org.ow2.proactive.scheduler.common.job.JobEvent)
     */
    public void jobResumedEvent(JobInfo jInfo) {
        monitorsHandler.handleJobEvent(SchedulerEvent.JOB_RESUMED, jInfo);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.SchedulerEventListener#schedulerFrozenEvent()
     */
    public void schedulerFrozenEvent() {
        monitorsHandler.handleSchedulerStateEvent(SchedulerEvent.FROZEN);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.SchedulerEventListener#schedulerKilledEvent()
     */
    public void schedulerKilledEvent() {
        monitorsHandler.handleSchedulerStateEvent(SchedulerEvent.KILLED);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.SchedulerEventListener#schedulerPausedEvent()
     */
    public void schedulerPausedEvent() {
        monitorsHandler.handleSchedulerStateEvent(SchedulerEvent.PAUSED);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.SchedulerEventListener#schedulerRMDownEvent()
     */
    public void schedulerRMDownEvent() {
        monitorsHandler.handleSchedulerStateEvent(SchedulerEvent.RM_DOWN);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.SchedulerEventListener#schedulerRMUpEvent()
     */
    public void schedulerRMUpEvent() {
        monitorsHandler.handleSchedulerStateEvent(SchedulerEvent.RM_UP);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.SchedulerEventListener#schedulerResumedEvent()
     */
    public void schedulerResumedEvent() {
        monitorsHandler.handleSchedulerStateEvent(SchedulerEvent.RESUMED);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.SchedulerEventListener#schedulerShutDownEvent()
     */
    public void schedulerShutDownEvent() {
        monitorsHandler.handleSchedulerStateEvent(SchedulerEvent.SHUTDOWN);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.SchedulerEventListener#schedulerShuttingDownEvent()
     */
    public void schedulerShuttingDownEvent() {
        monitorsHandler.handleSchedulerStateEvent(SchedulerEvent.SHUTTING_DOWN);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.SchedulerEventListener#schedulerStartedEvent()
     */
    public void schedulerStartedEvent() {
        monitorsHandler.handleSchedulerStateEvent(SchedulerEvent.STARTED);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.SchedulerEventListener#schedulerStoppedEvent()
     */
    public void schedulerStoppedEvent() {
        monitorsHandler.handleSchedulerStateEvent(SchedulerEvent.STOPPED);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.SchedulerEventListener#usersUpdate(org.ow2.proactive.scheduler.common.job.UserIdentification)
     */
    public void usersUpdate(UserIdentification userIdentification) {
        monitorsHandler.handleSchedulerStateEvent(SchedulerEvent.USERS_UPDATE);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.SchedulerEventListener#schedulerPolicyChangedEvent(java.lang.String)
     */
    public void schedulerPolicyChangedEvent(String newPolicyName) {
        monitorsHandler.handleSchedulerStateEvent(SchedulerEvent.POLICY_CHANGED);
    }
}