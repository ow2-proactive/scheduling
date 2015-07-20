/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package functionaltests;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.objectweb.proactive.api.PAActiveObject;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.job.Job;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.Log4JTaskLogs;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.executable.JavaExecutable;
import org.ow2.proactive.scheduler.common.util.logforwarder.LogForwardingService;
import org.ow2.proactive.scheduler.common.util.logforwarder.providers.SocketBasedForwardingProvider;
import org.apache.commons.collections.ListUtils;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


/**
 * Test against method Scheduler.listenJobLogs.
 * Test calls Scheduler.listenJobLogs several times for the same job and check 
 * that it works properly in different conditions:
 * <ul>
 * <li>can get logs for task which is running
 * <li>can get log for task which finished but job is still running
 * <li>can get log for task after job has finished
 * </ul>
 * 
 * @author ProActive team
 *
 */
public class TestListenJobLogs extends RMFunctionalTest {

    static final long LOG_EVENT_TIMEOUT = 30000;

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
                    getOut().println(command);
                } else {
                    throw new IllegalArgumentException(command);
                }
            }

            return "OK";
        }

    }

    private TaskFlowJob createJob(String communicationObjectUrl1, String communicationObjectUrl2)
            throws Exception {
        TaskFlowJob job = new TaskFlowJob();
        job.setName(this.getClass().getSimpleName());

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
        SchedulerTHelper.startScheduler(new File(SchedulerTHelper.class.getResource(
          "config/scheduler-nonforkedscripttasks.ini").toURI()).getAbsolutePath());

        testLogs();
    }

    public void testLogs() throws Exception {
        CommunicationObject communicationObject1 = PAActiveObject.newActive(CommunicationObject.class,
                new Object[] {});
        String communicationObjectUrl1 = PAActiveObject.getUrl(communicationObject1);

        CommunicationObject communicationObject2 = PAActiveObject.newActive(CommunicationObject.class,
                new Object[] {});
        String communicationObjectUrl2 = PAActiveObject.getUrl(communicationObject2);

        Scheduler scheduler = SchedulerTHelper.getSchedulerInterface();

        Job job = createJob(communicationObjectUrl1, communicationObjectUrl2);
        JobId jobId = scheduler.submit(job);

        SchedulerTHelper.waitForEventTaskRunning(jobId, TASK_NAME1);

        communicationObject1.setCommand("output1");

        // listenJobLogs for running task
        TestAppender appender1 = new TestAppender("appender1");
        String loggerName = Log4JTaskLogs.JOB_LOGGER_PREFIX + jobId;
        logForwardingService.addAppender(loggerName, appender1);
        scheduler.listenJobLogs(jobId, logForwardingService.getAppenderProvider());
        appender1.waitForLoggingEvent(LOG_EVENT_TIMEOUT, "output1");

        /*
         * second listenJobLogs for running task (should get output produced before listenJobLogs
         * was called)
         */
        TestAppender appender2 = new TestAppender("appender2");
        logForwardingService.addAppender(loggerName, appender2);

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
        logForwardingService.removeAllAppenders(loggerName);
        logForwardingService.addAppender(loggerName, appender3);
        scheduler.listenJobLogs(jobId, logForwardingService.getAppenderProvider());
        appender3.waitForLoggingEvent(LOG_EVENT_TIMEOUT, "output1", "output2", "output3");

        // let second task finish
        communicationObject2.setCommand("stop");

        SchedulerTHelper.waitForEventJobFinished(jobId);

        // add appender after job had finished, appender should receive output of all tasks
        TestAppender appender4 = new TestAppender("appender4");
        logForwardingService.removeAllAppenders(loggerName);
        logForwardingService.addAppender(loggerName, appender4);
        scheduler.listenJobLogs(jobId, logForwardingService.getAppenderProvider());
        appender4.waitForLoggingEvent(LOG_EVENT_TIMEOUT, "output1", "output2", "output3");

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
            this.actualMessages = new ArrayList<>();
        }

        synchronized void waitForLoggingEvent(long timeout, String... expectedMessages)
                throws InterruptedException {
            List<String> expectedMessagesList = new ArrayList<>();
            for (String message : expectedMessages) {
                expectedMessagesList.add(message);
            }
            System.out.println("Waiting for logging events with messages: " + expectedMessagesList + " (" +
                name + ")");

            long endTime = System.currentTimeMillis() + timeout;
            while (!ListUtils.removeAll(expectedMessagesList, actualMessages).isEmpty()) {
                long waitTime = endTime - System.currentTimeMillis();
                if (waitTime > 0) {
                    wait(100);
                } else {
                    break;
                }
            }

            Assert.assertTrue("Didn't receive expected events, expected: " + expectedMessagesList +
                ", actual: " + actualMessages, ListUtils.removeAll(expectedMessagesList, actualMessages)
                    .isEmpty());
            actualMessages.clear();
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
