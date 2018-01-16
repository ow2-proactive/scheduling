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
package functionaltests.workflow.variables;

import static functionaltests.utils.SchedulerTHelper.log;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.net.URL;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.job.factories.StaxJobFactory;
import org.ow2.proactive.scheduler.common.task.TaskInfo;
import org.ow2.proactive.scheduler.common.task.TaskStatus;

import functionaltests.utils.SchedulerFunctionalTestWithRestart;


public class TestPropagatedVariableResolution extends SchedulerFunctionalTestWithRestart {

    private static URL job_desc = TestModifyPropagatedVariables.class.getClassLoader()
                                                                     .getResource("workflow/descriptors/flow_simple_unresolved_variables.xml");

    @Test
    public void testPropagatedVariableResolution() throws Throwable {
        TaskFlowJob job = (TaskFlowJob) StaxJobFactory.getFactory().createJob(absolutePath(job_desc));
        JobId id = schedulerHelper.submitJob(job);

        TaskInfo taskInfo = schedulerHelper.waitForEventTaskFinished(id, "Groovy_Task2");
        //assert the task finished correctly
        assertEquals(TaskStatus.FINISHED, taskInfo.getStatus());

        //for the first task:
        String jobLog = schedulerHelper.getTaskResult(id, "Groovy_Task").getOutput().getAllLogs();
        //1- resolve reference to job variable
        Assert.assertThat(jobLog, CoreMatchers.containsString("TESTVAR=var_" + id.toString()));
        //2- resolve reference to another task variable
        Assert.assertThat(jobLog, CoreMatchers.containsString("TO_RESOLVE_IN_TASK=hello_world"));

        //for the second task:
        jobLog = schedulerHelper.getTaskResult(id, "Groovy_Task2").getOutput().getAllLogs();
        //3- resolve reference to previous task variable
        Assert.assertThat(jobLog, CoreMatchers.containsString("TESTVAR_NEW=var_" + id.toString()));
    }

    private String absolutePath(URL file) throws Exception {
        File temp = new File(file.toURI());
        return temp.getAbsolutePath();
    }
}
