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

import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.utils.OperatingSystem;
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
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import functionaltests.executables.PAHomeExecutable;


/**
 * Test checks that pa.home, pa.rm.home and pa.scheduler.home is defined for all kind of tasks
 */
public class TestJobSchedulerHome extends FunctionalTest {

    static Script pahomeScript;

    String pahome;

    private static final long TIMEOUT = 30000;

    @Before
    public void init() throws Throwable {
        pahomeScript = new SimpleScript(
            "prop = variables.get(\"pa.scheduler.home\"); \n"
                + "if (expectedHome != prop) throw new Error(\"Invalid java home, expected : \" + expectedHome + \", received : + \" + prop)\n",
            "javascript");
        SchedulerTHelper.startScheduler();
        pahome = CentralPAPropertyRepository.PA_HOME.getValue();
    }

    @After
    public void clean() throws Throwable {
        SchedulerTHelper.killScheduler();
    }

    @Test
    public void run() throws Throwable {
        pahomeJavaTask();
        pahomeForkedJavaTask();
        pahomeNativeTask();
        pahomeScriptTask();
    }

    public void pahomeJavaTask() throws Throwable {
        SchedulerTHelper.log("Test ProActive Home Java Task ...");
        String tname = "pahomeJavaTask";
        TaskFlowJob job = new TaskFlowJob();
        job.setName(this.getClass().getSimpleName() + "_" + tname);
        JavaTask task1 = new JavaTask();
        task1.setName(tname);
        task1.setExecutableClassName(PAHomeExecutable.class.getName());
        task1.addArgument("expectedHome", pahome);
        job.addTask(task1);

        submitAndCheckJob(job, tname, false);
    }

    public void pahomeForkedJavaTask() throws Throwable {
        SchedulerTHelper.log("Test ProActive Home Forked Java Task ...");
        String tname = "pahomeForkedJavaTask";
        TaskFlowJob job = new TaskFlowJob();
        job.setName(this.getClass().getSimpleName() + "_" + tname);
        JavaTask task1 = new JavaTask();
        task1.setName(tname);
        task1.setExecutableClassName(PAHomeExecutable.class.getName());
        task1.addArgument("expectedHome", pahome);
        task1.setForkEnvironment(new ForkEnvironment());
        job.addTask(task1);

        submitAndCheckJob(job, tname, false);
    }

    public void pahomeScriptTask() throws Throwable {
        SchedulerTHelper.log("Test ProActive Home Script Task...");
        String tname = "pahomeScriptTask";
        TaskFlowJob job = new TaskFlowJob();
        job.setName(this.getClass().getSimpleName() + "_" + tname);
        ScriptTask task1 = new ScriptTask();
        task1.setName(tname);
        Script pahomeScript = new SimpleScript(TestJobSchedulerHome.class
                .getResource("/functionaltests/scripts/schedulerHome.js"), new String[] { pahome });
        ;
        TaskScript ts = new TaskScript(pahomeScript);

        task1.setScript(ts);
        job.addTask(task1);

        submitAndCheckJob(job, tname, false);
    }

    public void pahomeNativeTask() throws Throwable {
        SchedulerTHelper.log("Test ProActive Home Native Task ...");
        String tname = "pahomeNativeTask";
        TaskFlowJob job = new TaskFlowJob();
        job.setName(this.getClass().getSimpleName() + "_" + tname);
        NativeTask task1 = new NativeTask();
        task1.setName(tname);

        if (OperatingSystem.getOperatingSystem() == OperatingSystem.windows) {
            task1.setCommandLine("cmd.exe", "/c", "echo %variables_proactive.home%");
        } else {
            //task1.setCommandLine("bash", "-c", "echo ${variables_proactive.home}");
            task1.setCommandLine("env");
        }

        job.addTask(task1);

        submitAndCheckJob(job, tname, true);
    }

    private void submitAndCheckJob(Job job, String tname, boolean nativ) throws Exception {

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

        JobInfo info = SchedulerTHelper.waitForEventJobFinished(id, TIMEOUT);
        Assert.assertNotNull(info);

        JobResult res = SchedulerTHelper.getJobResult(id);
        Assert.assertNotNull(res);

        TaskResult tres = res.getResult(tname);

        String logs = tres.getOutput().getAllLogs(false);
        SchedulerTHelper.log("Task logs:");
        SchedulerTHelper.log(logs);
        if (nativ) {
            Assert.assertTrue(logs.contains(pahome));
        }

        Assert.assertFalse(tres.hadException());

    }
}
