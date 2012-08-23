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
import org.ow2.proactive.scheduler.common.util.dsclient.ISchedulerEventListenerExtended;

import functionaltests.monitor.EventMonitor;


public class MyEventListener implements ISchedulerEventListenerExtended, Serializable {

    boolean jobFinished = false;
    boolean pullDataFinished = false;
    JobId jobId;

    EventMonitor monitor;

    /**
     *
     * @param id
     *            of the job this Listener is intrested in
     */
    public void setJobID(JobId id) {
        this.jobId = id;
    }

    public MyEventListener() {

    }

    /**
     * to be called directly on the java object (not on the remote active
     * reference) the monitor needs to be copied by reference
     */
    public void setMonitor(EventMonitor monitor) {
        this.monitor = monitor;
    }

    @Override
    public void schedulerStateUpdatedEvent(SchedulerEvent eventType) {
        System.out.println("MyEvenetListener.schedulerStateUpdatedEvent() ");

    }

    @Override
    public void jobSubmittedEvent(JobState job) {
        System.out.println("MyEvenetListener.jobSubmittedEvent()");

    }

    @Override
    public void jobStateUpdatedEvent(NotificationData<JobInfo> notification) {
        System.out.println("TestDSClient.MyEvenetListener.jobStateUpdatedEvent() " + notification);

        JobId id = notification.getData().getJobId();
        if (!id.equals(jobId))
            return;

        SchedulerEvent event = notification.getEventType();
        if (event == SchedulerEvent.JOB_RUNNING_TO_FINISHED) {
            jobFinished = true;
        }
    }

    @Override
    public void taskStateUpdatedEvent(NotificationData<TaskInfo> notification) {
        System.out.println("taskStateUpdatedEvent()");

    }

    @Override
    public void usersUpdatedEvent(NotificationData<UserIdentification> notification) {
        System.out.println("MyEvenetListener.usersUpdatedEvent()");

    }

    @Override
    public void pullDataFinished(String jobId, String localFolderPath) {

        if (!jobId.equals(this.jobId.toString()))
            return;

        synchronized (monitor) {
            monitor.setEventOccured();
            System.out.println("set event occured for " + monitor);
        }
    }

    @Override
    public void pullDataFailed(String jobId, String remoteFolder_URL, Throwable t) {
        t.printStackTrace();
        Assert.assertTrue("Pull data operation failed: " + t.getMessage(), false);
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