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

import com.google.common.io.Files;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.ow2.proactive.scheduler.common.NotificationData;
import org.ow2.proactive.scheduler.common.SchedulerEvent;
import org.ow2.proactive.scheduler.common.SchedulerEventListener;
import org.ow2.proactive.scheduler.common.SchedulerStatus;
import org.ow2.proactive.scheduler.common.job.*;
import org.ow2.proactive.scheduler.common.task.TaskInfo;
import org.ow2.proactive.authentication.ConnectionInfo;
import org.ow2.proactive.scheduler.rest.ISchedulerClient;
import org.ow2.proactive.scheduler.rest.SchedulerClient;

import java.io.File;
import java.net.URI;
import java.util.Stack;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static functionaltests.RestFuncTHelper.getRestServerUrl;

public class SchedulerClientTest extends AbstractRestFuncTestCase {

    /** Maximum wait time of 5 minutes */
    private static final long MAX_WAIT_TIME = 5 * 60 * 1000;

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

    @BeforeClass
    public static void beforeClass() throws Exception {
        init();
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
        Assert.assertTrue(
                "Unable to delete the local file after push, maybe there are still some open streams?",
                emptyFile.delete());

        // Pull it from the userspace to be sure that it was pushed
        client.pullFile("USERSPACE", "", emptyFile.getCanonicalPath());

        // Check the file was pulled
        Assert.assertTrue("Unable to pull the empty file, maybe the pull mechanism is broken?",
                emptyFile.exists());

        // Delete the local file
        Assert.assertTrue(
                "Unable to delete the local file after pull, maybe there are still some open streams?",
                emptyFile.delete());

        // Delete the file in the user space
        // client.deleteFile("USERSPACE", emptyFile.getName()); TODO: TEST THIS
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
    }
    
    @Test
    public void testGetJobContent() throws Exception {
    	ISchedulerClient client = clientInstance();
    	Job job = defaultJob();
        JobId jobId = client.submit(job);
        // FIXME due to the TaskFlowJob deserialization issue, the following line has to be commented out
        // will fix it after
        //TaskFlowJob content = (TaskFlowJob) client.getInitialJobContent(jobId);
    }

    @Test(timeout = MAX_WAIT_TIME)
    public void testPushFileWithNonAdminUserPwdShouldSucceed() throws Exception {
        File tmpFile = testFolder.newFile();
        Files.write("non_admin_user_push_file_contents".getBytes(), tmpFile);
        ISchedulerClient client = SchedulerClient.createInstance();
        client.init(new ConnectionInfo(getRestServerUrl(), getNonAdminLogin(), getNonAdminLoginPassword(), null, true));
        client.pushFile("USERSPACE", "/test_non_admin_user_push_file", "tmpfile.tmp",
                tmpFile.getAbsolutePath());
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

        @Override
        public void jobSubmittedEvent(JobState jobState) {
            System.out.println("JobSubmittedEvent()");
            synchronized (this) {

                jobStateStack.push(jobState);
                notifyAll();
            }
        }

        public JobState getSubmittedJob() {
            System.out.println("getSubmittedJbo");
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

        @Override
        public void jobStateUpdatedEvent(NotificationData<JobInfo> arg0) {
        }

        @Override
        public void schedulerStateUpdatedEvent(SchedulerEvent arg0) {
        }

        @Override
        public void taskStateUpdatedEvent(NotificationData<TaskInfo> arg0) {
        }

        @Override
        public void usersUpdatedEvent(NotificationData<UserIdentification> arg0) {
        }

		@Override
		public void jobUpdatedFullDataEvent(JobState job) {			
		}
    }
}
