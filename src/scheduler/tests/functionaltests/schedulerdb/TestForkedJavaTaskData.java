package functionaltests.schedulerdb;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.ForkEnvironment;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.task.ForkedJavaExecutableContainer;
import org.ow2.proactive.scheduler.task.launcher.InternalForkEnvironment;
import org.ow2.proactive.scripting.SimpleScript;


public class TestForkedJavaTaskData extends BaseSchedulerDBTest {

    @Test
    public void test() throws Exception {
        TaskFlowJob jobDef = new TaskFlowJob();

        ForkEnvironment initialForkEnv = new ForkEnvironment();
        initialForkEnv.setJavaHome("javaHome");
        initialForkEnv.setWorkingDir("workingDir");
        for (int i = 0; i < 10; i++) {
            initialForkEnv.addAdditionalClasspath("cp-" + i);
            initialForkEnv.addJVMArgument("arg-" + i);
        }
        initialForkEnv.addSystemEnvironmentVariable("n1", "v1", true);
        initialForkEnv.addSystemEnvironmentVariable("n2", "v2", false);
        initialForkEnv.addSystemEnvironmentVariable("n3", "v3", ',');

        SimpleScript script = new SimpleScript("script", "js", new String[] { "a", "b", "c" });
        initialForkEnv.setEnvScript(script);

        JavaTask taskDef1 = createDefaultTask("task1");
        taskDef1.setExecutableClassName(TestDummyExecutable.class.getName());
        taskDef1.setForkEnvironment(initialForkEnv);
        jobDef.addTask(taskDef1);

        InternalJob job = defaultSubmitJob(jobDef);

        ForkedJavaExecutableContainer container = (ForkedJavaExecutableContainer) dbManager
                .loadExecutableContainer(job.getTask("task1"));
        Assert.assertEquals(TestDummyExecutable.class.getName(), container.getUserExecutableClassName());

        ForkEnvironment restoredForkEnv = container.getForkEnvironment();
        Assert.assertEquals("javaHome", restoredForkEnv.getJavaHome());
        Assert.assertEquals("workingDir", restoredForkEnv.getWorkingDir());
        Assert.assertEquals(10, restoredForkEnv.getAdditionalClasspath().size());
        Assert.assertEquals(10, restoredForkEnv.getJVMArguments().size());
        Assert.assertEquals("script", restoredForkEnv.getEnvScript().getScript());
        Assert.assertEquals(3, restoredForkEnv.getEnvScript().getParameters().length);

        Map<String, String> envMap = new HashMap<String, String>();
        envMap.put("n1", "ov1");
        envMap.put("n2", "ov2");
        envMap.put("n3", "ov3");

        Map<String, String> expectedEnvMap = new HashMap<String, String>();
        expectedEnvMap.put("n1", "ov1v1");
        expectedEnvMap.put("n2", "v2");
        expectedEnvMap.put("n3", "ov3,v3");

        InternalForkEnvironment env = new InternalForkEnvironment(restoredForkEnv, envMap);
        Assert.assertEquals(expectedEnvMap, env.getSystemEnvironment());
    }

}
