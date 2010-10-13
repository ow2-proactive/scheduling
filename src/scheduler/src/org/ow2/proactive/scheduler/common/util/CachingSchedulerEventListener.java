package org.ow2.proactive.scheduler.common.util;

import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.api.PARemoteObject;
import org.objectweb.proactive.core.ProActiveException;
import org.ow2.proactive.scheduler.common.NotificationData;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.SchedulerEvent;
import org.ow2.proactive.scheduler.common.SchedulerEventListener;
import org.ow2.proactive.scheduler.common.SchedulerState;
import org.ow2.proactive.scheduler.common.UniversalSchedulerListener;
import org.ow2.proactive.scheduler.common.exception.NotConnectedException;
import org.ow2.proactive.scheduler.common.exception.PermissionException;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.UserIdentification;
import org.ow2.proactive.scheduler.common.task.TaskInfo;


/**
 *
 *
 */
public class CachingSchedulerEventListener implements SchedulerEventListener {

    protected SchedulerState schedulerState = null;

    public CachingSchedulerEventListener(Scheduler scheduler) throws ProActiveException,
            NotConnectedException, PermissionException {
        UniversalSchedulerListener usl = new UniversalSchedulerListener(this);
        usl = PARemoteObject.turnRemote(usl);
        schedulerState = scheduler.addEventListener(usl, false, true);
        schedulerState = PAFuture.getFutureValue(schedulerState);
    }

    public void schedulerStateUpdatedEvent(SchedulerEvent eventType) {
        schedulerState.update(eventType);
    }

    public void jobSubmittedEvent(NotificationData<JobState> job) {
        schedulerState.update(job);

    }

    public void jobStateUpdatedEvent(NotificationData<JobInfo> notification) {
        schedulerState.update(notification);
    }

    public void taskStateUpdatedEvent(NotificationData<TaskInfo> notification) {
        schedulerState.update(notification);

    }

    public void usersUpdatedEvent(NotificationData<UserIdentification> notification) {
        schedulerState.update(notification);
    }

    public SchedulerState getSchedulerState() {
        return schedulerState;
    }

    public void jobSubmittedEvent(JobState job) {
        schedulerState.update(job);
    }

}
