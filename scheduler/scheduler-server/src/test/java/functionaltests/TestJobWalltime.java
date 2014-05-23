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
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package functionaltests;

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.utils.OperatingSystem;
import org.ow2.proactive.scheduler.common.exception.WalltimeExceededException;
import org.ow2.proactive.scheduler.common.job.Job;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.ForkEnvironment;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.NativeTask;
import org.ow2.proactive.scheduler.common.task.ScriptTask;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scripting.Script;
import org.ow2.proactive.scripting.SimpleScript;
import org.ow2.proactive.scripting.TaskScript;
import org.ow2.tests.FunctionalTest;

import functionaltests.executables.EndlessExecutable;


/**
 * Test checks that walltime parameter is correctly taken into account for various tasks
 */
public class TestJobWalltime extends FunctionalTest {

    static Script endlessScript;

    private static final long TIMEOUT = 30000;

    @Before
    public void init() throws Throwable {
        endlessScript = new SimpleScript(
            "file = new java.io.File(java.lang.System.getProperty(\"java.io.tmpdir\"),\"started.ok\");file.createNewFile();while(true){java.lang.Thread.sleep(500);}",
            "javascript");
        SchedulerTHelper.startScheduler();
    }

    @After
    public void clean() throws Throwable {
        SchedulerTHelper.killScheduler();
    }

    @Test
    public void walltimeJavaTask() throws Throwable {
        SchedulerTHelper.log("Test WallTime Java Task ...");
        String tname = "walltimeJavaTask";
        // pre script interruption
        TaskFlowJob job = new TaskFlowJob();
        job.setName(this.getClass().getSimpleName() + "_" + tname);
        JavaTask task1 = new JavaTask();
        task1.setName(tname);
        task1.setWallTime(5000);
        task1.setExecutableClassName(EndlessExecutable.class.getName());
        job.addTask(task1);

        submitAndCheckJob(job, tname);
    }

    @Test
    public void walltimeForkedJavaTask() throws Throwable {
        SchedulerTHelper.log("Test WallTime Forked Java Task ...");
        String tname = "walltimeForkedJavaTask";
        // pre script interruption
        TaskFlowJob job = new TaskFlowJob();
        job.setName(this.getClass().getSimpleName() + "_" + tname);
        JavaTask task1 = new JavaTask();
        task1.setName(tname);
        task1.setWallTime(5000);
        task1.setExecutableClassName(EndlessExecutable.class.getName());
        task1.setForkEnvironment(new ForkEnvironment());
        job.addTask(task1);

        submitAndCheckJob(job, tname);
    }

    @Test
    public void walltimeScriptTask() throws Throwable {
        SchedulerTHelper.log("Test WallTime Script Task...");
        String tname = "walltimeScriptTask";
        // pre script interruption
        TaskFlowJob job = new TaskFlowJob();
        job.setName(this.getClass().getSimpleName() + "_" + tname);
        ScriptTask task1 = new ScriptTask();
        task1.setName(tname);
        task1.setWallTime(5000);
        task1.setScript(new TaskScript(endlessScript));
        job.addTask(task1);

        submitAndCheckJob(job, tname);
    }

    @Test
    public void walltimeNativeTask() throws Throwable {
        SchedulerTHelper.log("Test WallTime Native Task ...");
        String tname = "walltimeNativeTask";
        // pre script interruption
        TaskFlowJob job = new TaskFlowJob();
        job.setName(this.getClass().getSimpleName() + "_" + tname);
        NativeTask task1 = new NativeTask();
        task1.setName(tname);
        task1.setWallTime(5000);

        if (OperatingSystem.getOperatingSystem() == OperatingSystem.windows) {
            task1.setCommandLine("$JAVA_HOME\\..\\bin\\jrunscript.exe", "-e",
                    "java.lang.Thread.sleep(300000)");
            // task1.setCommandLine("timeout", "10000"); // if we launch it on windows it give an error that redirection is not supported
        } else {
            task1.setCommandLine("sleep", "10000");
        }

        job.addTask(task1);

        submitAndCheckJob(job, tname);
    }

    private void submitAndCheckJob(Job job, String tname) throws Exception {
        //test submission and event reception
        if (EndlessExecutable.STARTED_FILE.exists()) {
            EndlessExecutable.STARTED_FILE.delete();
        }
        JobId id = SchedulerTHelper.submitJob(job);
        SchedulerTHelper.log("Job submitted, id " + id.toString());
        SchedulerTHelper.log("Waiting for jobSubmitted Event");
        Job receivedJob = SchedulerTHelper.waitForEventJobSubmitted(id);
        Assert.assertEquals(receivedJob.getId(), id);
        SchedulerTHelper.log("Waiting for job running");
        JobInfo jInfo = SchedulerTHelper.waitForEventJobRunning(id);
        SchedulerTHelper.log("Waiting for task " + tname + " running");
        SchedulerTHelper.waitForEventTaskRunning(id, tname);
        Assert.assertEquals(jInfo.getJobId(), id);

        SchedulerTHelper.log("Waiting until the walltime is reached");
        JobInfo info = SchedulerTHelper.waitForEventJobFinished(id, TIMEOUT);
        Assert.assertNotNull(info);

        JobResult res = SchedulerTHelper.getJobResult(id);
        Assert.assertNotNull(res);

        // The task result should receive the walltime exceeded exception
        TaskResult tres = res.getResult(tname);

        System.out.println(tres.getOutput().getAllLogs(false));

        Assert.assertTrue(tres.hadException());

        tres.getException().printStackTrace();
        Assert.assertTrue(tres.getException() instanceof WalltimeExceededException);

        // Make sure that the task has been properly killed

        RMTHelper rm = RMTHelper.getDefaultInstance();

        List<Node> nodes = rm.listAliveNodes();
        // We wait until no active object remain on the nodes.
        // If AO remains the test will fail with a timeout.
        boolean remainingAO = true;

        long wait = 0;
        while (remainingAO && wait < 5000) {
            Thread.sleep(50);
            wait += 50;
            remainingAO = false;
            for (Node node : nodes) {
                remainingAO = remainingAO || (node.getNumberOfActiveObjects() > 0);
            }
        }
        Assert.assertFalse("No Active Objects should remain", remainingAO);
    }
}
