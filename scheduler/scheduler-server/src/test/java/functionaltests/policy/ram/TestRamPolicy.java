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
package functionaltests.policy.ram;

import static functionaltests.utils.SchedulerTHelper.log;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobResult;

import functionaltests.utils.SchedulerFunctionalTestRamPolicy;


public class TestRamPolicy extends SchedulerFunctionalTestRamPolicy {

    private static URL jobDescriptor = TestRamPolicy.class.getResource("/functionaltests/descriptors/Job_simple_ram_policy.xml");

    @Test
    public void testRamPolicy() throws Throwable {

        Map<String, String> variables = new HashMap<String, String>();
        variables.put("MERGE_RAM", getHalfLocalRam());
        JobId jobId = schedulerHelper.submitJob(new File(jobDescriptor.toURI()).getAbsolutePath(), variables);

        log("Waiting for job finished");
        JobInfo jInfo = schedulerHelper.waitForEventJobFinished(jobId);

        JobResult res = schedulerHelper.getJobResult(jobId);

        assertThat(res.hadException(), is(false));

    }

    public String getHalfLocalRam() {
        long memorySize = ((com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean()).getTotalPhysicalMemorySize();

        return FileUtils.byteCountToDisplaySize(memorySize / 2).replaceAll(" GB", "");

    }

}
