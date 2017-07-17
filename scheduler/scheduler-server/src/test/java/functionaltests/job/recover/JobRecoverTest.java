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
package functionaltests.job.recover;

import static com.google.common.truth.Truth.assertThat;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Map;

import org.junit.Test;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.task.TaskResult;

import functionaltests.utils.SchedulerFunctionalTestWithRestart;
import functionaltests.utils.SchedulerTHelper;


/**
 * This class tests the failure of the scheduler.
 * Even if it is in pending, running, or finished list, the scheduled jobs must be restarted
 * as expected after the scheduler restart.
 * This test case is about the behavior of the scheduler after a failure.
 * <p>
 * This test will first commit 3 jobs.
 * When the each one will be in each list (pending, running, finished), the scheduler will
 * be interrupt abnormally.
 * After restart, It will check that every data, tags, status are those expected.
 * It will finally check if the scheduling process will terminate.
 *
 * @author The ProActive Team
 * @date 2 jun 08
 * @since ProActive Scheduling 1.0
 */
public class JobRecoverTest extends SchedulerFunctionalTestWithRestart {

    @Test
    public void testJobRecover() throws Throwable {
        JobId firstJobId = schedulerHelper.submitJob(getWorkflowFile());
        JobId secondJobId = schedulerHelper.submitJob(getWorkflowFile());
        JobId thirdJobId = schedulerHelper.submitJob(getWorkflowFile());

        schedulerHelper.waitForEventJobRunning(firstJobId);

        SchedulerTHelper.log("Waiting for job 1 to finish");
        schedulerHelper.waitForFinishedJob(firstJobId);

        SchedulerTHelper.log("Kill Scheduler");
        schedulerHelper.killSchedulerAndNodesAndRestart(new File(SchedulerTHelper.class.getResource("/functionaltests/config/functionalTSchedulerProperties-updateDB.ini")
                                                                                       .toURI()).getAbsolutePath());

        SchedulerTHelper.log("Waiting for job 2 to finish");
        schedulerHelper.waitForEventJobFinished(secondJobId);

        SchedulerTHelper.log("Waiting for job 3 to finish");
        schedulerHelper.waitForFinishedJob(thirdJobId);

        SchedulerTHelper.log("Check result job 1");
        JobResult result = schedulerHelper.getJobResult(firstJobId);
        checkJobResults(result);

        SchedulerTHelper.log("Check result job 2");
        result = schedulerHelper.getJobResult(secondJobId);
        checkJobResults(result);

        SchedulerTHelper.log("Check result job 3");
        result = schedulerHelper.getJobResult(thirdJobId);
        checkJobResults(result);
    }

    private String getWorkflowFile() throws URISyntaxException {
        return new File(this.getClass()
                            .getResource("/functionaltests/descriptors/Job_PI_recover.xml")
                            .toURI()).getAbsolutePath();
    }

    private void checkJobResults(JobResult result) throws Throwable {
        Map<String, TaskResult> allResults = result.getAllResults();

        assertThat(allResults).hasSize(6);

        for (int i = 1; i <= allResults.size(); i++) {
            TaskResult taskResult = result.getResult("Computation" + i);
            assertThat(taskResult.value()).isNotNull();
            assertThat(taskResult.getException()).isNull();
        }
    }

}
