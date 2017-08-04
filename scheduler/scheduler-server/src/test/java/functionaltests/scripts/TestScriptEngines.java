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

import static functionaltests.utils.SchedulerTHelper.log;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeFalse;
import static org.junit.Assume.assumeTrue;

import java.io.File;
import java.net.URL;
import java.util.Map;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import org.objectweb.proactive.utils.OperatingSystem;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.task.TaskResult;

import functionaltests.utils.SchedulerFunctionalTestNoRestart;


/**
 * TestScriptEngines
 *
 * This test checks that scripts from all used script engines can be executed
 *
 * @author The ProActive Team
 */
public class TestScriptEngines extends SchedulerFunctionalTestNoRestart {

    private static String jobDescriptorsLoc = "JobScriptEngines.xml";

    private static String jobDescriptorsLocWindows = "JobScriptEnginesWindowsOnly.xml";

    private static String jobDescriptorsLocLinux = "JobScriptEnginesLinuxOnly.xml";

    private URL jobDescriptor = TestScriptEngines.class.getResource("/functionaltests/scripts/" + jobDescriptorsLoc);

    private URL jobDescriptorWindows = TestScriptEngines.class.getResource("/functionaltests/scripts/" +
                                                                           jobDescriptorsLocWindows);

    private URL jobDescriptorLinux = TestScriptEngines.class.getResource("/functionaltests/scripts/" +
                                                                         jobDescriptorsLocLinux);

    @Test
    public void testScriptEngines() throws Throwable {
        testScriptEnginesWithGivenWorkflow(jobDescriptor, 4);
    }

    @Test
    public void testScriptEnginesLinuxOnly() throws Throwable {
        assumeFalse(OperatingSystem.getOperatingSystem() == OperatingSystem.windows);
        testScriptEnginesWithGivenWorkflow(jobDescriptorLinux, 1);
    }

    @Test
    public void testScriptEnginesWindowsOnly() throws Throwable {
        assumeTrue(OperatingSystem.getOperatingSystem() == OperatingSystem.windows);
        testScriptEnginesWithGivenWorkflow(jobDescriptorWindows, 2);
    }

    private void testScriptEnginesWithGivenWorkflow(URL workflow, int numberOfTasks) throws Throwable {
        log("Testing submission of job descriptor : " + workflow);
        JobId id = schedulerHelper.testJobSubmission(new File(workflow.toURI()).getAbsolutePath());

        // check result are not null
        JobResult res = schedulerHelper.getJobResult(id);
        assertFalse("Had Exception : " + workflow.toString(), schedulerHelper.getJobResult(id).hadException());

        Assert.assertEquals(numberOfTasks, res.getAllResults().size());
        for (Map.Entry<String, TaskResult> entry : res.getAllResults().entrySet()) {

            assertFalse("Had Exception : " + entry.getKey(), entry.getValue().hadException());

            assertNotNull("Result not null : " + entry.getKey(), entry.getValue().value());

            assertTrue("Hello World in Output : " + entry.getKey(),
                       entry.getValue().getOutput().getAllLogs(false).contains("Hello World"));
        }

        schedulerHelper.removeJob(id);
        schedulerHelper.waitForEventJobRemoved(id);
    }
}
