package functionaltests;

import java.io.Serializable;

import org.ow2.proactive.scheduler.common.job.JobEnvironment;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.JavaTask;

import functionalTests.FunctionalTest;
import functionaltests.executables.ComplexParamsExecutable;


public class ComplexTypeArgsTest extends FunctionalTest {

    @org.junit.Test
    public void run() throws Throwable {

        SchedulerTHelper.startScheduler();

        //creating job
        TaskFlowJob submittedJob = new TaskFlowJob();
        JavaTask task = new JavaTask();
        task.setName("t1");
        task.setExecutableClassName(ComplexParamsExecutable.class.getName());
        task.addArgument("param1", new UserTypeA(3));
        submittedJob.addTask(task);

        JobEnvironment env = new JobEnvironment();
        env.setJobClasspath(new String[] { System.getProperty("pa.scheduler.home") +
            "/classes/schedulerTests/" });
        submittedJob.setEnvironment(env);

        //test submission and event reception
        JobId id = SchedulerTHelper.testJobSubmission(submittedJob);

        //check job results
        JobResult res = SchedulerTHelper.getJobResult(id);

        Thread.sleep(5000);

    }

    public static class UserTypeA implements Serializable {
        private int value;

        public UserTypeA(int v) {
            this.value = v;
        }

        public int getValue() {
            return this.value;
        }

        public String toString() {
            return "[UserTypeA : " + this.value + "]";
        }
    }

}
