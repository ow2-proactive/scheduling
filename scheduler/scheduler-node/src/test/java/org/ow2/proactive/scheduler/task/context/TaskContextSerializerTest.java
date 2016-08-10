package org.ow2.proactive.scheduler.task.context;

import java.io.File;
import java.io.IOException;

import org.objectweb.proactive.core.node.NodeException;
import org.ow2.proactive.scheduler.job.JobIdImpl;
import org.ow2.proactive.scheduler.task.TaskIdImpl;
import org.ow2.proactive.scheduler.task.TaskLauncherInitializer;
import org.ow2.proactive.scheduler.task.containers.ScriptExecutableContainer;
import org.ow2.proactive.scripting.ForkEnvironmentScript;
import org.ow2.proactive.scripting.InvalidScriptException;
import org.ow2.proactive.scripting.SimpleScript;
import org.ow2.proactive.scripting.TaskScript;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

public class TaskContextSerializerTest {


    @Test
    public void testSerializeContextToFile() throws Exception {
        TaskContext taskContext = createTaskContext();
        File serializedContextFile = new TaskContextSerializer().serializeContext(taskContext ,
                new File(System.getProperty("java.io.tmpdir")));
        serializedContextFile.deleteOnExit();

        assertThat("File is not written to disk, but it should.", serializedContextFile.exists(), is(true));
        assertThat("File must be readable", serializedContextFile.canRead(), is(true));
    }

    @Test(expected = java.io.IOException.class)
    public void testSerializeContextThrowsIoExceptionIfDirectoryIsInvalid() throws InvalidScriptException, NodeException, IOException {
        TaskContext taskContext = createTaskContext();
        File invalidDirectory = new File("/-->invalid!!/66/4/32/4/234/");

        new TaskContextSerializer().serializeContext(taskContext ,invalidDirectory);
    }

    private TaskContext createTaskContext() throws NodeException, InvalidScriptException {
        TaskLauncherInitializer taskLauncherInitializer = new TaskLauncherInitializer();
        taskLauncherInitializer.setTaskId(TaskIdImpl.createTaskId(
                new JobIdImpl(1L, "testSerializeContextToFile"),
                "testSerializeContextToFile",
                1L));
        // Mock TaskContext except of the tested method
        // Invoke method to test it
        return new TaskContext(
                new ScriptExecutableContainer(
                        new TaskScript(
                                new ForkEnvironmentScript(
                                        new SimpleScript("", "python")))),
                taskLauncherInitializer,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null);
    }

}