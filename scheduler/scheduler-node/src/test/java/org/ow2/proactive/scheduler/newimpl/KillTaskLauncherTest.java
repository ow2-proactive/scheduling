package org.ow2.proactive.scheduler.newimpl;

import java.io.File;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.List;
import java.util.concurrent.Semaphore;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.extensions.dataspaces.core.naming.NamingService;
import org.objectweb.proactive.extensions.dataspaces.exceptions.FileSystemException;
import org.ow2.proactive.scheduler.common.TaskTerminateNotification;
import org.ow2.proactive.scheduler.common.exception.TaskAbortedException;
import org.ow2.proactive.scheduler.common.task.Decrypter;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.dataspaces.InputSelector;
import org.ow2.proactive.scheduler.common.task.dataspaces.OutputSelector;
import org.ow2.proactive.scheduler.job.JobIdImpl;
import org.ow2.proactive.scheduler.task.TaskIdImpl;
import org.ow2.proactive.scheduler.task.TaskLauncherInitializer;
import org.ow2.proactive.scheduler.task.TaskResultImpl;
import org.ow2.proactive.scheduler.task.script.ForkedScriptExecutableContainer;
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

    private class TestTaskLauncherFactory extends TaskLauncherFactory {
        private Semaphore taskRunning;

        public TestTaskLauncherFactory(Semaphore taskRunning) {
            this.taskRunning = taskRunning;
        }

        @Override
        public TaskDataspaces createTaskDataspaces(TaskId taskId, NamingService namingService) {
            return new TaskFileDataspaces();
        }

        @Override
        public TaskExecutor createTaskExecutor(final File workingDir, final Decrypter decrypter) {
            return new TaskExecutor() {
                @Override
                public TaskResultImpl execute(TaskContext container, PrintStream output, PrintStream error) {
                    taskRunning.release();
                    return new ForkerTaskExecutor(workingDir, decrypter).execute(container, output, error);
                }
            };
        }
    }

    private class TaskFileDataspaces implements TaskDataspaces {

        @Override
        public File getScratchFolder() {
            return new File(".");
        }

        @Override
        public void copyInputDataToScratch(List<InputSelector> inputFiles) throws FileSystemException {

        }

        @Override
        public void copyScratchDataToOutput(List<OutputSelector> outputFiles) throws FileSystemException {

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

    private class SlowDataspacesTaskLauncherFactory extends TaskLauncherFactory {
        private Semaphore taskRunning;

        public SlowDataspacesTaskLauncherFactory(Semaphore taskRunning) {
            this.taskRunning = taskRunning;
        }

        @Override
        public TaskDataspaces createTaskDataspaces(TaskId taskId, NamingService namingService) {
            return new SlowDataspaces(taskRunning);
        }

        @Override
        public TaskExecutor createTaskExecutor(File workingDir, Decrypter decrypter) {
            return new NonForkedTaskExecutor();
        }

    }

    private class SlowDataspaces implements TaskDataspaces {

        private Semaphore taskRunning;

        public SlowDataspaces(Semaphore taskRunning) {
            this.taskRunning = taskRunning;
        }

        @Override
        public File getScratchFolder() {
            return new File(".");
        }

        @Override
        public void copyInputDataToScratch(List<InputSelector> inputFiles) throws FileSystemException {
            taskRunning.release();
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void copyScratchDataToOutput(List<OutputSelector> outputFiles) throws FileSystemException {

        }
    }
}