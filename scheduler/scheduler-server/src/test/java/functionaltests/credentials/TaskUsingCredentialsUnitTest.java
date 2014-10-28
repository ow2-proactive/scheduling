/*
 *  *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2014 INRIA/University of
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
 *  * $$ACTIVEEON_INITIAL_DEV$$
 */
package functionaltests.credentials;

import java.io.Serializable;
import java.net.URISyntaxException;
import java.security.PublicKey;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.utils.OperatingSystem;
import org.ow2.proactive.authentication.crypto.CredData;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.scheduler.common.TaskTerminateNotification;
import org.ow2.proactive.scheduler.common.task.ForkEnvironment;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.util.Object2ByteConverter;
import org.ow2.proactive.scheduler.job.JobIdImpl;
import org.ow2.proactive.scheduler.task.TaskIdImpl;
import org.ow2.proactive.scheduler.task.TaskLauncherInitializer;
import org.ow2.proactive.scheduler.task.forked.ForkedJavaExecutableContainer;
import org.ow2.proactive.scheduler.task.forked.JavaTaskLauncherForker;
import org.ow2.proactive.scheduler.task.java.JavaExecutableContainer;
import org.ow2.proactive.scheduler.task.java.JavaTaskLauncher;
import org.ow2.proactive.scheduler.task.nativ.NativeExecutableContainer;
import org.ow2.proactive.scheduler.task.nativ.NativeTaskLauncher;
import org.ow2.proactive.scheduler.task.script.ScriptExecutableContainer;
import org.ow2.proactive.scheduler.task.script.ScriptTaskLauncher;
import org.ow2.proactive.scripting.SimpleScript;
import org.ow2.proactive.scripting.TaskScript;
import org.ow2.proactive.utils.NodeSet;
import org.ow2.tests.ProActiveTest;
import org.junit.Assume;
import org.junit.Test;

import functionaltests.SchedulerTHelper;
import functionaltests.executables.PrintAndReturn;

import static org.junit.Assert.*;


public class TaskUsingCredentialsUnitTest extends ProActiveTest {

    @Test
    public void javaTaskWithParameter() throws Throwable {
        JavaTaskLauncher javaTaskLauncher = new JavaTaskLauncher(createTaskInitializer());
        javaTaskLauncher = PAActiveObject.turnActive(javaTaskLauncher);

        TaskResultWaiter taskResultWaiter = new TaskResultWaiter();
        WaitForResultNotification waitForResultNotification = new WaitForResultNotification(taskResultWaiter);
        waitForResultNotification = PAActiveObject.turnActive(waitForResultNotification);

        JavaExecutableContainer executableContainer = new JavaExecutableContainer(PrintAndReturn.class
                .getName(), Collections.singletonMap("variable", Object2ByteConverter
                .convertObject2Byte("$CREDENTIALS_MY_APP_PASSWORD")));

        Map<String, String> thirdPartyCredentials = Collections.singletonMap("MY_APP_PASSWORD",
                "superpassword");
        PublicKey publicKey = javaTaskLauncher.generatePublicKey();
        Credentials cred = Credentials.createCredentials(new CredData(SchedulerTHelper.admin_username,
            SchedulerTHelper.admin_password, thirdPartyCredentials), publicKey);
        executableContainer.setCredentials(cred);

        javaTaskLauncher.doTask(waitForResultNotification, executableContainer);

        assertEquals("superpassword", taskResultWaiter.getTaskResult().value());
    }

    @Test
    public void javaTaskWithAPI() throws Throwable {
        JavaTaskLauncher javaTaskLauncher = new JavaTaskLauncher(createTaskInitializer());
        javaTaskLauncher = PAActiveObject.turnActive(javaTaskLauncher);

        TaskResultWaiter taskResultWaiter = new TaskResultWaiter();
        WaitForResultNotification waitForResultNotification = new WaitForResultNotification(taskResultWaiter);
        waitForResultNotification = PAActiveObject.turnActive(waitForResultNotification);

        JavaExecutableContainer executableContainer = new JavaExecutableContainer(
            PrintAndReturnCredentialsTask.class.getName(), Collections.<String, byte[]> emptyMap());

        Map<String, String> thirdPartyCredentials = Collections.singletonMap("MY_APP_PASSWORD",
                "superpassword");
        PublicKey publicKey = javaTaskLauncher.generatePublicKey();
        Credentials cred = Credentials.createCredentials(new CredData(SchedulerTHelper.admin_username,
            SchedulerTHelper.admin_password, thirdPartyCredentials), publicKey);
        executableContainer.setCredentials(cred);

        javaTaskLauncher.doTask(waitForResultNotification, executableContainer);

        assertEquals("superpassword", taskResultWaiter.getTaskResult().value());
    }

    @Test
    public void forkedJavaTask() throws Throwable {
        JavaTaskLauncherForker javaTaskLauncherForker = new JavaTaskLauncherForker(createTaskInitializer());
        javaTaskLauncherForker = PAActiveObject.turnActive(javaTaskLauncherForker);

        TaskResultWaiter taskResultWaiter = new TaskResultWaiter();
        WaitForResultNotification waitForResultNotification = new WaitForResultNotification(taskResultWaiter);
        waitForResultNotification = PAActiveObject.turnActive(waitForResultNotification);

        ForkedJavaExecutableContainer executableContainer = new ForkedJavaExecutableContainer(
            "functionaltests.executables.PrintAndReturn", Collections.singletonMap("variable",
                    Object2ByteConverter.convertObject2Byte("$CREDENTIALS_MY_APP_PASSWORD")));
        executableContainer.setNodes(new NodeSet());

        Map<String, String> thirdPartyCredentials = Collections.singletonMap("MY_APP_PASSWORD",
                "superpassword");
        PublicKey publicKey = javaTaskLauncherForker.generatePublicKey();
        Credentials cred = Credentials.createCredentials(new CredData(SchedulerTHelper.admin_username,
            SchedulerTHelper.admin_password, thirdPartyCredentials), publicKey);
        executableContainer.setCredentials(cred);

        ForkEnvironment forkEnvironment = new ForkEnvironment();
        forkEnvironment.setWorkingDir(".");
        executableContainer.setForkEnvironment(forkEnvironment);

        javaTaskLauncherForker.doTask(waitForResultNotification, executableContainer);

        assertEquals("superpassword", taskResultWaiter.getTaskResult().value());
    }

    @Test
    public void nativeTask() throws Throwable {
        Assume.assumeTrue(OperatingSystem.getOperatingSystem() == OperatingSystem.unix);

        NativeTaskLauncher nativeTaskLauncher = new NativeTaskLauncher(createTaskInitializer());
        nativeTaskLauncher = PAActiveObject.turnActive(nativeTaskLauncher);

        TaskResultWaiter taskResultWaiter = new TaskResultWaiter();
        WaitForResultNotification waitForResultNotification = new WaitForResultNotification(taskResultWaiter);
        waitForResultNotification = PAActiveObject.turnActive(waitForResultNotification);

        NativeExecutableContainer executableContainer = new NativeExecutableContainer(new String[] {
                "test", "superpassword", "=", "$CREDENTIALS_MY_APP_PASSWORD" }, null, ".");
        executableContainer.setNodes(new NodeSet());

        Map<String, String> thirdPartyCredentials = new HashMap<String, String>();
        thirdPartyCredentials.put("MY_APP_PASSWORD", "superpassword");
        // to test partial replacements
        thirdPartyCredentials.put("MY_APP", "doo");
        PublicKey publicKey = nativeTaskLauncher.generatePublicKey();
        Credentials cred = Credentials.createCredentials(new CredData(SchedulerTHelper.admin_username,
            SchedulerTHelper.admin_password, thirdPartyCredentials), publicKey);
        executableContainer.setCredentials(cred);

        nativeTaskLauncher.doTask(waitForResultNotification, executableContainer);

        assertEquals(0, taskResultWaiter.getTaskResult().value());
    }

    @Test
    public void scriptTask() throws Throwable {
        ScriptTaskLauncher scriptTaskLauncher = new ScriptTaskLauncher(createTaskInitializer());
        scriptTaskLauncher = PAActiveObject.turnActive(scriptTaskLauncher);

        TaskResultWaiter taskResultWaiter = new TaskResultWaiter();
        WaitForResultNotification waitForResultNotification = new WaitForResultNotification(taskResultWaiter);
        waitForResultNotification = PAActiveObject.turnActive(waitForResultNotification);

        ScriptExecutableContainer executableContainer = new ScriptExecutableContainer(new TaskScript(
            new SimpleScript("result = String(credentials.get('MY_APP_PASSWORD'))", "javascript")));

        Map<String, String> thirdPartyCredentials = Collections.singletonMap("MY_APP_PASSWORD",
                "superpassword");
        PublicKey publicKey = scriptTaskLauncher.generatePublicKey();
        Credentials cred = Credentials.createCredentials(new CredData(SchedulerTHelper.admin_username,
            SchedulerTHelper.admin_password, thirdPartyCredentials), publicKey);
        executableContainer.setCredentials(cred);

        scriptTaskLauncher.doTask(waitForResultNotification, executableContainer);

        assertEquals("superpassword", taskResultWaiter.getTaskResult().value());
    }

    private TaskLauncherInitializer createTaskInitializer() throws ProActiveException, URISyntaxException {
        TaskLauncherInitializer initializer = new TaskLauncherInitializer();
        initializer.setTaskId(TaskIdImpl.createTaskId(JobIdImpl.makeJobId("1000"), "job", 1000L, false));
        initializer.setPingAttempts(1);
        initializer.setPolicyContent("grant {\n" + "permission java.security.AllPermission;\n" + "};\n");
        return initializer;
    }

    public class TaskResultWaiter {
        private volatile TaskResult taskResult;

        public void setTaskResult(TaskResult taskResult) {
            synchronized (this) {
                this.taskResult = taskResult;
                notify();
            }
        }

        public TaskResult getTaskResult() throws InterruptedException {
            synchronized (this) {
                while (taskResult == null) {
                    wait(30000);
                }
            }
            return taskResult;
        }
    }

    public static class WaitForResultNotification implements TaskTerminateNotification, Serializable {

        private TaskResultWaiter taskResultWaiter;

        public WaitForResultNotification(TaskResultWaiter taskResultWaiter) {
            this.taskResultWaiter = taskResultWaiter;
        }

        // Needed for ProActive
        public WaitForResultNotification() {
        }

        @Override
        public void terminate(TaskId taskId, TaskResult taskResult) {
            this.taskResultWaiter.setTaskResult(taskResult);
        }

    }

}
