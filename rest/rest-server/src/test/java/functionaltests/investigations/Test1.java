/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2015 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $PROACTIVE_INITIAL_DEV$
 */
package functionaltests.investigations;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.SchedulerState;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.JobStatus;

import com.google.common.collect.Iterables;

import functionaltests.AbstractRestFuncTestCase;
import functionaltests.RestFuncTHelper;


public class Test1 extends AbstractRestFuncTestCase {

    @BeforeClass
    public static void beforeClass() throws Exception {
        init();
    }

    @Before
    public void setUp() throws Exception {
        Scheduler scheduler = RestFuncTHelper.getScheduler();
        SchedulerState state = scheduler.getState();

        Iterable<JobState> jobs = Iterables.concat(state.getPendingJobs(), state.getRunningJobs(),
                state.getFinishedJobs());

        for (JobState jobState : jobs) {
            JobId jobId = jobState.getId();
            scheduler.killJob(jobId);
            scheduler.removeJob(jobId);

            System.out.println("setUp kill and remove " + jobId);
        }
    }

    @Test
    public void test() throws Exception {

        for (int i = 0; i < 100; i++) {
            System.out.println("Starting iteration " + i);

            testKillJob();

            setUp();

            testJobResultValue();

            System.out.println("End of iteration " + i);
        }

    }

    public void testKillJob() throws Exception {
        String jobId = submitPendingJobId();
        String resource = "jobs/" + jobId + "/kill";
        String schedulerUrl = getResourceUrl(resource);
        HttpPut httpPut = new HttpPut(schedulerUrl);
        setSessionHeader(httpPut);
        HttpResponse response = executeUriRequest(httpPut);
        assertHttpStatusOK(response);
        JobState jobState = getScheduler().getJobState(jobId);
        assertEquals(JobStatus.KILLED, jobState.getStatus());
    }

    public void testJobResultValue() throws Exception {
        String jobId = submitFinishedJob();
        String resource = getResourceUrl("jobs/" + jobId + "/result/value");
        HttpGet httpGet = new HttpGet(resource);
        setSessionHeader(httpGet);
        HttpResponse response = executeUriRequest(httpGet);
        assertHttpStatusOK(response);
        JSONObject jsonObject = toJsonObject(response);
        assertEquals("TEST-JOB", jsonObject.get(getDefaultTaskName()).toString());
    }

}