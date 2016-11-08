package org.ow2.proactive.scheduler.core;

import java.io.Serializable;
import java.security.KeyException;

import org.ow2.proactive.scheduler.common.NotificationData;
import org.ow2.proactive.scheduler.common.SchedulerEvent;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.JobStatus;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.executable.JavaExecutable;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.job.InternalJobFactory;
import org.ow2.proactive.scheduler.job.JobIdImpl;
import org.ow2.proactive.scheduler.job.JobInfoImpl;
import org.ow2.proactive.scheduler.util.SendMail;
import org.ow2.tests.ProActiveTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.contains;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;


public class JobEmailNotificationTest extends ProActiveTest {

    public static class TestJavaTask extends JavaExecutable {
        @Override
        public Serializable execute(TaskResult... results) throws Throwable {
            System.out.println("OK");
            return "OK";
        }
    }

    private static final String ADMIN_EMAIL = "admin@example.com";
    private static final String DEFAULT_USER_NAME = "admin";
    private static final String INCOMPLETE_EMAIL = "foo";
    private static final String JOB_NAME = "job name";
    private static final String MALFORMED_EMAIL = "$@";
    private static final String TASK_NAME = "task name";
    private static final String USER_EMAIL = "user@example.com";

    private static void disableEmailNotifications() {
        PASchedulerProperties.EMAIL_NOTIFICATIONS_ENABLED.updateProperty("false");
    }

    private static void enableEmailNotifications() {
        PASchedulerProperties.EMAIL_NOTIFICATIONS_ENABLED.updateProperty("true");
    }

    private static NotificationData<JobInfo> getNotification(JobState js, SchedulerEvent event) {
        JobInfo jobInfo = new JobInfoImpl((JobInfoImpl) js.getJobInfo());
        return new NotificationData<>(event, jobInfo);
    }

    private static void setSenderAddress(String address) {
        PASchedulerProperties.EMAIL_NOTIFICATIONS_SENDER_ADDRESS.updateProperty(address);
    }

    private SendMail sender;

    private SendMail stubbedSender;

    private InternalJob createJob(String userEmail) throws Exception {
        TaskFlowJob job = new TaskFlowJob();
        job.setName(JOB_NAME);
        if (userEmail != null) {
            job.addGenericInformation(JobEmailNotification.GENERIC_INFORMATION_KEY_EMAIL, userEmail);
        }
        JavaTask javaTask = new JavaTask();
        javaTask.setExecutableClassName(TestJavaTask.class.getName());
        javaTask.setName(TASK_NAME);
        job.addTask(javaTask);
        InternalJob internalJob = InternalJobFactory.createJob(job, null);
        internalJob.setOwner(DEFAULT_USER_NAME);
        return internalJob;
    }

    private static boolean sendNotification(JobState jobState, SchedulerEvent event, SendMail sender)
            throws JobEmailNotificationException {
        NotificationData<JobInfo> notification = getNotification(jobState, event);
        JobEmailNotification emailNotification = new JobEmailNotification(jobState, notification, sender);
        return emailNotification.doCheckAndSend();
    }

    @Before
    public void setUp() throws KeyException {
        enableEmailNotifications();
        setSenderAddress(ADMIN_EMAIL);
        stubbedSender = mock(SendMail.class);
        sender = new SendMail();
    }

    @Test(expected = JobEmailNotificationException.class)
    public void testIncompleteFrom() throws Exception {
        setSenderAddress(INCOMPLETE_EMAIL);
        InternalJob job = createJob(USER_EMAIL);

        try {

            sendNotification(job, SchedulerEvent.JOB_RUNNING_TO_FINISHED, sender);

        } catch (JobEmailNotificationException e) {
            Assert.assertThat("Wrong exception message", e.getMessage(),
                    containsString("Error sending email"));
            throw e;
        }
    }

    @Test(expected = JobEmailNotificationException.class)
    public void testIncompleteTo() throws Exception {
        InternalJob job = createJob(INCOMPLETE_EMAIL);

        try {

            sendNotification(job, SchedulerEvent.JOB_RUNNING_TO_FINISHED, sender);

        } catch (JobEmailNotificationException e) {
            Assert.assertThat("Wrong exception message", e.getMessage(),
                    containsString("Error sending email"));
            throw e;
        }
    }

    @Test(expected = JobEmailNotificationException.class)
    public void testMalformedFrom() throws Exception {
        setSenderAddress(MALFORMED_EMAIL);
        InternalJob job = createJob(USER_EMAIL);

        try {

            sendNotification(job, SchedulerEvent.JOB_RUNNING_TO_FINISHED, sender);

        } catch (JobEmailNotificationException e) {
            Assert.assertThat("Wrong exception message", e.getMessage(),
                    containsString("Malformed email address"));
            throw e;
        }
    }

    @Test(expected = JobEmailNotificationException.class)
    public void testMalformedTo() throws Exception {
        InternalJob job = createJob(MALFORMED_EMAIL);

        try {

            sendNotification(job, SchedulerEvent.JOB_RUNNING_TO_FINISHED, sender);

        } catch (JobEmailNotificationException e) {
            Assert.assertThat("Wrong exception message", e.getMessage(),
                    containsString("Malformed email address"));
            throw e;
        }
    }

    @Test(expected = JobEmailNotificationException.class)
    public void testNoTo() throws Exception {
        InternalJob job = createJob(null);

        try {

            sendNotification(job, SchedulerEvent.JOB_RUNNING_TO_FINISHED, sender);

        } catch (JobEmailNotificationException e) {
            Assert.assertThat("Wrong exception message", e.getMessage(),
                    containsString("Recipient address is not set in generic information"));
            throw e;
        }
    }

    @Test
    public void testDisabled() throws Exception {
        disableEmailNotifications();
        InternalJob job = createJob(USER_EMAIL);

        boolean sent = sendNotification(job, SchedulerEvent.JOB_RUNNING_TO_FINISHED, stubbedSender);

        assertFalse(sent);
        verifyNoMoreInteractions(stubbedSender);
    }

    @Test
    public void testWrongEvent() throws Exception {
        InternalJob job = createJob(USER_EMAIL);

        assertFalse(sendNotification(job, SchedulerEvent.FROZEN, stubbedSender));
        assertFalse(sendNotification(job, SchedulerEvent.RESUMED, stubbedSender));
        assertFalse(sendNotification(job, SchedulerEvent.SHUTDOWN, stubbedSender));
        assertFalse(sendNotification(job, SchedulerEvent.SHUTTING_DOWN, stubbedSender));
        assertFalse(sendNotification(job, SchedulerEvent.STARTED, stubbedSender));
        assertFalse(sendNotification(job, SchedulerEvent.STOPPED, stubbedSender));
        assertFalse(sendNotification(job, SchedulerEvent.KILLED, stubbedSender));
        assertFalse(sendNotification(job, SchedulerEvent.JOB_REMOVE_FINISHED, stubbedSender));
        assertFalse(sendNotification(job, SchedulerEvent.TASK_PENDING_TO_RUNNING, stubbedSender));
        assertFalse(sendNotification(job, SchedulerEvent.TASK_RUNNING_TO_FINISHED, stubbedSender));
        assertFalse(sendNotification(job, SchedulerEvent.TASK_WAITING_FOR_RESTART, stubbedSender));
        assertFalse(sendNotification(job, SchedulerEvent.PAUSED, stubbedSender));
        assertFalse(sendNotification(job, SchedulerEvent.RM_DOWN, stubbedSender));
        assertFalse(sendNotification(job, SchedulerEvent.RM_UP, stubbedSender));
        assertFalse(sendNotification(job, SchedulerEvent.USERS_UPDATE, stubbedSender));
        assertFalse(sendNotification(job, SchedulerEvent.POLICY_CHANGED, stubbedSender));
        assertFalse(sendNotification(job, SchedulerEvent.TASK_REPLICATED, stubbedSender));
        assertFalse(sendNotification(job, SchedulerEvent.TASK_SKIPPED, stubbedSender));
        assertFalse(sendNotification(job, SchedulerEvent.TASK_PROGRESS, stubbedSender));
        assertFalse(sendNotification(job, SchedulerEvent.DB_DOWN, stubbedSender));

        verifyNoMoreInteractions(stubbedSender);
    }

    @Test
    public void testSimple() throws Exception {
        InternalJob job = createJob(USER_EMAIL);

        boolean sent = sendNotification(job, SchedulerEvent.JOB_RUNNING_TO_FINISHED, stubbedSender);

        assertTrue(sent);
        verify(stubbedSender).send(eq(ADMIN_EMAIL), eq(USER_EMAIL), anyString(), anyString());
        verifyNoMoreInteractions(stubbedSender);
    }

    @Test
    public void testFinished() throws Exception {
        InternalJob job = createJob(USER_EMAIL);
        job.setId(new JobIdImpl(123890, job.getName()));
        job.setStatus(JobStatus.FINISHED);

        boolean sent = sendNotification(job, SchedulerEvent.JOB_RUNNING_TO_FINISHED, stubbedSender);

        assertTrue(sent);
        verify(stubbedSender).send(eq(ADMIN_EMAIL), eq(USER_EMAIL), contains("ProActive Job 123890 : Job running to finished"),
                contains("Status: Finished"));
        verifyNoMoreInteractions(stubbedSender);
    }

    @Test
    public void testKilled() throws Exception {
        InternalJob job = createJob(USER_EMAIL);
        job.setId(new JobIdImpl(123890, job.getName()));
        job.setStatus(JobStatus.KILLED);

        boolean sent = sendNotification(job, SchedulerEvent.JOB_RUNNING_TO_FINISHED, stubbedSender);

        assertTrue(sent);
        verify(stubbedSender).send(eq(ADMIN_EMAIL), eq(USER_EMAIL), contains("ProActive Job 123890 : Job running to finished"),
                contains("Status: Killed"));
        verifyNoMoreInteractions(stubbedSender);
    }

    @Test
    public void testPendingToFinished() throws Exception {
        InternalJob job = createJob(USER_EMAIL);
        job.setId(new JobIdImpl(123890, job.getName()));
        job.setStatus(JobStatus.FINISHED);

        boolean sent = sendNotification(job, SchedulerEvent.JOB_PENDING_TO_FINISHED, stubbedSender);

        assertTrue(sent);
        verify(stubbedSender).send(eq(ADMIN_EMAIL), eq(USER_EMAIL), contains("ProActive Job 123890 : Job pending to finished"),
                contains("Status: Finished"));
        verifyNoMoreInteractions(stubbedSender);
    }

}
