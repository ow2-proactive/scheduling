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
import org.ow2.proactive.scheduler.common.job.JobStatus;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.TaskStatus;
import org.ow2.proactive.scheduler.core.db.RecoveredSchedulerState;
import org.ow2.proactive.scheduler.core.db.SchedulerStateRecoverHelper;
import org.ow2.proactive.scheduler.descriptor.EligibleTaskDescriptor;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.task.containers.ExecutableContainer;
import org.ow2.proactive.scheduler.task.internal.InternalTask;


public class SchedulerTasksStateRecoverIntegrationTest extends BaseSchedulerDBTest {

    @Test
    public void testRecoverAfterRestart() throws Exception {
        TaskFlowJob jobDef = new TaskFlowJob();
        jobDef.addTask(createDefaultTask("task1"));
        InternalJob job = defaultSubmitJobAndLoadInternal(true, jobDef);
        InternalTask task = job.getTask("task1");

        job.start();
        startTask(job, task);
        dbManager.jobTaskStarted(job, task, true);

        SchedulerStateRecoverHelper recoverHelper = new SchedulerStateRecoverHelper(dbManager);

        JobStateMatcher expectedJob;

        expectedJob = job(job.getId(), JobStatus.STALLED).withPending(task("task1", TaskStatus.PENDING), true)
                                                         .withEligible("task1");

        RecoveredSchedulerState state;

        state = checkRecoveredState(recoverHelper.recover(-1), state().withRunning(expectedJob));

        job = state.getRunningJobs().get(0);
        task = job.getTask("task1");

        startTask(job, task);
        dbManager.jobTaskStarted(job, task, true);

        job.newWaitingTask();
        job.reStartTask(task);
        dbManager.taskRestarted(job, task, null);

        state = checkRecoveredState(recoverHelper.recover(-1), state().withRunning(expectedJob));

        // check it is possible to load ExecutableContainer for restored task
        job = state.getRunningJobs().get(0);
        ExecutableContainer container = dbManager.loadExecutableContainer(job.getTask("task1"));
        Assert.assertNotNull(container);
    }

    @Test
    public void testRecover() throws Exception {
        SchedulerStateRecoverHelper recoverHelper = new SchedulerStateRecoverHelper(dbManager);
        RecoveredSchedulerState state = recoverHelper.recover(-1);
        Assert.assertEquals(0, state.getFinishedJobs().size());
        Assert.assertEquals(0, state.getRunningJobs().size());
        Assert.assertEquals(0, state.getPendingJobs().size());

        TaskFlowJob job1 = new TaskFlowJob();
        JavaTask task1 = createDefaultTask("task1");
        JavaTask task2 = createDefaultTask("task2");
        JavaTask task3 = createDefaultTask("task3");
        task1.addDependence(task2);
        task1.addDependence(task3);
        task2.addDependence(task3);
        job1.addTask(task1);
        job1.addTask(task2);
        job1.addTask(task3);

        InternalJob job = defaultSubmitJob(job1);

        JobStateMatcher expectedJob;

        expectedJob = job(job.getId(), JobStatus.PENDING).withPending(task("task1", TaskStatus.SUBMITTED), false)
                                                         .withPending(task("task2", TaskStatus.SUBMITTED), false)
                                                         .withPending(task("task3", TaskStatus.SUBMITTED), false)
                                                         .withEligible("task3");

        state = checkRecoveredState(recoverHelper.recover(-1), state().withPending(expectedJob));

        job = state.getPendingJobs().get(0);
        EligibleTaskDescriptor task = job.getJobDescriptor().getEligibleTasks().iterator().next();
        Assert.assertEquals(2, task.getChildren().size());
        Assert.assertEquals(0, task.getParents().size());

        job.terminate();
        dbManager.updateAfterTaskFinished(job, null, null);

        expectedJob = job(job.getId(), JobStatus.FINISHED).withPending(task("task1", TaskStatus.SUBMITTED), false)
                                                          .withPending(task("task2", TaskStatus.SUBMITTED), false)
                                                          .withPending(task("task3", TaskStatus.SUBMITTED), false)
                                                          .withEligible("task3");
        state = checkRecoveredState(recoverHelper.recover(-1), state().withFinished(expectedJob));

        job = state.getFinishedJobs().get(0);
        task = job.getJobDescriptor().getEligibleTasks().iterator().next();
        Assert.assertEquals(2, task.getChildren().size());
        Assert.assertEquals(0, task.getParents().size());
    }

}
