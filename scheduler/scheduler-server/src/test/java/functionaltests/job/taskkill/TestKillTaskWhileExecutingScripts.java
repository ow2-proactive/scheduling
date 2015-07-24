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
package functionaltests.job.taskkill;

import java.util.List;

import org.objectweb.proactive.core.node.Node;
import org.ow2.proactive.scheduler.common.job.Job;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.ForkEnvironment;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.ScriptTask;
import org.ow2.proactive.scheduler.common.task.flow.FlowScript;
import org.ow2.proactive.scripting.Script;
import org.ow2.proactive.scripting.SimpleScript;
import org.ow2.proactive.scripting.TaskScript;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import functionaltests.utils.SchedulerFunctionalTest;
import functionaltests.executables.EmptyExecutable;
import functionaltests.executables.EndlessExecutable;

import static functionaltests.utils.SchedulerTHelper.log;
import static org.junit.Assert.*;


/**
 * This class tests a basic actions of a job submission to ProActive scheduler :
 * Connection to scheduler, with authentication
 * Register a monitor to Scheduler in order to receive events concerning
 * job submission.
 *
 * This test will try many kind of possible errors.
 * The goal for this test is to terminate. If the Test timeout is reached, it is considered as failed.
 * Possible problems may come from many error count. If this job finish in a
 * reasonable time, it is considered that it passed the test.
 * Every events coming from the scheduler are also checked.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 1.0
 */
public class TestKillTaskWhileExecutingScripts extends SchedulerFunctionalTest {

    static Script endlessScript;

    @Before
    public void createScript() throws Throwable {
        endlessScript = new SimpleScript(
            "file = new java.io.File(java.lang.System.getProperty(\"java.io.tmpdir\"),"
                + "\"started.ok\");file.createNewFile();while(true){java.lang.Thread.sleep(500);}", "groovy");
    }

    @Test
    public void runAllTests() throws Throwable {
        javaTaskKillEndlessPreScript();
        javaTaskKillEndlessPostScript();
        javaTaskKillEndlessFlowScript();
        javaTaskKillEndlessJavaExecutable();
        forkedJavaTaskKillEndlessEnvScript();
        forkedJavaTaskKillEndlessJavaExecutable();
        killEndlessScriptTask();
    }

    public void javaTaskKillEndlessPreScript() throws Throwable {

        log("Test Java Task : killing an Endless PreScript ...");
        String tname = "javaTaskKillEndlessPreScript";
        // pre script interruption
        TaskFlowJob job = new TaskFlowJob();
        job.setName(this.getClass().getSimpleName() + "_" + tname);
        JavaTask task1 = new JavaTask();
        task1.setName(tname);
        task1.setExecutableClassName(EmptyExecutable.class.getName());
        task1.setPreScript(endlessScript);
        job.addTask(task1);

        submitAndCheckJob(job, tname);
    }

    public void javaTaskKillEndlessPostScript() throws Throwable {
        log("Test Java Task : killing an Endless PostScript ...");
        String tname = "javaTaskKillEndlessPostScript";
        // pre script interruption
        TaskFlowJob job = new TaskFlowJob();
        job.setName(this.getClass().getSimpleName() + "_" + tname);
        JavaTask task1 = new JavaTask();
        task1.setName(tname);
        task1.setExecutableClassName(EmptyExecutable.class.getName());
        task1.setPostScript(endlessScript);
        job.addTask(task1);

        submitAndCheckJob(job, tname);
    }

    public void javaTaskKillEndlessFlowScript() throws Throwable {
        log("Test Java Task : killing an Endless FlowScript ...");
        String tname = "javaTaskKillEndlessFlowScript";
        // pre script interruption
        TaskFlowJob job = new TaskFlowJob();
        job.setName(this.getClass().getSimpleName() + "_" + tname);
        JavaTask task1 = new JavaTask();
        task1.setName(tname);
        task1.setExecutableClassName(EmptyExecutable.class.getName());
        task1.setFlowScript(FlowScript.createLoopFlowScript(endlessScript, tname));
        job.addTask(task1);

        submitAndCheckJob(job, tname);
    }

    public void javaTaskKillEndlessJavaExecutable() throws Throwable {
        log("Test Java Task : killing an Endless Java Executable ...");
        String tname = "javaTaskKillEndlessJavaExecutable";
        // pre script interruption
        TaskFlowJob job = new TaskFlowJob();
        job.setName(this.getClass().getSimpleName() + "_" + tname);
        JavaTask task1 = new JavaTask();
        task1.setName(tname);
        task1.setExecutableClassName(EndlessExecutable.class.getName());
        job.addTask(task1);

        submitAndCheckJob(job, tname);
    }

    public void forkedJavaTaskKillEndlessEnvScript() throws Throwable {
        log("Test Forked Java Task : killing an Endless Java Executable ...");
        String tname = "forkedJavaTaskKillEndlessEnvScript";
        // pre script interruption
        TaskFlowJob job = new TaskFlowJob();
        job.setName(this.getClass().getSimpleName() + "_" + tname);
        JavaTask task1 = new JavaTask();
        task1.setName(tname);
        task1.setExecutableClassName(EmptyExecutable.class.getName());
        ForkEnvironment fe = new ForkEnvironment();
        fe.setEnvScript(endlessScript);
        task1.setForkEnvironment(fe);
        job.addTask(task1);

        submitAndCheckJob(job, tname);
    }

    public void forkedJavaTaskKillEndlessJavaExecutable() throws Throwable {
        log("Test : killing an Endless Java Executable ...");
        String tname = "forkedJavaTaskKillEndlessJavaExecutable";
        // pre script interruption
        TaskFlowJob job = new TaskFlowJob();
        job.setName(this.getClass().getSimpleName() + "_" + tname);
        JavaTask task1 = new JavaTask();
        task1.setName(tname);
        task1.setExecutableClassName(EndlessExecutable.class.getName());
        task1.setForkEnvironment(new ForkEnvironment());
        job.addTask(task1);

        submitAndCheckJob(job, tname);
    }

    public void killEndlessScriptTask() throws Throwable {
        log("Test : killing an Endless Script Task...");
        String tname = "killEndlessScriptTask";
        // pre script interruption
        TaskFlowJob job = new TaskFlowJob();
        job.setName(this.getClass().getSimpleName() + "_" + tname);
        ScriptTask task1 = new ScriptTask();
        task1.setName(tname);
        task1.setScript(new TaskScript(endlessScript));
        job.addTask(task1);

        submitAndCheckJob(job, tname);
    }

    private void submitAndCheckJob(Job job, String tname) throws Exception {
        //test submission and event reception
        FileUtils.deleteQuietly(EndlessExecutable.STARTED_FILE);
        JobId id = schedulerHelper.submitJob(job);
        log("Job submitted, id " + id.toString());
        log("Waiting for jobSubmitted Event");
        Job receivedJob = schedulerHelper.waitForEventJobSubmitted(id);
        assertEquals(receivedJob.getId(), id);
        log("Waiting for job running");
        JobInfo jInfo = schedulerHelper.waitForEventJobRunning(id);
        log("Waiting for task " + tname + " running");
        schedulerHelper.waitForEventTaskRunning(id, tname);
        assertEquals(jInfo.getJobId(), id);

        log("Waiting until file marker is created");
        while (!EndlessExecutable.STARTED_FILE.exists()) {
            Thread.sleep(50);
        }

        log("Kill job");
        assertTrue(schedulerHelper.killJob(id.toString()));


        List<Node> nodes = schedulerHelper.listAliveNodes();
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
