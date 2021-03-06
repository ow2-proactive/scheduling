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

import static functionaltests.utils.SchedulerTHelper.setExecutable;
import static org.objectweb.proactive.utils.OperatingSystem.unix;

import java.io.File;
import java.net.URL;

import org.junit.Assert;
import org.junit.Test;
import org.objectweb.proactive.utils.OperatingSystem;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobResult;

import functionaltests.utils.SchedulerFunctionalTestNoRestart;


public class TestModifyPropagatedVariables extends SchedulerFunctionalTestNoRestart {

    private static URL job_desc = TestModifyPropagatedVariables.class.getResource("/functionaltests/descriptors/Job_modify_propagated_vars.xml");

    private static URL job_desc2 = TestModifyPropagatedVariables.class.getResource("/functionaltests/descriptors/Job_modify_propagated_vars_2.xml");

    private static URL job_desc_unix = TestModifyPropagatedVariables.class.getResource("/functionaltests/descriptors/Job_modify_propagated_vars_on_unix.xml");

    private static URL unix_sh = TestModifyPropagatedVariables.class.getResource("/functionaltests/vars/test-vars.sh");

    @Test
    public void testModifyPropagatedVariables() throws Throwable {
        schedulerHelper.testJobSubmission(absolutePath(job_desc));
        OperatingSystem os = OperatingSystem.getOperatingSystem();
        if (unix == os) {
            setExecutable(absolutePath(unix_sh));
            schedulerHelper.testJobSubmission(absolutePath(job_desc_unix));
        }
    }

    @Test
    public void testModifyPropagatedVariables2() throws Throwable {
        JobId jobid = schedulerHelper.testJobSubmission(absolutePath(job_desc2));
        JobResult result = schedulerHelper.getJobResult(jobid);
        System.out.println(result.getPreciousResults().get("task2").getOutput().getAllLogs());
        Assert.assertEquals("propagated-value", result.getPreciousResults().get("task2").getValue());

    }

    private String absolutePath(URL file) throws Exception {
        return ((new File(file.toURI())).getAbsolutePath());
    }
}
