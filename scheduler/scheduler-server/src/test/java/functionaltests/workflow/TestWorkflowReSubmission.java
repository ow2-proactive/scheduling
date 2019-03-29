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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.ow2.proactive.scheduler.common.job.JobId;

import functionaltests.utils.SchedulerFunctionalTestWithRestart;


public class TestWorkflowReSubmission extends SchedulerFunctionalTestWithRestart {

    private static URL jobDescriptor = TestWorkflowReSubmission.class.getResource("/functionaltests/descriptors/Job_5s.xml");

    @Test
    public void testEmptyVarsEmptyInfo() throws Throwable {

        JobId jobId = schedulerHelper.submitJob(new File(jobDescriptor.toURI()).getAbsolutePath());

        JobId jobId1 = schedulerHelper.getSchedulerInterface().reSubmit(jobId,
                                                                        Collections.emptyMap(),
                                                                        Collections.emptyMap());

        schedulerHelper.waitForEventJobFinished(jobId);
        schedulerHelper.waitForEventJobFinished(jobId1);

        String jobContent = schedulerHelper.getSchedulerInterface().getJobContent(jobId);
        String jobContent1 = schedulerHelper.getSchedulerInterface().getJobContent(jobId1);

        assertEquals(jobContent, jobContent1);
        assertFalse(jobContent.contains("<variables>"));
        assertFalse(jobContent.contains("<genericInformation>"));
    }

    @Test
    public void testNullVarsNullInfo() throws Throwable {

        JobId jobId = schedulerHelper.submitJob(new File(jobDescriptor.toURI()).getAbsolutePath());

        JobId jobId1 = schedulerHelper.getSchedulerInterface().reSubmit(jobId, null, null);

        schedulerHelper.waitForEventJobFinished(jobId);
        schedulerHelper.waitForEventJobFinished(jobId1);

        String jobContent = schedulerHelper.getSchedulerInterface().getJobContent(jobId);
        String jobContent1 = schedulerHelper.getSchedulerInterface().getJobContent(jobId1);

        assertEquals(jobContent, jobContent1);
        assertFalse(jobContent.contains("<variables>"));
        assertFalse(jobContent.contains("<genericInformation>"));
    }

    @Test
    public void testAddVars() throws Throwable {

        JobId jobId = schedulerHelper.submitJob(new File(jobDescriptor.toURI()).getAbsolutePath());

        Map<String, String> vars = new HashMap<>();
        vars.put("x", "50");
        JobId jobId1 = schedulerHelper.getSchedulerInterface().reSubmit(jobId, vars, null);

        schedulerHelper.waitForEventJobFinished(jobId);
        schedulerHelper.waitForEventJobFinished(jobId1);

        String jobContent = schedulerHelper.getSchedulerInterface().getJobContent(jobId);
        String jobContent1 = schedulerHelper.getSchedulerInterface().getJobContent(jobId1);

        assertFalse(jobContent.contains("<variables>"));
        assertFalse(jobContent.contains("<genericInformation>"));
        assertTrue(jobContent1.contains("<variables>"));
        assertFalse(jobContent1.contains("<genericInformation>"));
    }

    @Test
    public void testAddInfo() throws Throwable {

        JobId jobId = schedulerHelper.submitJob(new File(jobDescriptor.toURI()).getAbsolutePath());

        Map<String, String> info = new HashMap<>();
        info.put("x", "50");
        JobId jobId1 = schedulerHelper.getSchedulerInterface().reSubmit(jobId, null, info);

        schedulerHelper.waitForEventJobFinished(jobId);
        schedulerHelper.waitForEventJobFinished(jobId1);

        String jobContent = schedulerHelper.getSchedulerInterface().getJobContent(jobId);
        String jobContent1 = schedulerHelper.getSchedulerInterface().getJobContent(jobId1);

        assertFalse(jobContent.contains("<variables>"));
        assertFalse(jobContent.contains("<genericInformation>"));
        assertFalse(jobContent1.contains("<variables>"));
        assertTrue(jobContent1.contains("<genericInformation>"));
    }

    @Test
    public void testAddVarsAddInfo() throws Throwable {

        JobId jobId = schedulerHelper.submitJob(new File(jobDescriptor.toURI()).getAbsolutePath());

        Map<String, String> vars = new HashMap<>();
        vars.put("x", "50");
        JobId jobId1 = schedulerHelper.getSchedulerInterface().reSubmit(jobId, vars, vars);

        schedulerHelper.waitForEventJobFinished(jobId);
        schedulerHelper.waitForEventJobFinished(jobId1);

        String jobContent = schedulerHelper.getSchedulerInterface().getJobContent(jobId);
        String jobContent1 = schedulerHelper.getSchedulerInterface().getJobContent(jobId1);

        assertFalse(jobContent.contains("<variables>"));
        assertFalse(jobContent.contains("<genericInformation>"));
        assertTrue(jobContent1.contains("<variables>"));
        assertTrue(jobContent1.contains("<genericInformation>"));
    }
}
