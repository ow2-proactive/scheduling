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
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package functionaltests.api;

import functionaltests.executables.EmptyExecutable;
import functionaltests.utils.SchedulerFunctionalTestNoRestart;
import functionaltests.utils.TestUsers;
import org.junit.Test;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.job.Job;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.job.SchedulerUserInfo;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


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

        assertTrue("Unexpected connect time: " + users.get(0).getConnectionTime(), users.get(0)
          .getConnectionTime() > testStartTime);
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
        System.out.println("User: " + user.getUsername() + " " + user.getHostName() + " " +
            user.getConnectionTime() + " " + user.getLastSubmitTime() + " " + user.getSubmitNumber());
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
