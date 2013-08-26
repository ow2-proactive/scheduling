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

import java.io.File;
import java.net.URL;

import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.job.factories.JobFactory_stax;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.TaskState;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class TestScriptTask extends SchedulerConsecutive {

    private static URL jobDescriptor = TestScriptTask.class
            .getResource("/functionaltests/descriptors/Job_script_task.xml");

    @Test
    public void forkedTasks() throws Throwable {
        TaskFlowJob job = (TaskFlowJob) JobFactory_stax.getFactory().createJob(
                new File(jobDescriptor.toURI()).getAbsolutePath());

        JobId id = SchedulerTHelper.submitJob(job);
        SchedulerTHelper.waitForEventJobFinished(id);
        JobResult jobResult = SchedulerTHelper.getJobResult(id);

        // Hello Work script task
        TaskResult simpleTaskResult = jobResult.getResult("simple");
        assertEquals(true, simpleTaskResult.value());
        assertTrue(simpleTaskResult.getOutput().getAllLogs(false).contains("hello"));

        // return binding should be used as task result
        TaskResult returnTaskResult = jobResult.getResult("return");
        assertEquals("42", returnTaskResult.value().toString());

        // results binding should be avaible in dependent tasks
        TaskResult resultFromDependentTaskTaskResult = jobResult.getResult("results_from_dependent_task");
        assertEquals("42", resultFromDependentTaskTaskResult.value().toString());

        // pas properties are exposed in the script task
        TaskResult propertiesTaskResult = jobResult.getResult("properties");
        String logs = propertiesTaskResult.getOutput().getAllLogs(false);
        assertTrue(logs.contains("pas.job.id=" + jobResult.getJobId().value()));
        assertTrue(logs.contains("pas.job.name=" + jobResult.getName()));
        assertTrue(logs.contains("pas.task.id=" + propertiesTaskResult.getTaskId().value()));
        assertTrue(logs.contains("pas.task.name=properties"));
        assertTrue(logs.contains("pas.task.iteration=0"));
        assertTrue(logs.contains("pas.task.replication=0"));

        // the script can be a file
        TaskResult fileTaskResult = jobResult.getResult("file");
        assertTrue(fileTaskResult.getOutput().getAllLogs(false).contains("Beginning of clean script"));

        TaskResult fileAndArgsTaskResult = jobResult.getResult("file_and_args");
        assertTrue(fileAndArgsTaskResult.getOutput().getAllLogs(false).contains("My_Magic_Arg"));

        // dataspaces binding should be available
        TaskResult dataspacesTaskResult = jobResult.getResult("dataspaces");
        String dataspacesLogs = dataspacesTaskResult.getOutput().getAllLogs(false);
        assertTrue(dataspacesLogs.contains("localspace=vfs://"));
        assertTrue(dataspacesLogs.contains("global=vfs://"));
        assertTrue(dataspacesLogs.contains("user=vfs://"));
        assertTrue(dataspacesLogs.contains("input=vfs://"));
        assertTrue(dataspacesLogs.contains("output=vfs://"));

        // script task should be forked by default
        JobState jobState = SchedulerTHelper.getSchedulerInterface().getJobState(id);
        TaskResult killJVMTaskResult = jobResult.getResult("killJVM");
        TaskState killJVMTaskState = jobState.getHMTasks().get(killJVMTaskResult.getTaskId());
        System.out.println(killJVMTaskState.getStatus());

    }

}
