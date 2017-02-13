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
package functionaltests.job;

import static functionaltests.utils.SchedulerTHelper.log;
import static org.junit.Assert.*;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.objectweb.proactive.utils.OperatingSystem;
import org.ow2.proactive.scheduler.common.job.*;
import org.ow2.proactive.scheduler.common.task.*;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scripting.Script;
import org.ow2.proactive.scripting.SimpleScript;
import org.ow2.proactive.scripting.TaskScript;

import functionaltests.executables.PAHomeExecutable;
import functionaltests.utils.SchedulerFunctionalTestNoRestart;


/**
 * Test checks that pa.home, pa.rm.home and pa.scheduler.home is defined for all kind of tasks
 */
public class TestJobSchedulerHome extends SchedulerFunctionalTestNoRestart {

    private static final long TIMEOUT = 30000;

    private String schedulerHomePath;

    @Before
    public void init() throws Throwable {
        schedulerHomePath = PASchedulerProperties.SCHEDULER_HOME.getValueAsString();
    }

    @Test
    public void testJobSchedulerHome() throws Throwable {
        pahomeJavaTask();
        pahomeForkedJavaTask();
        pahomeNativeTask();
        pahomeScriptTask();
    }

    public void pahomeJavaTask() throws Throwable {
        log("Test ProActive Home Java Task ...");
        String tname = "pahomeJavaTask";
        TaskFlowJob job = new TaskFlowJob();
        job.setName(this.getClass().getSimpleName() + "_" + tname);
        JavaTask task1 = new JavaTask();
        task1.setName(tname);
        task1.setExecutableClassName(PAHomeExecutable.class.getName());
        task1.addArgument("expectedHome", schedulerHomePath);
        job.addTask(task1);

        submitAndCheckJob(job, tname, false);
    }

    public void pahomeForkedJavaTask() throws Throwable {
        log("Test ProActive Home Forked Java Task ...");
        String tname = "pahomeForkedJavaTask";
        TaskFlowJob job = new TaskFlowJob();
        job.setName(this.getClass().getSimpleName() + "_" + tname);
        JavaTask task1 = new JavaTask();
        task1.setName(tname);
        task1.setExecutableClassName(PAHomeExecutable.class.getName());
        task1.addArgument("expectedHome", schedulerHomePath);
        task1.setForkEnvironment(new ForkEnvironment());
        job.addTask(task1);

        submitAndCheckJob(job, tname, false);
    }

    public void pahomeScriptTask() throws Throwable {
        log("Test ProActive Home Script Task...");
        String tname = "pahomeScriptTask";
        TaskFlowJob job = new TaskFlowJob();
        job.setName(this.getClass().getSimpleName() + "_" + tname);
        ScriptTask task1 = new ScriptTask();
        task1.setName(tname);
        Script pahomeScript = new SimpleScript(TestJobSchedulerHome.class.getResource("/functionaltests/scripts/schedulerHome.js"),
                                               new String[] { schedulerHomePath });
        ;
        TaskScript ts = new TaskScript(pahomeScript);

        task1.setScript(ts);
        job.addTask(task1);

        submitAndCheckJob(job, tname, false);
    }

    public void pahomeNativeTask() throws Throwable {
        log("Test ProActive Home Native Task ...");
        String tname = "pahomeNativeTask";
        TaskFlowJob job = new TaskFlowJob();
        job.setName(this.getClass().getSimpleName() + "_" + tname);
        NativeTask task1 = new NativeTask();
        task1.setName(tname);

        if (OperatingSystem.getOperatingSystem() == OperatingSystem.windows) {
            task1.setCommandLine("cmd.exe", "/c", "echo %variables_PA_SCHEDULER_HOME%");
        } else {
            task1.setCommandLine("env");
        }

        job.addTask(task1);

        submitAndCheckJob(job, tname, true);
    }

    private void submitAndCheckJob(Job job, String tname, boolean nativ) throws Exception {

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

        JobInfo info = schedulerHelper.waitForEventJobFinished(id, TIMEOUT);
        assertNotNull(info);

        JobResult res = schedulerHelper.getJobResult(id);
        assertNotNull(res);

        TaskResult tres = res.getResult(tname);

        String logs = tres.getOutput().getAllLogs(false);
        log("Task logs:");
        log(logs);
        if (nativ) {
            assertTrue(logs.contains(new File(schedulerHomePath).getCanonicalPath()));
        }

        assertFalse(tres.hadException());

    }
}
