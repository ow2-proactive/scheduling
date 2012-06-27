package functionaltests.schedulerdb;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.ForkEnvironment;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.task.JavaExecutableContainer;


public class TestJavaTaskData extends BaseSchedulerDBTest {

    public static class Arg implements Serializable {

        private final String data;

        public Arg(String data) {
            this.data = data;
        }

        public boolean equals(Object obj) {
            return ((Arg) obj).data.equals(data);
        }
    }

    private void setArgs(JavaTask task, Map<String, Serializable> args) {
        for (Map.Entry<String, Serializable> entry : args.entrySet()) {
            task.addArgument(entry.getKey(), entry.getValue());
        }
    }

    @Test
    public void testJavaTask() throws Exception {
        test(null);
    }

    @Test
    public void testForkedJavaTask() throws Exception {
        ForkEnvironment env = new ForkEnvironment();
        test(env);
    }

    @Test
    public void testIdMapping() throws Exception {
        /*
         * during development there was a bug when java task and forked java
         * task couldn't have property with the same name, this test case
         * tries to catch this bug
         */

        TaskFlowJob jobDef = new TaskFlowJob();

        Map<String, Serializable> args = new HashMap<String, Serializable>();
        args.put("arg1", new Arg("v1"));

        JavaTask taskDef1 = createDefaultTask("task1");
        taskDef1.setExecutableClassName(TestDummyExecutable.class.getName());
        setArgs(taskDef1, args);
        jobDef.addTask(taskDef1);

        JavaTask taskDef2 = createDefaultTask("task2");
        taskDef2.setExecutableClassName(TestDummyExecutable.class.getName());
        setArgs(taskDef2, args);
        jobDef.addTask(taskDef2);
        taskDef2.setForkEnvironment(new ForkEnvironment());

        InternalJob job = defaultSubmitJob(jobDef);

        Assert.assertNotNull(dbManager.loadExecutableContainer(job.getTask("task1")));
        Assert.assertNotNull(dbManager.loadExecutableContainer(job.getTask("task2")));
    }

    private void test(ForkEnvironment env) throws Exception {
        Map<String, Serializable> args1 = new HashMap<String, Serializable>();
        args1.put("arg1", new Arg("v1"));
        args1.put("arg2", new Arg("v2"));
        args1.put("arg3", new Arg("v3"));
        ArrayList<Arg> args = new ArrayList<Arg>();
        for (int i = 0; i < 1000; i++) {
            args.add(new Arg("Argument-" + i));
        }
        args1.put("arg4", args);

        TaskFlowJob jobDef = new TaskFlowJob();
        JavaTask taskDef1 = createDefaultTask("task1");
        taskDef1.setExecutableClassName(TestDummyExecutable.class.getName());
        setArgs(taskDef1, args1);
        taskDef1.setForkEnvironment(env);
        jobDef.addTask(taskDef1);

        Map<String, Serializable> args2 = new HashMap<String, Serializable>();
        args2.put("task2 arg1", new Arg("task2 v1"));

        JavaTask taskDef2 = createDefaultTask("task2");
        taskDef2.setExecutableClassName(TestDummyExecutable.class.getName());
        taskDef2.setForkEnvironment(env);
        setArgs(taskDef2, args2);
        jobDef.addTask(taskDef2);

        JavaTask taskDef3 = createDefaultTask("task3");
        taskDef3.setExecutableClassName(TestDummyExecutable.class.getName());
        taskDef3.setForkEnvironment(env);
        jobDef.addTask(taskDef3);

        InternalJob job = defaultSubmitJob(jobDef);

        System.out.println("Load container1");
        JavaExecutableContainer container = (JavaExecutableContainer) dbManager.loadExecutableContainer(job
                .getTask("task1"));
        Assert.assertEquals(TestDummyExecutable.class.getName(), container.getUserExecutableClassName());
        Map<String, Serializable> loadedArgs = container.createExecutableInitializer().getArguments(
                Thread.currentThread().getContextClassLoader());
        Assert.assertEquals(args1, loadedArgs);

        System.out.println("Load container2");
        container = (JavaExecutableContainer) dbManager.loadExecutableContainer(job.getTask("task2"));
        loadedArgs = container.createExecutableInitializer().getArguments(
                Thread.currentThread().getContextClassLoader());
        Assert.assertEquals(args2, loadedArgs);

        System.out.println("Load container3");
        container = (JavaExecutableContainer) dbManager.loadExecutableContainer(job.getTask("task3"));
        loadedArgs = container.createExecutableInitializer().getArguments(
                Thread.currentThread().getContextClassLoader());
        Assert.assertEquals(new HashMap<String, Serializable>(), loadedArgs);
    }

}
