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
package org.ow2.proactive_grid_cloud_portal.scheduler;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.ow2.proactive.scheduler.common.JobFilterCriteria;
import org.ow2.proactive.scheduler.common.Page;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.util.SchedulerProxyUserInterface;
import org.ow2.proactive.scheduler.job.JobIdImpl;
import org.ow2.proactive_grid_cloud_portal.RestTestServer;
import org.ow2.proactive_grid_cloud_portal.common.SchedulerRestInterface;
import org.ow2.proactive_grid_cloud_portal.common.SharedSessionStoreTestUtils;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.JobInfoData;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.RestPage;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.UserJobData;


/**
 * The [offset, limit[ boundaries and tag filtering are out of the scope of these tests
 * because their control is done in the <code>SchedulerDBManager</code> class.
 * We are only testing the paginated jobs format and the unitary job info resource.
 *
 */
public class SchedulerStateRestJobTest extends RestTestServer {

    private SchedulerRestInterface restInterface = null;

    private SchedulerProxyUserInterface mockOfScheduler = null;

    private String sessionId = null;

    @Before
    public void setUp() throws Throwable {
        restInterface = new SchedulerStateRest();
        mockOfScheduler = mock(SchedulerProxyUserInterface.class);
        sessionId = SharedSessionStoreTestUtils.createValidSession(mockOfScheduler);
    }

    @Test
    public void testChangeStartAt() throws Throwable {

        String JobId = "3";
        String startAt = "2017-07-07T00:00:00+01:00";

        when(mockOfScheduler.changeStartAt(JobIdImpl.makeJobId(JobId), startAt)).thenReturn(true);

        boolean startAtChanged = restInterface.changeStartAt(sessionId, JobId, startAt);

        assertThat(startAtChanged, is(true));

    }

    @Test
    public void testJobs() throws Throwable {

        Page<JobInfo> expectedJobs = RestTestUtils.newMockedJobInfoPage("1", null, 10, 50, 13);

        when(mockOfScheduler.getJobs(eq(-1),
                                     eq(-1),
                                     (JobFilterCriteria) notNull(),
                                     eq(SchedulerStateRest.DEFAULT_JOB_SORT_PARAMS))).thenReturn(expectedJobs);

        RestPage<String> actualPage = restInterface.jobs(sessionId, -1, -1);

        RestTestUtils.assertJobInfoPage(expectedJobs, actualPage);

    }

    @Test
    public void testJobsInfo() throws Throwable {
        Page<JobInfo> expectedJobs = RestTestUtils.newMockedJobInfoPage("2", null, 10, 50, 13);

        when(mockOfScheduler.getJobs(eq(-1),
                                     eq(-1),
                                     (JobFilterCriteria) notNull(),
                                     eq(SchedulerStateRest.DEFAULT_JOB_SORT_PARAMS))).thenReturn(expectedJobs);

        RestPage<UserJobData> actualPage = restInterface.jobsInfo(sessionId, -1, -1);

        RestTestUtils.assertJobsInfoPage(expectedJobs, actualPage);
    }

    @Test
    public void testJobInfo() throws Throwable {
        String JobId = "3";
        JobInfo jobInfo = RestTestUtils.newMockedJobInfo(JobId, null, 50);

        when(mockOfScheduler.getJobInfo("3")).thenReturn(jobInfo);

        JobInfoData jobInfoData = restInterface.jobInfo(sessionId, JobId);

        RestTestUtils.assertJobInfo(jobInfo, jobInfoData);

    }

}
