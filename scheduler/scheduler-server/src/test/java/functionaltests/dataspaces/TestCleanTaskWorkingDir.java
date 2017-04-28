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

import static org.junit.Assert.assertFalse;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.job.factories.StaxJobFactory;
import org.ow2.proactive.scheduler.common.task.TaskResult;

import functionaltests.utils.SchedulerFunctionalTestNoRestart;


public class TestCleanTaskWorkingDir extends SchedulerFunctionalTestNoRestart {

    @Test
    public void input_files_are_in_working_dir_for_forked_tasks() throws Throwable {
        TaskFlowJob job = (TaskFlowJob) StaxJobFactory.getFactory()
                                                      .createJob(new File(TestCleanTaskWorkingDir.class.getResource("/functionaltests/descriptors/Job_fill_working_dir_script_task.xml")
                                                                                                       .toURI()).getAbsolutePath());

        JobId id = schedulerHelper.testJobSubmission(job);
        assertFalse(schedulerHelper.getJobResult(id).hadException());

        JobResult jobResult = schedulerHelper.getJobResult(id);
        TaskResult taskResult = jobResult.getResult("fill_working_dir_task");
        String workingDirPath = taskResult.getOutput().getAllLogs(false);
        Assert.assertFalse(new File(workingDirPath).exists());
    }

}
