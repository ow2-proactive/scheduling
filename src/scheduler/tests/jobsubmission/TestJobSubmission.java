package jobsubmission;

import static junit.framework.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;

import org.objectweb.proactive.api.PAActiveObject;
import org.ow2.proactive.scheduler.common.job.Job;
import org.ow2.proactive.scheduler.common.job.JobEvent;
import org.ow2.proactive.scheduler.common.job.JobFactory;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.scheduler.SchedulerEvent;
import org.ow2.proactive.scheduler.common.task.TaskEvent;
import org.ow2.proactive.scheduler.common.task.TaskResult;


/*
 * This class tests a basic actions of a job submission to ProActive scheduler :
 * Connection to scheduler, with authentication
 * Register a monitor to Scheduler in order to receive events concerning
 * job submission.
 * 
 * Submit a job (test 1). 
 * After the job submission, the test monitor all jobs states changes, in order
 * to observe its execution :
 * job submitted (test 2),
 * job pending to running (test 3),
 * all task pending to running, and all tasks running to to finished (test 4),
 * job running to finished (test 5).
 * After it retrieves job's result and check that all 
 * tasks results are available (test 6).
 * 
 */
public class TestJobSubmission extends FunctionalTDefaultScheduler {

    private static String jobDescriptor = TestJobSubmission.class.getResource("/jobsubmission/Job_PI.xml")
            .getPath();

    @org.junit.Test
    public void action() throws Throwable {

        System.out.println("TestJobSubmission.action()!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");

        //Connect to Scheduler launched by  FunctionalTestDefaultScheduler.before();
        connect();

        //Create an Event receiver AO in order to observe jobs and tasks states changes
        SchedulerEventReceiver receiver = (SchedulerEventReceiver) PAActiveObject.newActive(
                SchedulerEventReceiver.class.getName(), new Object[] {});

        //register as EventListener AO previously created
        schedUserInterface.addSchedulerEventListener(receiver, SchedulerEvent.JOB_SUBMITTED,
                SchedulerEvent.JOB_PENDING_TO_RUNNING, SchedulerEvent.JOB_RUNNING_TO_FINISHED,
                SchedulerEvent.JOB_REMOVE_FINISHED, SchedulerEvent.TASK_PENDING_TO_RUNNING,
                SchedulerEvent.TASK_RUNNING_TO_FINISHED);

        System.out.println("------------------------------ Test 1");

        //job creation
        Job submittedJob = JobFactory.getFactory().createJob(jobDescriptor);

        //job submission
        JobId id = schedUserInterface.submit(submittedJob);

        System.out.println("------------------------------ Test 2");

        // wait for event : job submitted
        receiver.waitForNEvent(1);
        ArrayList<Job> jobsList = receiver.cleanNgetJobSubmittedEvents();
        assertTrue(jobsList.size() == 1);
        Job job = jobsList.get(0);
        assertTrue(job.getId().equals(id));

        System.out.println("------------------------------ Test 3");

        //wait for event : job pending to running
        receiver.waitForNEvent(1);
        ArrayList<JobEvent> eventsList = receiver.cleanNgetJobPendingToRunningEvents();
        assertTrue(eventsList.size() == 1);
        JobEvent jEvent = eventsList.get(0);
        assertTrue(jEvent.getJobId().equals(id));

        System.out.println("------------------------------ Test 4");

        //wait whole tasks execution : two events per task, task pending to running, and task running to finished  
        receiver.waitForNEvent(jEvent.getTotalNumberOfTasks() * 2);
        ArrayList<TaskEvent> tEventList = receiver.cleanNgetTaskPendingToRunningEvents();

        assertTrue(tEventList.size() == jEvent.getTotalNumberOfTasks());

        tEventList = receiver.cleanNgetTaskRunningToFinishedEvents();
        assertTrue(tEventList.size() == jEvent.getTotalNumberOfTasks());

        System.out.println("------------------------------ Test 5");

        //wait for event : job Running to finished
        receiver.waitForNEvent(1);
        eventsList = receiver.cleanNgetjobRunningToFinishedEvents();
        assertTrue(eventsList.size() == 1);
        jEvent = eventsList.get(0);
        assertTrue(jEvent.getJobId().equals(id));

        //disconnect();
        //connect();        

        System.out.println("------------------------------ Test 6");

        JobResult res = schedUserInterface.getJobResult(id);
        schedUserInterface.remove(id);

        //check that there is no exception in results
        assertTrue(res.getExceptionResults().size() == 0);

        //wait for event : result retrieval
        receiver.waitForNEvent(1);
        eventsList = receiver.cleanNgetjobRemoveFinishedEvents();
        assertTrue(eventsList.size() == 1);
        jEvent = eventsList.get(0);
        assertTrue(jEvent.getJobId().equals(id));

        HashMap<String, TaskResult> results = res.getAllResults();

        //check that number of results correspond to number of tasks       
        assertTrue(jEvent.getNumberOfFinishedTasks() == results.size());

        //check that all tasks results are defined
        for (TaskResult taskRes : results.values()) {
            assertTrue(taskRes.value() != null);
        }

        System.out.println("------------------------------ end of test");
    }
}
