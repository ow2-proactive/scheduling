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
package functionaltests.scripts;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;
import java.util.Map;

import org.junit.Test;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.job.factories.StaxJobFactory;
import org.ow2.proactive.scheduler.common.task.TaskLogs;

import functionaltests.utils.SchedulerFunctionalTestNoRestart;


/**
 * This test checks that task variables are available from task scope but not propagated
 */
public class TestTaskScriptVariables extends SchedulerFunctionalTestNoRestart {

    private static URL jobDescriptor = TestTaskScriptVariables.class.getResource("/functionaltests/descriptors/Task_script_variables.xml");

    @Test
    public void testTaskVariables() throws Throwable {
        TaskFlowJob job = (TaskFlowJob) StaxJobFactory.getFactory()
                                                      .createJob(new File(jobDescriptor.toURI()).getAbsolutePath());

        JobId id = schedulerHelper.submitJob(job);
        schedulerHelper.waitForEventJobFinished(id);

        //Wait for cleaning scripts to be executed
        Thread.sleep(2000);

        //Get logs and task output
        String logs = schedulerHelper.getJobServerLogs(id);
        TaskLogs outputVariables = schedulerHelper.getTaskResult(id, "taskVariables").getOutput();
        String[] outputVariablesLines = outputVariables.getStdoutLogs(false).split(System.lineSeparator());
        TaskLogs outputChild = schedulerHelper.getTaskResult(id, "childTask").getOutput();
        String[] outputChildLines = outputChild.getStdoutLogs(false).split(System.lineSeparator());
        TaskLogs outputInherited = schedulerHelper.getTaskResult(id, "inheritedVariablesTask").getOutput();
        String[] outputInheritedlines = outputInherited.getStdoutLogs(false).split(System.lineSeparator());
        TaskLogs outputNoVariables = schedulerHelper.getTaskResult(id, "taskNoVariables").getOutput();
        String[] outputNoVariablesLines = outputNoVariables.getStdoutLogs(false).split(System.lineSeparator());

        //Tests variable access
        assertEquals("testvarjob3", job.getVariables().get("TESTVAR3").getValue());
        //Selection files access
        assertTrue(logs.contains("testvartask0"));

        //Fork environment, pre, scriptExecutable, post scripts 
        assertEquals(5, outputVariablesLines.length);
        assertEquals("testvartask1", outputVariablesLines[0]);
        assertEquals("testvartask2", outputVariablesLines[1]);
        assertEquals("testvartask3", outputVariablesLines[2]);
        assertEquals("testvartask4", outputVariablesLines[3]);
        //Inherited value
        assertEquals("testvarjob6", outputVariablesLines[4]);

        //Test that child tasks don't have access to task variables
        assertEquals(8, outputChildLines.length);
        assertEquals("testvarjob0", outputChildLines[0]);
        assertEquals("testvarjob1", outputChildLines[1]);
        assertEquals("testvarjob2", outputChildLines[2]);
        assertEquals("testvarjob3", outputChildLines[3]);
        assertEquals("testvarjob4", outputChildLines[4]);
        assertEquals("testvarjob5", outputChildLines[5]);
        assertEquals("testvarjob6", outputChildLines[6]);
        assertEquals("testvar7modified", outputChildLines[7]);

        //Test that inherited variables are propagated correctly
        assertEquals(6, outputInheritedlines.length);
        assertEquals("testvartask0", outputInheritedlines[0]);
        assertEquals("testvarjob1", outputInheritedlines[1]);
        assertEquals("testvartask7", outputInheritedlines[2]);
        assertEquals("testvar8propagated", outputInheritedlines[3]);
        assertEquals("testvartask9", outputInheritedlines[4]);
        assertEquals("testvartask10", outputInheritedlines[5]);

        //Test that other tasks don't have access to task variables
        assertEquals(8, outputNoVariablesLines.length);
        assertEquals("testvarjob0", outputNoVariablesLines[0]);
        assertEquals("testvarjob1", outputNoVariablesLines[1]);
        assertEquals("testvarjob2", outputNoVariablesLines[2]);
        assertEquals("testvarjob3", outputNoVariablesLines[3]);
        assertEquals("testvarjob4", outputNoVariablesLines[4]);
        assertEquals("testvarjob5", outputNoVariablesLines[5]);
        assertEquals("testvarjob6", outputNoVariablesLines[6]);
        assertEquals("testvarjob7", outputNoVariablesLines[7]);

        //Cleaning script
        assertTrue(logs.contains("(taskVariables) testvartask5"));

        //Variables use into generic information
        Map<String, String> genericInformations = job.getGenericInformation();
        assertEquals(1, genericInformations.size());
        assertEquals("testvarjob0", genericInformations.get("jobGI"));

        genericInformations = job.getTask("taskVariables").getGenericInformation();
        assertEquals(1, genericInformations.size());
        assertEquals("testvartask1", genericInformations.get("taskVariablesGI"));

        genericInformations = job.getTask("childTask").getGenericInformation();
        assertEquals(1, genericInformations.size());
        assertEquals("testvarjob4", genericInformations.get("childTaskGI"));

        genericInformations = job.getTask("taskNoVariables").getGenericInformation();
        assertEquals(1, genericInformations.size());
        assertEquals("testvarjob2", genericInformations.get("taskNoVariablesGI"));

        //Variables use into node configuration
        assertEquals(2, job.getTask("taskVariables").getParallelEnvironment().getNodesNumber());
        assertEquals(1, job.getTask("childTask").getParallelEnvironment().getNodesNumber());
        assertEquals(1, job.getTask("taskNoVariables").getParallelEnvironment().getNodesNumber());
    }

}
