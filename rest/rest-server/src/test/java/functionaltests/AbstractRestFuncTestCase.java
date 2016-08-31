/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2015 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $PROACTIVE_INITIAL_DEV$
 */
package functionaltests;

import java.security.Policy;

import javax.ws.rs.core.MediaType;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.AfterClass;
import org.junit.Assert;
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
        assertEquals(STATUS_OK, getStatusCode(response));
    }

    protected void assertContentNotEmpty(HttpResponse response) throws Exception {
        String content = getContent(response);
        assertNotNull(content);
        assertTrue(content.length() != 0);
    }

    protected int getStatusCode(HttpResponse response) {
        return response.getStatusLine().getStatusCode();
    }

    protected String getContent(HttpResponse response) throws Exception {
        return EntityUtils.toString(response.getEntity());
    }

    protected HttpResponse executeUriRequest(HttpUriRequest request) throws Exception {
        return HttpClientBuilder.create().build().execute(request);
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

    protected String submitFinishedJob() throws Exception {
        Scheduler scheduler = getScheduler();
        Job job = defaultJob();
        JobId jobId = scheduler.submit(job);
        waitJobState(jobId, JobStatus.FINISHED, 120000);
        return jobId.value();
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

    protected Job createJob(Class<?> clazz) throws Exception {
        TaskFlowJob job = new TaskFlowJob();
        job.setName(clazz.getSimpleName());
        job.setPriority(JobPriority.NORMAL);
        job.setOnTaskError(OnTaskError.CANCEL_JOB);
        job.setDescription("Test " + clazz.getSimpleName());
        job.setMaxNumberOfExecution(1);

        JavaTask task = new JavaTask();
        task.setName(clazz.getSimpleName() + "Task");
        task.setExecutableClassName(clazz.getName());
        task.setMaxNumberOfExecution(1);
        task.setOnTaskError(OnTaskError.CANCEL_JOB);

        String classpath = RestFuncTUtils.getClassPath(clazz);
        ForkEnvironment forkEnvironment = new ForkEnvironment();
        forkEnvironment.addAdditionalClasspath(classpath);
        task.setForkEnvironment(forkEnvironment);

        job.addTask(task);

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
        HttpResponse response = HttpClientBuilder.create().build().execute(httpPost);
        String responseContent = EntityUtils.toString(response.getEntity());
        if (STATUS_OK != getStatusCode(response)) {
            throw new RuntimeException(String.format("Authentication error: %n%s", responseContent));
        } else {
            return responseContent;
        }
    }

    public static void init() throws Exception {
        try {
            System.out.println("Starting the Scheduler & REST server");
            RestFuncTHelper.startRestfulSchedulerWebapp();
        } catch (Exception e) {
            e.printStackTrace();
            RestFuncTHelper.stopRestfulSchedulerWebapp();
            throw e;
        }
    }

    @AfterClass
    public static void afterClass() {
        RestFuncTHelper.stopRestfulSchedulerWebapp();
    }

}
