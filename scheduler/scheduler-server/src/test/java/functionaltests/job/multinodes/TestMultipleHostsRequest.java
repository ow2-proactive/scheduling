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
package functionaltests.job.multinodes;

import static functionaltests.utils.SchedulerTHelper.log;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;

import org.junit.Test;
import org.objectweb.proactive.utils.OperatingSystem;
import org.ow2.proactive.scheduler.common.job.*;
import org.ow2.proactive.scheduler.common.job.factories.StaxJobFactory;
import org.ow2.proactive.scheduler.common.task.TaskInfo;
import org.ow2.proactive.scheduler.common.task.TaskStatus;

import functionaltests.utils.SchedulerFunctionalTestNoRestart;
import functionaltests.utils.SchedulerTHelper;


/**
 * Test whether attribute reservation of several nodes for a native task
 * book correctly number a needed node (attribute coresNumber in nativeExecutable in a job SML descriptor)
 * Test whther PA_NODEFILE and PA_CORE_NB environment variables are correctly set
 *
 * @author The ProActive Team
 */
public class TestMultipleHostsRequest extends SchedulerFunctionalTestNoRestart {

    private static URL jobDescriptor = TestMultipleHostsRequest.class.getResource("/functionaltests/descriptors/Job_native_4_hosts.xml");

    private static final String executablePathPropertyName = "EXEC_PATH";

    private static URL executablePath = TestMultipleHostsRequest.class.getResource("/functionaltests/executables/test_multiple_hosts_request.sh");

    private static URL executablePathWindows = TestMultipleHostsRequest.class.getResource("/functionaltests/executables/test_multiple_hosts_request.bat");

    @Test
    public void testMultipleHostsRequest() throws Throwable {

        String task1Name = "task1";

        switch (OperatingSystem.getOperatingSystem()) {
            case windows:
                //set system Property for executable path
                System.setProperty(executablePathPropertyName,
                                   new File(executablePathWindows.toURI()).getAbsolutePath());
                break;
            case unix:
                SchedulerTHelper.setExecutable(new File(executablePath.toURI()).getAbsolutePath());
                //set system Property for executable path
                System.setProperty(executablePathPropertyName, new File(executablePath.toURI()).getAbsolutePath());
                break;
            default:
                throw new IllegalStateException("Unsupported operating system");
        }

        //test submission and event reception
        TaskFlowJob job = (TaskFlowJob) StaxJobFactory.getFactory()
                                                      .createJob(new File(jobDescriptor.toURI()).getAbsolutePath());

        JobId id = schedulerHelper.submitJob(job);
        schedulerHelper.addExtraNodes(3);

        log("Job submitted, id " + id.toString());

        log("Waiting for jobSubmitted Event");
        JobState receivedState = schedulerHelper.waitForEventJobSubmitted(id);

        assertEquals(receivedState.getId(), id);

        log("Waiting for job running");
        JobInfo jInfo = schedulerHelper.waitForEventJobRunning(id);
        assertEquals(jInfo.getJobId(), id);
        assertEquals(JobStatus.RUNNING, jInfo.getStatus());

        schedulerHelper.waitForEventTaskRunning(id, task1Name);
        TaskInfo tInfo = schedulerHelper.waitForEventTaskFinished(id, task1Name);

        log(schedulerHelper.getSchedulerInterface().getTaskResult(id, "task1").getOutput().getAllLogs(false));

        assertEquals(TaskStatus.FINISHED, tInfo.getStatus());

        schedulerHelper.waitForEventJobFinished(id);
        JobResult res = schedulerHelper.getJobResult(id);

        //check that there is one exception in results
        assertTrue(res.getExceptionResults().isEmpty());

        //remove job
        schedulerHelper.removeJob(id);
        schedulerHelper.waitForEventJobRemoved(id);
    }
}
