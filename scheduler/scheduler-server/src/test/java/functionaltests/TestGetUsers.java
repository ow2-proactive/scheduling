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
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package functionaltests;

import java.io.Serializable;
import java.util.List;

import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.executable.JavaExecutable;
import org.ow2.proactive.scheduler.job.SchedulerUserInfo;
import org.junit.Assert;
import org.junit.Test;


/**
 * Sanity test against method 'Scheduler.getUsers' and 'Scheduler.getUsersWithJobs'.
 *
 */
public class TestGetUsers extends RMFunctionalTest {

    public static class TestJavaTask extends JavaExecutable {
        @Override
        public Serializable execute(TaskResult... results) throws Throwable {
            return "OK";
        }
    }

    long testStartTime = System.currentTimeMillis();

    private void checkUser(SchedulerUserInfo user, String name, int submit, Long connectTime) {
        System.out.println("User: " + user.getUsername() + " " + user.getHostName() + " " +
            user.getConnectionTime() + " " + user.getLastSubmitTime() + " " + user.getSubmitNumber());
        if (connectTime != null) {
            Assert.assertEquals(connectTime.longValue(), user.getConnectionTime());
        }
        Assert.assertEquals(name, user.getUsername());
        Assert.assertEquals(submit, user.getSubmitNumber());
    }

    @Test
    public void test() throws Exception {
        Scheduler scheduler = SchedulerTHelper.getSchedulerInterface();
        List<SchedulerUserInfo> users;
        List<SchedulerUserInfo> usersWithJobs;

        users = scheduler.getUsers();
        usersWithJobs = scheduler.getUsersWithJobs();
        Assert.assertEquals(1, users.size());
        Assert.assertEquals(0, usersWithJobs.size());

        checkUser(users.get(0), SchedulerTHelper.admin_username, 0, null);

        Assert.assertTrue("Unexpected connect time: " + users.get(0).getConnectionTime(), users.get(0)
                .getConnectionTime() > testStartTime);
        Long connectTime = users.get(0).getConnectionTime();

        scheduler.submit(createJob());

        users = scheduler.getUsers();
        usersWithJobs = scheduler.getUsersWithJobs();
        Assert.assertEquals(1, users.size());
        Assert.assertEquals(1, usersWithJobs.size());
        checkUser(users.get(0), SchedulerTHelper.admin_username, 1, connectTime);
        checkUser(usersWithJobs.get(0), SchedulerTHelper.admin_username, 1, null);

        scheduler.submit(createJob());

        users = scheduler.getUsers();
        usersWithJobs = scheduler.getUsersWithJobs();
        Assert.assertEquals(1, users.size());
        Assert.assertEquals(1, usersWithJobs.size());
        checkUser(users.get(0), SchedulerTHelper.admin_username, 2, connectTime);
        checkUser(usersWithJobs.get(0), SchedulerTHelper.admin_username, 2, null);
    }

    private TaskFlowJob createJob() throws Exception {
        TaskFlowJob job = new TaskFlowJob();
        job.setName(this.getClass().getSimpleName());
        JavaTask task = new JavaTask();
        task.setExecutableClassName(TestJavaTask.class.getName());
        job.addTask(task);
        return job;
    }
}
