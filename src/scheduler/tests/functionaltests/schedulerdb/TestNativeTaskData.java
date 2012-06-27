package functionaltests.schedulerdb;

import org.junit.Assert;
import org.junit.Test;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.NativeTask;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.task.NativeExecutableContainer;
import org.ow2.proactive.scripting.GenerationScript;


public class TestNativeTaskData extends BaseSchedulerDBTest {

    @Test
    public void testNoScript() throws Exception {
        TaskFlowJob jobDef = new TaskFlowJob();
        NativeTask taskDef1 = new NativeTask();
        taskDef1.setName("task1");
        taskDef1.setCommandLine("command");
        jobDef.addTask(taskDef1);

        InternalJob job = defaultSubmitJob(jobDef);

        NativeExecutableContainer container = (NativeExecutableContainer) dbManager
                .loadExecutableContainer(job.getTask("task1"));
        Assert.assertNull(container.getGenerationScript());
        Assert.assertArrayEquals(new String[] { "command" }, container.getCommand());
    }

    @Test
    public void test() throws Exception {
        String[] stringArray = new String[100];
        StringBuilder largeStringBuilder = new StringBuilder();
        for (int i = 0; i < stringArray.length; i++) {
            stringArray[i] = "string-" + i;
            largeStringBuilder.append(stringArray[i]);
        }

        String largeString = largeStringBuilder.toString();

        TaskFlowJob jobDef = new TaskFlowJob();
        NativeTask taskDef1 = new NativeTask();
        taskDef1.setName("task1");
        taskDef1.setGenerationScript(new GenerationScript(largeString, "js", stringArray));
        taskDef1.setWorkingDir(largeString);
        taskDef1.setCommandLine(stringArray);
        jobDef.addTask(taskDef1);
        InternalJob job = defaultSubmitJob(jobDef);

        NativeExecutableContainer container = (NativeExecutableContainer) dbManager
                .loadExecutableContainer(job.getTask("task1"));
        Assert.assertEquals(largeString, container.getWorkingDir());
        Assert.assertArrayEquals(stringArray, container.getCommand());
        GenerationScript script = container.getGenerationScript();
        Assert.assertEquals("js", script.getEngineName());
        Assert.assertEquals(largeString, script.getScript());
        Assert.assertArrayEquals(stringArray, script.getParameters());
    }

}
