/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
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

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.SchedulerState;
import org.ow2.proactive.scheduler.common.job.Job;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.JobStatus;
import org.ow2.proactive.scheduler.common.task.TaskState;
import org.ow2.proactive_grid_cloud_portal.scheduler.WorkflowSubmitter;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Sandrine on 12/09/2015.
 */
public class RestSchedulerTagTest extends AbstractRestFuncTestCase{

    private static Scheduler scheduler;

    private static JobId jobId = null;


    @BeforeClass
    public static void beforeClass() throws Exception {
        init();
    }

    @Before
    public void setUp() throws Exception {
        if(jobId == null) {
            scheduler = RestFuncTHelper.getScheduler();
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


            //submit a job with a loop and out and err outputs
            System.out.println("submit a job with loop, out and err outputs");
            jobId = submitJob("flow_loop_out.xml");
        }
    }


    private JobId submitJob(String filename) throws Exception{
        File jobFile = new File(this.getClass().getResource("config/" + filename).toURI());
        WorkflowSubmitter submitter = new WorkflowSubmitter(scheduler);
        JobId id = submitter.submit(jobFile, new HashMap<String, String>());
        waitJobState(id, JobStatus.FINISHED, 100000);
        return id;
    }


    private HttpResponse sendRequest(String url) throws Exception{
        String schedulerUrl = getResourceUrl(url);
        HttpGet httpGet = new HttpGet(schedulerUrl);
        setSessionHeader(httpGet);
        HttpResponse response = executeUriRequest(httpGet);
        assertHttpStatusOK(response);
        return response;
    }


    @Test
    public void testTaskIdsByTag() throws Exception {
        HttpResponse response = sendRequest("jobs/" + jobId + "/tasks/tag/LOOP-T2-1");
        JSONArray jsonArray = toJsonArray(response);

        System.out.println(jsonArray.toJSONString());
        assertTrue(jsonArray.contains("T1#1"));
        assertTrue(jsonArray.contains("Print1#1"));
        assertTrue(jsonArray.contains("Print2#1"));
        assertTrue(jsonArray.contains("T2#1"));
        assertEquals(4, jsonArray.size());
    }


    @Test
    public void testTaskIdsByUnknownTag() throws Exception {
        HttpResponse response = sendRequest("jobs/" + jobId + "/tasks/tag/unknownTag");
        JSONArray jsonArray = toJsonArray(response);

        System.out.println(jsonArray.toJSONString());
        assertEquals(0, jsonArray.size());
    }


    @Test
    public void testTaskStatesByTag() throws Exception {
        HttpResponse response = sendRequest("jobs/" + jobId + "/taskstates/LOOP-T2-1");
        JSONArray jsonArray = toJsonArray(response);

        System.out.println(jsonArray.toJSONString());
        assertEquals(4, jsonArray.size());
    }


    @Test
    public void testTaskStatesByUnknownTag() throws Exception {
        HttpResponse response = sendRequest("jobs/" + jobId + "/taskstates/unknownTag");
        JSONArray jsonArray = toJsonArray(response);

        System.out.println(jsonArray.toJSONString());
        assertEquals(0, jsonArray.size());
    }


    @Test
    public void testTaskLogAllByTag() throws Exception {
        HttpResponse response = sendRequest("jobs/" + jobId + "/tasks/tag/LOOP-T2-1/result/log/all");
        String responseContent = getContent(response);

        System.out.println(responseContent);

        assertEquals(2, StringUtils.countMatches(responseContent, "Task 1 : Test STDERR"));
        assertEquals(2, StringUtils.countMatches(responseContent, "Task 1 : Test STDOUT"));
        assertEquals(2, StringUtils.countMatches(responseContent, "Terminate task number 1"));
    }


    @Test
    public void testTaskLogAllByUnknownTag() throws Exception {
        HttpResponse response = sendRequest("jobs/" + jobId + "/tasks/tag/unknownTag/result/log/all");
        String responseContent = getContent(response);

        assertEquals("", responseContent);
    }


    @Test
    public void testTaskLogErrByTag() throws Exception {
        HttpResponse response = sendRequest("jobs/" + jobId + "/tasks/tag/LOOP-T2-1/result/log/err");
        String responseContent = getContent(response);

        System.out.println(responseContent);

        assertEquals(2, StringUtils.countMatches(responseContent, "Task 1 : Test STDERR"));
    }


    @Test
    public void testTaskLogErrByUnknownTag() throws Exception {
        HttpResponse response = sendRequest("jobs/" + jobId + "/tasks/tag/unknownTag/result/log/err");
        String responseContent = getContent(response);

        assertEquals("", responseContent);
    }


    @Test
    public void testTaskLogOutByTag() throws Exception {
        HttpResponse response = sendRequest("jobs/" + jobId + "/tasks/tag/LOOP-T2-1/result/log/out");
        String responseContent = getContent(response);

        System.out.println(responseContent);

        assertEquals(2, StringUtils.countMatches(responseContent, "Task 1 : Test STDOUT"));
        assertEquals(2, StringUtils.countMatches(responseContent, "Terminate task number 1"));
    }


    @Test
    public void testTaskLogOutByUnknownTag() throws Exception {
        HttpResponse response = sendRequest("jobs/" + jobId + "/tasks/tag/unknownTag/result/log/all");
        String responseContent = getContent(response);

        assertEquals("", responseContent);
    }


    @Test
    public void testTaskLogServerByTag() throws Exception {
        HttpResponse response = sendRequest("jobs/" + jobId + "/tasks/tag/LOOP-T2-1/log/server");
        String responseContent = getContent(response);

        for(TaskState state: scheduler.getJobState(jobId).getTaskByTag("LOOP-T2-1")){
            assertTrue(responseContent.contains("Task " + state.getId().value() + " logs"));
        }
    }


    @Test
    public void testTaskLogSeverByUnknownTag() throws Exception {
        HttpResponse response = sendRequest("jobs/" + jobId + "/tasks/tag/unknownTag/log/server");
        String responseContent = getContent(response);

        assertTrue(!responseContent.contains("TaskLogger"));
    }


    @Test
    public void testTaskResultByTag() throws Exception {
        HttpResponse response = sendRequest("jobs/" + jobId + "/tasks/tag/LOOP-T2-1/result");
        JSONArray jsonArray = toJsonArray(response);

        System.out.println(jsonArray.toJSONString());

        ArrayList<String> taskNames = new ArrayList<>();
        for(int i = 0; i < jsonArray.size(); i++){
            JSONObject id = (JSONObject) ((JSONObject) jsonArray.get(i)).get("id");
            String name = (String) id.get("readableName");
            taskNames.add(name);
        }

        assertTrue(taskNames.contains("T1#1"));
        assertTrue(taskNames.contains("Print1#1"));
        assertTrue(taskNames.contains("Print2#1"));
        assertTrue(taskNames.contains("T2#1"));
        assertEquals(4, jsonArray.size());
    }


    @Test
    public void testTaskResultByUnknownTag() throws Exception {
        HttpResponse response = sendRequest("jobs/" + jobId + "/tasks/tag/unknownTag/result");
        JSONArray jsonArray = toJsonArray(response);

        System.out.println(jsonArray.toJSONString());
        assertEquals(0, jsonArray.size());
    }

    @Test
    public void testTaskResultValueByTag() throws Exception {
        HttpResponse response = sendRequest("jobs/" + jobId + "/tasks/tag/LOOP-T2-1/result/value");
        JSONObject jsonObject = toJsonObject(response);

        System.out.println(jsonObject.toJSONString());

        assertTrue(jsonObject.containsKey("T1#1"));
        assertTrue(jsonObject.containsKey("Print1#1"));
        assertTrue(jsonObject.containsKey("Print2#1"));
        assertTrue(jsonObject.containsKey("T2#1"));
        assertEquals(4, jsonObject.size());
    }


    @Test
    public void testTaskResultValueByUnknownTag() throws Exception {
        HttpResponse response = sendRequest("jobs/" + jobId + "/tasks/tag/unknownTag/result/value");
        JSONObject jsonObject = toJsonObject(response);

        System.out.println(jsonObject.toJSONString());
        assertEquals(0, jsonObject.size());
    }


    @Test
    public void testTaskResultSerializedvalueByTag() throws Exception {
        HttpResponse response = sendRequest("jobs/" + jobId + "/tasks/tag/LOOP-T2-1/result/serializedvalue");
        JSONObject jsonObject = toJsonObject(response);

        System.out.println(jsonObject.toJSONString());

        assertTrue(jsonObject.containsKey("T1#1"));
        assertTrue(jsonObject.containsKey("Print1#1"));
        assertTrue(jsonObject.containsKey("Print2#1"));
        assertTrue(jsonObject.containsKey("T2#1"));
        assertEquals(4, jsonObject.size());
    }


    @Test
    public void testTaskResultSerializedvalueByUnknownTag() throws Exception {
        HttpResponse response = sendRequest("jobs/" + jobId + "/tasks/tag/unknownTag/result/serializedvalue");
        JSONObject jsonObject = toJsonObject(response);

        System.out.println(jsonObject.toJSONString());
        assertEquals(0, jsonObject.size());
    }


    @Test
    public void testJobTags() throws Exception {
        HttpResponse response = sendRequest("jobs/" + jobId + "/tasks/tags");
        JSONArray jsonArray = toJsonArray(response);

        System.out.println(jsonArray.toJSONString());

        assertTrue(jsonArray.contains("LOOP-T2-1"));
        assertTrue(jsonArray.contains("LOOP-T2-2"));
        assertTrue(jsonArray.contains("LOOP-T2-3"));
        assertEquals(3, jsonArray.size());
    }

}
