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
package functionaltests.scripts;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.job.factories.StaxJobFactory;
import org.ow2.proactive.scheduler.common.task.TaskLogs;

import functionaltests.utils.SchedulerFunctionalTestWithCustomConfigAndRestart;
import functionaltests.utils.SchedulerTHelper;


/**
 * This test checks that variable bindings are available and correctly set in various scripts (pre/post/fork/task)
 */
public class TestTaskScriptVariables extends SchedulerFunctionalTestWithCustomConfigAndRestart {

    static URL configFile = TestTaskScriptVariables.class
            .getResource("/functionaltests/scripts/schedulerPropertiesCustomSchedulerRestUrl.ini");

    private static URL jobDescriptor = TestTaskScriptVariables.class
            .getResource("/functionaltests/descriptors/Task_script_variables.xml");

    @BeforeClass
    public static void before() throws Throwable {
        File propertiesfile = new File(configFile.toURI());
        schedulerHelper = new SchedulerTHelper(true, propertiesfile.getAbsolutePath());
    }

    @Test
    public void testTaskVariables() throws Throwable {
        TaskFlowJob job = (TaskFlowJob) StaxJobFactory.getFactory().createJob(
                new File(jobDescriptor.toURI()).getAbsolutePath());
        
        JobId id = schedulerHelper.submitJob(job);
        schedulerHelper.waitForEventJobFinished(id);  

        //Wait for cleaning scripts to be executed
        Thread.sleep(2000);

        //Get logs and task output
        String logs = schedulerHelper.getJobServerLogs(id);
        String[] logsLines = logs.split(System.lineSeparator());
        TaskLogs outputVariables = schedulerHelper.getTaskResult(id, "taskVariables").getOutput();
        String[] outputVariablesLines = outputVariables.getStdoutLogs(false).split(System.lineSeparator());
        TaskLogs outputChild = schedulerHelper.getTaskResult(id, "childTask").getOutput();
        String[] outputChildLines = outputChild.getStdoutLogs(false).split(System.lineSeparator());
        TaskLogs outputNoVariables = schedulerHelper.getTaskResult(id, "taskNoVariables").getOutput();
        String[] outputNoVariablesLines = outputNoVariables.getStdoutLogs(false).split(System.lineSeparator());        

        //Tests variable access
        assertEquals("testvarjob3", job.getVariables().get("TESTVAR3"));
        //Selection files access
        assertTrue(logsMatch(logsLines, "testvartask0"));

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
        assertTrue(logsMatch(logsLines, ".*\\(taskVariables\\) testvartask5"));
        
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
    
    private boolean logsMatch(String[] logsLines, String pattern){
        for (String line: logsLines){
            if (line.matches(pattern)){
                return true;
            }
        }
        return false;
    }

}
