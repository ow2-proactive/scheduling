/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
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
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;
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
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.executable.Executable;
import org.ow2.proactive.scheduler.common.task.executable.JavaExecutable;
import org.ow2.proactive.scheduler.common.util.JarUtils;
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
        init(SchedulerClientTest.class.getSimpleName());
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
    public void testWaitForTerminatingJob() throws Exception {
        ISchedulerClient client = clientInstance();
        Job job = defaultJob();
        JobId jobId = submitJob(job, client);
        // should return immediately
        client.waitForJob(jobId, TimeUnit.SECONDS.toMillis(10));
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
                "Unable to delete the local file after push, maybe there are still some open streams ?",
                emptyFile.delete());

        // Pull it from the userspace to be sure that it was pushed
        client.pullFile("USERSPACE", "", emptyFile.getCanonicalPath());

        // Check the file was pulled
        Assert.assertTrue("Unable to pull the empty file, maybe the pull mechanism is broken ?",
                emptyFile.exists());

        // Delete the local file
        Assert.assertTrue(
                "Unable to delete the local file after pull, maybe there are still some open streams ?",
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

    @Test(timeout = MAX_WAIT_TIME)
    public void testPushFileWithNonAdminUserPwdShouldSucceed() throws Exception {
        File tmpFile = testFolder.newFile();
        Files.write("non_admin_user_push_file_contents".getBytes(), tmpFile);
        ISchedulerClient client = SchedulerClient.createInstance();
        client.init(getRestServerUrl(), getNonAdminLogin(), getNonAdminLoginPassword());
        client.pushFile("USERSPACE", "/test_non_admin_user_push_file", "tmpfile.tmp",
                tmpFile.getAbsolutePath());
        String destDirPath = URI.create(client.getUserSpaceURIs().get(0)).getPath();
        File destFile = new File(destDirPath, "test_non_admin_user_push_file/tmpfile.tmp");
        assertTrue(Files.equal(tmpFile, destFile));
    }

    private ISchedulerClient clientInstance() throws Exception {
        ISchedulerClient client = SchedulerClient.createInstance();
        client.init(getRestServerUrl(), getLogin(), getPassword());
        return client;
    }

    private JobId submitJob(Job job, ISchedulerClient client) throws Exception {
        return client.submit(job);
    }

    /**
     * Creates a class that inherits from JavaExecutable and implements the
     * execute method that returns the value to test.
     *
     * @param className
     *            the className of the class to create
     * @param valueToReturn
     *            the int value to return
     * @param insideDir
     *            the dir that will contain the created class
     * @throws Exception
     *             If the classes cannot be created
     */
    public static void createClass(String className, int valueToReturn, File insideDir) throws Exception {
        ClassPool pool = ClassPool.getDefault();
        // create new classes
        CtClass cc1 = pool.makeClass(className);
        CtClass serializableClass = pool.get("java.io.Serializable");

        // get super-type and super-super-type
        CtClass upper = pool.get(JavaExecutable.class.getName());
        CtClass upupper = pool.get(Executable.class.getName());

        // get Executable 'execute' method
        CtMethod absExec = upupper.getMethod("execute",
                "([Lorg/ow2/proactive/scheduler/common/task/TaskResult;)Ljava/io/Serializable;");

        // set superclass of new classes
        cc1.setSuperclass(upper);
        cc1.addInterface(serializableClass);

        // create method for first class
        CtMethod exec1 = CtNewMethod.make(serializableClass, absExec.getName(), absExec.getParameterTypes(),
                absExec.getExceptionTypes(), "return new java.lang.Integer(" + valueToReturn + ");", cc1);
        cc1.addMethod(exec1);

        cc1.writeFile(insideDir.getAbsolutePath());
    }

    private static class SchedulerEventListenerImpl implements SchedulerEventListener {
        private Stack<JobState> jobStateStack = new Stack<JobState>();

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
    }
}
