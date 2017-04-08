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
package functionaltests.dataspaces;

import static functionaltests.utils.SchedulerTHelper.log;
import static org.junit.Assert.*;

import java.io.File;

import org.junit.BeforeClass;
import org.junit.Test;
import org.ow2.proactive.scheduler.common.exception.UserException;
import org.ow2.proactive.scheduler.common.job.Job;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.OnTaskError;
import org.ow2.proactive.scheduler.common.task.ScriptTask;
import org.ow2.proactive.scheduler.common.task.dataspaces.OutputAccessMode;
import org.ow2.proactive.scripting.InvalidScriptException;
import org.ow2.proactive.scripting.SimpleScript;
import org.ow2.proactive.scripting.TaskScript;

import functionaltests.utils.SchedulerFunctionalTest;
import functionaltests.utils.SchedulerFunctionalTestWithCustomConfigAndRestart;
import functionaltests.utils.SchedulerTHelper;


/**
 * This test is checking whether files are left over when tasks are killed concurrently
 * When files are left, it prevents subsequent tasks from running
 * The test starts a clean scheduler in non-fork mode (for memory consumption reasons) with many nodes running in the same JVM
 * It submits a job with matching number of tasks which all create a file and wait one second
 * As soon as each task is running the test kills the task.
 * <p/>
 * When all tasks are killed, the same job is resubmitted without killing, when this second job is executed without error,
 * , it means that no junk file has been created by the previous iteration
 */
public class TestDataspaceConcurrentKilling extends SchedulerFunctionalTestWithCustomConfigAndRestart {

    static final int NB_NODES = 50;

    static final int NB_TASKS = NB_NODES;

    static final String JOB_NAME = "TestDataspaceConcurrent";

    static final String TASK_NAME = "ConcurrentFT";

    static final String FILE_NAME = "TASKFILE_";

    @BeforeClass
    public static void startDedicatedScheduler() throws Exception {
        // we start a scheduler with an empty RM and add multiple nodes
        schedulerHelper = new SchedulerTHelper(true,
                                               true,
                                               new File(SchedulerFunctionalTest.class.getResource("/functionaltests/config/scheduler-nonforkedscripttasks.ini")
                                                                                     .toURI()).getAbsolutePath());

        schedulerHelper.createNodeSource(JOB_NAME, NB_NODES);
    }

    @Test
    public void multiple_tasks_transferring_with_kill() throws Throwable {
        Job job = createJobWithFileTransfers();

        JobId id = schedulerHelper.submitJob(job);
        log("Job submitted, id " + id.toString());
        log("Waiting for jobSubmitted Event");
        Job receivedJob = schedulerHelper.waitForEventJobSubmitted(id);
        assertEquals(receivedJob.getId(), id);
        log("Waiting for job running");
        JobInfo jInfo = schedulerHelper.waitForEventJobRunning(id);
        for (int i = 0; i < NB_TASKS; i++) {
            log("Waiting for task " + TASK_NAME + i + " running");
            schedulerHelper.waitForEventTaskRunning(id, TASK_NAME + i);
            log("Kill task " + TASK_NAME + i);
            try {
                schedulerHelper.killTask(id.toString(), TASK_NAME + i);
            } catch (Exception ignored) {
                // sometimes the job can be finished before all kill tasks messages are completed
            }
        }

        // Run it again and verify that it works without problem
        id = schedulerHelper.testJobSubmission(job);
        assertFalse("The job execution must not fail, check the node source log file : Node-local-" + JOB_NAME + ".log",
                    schedulerHelper.getJobResult(id).hadException());
    }

    public Job createJobWithFileTransfers() throws UserException, InvalidScriptException {
        TaskFlowJob job = new TaskFlowJob();
        job.setName(JOB_NAME);
        job.setOnTaskError(OnTaskError.CONTINUE_JOB_EXECUTION);
        for (int i = 0; i < NB_TASKS; i++) {
            ScriptTask st = new ScriptTask();
            st.setName(TASK_NAME + i);
            st.setScript(new TaskScript(new SimpleScript("new File(\"" + FILE_NAME + i +
                                                         "\").createNewFile(); java.lang.Thread.sleep(1000)",
                                                         "groovy")));
            st.addOutputFiles(FILE_NAME + i, OutputAccessMode.TransferToUserSpace);
            job.addTask(st);
        }
        return job;
    }

}
