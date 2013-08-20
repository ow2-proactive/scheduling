package functionaltests.schedulerdb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import junit.framework.Assert;

import org.hibernate.Session;
import org.hibernate.metadata.ClassMetadata;
import org.junit.Test;
import org.ow2.proactive.db.types.BigString;
import org.ow2.proactive.scheduler.common.job.JobEnvironment;
import org.ow2.proactive.scheduler.common.job.JobStatus;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.ForkEnvironment;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.NativeTask;
import org.ow2.proactive.scheduler.common.task.Task;
import org.ow2.proactive.scheduler.common.task.dataspaces.InputAccessMode;
import org.ow2.proactive.scheduler.common.task.dataspaces.OutputAccessMode;
import org.ow2.proactive.scheduler.common.task.flow.FlowScript;
import org.ow2.proactive.scheduler.core.db.JobClasspathContent;
import org.ow2.proactive.scheduler.core.db.JobData;
import org.ow2.proactive.scheduler.core.db.TaskData;
import org.ow2.proactive.scheduler.core.db.TaskResultData;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.task.TaskResultImpl;
import org.ow2.proactive.scripting.GenerationScript;
import org.ow2.proactive.scripting.SelectionScript;
import org.ow2.proactive.scripting.SimpleScript;


public class TestJobRemove extends BaseSchedulerDBTest {

    @Test
    public void testRuntimeDataRemoveAfterFinish() throws Throwable {
        TaskFlowJob jobDef = createJob(2);
        InternalJob job = defaultSubmitJobAndLoadInternal(false, jobDef);

        dbManager.updateAfterTaskFinished(job, job.getTask("javaTask-0"), new TaskResultImpl(null, "OK1",
            null, 0, null));
        dbManager.updateAfterTaskFinished(job, job.getTask("forkedJavaTask-0"), new TaskResultImpl(null,
            "OK2", null, 0, null));
        dbManager.updateAfterTaskFinished(job, job.getTask("nativeTask-0"), new TaskResultImpl(null, "OK3",
            null, 0, null));

        job.setStatus(JobStatus.FINISHED);

        dbManager.updateAfterTaskFinished(job, null, null);

        checkAllEntitiesDeleted(JobData.class.getName(), TaskData.class.getName(), TaskResultData.class
                .getName());

        // check can still load task results

        Assert.assertEquals("OK1", dbManager.loadTaskResult(job.getTask("javaTask-0").getId(), 0).value());
        Assert.assertEquals("OK2", dbManager.loadTaskResult(job.getTask("forkedJavaTask-0").getId(), 0)
                .value());
        Assert.assertEquals("OK3", dbManager.loadTaskResult(job.getTask("nativeTask-0").getId(), 0).value());
    }

    @Test
    public void testRuntimeDataRemoveAfterFinishParallel() throws Throwable {
        int THREAD_COUNT = 4;
        ExecutorService executorService = Executors.newCachedThreadPool();

        List<InternalJob> jobs = new ArrayList<InternalJob>();
        TaskFlowJob jobDef;
        for (int i = 0; i < THREAD_COUNT; i++) {
            jobDef = createJob(2);
            jobs.add(defaultSubmitJobAndLoadInternal(false, jobDef));
        }

        for (int i = 0; i < THREAD_COUNT; i++) {
            final InternalJob job = jobs.get(i);
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        dbManager.updateAfterTaskFinished(job, job.getTask("javaTask-0"), new TaskResultImpl(
                            null, "OK1", null, 0, null));
                        dbManager.updateAfterTaskFinished(job, job.getTask("forkedJavaTask-0"),
                                new TaskResultImpl(null, "OK2", null, 0, null));
                        dbManager.updateAfterTaskFinished(job, job.getTask("nativeTask-0"),
                                new TaskResultImpl(null, "OK3", null, 0, null));

                        job.setStatus(JobStatus.FINISHED);

                        dbManager.updateAfterTaskFinished(job, null, null);

                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                    }
                }
            });
        }

        executorService.shutdown();
        executorService.awaitTermination(30, TimeUnit.SECONDS);

        for (int i = 0; i < THREAD_COUNT; i++) {
            final InternalJob job = jobs.get(i);

            List<InternalJob> jobsNotFinished = dbManager.loadNotFinishedJobs(true);
            Assert.assertEquals("All jobs should be finished", 0, jobsNotFinished.size());

            checkAllEntitiesDeleted(JobData.class.getName(), TaskData.class.getName(), TaskResultData.class
                    .getName());

            // check can still load task results

            Assert
                    .assertEquals("OK1", dbManager.loadTaskResult(job.getTask("javaTask-0").getId(), 0)
                            .value());
            Assert.assertEquals("OK2", dbManager.loadTaskResult(job.getTask("forkedJavaTask-0").getId(), 0)
                    .value());
            Assert.assertEquals("OK3", dbManager.loadTaskResult(job.getTask("nativeTask-0").getId(), 0)
                    .value());
        }

    }

    @Test
    public void testSetRemovedTime() throws Exception {
        InternalJob job = defaultSubmitJobAndLoadInternal(false, new TaskFlowJob());
        Assert.assertEquals(-1L, job.getRemovedTime());

        long removedTime = System.currentTimeMillis();
        dbManager.removeJob(job.getId(), removedTime, false);
        job = loadInternalJob(false, job.getId());

        Assert.assertEquals(removedTime, job.getRemovedTime());
    }

    @Test
    public void testFullDataRemove1() throws Exception {
        TaskFlowJob jobDef = new TaskFlowJob();
        JavaTask task1 = new JavaTask();
        task1.setName("task1");
        task1.setExecutableClassName(TestDummyExecutable.class.getName());
        jobDef.addTask(task1);

        InternalJob job = defaultSubmitJob(jobDef);
        System.out.println("Remove job");
        long start = System.currentTimeMillis();
        dbManager.removeJob(job.getId(), 0, true);
        System.out.println("Remove time (single task)" + (System.currentTimeMillis() - start));

        checkAllEntitiesDeleted();
    }

    @Test
    public void testFullDataRemove2() throws Exception {
        removeScenario(1);
    }

    @Test
    public void testFullDataRemoveManyTasks() throws Exception {
        removeScenario(100);
    }

    private TaskFlowJob createJob(int tasksNumber) throws Exception {
        JobEnvironment env = new JobEnvironment();
        env.setJobClasspath(new String[] { "lib/ProActive/ProActive.jar", "compile/lib/ant.jar" });

        TaskFlowJob jobDef = new TaskFlowJob();
        jobDef.setEnvironment(env);
        jobDef.addGenericInformation("k1", "v1");
        jobDef.addGenericInformation("k2", "v2");

        // add data with non-null ifBranch
        JavaTask A = createDefaultTask("A");
        FlowScript ifScript = FlowScript.createIfFlowScript("branch = \"if\";", "B", "C", null);
        A.setFlowScript(ifScript);
        jobDef.addTask(A);
        JavaTask B = createDefaultTask("B");
        jobDef.addTask(B);
        JavaTask C = createDefaultTask("C");
        jobDef.addTask(C);

        for (int i = 0; i < tasksNumber; i++) {
            JavaTask task1 = new JavaTask();
            task1.setName("javaTask-" + i);
            task1.setExecutableClassName(TestDummyExecutable.class.getName());
            task1.addArgument("arg1", "arg1");
            task1.addArgument("arg2", "arg2");
            setAttributes(task1);

            JavaTask task2 = new JavaTask();
            task2.setName("forkedJavaTask-" + i);
            task2.setExecutableClassName(TestDummyExecutable.class.getName());
            ForkEnvironment forkEnv = new ForkEnvironment();
            forkEnv.addAdditionalClasspath("lib/ProActive/ProActive.jar");
            forkEnv.addAdditionalClasspath("compile/lib/ant.jar");
            forkEnv.addJVMArgument("jvmArg1");
            forkEnv.addJVMArgument("jvmArg2");
            forkEnv.addSystemEnvironmentVariable("e1", "v1", false);
            forkEnv.addSystemEnvironmentVariable("e2", "v2", true);
            forkEnv.setEnvScript(new SimpleScript("env script", "javascript", new String[] { "param1",
                    "param2" }));
            task2.setForkEnvironment(forkEnv);
            task2.addArgument("arg1", "arg1");
            task2.addArgument("arg2", "arg2");
            setAttributes(task2);

            NativeTask task3 = new NativeTask();
            task3.setName("nativeTask-" + i);
            task3.setGenerationScript(new GenerationScript("gen script", "javascript", new String[] {
                    "param1", "param2" }));
            task3.setCommandLine("command1", "command2", "command3");
            setAttributes(task3);

            task1.addDependence(task2);
            task3.addDependence(task2);

            jobDef.addTask(task1);
            jobDef.addTask(task2);
            jobDef.addTask(task3);
        }

        return jobDef;
    }

    private void removeScenario(int tasksNumber) throws Exception {
        TaskFlowJob jobDef = createJob(tasksNumber);
        InternalJob job = defaultSubmitJobAndLoadInternal(false, jobDef);

        Map<String, BigString> resProperties = new HashMap<String, BigString>();
        resProperties.put("property1", new BigString("value1"));
        resProperties.put("property2", new BigString("value2"));

        for (int i = 0; i < tasksNumber; i++) {
            dbManager.updateAfterTaskFinished(job, job.getTask("javaTask-" + i), new TaskResultImpl(null,
                "OK", null, 0, resProperties));
            dbManager.updateAfterTaskFinished(job, job.getTask("javaTask-" + i), new TaskResultImpl(null,
                "OK", null, 0, resProperties));
            dbManager.updateAfterTaskFinished(job, job.getTask("forkedJavaTask-" + i), new TaskResultImpl(
                null, "OK", null, 0, resProperties));
            dbManager.updateAfterTaskFinished(job, job.getTask("forkedJavaTask-" + i), new TaskResultImpl(
                null, "OK", null, 0, resProperties));
            dbManager.updateAfterTaskFinished(job, job.getTask("nativeTask-" + i), new TaskResultImpl(null,
                "OK", null, 0, resProperties));
            dbManager.updateAfterTaskFinished(job, job.getTask("nativeTask-" + i), new TaskResultImpl(null,
                "OK", null, 0, resProperties));
        }

        System.out.println("Remove job");
        long start = System.currentTimeMillis();
        dbManager.removeJob(job.getId(), 0, true);
        System.out.println("Remove time (tasks: " + tasksNumber + ")" + (System.currentTimeMillis() - start));

        checkAllEntitiesDeleted();
    }

    private void setAttributes(Task task) throws Exception {
        task.addGenericInformation("k1", "v1");
        task.addGenericInformation("k2", "v2");
        SimpleScript script = new SimpleScript(task.getName() + "selection script", "javascript",
            new String[] { "param1", "param2" });
        SelectionScript ss1 = new SelectionScript(script, true);
        SelectionScript ss2 = new SelectionScript(script, false);
        task.addSelectionScript(ss1);
        task.addSelectionScript(ss2);

        task.setPreScript(new SimpleScript(task.getName() + "pre script", "javascript", new String[] {
                "param1", "param2" }));
        task.setPostScript(new SimpleScript(task.getName() + "post script", "javascript", new String[] {
                "param1", "param2" }));
        task.setCleaningScript(new SimpleScript(task.getName() + "clean script", "javascript", new String[] {
                "param1", "param2" }));
        task.setFlowScript(FlowScript.createContinueFlowScript());

        task.addInputFiles("f1", InputAccessMode.TransferFromGlobalSpace);
        task.addInputFiles("f2", InputAccessMode.TransferFromInputSpace);
        task.addInputFiles("f3", InputAccessMode.TransferFromUserSpace);
        task.addOutputFiles("f1", OutputAccessMode.TransferToGlobalSpace);
        task.addOutputFiles("f2", OutputAccessMode.TransferToOutputSpace);
        task.addOutputFiles("f3", OutputAccessMode.TransferToUserSpace);
    }

    private void checkAllEntitiesDeleted(String... skipClasses) {
        List<String> skip = new ArrayList<String>(Arrays.asList(skipClasses));
        skip.add(JobClasspathContent.class.getName());

        Session session = dbManager.getSessionFactory().openSession();
        try {
            for (ClassMetadata metadata : session.getSessionFactory().getAllClassMetadata().values()) {
                if (!skip.contains(metadata.getEntityName())) {
                    System.out.println("Check " + metadata.getEntityName());
                    List<Object> list = session.createCriteria(metadata.getEntityName()).list();
                    Assert.assertEquals("Unexpected " + metadata.getEntityName(), 0, list.size());
                }
            }
        } finally {
            session.close();
        }
    }
}
