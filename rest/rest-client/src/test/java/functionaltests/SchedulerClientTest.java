/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package functionaltests;

import static functionaltests.RestFuncTHelper.getRestServerUrl;
import static functionaltests.jobs.SimpleJob.TEST_JOB;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.objectweb.proactive.core.util.wrapper.StringWrapper;
import org.ow2.proactive.authentication.ConnectionInfo;
import org.ow2.proactive.scheduler.common.NotificationData;
import org.ow2.proactive.scheduler.common.SchedulerEvent;
import org.ow2.proactive.scheduler.common.SchedulerEventListener;
import org.ow2.proactive.scheduler.common.SchedulerStatus;
import org.ow2.proactive.scheduler.common.job.Job;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.JobStatus;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.job.UserIdentification;
import org.ow2.proactive.scheduler.common.task.ForkEnvironment;
import org.ow2.proactive.scheduler.common.task.ScriptTask;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskInfo;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.TaskState;
import org.ow2.proactive.scheduler.common.task.TaskStatus;
import org.ow2.proactive.scheduler.rest.ISchedulerClient;
import org.ow2.proactive.scheduler.rest.SchedulerClient;
import org.ow2.proactive.scheduler.task.exceptions.TaskException;
import org.ow2.proactive.scripting.SimpleScript;
import org.ow2.proactive.scripting.TaskScript;

import com.google.common.io.Files;

import functionaltests.jobs.ErrorTask;
import functionaltests.jobs.LogTask;
import functionaltests.jobs.MetadataTask;
import functionaltests.jobs.NonTerminatingJob;
import functionaltests.jobs.SimpleJob;
import functionaltests.jobs.VariableTask;


public class SchedulerClientTest extends AbstractRestFuncTestCase {

    /** Maximum wait time of 5 minutes */
    private static final long MAX_WAIT_TIME = 5 * 60 * 1000;

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

    @BeforeClass
    public static void beforeClass() throws Exception {
        init(2);
    }

    @Test(timeout = MAX_WAIT_TIME)
    public void testLogin() throws Exception {
        clientInstance();
    }

    @Test(timeout = MAX_WAIT_TIME)
    public void testRenewSession() throws Exception {
        ISchedulerClient client = clientInstance();
        SchedulerStatus status = client.getStatus();
        assertNotNull(status);
        // use an invalid session
        client.setSession("invalid-session-identifier");
        // client should automatically renew the session identifier
        status = client.getStatus();
        assertNotNull(status);
    }

    @Test(timeout = MAX_WAIT_TIME)
    public void testDisconnect() throws Exception {
        ISchedulerClient client = clientInstance();
        client.disconnect();
        Assert.assertFalse(client.isConnected());
        client = clientInstance();
        Assert.assertTrue(client.isConnected());

    }

    @Test(timeout = MAX_WAIT_TIME)
    public void testWaitForTerminatingJob() throws Exception {
        ISchedulerClient client = clientInstance();
        Job job = defaultJob();
        JobId jobId = submitJob(job, client);
        // should return immediately
        client.waitForJob(jobId, TimeUnit.MINUTES.toMillis(3));
    }

    @Test(timeout = MAX_WAIT_TIME)
    public void testJobResult() throws Throwable {
        ISchedulerClient client = clientInstance();
        Job job = createJobManyTasks("JobResult",
                                     SimpleJob.class,
                                     ErrorTask.class,
                                     LogTask.class,
                                     VariableTask.class,
                                     MetadataTask.class);
        JobId jobId = submitJob(job, client);
        JobResult result = client.waitForJob(jobId, TimeUnit.MINUTES.toMillis(3));
        // job result
        Assert.assertNotNull(result.getJobId());
        Assert.assertNotNull(result.getJobInfo());
        Assert.assertEquals(JobStatus.FINISHED, result.getJobInfo().getStatus());

        // the following check cannot work because of the way the job id is created on the client side.
        //Assert.assertEquals(job.getName(), result.getName());
        Assert.assertTrue(result.hadException());
        Assert.assertEquals(1, result.getExceptionResults().size());

        // job info
        checkJobInfo(result.getJobInfo());
        checkJobInfo(client.getJobInfo(jobId.value()));

        JobState jobState = client.getJobState(jobId.value());
        JobStatus status = jobState.getStatus();
        Assert.assertFalse(status.isJobAlive());
        Assert.assertEquals(JobStatus.FINISHED, status);

        checkJobInfo(jobState.getJobInfo());

        TaskState errorTaskState = findTask(getTaskNameForClass(ErrorTask.class), jobState.getHMTasks());
        Assert.assertNotNull(errorTaskState);
        TaskState simpleTaskState = findTask(getTaskNameForClass(SimpleJob.class), jobState.getHMTasks());
        Assert.assertNotNull(simpleTaskState);
        Assert.assertEquals(TaskStatus.FAULTY, errorTaskState.getStatus());
        Assert.assertEquals(TaskStatus.FINISHED, simpleTaskState.getStatus());

        // task result simple
        TaskResult tResSimple = result.getResult(getTaskNameForClass(SimpleJob.class));
        Assert.assertNotNull(tResSimple.value());
        Assert.assertEquals(new StringWrapper(TEST_JOB), tResSimple.value());

        // task result with error
        TaskResult tResError = result.getResult(getTaskNameForClass(ErrorTask.class));
        Assert.assertNotNull(tResError);
        Assert.assertTrue(tResError.hadException());
        Assert.assertNotNull(tResError.getException());
        Assert.assertTrue(tResError.getException() instanceof TaskException);

        // task result with logs
        TaskResult tResLog = result.getResult(getTaskNameForClass(LogTask.class));
        Assert.assertNotNull(tResLog);
        Assert.assertNotNull(tResLog.getOutput());
        System.err.println(tResLog.getOutput().getStdoutLogs(false));
        Assert.assertTrue(tResLog.getOutput().getStdoutLogs(false).contains(LogTask.HELLO_WORLD));

        // task result with variables
        TaskResult tResVar = result.getResult(getTaskNameForClass(VariableTask.class));
        Assert.assertNotNull(tResVar.getPropagatedVariables());
        Assert.assertTrue(tResVar.getPropagatedVariables().containsKey(VariableTask.MYVAR));

        // task result with metadata
        TaskResult tMetaVar = result.getResult(getTaskNameForClass(MetadataTask.class));
        Assert.assertNotNull(tMetaVar.getMetadata());
        Assert.assertTrue(tMetaVar.getMetadata().containsKey(MetadataTask.MYVAR));

    }

    private TaskState findTask(String name, Map<TaskId, TaskState> hmTasks) {
        for (TaskId taskId : hmTasks.keySet()) {
            if (taskId.getReadableName().equals(name)) {
                return hmTasks.get(taskId);
            }
        }
        return null;
    }

    private void checkJobInfo(JobInfo jobInfo) {
        Assert.assertNotNull(jobInfo);
        Assert.assertEquals(5, jobInfo.getTotalNumberOfTasks());
        Assert.assertEquals(5, jobInfo.getNumberOfFinishedTasks());
        Assert.assertEquals(1, jobInfo.getNumberOfFaultyTasks());
    }

    @Test(timeout = MAX_WAIT_TIME)
    public void testSchedulerNodeClient() throws Throwable {
        ISchedulerClient client = clientInstance();
        Job job = nodeClientJob("/functionaltests/descriptors/scheduler_client_node.groovy",
                                "/functionaltests/descriptors/scheduler_client_node_fork.groovy");
        JobId jobId = submitJob(job, client);
        TaskResult tres = client.waitForTask(jobId.toString(), "NodeClientTask", TimeUnit.MINUTES.toMillis(5));
        System.out.println(tres.getOutput().getAllLogs(false));
        Assert.assertNotNull(tres);
        Assert.assertEquals("Hello NodeClientTask I'm HelloTask", tres.value());
    }

    @Test(timeout = MAX_WAIT_TIME)
    public void testDataSpaceNodeClientPushPull() throws Throwable {
        ISchedulerClient client = clientInstance();
        Job job = nodeClientJob("/functionaltests/descriptors/dataspace_client_node_push_pull.groovy",
                                "/functionaltests/descriptors/dataspace_client_node_fork.groovy");
        JobId jobId = submitJob(job, client);
        TaskResult tres = client.waitForTask(jobId.toString(), "NodeClientTask", TimeUnit.MINUTES.toMillis(5));
        System.out.println(tres.getOutput().getAllLogs(false));
        Assert.assertNotNull(tres);
        Assert.assertEquals("HelloWorld", tres.value());
    }

    @Test(timeout = MAX_WAIT_TIME)
    public void testDataSpaceNodeClientPushDelete() throws Throwable {
        ISchedulerClient client = clientInstance();
        Job job = nodeClientJob("/functionaltests/descriptors/dataspace_client_node_push_delete.groovy",
                                "/functionaltests/descriptors/dataspace_client_node_fork.groovy");
        JobId jobId = submitJob(job, client);
        TaskResult tres = client.waitForTask(jobId.toString(), "NodeClientTask", TimeUnit.MINUTES.toMillis(5));
        System.out.println(tres.getOutput().getAllLogs(false));
        Assert.assertNotNull(tres);
        Assert.assertEquals("OK", tres.value());
    }

    protected Job nodeClientJob(String groovyScript, String forkScript) throws Exception {

        URL scriptURL = SchedulerClientTest.class.getResource(groovyScript);
        URL forkScriptURL = SchedulerClientTest.class.getResource(forkScript);

        TaskFlowJob job = new TaskFlowJob();
        job.setName("NodeClientJob");
        ScriptTask task = new ScriptTask();
        task.setName("NodeClientTask");
        ForkEnvironment forkEnvironment = new ForkEnvironment();
        forkEnvironment.setEnvScript(new SimpleScript(IOUtils.toString(forkScriptURL.toURI()), "groovy"));
        task.setForkEnvironment(forkEnvironment);
        task.setScript(new TaskScript(new SimpleScript(IOUtils.toString(scriptURL.toURI()), "groovy")));
        job.addTask(task);
        return job;
    }

    @Test(timeout = MAX_WAIT_TIME, expected = TimeoutException.class)
    public void testWaitForNonTerminatingJob() throws Exception {
        ISchedulerClient client = clientInstance();
        Job job = pendingJob();
        JobId jobId = submitJob(job, client);
        try {
            client.waitForJob(jobId, TimeUnit.SECONDS.toMillis(10));
        } finally {
            // Once the TimeoutException has been thrown
            // kill the job to free the node
            client.killJob(jobId);
        }
    }

    @Test(timeout = MAX_WAIT_TIME)
    public void testPushPullDeleteEmptyFile() throws Exception {
        File emptyFile = File.createTempFile("emptyFile", ".tmp");
        ISchedulerClient client = clientInstance();
        // Push the empty file into the userspace
        client.pushFile("USERSPACE", "", emptyFile.getName(), emptyFile.getCanonicalPath());

        // Delete the local file
        Assert.assertTrue("Unable to delete the local file after push, maybe there are still some open streams?",
                          emptyFile.delete());

        // Pull it from the userspace to be sure that it was pushed
        client.pullFile("USERSPACE", "", emptyFile.getCanonicalPath());

        // Check the file was pulled
        Assert.assertTrue("Unable to pull the empty file, maybe the pull mechanism is broken?", emptyFile.exists());

        // Delete the local file
        Assert.assertTrue("Unable to delete the local file after pull, maybe there are still some open streams?",
                          emptyFile.delete());

        // Delete the file in the user space
        client.deleteFile("USERSPACE", "/" + emptyFile.getName()); //TODO: TEST THIS
        // LATER
    }

    @Test(timeout = MAX_WAIT_TIME * 2)
    public void testJobSubmissionEventListener() throws Exception {
        ISchedulerClient client = clientInstance();
        SchedulerEventListenerImpl listener = new SchedulerEventListenerImpl();
        client.addEventListener(listener, true, SchedulerEvent.JOB_SUBMITTED);
        Job job = defaultJob();
        JobId jobId = client.submit(job);
        JobState submittedJob = listener.getSubmittedJob();
        while (!submittedJob.getId().value().equals(jobId.value())) {
            submittedJob = listener.getSubmittedJob();
        }
        client.removeEventListener();

        client.waitForJob(jobId, TimeUnit.SECONDS.toMillis(120));
    }

    @Test(timeout = MAX_WAIT_TIME)
    public void testKillTask() throws Exception {
        ISchedulerClient client = clientInstance();
        Job job = createJob(NonTerminatingJob.class);
        SchedulerEventListenerImpl listener = new SchedulerEventListenerImpl();
        client.addEventListener(listener, true, SchedulerEvent.TASK_PENDING_TO_RUNNING);
        JobId jobId = submitJob(job, client);

        TaskInfo startedTask = listener.getStartedTask();
        while (!startedTask.getJobId().value().equals(jobId.value())) {
            startedTask = listener.getStartedTask();
        }

        client.killTask(jobId, getTaskNameForClass(NonTerminatingJob.class));

        client.removeEventListener();
        // should return immediately
        client.waitForJob(jobId, TimeUnit.MINUTES.toMillis(3));
    }

    @Test(timeout = MAX_WAIT_TIME)
    public void testPushFileWithNonAdminUserPwdShouldSucceed() throws Exception {
        File tmpFile = testFolder.newFile();
        Files.write("non_admin_user_push_file_contents".getBytes(), tmpFile);
        ISchedulerClient client = SchedulerClient.createInstance();
        client.init(new ConnectionInfo(getRestServerUrl(), getNonAdminLogin(), getNonAdminLoginPassword(), null, true));
        client.pushFile("USERSPACE", "/test_non_admin_user_push_file", "tmpfile.tmp", tmpFile.getAbsolutePath());
        String destDirPath = URI.create(client.getUserSpaceURIs().get(0)).getPath();
        File destFile = new File(destDirPath, "test_non_admin_user_push_file/tmpfile.tmp");
        assertTrue(Files.equal(tmpFile, destFile));
    }

    private ISchedulerClient clientInstance() throws Exception {
        ISchedulerClient client = SchedulerClient.createInstance();
        client.init(new ConnectionInfo(getRestServerUrl(), getLogin(), getPassword(), null, true));
        return client;
    }

    private JobId submitJob(Job job, ISchedulerClient client) throws Exception {
        return client.submit(job);
    }

    private static class SchedulerEventListenerImpl implements SchedulerEventListener {
        private Stack<JobState> jobStateStack = new Stack<>();

        private Stack<TaskInfo> taskStateStack = new Stack<>();

        @Override
        public void jobSubmittedEvent(JobState jobState) {
            System.out.println("JobSubmittedEvent()");
            synchronized (this) {

                jobStateStack.push(jobState);
                notifyAll();
            }
        }

        public JobState getSubmittedJob() {
            System.out.println("getSubmittedJob");
            synchronized (this) {
                if (jobStateStack.isEmpty()) {
                    System.out.println("Stack is empty");
                    try {
                        System.out.println("wait");
                        wait();
                    } catch (InterruptedException ie) {
                    }
                }
                return jobStateStack.pop();
            }
        }

        public TaskInfo getStartedTask() {
            System.out.println("getStartedTask");
            synchronized (this) {
                if (taskStateStack.isEmpty()) {
                    System.out.println("Stack is empty");
                    try {
                        System.out.println("wait");
                        wait();
                    } catch (InterruptedException ie) {
                    }
                }
                return taskStateStack.pop();
            }
        }

        @Override
        public void jobStateUpdatedEvent(NotificationData<JobInfo> arg0) {
        }

        @Override
        public void schedulerStateUpdatedEvent(SchedulerEvent arg0) {
        }

        @Override
        public void taskStateUpdatedEvent(NotificationData<TaskInfo> data) {
            System.out.println("taskStateUpdatedEvent() : " + data);
            synchronized (this) {
                taskStateStack.push(data.getData());
                notifyAll();
            }
        }

        @Override
        public void usersUpdatedEvent(NotificationData<UserIdentification> arg0) {
        }

        @Override
        public void jobUpdatedFullDataEvent(JobState job) {
        }
    }
}
