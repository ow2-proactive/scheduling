package functionaltests;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.ow2.proactive.authentication.crypto.CredData;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.SchedulerAuthenticationInterface;
import org.ow2.proactive.scheduler.common.exception.NotConnectedException;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.executable.JavaExecutable;
import org.ow2.proactive.scheduler.common.usage.JobUsage;
import org.ow2.tests.FunctionalTest;


/**
 * Test against SchedulerUsage interface.
 */
public class TestGetUsage extends FunctionalTest {

    @Test
    public void testGetMyAccountUsage() throws Exception {
        Date beforeJobExecution = new Date();

        // put some data in the database
        Scheduler firstUser = SchedulerTHelper.getSchedulerInterface();
        JobId jobId = firstUser.submit(createJob());
        SchedulerTHelper.waitForEventJobFinished(jobId, 30000);

        Date afterJobExecution = new Date();

        // We try to retrieve usage on the job I just ran
        List<JobUsage> adminUsages = firstUser.getMyAccountUsage(beforeJobExecution, afterJobExecution);
        assertFalse(adminUsages.isEmpty());

        // Do we properly check for user connection ?
        firstUser.disconnect();
        try {
            firstUser.getMyAccountUsage(beforeJobExecution, afterJobExecution);
            fail("Should throw a not connecter exception because i just disconnected");
        } catch (NotConnectedException e) {
            // Ok that is expected
        }

        // another user
        SchedulerAuthenticationInterface auth = SchedulerTHelper.getSchedulerAuth();
        Credentials cred = Credentials.createCredentials(new CredData("user", "pwd"), auth.getPublicKey());
        Scheduler otherUser = auth.login(cred);

        // This user has not ran any job
        List<JobUsage> userUsages = otherUser.getMyAccountUsage(beforeJobExecution, afterJobExecution);
        assertTrue(userUsages.isEmpty());
    }

    private TaskFlowJob createJob() throws Exception {
        TaskFlowJob job = new TaskFlowJob();
        job.setName(this.getClass().getSimpleName());

        JavaTask javaTask = new JavaTask();
        javaTask.setExecutableClassName(TestJavaTask.class.getName());
        javaTask.setName("Test task");

        job.addTask(javaTask);

        return job;
    }

    public static class TestJavaTask extends JavaExecutable {

        @Override
        public Serializable execute(TaskResult... results) throws Throwable {
            return "OK";
        }

    }
}
