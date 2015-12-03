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
package functionaltests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.SchedulerState;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobPriority;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.JobStatus;

import functionaltests.jobs.NonTerminatingJob;


/**
 * Sanity test against scheduler REST requests supporting pagination:
 * <ul>
 * <li>jobs
 * <li>jobsinfo
 * <li>revisionjobsinfo
 * </ul> 
 */
public class RestSchedulerJobPaginationTest extends AbstractRestFuncTestCase {

    @BeforeClass
    public static void beforeClass() throws Exception {
        init();
    }

    @Before
    public void setUp() throws Exception {
        Scheduler scheduler = RestFuncTHelper.getScheduler();
        SchedulerState state = scheduler.getState();
        List<JobState> jobStates = new ArrayList<>();
        jobStates.addAll(state.getPendingJobs());
        jobStates.addAll(state.getRunningJobs());
        jobStates.addAll(state.getFinishedJobs());
        for (JobState jobState : jobStates) {
            JobId jobId = jobState.getId();
            scheduler.killJob(jobId);
            scheduler.removeJob(jobId);
        }
    }

    @Test
    public void testJobsPagination() throws Exception {
        Assert.assertEquals("Test expects only one node", 1, RestFuncTHelper.defaultNumberOfNodes);

        JobId jobId1 = getScheduler().submit(createJob(NonTerminatingJob.class));
        waitJobState(jobId1, JobStatus.RUNNING, 30000);

        JobId jobId2 = submitDefaultJob();
        JobId jobId3 = submitDefaultJob();

        checkPagingRequests1();

        // check 'jobsinfo' and 'revisionjobsinfo' provide job's attributes

        JSONObject page;
        JSONArray jobs;

        page = getRequestJSONObject(getResourceUrl("jobsinfo"));
        jobs = (JSONArray) page.get("list");
        checkJob((JSONObject) jobs.get(2), JobStatus.RUNNING, 1, 0);

        page = getRequestJSONObject(getResourceUrl("revisionjobsinfo"));
        JSONObject map = (JSONObject) page.get("map");
        Assert.assertEquals(1, map.keySet().size());
        jobs = (JSONArray) map.get(map.keySet().iterator().next());
        checkJob((JSONObject) jobs.get(2), JobStatus.RUNNING, 1, 0);

        checkFiltering1();

        String resource = "jobs/" + jobId1.value() + "/kill";
        String schedulerUrl = getResourceUrl(resource);
        HttpPut httpPut = new HttpPut(schedulerUrl);
        setSessionHeader(httpPut);
        HttpResponse response = executeUriRequest(httpPut);
        assertHttpStatusOK(response);

        waitJobState(jobId1, JobStatus.KILLED, 30000);
        waitJobState(jobId2, JobStatus.FINISHED, 30000);
        waitJobState(jobId3, JobStatus.FINISHED, 30000);

        checkFiltering2();

        // check 'jobsinfo' and 'revisionjobsinfo' provide updated job's attributes

        JSONObject job = null;

        page = getRequestJSONObject(getResourceUrl("jobsinfo"));
        jobs = (JSONArray) page.get("list");
        checkJob(findJob("1", jobs), JobStatus.KILLED, 0, 0);

        page = getRequestJSONObject(getResourceUrl("revisionjobsinfo"));
        map = (JSONObject) page.get("map");
        Assert.assertEquals(1, map.keySet().size());
        jobs = (JSONArray) map.get(map.keySet().iterator().next());
        checkJob(findJob("1", jobs), JobStatus.KILLED, 0, 0);

        checkPagingRequests2();
    }

    private JSONObject findJob(String id, JSONArray jobs) {
        for (int i = 0; i < jobs.size(); i++) {
            if (((JSONObject) jobs.get(i)).get("jobid").equals(id)) {
                return (JSONObject) jobs.get(i);
            }
        }
        Assert.fail("Failed to find job " + id + ", all jobs: " + jobs);
        return null;
    }

    void checkFiltering1() throws Exception {
        boolean pending;
        boolean running;
        boolean finished;

        String url;

        pending = true;
        running = true;
        finished = true;
        url = getResourceUrl("revisionjobsinfo?pending=" + pending + "&running=" + running + "&finished=" +
            finished);
        checkRevisionJobsInfo(url, "1", "2", "3");

        pending = false;
        running = true;
        finished = true;
        url = getResourceUrl("revisionjobsinfo?pending=" + pending + "&running=" + running + "&finished=" +
            finished);
        checkRevisionJobsInfo(url, "1");

        pending = true;
        running = false;
        finished = true;
        url = getResourceUrl("revisionjobsinfo?pending=" + pending + "&running=" + running + "&finished=" +
            finished);
        checkRevisionJobsInfo(url, "2", "3");

        pending = false;
        running = false;
        finished = true;
        url = getResourceUrl("revisionjobsinfo?pending=" + pending + "&running=" + running + "&finished=" +
            finished);
        checkRevisionJobsInfo(url);
    }

    void checkFiltering2() throws Exception {
        boolean pending;
        boolean running;
        boolean finished;

        String url;

        pending = true;
        running = true;
        finished = true;
        url = getResourceUrl("revisionjobsinfo?pending=" + pending + "&running=" + running + "&finished=" +
            finished);
        checkRevisionJobsInfo(url, "1", "2", "3");

        pending = false;
        running = false;
        finished = true;
        url = getResourceUrl("revisionjobsinfo?pending=" + pending + "&running=" + running + "&finished=" +
            finished);
        checkRevisionJobsInfo(url, "1", "2", "3");

        pending = true;
        running = true;
        finished = false;
        url = getResourceUrl("revisionjobsinfo?pending=" + pending + "&running=" + running + "&finished=" +
            finished);
        checkRevisionJobsInfo(url);
    }

    private void checkJob(JSONObject job, JobStatus status, int runningTasks, int finishedTasks)
            throws Exception {
        Assert.assertEquals("1", job.get("jobid"));
        Assert.assertEquals(getLogin(), job.get("jobOwner"));
        JSONObject jobInfo = (JSONObject) job.get("jobInfo");
        Assert.assertEquals(status.name(), jobInfo.get("status"));
        Assert.assertEquals(JobPriority.NORMAL.name(), jobInfo.get("priority"));
        Assert.assertEquals((long) runningTasks, jobInfo.get("numberOfRunningTasks"));
        Assert.assertEquals((long) finishedTasks, jobInfo.get("numberOfFinishedTasks"));
        Assert.assertEquals((long) 0, jobInfo.get("numberOfPendingTasks"));
        Assert.assertEquals((long) 1, jobInfo.get("totalNumberOfTasks"));
        Assert.assertEquals((long) -1, jobInfo.get("removedTime"));
    }

    JSONArray getRequestJSONArray(String url) throws Exception {
        HttpGet httpGet = new HttpGet(url);
        setSessionHeader(httpGet);
        HttpResponse response = executeUriRequest(httpGet);
        assertHttpStatusOK(response);
        return toJsonArray(response);
    }

    JSONObject getRequestJSONObject(String url) throws Exception {
        HttpGet httpGet = new HttpGet(url);
        setSessionHeader(httpGet);
        HttpResponse response = executeUriRequest(httpGet);
        assertHttpStatusOK(response);
        return toJsonObject(response);
    }

    private void checkPagingRequests1() throws Exception {
        checkJobIds(false, 0, 0, "3", "2", "1");
        checkJobIds(true, 0, 10, "3", "2", "1");
        checkJobIds(true, 0, 2, "3", "2");
        checkJobIds(true, 2, 10, "1");
    }

    private void checkPagingRequests2() throws Exception {
        checkJobIds(false, 0, 0, "3", "2", "1");
        checkJobIds(true, 0, 10, "3", "2", "1");
    }

    private void checkJobIds(boolean indexAndRange, int index, int limit, String... expectedIds)
            throws Exception {
        JSONObject page;
        JSONArray jobs;

        Set<String> expected = new HashSet<>(Arrays.asList(expectedIds));
        Set<String> actual = new HashSet<>();

        String url;

        // test 'jobs' request
        if (indexAndRange) {
            url = getResourceUrl("jobs?index=" + index + "&limit=" + limit);
        } else {
            url = getResourceUrl("jobs");
        }
        page = getRequestJSONObject(url);
        jobs = (JSONArray) page.get("list");
        for (int i = 0; i < jobs.size(); i++) {
            actual.add((String) jobs.get(i));
        }

        Assert.assertEquals("Unexpected result of 'jobs' request (" + url + ")", expected, actual);

        // test 'jobsinfo' request
        if (indexAndRange) {
            url = getResourceUrl("jobsinfo?index=" + index + "&limit=" + limit);
        } else {
            url = getResourceUrl("jobsinfo");
        }
        page = getRequestJSONObject(url);
        jobs = (JSONArray) page.get("list");
        actual.clear();
        for (int i = 0; i < jobs.size(); i++) {
            JSONObject job = (JSONObject) jobs.get(i);
            actual.add((String) job.get("jobid"));
        }
        Assert.assertEquals("Unexpected result of 'jobsinfo' request (" + url + ")", expected, actual);

        // test 'revisionjobsinfo' request
        if (indexAndRange) {
            url = getResourceUrl("revisionjobsinfo?index=" + index + "&limit=" + limit);
        } else {
            url = getResourceUrl("revisionjobsinfo");
        }
        checkRevisionJobsInfo(url, expectedIds);
    }

    private void checkRevisionJobsInfo(String url, String... expectedIds) throws Exception {
        Set<String> expected = new HashSet<>(Arrays.asList(expectedIds));
        Set<String> actual = new HashSet<>();

        JSONObject page = getRequestJSONObject(url);
        JSONObject map = (JSONObject) page.get("map");
        Assert.assertEquals(1, map.keySet().size());
        JSONArray jobs = (JSONArray) map.get(map.keySet().iterator().next());
        for (int i = 0; i < jobs.size(); i++) {
            JSONObject job = (JSONObject) jobs.get(i);
            actual.add((String) job.get("jobid"));
        }
        Assert
                .assertEquals("Unexpected result of 'revisionjobsinfo' request (" + url + ")", expected,
                        actual);
    }

}