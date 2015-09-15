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
package functionaltests.job;

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
import org.ow2.proactive.scripting.SimpleScript;
import org.ow2.proactive.scripting.TaskScript;
import org.junit.Test;

import functionaltests.executables.EndlessExecutable;
import functionaltests.utils.SchedulerFunctionalTest;

import static functionaltests.utils.SchedulerTHelper.log;
import static org.junit.Assert.*;


/**
 * Test checks that walltime parameter is correctly taken into account for various tasks
 */
public class TestJobWalltime extends SchedulerFunctionalTest {

    private static final long TIMEOUT = 30000;

    @Test
    public void run() throws Throwable {
        JobId walltimeJavaTask = walltimeJavaTask();
        JobId walltimeForkedJavaTask = walltimeForkedJavaTask();
        JobId walltimeNativeTask = walltimeNativeTask();
        JobId walltimeScriptTask = walltimeScriptTask();

        checkJobRanAndWalltimed("walltimeJavaTask", walltimeJavaTask);
        checkJobRanAndWalltimed("walltimeForkedJavaTask", walltimeForkedJavaTask);
        checkJobRanAndWalltimed("walltimeNativeTask", walltimeNativeTask);
        checkJobRanAndWalltimed("walltimeScriptTask", walltimeScriptTask);

        schedulerHelper.checkNodesAreClean();
    }

    public JobId walltimeJavaTask() throws Throwable {
        log("Test WallTime Java Task ...");
        String tname = "walltimeJavaTask";
        // pre script interruption
        TaskFlowJob job = new TaskFlowJob();
        job.setName(this.getClass().getSimpleName() + "_" + tname);
        JavaTask task1 = new JavaTask();
        task1.setName(tname);
        task1.setWallTime(5000);
        task1.setExecutableClassName(EndlessExecutable.class.getName());
        job.addTask(task1);

        return submitJob(job);
    }

    public JobId walltimeForkedJavaTask() throws Throwable {
        log("Test WallTime Forked Java Task ...");
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

        return submitJob(job);
    }

    public JobId walltimeScriptTask() throws Throwable {
        log("Test WallTime Script Task...");
        String tname = "walltimeScriptTask";
        // pre script interruption
        TaskFlowJob job = new TaskFlowJob();
        job.setName(this.getClass().getSimpleName() + "_" + tname);
        ScriptTask task1 = new ScriptTask();
        task1.setName(tname);
        task1.setWallTime(5000);
        task1.setScript(new TaskScript(new SimpleScript("while(true){java.lang.Thread.sleep(500);}",
            "javascript")));
        job.addTask(task1);

        return submitJob(job);
    }

    public JobId walltimeNativeTask() throws Throwable {
        log("Test WallTime Native Task ...");
        String tname = "walltimeNativeTask";
        // pre script interruption
        TaskFlowJob job = new TaskFlowJob();
        job.setName(this.getClass().getSimpleName() + "_" + tname);
        NativeTask task1 = new NativeTask();
        task1.setName(tname);
        task1.setWallTime(5000);

        if (OperatingSystem.getOperatingSystem() == OperatingSystem.windows) {
            task1.setCommandLine("ping", "127.0.0.1", "-n", "10000");
        } else {
            task1.setCommandLine("sleep", "10000");
        }

        job.addTask(task1);

        return submitJob(job);
    }

    private JobId submitJob(Job job) throws Exception {
        JobId id = schedulerHelper.submitJob(job);
        log("Job submitted, id " + id.toString());
        log("Waiting for jobSubmitted Event");
        Job receivedJob = schedulerHelper.waitForEventJobSubmitted(id);
        assertEquals(receivedJob.getId(), id);
        return id;
    }

    private void checkJobRanAndWalltimed(String tname, JobId id) throws Exception {
        log("Waiting for job running");
        JobInfo jInfo = schedulerHelper.waitForEventJobRunning(id);
        log("Waiting for task " + tname + " running");
        schedulerHelper.waitForEventTaskRunning(id, tname);
        assertEquals(jInfo.getJobId(), id);

        log("Waiting until the walltime is reached");
        JobInfo info = schedulerHelper.waitForEventJobFinished(id, TIMEOUT);
        assertNotNull(info);

        JobResult res = schedulerHelper.getJobResult(id);
        assertNotNull(res);

        // The task result should receive the walltime exceeded exception
        TaskResult tres = res.getResult(tname);

        System.out.println(tres.getOutput().getAllLogs(false));

        assertTrue(tres.hadException());
        assertTrue(tres.getException() instanceof WalltimeExceededException);
    }

}
