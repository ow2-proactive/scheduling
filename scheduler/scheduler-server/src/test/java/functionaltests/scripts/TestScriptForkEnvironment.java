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

import java.io.File;

import org.junit.Test;
import org.ow2.proactive.scheduler.common.exception.JobCreationException;

import functionaltests.utils.SchedulerFunctionalTestNoRestart;


/**
 * Tests used to check that {@code forkEnvironment} element is allowed
 * in {@code task} element for Jobs Schema in version 3.3 but not longer
 * in {@code javaExecutable} element.
 */
public class TestScriptForkEnvironment extends SchedulerFunctionalTestNoRestart {

    @Test
    public void testValidForkEnvironmentPositionWithSchemaVersion3_3() throws Throwable {
        testJobSubmissionWithJobDefinedUsingSchema3_3("Job_script_task_fork_environment_valid.xml");
    }

    @Test(expected = JobCreationException.class)
    public void testInvalidForkEnvironmentPositionWithSchemaVersion3_3() throws Throwable {
        testJobSubmissionWithJobDefinedUsingSchema3_3("Job_script_task_fork_environment_invalid.xml");
    }

    private void testJobSubmissionWithJobDefinedUsingSchema3_3(String filename) throws Exception {
        schedulerHelper.testJobSubmission(new File(TestScriptForkEnvironment.class.getResource("/functionaltests/descriptors/" +
                                                                                               filename)
                                                                                  .toURI()).getAbsolutePath());
    }

}
