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
package org.ow2.proactive_grid_cloud_portal.scheduler.client;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ow2.proactive_grid_cloud_portal.common.SchedulerRestInterface;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.JobIdData;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.JobResultData;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.JobStateData;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.RestMapPage;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.SchedulerUserData;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.TaskResultData;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.TaskStateData;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.UserJobData;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.UnknownJobRestException;


public class SchedulerClientExample {

    public static void main(String[] args) throws Exception {

        // LOGIN IN
        SchedulerRestClient client = new SchedulerRestClient("http://localhost:8080/rest/");
        SchedulerRestInterface scheduler = client.getScheduler();
        String sessionId = scheduler.login("admin", "admin");

        // JOB SUBMISSION
        File xmlJobFile = new File("/path/to/some/existing/job/job_1.xml");
        JobIdData xmlJob;
        try (FileInputStream inputStream = new FileInputStream(xmlJobFile)) {
            xmlJob = client.submitXml(sessionId, inputStream);
        }
        System.out.println(xmlJob.getReadableName() + " " + xmlJob.getId());

        // FLAT JOB SUBMISSION
        JobIdData flatJob = scheduler.submitFlat(sessionId, "echo hello", "test-hello", null, null);
        System.out.println("Jobid=" + flatJob);

        String serverlog = scheduler.jobServerLog(sessionId, Long.toString(flatJob.getId()));
        System.out.println(serverlog);

        while (true) {
            JobStateData jobState2 = scheduler.listJobs(sessionId, Long.toString(flatJob.getId()));
            System.out.println(jobState2);
            if (jobState2.getJobInfo().getStatus().name().equals("FINISHED")) {
                break;
            }
            Thread.sleep(100);
        }

        JobResultData jobResultData = scheduler.jobResult(sessionId, Long.toString(flatJob.getId()));
        System.out.println(jobResultData);

        TaskResultData taskresult = scheduler.taskResult(sessionId, Long.toString(flatJob.getId()), "task_1");
        System.out.println(taskresult);

        List<TaskStateData> jobTaskStates = scheduler.getJobTaskStates(sessionId, Long.toString(flatJob.getId()))
                                                     .getList();
        System.out.println(jobTaskStates);

        TaskStateData task_1 = scheduler.jobTask(sessionId, Long.toString(flatJob.getId()), "task_1");
        System.out.println(task_1);

        // OTHER CALLS

        List<SchedulerUserData> users = scheduler.getUsers(sessionId);
        System.out.println(users);
        System.out.println(users.size());

        RestMapPage<Long, ArrayList<UserJobData>> page = scheduler.revisionAndJobsInfo(sessionId,
                                                                                       0,
                                                                                       50,
                                                                                       true,
                                                                                       true,
                                                                                       true,
                                                                                       true);
        Map<Long, ArrayList<UserJobData>> map = page.getMap();
        System.out.println(map);

        System.out.println(scheduler.getSchedulerStatus(sessionId));
        System.out.println(scheduler.getUsageOnMyAccount(sessionId, new Date(), new Date()));

        // FAILING CALL
        try {
            JobStateData jobState = scheduler.listJobs(sessionId, "601");
            System.out.println(jobState);
        } catch (UnknownJobRestException e) {
            System.err.println("exception! " + e.getMessage());
            e.printStackTrace();
        }

    }

}
