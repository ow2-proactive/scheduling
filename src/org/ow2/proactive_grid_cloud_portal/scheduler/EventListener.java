package org.ow2.proactive_grid_cloud_portal.scheduler;

import org.ow2.proactive.scheduler.common.NotificationData;
import org.ow2.proactive.scheduler.common.SchedulerEvent;
import org.ow2.proactive.scheduler.common.SchedulerEventListener;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.UserIdentification;
import org.ow2.proactive.scheduler.common.task.TaskInfo;
import org.ow2.proactive_grid_cloud_portal.scheduler.SchedulerStateListener.State;

public class EventListener implements SchedulerEventListener {

    private State state;

    public EventListener() {
    }

    public EventListener(State state) {
        this.state = state;
    }

    @Override
    public void jobStateUpdatedEvent(NotificationData<JobInfo> data) {
        state.incrementRevision();
    }

    @Override
    public void jobSubmittedEvent(JobState jobState) {
        state.incrementRevision();
    }

    @Override
    public void schedulerStateUpdatedEvent(SchedulerEvent event) {
        state.incrementRevision();
        // event doesn't provide current state, just reset stored value so that state will be re-read on demand
        state.setStatus(null);
    }

    @Override
    public void taskStateUpdatedEvent(NotificationData<TaskInfo> event) {
        state.incrementRevision();
    }

    @Override
    public void usersUpdatedEvent(NotificationData<UserIdentification> event) {
        state.incrementRevision();
    }

}
