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
package functionaltests.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.job.Job;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.job.SchedulerUserInfo;

import functionaltests.executables.EmptyExecutable;
import functionaltests.utils.SchedulerFunctionalTestNoRestart;
import functionaltests.utils.TestUsers;


/**
 * Sanity test against method 'Scheduler.getUsers' and 'Scheduler.getUsersWithJobs'.
 *
 */
public class TestGetUsers extends SchedulerFunctionalTestNoRestart {

    long testStartTime = System.currentTimeMillis();

    @Test
    public void test() throws Exception {

        Scheduler scheduler = schedulerHelper.getSchedulerInterface();
        List<SchedulerUserInfo> users;
        List<SchedulerUserInfo> usersWithJobs;

        users = scheduler.getUsers();
        assertEquals(1, users.size());

        checkUser(users.get(0), TestUsers.DEMO.username, null);

        assertTrue("Unexpected connect time: " + users.get(0).getConnectionTime(),
                   users.get(0).getConnectionTime() > testStartTime);
        Long connectTime = users.get(0).getConnectionTime();

        scheduler.submit(createJob());

        users = scheduler.getUsers();
        usersWithJobs = scheduler.getUsersWithJobs();
        assertEquals(1, users.size());
        assertEquals(1, usersWithJobs.size());
        checkUser(users.get(0), TestUsers.DEMO.username, connectTime);
        checkUser(usersWithJobs.get(0), TestUsers.DEMO.username, null);

        scheduler.submit(createJob());

        users = scheduler.getUsers();
        usersWithJobs = scheduler.getUsersWithJobs();
        assertEquals(1, users.size());
        assertEquals(1, usersWithJobs.size());
        checkUser(users.get(0), TestUsers.DEMO.username, connectTime);
        checkUser(usersWithJobs.get(0), TestUsers.DEMO.username, null);
    }

    private void checkUser(SchedulerUserInfo user, String name, Long connectTime) {
        System.out.println("User: " + user.getUsername() + " " + user.getHostName() + " " + user.getConnectionTime() +
                           " " + user.getLastSubmitTime() + " " + user.getSubmitNumber());
        if (connectTime != null) {
            assertEquals(connectTime.longValue(), user.getConnectionTime());
        }
        assertEquals(name, user.getUsername());
    }

    private TaskFlowJob createJob() throws Exception {
        TaskFlowJob job = new TaskFlowJob();
        job.setName(this.getClass().getSimpleName());
        JavaTask task = new JavaTask();
        task.setExecutableClassName(EmptyExecutable.class.getName());
        job.addTask(task);
        return job;
    }

}
