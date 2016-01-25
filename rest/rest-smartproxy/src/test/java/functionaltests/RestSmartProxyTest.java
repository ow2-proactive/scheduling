/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2015 INRIA/University of
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

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import org.ow2.proactive.scheduler.common.NotificationData;
import org.ow2.proactive.scheduler.common.SchedulerEvent;
import org.ow2.proactive.scheduler.common.job.*;
import org.ow2.proactive.scheduler.common.job.factories.Job2XMLTransformer;
import org.ow2.proactive.scheduler.common.task.ForkEnvironment;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.TaskInfo;
import org.ow2.proactive.scheduler.common.task.dataspaces.InputAccessMode;
import org.ow2.proactive.scheduler.common.task.dataspaces.OutputAccessMode;
import org.ow2.proactive.scheduler.smartproxy.common.SchedulerEventListenerExtended;
import org.ow2.proactive_grid_cloud_portal.smartproxy.RestSmartProxyImpl;

import java.io.File;
import java.io.FileWriter;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import static functionaltests.RestFuncTHelper.getRestServerUrl;

public final class RestSmartProxyTest extends AbstractRestFuncTestCase {

    private static final long ONE_SECOND = TimeUnit.SECONDS.toMillis(1);

    private static final long TEN_MINUTES = 36000; // in milliseconds

    protected static int NB_TASKS = 4;

    protected File inputLocalFolder;
    protected File outputLocalFolder;

    protected String userspace;

    protected String pushUrl;
    protected String pullUrl;

    protected static final String TASK_NAME = "TestJavaTask";

    // we add special characters to ensure they are supported
    public final static String INPUT_FILE_BASE_NAME = "input é";
    public final static String INPUT_FILE_EXT = ".txt";
    public final static String OUTPUT_FILE_BASE_NAME = "output é";
    public final static String OUTPUT_FILE_EXT = ".out";

    protected RestSmartProxyImpl restSmartProxy;

    @Rule
    public TemporaryFolder tempDir = new TemporaryFolder();

    @BeforeClass
    public static void beforeClass() throws Exception {
        init();
    }

    @Before
    public void setup() throws Exception {
        initializeRestSmartProxyInstance();
    }

    @After
    public void teardown() throws Exception {
        if (restSmartProxy != null) {
            restSmartProxy.terminate();
        }
    }

    private void initializeRestSmartProxyInstance() throws Exception {
        restSmartProxy = new RestSmartProxyImpl();
        restSmartProxy.cleanDatabase();
        restSmartProxy.setSessionName(uniqueSessionId());
        restSmartProxy.init(getRestServerUrl(), getLogin(), getPassword());

        userspace = restSmartProxy.getUserSpaceURIs().get(0);
        pushUrl = userspace;
        pullUrl = userspace;

        // we add special characters and space to the folders to make sure transfer occurs normally
        inputLocalFolder = tempDir.newFolder("input é");
        outputLocalFolder = tempDir.newFolder("output é");
    }

    @Test(timeout = TEN_MINUTES)
    public void test_no_automatic_transfer() throws Exception {
        testJobSubmission(false, false);
    }

    @Test(timeout = TEN_MINUTES)
    public void test_automatic_transfer() throws Exception {
        testJobSubmission(false, true);
    }

    @Test
    public void testReconnection() throws Exception {

        restSmartProxy.reconnect();
        Assert.assertTrue(restSmartProxy.isConnected());

        // try a random method and verify that no exception is thrown
        restSmartProxy.getStatus();

        restSmartProxy.disconnect();
        Assert.assertFalse(restSmartProxy.isConnected());

    }

    private void testJobSubmission(boolean isolateTaskOutput, boolean automaticTransfer) throws Exception {
        TaskFlowJob job = createTestJob(isolateTaskOutput);

        // debugging the job produced
        String jobXml = new Job2XMLTransformer().jobToxmlString(job);
        System.out.println(jobXml);

        DataTransferNotifier notifier = new DataTransferNotifier();

        if (automaticTransfer) {
            restSmartProxy.addEventListener(notifier);
        }

        JobId id =
                restSmartProxy.submit(
                        job, inputLocalFolder.getAbsolutePath(),
                        pushUrl, outputLocalFolder.getAbsolutePath(), pullUrl,
                        isolateTaskOutput, automaticTransfer);

        JobState jobState = restSmartProxy.getJobState(id.toString());

        Thread.sleep(ONE_SECOND);
        while (!jobState.isFinished()) {
            jobState = restSmartProxy.getJobState(id.toString());
            Thread.sleep(ONE_SECOND);
        }

        assertEquals(JobStatus.FINISHED, jobState.getStatus());

        if (!automaticTransfer) {
            for (int i = 0; i < NB_TASKS; i++) {
                restSmartProxy.pullData(id.toString(), TASK_NAME + i, outputLocalFolder.getAbsolutePath());
            }
        } else {
            List<String> taskNames = taskNameList();
            while (!taskNames.isEmpty()) {
                String finishedTask = notifier.finishedTask();
                if (taskNames.contains(finishedTask)) {
                    taskNames.remove(finishedTask);
                }
            }
        }

        // check the presence of output files
        for (int i = 0; i < NB_TASKS; i++) {
            String outputFileName = OUTPUT_FILE_BASE_NAME + "_" + i + OUTPUT_FILE_EXT;
            File outputFile = new File(outputLocalFolder, outputFileName);
            Assert.assertTrue(
                    String.format("%s does not exist.", outputFile.getAbsolutePath()),
                    outputFile.exists());
        }
    }

    private TaskFlowJob createTestJob(boolean isolateOutputs) throws Exception {
        TaskFlowJob job = new TaskFlowJob();
        // add a special character to the job name to ensure the job is parsed correctly by the server
        job.setName(this.getClass().getSimpleName() + " é");

        for (int i = 0; i < NB_TASKS; i++) {
            JavaTask testTask = new JavaTask();
            testTask.setName(TASK_NAME + i);
            testTask.setExecutableClassName(SimpleJavaExecutable.class.getName());
            testTask.setForkEnvironment(new ForkEnvironment());
            File inputFile = new File(inputLocalFolder, INPUT_FILE_BASE_NAME + "_" + i + INPUT_FILE_EXT);
            String outputFileName = OUTPUT_FILE_BASE_NAME + "_" + i + OUTPUT_FILE_EXT;

            // delete files after the test is finished
            File outputFile = new File(outputLocalFolder, outputFileName);
            outputFile.deleteOnExit();

            inputFile.deleteOnExit();

            FileWriter fileWriter = new FileWriter(inputFile);
            for (int j = 0; j <= Math.round(Math.random() * 100) + 1; j++) {
                fileWriter.write("Some random input");
            }
            fileWriter.close();

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

        return job;
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

    private static final class DataTransferNotifier implements SchedulerEventListenerExtended {

        private final BlockingQueue<String> finishedTask = new ArrayBlockingQueue<>(NB_TASKS);

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