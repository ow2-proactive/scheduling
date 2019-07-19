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
package functionaltests.fork;

import org.junit.BeforeClass;
import org.junit.Test;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;

import functionaltests.utils.SchedulerFunctionalTestNonForkedModeNoRestart;
import functionaltests.utils.SchedulerTHelper;


/**
 * Test the task fork parameter while the global task fork mode is set to false.
 */
public class TestTaskForkWithGlobalConfigNonForked extends SchedulerFunctionalTestNonForkedModeNoRestart {
    private static ForkTaskTestHelper forkTaskTestHelper;

    @BeforeClass
    public static void initialize() {
        // initialize forkTaskTestHelper after the parent class start scheduler and initialize schedulerHelper
        forkTaskTestHelper = new ForkTaskTestHelper(schedulerHelper);
    }

    @Test
    public void testMissingForkParameterWithGlobalConfigNonForked() throws Exception {
        // test the job task which doesn't have the parameter fork
        forkTaskTestHelper.testTaskIsRunningInExpectedForkedMode("/functionaltests/descriptors/Job_MissingFork.xml",
                                                                 false);
    }

    @Test
    public void testTaskForkParameterShouldBeOverridedGlobalConfigNonForked() throws Exception {
        // test the job task which set the parameter fork, but it should be overrided by the global config
        forkTaskTestHelper.testTaskIsRunningInExpectedForkedMode("/functionaltests/descriptors/Job_Fork.xml", false);
    }
}
