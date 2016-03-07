package org.ow2.proactive.scheduler.task;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.ow2.proactive.scheduler.common.TaskTerminateNotification;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.job.JobIdImpl;
import org.ow2.proactive.scheduler.task.containers.ScriptExecutableContainer;
import org.ow2.proactive.scripting.SimpleScript;
import org.ow2.proactive.scripting.TaskScript;
import org.ow2.proactive.utils.Repeat;
import org.ow2.proactive.utils.RepeatRule;

import java.io.Serializable;
import java.util.concurrent.Semaphore;

import static org.junit.Assert.assertEquals;


public class KillTaskLauncherTest {

    static final int repetitions = 10;
    static final boolean parallel = true;
    static final long timeout = 10000;

    @Rule
    public RepeatRule repeatRule = new RepeatRule();

    @Before
    public void setUp() throws Exception {
        CentralPAPropertyRepository.PA_CLASSLOADING_USEHTTP.setValue(false);
    }

    @Test
    @Repeat(value = repetitions, parallel = parallel, timeout = timeout)
    public void kill_while_sleeping_in_task() throws Exception {

        final ScriptExecutableContainer executableContainer = new ScriptExecutableContainer(new TaskScript(
            new SimpleScript("java.lang.Thread.sleep(10000)", "javascript")));

        TaskLauncherInitializer initializer = new TaskLauncherInitializer();
        initializer.setTaskId(TaskIdImpl.createTaskId(JobIdImpl.makeJobId("1000"), "job", 1000L));

        Semaphore taskRunning = new Semaphore(0);

        final TaskLauncher taskLauncher = TaskLauncherUtils.create(initializer,
          new TestTaskLauncherFactory(taskRunning));
        final TaskLauncher taskLauncherPA = PAActiveObject.turnActive(taskLauncher);

        taskLauncherPA.doTask(executableContainer, null, null);

        taskRunning.acquire();
        taskLauncherPA.kill();

        assertTaskLauncherIsTerminated(taskLauncherPA);
    }

    @Test
    @Repeat(value = repetitions, parallel = parallel, timeout = timeout)
    public void kill_while_looping_in_task() throws Exception {

        final ScriptExecutableContainer executableContainer = new ScriptExecutableContainer(new TaskScript(
            new SimpleScript("for(;;){}", "javascript")));

        TaskLauncherInitializer initializer = new TaskLauncherInitializer();
        initializer.setTaskId(TaskIdImpl.createTaskId(JobIdImpl.makeJobId("1000"), "job", 1000L));

        Semaphore taskRunning = new Semaphore(0);
        final TaskLauncher taskLauncher = TaskLauncherUtils.create(initializer, new TestTaskLauncherFactory(
          taskRunning));
        final TaskLauncher taskLauncherPA = PAActiveObject.turnActive(taskLauncher);

        taskLauncherPA.doTask(executableContainer, null, null);

        taskRunning.acquire();
        taskLauncherPA.kill();

        assertTaskLauncherIsTerminated(taskLauncherPA);
    }

    @Test
    @Repeat(value = repetitions, parallel = parallel, timeout = timeout)
    public void finished_but_terminate_not_called_back() throws Throwable {

        final ScriptExecutableContainer executableContainer = new ScriptExecutableContainer(new TaskScript(
            new SimpleScript("result='done'", "javascript")));

        TaskLauncherInitializer initializer = new TaskLauncherInitializer();
        initializer.setTaskId(TaskIdImpl.createTaskId(JobIdImpl.makeJobId("1000"), "job", 1000L));

        final TaskLauncher taskLauncher = TaskLauncherUtils.create(initializer, new TestTaskLauncherFactory(
            new Semaphore(0)));
        final TaskLauncher taskLauncherPA = PAActiveObject.turnActive(taskLauncher);

        TaskResultWaiter taskResultWaiter = new TaskResultWaiter();
        WaitForResultNotification waitForResultNotification = new WaitForResultNotification(taskResultWaiter);
        waitForResultNotification = PAActiveObject.turnActive(waitForResultNotification);
        taskLauncherPA.doTask(executableContainer, null, waitForResultNotification);

        assertEquals("done", taskResultWaiter.getTaskResult().value());

        assertTaskLauncherIsTerminated(taskLauncherPA);
    }

    @Test
    @Repeat(value = repetitions, parallel = parallel, timeout = timeout)
    public void kill_when_finished() throws Throwable {

        final ScriptExecutableContainer executableContainer = new ScriptExecutableContainer(new TaskScript(
          new SimpleScript("result='done'", "javascript")));

        TaskLauncherInitializer initializer = new TaskLauncherInitializer();
        initializer.setTaskId(TaskIdImpl.createTaskId(JobIdImpl.makeJobId("1000"), "job", 1000L));

        final TaskLauncher taskLauncher = TaskLauncherUtils.create(initializer, new TestTaskLauncherFactory(
          new Semaphore(0)));
        final TaskLauncher taskLauncherPA = PAActiveObject.turnActive(taskLauncher);

        TaskResultWaiter taskResultWaiter = new TaskResultWaiter();
        WaitForResultNotification waitForResultNotification = new WaitForResultNotification(taskResultWaiter);
        waitForResultNotification = PAActiveObject.turnActive(waitForResultNotification);
        taskLauncherPA.doTask(executableContainer, null, waitForResultNotification);

        assertEquals("done", taskResultWaiter.getTaskResult().value());

        try {
            taskLauncherPA.kill();
        } catch (Exception ignored) {
            // task launcher can be terminated before the kill message is received
        }


        assertTaskLauncherIsTerminated(taskLauncherPA);
    }

    @Test
    @Repeat(value = repetitions, parallel = parallel, timeout = timeout)
    public void kill_when_copying() throws Throwable {

        final ScriptExecutableContainer executableContainer = new ScriptExecutableContainer(new TaskScript(
            new SimpleScript("result='done'", "javascript")));

        TaskLauncherInitializer initializer = new TaskLauncherInitializer();
        initializer.setTaskId(TaskIdImpl.createTaskId(JobIdImpl.makeJobId("1000"), "job", 1000L));

        Semaphore taskRunning = new Semaphore(0);
        final TaskLauncher taskLauncher = TaskLauncherUtils.create(initializer,
                new SlowDataspacesTaskLauncherFactory(taskRunning));
        final TaskLauncher taskLauncherPA = PAActiveObject.turnActive(taskLauncher);

        taskLauncherPA.doTask(executableContainer, null, null);

        taskRunning.acquire();
        taskLauncherPA.kill();

        assertTaskLauncherIsTerminated(taskLauncherPA);
    }

    private void assertTaskLauncherIsTerminated(TaskLauncher taskLauncherPA) throws InterruptedException {
        try {
            while (PAActiveObject.pingActiveObject(taskLauncherPA)) {
                Thread.sleep(10);
            }
        } catch (Throwable expected) {
            // expected when PA object dies
        }
    }

    public class TaskResultWaiter {
        private volatile TaskResult taskResult;

        public void setTaskResult(TaskResult taskResult) {
            synchronized (this) {
                this.taskResult = taskResult;
                notify();
            }
        }

        public TaskResult getTaskResult() throws InterruptedException {
            synchronized (this) {
                while (taskResult == null) {
                    wait(30000);
                }
            }
            return taskResult;
        }
    }

    public static class WaitForResultNotification implements TaskTerminateNotification, Serializable {

        private TaskResultWaiter taskResultWaiter;

        public WaitForResultNotification(TaskResultWaiter taskResultWaiter) {
            this.taskResultWaiter = taskResultWaiter;
        }

        // Needed for ProActive
        public WaitForResultNotification() {
        }

        @Override
        public void terminate(TaskId taskId, TaskResult taskResult) {
            this.taskResultWaiter.setTaskResult(taskResult);
        }

    }

}