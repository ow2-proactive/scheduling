package functionaltests;

import org.objectweb.proactive.api.PAActiveObject;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.scheduler.common.NotificationData;
import org.ow2.proactive.scheduler.common.SchedulerAuthenticationInterface;
import org.ow2.proactive.scheduler.common.SchedulerConnection;
import org.ow2.proactive.scheduler.common.SchedulerEvent;
import org.ow2.proactive.scheduler.common.SchedulerEventListener;
import org.ow2.proactive.scheduler.common.UserSchedulerInterface;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.UserIdentification;
import org.ow2.proactive.scheduler.common.task.TaskInfo;
import org.ow2.proactive.scheduler.common.task.TaskResult;


public class SubmitJob implements SchedulerEventListener {

    private JobId myJobId;
    private UserSchedulerInterface user;
    private int terminated = 0;

    public void begin() {
        //non blocking method to use futur management
        try {
            //connect the Scheduler
            //get the authentication interface using the SchedulerConnection
            SchedulerAuthenticationInterface auth = SchedulerConnection.waitAndJoin("rmi://localhost/");
            //get the user interface using the retrieved SchedulerAuthenticationInterface
            user = auth.logAsUser(Credentials.createCredentials(SchedulerTHelper.username,
                    SchedulerTHelper.password, auth.getPublicKey()));

            //let the client be notified of its own 'job termination' -> job running to finished event
            user.addEventListener((SubmitJob) PAActiveObject.getStubOnThis(), true,
                    SchedulerEvent.TASK_RUNNING_TO_FINISHED, SchedulerEvent.JOB_RUNNING_TO_FINISHED);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void setJobId(JobId id) {
        myJobId = id;
    }

    //get the running to finished event of my job
    public void jobStateUpdatedEvent(NotificationData<JobInfo> notification) {
        if (myJobId.equals(notification.getData().getJobId())) {
            //test if it is my job
            System.out.print("Job " + myJobId + " terminated in ");
            //get the job result
            try {
                //1. get the job result
                JobResult result = user.getJobResult(myJobId);
                System.out.println(result.getJobInfo().getFinishedTime() -
                    result.getJobInfo().getStartTime() + "ms");
                //notify the test that it is terminated
                user.remove(notification.getData().getJobId());
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    public void taskStateUpdatedEvent(NotificationData<TaskInfo> notif) {
        try {
            System.out.println("Task '" + notif.getData().getTaskId() + "' result received !!");
            TaskResult result = user.getTaskResult(notif.getData().getJobId(), notif.getData().getTaskId()
                    .getReadableName());
            terminated++;
            System.out.println("(" + terminated + ")Result value = " + result.value());
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public void jobSubmittedEvent(JobState arg0) {
    }

    public void schedulerStateUpdatedEvent(SchedulerEvent arg0) {
    }

    public void usersUpdatedEvent(NotificationData<UserIdentification> arg0) {
    }

}