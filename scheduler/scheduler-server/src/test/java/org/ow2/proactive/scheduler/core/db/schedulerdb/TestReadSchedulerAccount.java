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
package org.ow2.proactive.scheduler.core.db.schedulerdb;

import org.junit.Assert;
import org.junit.Test;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.ForkEnvironment;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.NativeTask;
import org.ow2.proactive.scheduler.core.account.SchedulerAccount;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.task.TaskResultImpl;
import org.ow2.proactive.scheduler.task.internal.InternalTask;


public class TestReadSchedulerAccount extends BaseSchedulerDBTest {

    static final String INVALID_USER_NAME = "nosuchuser";

    static final String TEST_USER_NAME1 = "TestReadSchedulerAccount1";

    static final String TEST_USER_NAME2 = "TestReadSchedulerAccount2";

    static class AccountData {
        long taskTime;

        int taskCount;

        long jobTime;

        int jobCount;

        final String userName;

        AccountData(String userName) {
            this.userName = userName;
        }
    }

    AccountData invalidUser = new AccountData(INVALID_USER_NAME);

    @Test
    public void testReadAccount() throws Exception {
        // database is empty
        AccountData user1 = new AccountData(TEST_USER_NAME1);
        AccountData user2 = new AccountData(TEST_USER_NAME2);
        checkAccount(invalidUser);
        checkAccount(user1);
        checkAccount(user2);

        singleJobScenario(user1);

        singleJobScenario(user2);

        for (int i = 0; i < 10; i++) {
            singleJobScenario(user1);
            checkAccount(invalidUser);
            checkAccount(user1);
            checkAccount(user2);
        }

        singleJobScenario(user2);

        checkAccount(invalidUser);
        checkAccount(user1);
        checkAccount(user2);
    }

    private void singleJobScenario(AccountData accountData) throws Exception {
        TaskFlowJob jobDef1 = new TaskFlowJob();
        JavaTask javaTask = new JavaTask();
        javaTask.setExecutableClassName(TestDummyExecutable.class.getName());
        javaTask.setName("task1");
        jobDef1.addTask(javaTask);

        JavaTask forkJavaTask = createDefaultTask("task2");
        forkJavaTask.setExecutableClassName(TestDummyExecutable.class.getName());
        forkJavaTask.setForkEnvironment(new ForkEnvironment());
        jobDef1.addTask(forkJavaTask);

        NativeTask nativeTask = new NativeTask();
        nativeTask.setName("task3");
        nativeTask.setCommandLine("command");
        jobDef1.addTask(nativeTask);

        InternalJob job1 = defaultSubmitJobAndLoadInternal(true, jobDef1, accountData.userName);

        // job is submitted

        checkAccount(invalidUser);
        checkAccount(accountData);

        // task1 started
        job1.start();
        startTask(job1, job1.getTask("task1"));
        dbManager.jobTaskStarted(job1, job1.getTask("task1"), true);

        checkAccount(invalidUser);
        checkAccount(accountData);

        // task1 finished
        accountData.taskTime += finishTask(job1, "task1");
        accountData.taskCount++;

        checkAccount(invalidUser);
        checkAccount(accountData);

        // task2 and task3 started
        startTask(job1, job1.getTask("task2"));
        dbManager.jobTaskStarted(job1, job1.getTask("task2"), true);
        startTask(job1, job1.getTask("task3"));
        dbManager.jobTaskStarted(job1, job1.getTask("task3"), true);

        checkAccount(invalidUser);
        checkAccount(accountData);

        // task2 finished
        accountData.taskTime += finishTask(job1, "task2");
        accountData.taskCount++;

        checkAccount(invalidUser);
        checkAccount(accountData);

        // task3 and job finished
        accountData.taskTime += finishTask(job1, "task3");
        accountData.taskCount++;
        accountData.jobCount++;
        accountData.jobTime += job1.getFinishedTime() - job1.getStartTime();

        checkAccount(invalidUser);
        checkAccount(accountData);
    }

    private long finishTask(InternalJob job, String taskName) throws Exception {
        Thread.sleep(100);

        InternalTask task = job.getTask(taskName);
        TaskResultImpl res = new TaskResultImpl(null, "ok", null, 0);
        job.terminateTask(false, task.getId(), null, null, res);
        if (job.isFinished()) {
            job.terminate();
        }

        dbManager.updateAfterTaskFinished(job, task, res);

        return task.getFinishedTime() - task.getStartTime();
    }

    private void checkAccount(AccountData accountData) {
        SchedulerAccount account = dbManager.readAccount(accountData.userName);
        Assert.assertEquals(accountData.userName, account.getName());
        Assert.assertEquals("Tasks count", accountData.taskCount, account.getTotalTaskCount());
        Assert.assertEquals("Tasks duration", accountData.taskTime, account.getTotalTaskDuration());
        Assert.assertEquals("Jobs count", accountData.jobCount, account.getTotalJobCount());
        Assert.assertEquals("Jobs duration", accountData.jobTime, account.getTotalJobDuration());

    }
}
