/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package functionaltests;

import static org.junit.Assume.assumeTrue;

import java.io.File;
import java.io.FileWriter;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import org.apache.log4j.Level;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.ProActiveTimeoutException;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.utils.OperatingSystem;
import org.objectweb.proactive.utils.TimeoutAccounter;
import org.ow2.proactive.authentication.ConnectionInfo;
import org.ow2.proactive.scheduler.common.job.Job;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.ForkEnvironment;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.dataspaces.InputAccessMode;
import org.ow2.proactive.scheduler.common.task.dataspaces.OutputAccessMode;
import org.ow2.proactive.scheduler.common.util.SchedulerProxyUserInterface;
import org.ow2.proactive.scheduler.smartproxy.SmartProxyImpl;

import functionaltests.monitor.EventMonitor;
import functionaltests.utils.SchedulerFunctionalTestNoRestart;
import functionaltests.utils.SchedulerTHelper;
import functionaltests.utils.TestUsers;


/**
 * @author esalagea
 */
public class TestSmartProxy extends SchedulerFunctionalTestNoRestart {

    /**
     * Local folder on client side where the input data is located and where the
     * output data is to be downloaded
     */
    public static final String workFolderPath = System.getProperty("java.io.tmpdir") + File.separator +
                                                "testDS_LocalFolder";

    /**
     * Intermediary folder accessible (via file transfer protocol supported by
     * VFS) both from client side and from computing node side
     */
    public static final String dataServerFolderPath = System.getProperty("java.io.tmpdir") + File.separator +
                                                      "testDS_remoteFolder";

    public static long TIMEOUT = 120000;

    public static int NB_TASKS = 4;

    protected File inputLocalFolder;

    protected File outputLocalFolder;

    private File workLocalFolder;

    // private DataServerProvider dataProvider;
    protected String dataServerURI;

    protected String push_url;

    protected String pull_url;

    protected static final String TEST_SESSION_NAME = "TestDSSupport";

    protected static final String TASK_NAME = "TestJavaTask";

    public final static String inputFileBaseName = "input";

    public final static String inputFileExt = ".in";

    public final static String outputFileBaseName = "output";

    public final static String outputFileExt = ".out";

    // the proxy to be tested
    protected SmartProxyImpl schedProxy;

    protected MyEventListener eventListener;

    public TestSmartProxy() throws MalformedURLException, URISyntaxException {
        push_url = (new File(dataServerFolderPath)).toURI().toURL().toExternalForm();
        pull_url = (new File(dataServerFolderPath)).toURI().toURL().toExternalForm();
    }

    @Before
    public void init() throws Exception {

        // because of https://issues.jenkins-ci.org/browse/JENKINS-29285, test is unstable on windows
        assumeTrue(OperatingSystem.getOperatingSystem() != OperatingSystem.windows);

        // log all data transfer related events
        ProActiveLogger.getLogger(SchedulerProxyUserInterface.class).setLevel(Level.DEBUG);

        workLocalFolder = new File(workFolderPath);
        inputLocalFolder = new File(workLocalFolder, "input");
        outputLocalFolder = new File(workLocalFolder, "output");

        inputLocalFolder.mkdirs();
        outputLocalFolder.mkdirs();

        // ----------------- start Data Server -------------
        // this simulates a remote data server
        // dataServerURI =
        // dataProvider.deployProActiveDataServer(dataServerFolderPath, "data");
        dataServerURI = (new File(dataServerFolderPath)).toURI().toURL().toExternalForm();

        // start scheduler and nodes
        schedulerHelper.getSchedulerAuth();

        schedProxy = SmartProxyImpl.getActiveInstance();

        schedProxy.cleanDatabase();

        String schedulerUrl = SchedulerTHelper.getLocalUrl();
        schedProxy.setSessionName(TEST_SESSION_NAME);

        schedProxy.init(new ConnectionInfo(schedulerUrl, TestUsers.DEMO.username, TestUsers.DEMO.password, null, true));

        eventListener = new MyEventListener();
        MyEventListener myListenerRemoteReference = PAActiveObject.turnActive(eventListener);
        schedProxy.addEventListener(myListenerRemoteReference);

    }

    protected TaskFlowJob createTestJob(boolean isolateOutputs) throws Exception {
        TaskFlowJob job = new TaskFlowJob();
        job.setName(this.getClass().getSimpleName());

        ForkEnvironment forkEnvironment = new ForkEnvironment();
        forkEnvironment.addAdditionalClasspath(getClasspath(job));

        for (int i = 0; i < NB_TASKS; i++) {
            JavaTask testTask = new JavaTask();
            testTask.setName(TASK_NAME + i);
            testTask.setExecutableClassName(SimpleJavaExecutable.class.getName());
            testTask.setForkEnvironment(forkEnvironment);
            // testTask.
            // ------------- create an input File ------------
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
                testTask.addOutputFiles("*" + outputFileExt, OutputAccessMode.TransferToOutputSpace);
            } else {
                testTask.addOutputFiles(outputFileName, OutputAccessMode.TransferToOutputSpace);
            }
            job.addTask(testTask);
        }
        job.setInputSpace(dataServerURI);
        job.setOutputSpace(dataServerURI);

        return job;
    }

    /**
     * This method adds to the job the classpath of the application
     *
     * @param job
     */
    protected String getClasspath(Job job) {
        String appClassPath = "";
        try {
            File appMainFolder = new File(this.getClass().getProtectionDomain().getCodeSource().getLocation().toURI());

            return appMainFolder.getAbsolutePath();
        } catch (URISyntaxException e1) {
            functionaltests.utils.SchedulerTHelper.log("Preview of the partial results will not be possible as some resources could not be " +
                                                       "found by the system. \nThis will not alterate your results in any way. ");
            functionaltests.utils.SchedulerTHelper.log("JobCreator: The bin folder of the project is null. It is needed to set the job environment. ");
            functionaltests.utils.SchedulerTHelper.log(e1);
        }
        return appClassPath;
    }

    protected void waitWithMonitor(EventMonitor monitor, long timeout) throws ProActiveTimeoutException {
        TimeoutAccounter counter = TimeoutAccounter.getAccounter(timeout);
        synchronized (monitor) {
            monitor.setTimeouted(false);
            while (!counter.isTimeoutElapsed()) {
                if (monitor.eventOccured())
                    return;
                try {
                    functionaltests.utils.SchedulerTHelper.log("waiting for event monitor " + monitor);
                    monitor.wait(counter.getRemainingTimeout());
                } catch (InterruptedException e) {
                    // spurious wake-up, nothing to do
                    e.printStackTrace();
                }
            }
            if (monitor.eventOccured())
                return;
            monitor.setTimeouted(true);
        }
        throw new ProActiveTimeoutException("timeout elapsed");
    }

    @Test
    public void testSmartProxy() throws Throwable {
        if (true) {
            return;
        }

        functionaltests.utils.SchedulerTHelper.log("***************************************************************************************************");
        functionaltests.utils.SchedulerTHelper.log("********************** Testing isolateTaskOutputs = false automaticTransfer = false " +
                                                   "***************");
        functionaltests.utils.SchedulerTHelper.log("***************************************************************************************************");
        submitJobWithDataAndWaitToFinish(inputLocalFolder.getAbsolutePath(),
                                         outputLocalFolder.getAbsolutePath(),
                                         false,
                                         false);
        functionaltests.utils.SchedulerTHelper.log("***************************************************************************************************");
        functionaltests.utils.SchedulerTHelper.log("********************** Testing isolateTaskOutputs = true automaticTransfer = false ****************");
        functionaltests.utils.SchedulerTHelper.log("***************************************************************************************************");
        submitJobWithDataAndWaitToFinish(inputLocalFolder.getAbsolutePath(),
                                         outputLocalFolder.getAbsolutePath(),
                                         true,
                                         false);
        functionaltests.utils.SchedulerTHelper.log("***************************************************************************************************");
        functionaltests.utils.SchedulerTHelper.log("********************** Testing isolateTaskOutputs = false automaticTransfer = true ****************");
        functionaltests.utils.SchedulerTHelper.log("***************************************************************************************************");
        submitJobWithDataAndWaitToFinish(inputLocalFolder.getAbsolutePath(),
                                         outputLocalFolder.getAbsolutePath(),
                                         false,
                                         true);
        functionaltests.utils.SchedulerTHelper.log("***************************************************************************************************");
        functionaltests.utils.SchedulerTHelper.log("********************** Testing isolateTaskOutputs = true automaticTransfer = true *****************");
        functionaltests.utils.SchedulerTHelper.log("***************************************************************************************************");
        submitJobWithDataAndWaitToFinish(inputLocalFolder.getAbsolutePath(),
                                         outputLocalFolder.getAbsolutePath(),
                                         true,
                                         true);

    }

    protected void submitJobWithDataAndWaitToFinish(String localInputFolderPath, String localOutputFolderPath,
            boolean isolateTaskOutputs, boolean automaticTransfer) throws Exception {

        TaskFlowJob job = createTestJob(isolateTaskOutputs);
        EventMonitor em = new EventMonitor(null);
        // clean old data
        for (int i = 0; i < NB_TASKS; i++) {

            String outputFileName = outputFileBaseName + "_" + i + outputFileExt;

            File outputFile = new File(outputLocalFolder, outputFileName);

            if (outputFile.exists()) {
                outputFile.delete();
            }
        }

        eventListener.reset();
        eventListener.setSynchronous(!automaticTransfer);
        eventListener.setMonitor(em);

        JobId id = schedProxy.submit(job,
                                     localInputFolderPath,
                                     push_url,
                                     localOutputFolderPath,
                                     pull_url,
                                     isolateTaskOutputs,
                                     automaticTransfer);

        eventListener.setJobID(id);

        Thread.sleep(1000);
        schedProxy.disconnect();
        schedProxy.reconnect();
        waitWithMonitor(em, TIMEOUT);
        if (!automaticTransfer) {
            for (int i = 0; i < NB_TASKS; i++) {
                try {
                    schedProxy.pullData(id.toString(), TASK_NAME + i, localOutputFolderPath);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        // check the presence of output files
        for (int i = 0; i < NB_TASKS; i++) {
            functionaltests.utils.SchedulerTHelper.log(schedProxy.getTaskResult(id.toString(), TASK_NAME + i)
                                                                 .getOutput()
                                                                 .getAllLogs(true));
            String outputFileName = outputFileBaseName + "_" + i + outputFileExt;
            File outputFile = new File(outputLocalFolder, outputFileName);
            SchedulerTHelper.log("Checking file exists : " + outputFile);
            Assert.assertTrue(outputFile + " exists", outputFile.isFile());
        }
    }

}
