package org.ow2.proactive.scheduler.task;

import java.io.Serializable;
import java.util.concurrent.Semaphore;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.ow2.proactive.scheduler.common.TaskTerminateNotification;
import org.ow2.proactive.scheduler.common.exception.TaskAbortedException;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.job.JobIdImpl;
import org.ow2.proactive.scheduler.task.containers.ForkedScriptExecutableContainer;
import org.ow2.proactive.scripting.SimpleScript;
import org.ow2.proactive.scripting.TaskScript;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;


public class KillTaskLauncherTest {

    @Before
    public void setUp() throws Exception {
        CentralPAPropertyRepository.PA_CLASSLOADING_USEHTTP.setValue(false);
    }

    @Test(timeout = 5000)
    public void kill_while_sleeping_in_task() throws Exception {

        final ForkedScriptExecutableContainer executableContainer = new ForkedScriptExecutableContainer(
            new TaskScript(new SimpleScript("java.lang.Thread.sleep(10000)", "javascript")));

        TaskLauncherInitializer initializer = new TaskLauncherInitializer();
        initializer.setTaskId(TaskIdImpl.createTaskId(JobIdImpl.makeJobId("1000"), "job", 1000L, false));

        Semaphore taskRunning = new Semaphore(0);

        final TaskLauncher taskLauncher = new TaskLauncher(initializer, new TestTaskLauncherFactory(taskRunning));
        final TaskLauncher taskLauncherPA = PAActiveObject.turnActive(taskLauncher);

        TaskResultWaiter taskResultWaiter = new TaskResultWaiter();
        Thread launchTaskInBackground = runTaskLauncher(executableContainer, taskLauncherPA, taskResultWaiter);

        taskRunning.acquire();
        taskLauncher.terminate(false);

        launchTaskInBackground.join();
        assertEquals(TaskAbortedException.class, taskResultWaiter.getTaskResult().getException().getClass());
    }

    @Test(timeout = 5000)
    public void kill_while_looping_in_task() throws Exception {

        final ForkedScriptExecutableContainer executableContainer = new ForkedScriptExecutableContainer(
          new TaskScript(new SimpleScript("for(;;){}", "javascript")));

        TaskLauncherInitializer initializer = new TaskLauncherInitializer();
        initializer.setTaskId(TaskIdImpl.createTaskId(JobIdImpl.makeJobId("1000"), "job", 1000L, false));

        Semaphore taskRunning = new Semaphore(0);
        final TaskLauncher taskLauncher = new TaskLauncher(initializer, new TestTaskLauncherFactory(taskRunning));
        final TaskLauncher taskLauncherPA = PAActiveObject.turnActive(taskLauncher);

        TaskResultWaiter taskResultWaiter = new TaskResultWaiter();
        Thread launchTaskInBackground = runTaskLauncher(executableContainer, taskLauncherPA, taskResultWaiter);

        taskRunning.acquire();
        taskLauncher.terminate(false);

        launchTaskInBackground.join();
        assertEquals(TaskAbortedException.class, taskResultWaiter.getTaskResult().getException().getClass());
    }

    @Test(timeout = 5000)
    public void kill_when_finished() throws Throwable {

        final ForkedScriptExecutableContainer executableContainer = new ForkedScriptExecutableContainer(
          new TaskScript(new SimpleScript("result='done'", "javascript")));

        TaskLauncherInitializer initializer = new TaskLauncherInitializer();
        initializer.setTaskId(TaskIdImpl.createTaskId(JobIdImpl.makeJobId("1000"), "job", 1000L, false));

        final TaskLauncher taskLauncher = new TaskLauncher(initializer, new TestTaskLauncherFactory(
          new Semaphore(0)));
        final TaskLauncher taskLauncherPA = PAActiveObject.turnActive(taskLauncher);

        TaskResultWaiter taskResultWaiter = new TaskResultWaiter();
        Thread launchTaskInBackground = runTaskLauncher(executableContainer, taskLauncherPA, taskResultWaiter);

        launchTaskInBackground.join();
        taskLauncher.terminate(false);

        assertEquals("done", taskResultWaiter.getTaskResult().value());
    }

    @Test(timeout = 5000)
    public void kill_when_copying() throws Throwable {

        final ForkedScriptExecutableContainer executableContainer = new ForkedScriptExecutableContainer(
          new TaskScript(new SimpleScript("result='done'", "javascript")));

        TaskLauncherInitializer initializer = new TaskLauncherInitializer();
        initializer.setTaskId(TaskIdImpl.createTaskId(JobIdImpl.makeJobId("1000"), "job", 1000L, false));

        Semaphore taskRunning = new Semaphore(0);
        final TaskLauncher taskLauncher = new TaskLauncher(initializer, new SlowDataspacesTaskLauncherFactory(taskRunning));
        final TaskLauncher taskLauncherPA = PAActiveObject.turnActive(taskLauncher);

        TaskResultWaiter taskResultWaiter = new TaskResultWaiter();
        Thread launchTaskInBackground = runTaskLauncher(executableContainer, taskLauncherPA, taskResultWaiter);

        taskRunning.acquire();
        taskLauncher.terminate(false);
        launchTaskInBackground.join();

        assertEquals(TaskAbortedException.class, taskResultWaiter.getTaskResult().getException().getClass());
    }

    private Thread runTaskLauncher(final ForkedScriptExecutableContainer executableContainer,
      final TaskLauncher taskLauncherPA, final TaskResultWaiter taskResultWaiter) {
        Thread launchTaskInBackground = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    WaitForResultNotification waitForResultNotification = new WaitForResultNotification(taskResultWaiter);
                    waitForResultNotification = PAActiveObject.turnActive(waitForResultNotification);
                    taskLauncherPA.doTask(executableContainer, null, waitForResultNotification);
                    taskResultWaiter.getTaskResult();
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        });
        launchTaskInBackground.start();
        return launchTaskInBackground;
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