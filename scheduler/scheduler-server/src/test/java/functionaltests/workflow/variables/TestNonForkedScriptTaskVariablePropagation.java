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

import java.io.File;
import java.net.URL;

import org.junit.Test;

import functionaltests.utils.SchedulerFunctionalTestNonForkedModeNoRestart;


public class TestNonForkedScriptTaskVariablePropagation extends SchedulerFunctionalTestNonForkedModeNoRestart {
    private static final long five_minutes = 5 * 60 * 1000;

    private static String jobDescPath;

    @Test(timeout = five_minutes)
    public void propagateVariables_byNonForkedScriptTask_shouldSucceed() throws Throwable {
        jobDescPath = absolutePath(TestNonForkedScriptTaskVariablePropagation.class.getResource("/functionaltests/descriptors/Job_variable_propagation_with_non_forked_script_task.xml"));
        schedulerHelper.testJobSubmission(jobDescPath);
    }

    private static String absolutePath(URL url) throws Exception {
        return (new File(url.toURI())).getAbsolutePath();
    }
}
