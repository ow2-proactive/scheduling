/*
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
 *  * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive_grid_cloud_portal.scheduler.client;

import java.io.File;
import java.io.FileInputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.ow2.proactive_grid_cloud_portal.common.SchedulerRestInterface;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.JobIdData;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.JobResultData;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.JobStateData;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.SchedulerUserData;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.TaskResultData;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.TaskStateData;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.UserJobData;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.UnknownJobRestException;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class SchedulerClientExample {

    public static void main(String[] args) throws Exception {

        // LOGIN IN
        SchedulerRestClient client = new SchedulerRestClient("http://localhost:9191/rest/rest/");
        SchedulerRestInterface scheduler = client.getScheduler();
        String sessionId = scheduler.login("admin", "admin");

        // JOB SUBMISSION
        File xmlJobFile = new File(
                "/home/ybonnaffe/src/cloud_service_provider_conectors/cloudstack/vminfo_job.xml");
        JobIdData xmlJob = client.submitXml(sessionId, new FileInputStream(xmlJobFile));
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
        System.out.println(ToStringBuilder.reflectionToString(jobResultData));

        TaskResultData taskresult = scheduler.taskresult(sessionId, Long.toString(flatJob.getId()), "task_1");
        System.out.println(ToStringBuilder.reflectionToString(taskresult));


        List<TaskStateData> jobTaskStates = scheduler.getJobTaskStates(sessionId,
                Long.toString(flatJob.getId()));
        System.out.println(ToStringBuilder.reflectionToString(jobTaskStates));

        TaskStateData task_1 = scheduler.jobtasks(sessionId, Long.toString(flatJob.getId()), "task_1");
        System.out.println(ToStringBuilder.reflectionToString(task_1));


        // OTHER CALLS

        List<SchedulerUserData> users = scheduler.getUsers(sessionId);
        System.out.println(users);
        System.out.println(users.size());


        Map<Long, List<UserJobData>> map = scheduler.revisionAndjobsinfo(sessionId, 0, 50, true,
                true, true, true);
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
