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

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobResult;

import functionaltests.utils.SchedulerFunctionalTestNoRestart;


public class TestPauseResumeMany extends SchedulerFunctionalTestNoRestart {

    private static URL jobDescriptor = TestPauseResumeMany.class.getResource("/functionaltests/descriptors/Job_pause_resume_many.xml");

    int NB_RUNS = 20;

    @Test
    public void test() throws Throwable {
        String jobPath = new File(jobDescriptor.toURI()).getAbsolutePath();
        for (int i = 0; i < NB_RUNS; i++) {
            JobId jobId = schedulerHelper.submitJob(jobPath);
            schedulerHelper.waitForEventJobRunning(jobId);
            schedulerHelper.waitForEventTaskFinished(jobId, "Split");
            schedulerHelper.getSchedulerInterface().pauseJob(jobId);
            schedulerHelper.getSchedulerInterface().resumeJob(jobId);
            Thread.sleep(50);
            schedulerHelper.getSchedulerInterface().pauseJob(jobId);
            Thread.sleep(50);
            schedulerHelper.getSchedulerInterface().resumeJob(jobId);
            schedulerHelper.waitForEventJobFinished(jobId);
            JobResult result = schedulerHelper.getJobResult(jobId);
            List<String> tasksOutputList = result.getAllResults()
                                                 .entrySet()
                                                 .stream()
                                                 .filter(entry -> entry.getKey().contains("Process"))
                                                 .map(entry -> entry.getValue().getOutput().getAllLogs())
                                                 .collect(Collectors.toList());
            log("Received output:");
            System.out.println("" + tasksOutputList);
            boolean allTaskOutputContainExpectedOutput = result.getAllResults()
                                                               .entrySet()
                                                               .stream()
                                                               .filter(entry -> entry.getKey().contains("Process"))
                                                               .allMatch(entry -> entry.getValue()
                                                                                       .getOutput()
                                                                                       .getStdoutLogs()
                                                                                       .contains("Hello Test"));
            Assert.assertTrue("All tasks output should contain the expected text", allTaskOutputContainExpectedOutput);
        }

    }
}
