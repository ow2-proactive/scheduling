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
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.anyMap;

import java.io.File;
import java.io.Serializable;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.objectweb.proactive.core.util.wrapper.StringWrapper;
import org.ow2.proactive.authentication.ConnectionInfo;
import org.ow2.proactive.authentication.UserData;
import org.ow2.proactive.db.SortOrder;
import org.ow2.proactive.db.SortParameter;
import org.ow2.proactive.resourcemanager.common.event.RMNodeEvent;
import org.ow2.proactive.scheduler.common.*;
import org.ow2.proactive.scheduler.common.exception.NotConnectedException;
import org.ow2.proactive.scheduler.common.exception.PermissionException;
import org.ow2.proactive.scheduler.common.exception.TaskAbortedException;
import org.ow2.proactive.scheduler.common.job.Job;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.JobStatus;
import org.ow2.proactive.scheduler.common.job.JobVariable;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.job.UserIdentification;
import org.ow2.proactive.scheduler.common.task.ForkEnvironment;
import org.ow2.proactive.scheduler.common.task.ScriptTask;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskInfo;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.TaskState;
import org.ow2.proactive.scheduler.common.task.TaskStatus;
import org.ow2.proactive.scheduler.job.ExternalEndpoint;
import org.ow2.proactive.scheduler.rest.ISchedulerClient;
import org.ow2.proactive.scheduler.rest.SchedulerClient;
import org.ow2.proactive.scheduler.task.exceptions.TaskException;
import org.ow2.proactive.scripting.SimpleScript;
import org.ow2.proactive.scripting.TaskScript;
import org.ow2.proactive.utils.ObjectByteConverter;

import com.google.common.io.Files;

import functionaltests.jobs.ErrorTask;
import functionaltests.jobs.JobResultTask;
import functionaltests.jobs.LogTask;
import functionaltests.jobs.MetadataTask;
import functionaltests.jobs.NonTerminatingJob;
import functionaltests.jobs.RawTask;
import functionaltests.jobs.SimpleJob;
import functionaltests.jobs.VariableTask;


public class SchedulerClientTest extends AbstractRestFuncTestCase {

    /**
     * Maximum wait time of 5 minutes
     */
    protected static final long MAX_WAIT_TIME = 5 * 60 * 1000;

    private static URL jobDescriptor = SchedulerClientTest.class.getResource("/functionaltests/descriptors/Job_get_generic_info.xml");

    private static URL jobDescriptorParentId = SchedulerClientTest.class.getResource("/functionaltests/descriptors/Job_get_parent_id.xml");

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
    public void testRMNodeClient() throws Throwable {
        ISchedulerClient client = clientInstance();
        Job job = nodeClientJob("/functionaltests/descriptors/rm_client_node.groovy", null, null);
        JobId jobId = submitJob(job, client);
        TaskResult tRes = client.waitForTask(jobId.toString(), "NodeClientTask", TimeUnit.MINUTES.toMillis(5));
        System.out.println(tRes.getOutput().getAllLogs(false));
        Assert.assertNotNull(tRes);
        Assert.assertTrue(((ArrayList) tRes.value()).get(0) instanceof RMNodeEvent);
    }

    @Test(timeout = MAX_WAIT_TIME)
    public void testAttachService() throws Throwable {
        ISchedulerClient client = clientInstance();
        Job job = nodeClientJob("/functionaltests/descriptors/register_service.groovy", null, null);
        JobId jobId = submitJob(job, client);
        client.waitForJob(jobId.toString(), TimeUnit.MINUTES.toMillis(5));
        JobInfo jobInfo = client.getJobInfo(jobId.toString());
        Assert.assertNotNull(jobInfo);
        Assert.assertNotNull(jobInfo.getAttachedServices());
        Assert.assertEquals(1, jobInfo.getAttachedServices().size());
        Assert.assertTrue(jobInfo.getAttachedServices().containsKey(12));
    }

    @Test(timeout = MAX_WAIT_TIME)
    public void testAddAndRemoveExternalEndpointUrl() throws Throwable {
        ISchedulerClient client = clientInstance();
        Job job = nodeClientJob("/functionaltests/descriptors/add_and_remove_external_endpoint_urls.groovy",
                                null,
                                null);
        JobId jobId = submitJob(job, client);
        client.waitForJob(jobId.toString(), TimeUnit.MINUTES.toMillis(5));
        JobInfo jobInfo = client.getJobInfo(jobId.toString());
        Assert.assertNotNull(jobInfo);
        Assert.assertNotNull(jobInfo.getExternalEndpointUrls());
        Assert.assertEquals(3, jobInfo.getExternalEndpointUrls().size());
        Assert.assertEquals(new ExternalEndpoint("ccc", "http://ccc.fr", "icon/ccc"),
                            jobInfo.getExternalEndpointUrls().get("ccc"));
        Assert.assertEquals(new ExternalEndpoint("bbb", "http://bbb.fr", "icon/bbb"),
                            jobInfo.getExternalEndpointUrls().get("bbb"));
        // the last endpoint has null icon on purpose (to check that it does not cause issues)
        Assert.assertEquals(new ExternalEndpoint("eee", "http://eee.fr", null),
                            jobInfo.getExternalEndpointUrls().get("eee"));
    }

    @Test(timeout = MAX_WAIT_TIME)
    public void testSignals() throws Throwable {
        ISchedulerClient client = clientInstance();
        Job job = nodeClientJob("/functionaltests/descriptors/register_service_with_signals.groovy", null, null);
        //submit job with 'ready signal' and add variables
        JobId jobId = submitJob(job, client);
        JobInfo jobInfo;
        //wait until 'ready_my_signal' signal is sent
        do {
            jobInfo = client.getJobInfo(jobId.toString());
            Assert.assertNotNull(jobInfo);
            Thread.sleep(1000);
        } while (!jobInfo.getSignals().contains("ready_my_signal"));
        Map<String, String> outpuVariables = new HashMap<>();
        outpuVariables.put("name", "15");
        // validate the job variables
        List<JobVariable> jobVariables = client.validateJobSignal(jobId.value(), "my_signal", outpuVariables);
        // add 'my_signal' signal
        Set<String> signals = client.addJobSignal(jobId.value(), "my_signal", outpuVariables);
        // wait until the job is finished
        client.waitForJob(jobId.toString(), TimeUnit.MINUTES.toMillis(5));
        Assert.assertFalse(signals.contains("ready_my_signal"));
        Assert.assertTrue(signals.contains("my_signal"));
        JobVariable nameJobVariable = jobVariables.stream()
                                                  .filter(jobVariable -> jobVariable.getName().equals("name"))
                                                  .findFirst()
                                                  .get();

        Assert.assertNotNull(nameJobVariable);
        Assert.assertEquals("15", nameJobVariable.getValue());
        Assert.assertEquals("PA:Integer", nameJobVariable.getModel());
        JobVariable secondJobVariable = jobVariables.stream()
                                                    .filter(jobVariable -> jobVariable.getName().equals("second"))
                                                    .findFirst()
                                                    .get();

        Assert.assertNotNull(secondJobVariable);
        Assert.assertEquals("15", secondJobVariable.getValue());
        Assert.assertEquals("PA:Integer", secondJobVariable.getModel());
    }

    @Test(timeout = MAX_WAIT_TIME)
    public void testDisconnect() throws Exception {
        ISchedulerClient client = clientInstance();
        client.disconnect();
        assertFalse(client.isConnected());
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
    public void getResultMap() throws Throwable {
        ISchedulerClient client = clientInstance();
        Job job = createJob(JobResultTask.class);
        JobId jobId = client.submit(job);
        final JobResult jobResult = client.waitForJob(jobId, TimeUnit.MINUTES.toMillis(2000));
        assertFalse(jobResult.getResultMap().isEmpty());
        Assert.assertEquals(jobResult.getResultMap().get("myvar"), "myvalue");
    }

    @Test(timeout = MAX_WAIT_TIME)
    public void testJobResult() throws Throwable {
        ISchedulerClient client = clientInstance();
        Job job = createJobManyTasks("JobResult",
                                     SimpleJob.class,
                                     ErrorTask.class,
                                     LogTask.class,
                                     VariableTask.class,
                                     MetadataTask.class,
                                     RawTask.class);
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
        assertFalse(status.isJobAlive());
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
        Assert.assertNotNull(tResSimple.getSerializedValue());
        Assert.assertEquals(new StringWrapper(TEST_JOB), tResSimple.value());
        Assert.assertEquals(new StringWrapper(TEST_JOB),
                            ObjectByteConverter.byteArrayToObject(tResSimple.getSerializedValue()));

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
        Map<String, Serializable> vars = tResVar.getVariables();
        System.out.println(vars);
        Assert.assertTrue(tResVar.getPropagatedVariables().containsKey(VariableTask.MYVAR));
        Assert.assertEquals("myvalue", vars.get(VariableTask.MYVAR));

        // task result with metadata
        TaskResult tMetaVar = result.getResult(getTaskNameForClass(MetadataTask.class));
        Assert.assertNotNull(tMetaVar.getMetadata());
        Assert.assertTrue(tMetaVar.getMetadata().containsKey(MetadataTask.MYVAR));

        // task result with raw result

        TaskResult tResRaw = result.getResult(getTaskNameForClass(RawTask.class));
        Assert.assertNotNull(tResRaw.value());
        Assert.assertNotNull(tResRaw.getSerializedValue());
        Assert.assertArrayEquals(TEST_JOB.getBytes(), (byte[]) tResRaw.value());
        Assert.assertArrayEquals(TEST_JOB.getBytes(), tResRaw.getSerializedValue());

    }

    private boolean removeAllJobs(ISchedulerClient client) throws NotConnectedException, PermissionException {
        JobFilterCriteria allJobsCriteria = new JobFilterCriteria(false,
                                                                  true,
                                                                  true,
                                                                  true,
                                                                  true,
                                                                  "",
                                                                  "",
                                                                  "",
                                                                  null,
                                                                  new Long(-1));
        List defaultSortOrder = Collections.singletonList(new SortParameter<>(JobSortParameter.ID, SortOrder.ASC));
        List<JobInfo> allJobInfos = client.getJobs(0, 1000, allJobsCriteria, defaultSortOrder).getList();
        List<JobId> allJobIds = allJobInfos.stream().map(x -> x.getJobId()).collect(Collectors.toList());

        return client.removeJobs(allJobIds);
    }

    @Test(timeout = MAX_WAIT_TIME)
    public void testGetJobs() throws Throwable {
        ISchedulerClient client = clientInstance();

        // Define jobs
        Job job1 = defaultJob();
        Job job2 = defaultJob();
        Job job3 = defaultJob();
        Job job4 = pendingJob();

        // Set job names
        job1.setName("myJobA");
        job2.setName("myJobB");
        job3.setName("myJobB");
        job4.setName("myJobC");

        // Set project names
        job1.setProjectName("myProjectA");
        job2.setProjectName("myProjectA");
        job3.setProjectName("myProjectB");
        job4.setProjectName("myProjectB");

        // Remove all existing jobs
        Assert.assertTrue(removeAllJobs(client));

        // Submit job2 job3 job4
        JobId job1Id = submitJob(job1, client);
        JobId job2Id = submitJob(job2, client);
        JobId job3Id = submitJob(job3, client);
        JobId job4Id = submitJob(job4, client);

        // Wait for job1, job2 and job3 to finish
        JobResult job1Result = client.waitForJob(job1Id, TimeUnit.MINUTES.toMillis(3));
        JobResult job2Result = client.waitForJob(job2Id, TimeUnit.MINUTES.toMillis(3));
        JobResult job3Result = client.waitForJob(job3Id, TimeUnit.MINUTES.toMillis(3));
        Assert.assertEquals(JobStatus.FINISHED, job1Result.getJobInfo().getStatus());
        Assert.assertEquals(JobStatus.FINISHED, job2Result.getJobInfo().getStatus());
        Assert.assertEquals(JobStatus.FINISHED, job3Result.getJobInfo().getStatus());

        // Test sorts
        JobFilterCriteria allJobsCriteria = new JobFilterCriteria(false,
                                                                  true,
                                                                  true,
                                                                  true,
                                                                  true,
                                                                  "",
                                                                  "",
                                                                  "",
                                                                  null,
                                                                  new Long(-1));
        SortParameter jobNameDescOrder = new SortParameter<>(JobSortParameter.NAME, SortOrder.DESC);
        SortParameter jobIdAscOrder = new SortParameter<>(JobSortParameter.ID, SortOrder.ASC);
        List<SortParameter<JobSortParameter>> sortParameterList1 = new ArrayList<>();
        sortParameterList1.add(jobNameDescOrder);
        sortParameterList1.add(jobIdAscOrder);
        List<JobInfo> jobsList1 = client.getJobs(0, 4, allJobsCriteria, sortParameterList1).getList();
        Assert.assertEquals(jobsList1.get(0).getJobId().value(), job4Id.value());
        Assert.assertEquals(jobsList1.get(1).getJobId().value(), job2Id.value());
        Assert.assertEquals(jobsList1.get(2).getJobId().value(), job3Id.value());
        Assert.assertEquals(jobsList1.get(3).getJobId().value(), job1Id.value());

        // Test range and limit
        List<JobInfo> jobsList2 = client.getJobs(2, 2, allJobsCriteria, Collections.singletonList(jobIdAscOrder))
                                        .getList();
        Assert.assertEquals(jobsList2.get(0).getJobId().value(), job3Id.value());
        Assert.assertEquals(jobsList2.get(1).getJobId().value(), job4Id.value());

        // Test filters
        JobFilterCriteria allJobsFinishedInMyProjectBCriteria = new JobFilterCriteria(false,
                                                                                      false,
                                                                                      false,
                                                                                      true,
                                                                                      false,
                                                                                      "",
                                                                                      "myProjectB",
                                                                                      "",
                                                                                      null,
                                                                                      new Long(-1));
        List<JobInfo> jobsList3 = client.getJobs(0,
                                                 4,
                                                 allJobsFinishedInMyProjectBCriteria,
                                                 Collections.singletonList(jobIdAscOrder))
                                        .getList();
        Assert.assertEquals(jobsList3.size(), 1);
        Assert.assertEquals(jobsList3.get(0).getJobId().value(), job3Id.value());

        client.killJob(job4Id);

        // Remove all existing jobs
        Assert.assertTrue(removeAllJobs(client));
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
        Assert.assertEquals(6, jobInfo.getTotalNumberOfTasks());
        Assert.assertEquals(6, jobInfo.getNumberOfFinishedTasks());
        Assert.assertEquals(1, jobInfo.getNumberOfFaultyTasks());
    }

    @Test(timeout = MAX_WAIT_TIME)
    public void testSchedulerNodeClient() throws Throwable {
        ISchedulerClient client = clientInstance();
        Job job = nodeClientJob("/functionaltests/descriptors/scheduler_client_node.groovy",
                                "/functionaltests/descriptors/scheduler_client_node_fork.groovy",
                                null);
        JobId jobId = submitJob(job, client);
        TaskResult tres = client.waitForTask(jobId.toString(), "NodeClientTask", TimeUnit.MINUTES.toMillis(5));
        System.out.println(tres.getOutput().getAllLogs(false));
        Assert.assertNotNull(tres);
        Assert.assertEquals("Hello NodeClientTask I'm HelloTask", tres.value());
    }

    @Test(timeout = MAX_WAIT_TIME)
    public void testSchedulerNodeClientDisconnect() throws Throwable {
        ISchedulerClient client = clientInstance();
        Job job = nodeClientJob("/functionaltests/descriptors/scheduler_client_node_disconnect.groovy", null, null);
        JobId jobId = submitJob(job, client);
        TaskResult tres = client.waitForTask(jobId.toString(), "NodeClientTask", TimeUnit.MINUTES.toMillis(5));
        System.out.println(tres.getOutput().getAllLogs(false));
        Assert.assertFalse(tres.hadException());
    }

    @Test(timeout = MAX_WAIT_TIME)
    public void testSchedulerNodeClientParentId() throws Throwable {
        ISchedulerClient client = clientInstance();
        // Submit a job with the generic informations map
        JobId jobId = client.submit(jobDescriptorParentId);
        TaskResult tres = client.waitForTask(jobId.toString(), "NodeClientTask", TimeUnit.MINUTES.toMillis(5));
        Assert.assertNotNull(tres);
        System.out.println(tres.getOutput().getAllLogs(false));
        Assert.assertFalse(tres.hadException());
        Assert.assertEquals(jobId.value(), tres.value());
    }

    @Test(timeout = MAX_WAIT_TIME)
    public void testDataspaceNodeClientDisconnect() throws Throwable {
        ISchedulerClient client = clientInstance();
        Job job = nodeClientJob("/functionaltests/descriptors/scheduler_client_node_space_disconnect.groovy",
                                null,
                                null);
        JobId jobId = submitJob(job, client);
        TaskResult tres = client.waitForTask(jobId.toString(), "NodeClientTask", TimeUnit.MINUTES.toMillis(5));
        System.out.println(tres.getOutput().getAllLogs(false));
        Assert.assertFalse(tres.hadException());
    }

    @Test(timeout = MAX_WAIT_TIME)
    public void testSchedulerNodeClientCleanScript() throws Throwable {
        ISchedulerClient client = clientInstance();

        client.putThirdPartyCredential("TEST_CREDS", "mypassword_${PA_JOB_ID}");

        Job job = nodeClientJob("/functionaltests/descriptors/scheduler_client_node.groovy",
                                "/functionaltests/descriptors/scheduler_client_node_fork.groovy",
                                "/functionaltests/descriptors/scheduler_client_node_cleaning.groovy");
        JobId jobId = submitJob(job, client);
        JobResult jres = client.waitForJob(jobId, TimeUnit.MINUTES.toMillis(5));
        Assert.assertNotNull(jres);

        // wait 10 seconds because it is possible clean script executes after job
        Thread.sleep(10000);

        String jobLog = client.getJobServerLogs("" + jobId);

        //assert schedulerapi.connect() worked
        Assert.assertThat(jobLog, CoreMatchers.containsString("SCHEDULERAPI_URI_LIST_NOT_NULL=true"));
        //assert userspaceapi.connect() worked
        Assert.assertThat(jobLog, CoreMatchers.containsString("USERSPACE_FILE_LIST_NOT_NULL=true"));
        //assert globalspaceapi.connect() worked
        Assert.assertThat(jobLog, CoreMatchers.containsString("GLOBALSPACE_FILE_LIST_NOT_NULL=true"));
        //assert globalspaceapi.connect() worked
        Assert.assertThat(jobLog, CoreMatchers.containsString("TEST_CREDS=mypassword_" + jobId.toString()));
    }

    @Test(timeout = MAX_WAIT_TIME)
    public void createThirdPartyCredentials() throws Throwable {
        ISchedulerClient client = clientInstance();
        client.putThirdPartyCredential("key/slash", "value/slash");
        Set<String> keySet1 = client.thirdPartyCredentialsKeySet();
        System.out.println("Server Third-Party Credentials : " + keySet1);
        Job job1 = createJob(JobResultTask.class);
        Assert.assertTrue("credentials should contain the key", keySet1.contains("key/slash"));
        client.removeThirdPartyCredential("key/slash");
        Set<String> keySet2 = client.thirdPartyCredentialsKeySet();
        System.out.println("Server Third-Party Credentials : " + keySet2);
        Job job2 = createJob(JobResultTask.class);
        Assert.assertFalse("credentials should not contain the key", keySet2.contains("key/slash"));

    }

    @Test(timeout = MAX_WAIT_TIME)
    public void testDataSpaceNodeClientPushPull() throws Throwable {
        ISchedulerClient client = clientInstance();
        Job job = nodeClientJob("/functionaltests/descriptors/dataspace_client_node_push_pull.groovy",
                                "/functionaltests/descriptors/dataspace_client_node_fork.groovy",
                                null);
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
                                "/functionaltests/descriptors/dataspace_client_node_fork.groovy",
                                null);
        JobId jobId = submitJob(job, client);
        TaskResult tres = client.waitForTask(jobId.toString(), "NodeClientTask", TimeUnit.MINUTES.toMillis(5));
        System.out.println(tres.getOutput().getAllLogs(false));
        Assert.assertNotNull(tres);
        Assert.assertEquals("OK", tres.value());
    }

    @Test
    public void testReSubmitJob() throws Exception {
        ISchedulerClient client = clientInstance();
        Job job = nodeClientJob("/functionaltests/descriptors/dataspace_client_node_push_delete.groovy",
                                "/functionaltests/descriptors/dataspace_client_node_fork.groovy",
                                null);
        JobId jobId = submitJob(job, client);
        JobId jobId1 = client.reSubmit(jobId, Collections.emptyMap(), Collections.emptyMap(), null);

        String jobContent = client.getJobContent(jobId).replaceAll("\\s+", "");
        String jobContent1 = client.getJobContent(jobId1).replaceAll("\\s+", "");

        assertEquals(jobContent, jobContent1);
    }

    @Test
    public void testReSubmitJobWithVars() throws Exception {
        ISchedulerClient client = clientInstance();
        Job job = nodeClientJob("/functionaltests/descriptors/dataspace_client_node_push_delete.groovy",
                                "/functionaltests/descriptors/dataspace_client_node_fork.groovy",
                                null);
        JobId jobId = submitJob(job, client);
        Map<String, String> vars = new HashMap<>();
        vars.put("myvar", "myvalue");
        JobId jobId1 = client.reSubmit(jobId, vars, Collections.emptyMap(), null);

        String jobContent1 = client.getJobContent(jobId1);

        assertTrue(jobContent1.contains("<variables>"));
        assertTrue(jobContent1.contains("myvar"));
        assertTrue(jobContent1.contains("myvalue"));
        assertFalse(jobContent1.contains("<genericInformation>"));
    }

    @Test
    public void testReSubmitJobWithInfo() throws Exception {
        ISchedulerClient client = clientInstance();
        Job job = nodeClientJob("/functionaltests/descriptors/dataspace_client_node_push_delete.groovy",
                                "/functionaltests/descriptors/dataspace_client_node_fork.groovy",
                                null);
        JobId jobId = submitJob(job, client);
        Map<String, String> infos = new HashMap<>();
        infos.put("myinfo", "myvalue");
        JobId jobId1 = client.reSubmit(jobId, Collections.emptyMap(), infos, null);

        String jobContent1 = client.getJobContent(jobId1);

        assertFalse(jobContent1.contains("<variables>"));
        assertTrue(jobContent1.contains("<genericInformation>"));
        assertTrue(jobContent1.contains("myinfo"));
        assertTrue(jobContent1.contains("myvalue"));
    }

    @Test
    public void testReSubmitJobReplaceVarValue() throws Exception {
        ISchedulerClient client = clientInstance();
        Job job = nodeClientJob("/functionaltests/descriptors/dataspace_client_node_push_delete.groovy",
                                "/functionaltests/descriptors/dataspace_client_node_fork.groovy",
                                null);

        job.getVariables().put("originalVar", new JobVariable("originalVar", "originalValue"));

        JobId jobId = submitJob(job, client);

        Map<String, String> vars = new HashMap<>();
        vars.put("originalVar", "newValue");

        JobId jobId1 = client.reSubmit(jobId, vars, Collections.emptyMap(), null);

        String jobContent1 = client.getJobContent(jobId1);

        assertTrue(jobContent1.contains("<variables>"));
        assertTrue(jobContent1.contains("originalVar"));
        assertTrue(jobContent1.contains("newValue"));
        assertFalse(jobContent1.contains("originalValue"));
        assertFalse(jobContent1.contains("<genericInformation>"));
    }

    @Test
    public void testReSubmitJobReplaceInfoValue() throws Exception {
        ISchedulerClient client = clientInstance();
        Job job = nodeClientJob("/functionaltests/descriptors/dataspace_client_node_push_delete.groovy",
                                "/functionaltests/descriptors/dataspace_client_node_fork.groovy",
                                null);

        Map<String, String> info = new HashMap<>();
        info.put("originalVar", "originalValue");
        job.setGenericInformation(info);

        JobId jobId = submitJob(job, client);

        Map<String, String> info1 = new HashMap<>();
        info1.put("originalVar", "newValue");

        JobId jobId1 = client.reSubmit(jobId, Collections.emptyMap(), info1, null);

        String jobContent1 = client.getJobContent(jobId1);

        assertTrue(jobContent1.contains("<genericInformation>"));
        assertTrue(jobContent1.contains("originalVar"));
        assertTrue(jobContent1.contains("newValue"));
        assertFalse(jobContent1.contains("originalValue"));
        assertFalse(jobContent1.contains("<variables>"));
    }

    @Test
    public void testReSubmitJobMergeVar() throws Exception {
        ISchedulerClient client = clientInstance();
        Job job = nodeClientJob("/functionaltests/descriptors/dataspace_client_node_push_delete.groovy",
                                "/functionaltests/descriptors/dataspace_client_node_fork.groovy",
                                null);

        job.getVariables().put("originalVar", new JobVariable("originalVar", "originalValue"));

        JobId jobId = submitJob(job, client);

        Map<String, String> vars = new HashMap<>();
        vars.put("newVar", "newValue");

        JobId jobId1 = client.reSubmit(jobId, vars, Collections.emptyMap(), null);

        String jobContent1 = client.getJobContent(jobId1);

        assertTrue(jobContent1.contains("<variables>"));
        assertTrue(jobContent1.contains("originalVar"));
        assertTrue(jobContent1.contains("originalValue"));
        assertTrue(jobContent1.contains("newVar"));
        assertTrue(jobContent1.contains("newValue"));
        assertFalse(jobContent1.contains("<genericInformation>"));
    }

    @Test
    public void testReSubmitJobMergeInfo() throws Exception {
        ISchedulerClient client = clientInstance();
        Job job = nodeClientJob("/functionaltests/descriptors/dataspace_client_node_push_delete.groovy",
                                "/functionaltests/descriptors/dataspace_client_node_fork.groovy",
                                null);

        Map<String, String> info = new HashMap<>();
        info.put("originalVar", "originalValue");
        job.setGenericInformation(info);

        JobId jobId = submitJob(job, client);

        Map<String, String> info1 = new HashMap<>();
        info1.put("newVar", "newValue");

        JobId jobId1 = client.reSubmit(jobId, Collections.emptyMap(), info1, null);

        String jobContent1 = client.getJobContent(jobId1);

        assertTrue(jobContent1.contains("<genericInformation>"));
        assertTrue(jobContent1.contains("originalVar"));
        assertTrue(jobContent1.contains("originalValue"));
        assertTrue(jobContent1.contains("newVar"));
        assertTrue(jobContent1.contains("newValue"));
        assertFalse(jobContent1.contains("<variables>"));
    }

    protected Job nodeClientJob(String groovyScript, String forkScript, String cleaningScript) throws Exception {

        URL scriptURL = SchedulerClientTest.class.getResource(groovyScript);

        TaskFlowJob job = new TaskFlowJob();
        job.setName("NodeClientJob");
        ScriptTask task = new ScriptTask();
        task.setName("NodeClientTask");
        if (forkScript != null) {
            ForkEnvironment forkEnvironment = new ForkEnvironment();
            forkEnvironment.setEnvScript(new SimpleScript(IOUtils.toString(SchedulerClientTest.class.getResource(forkScript)
                                                                                                    .toURI()),
                                                          "groovy"));
            task.setForkEnvironment(forkEnvironment);
        }
        task.setScript(new TaskScript(new SimpleScript(IOUtils.toString(scriptURL.toURI()), "groovy")));
        //add CleanScript to test external APIs
        if (cleaningScript != null) {
            task.setCleaningScript(new SimpleScript(IOUtils.toString(SchedulerClientTest.class.getResource(cleaningScript)
                                                                                              .toURI()),
                                                    "groovy"));
        }
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
        client.addEventListener(listener, true, SchedulerEvent.JOB_SUBMITTED, SchedulerEvent.USERS_UPDATE);
        Job job = defaultJob();
        Map<String, JobVariable> jobVariables = new LinkedHashMap<>();
        jobVariables.put("MY_VAR",
                         new JobVariable("MY_VAR",
                                         "MY_VALUE",
                                         "PA:NOT_EMPTY_STRING",
                                         "a test variable",
                                         "MY_GROUP",
                                         true,
                                         true));
        job.setVariables(jobVariables);
        JobId jobId = client.submit(job);
        JobState submittedJob = listener.getSubmittedJob();
        while (!submittedJob.getId().value().equals(jobId.value())) {
            submittedJob = listener.getSubmittedJob();
        }
        UserIdentification userIdentification = listener.getLastUserUpdate();
        assertTrue(userIdentification.getLastSubmitTime() != -1);
        JobInfo jobInfo = client.getJobInfo(jobId.toString());
        System.out.println("Variables = " + jobInfo.getVariables());
        Map<String, JobVariable> stateJobVariables = jobInfo.getDetailedVariables();
        assertEquals(jobVariables, stateJobVariables);
        client.removeEventListener();

        client.waitForJob(jobId, TimeUnit.SECONDS.toMillis(120));
    }

    @Test(timeout = MAX_WAIT_TIME * 2)
    public void testJobSubmissionWithGenericInfo() throws Throwable {
        ISchedulerClient client = clientInstance();

        // Create a generic infos map
        String jobSubmissionGenericInfoKey = "job_generic_info";
        String jobSubmissionGenericInfoValue = "!*job generic info value*!";
        Map<String, String> genericInfosMap = Collections.singletonMap(jobSubmissionGenericInfoKey,
                                                                       jobSubmissionGenericInfoValue);

        // Submit a job with the generic informations map
        JobId jobId = client.submit(jobDescriptor, anyMap(), genericInfosMap, anyMap());
        client.waitForJob(jobId, TimeUnit.SECONDS.toMillis(120));

        // The job generic info must be returned by the task
        TaskResult taskResult = client.getJobResult(jobId).getResult("task1");
        Assert.assertEquals(jobSubmissionGenericInfoValue, taskResult.value());
    }

    @Test(timeout = MAX_WAIT_TIME * 3)
    public void testChildrenCount() throws Throwable {
        ISchedulerClient client = clientInstance();

        // Submit a first job
        JobId jobIdParent = client.submit(jobDescriptor, anyMap(), anyMap(), anyMap());
        client.waitForJob(jobIdParent, TimeUnit.SECONDS.toMillis(120));

        // Create a generic info map
        String jobSubmissionGenericInfoKey = SchedulerConstants.PARENT_JOB_ID;
        String jobSubmissionGenericInfoValue = "" + jobIdParent.value();
        Map<String, String> genericInfosMap = Collections.singletonMap(jobSubmissionGenericInfoKey,
                                                                       jobSubmissionGenericInfoValue);
        List<JobId> childrenJobs = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            // Submit a job with the generic information map
            JobId jobId = client.submit(jobDescriptor, anyMap(), genericInfosMap, anyMap());
            childrenJobs.add(jobId);
            client.waitForJob(jobId, TimeUnit.SECONDS.toMillis(120));
            JobInfo childJobInfo = client.getJobInfo(jobId.value());
            Assert.assertEquals(new Long(jobIdParent.longValue()), childJobInfo.getParentId());
        }
        JobInfo parentJobInfo = client.getJobInfo(jobIdParent.value());
        Assert.assertEquals(3, parentJobInfo.getChildrenCount());

        // remove a job and check children count again
        client.removeJob(childrenJobs.remove(0));
        // As removeJob is asynchronous, we need to sleep a bit
        Thread.sleep(2000);
        parentJobInfo = client.getJobInfo(jobIdParent.value());
        Assert.assertEquals(2, parentJobInfo.getChildrenCount());

        // remove the remaining jobs and check children count again
        client.removeJobs(childrenJobs);
        // As removeJob is asynchronous, we need to sleep a bit
        Thread.sleep(2000);
        parentJobInfo = client.getJobInfo(jobIdParent.value());
        Assert.assertEquals(0, parentJobInfo.getChildrenCount());
    }

    @Test(timeout = MAX_WAIT_TIME * 2)
    public void testJobSubmissionWithGenericInfoResolvedWithVariable() throws Throwable {
        ISchedulerClient client = clientInstance();

        // The job submission generic info must be replaced by the job submission variable
        String jobSubmissionGenericInfoKey = "job_generic_info";
        String jobSubmissionGenericInfoValue = "${updated_with_job_variable}";
        Map<String, String> genericInfosMap = Collections.singletonMap(jobSubmissionGenericInfoKey,
                                                                       jobSubmissionGenericInfoValue);

        // Create a variables map
        String jobVariableKey = "updated_with_job_variable";
        String jobVariableValue = "!*variable value*!";
        Map<String, String> variablesMap = Collections.singletonMap(jobVariableKey, jobVariableValue);

        // Submit a job with the generic informations map and the variables map
        JobId jobId = client.submit(jobDescriptor, variablesMap, genericInfosMap, anyMap());
        client.waitForJob(jobId, TimeUnit.SECONDS.toMillis(120));

        // The job generic info must be returned by the task
        TaskResult taskResult = client.getJobResult(jobId).getResult("task1");
        Assert.assertEquals(jobVariableValue, taskResult.value());
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
        JobResult result = client.waitForJob(jobId, TimeUnit.MINUTES.toMillis(3));
        TaskResult tresult = result.getResult(getTaskName(NonTerminatingJob.class));
        Assert.assertTrue(tresult.hadException());
        Assert.assertTrue(tresult.getException() instanceof TaskAbortedException);
    }

    @Test(timeout = MAX_WAIT_TIME)
    public void testPushFileWithNonAdminUserPwdShouldSucceed() throws Exception {
        File tmpFile = testFolder.newFile();
        Files.write("non_admin_user_push_file_contents".getBytes(), tmpFile);
        ISchedulerClient client = SchedulerClient.createInstance();
        client.init(new ConnectionInfo(getRestServerUrl(),
                                       getNonAdminLogin(),
                                       null,
                                       getNonAdminLoginPassword(),
                                       null,
                                       true));
        client.pushFile("USERSPACE", "/test_non_admin_user_push_file", "tmpfile.tmp", tmpFile.getAbsolutePath());
        String destDirPath = URI.create(client.getUserSpaceURIs().get(0)).getPath();
        File destFile = new File(destDirPath, "test_non_admin_user_push_file/tmpfile.tmp");
        assertTrue(Files.equal(tmpFile, destFile));
    }

    @Test(timeout = MAX_WAIT_TIME)
    public void testGetGroups() throws Exception {
        ISchedulerClient client = SchedulerClient.createInstance();
        client.init(new ConnectionInfo(getRestServerUrl(), getLogin(), null, getPassword(), null, true));
        UserData userData = client.getCurrentUserData();
        Assert.assertNotNull(userData);
        Assert.assertNotNull(userData.getGroups());
        Assert.assertTrue(userData.getGroups().contains("scheduleradmins"));
        client.disconnect();

        client.init(new ConnectionInfo(getRestServerUrl(),
                                       getNonAdminLogin(),
                                       null,
                                       getNonAdminLoginPassword(),
                                       null,
                                       true));
        userData = client.getCurrentUserData();
        Assert.assertNotNull(userData);
        Assert.assertNotNull(userData.getGroups());
        Assert.assertTrue(userData.getGroups().contains("user"));
        client.disconnect();
    }

    protected ISchedulerClient clientInstance() throws Exception {
        ISchedulerClient client = SchedulerClient.createInstance();
        client.init(new ConnectionInfo(getRestServerUrl(), getLogin(), null, getPassword(), null, true));
        return client;
    }

    protected JobId submitJob(Job job, ISchedulerClient client) throws Exception {
        return client.submit(job);
    }

    private static class SchedulerEventListenerImpl implements SchedulerEventListener {
        private Stack<JobState> jobStateStack = new Stack<>();

        private Stack<TaskInfo> taskStateStack = new Stack<>();

        private Stack<UserIdentification> userIdentificationStack = new Stack<>();

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

        public UserIdentification getLastUserUpdate() {
            System.out.println("getLastUserUpdate");
            synchronized (this) {
                if (userIdentificationStack.isEmpty()) {
                    System.out.println("Stack is empty");
                    try {
                        System.out.println("wait");
                        wait();
                    } catch (InterruptedException ie) {
                    }
                }
                return userIdentificationStack.pop();
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
        public void usersUpdatedEvent(NotificationData<UserIdentification> data) {
            System.out.println("userIdentificationUpdatedEvent() : " + data);
            synchronized (this) {
                userIdentificationStack.push(data.getData());
                notifyAll();
            }
        }

        @Override
        public void jobUpdatedFullDataEvent(JobState job) {
        }
    }
}
