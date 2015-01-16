package org.ow2.proactive.scheduler.newimpl;

import java.io.File;

import org.ow2.proactive.scheduler.job.JobIdImpl;
import org.ow2.proactive.scheduler.task.TaskIdImpl;
import org.ow2.proactive.scheduler.task.TaskIdPojo;
import org.ow2.proactive.scheduler.task.TaskLauncherInitializer;
import org.ow2.proactive.scheduler.task.TaskResultImpl;
import org.ow2.proactive.scheduler.task.script.ForkedScriptExecutableContainer;
import org.ow2.proactive.scripting.SimpleScript;
import org.ow2.proactive.scripting.TaskScript;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.junit.Assert.*;


public class ForkedTaskExecutorTest {

    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();
    private String oldJavaHome;

    @Test
    public void result() throws Throwable {
        TestTaskOutput taskOutput = new TestTaskOutput();

        ForkedTaskExecutor taskExecutor = new ForkedTaskExecutor(tmpFolder.newFolder(), null);

        TaskLauncherInitializer initializer = new TaskLauncherInitializer();
        initializer.setTaskId(TaskIdPojo.createTaskId(TaskIdImpl.createTaskId(JobIdImpl.makeJobId("1000"),
                "job", 1000L, false)));

        TaskResultImpl result = taskExecutor.execute(new TaskContext(new ForkedScriptExecutableContainer(
            new TaskScript(new SimpleScript("print('hello'); result='hello'", "javascript"))), initializer),
                taskOutput.outputStream, taskOutput.error);

        assertEquals("hello\n", taskOutput.output());
        assertEquals("hello", result.value());
    }

    @Test
    public void failToSerialize() throws Throwable {
        TestTaskOutput taskOutput = new TestTaskOutput();

        ForkedTaskExecutor taskExecutor = new ForkedTaskExecutor(new File("non_existing_folder"), null);

        TaskLauncherInitializer initializer = new TaskLauncherInitializer();
        initializer.setTaskId(TaskIdPojo.createTaskId(TaskIdImpl.createTaskId(JobIdImpl.makeJobId("1000"),
                "job", 1000L, false)));

        TaskResultImpl result = taskExecutor.execute(new TaskContext(new ForkedScriptExecutableContainer(
            new TaskScript(new SimpleScript("print('hello'); result='hello'", "javascript"))), initializer),
                taskOutput.outputStream, taskOutput.error);

        assertNotEquals("", taskOutput.error());
        assertNotNull(result.getException());
    }

    @Test
    public void failToFindJava() throws Throwable {
        System.setProperty("java.home", "does not exist");
        TestTaskOutput taskOutput = new TestTaskOutput();

        ForkedTaskExecutor taskExecutor = new ForkedTaskExecutor(new File("non_existing_folder"), null);

        TaskLauncherInitializer initializer = new TaskLauncherInitializer();
        initializer.setTaskId(TaskIdPojo.createTaskId(TaskIdImpl.createTaskId(JobIdImpl.makeJobId("1000"),
                "job", 1000L, false)));

        TaskResultImpl result = taskExecutor.execute(new TaskContext(new ForkedScriptExecutableContainer(
            new TaskScript(new SimpleScript("print('hello'); result='hello'", "javascript"))), initializer),
                taskOutput.outputStream, taskOutput.error);

        assertNotEquals("", taskOutput.error());
        assertNotNull(result.getException());
    }

    @Before
    public void setUp() throws Exception {
        oldJavaHome = System.getProperty("java.home");
    }

    @After
    public void tearDown() throws Exception {
        System.setProperty("java.home", oldJavaHome);
    }
}