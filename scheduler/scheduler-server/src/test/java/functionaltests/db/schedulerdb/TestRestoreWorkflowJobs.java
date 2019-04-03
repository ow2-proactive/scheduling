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
package functionaltests.db.schedulerdb;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;
import org.ow2.proactive.scheduler.common.exception.UserException;
import org.ow2.proactive.scheduler.common.job.JobStatus;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.ForkEnvironment;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.TaskStatus;
import org.ow2.proactive.scheduler.common.task.flow.FlowAction;
import org.ow2.proactive.scheduler.common.task.flow.FlowActionType;
import org.ow2.proactive.scheduler.common.task.flow.FlowBlock;
import org.ow2.proactive.scheduler.common.task.flow.FlowScript;
import org.ow2.proactive.scheduler.core.db.SchedulerStateRecoverHelper;
import org.ow2.proactive.scheduler.examples.EmptyTask;
import org.ow2.proactive.scheduler.job.ChangedTasksInfo;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.task.TaskResultImpl;
import org.ow2.proactive.scheduler.task.internal.InternalTask;
import org.ow2.proactive.scripting.InvalidScriptException;
import org.ow2.proactive.scripting.SelectionScript;
import org.ow2.proactive.scripting.SimpleScript;


public class TestRestoreWorkflowJobs extends BaseSchedulerDBTest {

    @Test
    public void test() throws Exception {
        TaskFlowJob jobDef = createJob();

        InternalJob job = defaultSubmitJobAndLoadInternal(true, jobDef);

        job.start();
        InternalTask mainTask = job.getTask("T");
        startTask(job, mainTask);
        dbManager.jobTaskStarted(job, mainTask, true);

        TaskResultImpl result = new TaskResultImpl(mainTask.getId(), "ok", null, 0);
        FlowAction action = new FlowAction(FlowActionType.REPLICATE);
        action.setDupNumber(2);
        ChangedTasksInfo changesInfo = job.terminateTask(false, mainTask.getId(), null, action, result);

        dbManager.updateAfterWorkflowTaskFinished(job, changesInfo, result);

        SchedulerStateRecoverHelper recoverHelper = new SchedulerStateRecoverHelper(dbManager);

        JobStateMatcher expectedJob;

        expectedJob = job(job.getId(), JobStatus.STALLED)
                                                         .withFinished(task("T", TaskStatus.FINISHED).checkFinished(),
                                                                       true)
                                                         .withPending(task("T1", TaskStatus.SUBMITTED), true)
                                                         .withPending(task("T1*1", TaskStatus.SUBMITTED), true)
                                                         .withPending(task("T2", TaskStatus.SUBMITTED), true)
                                                         .withPending(task("T3", TaskStatus.SUBMITTED), true)
                                                         .withPending(task("T2*1", TaskStatus.SUBMITTED), true)
                                                         .withPending(task("T3*1", TaskStatus.SUBMITTED), true)
                                                         .withPending(task("T4", TaskStatus.SUBMITTED), true)
                                                         .withEligible("T1", "T1*1");

        checkRecoveredState(recoverHelper.recover(-1), state().withRunning(expectedJob));
    }

    @Test
    public void finishedJobWithScriptsCanBeRecoveredAndLoaded() throws Exception {
        InternalJob job = defaultSubmitJobAndLoadInternal(true, createJobWithAllKindOfScripts());

        job.start();
        InternalTask mainTask = job.getTask("T");
        startTask(job, mainTask);
        dbManager.jobTaskStarted(job, mainTask, true);

        TaskResultImpl result = new TaskResultImpl(mainTask.getId(), "ok", null, 0);
        ChangedTasksInfo changesInfo = job.terminateTask(false, mainTask.getId(), null, null, result);
        job.setStatus(JobStatus.FINISHED);

        dbManager.updateAfterWorkflowTaskFinished(job, changesInfo, result);

        SchedulerStateRecoverHelper recoverHelper = new SchedulerStateRecoverHelper(dbManager);

        JobStateMatcher expectedJob = job(job.getId(), JobStatus.FINISHED).withFinished(
                                                                                        task("T", TaskStatus.FINISHED)
                                                                                                                      .checkFinished(),
                                                                                        true);
        checkRecoveredState(recoverHelper.recover(-1), state().withFinished(expectedJob));

        List<InternalJob> finishedJobs = dbManager.loadFinishedJobs(true, -1);

        assertEquals(1, finishedJobs.size());
    }

    private TaskFlowJob createJobWithAllKindOfScripts() throws InvalidScriptException, UserException {
        TaskFlowJob jobDef = new TaskFlowJob();
        JavaTask t = task("T");
        ForkEnvironment forkEnvironment = new ForkEnvironment();
        SimpleScript aScript = new SimpleScript("", "");
        forkEnvironment.setEnvScript(aScript);
        t.setPreScript(aScript);
        t.setPostScript(aScript);
        t.setCleaningScript(aScript);
        t.setSelectionScript(new SelectionScript("", ""));
        t.setFlowScript(FlowScript.createContinueFlowScript());
        t.setForkEnvironment(forkEnvironment);
        jobDef.addTask(t);
        return jobDef;
    }

    static JavaTask task(String name) {
        JavaTask task = new JavaTask();
        task.setExecutableClassName(EmptyTask.class.getName());
        task.setName(name);
        return task;
    }

    private TaskFlowJob createJob() throws Exception {
        TaskFlowJob job = new TaskFlowJob();

        JavaTask t = task("T");
        JavaTask t1 = task("T1");
        JavaTask t2 = task("T2");
        JavaTask t3 = task("T3");
        JavaTask t4 = task("T4");

        t1.addDependence(t);
        t2.addDependence(t1);
        t3.addDependence(t2);
        t4.addDependence(t3);

        String replicateScript = String.format("runs = %d", 2);
        t.setFlowScript(FlowScript.createReplicateFlowScript(replicateScript));

        t1.setFlowBlock(FlowBlock.START);
        t1.setFlowScript(FlowScript.createReplicateFlowScript(replicateScript));

        t3.setFlowBlock(FlowBlock.END);

        job.addTask(t);
        job.addTask(t1);
        job.addTask(t2);
        job.addTask(t3);
        job.addTask(t4);

        return job;
    }

}
