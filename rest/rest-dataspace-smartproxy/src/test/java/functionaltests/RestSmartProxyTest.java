/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2014 INRIA/University of
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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package functionaltests;

import static functionaltests.RestFuncTHelper.getRestServerUrl;

import java.io.File;
import java.io.FileWriter;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.ow2.proactive.scheduler.common.NotificationData;
import org.ow2.proactive.scheduler.common.SchedulerEvent;
import org.ow2.proactive.scheduler.common.job.Job;
import org.ow2.proactive.scheduler.common.job.JobEnvironment;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.JobStatus;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.job.UserIdentification;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.TaskInfo;
import org.ow2.proactive.scheduler.common.task.UpdatableProperties;
import org.ow2.proactive.scheduler.common.task.dataspaces.InputAccessMode;
import org.ow2.proactive.scheduler.common.task.dataspaces.OutputAccessMode;
import org.ow2.proactive.scheduler.common.util.dsclient.ISchedulerEventListenerExtended;
import org.ow2.proactive_grid_cloud_portal.ds.client.RestSmartProxyImpl;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;


public class RestSmartProxyTest extends AbstractRestFuncTestCase {

    private static final long ONE_SECOND_IN_MILLIS = TimeUnit.SECONDS.toMillis(1);

    private static final long TEN_MINUTES_IN_MILLS = 10 * 60 * 1000;

    public static int NB_TASKS = 4;

    protected File inputLocalFolder;
    protected File outputLocalFolder;

    protected String userspace;

    protected String pushUrl;
    protected String pullUrl;

    protected static final String TEST_SESSION_NAME = "TestDSSupport";

    protected static final String TASK_NAME = "TestJavaTask";

    public final static String inputFileBaseName = "input";
    public final static String inputFileExt = ".txt";
    public final static String outputFileBaseName = "output";
    public final static String outputFileExt = ".out";

    protected RestSmartProxyImpl schedProxy;

    @Rule
    public TemporaryFolder tempDir = new TemporaryFolder();

    @BeforeClass
    public static void beforeClass() throws Exception {
        init(RestSmartProxyTest.class.getSimpleName());
    }

    @Before
    public void setup() throws Exception {
        initializeRestSmartProxyInstance();
    }

    @After
    public void teardown() throws Exception {
        if (schedProxy != null) {
            schedProxy.terminate();
        }
    }

    private void initializeRestSmartProxyInstance() throws Exception {
        schedProxy = new RestSmartProxyImpl();
        schedProxy.setSessionName(uniqueSessionId());
        schedProxy.cleanDatabase();
        schedProxy.init(getRestServerUrl(), getLogin(), getPassword());

        userspace = schedProxy.getUserSpaceURIs().get(0);
        pushUrl = userspace;
        pullUrl = userspace;

        inputLocalFolder = tempDir.newFolder("input");
        outputLocalFolder = tempDir.newFolder("output");
    }

    @Test(timeout = TEN_MINUTES_IN_MILLS)
    public void test_no_automatic_transfer() throws Exception {
        testJobSubmission(false, false);
    }

    @Test(timeout = TEN_MINUTES_IN_MILLS)
    public void test_automatic_transfer() throws Exception {
        testJobSubmission(false, true);
    }

    private void testJobSubmission(boolean isolateTaskOutput, boolean automaticTransfer) throws Exception {
        TaskFlowJob job = createTestJob(isolateTaskOutput);

        DataTransferNotifier notifier = new DataTransferNotifier();
        if (automaticTransfer) {
            System.out.println("Register the DataTransferNotifer instance.");
            schedProxy.addEventListener(notifier);
        }

        JobId id = schedProxy.submit(job, inputLocalFolder.getAbsolutePath(), pushUrl, outputLocalFolder
                .getAbsolutePath(), pullUrl, isolateTaskOutput, automaticTransfer);

        Thread.sleep(ONE_SECOND_IN_MILLIS);

        schedProxy.disconnect();
        schedProxy.reconnect();

        JobState jobState = schedProxy.getJobState(id.toString());
        while (!jobState.isFinished()) {
            Thread.sleep(ONE_SECOND_IN_MILLIS);
            jobState = schedProxy.getJobState(id.toString());
        }

        assertEquals(JobStatus.FINISHED, jobState.getStatus());

        if (!automaticTransfer) {
            for (int i = 0; i < NB_TASKS; i++) {
                System.out.println("storing data:" + id.toString() + "_" + TASK_NAME + i + " --> " +
                    outputLocalFolder.getAbsolutePath());
                schedProxy.pullData(id.toString(), TASK_NAME + i, outputLocalFolder.getAbsolutePath());
            }
        } else {
            List<String> taskNames = taskNameList();
            System.out.println("Automated data transfer task list: " + taskNames);
            while (!taskNames.isEmpty()) {
                String finishedTask = notifier.finishedTask();
                System.out.println("Data transfer has finished for " + finishedTask);
                if (taskNames.contains(finishedTask)) {
                    taskNames.remove(finishedTask);
                }
            }
            System.out.println("Data transfer has finised for all tasks: " + taskNames);
        }

        // check the presence of output files
        for (int i = 0; i < NB_TASKS; i++) {
            String outputFileName = outputFileBaseName + "_" + i + outputFileExt;
            File outputFile = new File(outputLocalFolder, outputFileName);
            Assert.assertTrue(String.format("%s does not exisit.", outputFile.getAbsolutePath()), outputFile
                    .exists());
        }
    }

    private TaskFlowJob createTestJob(boolean isolateOutputs) throws Exception {
        TaskFlowJob job = new TaskFlowJob();
        job.setName(this.getClass().getSimpleName());
        for (int i = 0; i < NB_TASKS; i++) {
            JavaTask testTask = new JavaTask();
            testTask.setName(TASK_NAME + i);
            testTask.setExecutableClassName(SimpleJavaExecutable.class.getName());
            File inputFile = new File(inputLocalFolder, inputFileBaseName + "_" + i + inputFileExt);
            String outputFileName = outputFileBaseName + "_" + i + outputFileExt;

            // delete files after the test is finished
            File outputFile = new File(outputLocalFolder, outputFileName);
            outputFile.deleteOnExit();

            inputFile.deleteOnExit();

            FileWriter fw = new FileWriter(inputFile);
            for (int j = 0; j <= Math.round(Math.random() * 100) + 1; j++)
                fw.write("Some random input");
            fw.close();
            // Add dummy input files, make sure no error happen
            testTask.addInputFiles("DUMMY", InputAccessMode.TransferFromInputSpace);
            testTask.addInputFiles(inputFile.getName(), InputAccessMode.TransferFromInputSpace);
            if (isolateOutputs) {
                testTask.addOutputFiles("*.out", OutputAccessMode.TransferToOutputSpace);
            } else {
                testTask.addOutputFiles(outputFileName, OutputAccessMode.TransferToOutputSpace);
            }
            job.addTask(testTask);
        }

        job.setInputSpace(userspace);
        job.setOutputSpace(userspace);

        setJobClassPath(job);
        return job;
    }

    private void setJobClassPath(Job job) throws Exception {
        File appMainFolder = new File(this.getClass().getProtectionDomain().getCodeSource().getLocation()
                .toURI());
        String appClassPath = appMainFolder.getAbsolutePath();
        JobEnvironment je = new JobEnvironment();
        je.setJobClasspath(new String[] { appClassPath });
        job.setEnvironment(je);
    }

    private String uniqueSessionId() {
        return String.format("TEST_SID_%s", Long.toHexString(System.currentTimeMillis()));
    }

    private List<String> taskNameList() {
        List<String> taskNames = Lists.newArrayList();
        for (int i = 0; i < NB_TASKS; i++) {
            taskNames.add(TASK_NAME + i);
        }
        return taskNames;
    }

    private static class DataTransferNotifier implements ISchedulerEventListenerExtended {

        private final BlockingQueue<String> finishedTask = new ArrayBlockingQueue<String>(NB_TASKS);

        @Override
        public void pullDataFailed(String jobId, String taskName, String localFolderPath, Throwable error) {
            try {
                finishedTask.put(taskName);
            } catch (InterruptedException e) {
                e.printStackTrace(System.err);
            }
        }

        @Override
        public void pullDataFinished(String jobId, String taskName, String localFolderPath) {
            try {
                finishedTask.put(taskName);
            } catch (InterruptedException e) {
                e.printStackTrace(System.err);
            }
        }

        public String finishedTask() {
            try {
                return finishedTask.take();
            } catch (InterruptedException e) {
                throw Throwables.propagate(e);
            }
        }

        @Override
        public void jobStateUpdatedEvent(NotificationData<JobInfo> arg0) {
        }

        @Override
        public void jobSubmittedEvent(JobState arg0) {
        }

        @Override
        public void schedulerStateUpdatedEvent(SchedulerEvent arg0) {
        }

        @Override
        public void taskStateUpdatedEvent(NotificationData<TaskInfo> arg0) {
        }

        @Override
        public void usersUpdatedEvent(NotificationData<UserIdentification> arg0) {
        }
    }
}
