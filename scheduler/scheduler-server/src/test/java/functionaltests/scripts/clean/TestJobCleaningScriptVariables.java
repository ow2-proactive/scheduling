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
package functionaltests.scripts.clean;

import java.io.File;
import java.net.URL;
import java.util.HashMap;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.ow2.proactive.scheduler.common.job.JobId;

import functionaltests.utils.SchedulerFunctionalTestWithRestart;
import functionaltests.utils.SchedulerStartForFunctionalTest;


/**
 * This test checks that variable bindings are available and correctly set in cleaning scripts
 * If test fails : a file will be created in the cleaning script and you'll see
 * the content in the console
 * If test suceeds, nothing will appear
 * This is also a test for the PA_TASK_SUCCESS variable which indicates if a task has ended
 * with errors or not
 */
public class TestJobCleaningScriptVariables extends SchedulerFunctionalTestWithRestart {

    @Rule
    public TemporaryFolder tmpDir = new TemporaryFolder();

    private static URL jobDescriptor = TestJobCleaningScriptVariables.class.getResource("/functionaltests/descriptors/Job_cleaning_variables.xml");

    @Test
    public void testJobCleaningScriptVariables() throws Throwable {

        HashMap<String, String> variables = new HashMap<>();
        File ko = new File(tmpDir.getRoot(), "ko");

        variables.put("path", ko.toString());
        variables.put("test", "initialValue");

        JobId jobId = schedulerHelper.submitJob(new File(jobDescriptor.toURI()).getAbsolutePath(), variables);

        schedulerHelper.waitForEventJobFinished(jobId);

        while (schedulerHelper.getResourceManager()
                              .getState()
                              .getFreeNodesNumber() != SchedulerStartForFunctionalTest.RM_NODE_NUMBER)
            ;

        if (ko.exists()) {
            String content;
            content = FileUtils.readFileToString(ko);
            Assert.fail(content);
        }
    }
}
