package org.ow2.proactive.scheduler.task;

import org.ow2.proactive.scheduler.task.containers.ScriptExecutableContainer;
import org.ow2.proactive.scripting.SimpleScript;
import org.ow2.proactive.scripting.TaskScript;
import org.junit.Test;

import static org.junit.Assert.*;


public class TaskContextTest {

    @Test
    public void nodesURLsAndHostsCanBeRepeated() throws Exception {
        TaskContext context = new TaskContext(new ScriptExecutableContainer(
          new TaskScript(new SimpleScript("", "javascript"))), new TaskLauncherInitializer());

        context.getNodesURLs().add("host1");
        context.getNodesURLs().add("host1");
        assertEquals(2, context.getNodesURLs().size());

        context.getNodesHosts().add("host1");
        context.getNodesHosts().add("host1");
        assertEquals(2, context.getNodesHosts().size());
    }
}