package functionaltests;

import java.io.Serializable;

import org.junit.Assert;
import org.ow2.proactive.scheduler.common.NotificationData;
import org.ow2.proactive.scheduler.common.SchedulerEvent;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.UserIdentification;
import org.ow2.proactive.scheduler.common.task.TaskInfo;
import org.ow2.proactive.scheduler.smartproxy.common.SchedulerEventListenerExtended;

import functionaltests.monitor.EventMonitor;


public class MyEventListener implements SchedulerEventListenerExtended, Serializable {

    boolean jobFinished = false;
    boolean pullDataFinished = false;
    JobId jobId;
    int count = 0;
    boolean synchronous = false;

    EventMonitor monitor;

    /**
     *
     * @param id
     *            of the job this Listener is intrested in
     */
    public boolean setJobID(JobId id) {
        this.jobId = id;
        return true;
    }

    public MyEventListener() {

    }

    public boolean reset() {
        jobFinished = false;
        count = 0;
        return true;
    }

    public synchronized boolean setSynchronous(boolean synchronous) {
        this.synchronous = synchronous;
        return true;
    }

    /**
     * to be called directly on the java object (not on the remote active
     * reference) the monitor needs to be copied by reference
     */
    public boolean setMonitor(EventMonitor monitor) {
        this.monitor = monitor;
        return true;
    }

    @Override
    public void schedulerStateUpdatedEvent(SchedulerEvent eventType) {
        System.out.println("MyEventListener.schedulerStateUpdatedEvent() ");

    }

    @Override
    public void jobSubmittedEvent(JobState job) {
        System.out.println("MyEventListener.jobSubmittedEvent()");

    }

    @Override
    public void jobStateUpdatedEvent(NotificationData<JobInfo> notification) {
        System.out.println("MyEventListener.jobStateUpdatedEvent() " + notification);

        JobId id = notification.getData().getJobId();
        if (!id.equals(jobId))
            return;

        SchedulerEvent event = notification.getEventType();
        if (event == SchedulerEvent.JOB_RUNNING_TO_FINISHED) {
            jobFinished = true;
            if (synchronous) {
                synchronized (monitor) {
                    System.out.println("[MyEventListener] job finished event occured for " + jobId);
                    monitor.setEventOccured();
                    monitor.notifyAll();
                }
            }
        }
    }

    @Override
    public void taskStateUpdatedEvent(NotificationData<TaskInfo> notification) {
        System.out.println("MyEventListener.taskStateUpdatedEvent()");
    }

    @Override
    public void usersUpdatedEvent(NotificationData<UserIdentification> notification) {
        System.out.println("MyEventListener.usersUpdatedEvent()");
    }

    @Override
    public void pullDataFinished(String jobId, String taskName, String localFolderPath) {

        if (!jobId.equals(this.jobId.toString()))
            return;

        synchronized (monitor) {
            count++;
            System.out.println("[MyEventListener] pull data finished event occured for " + taskName);
            if (count == TestSmartProxy.NB_TASKS) {
                count = 0;
                monitor.setEventOccured();
                monitor.notifyAll();
            }
        }
    }

    @Override
    public void pullDataFailed(String jobId, String taskName, String remoteFolder_URL, Throwable t) {
        t.printStackTrace();
        count = 0;
        Assert.assertTrue("[MyEventListener] Pull data operation failed: " + t.getMessage(), false);
    }

    /**
     *
     * @return true if an event indicating the job is finished has been received
     *         by this listener
     */
    public boolean getJobFinished() {
        return jobFinished;
    }

}