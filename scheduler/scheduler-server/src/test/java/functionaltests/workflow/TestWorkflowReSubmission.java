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
package functionaltests.workflow;

import java.io.File;
import java.net.URL;
import java.util.HashMap;

import org.junit.Test;
import org.ow2.proactive.scheduler.common.job.JobId;

import functionaltests.utils.SchedulerFunctionalTestWithRestart;


public class TestWorkflowReSubmission extends SchedulerFunctionalTestWithRestart {

    private static URL jobDescriptor = TestWorkflowReSubmission.class.getResource("/functionaltests/descriptors/Job_5s.xml");

    @Test
    public void testValid() throws Throwable {

        final JobId jobId = schedulerHelper.submitJob(new File(jobDescriptor.toURI()).getAbsolutePath());

        final JobId jobId1 = schedulerHelper.getSchedulerInterface().reSubmit(jobId, new HashMap<>(), new HashMap<>());

        schedulerHelper.waitForEventJobFinished(jobId);

        schedulerHelper.waitForEventJobFinished(jobId1);

    }
}
