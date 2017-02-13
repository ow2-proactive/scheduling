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

import static org.junit.Assert.*;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.ow2.proactive.authentication.crypto.CredData;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.SchedulerAuthenticationInterface;
import org.ow2.proactive.scheduler.common.exception.NotConnectedException;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.executable.JavaExecutable;
import org.ow2.proactive.scheduler.common.usage.JobUsage;

import functionaltests.utils.SchedulerFunctionalTestNoRestart;
import functionaltests.utils.TestUsers;


/**
 * Test against SchedulerUsage interface.
 */
public class TestGetUsage extends SchedulerFunctionalTestNoRestart {

    @Test
    public void testGetMyAccountUsage() throws Exception {
        Date beforeJobExecution = new Date();

        // put some data in the database
        Scheduler firstUser = schedulerHelper.getSchedulerInterface();
        JobId jobId = firstUser.submit(createJob());
        schedulerHelper.waitForEventJobFinished(jobId, 30000);

        Date afterJobExecution = new Date();

        // We try to retrieve usage on the job I just ran
        List<JobUsage> adminUsages = firstUser.getMyAccountUsage(beforeJobExecution, afterJobExecution);
        assertFalse(adminUsages.isEmpty());

        // Do we properly check for user connection ?
        firstUser.disconnect();
        try {
            firstUser.getMyAccountUsage(beforeJobExecution, afterJobExecution);
            fail("Should throw a not connected exception because i just disconnected");
        } catch (NotConnectedException e) {
            // Ok that is expected
        }

        // another user
        SchedulerAuthenticationInterface auth = schedulerHelper.getSchedulerAuth();
        Credentials cred = Credentials.createCredentials(new CredData(TestUsers.USER.username, TestUsers.USER.password),
                                                         auth.getPublicKey());
        Scheduler otherUser = auth.login(cred);

        // This user has not ran any job
        List<JobUsage> userUsages = otherUser.getMyAccountUsage(beforeJobExecution, afterJobExecution);
        assertTrue(userUsages.isEmpty());
        otherUser.disconnect();
    }

    private TaskFlowJob createJob() throws Exception {
        TaskFlowJob job = new TaskFlowJob();
        job.setName(this.getClass().getSimpleName());

        JavaTask javaTask = new JavaTask();
        javaTask.setExecutableClassName(TestJavaTask.class.getName());
        javaTask.setName("Test task");

        job.addTask(javaTask);

        return job;
    }

    public static class TestJavaTask extends JavaExecutable {

        @Override
        public Serializable execute(TaskResult... results) throws Throwable {
            return "OK";
        }

    }
}
