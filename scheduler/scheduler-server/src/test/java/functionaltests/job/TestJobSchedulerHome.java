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
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package functionaltests.job;

import functionaltests.executables.PAHomeExecutable;
import functionaltests.utils.SchedulerFunctionalTest;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.proactive.utils.OperatingSystem;
import org.ow2.proactive.scheduler.common.job.*;
import org.ow2.proactive.scheduler.common.task.*;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scripting.Script;
import org.ow2.proactive.scripting.SimpleScript;
import org.ow2.proactive.scripting.TaskScript;

import java.io.File;

import static functionaltests.utils.SchedulerTHelper.log;
import static org.junit.Assert.*;


/**
 * Test checks that pa.home, pa.rm.home and pa.scheduler.home is defined for all kind of tasks
 */
public class TestJobSchedulerHome extends SchedulerFunctionalTest {

    private static final long TIMEOUT = 30000;

    private String schedulerHomePath;

    @Before
    public void init() throws Throwable {
        schedulerHomePath = PASchedulerProperties.SCHEDULER_HOME.getValueAsString();
    }

    @Test
    public void run() throws Throwable {
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
        Script pahomeScript = new SimpleScript(TestJobSchedulerHome.class
                .getResource("/functionaltests/scripts/schedulerHome.js"), new String[] { schedulerHomePath });
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
