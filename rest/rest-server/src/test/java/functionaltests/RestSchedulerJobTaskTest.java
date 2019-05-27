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

import java.io.File;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.FileBody;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.SchedulerState;
import org.ow2.proactive.scheduler.common.exception.NotConnectedException;
import org.ow2.proactive.scheduler.common.exception.PermissionException;
import org.ow2.proactive.scheduler.common.exception.UnknownJobException;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.JobStatus;

import com.google.common.collect.Iterables;

import functionaltests.jobs.LogTask;
import functionaltests.jobs.LogTaskWithError;
import functionaltests.utils.RestFuncTUtils;


public class RestSchedulerJobTaskTest extends AbstractRestFuncTestCase {

    @BeforeClass
    public static void setUp() throws Exception {
        init();

        cleanScheduler();
    }

    private static void cleanScheduler() throws NotConnectedException, PermissionException, UnknownJobException {
        Scheduler scheduler = RestFuncTHelper.getScheduler();
        SchedulerState state = scheduler.getState();

        Iterable<JobState> jobs = Iterables.concat(state.getPendingJobs(),
                                                   state.getRunningJobs(),
                                                   state.getFinishedJobs());

        for (JobState jobState : jobs) {
            JobId jobId = jobState.getId();
            scheduler.killJob(jobId);
            scheduler.removeJob(jobId);
        }
    }

    @AfterClass
    public static void tearDown() {
        RestFuncTHelper.stopRestfulSchedulerWebapp();
    }

    @Test
    public void testLogin() throws Exception {
        RestFuncTestConfig config = RestFuncTestConfig.getInstance();
        String url = getResourceUrl("login");
        HttpPost httpPost = new HttpPost(url);
        StringEntity entity = new StringEntity("username=" + config.getLogin() + "&password=" + config.getPassword());
        entity.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        httpPost.setEntity(entity);
        HttpResponse response = executeUriRequest(httpPost);
        assertHttpStatusOK(response);
        assertContentNotEmpty(response);
    }

    @Test
    public void testLoginWithCredentials() throws Exception {
        RestFuncTestConfig config = RestFuncTestConfig.getInstance();
        Credentials credentials = RestFuncTUtils.createCredentials(config.getLogin(),
                                                                   config.getPassword(),
                                                                   RestFuncTHelper.getSchedulerPublicKey());
        String schedulerUrl = getResourceUrl("login");
        HttpPost httpPost = new HttpPost(schedulerUrl);
        MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create()
                                                                              .addPart("credential",
                                                                                       new ByteArrayBody(credentials.getBase64(),
                                                                                                         ContentType.APPLICATION_OCTET_STREAM,
                                                                                                         null));
        httpPost.setEntity(multipartEntityBuilder.build());
        HttpResponse response = executeUriRequest(httpPost);
        assertHttpStatusOK(response);
        String sessionId = assertContentNotEmpty(response);

        String currentUserUrl = getResourceUrl("logins/sessionid/" + sessionId);

        HttpGet httpGet = new HttpGet(currentUserUrl);
        response = executeUriRequest(httpGet);
        assertHttpStatusOK(response);
        String userName = assertContentNotEmpty(response);
        Assert.assertEquals(config.getLogin(), userName);
    }

    @Test
    public void testSubmit() throws Exception {
        String schedulerUrl = getResourceUrl("submit");
        HttpPost httpPost = new HttpPost(schedulerUrl);
        setSessionHeader(httpPost);
        File jobFile = RestFuncTHelper.getDefaultJobXmlfile();
        MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create()
                                                                              .addPart("file",
                                                                                       new FileBody(jobFile,
                                                                                                    ContentType.APPLICATION_XML));
        httpPost.setEntity(multipartEntityBuilder.build());
        HttpResponse response = executeUriRequest(httpPost);
        assertHttpStatusOK(response);
        JSONObject jsonObj = toJsonObject(response);
        assertNotNull(jsonObj.get("id").toString());
    }

    @Test
    public void testSubmitFromUrl() throws Exception {
        String schedulerUrl = getResourceUrl("jobs");
        HttpPost httpPost = new HttpPost(schedulerUrl);
        setSessionHeader(httpPost);
        File jobFile = RestFuncTHelper.getDefaultJobXmlfile();
        httpPost.setHeader("link", jobFile.toURI().toURL().toExternalForm());
        HttpResponse response = executeUriRequest(httpPost);
        assertHttpStatusOK(response);
        JSONObject jsonObj = toJsonObject(response);
        assertNotNull(jsonObj.get("id").toString());
    }

    @Test
    public void testUrlMatrixParamsShouldReplaceJobVariables() throws Exception {
        File jobFile = new File(RestSchedulerJobTaskTest.class.getResource("config/job_matrix_params.xml").toURI());

        String schedulerUrl = getResourceUrl("submit;var=matrix_param_val");
        HttpPost httpPost = new HttpPost(schedulerUrl);
        setSessionHeader(httpPost);

        MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create()
                                                                              .addPart("file",
                                                                                       new FileBody(jobFile,
                                                                                                    ContentType.APPLICATION_XML));
        httpPost.setEntity(multipartEntityBuilder.build());

        HttpResponse response = executeUriRequest(httpPost);
        assertHttpStatusOK(response);
        JSONObject jsonObj = toJsonObject(response);
        final String jobId = jsonObj.get("id").toString();
        assertNotNull(jobId);

        waitJobState(jobId, JobStatus.FINISHED, TimeUnit.MINUTES.toMillis(1));
    }

    @Test
    public void testListJobs() throws Exception {
        String jobId = submitDefaultJob().value();
        String schedulerUrl = getResourceUrl("jobs/" + jobId);
        HttpGet httpGet = new HttpGet(schedulerUrl);
        setSessionHeader(httpGet);
        HttpResponse response = executeUriRequest(httpGet);
        assertHttpStatusOK(response);
        JSONObject jsonObject = toJsonObject(response);
        assertJobId(jobId, jsonObject);
    }

    @Test
    public void testJobResult() throws Exception {
        String jobId = submitFinishedJob();
        String resource = "jobs/" + jobId + "/result";
        String schedulerUrl = getResourceUrl(resource);
        HttpGet httpGet = new HttpGet(schedulerUrl);
        setSessionHeader(httpGet);
        HttpResponse response = executeUriRequest(httpGet);
        assertHttpStatusOK(response);
        JSONObject jsonObject = toJsonObject(response);
        String taskResult = getTaskResult(jsonObject, getDefaultTaskName());
        assertNotNull(taskResult);
    }

    @Test
    public void testGetNoJob() throws Exception {
        cleanScheduler();
        String resourceUrl = getResourceUrl("jobs");
        HttpGet httpGet = new HttpGet(resourceUrl);
        setSessionHeader(httpGet);
        HttpResponse response = executeUriRequest(httpGet);
        assertHttpStatusOK(response);
        JSONObject jsonObject = toJsonObject(response);
        JSONArray jsonArray = (JSONArray) jsonObject.get("list");
        assertTrue(jsonArray.isEmpty());
    }

    @Test
    public void testJobResultValue() throws Exception {
        String jobId = submitFinishedJob();
        String resource = getResourceUrl("jobs/" + jobId + "/result/value");
        HttpGet httpGet = new HttpGet(resource);
        setSessionHeader(httpGet);
        HttpResponse response = executeUriRequest(httpGet);
        assertHttpStatusOK(response);
        JSONObject jsonObject = toJsonObject(response);
        assertEquals("TEST-JOB", jsonObject.get(getDefaultTaskName()).toString());
    }

    @Test(expected = UnknownJobException.class)
    public void testRemoveJob() throws Exception {
        String jobId = submitDefaultJob().value();
        String resource = "jobs/" + jobId;
        String schedulerUrl = getResourceUrl(resource);
        HttpDelete delete = new HttpDelete(schedulerUrl);
        setSessionHeader(delete);
        HttpResponse response = executeUriRequest(delete);
        assertHttpStatusOK(response);
        assertTrue(Boolean.valueOf(getContent(response)));
        RestFuncTHelper.getScheduler().getJobState(jobId);
        fail("UnknownJobException should be thrown");
    }

    @Test
    public void testKillJob() throws Exception {
        String jobId = submitPendingJobId();
        String resource = "jobs/" + jobId + "/kill";
        String schedulerUrl = getResourceUrl(resource);
        HttpPut httpPut = new HttpPut(schedulerUrl);
        setSessionHeader(httpPut);
        HttpResponse response = executeUriRequest(httpPut);
        assertHttpStatusOK(response);
        JobState jobState = getScheduler().getJobState(jobId);
        assertEquals(JobStatus.KILLED, jobState.getStatus());
    }

    @Test
    public void testGetJobTaskIds() throws Exception {
        String jobId = submitDefaultJob().value();
        String resource = "jobs/" + jobId + "/tasks";
        String schedulerUrl = getResourceUrl(resource);
        HttpGet httpGet = new HttpGet(schedulerUrl);
        setSessionHeader(httpGet);
        HttpResponse response = executeUriRequest(httpGet);
        assertHttpStatusOK(response);
        JSONObject jsonObject = toJsonObject(response);
        JSONArray jsonArray = (JSONArray) jsonObject.get("list");
        assertEquals(getDefaultTaskName(), jsonArray.get(0).toString());
    }

    @Test
    public void testJobTaskStates() throws Exception {
        String jobId = submitDefaultJob().value();
        String resource = "jobs/" + jobId + "/taskstates";
        String schedulerUrl = getResourceUrl(resource);
        HttpGet httpGet = new HttpGet(schedulerUrl);
        setSessionHeader(httpGet);
        HttpResponse response = executeUriRequest(httpGet);
        assertHttpStatusOK(response);
        JSONObject jsonObject = toJsonObject(response);
        JSONArray jsonArray = (JSONArray) jsonObject.get("list");
        assertTrue(jsonArray.size() > 0);
    }

    @Test
    public void testJobAndTaskLogs() throws Exception {
        String jobId = submitFinishedJob(LogTask.class, 1);
        String logs = getResourceAsString("jobs/" + jobId + "/result/log/all");
        System.out.println("job testJobAndTaskLogs all logs:");
        System.out.println(logs);
        Assert.assertTrue("Logs should contain stdout of the task", logs.contains(LogTask.HELLO_WORLD));
        Assert.assertTrue("Logs should contain stderr of the task", logs.contains(LogTask.ERROR_MESSAGE));

        logs = getResourceAsString("jobs/" + jobId + "/tasks/" + getTaskNameForClass(LogTask.class) +
                                   "/result/log/all");
        System.out.println("task testJobAndTaskLogs all logs:");
        System.out.println(logs);
        Assert.assertTrue("Logs should contain stdout of the task", logs.contains(LogTask.HELLO_WORLD));
        Assert.assertTrue("Logs should contain stderr of the task", logs.contains(LogTask.ERROR_MESSAGE));

        logs = getResourceAsString("jobs/" + jobId + "/tasks/" + getTaskNameForClass(LogTask.class) +
                                   "/result/log/out");
        System.out.println("task testJobAndTaskLogs out logs:");
        System.out.println(logs);
        Assert.assertTrue("Logs should contain stdout of the task", logs.contains(LogTask.HELLO_WORLD));
        Assert.assertFalse("Logs should not contain stderr of the task", logs.contains(LogTask.ERROR_MESSAGE));

        logs = getResourceAsString("jobs/" + jobId + "/tasks/" + getTaskNameForClass(LogTask.class) +
                                   "/result/log/err");
        System.out.println("task testJobAndTaskLogs err logs:");
        System.out.println(logs);
        Assert.assertFalse("Logs should not contain stdout of the task", logs.contains(LogTask.HELLO_WORLD));
        Assert.assertTrue("Logs should contain stderr of the task", logs.contains(LogTask.ERROR_MESSAGE));
    }

    private String getResourceAsString(String resourcePath) throws Exception {
        String resource = getResourceUrl(resourcePath);
        HttpGet httpGet = new HttpGet(resource);
        setSessionHeader(httpGet);
        HttpResponse response = executeUriRequest(httpGet);
        assertHttpStatusOK(response);
        return getContent(response);
    }

    @Test
    public void testJobLogsWithError() throws Exception {
        int executionAttempts = 2;
        String jobId = submitFinishedJob(LogTaskWithError.class, executionAttempts);
        String logs = getResourceAsString("jobs/" + jobId + "/result/log/all");
        System.out.println("job testJobLogsWithError all logs:");
        System.out.println(logs);
        Assert.assertTrue("Logs should contain stdout of the task", logs.contains(LogTask.HELLO_WORLD));
        Assert.assertTrue("Logs should contain stderr of the task", logs.contains(LogTask.ERROR_MESSAGE));

        Assert.assertEquals("Job standard logs should contain " + 1 + " occurrence.",
                            1,
                            StringUtils.countMatches(logs, LogTask.HELLO_WORLD));
        Assert.assertEquals("Job standard logs should contain " + 1 + " occurrence.",
                            1,
                            StringUtils.countMatches(logs, LogTask.ERROR_MESSAGE));

        logs = getResourceAsString("jobs/" + jobId + "/log/full");
        System.out.println("job testJobLogsWithError full logs:");
        System.out.println(logs);
        Assert.assertTrue("Logs should contain output of the task", logs.contains(LogTask.HELLO_WORLD));
        Assert.assertTrue("Logs should contain stderr of the task", logs.contains(LogTask.ERROR_MESSAGE));

        Assert.assertEquals("Job full logs should contain " + executionAttempts + " occurrence.",
                            executionAttempts,
                            StringUtils.countMatches(logs, LogTask.HELLO_WORLD));
        Assert.assertEquals("Job full logs should contain " + executionAttempts + " occurrence.",
                            executionAttempts,
                            StringUtils.countMatches(logs, LogTask.ERROR_MESSAGE));

        logs = getResourceAsString("jobs/" + jobId + "/tasks/" + getTaskNameForClass(LogTaskWithError.class) +
                                   "/result/log/all");
        System.out.println("task testJobLogsWithError all logs:");
        System.out.println(logs);
        Assert.assertEquals("Task logs should contain " + executionAttempts + " occurrence.",
                            executionAttempts,
                            StringUtils.countMatches(logs, LogTask.HELLO_WORLD));
        Assert.assertEquals("Task logs should contain " + executionAttempts + " occurrence.",
                            executionAttempts,
                            StringUtils.countMatches(logs, LogTask.ERROR_MESSAGE));

        logs = getResourceAsString("jobs/" + jobId + "/tasks/" + getTaskNameForClass(LogTaskWithError.class) +
                                   "/result/log/out");
        System.out.println("task testJobLogsWithError out logs:");
        System.out.println(logs);
        Assert.assertEquals("Task stdout logs should contain " + executionAttempts + " occurrence.",
                            executionAttempts,
                            StringUtils.countMatches(logs, LogTask.HELLO_WORLD));
        Assert.assertEquals("Task stdout logs should contain " + 0 + " occurrence.",
                            0,
                            StringUtils.countMatches(logs, LogTask.ERROR_MESSAGE));

        logs = getResourceAsString("jobs/" + jobId + "/tasks/" + getTaskNameForClass(LogTaskWithError.class) +
                                   "/result/log/err");
        System.out.println("task testJobLogsWithError err logs:");
        System.out.println(logs);
        Assert.assertEquals("Task stderr logs should contain " + 0 + " occurrence.",
                            0,
                            StringUtils.countMatches(logs, LogTask.HELLO_WORLD));
        Assert.assertEquals("Task stderr logs should contain " + executionAttempts + " occurrence.",
                            executionAttempts,
                            StringUtils.countMatches(logs, LogTask.ERROR_MESSAGE));
    }

    private void assertJobId(String expected, JSONObject job) {
        JSONObject jobInfo = (JSONObject) job.get("jobInfo");
        JSONObject jobId = (JSONObject) jobInfo.get("jobId");
        String actual = jobId.get("id").toString();
        assertEquals(expected, actual);
    }

    private String getTaskResult(JSONObject job, String taskName) {
        Object allResults = job.get("allResults");
        Object result = ((JSONObject) allResults).get(taskName);
        return ((JSONObject) result).get("serializedValue").toString();
    }

}
