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
package functionaltests.job;

import static functionaltests.utils.SchedulerTHelper.log;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

import org.junit.Test;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobLabelInfo;

import functionaltests.utils.SchedulerFunctionalTestNoRestart;


/**
 * Test checks operations on job labels
 */
public class TestJobLabels extends SchedulerFunctionalTestNoRestart {

    private static URL runningJob = TestJobRemoved.class.getResource("/functionaltests/descriptors/Job_running.xml");

    private final static int EVENT_TIMEOUT = 5000;

    @Test
    public void testJobLabels() throws Throwable {

        log("Create labels label, label2");
        java.util.List<String> labels = Arrays.asList("label1", "label2");
        java.util.List<JobLabelInfo> jobLabelInfoList = schedulerHelper.createLabels(labels);
        assertEquals(2, jobLabelInfoList.size());
        assertNotNull(jobLabelInfoList.stream()
                                      .filter(jobLabelInfo -> jobLabelInfo.getLabel().equals("label1"))
                                      .collect(Collectors.toList()));
        assertNotNull(jobLabelInfoList.stream()
                                      .filter(jobLabelInfo -> jobLabelInfo.getLabel().equals("label2"))
                                      .collect(Collectors.toList()));

        log("Update label newLabel");
        JobLabelInfo jobLabelInfo = schedulerHelper.updateLabel(String.valueOf(jobLabelInfoList.get(0).getId()),
                                                                "newLabel");
        assertEquals("newLabel", jobLabelInfo.getLabel());

        log("Delete label newLabel");
        schedulerHelper.deleteLabel(String.valueOf(jobLabelInfoList.get(0).getId()));
        java.util.List<JobLabelInfo> actualJobLabels = schedulerHelper.getLabels();
        assertEquals(1, actualJobLabels.size());

        log("Set labels newLabel1, newLabel2");
        java.util.List<String> updatedLabels = Arrays.asList("newLabel1", "newLabel2");
        java.util.List<JobLabelInfo> updatedJobLabels = schedulerHelper.setLabels(updatedLabels);
        assertEquals(2, updatedJobLabels.size());
        assertNotNull(updatedJobLabels.stream()
                                      .filter(jobLabel -> jobLabel.getLabel().equals("newLabel1"))
                                      .collect(Collectors.toList()));
        assertNotNull(updatedJobLabels.stream()
                                      .filter(jobLabel -> jobLabel.getLabel().equals("newLabel2"))
                                      .collect(Collectors.toList()));

        log("Set label newLabel1 on job");
        JobId id = schedulerHelper.submitJob(new File(runningJob.toURI()).getAbsolutePath());
        schedulerHelper.setLabelOnJob(String.valueOf(updatedJobLabels.get(0).getId()),
                                      Collections.singletonList(id.value()));

        JobInfo jobInfo = schedulerHelper.getJobInfo(id.toString());
        assertEquals("newLabel1", jobInfo.getLabel());

        log("Remove label from job");
        schedulerHelper.removeLabelFromJob(Collections.singletonList(id.value()));
        JobInfo updatedJobInfo = schedulerHelper.getJobInfo(id.toString());
        assertNull(updatedJobInfo.getLabel());

        // removing running job
        schedulerHelper.removeJob(id);
        // it should kill the job
        schedulerHelper.waitForEventJobFinished(id);

        schedulerHelper.waitForEventJobRemoved(id, EVENT_TIMEOUT);
    }

}
