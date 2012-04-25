package functionaltests;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.proactive.api.PAActiveObject;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.job.Job;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.ForkEnvironment;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.Log4JTaskLogs;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.executable.JavaExecutable;
import org.ow2.proactive.scheduler.common.util.logforwarder.LogForwardingService;
import org.ow2.proactive.scheduler.common.util.logforwarder.providers.SocketBasedForwardingProvider;
import org.ow2.tests.FunctionalTest;

import edu.emory.mathcs.backport.java.util.Arrays;


/**
 * - get logs for task which is running
 * - get log for task which finished but job is still running
 * - get log for task after job has finished
 * 
 * @author ProActive team
 *
 */
public class TestListenJobLogs extends FunctionalTest {

    static final long LOG_EVENT_TIMEOUT = 10000;

    static final String TASK_NAME1 = "TestTask1";

    static final String TASK_NAME2 = "TestTask2";

    public static class CommunicationObject {

        private String command;

        public String getCommand() {
            String result = command;
            command = null;
            return result;
        }

        public void setCommand(String command) {
            this.command = command;
        }

    }

    public static class TestJavaTask extends JavaExecutable {

        private String communicationObjectUrl;

        @Override
        public Serializable execute(TaskResult... results) throws Throwable {
            CommunicationObject communicationObject = PAActiveObject.lookupActive(CommunicationObject.class,
                    communicationObjectUrl);

            while (true) {
                String command = communicationObject.getCommand();
                if (command == null) {
                    Thread.sleep(1000);
                    continue;
                }
                if (command.equals("stop")) {
                    break;
                } else if (command.startsWith("output")) {
                    System.out.println(command);
                } else {
                    throw new IllegalArgumentException(command);
                }
            }

            return "OK";
        }

    }

    private TaskFlowJob createJob(String communicationObjectUrl1, String communicationObjectUrl2, boolean fork)
            throws Exception {
        TaskFlowJob job = new TaskFlowJob();
        job.setName("Test job");

        JavaTask javaTask1 = new JavaTask();
        javaTask1.setExecutableClassName(TestJavaTask.class.getName());
        javaTask1.addArgument("communicationObjectUrl", communicationObjectUrl1);
        javaTask1.setName(TASK_NAME1);

        JavaTask javaTask2 = new JavaTask();
        javaTask2.setExecutableClassName(TestJavaTask.class.getName());
        javaTask2.addArgument("communicationObjectUrl", communicationObjectUrl2);
        javaTask2.setName(TASK_NAME2);

        // task 2 starts after task1
        javaTask2.addDependence(javaTask1);

        if (fork) {
            ForkEnvironment env = new ForkEnvironment();
            javaTask1.setForkEnvironment(env);
            javaTask2.setForkEnvironment(env);
        }

        job.addTask(javaTask1);
        job.addTask(javaTask2);

        return job;
    }

    private LogForwardingService logForwardingService;

    @Before
    public void init() throws Exception {
        logForwardingService = new LogForwardingService(SocketBasedForwardingProvider.class.getName());
        logForwardingService.initialize();
    }

    @After
    public void cleanup() throws Exception {
        if (logForwardingService != null) {
            logForwardingService.terminate();
        }
    }

    @Test
    public void test() throws Exception {
        testLogs(false);

        testLogs(true);
    }

    public void testLogs(boolean forkJavaTask) throws Exception {
        CommunicationObject communicationObject1 = PAActiveObject.newActive(CommunicationObject.class,
                new Object[] {});
        String communicationObjectUrl1 = PAActiveObject.getUrl(communicationObject1);

        CommunicationObject communicationObject2 = PAActiveObject.newActive(CommunicationObject.class,
                new Object[] {});
        String communicationObjectUrl2 = PAActiveObject.getUrl(communicationObject2);

        Scheduler scheduler = SchedulerTHelper.getSchedulerInterface();

        Job job = createJob(communicationObjectUrl1, communicationObjectUrl2, forkJavaTask);
        JobId jobId = scheduler.submit(job);

        Logger logger = Logger.getLogger(Log4JTaskLogs.JOB_LOGGER_PREFIX + jobId);
        logger.removeAllAppenders();
        logger.setAdditivity(false);

        SchedulerTHelper.waitForEventTaskRunning(jobId, TASK_NAME1);

        communicationObject1.setCommand("output1");

        // listenJobLogs for running task
        TestAppender appender1 = new TestAppender("appender1");
        logger.addAppender(appender1);
        scheduler.listenJobLogs(jobId, logForwardingService.getAppenderProvider());
        appender1.waitForLoggingEvent(LOG_EVENT_TIMEOUT, "output1");

        /*
         * second listenJobLogs for running task (should get output produced before listenJobLogs
         * was called)
         */
        TestAppender appender2 = new TestAppender("appender2");
        logger.removeAppender(appender1);
        logger.addAppender(appender2);

        scheduler.listenJobLogs(jobId, logForwardingService.getAppenderProvider());
        appender2.waitForLoggingEvent(LOG_EVENT_TIMEOUT, "output1");

        // task produces more output, check it is received
        communicationObject1.setCommand("output2");

        /*
         * TODO: at the time of this writing there is no way to remove first log appender, 
         * so this JVM receives two identical log events for running job
         */
        appender2.waitForLoggingEvent(LOG_EVENT_TIMEOUT, "output2", "output2");
        // let first task finish
        communicationObject1.setCommand("stop");

        SchedulerTHelper.waitForEventTaskFinished(jobId, TASK_NAME1);

        SchedulerTHelper.waitForEventTaskRunning(jobId, TASK_NAME2);

        // now first task had finished, second tasks is still running

        communicationObject2.setCommand("output3");
        /*
         * TODO: at the time of this writing there is no way to remove first log appender, 
         * so this JVM receives two identical log events for running job  
         */
        appender2.waitForLoggingEvent(LOG_EVENT_TIMEOUT, "output3", "output3");

        // add appender after first task had finished, appender should receive its output
        TestAppender appender3 = new TestAppender("appender3");
        logger.removeAllAppenders();
        logger.addAppender(appender3);
        scheduler.listenJobLogs(jobId, logForwardingService.getAppenderProvider());
        appender3.waitForLoggingEvent(LOG_EVENT_TIMEOUT, "output1", "output2", "output3");

        // let second task finish
        communicationObject2.setCommand("stop");

        SchedulerTHelper.waitForEventJobFinished(jobId);

        // add appender after job had finished, appender should receive output of all tasks
        TestAppender appender4 = new TestAppender("appender4");
        logger.removeAllAppenders();
        logger.addAppender(appender4);
        scheduler.listenJobLogs(jobId, logForwardingService.getAppenderProvider());

        /*
         *  TODO: at the time of this writing output of different tasks is received in random order, 
         *  so can't check lists for equals, otherwise could replace following logic with this line:
         *  
         *  appender4.waitForLoggingEvent(LOG_EVENT_TIMEOUT, "output1", "output2", "output3");
         */
        Thread.sleep(LOG_EVENT_TIMEOUT);
        List<String> possibleOutput1 = Arrays.asList(new String[] { "output1", "output2", "output3" });
        List<String> possibleOutput2 = Arrays.asList(new String[] { "output3", "output1", "output2" });
        List<String> output = appender4.getActualMessages();
        Assert.assertTrue("Unexpected output: " + output + " expected is " + possibleOutput1 + " or " +
            possibleOutput2, output.equals(possibleOutput1) || output.equals(possibleOutput2));

        System.out.println("Check job result");

        JobResult jobResult = scheduler.getJobResult(jobId);
        for (TaskResult taskResult : jobResult.getAllResults().values()) {
            if (taskResult.getException() != null) {
                taskResult.getException().printStackTrace();
                Assert.fail("Task failed with exception " + taskResult.getException());
            }
            System.out.println("Task output:");
            System.out.println(taskResult.getOutput().getAllLogs(false));
        }
    }

    public class TestAppender extends AppenderSkeleton {

        private final String name;

        private final List<String> actualMessages;

        TestAppender(String name) {
            this.name = name;
            this.actualMessages = new ArrayList<String>();
        }

        synchronized void waitForLoggingEvent(long timeout, String... expectedMessages)
                throws InterruptedException {
            List<String> expectedMessagesList = new ArrayList<String>();
            for (String message : expectedMessages) {
                expectedMessagesList.add(message);
            }
            System.out.println("Waiting for logging event (" + name + ")");

            long endTime = System.currentTimeMillis() + timeout;
            while (!expectedMessagesList.equals(actualMessages)) {
                long waitTime = endTime - System.currentTimeMillis();
                if (waitTime > 0) {
                    wait(waitTime);
                } else {
                    break;
                }
            }
            Assert.assertEquals("Didn't receive expected events", expectedMessagesList, actualMessages);
            actualMessages.clear();
        }

        List<String> getActualMessages() {
            return actualMessages;
        }

        @Override
        protected synchronized void append(LoggingEvent event) {
            String message = event.getMessage().toString();
            System.out.println("Test appender " + name + " received event: " + message);
            actualMessages.add(message);
        }

        @Override
        public void close() {
            super.closed = true;
        }

        @Override
        public boolean requiresLayout() {
            return false;
        }

    }
}
