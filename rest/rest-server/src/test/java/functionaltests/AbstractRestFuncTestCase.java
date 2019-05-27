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

import java.nio.charset.Charset;
import java.security.Policy;

import javax.ws.rs.core.MediaType;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.AfterClass;
import org.junit.Assert;
import org.ow2.proactive.http.HttpClientBuilder;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.job.Job;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobPriority;
import org.ow2.proactive.scheduler.common.job.JobStatus;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.ForkEnvironment;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.OnTaskError;

import functionaltests.jobs.JobResultTask;
import functionaltests.jobs.NonTerminatingJob;
import functionaltests.jobs.SimpleJob;
import functionaltests.utils.RestFuncTUtils;


public abstract class AbstractRestFuncTestCase {

    static {
        configureSecurityManager();
        configureLog4j();
    }

    static Logger logger;

    private static void configureLog4j() {
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.INFO);
        logger = Logger.getLogger(AbstractRestFuncTestCase.class);
    }

    private static void configureSecurityManager() {
        if (System.getProperty("java.security.policy") == null) {
            System.setProperty("java.security.policy",
                               System.getProperty(PAResourceManagerProperties.RM_HOME.getKey()) +
                                                       "/config/security.java.policy-server");

            Policy.getPolicy().refresh();
        }
    }

    private static final int STATUS_OK = 200;

    private volatile String session;

    protected void setSessionHeader(HttpUriRequest request) throws Exception {
        if (session == null) {
            synchronized (AbstractRestFuncTestCase.class) {
                if (session == null) {
                    session = getSession(RestFuncTHelper.getRestfulSchedulerUrl(), getLogin(), getPassword());
                }
            }

        }
        request.setHeader("sessionid", session);
    }

    protected JSONObject toJsonObject(HttpResponse response) throws Exception {
        String content = getContent(response);
        JSONParser parser = new JSONParser();
        return (JSONObject) parser.parse(content);
    }

    protected JSONArray toJsonArray(HttpResponse response) throws Exception {
        String content = getContent(response);
        JSONParser parser = new JSONParser();
        return (JSONArray) parser.parse(content);
    }

    protected Scheduler getScheduler() {
        return RestFuncTHelper.getScheduler();
    }

    protected void assertHttpStatusOK(HttpResponse response) {
        if (getStatusCode(response) != STATUS_OK) {
            try {
                System.out.println("Unexpected status response");
                System.out.println(IOUtils.toString(response.getEntity().getContent(), Charset.defaultCharset()));
            } catch (Exception e) {
                //ignored
            }
        }
        assertEquals(STATUS_OK, getStatusCode(response));
    }

    protected String assertContentNotEmpty(HttpResponse response) throws Exception {
        String content = getContent(response);
        assertNotNull(content);
        assertTrue(content.length() != 0);
        return content;
    }

    protected int getStatusCode(HttpResponse response) {
        return response.getStatusLine().getStatusCode();
    }

    protected String getContent(HttpResponse response) throws Exception {
        return EntityUtils.toString(response.getEntity());
    }

    protected HttpResponse executeUriRequest(HttpUriRequest request) throws Exception {
        return new HttpClientBuilder().insecure(true).useSystemProperties().build().execute(request);
    }

    protected void assertEquals(int expected, int actual) {
        Assert.assertEquals(expected, actual);
    }

    protected void assertEquals(long expected, long actual) {
        Assert.assertEquals(expected, actual);
    }

    protected void assertEquals(Object expected, Object actual) {
        Assert.assertEquals(expected, actual);
    }

    protected void assertNotNull(Object actual) {
        Assert.assertNotNull(actual);
    }

    protected void assertTrue(boolean condition) {
        Assert.assertTrue(condition);
    }

    protected String trim(String content) {
        return content.replaceAll("^\"|\"$", "");
    }

    protected void fail(String message) {
        Assert.fail(message);
    }

    protected String getResourceUrl(String resource) {
        return RestFuncTHelper.getResourceUrl(resource);
    }

    protected String getLogin() throws Exception {
        return RestFuncTestConfig.getInstance().getLogin();
    }

    protected String getNonAdminLogin() throws Exception {
        return RestFuncTestConfig.getInstance().getNonAdminLogin();
    }

    protected String getPassword() throws Exception {
        return RestFuncTestConfig.getInstance().getPassword();
    }

    protected String getNonAdminLoginPassword() throws Exception {
        return RestFuncTestConfig.getInstance().getNonAdminLonginPassword();
    }

    protected JobId submitDefaultJob() throws Exception {
        Job job = defaultJob();
        return getScheduler().submit(job);
    }

    protected JobId submitJobWithResults() throws Exception {
        Job job = createJob(JobResultTask.class);
        return getScheduler().submit(job);
    }

    protected String submitFinishedJob(Class<?> clazz, int executionAttempts) throws Exception {
        Scheduler scheduler = getScheduler();
        Job job = createJob(clazz, executionAttempts);
        JobId jobId = scheduler.submit(job);
        waitJobState(jobId, JobStatus.FINISHED, 120000);
        return jobId.value();
    }

    protected String submitFinishedJob() throws Exception {
        return submitFinishedJob(SimpleJob.class, 1);
    }

    public void waitJobState(JobId jobId, JobStatus expected, long timeout) throws Exception {
        waitJobState(jobId.value(), expected, timeout);
    }

    public void waitJobState(String jobId, JobStatus expected, long timeout) throws Exception {
        long stopTime = System.currentTimeMillis() + timeout;

        while (System.currentTimeMillis() < stopTime) {
            JobStatus currentStatus = getScheduler().getJobState(jobId).getStatus();
            if (currentStatus.equals(expected)) {
                return;
            } else if (!currentStatus.isJobAlive()) {
                break;
            } else {
                Thread.sleep(300);
            }
        }

        Assert.fail("Failed to wait when " + jobId + " is " + expected + ", current status is " +
                    getScheduler().getJobState(jobId).getStatus());
    }

    protected String submitPendingJobId() throws Exception {
        Job job = pendingJob();
        JobId jobId = getScheduler().submit(job);
        return jobId.value();
    }

    protected Job defaultJob() throws Exception {
        return createJob(SimpleJob.class);
    }

    protected String getDefaultTaskName() {
        return getTaskName(SimpleJob.class);
    }

    protected Job pendingJob() throws Exception {
        return createJob(NonTerminatingJob.class);
    }

    protected String getTaskNameForClass(Class<?> clazz) {
        return clazz.getSimpleName() + "Task";
    }

    protected Job createJob(Class<?> clazz) throws Exception {
        return createJob(clazz, 1);
    }

    protected Job createJob(Class<?> clazz, int executionAttempts) throws Exception {
        OnTaskError errorPolicy = (executionAttempts == 1 ? OnTaskError.CANCEL_JOB
                                                          : OnTaskError.CONTINUE_JOB_EXECUTION);
        TaskFlowJob job = new TaskFlowJob();
        job.setName(clazz.getSimpleName());
        job.setPriority(JobPriority.NORMAL);
        job.setOnTaskError(errorPolicy);
        job.setDescription("Test " + clazz.getSimpleName());
        job.setMaxNumberOfExecution(executionAttempts);

        JavaTask task = new JavaTask();
        task.setName(getTaskNameForClass(clazz));
        task.setExecutableClassName(clazz.getName());
        task.setMaxNumberOfExecution(executionAttempts);
        task.setOnTaskError(errorPolicy);
        task.setPreciousResult(true);

        String classpath = RestFuncTUtils.getClassPath(clazz);
        ForkEnvironment forkEnvironment = new ForkEnvironment();
        forkEnvironment.addAdditionalClasspath(classpath);
        task.setForkEnvironment(forkEnvironment);

        job.addTask(task);

        return job;
    }

    protected Job createJobManyTasks(String jobName, Class<?>... clazzes) throws Exception {
        TaskFlowJob job = new TaskFlowJob();
        job.setName(jobName);
        job.setPriority(JobPriority.NORMAL);
        job.setDescription("Test " + jobName);
        job.setMaxNumberOfExecution(1);

        for (Class<?> clazz : clazzes) {
            JavaTask task = new JavaTask();
            task.setName(clazz.getSimpleName() + "Task");
            task.setExecutableClassName(clazz.getName());
            task.setMaxNumberOfExecution(1);
            String classpath = RestFuncTUtils.getClassPath(clazz);
            ForkEnvironment forkEnvironment = new ForkEnvironment();
            forkEnvironment.addAdditionalClasspath(classpath);
            task.setForkEnvironment(forkEnvironment);
            job.addTask(task);
        }
        return job;
    }

    protected String getTaskName(Class<?> clazz) {
        return clazz.getSimpleName() + "Task";
    }

    private String getSession(String schedulerUrl, String username, String password) throws Exception {
        String resourceUrl = getResourceUrl("login");
        HttpPost httpPost = new HttpPost(resourceUrl);
        StringBuilder buffer = new StringBuilder();
        buffer.append("username=").append(username).append("&password=").append(password);
        StringEntity entity = new StringEntity(buffer.toString());
        entity.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        httpPost.setEntity(entity);
        HttpResponse response = new HttpClientBuilder().insecure(true).useSystemProperties().build().execute(httpPost);
        String responseContent = EntityUtils.toString(response.getEntity());
        if (STATUS_OK != getStatusCode(response)) {
            throw new RuntimeException(String.format("Authentication error: %n%s", responseContent));
        } else {
            return responseContent;
        }
    }

    public static void init(int nbNodes) throws Exception {
        try {
            System.out.println("Starting the Scheduler & REST server");
            RestFuncTHelper.startRestfulSchedulerWebapp(nbNodes);
        } catch (Exception e) {
            e.printStackTrace();
            RestFuncTHelper.stopRestfulSchedulerWebapp();
            throw e;
        }
    }

    public static void init() throws Exception {
        init(RestFuncTHelper.DEFAULT_NUMBER_OF_NODES);
    }

    @AfterClass
    public static void afterClass() {
        RestFuncTHelper.stopRestfulSchedulerWebapp();
    }

}
