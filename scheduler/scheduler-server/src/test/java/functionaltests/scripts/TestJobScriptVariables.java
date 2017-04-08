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
import java.net.URL;

import org.junit.BeforeClass;
import org.junit.Test;

import functionaltests.utils.SchedulerFunctionalTestWithCustomConfigAndRestart;
import functionaltests.utils.SchedulerTHelper;


/**
 * This test checks that variable bindings are available and correctly set in various scripts (pre/post/fork/task)
 */
public class TestJobScriptVariables extends SchedulerFunctionalTestWithCustomConfigAndRestart {

    static URL configFile = TestJobScriptVariables.class.getResource("/functionaltests/scripts/schedulerPropertiesCustomSchedulerRestUrl.ini");

    private static URL jobDescriptor = TestJobScriptVariables.class.getResource("/functionaltests/descriptors/Job_script_variables.xml");

    @BeforeClass
    public static void before() throws Throwable {
        File propertiesfile = new File(configFile.toURI());
        schedulerHelper = new SchedulerTHelper(true, propertiesfile.getAbsolutePath());
    }

    @Test
    public void testJobScriptVariables() throws Throwable {
        schedulerHelper.testJobSubmission(new File(jobDescriptor.toURI()).getAbsolutePath(), true);
        schedulerHelper.checkNodesAreClean();
    }

}
