package functionaltests.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.ow2.proactive.scheduler.common.NotificationData;
import org.ow2.proactive.scheduler.common.SchedulerEvent;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.UserIdentification;
import org.ow2.proactive.scheduler.common.task.TaskInfo;
import org.ow2.proactive.scheduler.core.SchedulerStateUpdate;
import org.junit.Assert;


public class MockSchedulingListener implements SchedulerStateUpdate {

    private List<SchedulerEvent> events = new ArrayList<>();

    @Override
    public void schedulerStateUpdated(SchedulerEvent eventType) {
        System.out.println("Scheduler state updated " + eventType);
    }

    @Override
    public void jobSubmitted(JobState jobState) {
        System.out.println("Submitted " + jobState.getName() + " " + jobState.getJobInfo().getJobId());
        events.add(SchedulerEvent.JOB_SUBMITTED);
    }

    @Override
    public void jobStateUpdated(String owner, NotificationData<JobInfo> notification) {
        System.out.println("Job state updated " + notification.getData().getJobId() + " " +
            notification.getEventType());
        events.add(notification.getEventType());
    }

    @Override
    public void taskStateUpdated(String owner, NotificationData<TaskInfo> notification) {
        System.out.println("Task state updated " + notification.getData().getName() + " " +
            notification.getEventType());
        events.add(notification.getEventType());
    }

    void assertEvents(SchedulerEvent... expectedEvents) {
        List<SchedulerEvent> expected = Arrays.asList(expectedEvents);
        Assert.assertEquals(expected, events);
        events.clear();
    }

    @Override
    public void usersUpdated(NotificationData<UserIdentification> notification) {
    }

}
