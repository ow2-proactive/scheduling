package org.ow2.proactive.scheduler.task;

import org.objectweb.proactive.core.node.NodeException;
import org.ow2.proactive.scheduler.job.JobIdImpl;
import org.ow2.proactive.scheduler.task.containers.ScriptExecutableContainer;
import org.ow2.proactive.scheduler.task.context.TaskContext;
import org.ow2.proactive.scripting.ForkEnvironmentScript;
import org.ow2.proactive.scripting.InvalidScriptException;
import org.ow2.proactive.scripting.SimpleScript;
import org.ow2.proactive.scripting.TaskScript;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TaskContextTest {

    @Test
    public void nodesURLsAndHostsCanBeRepeated() throws Exception {
        TaskContext context = createTaskContext();

        context.getOtherNodesURLs().add("host1");
        context.getOtherNodesURLs().add("host1");
        assertEquals(2, context.getOtherNodesURLs().size());

        context.getNodesHosts().add("host1");
        context.getNodesHosts().add("host1");
        assertEquals(2, context.getNodesHosts().size());
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