/**
 *
 */
package org.objectweb.proactive.extra.scheduler.common.scheduler;


/**
 * SchedulerEvent is an Enumeration of all different events that scheduler can invoke.
 *
 * @author jlscheef - ProActiveTeam
 * @date 18 oct. 07
 * @version 3.2
 *
 */
public enum SchedulerEvent {
    IMMEDIATE_PAUSED("schedulerImmediatePausedEvent"),
    RESUMED("schedulerResumedEvent"),SHUTDOWN("schedulerShutDownEvent"),
    SHUTTING_DOWN("schedulerShuttingDownEvent"),
    STARTED("schedulerStartedEvent"),
    STOPPED("schedulerStoppedEvent"),
    KILLED("schedulerKilledEvent"),
    JOB_KILLED("jobKilledEvent"),
    JOB_PAUSED("jobPausedEvent"),
    PENDING_TO_RUNNING_JOB("pendingToRunningJobEvent"),
    JOB_RESUMED("jobResumedEvent"),
    NEW_PENDING_JOB("newPendingJobEvent"),
    RUNNING_TO_FINISHED_JOB("runningToFinishedJobEvent"),
    REMOVE_FINISHED_JOB("removeFinishedJobEvent"),
    PENDING_TO_RUNNING_TASK("pendingToRunningTaskEvent"),
    RUNNING_TO_FINISHED_TASK("runningToFinishedTaskEvent"),
    CHANGE_JOB_PRIORITY("changeJobPriorityEvent"),
    PAUSED("schedulerPausedEvent");
    private String methodName;

    /**
     * Default constructor.
     * @param method the method to call as a string.
     */
    SchedulerEvent(String method) {
        methodName = method;
    }

    /**
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString() {
        return methodName;
    }
}
