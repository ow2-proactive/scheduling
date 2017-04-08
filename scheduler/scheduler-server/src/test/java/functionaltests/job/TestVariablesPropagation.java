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
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.net.URL;

import org.junit.BeforeClass;
import org.junit.Test;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.util.SerializationUtil;

import functionaltests.utils.SchedulerFunctionalTestWithCustomConfigAndRestart;
import functionaltests.utils.SchedulerTHelper;


/**
 * This class tests the preempt task, restart task, and kill task features.
 *
 * Submit a taskflow job with 4 tasks.
 * One has 4 max number of executions
 * one is failJobOnError
 * Preempt task must : (test 1)
 * - stop execution
 * - restart later without side effect
 * Restart task must :
 * - stop execution (ends like a normal termination with TaskRestartedException)
 * - restart later if possible, fails the job if 'failJobOnError'
 * Kill task must :
 * - stop execution
 * - not restart this task, fails the job if 'failJobOnError'
 *
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 3.0
 */
public class TestVariablesPropagation extends SchedulerFunctionalTestWithCustomConfigAndRestart {

    private static URL jobDescriptor = TestVariablesPropagation.class.getResource("/functionaltests/descriptors/Job_variables_propagation.xml");

    private static URL configFile = TestVariablesPropagation.class.getResource("/functionaltests/config/schedulerPropertiesNoRetry.ini");

    private static String variableName = "variableName";

    private static String variableValue = "variableValue";

    @BeforeClass
    public static void startSchedulerInAnyCase() throws Exception {
        SchedulerTHelper.log("Starting a clean scheduler.");
        schedulerHelper = new SchedulerTHelper(true, configFile.getPath());
    }

    @Test
    public void testVariablesPropagation() throws Throwable {
        String jobDescriptorPath = new File(jobDescriptor.toURI()).getAbsolutePath();
        testVariablesPropagation(jobDescriptorPath);
    }

    private void testVariablesPropagation(String jobDescriptorPath) throws Exception {
        log("Submitting job");
        log(schedulerHelper.getSchedulerInterface().getClass().toString());

        schedulerHelper.addExtraNodes(3);

        JobId id = schedulerHelper.submitJob(jobDescriptorPath);
        log("Wait for event job submitted");
        schedulerHelper.waitForEventJobSubmitted(id);
        log("Wait for event t1 running");
        schedulerHelper.waitForEventTaskRunning(id, "t1");
        log("Wait for event t2 running");
        schedulerHelper.waitForEventTaskRunning(id, "t2");
        log("Wait for event t3 running");
        schedulerHelper.waitForEventTaskRunning(id, "t3");

        log("Preempt t1");
        schedulerHelper.getSchedulerInterface().preemptTask(id, "t1", 1);

        log("Restart t2");
        schedulerHelper.getSchedulerInterface().restartTask(id, "t2", 1);

        log("Kill t3");
        schedulerHelper.getSchedulerInterface().killTask(id, "t3");

        //Wait for finish
        log("Wait for event t1 finished");
        schedulerHelper.waitForEventTaskFinished(id, "t1");
        log("Wait for event t2 finished");
        schedulerHelper.waitForEventTaskFinished(id, "t2");
        log("Wait for event t3 finished");
        schedulerHelper.waitForEventTaskFinished(id, "t3");

        //task result for t1
        TaskResult tr1 = schedulerHelper.getSchedulerInterface().getTaskResult(id, "t1");
        //task result for t2
        TaskResult tr2 = schedulerHelper.getSchedulerInterface().getTaskResult(id, "t2");
        //task result for t3
        TaskResult tr3 = schedulerHelper.getSchedulerInterface().getTaskResult(id, "t3");

        //check result j1
        assertEquals(variableValue,
                     SerializationUtil.deserializeVariableMap(tr1.getPropagatedVariables()).get(variableName));
        //check result j2
        assertEquals(variableValue,
                     SerializationUtil.deserializeVariableMap(tr2.getPropagatedVariables()).get(variableName));
        //check result j3
        assertEquals(variableValue,
                     SerializationUtil.deserializeVariableMap(tr3.getPropagatedVariables()).get(variableName));
    }
}
