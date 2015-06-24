package org.ow2.proactive.scheduler.task;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.objectweb.proactive.extensions.dataspaces.vfs.selector.FileSelector;
import org.ow2.proactive.scheduler.common.TaskTerminateNotification;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.dataspaces.InputAccessMode;
import org.ow2.proactive.scheduler.common.task.dataspaces.InputSelector;
import org.ow2.proactive.scheduler.common.task.dataspaces.OutputAccessMode;
import org.ow2.proactive.scheduler.common.task.dataspaces.OutputSelector;
import org.ow2.proactive.scheduler.job.JobIdImpl;
import org.ow2.proactive.scheduler.task.containers.ForkedScriptExecutableContainer;
import org.ow2.proactive.scripting.SimpleScript;
import org.ow2.proactive.scripting.TaskScript;

import java.io.File;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class TaskLauncherDataSpacesTest {

    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();

    @Test
    public void input_file_using_job_id_in_its_selector() throws Throwable {
        ForkedScriptExecutableContainer executableContainer = new ForkedScriptExecutableContainer(
                new TaskScript(new SimpleScript("println new File('.').listFiles();", "groovy")));

        TaskLauncherInitializer initializer = new TaskLauncherInitializer();
        initializer.setTaskId(TaskIdImpl.createTaskId(JobIdImpl.makeJobId("1000"), "job", 1000L, false));
        initializer.setTaskInputFiles(singletonList(
                new InputSelector(new FileSelector("input_$PA_JOB_ID.txt"), InputAccessMode.TransferFromInputSpace)
        ));

        TestTaskLauncherFactory testTaskLauncherFactory = new TestTaskLauncherFactory(tmpFolder.newFolder());

        File inputFile = new File(testTaskLauncherFactory.getDataSpaces().getInputURI(), "input_1000.txt");
        assertTrue(inputFile.createNewFile());

        TaskLauncher taskLauncher = new TaskLauncher(initializer, testTaskLauncherFactory);
        TaskResult taskResult = runTaskLauncher(taskLauncher, executableContainer);

        assertFalse(taskResult.hadException());
        assertTrue(taskResult.getOutput().getAllLogs(false).contains("input_1000.txt"));
    }

    @Test
    public void output_file_using_task_id_in_its_selector() throws Throwable {
        ForkedScriptExecutableContainer executableContainer = new ForkedScriptExecutableContainer(
                new TaskScript(new SimpleScript("new File('output_' + variables.get('PA_TASK_ID') + '.txt').text = 'hello'", "groovy")));

        TaskLauncherInitializer initializer = new TaskLauncherInitializer();
        initializer.setTaskId(TaskIdImpl.createTaskId(JobIdImpl.makeJobId("1000"), "job", 1000L, false));
        initializer.setTaskOutputFiles(singletonList(
                new OutputSelector(new FileSelector("output_${PA_TASK_ID}.txt"), OutputAccessMode.TransferToGlobalSpace)
        ));

        TestTaskLauncherFactory testTaskLauncherFactory = new TestTaskLauncherFactory(tmpFolder.newFolder());

        TaskLauncher taskLauncher = new TaskLauncher(initializer, testTaskLauncherFactory);
        TaskResult taskResult = runTaskLauncher(taskLauncher, executableContainer);

        assertFalse(taskResult.hadException());
        assertTrue(new File(testTaskLauncherFactory.getDataSpaces().getGlobalURI(), "output_1000.txt").exists());
    }

    private TaskResult runTaskLauncher(TaskLauncher taskLauncher, ForkedScriptExecutableContainer executableContainer) {
        TaskTerminateNotificationVerifier taskResult = new TaskTerminateNotificationVerifier();

        taskLauncher.doTask(executableContainer, null, taskResult);

        return taskResult.result;
    }

    private static class TaskTerminateNotificationVerifier implements TaskTerminateNotification {
        TaskResult result;

        @Override
        public void terminate(TaskId taskId, TaskResult taskResult) {
            this.result = taskResult;
        }
    }

}