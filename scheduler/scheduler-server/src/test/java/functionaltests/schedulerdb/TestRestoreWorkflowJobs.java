package functionaltests.schedulerdb;

import org.junit.Test;
import org.ow2.proactive.scheduler.common.job.JobStatus;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.TaskStatus;
import org.ow2.proactive.scheduler.common.task.flow.FlowAction;
import org.ow2.proactive.scheduler.common.task.flow.FlowActionType;
import org.ow2.proactive.scheduler.common.task.flow.FlowBlock;
import org.ow2.proactive.scheduler.common.task.flow.FlowScript;
import org.ow2.proactive.scheduler.core.db.SchedulerStateRecoverHelper;
import org.ow2.proactive.scheduler.job.ChangedTasksInfo;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.task.TaskResultImpl;
import org.ow2.proactive.scheduler.task.internal.InternalTask;

import functionaltests.TestReplicateTaskRestore2.TestTask;


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

        expectedJob = job(job.getId(), JobStatus.STALLED).withFinished(
                task("T", TaskStatus.FINISHED).checkFinished(), true).withPending(
                task("T1", TaskStatus.PENDING), true).withPending(task("T1*1", TaskStatus.SUBMITTED), true)
                .withPending(task("T2", TaskStatus.PENDING), true).withPending(
                        task("T3", TaskStatus.PENDING), true).withPending(task("T2*1", TaskStatus.SUBMITTED),
                        true).withPending(task("T3*1", TaskStatus.SUBMITTED), true).withPending(
                        task("T4", TaskStatus.PENDING), true).withEligible("T1", "T1*1");

        checkRecoveredState(recoverHelper.recover(-1), state().withRunning(expectedJob));
    }

    static JavaTask task(String name) {
        JavaTask task = new JavaTask();
        task.setExecutableClassName(TestTask.class.getName());
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
