package functionaltests.job.recover;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.objectweb.proactive.utils.OperatingSystem;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.SchedulerState;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.ForkEnvironment;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.NativeTask;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.executable.JavaExecutable;
import org.ow2.proactive.scripting.SelectionScript;
import org.ow2.proactive.scripting.SimpleScript;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import functionaltests.utils.SchedulerFunctionalTest;
import functionaltests.utils.SchedulerTHelper;

import static functionaltests.utils.SchedulerTHelper.log;
import static org.junit.Assert.*;


/**
 * Test checks that runtime state of tasks is properly 
 * restored after scheduler is killed and restarted.
 * <p/>
 * Test checks following data is restored properly:
 *<ul>
 *<li>pre, post, clean, and selection scripts
 *<li>task arguments
 *<li>data required to execute task (executable task name, command line, fork env)
 *<li>results for dependents task
 *</ul>
 */
public class TestTaskRestore extends SchedulerFunctionalTest {

    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();

    static final String TASK1_RES = "TestJavaTask1 OK";

    static final String TASK2_RES = "TestJavaTask2 OK";

    public static class TestJavaTask1 extends JavaExecutable {

        private String param1;

        @Override
        public Serializable execute(TaskResult... results) throws Throwable {
            if (!"javaTask1".equals(param1)) {
                throw new Exception("Unexpected param value: " + param1);
            }
            return TASK1_RES;
        }

    }

    public static class TestJavaTask2 extends JavaExecutable {

        private String param1;

        @Override
        public Serializable execute(TaskResult... results) throws Throwable {
            if (!"javaTask2".equals(param1)) {
                throw new Exception("Unexpected param value: " + param1);
            }

            Thread.sleep(5000);
            if (results.length != 1) {
                return "Results length: " + results.length;
            }
            TaskResult res = results[0];
            String val = (String) res.value();
            if (!val.equals(TASK1_RES)) {
                return "Unexpected parent res: " + val;
            }
            getOut().println("OK " + val);
            return TASK2_RES;
        }

    }

    private static String CREATED_FILES_NAMES[] = { "TestTaskRestore_clean2.tmp",
            "TestTaskRestore_clean3.tmp", "TestTaskRestore_sel2.tmp", "TestTaskRestore_sel3.tmp",
            "TestTaskRestore_env2.tmp" };

    // clean scripts are handled asynchronously, need wait some time
    private void checkFilesWereCreated() throws Exception {
        long finishTime = System.currentTimeMillis() + 60000;

        List<File> expectedFiles = new ArrayList<>();
        for (String fileName : CREATED_FILES_NAMES) {
            expectedFiles.add(new File(tmpFolder.getRoot(), fileName));
        }

        while (System.currentTimeMillis() < finishTime) {
            for (Iterator<File> i = expectedFiles.iterator(); i.hasNext();) {
                File file = i.next();
                if (file.exists()) {
                    i.remove();
                }
            }
            if (expectedFiles.isEmpty()) {
                return;
            } else {
                Thread.sleep(100);
            }
        }

        for (File file : expectedFiles) {
            fail("File " + file.getAbsolutePath() + " wasn't created");
        }
    }

    @Test
    public void test() throws Throwable {
        tmpFolder.create();

        TaskFlowJob job = new TaskFlowJob();
        job.setName(this.getClass().getSimpleName());

        JavaTask javaTask1 = new JavaTask();
        javaTask1.setName("task1");
        javaTask1.setExecutableClassName(TestJavaTask1.class.getName());
        javaTask1.addArgument("param1", "javaTask1");
        javaTask1.setPreScript(createScriptWithOutput("prescript1"));
        javaTask1.setPostScript(createScriptWithOutput("postscript1"));

        JavaTask javaTask2 = new JavaTask();
        javaTask2.setName("task2");
        javaTask2.setExecutableClassName(TestJavaTask2.class.getName());
        javaTask2.addDependence(javaTask1);
        javaTask2.addArgument("param1", "javaTask2");
        javaTask2.setPreScript(createScriptWithOutput("prescript2"));
        javaTask2.setPostScript(createScriptWithOutput("postscript2"));
        javaTask2.setCleaningScript(createFileCreatingScript(new File(tmpFolder.getRoot(),
            "TestTaskRestore_clean2.tmp")));
        javaTask2.setSelectionScript(createFileCreatingSelectionScript(new File(tmpFolder.getRoot(),
            "TestTaskRestore_sel2.tmp")));
        ForkEnvironment env = new ForkEnvironment();
        env.setEnvScript(createFileCreatingScript(new File(tmpFolder.getRoot(), "TestTaskRestore_env2.tmp")));
        javaTask2.setForkEnvironment(env);

        NativeTask nativeTask1 = new NativeTask();
        nativeTask1.setName("task3");
        nativeTask1.setPreScript(createScriptWithOutput("prescript3"));
        nativeTask1.setPostScript(createScriptWithOutput("postscript3"));
        nativeTask1.setCleaningScript(createFileCreatingScript(new File(tmpFolder.getRoot(),
            "TestTaskRestore_clean3.tmp")));
        nativeTask1.setSelectionScript(createFileCreatingSelectionScript(new File(tmpFolder.getRoot(),
            "TestTaskRestore_sel3.tmp")));

        File script;
        if (OperatingSystem.getOperatingSystem() == OperatingSystem.unix) {
            script = new File(getClass().getResource("/functionaltests/executables/test_echo_task.sh")
                    .getFile());
        } else {
            script = new File(getClass().getResource("/functionaltests/executables/test_echo_task.bat")
                    .getFile());
        }

        if (!script.exists()) {
            fail("Can't find script " + script.getAbsolutePath());
        }
        nativeTask1.setCommandLine(script.getAbsolutePath());
        nativeTask1.addDependence(javaTask1);

        job.addTask(javaTask1);
        job.addTask(javaTask2);
        job.addTask(nativeTask1);

        log("Submit pending job");
        schedulerHelper.submitJob(createPendingJob());

        log("Submit job");
        JobId jobId = schedulerHelper.submitJob(job);
        log("Submitted job " + jobId);

        log("Waiting for task1 to finish");
        schedulerHelper.waitForEventTaskFinished(jobId, "task1");

        log("Killing scheduler");

        schedulerHelper.killSchedulerAndNodesAndRestart(new File(SchedulerTHelper.class.getResource(
                "/functionaltests/config/functionalTSchedulerProperties-updateDB.ini").toURI()).getAbsolutePath());

        Scheduler scheduler = schedulerHelper.getSchedulerInterface();

        log("get state");
        SchedulerState state = scheduler.getState();
        assertEquals(1, state.getRunningJobs().size());
        assertEquals(1, state.getPendingJobs().size());

        log("State: " + state.getPendingJobs().size() + " " + state.getRunningJobs().size() + " " +
            state.getFinishedJobs().size());

        JobState jobState = state.getRunningJobs().get(0);
        assertEquals(1, jobState.getNumberOfFinishedTasks());
        assertEquals(2, jobState.getNumberOfPendingTasks() + jobState.getNumberOfRunningTasks());

        JobState pendingJobState = state.getPendingJobs().get(0);
        assertEquals(-1, pendingJobState.getStartTime());
        assertEquals(-1, pendingJobState.getFinishedTime());
        assertEquals(-1, pendingJobState.getRemovedTime());

        schedulerHelper.waitForEventJobFinished(jobId);

        log("Job finished");

        JobResult jobResult = scheduler.getJobResult(jobId);
        printResultAndCheckNoErrors(jobResult);
        TaskResult taskResult;

        taskResult = jobResult.getResult("task1");
        assertTrue(taskResult.getOutput().getAllLogs(false).contains("postscript1"));
        assertTrue(taskResult.getOutput().getAllLogs(false).contains("prescript1"));
        assertEquals(TASK1_RES, taskResult.value());

        taskResult = jobResult.getResult("task2");
        assertEquals(TASK2_RES, taskResult.value());
        assertTrue(taskResult.getOutput().getAllLogs(false).contains("postscript2"));
        assertTrue(taskResult.getOutput().getAllLogs(false).contains("prescript2"));

        taskResult = jobResult.getResult("task3");
        assertTrue(taskResult.getOutput().getAllLogs(false).contains("postscript3"));
        assertTrue(taskResult.getOutput().getAllLogs(false).contains("prescript3"));

        checkFilesWereCreated();
    }

    static SimpleScript createScriptWithOutput(String scriptOutput) throws Exception {
        return new SimpleScript(String.format("print('%s')", scriptOutput), "js");
    }

    static final String CREATE_FILE_SCRIPT_CONTENT = "if (!new java.io.File(args[0]).exists()) { print('Going to create file ' + args[0]); if (!new java.io.File(args[0]).createNewFile()) { throw new java.lang.Exception(); } } ; selected=true;";

    static SimpleScript createFileCreatingScript(File file) throws Exception {
        SimpleScript script = new SimpleScript(CREATE_FILE_SCRIPT_CONTENT, "groovy",
            new String[] { file.getPath() });
        return script;
    }

    static SelectionScript createFileCreatingSelectionScript(File file) throws Exception {
        return new SelectionScript(CREATE_FILE_SCRIPT_CONTENT, "groovy", new String[] { file.getPath() },
            true);
    }

    private void printResultAndCheckNoErrors(JobResult jobResult) throws Throwable {
        for (TaskResult taskResult : jobResult.getAllResults().values()) {
            log("Task result for " + taskResult.getTaskId() + " " + taskResult.getTaskId().getReadableName());
            if (taskResult.getException() != null) {
                taskResult.getException().printStackTrace();
                fail("Task failed with exception " + taskResult.getException());
            }
            log("Task output:");
            log(taskResult.getOutput().getAllLogs(false));
            log("Task result value: " + taskResult.value());
        }
    }

    private TaskFlowJob createPendingJob() throws Exception {
        TaskFlowJob job = new TaskFlowJob();
        job.setName(this.getClass().getSimpleName() + "_pending");
        job.setCancelJobOnError(false);

        JavaTask javaTask = new JavaTask();
        javaTask.setExecutableClassName(TestJavaTask1.class.getName());
        javaTask.setName("Task");
        javaTask.setSelectionScript(new SelectionScript("selected = false;", "JavaScript", false));
        job.addTask(javaTask);

        return job;
    }

}
