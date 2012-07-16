package functionaltests;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;

import javax.security.auth.login.LoginException;

import org.apache.commons.vfs.FileSystemException;
import org.apache.log4j.Level;
import org.junit.Assert;
import org.junit.Before;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.ProActiveTimeoutException;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.utils.TimeoutAccounter;
import org.ow2.proactive.authentication.Connection;
import org.ow2.proactive.scheduler.common.exception.SchedulerException;
import org.ow2.proactive.scheduler.common.exception.UserException;
import org.ow2.proactive.scheduler.common.job.Job;
import org.ow2.proactive.scheduler.common.job.JobEnvironment;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.dataspaces.InputAccessMode;
import org.ow2.proactive.scheduler.common.task.dataspaces.OutputAccessMode;
import org.ow2.proactive.scheduler.common.util.SchedulerProxyUserInterface;
import org.ow2.proactive.scheduler.common.util.dsclient.SchedulerProxyUIWithDSupport;
import org.ow2.tests.FunctionalTest;

import functionaltests.monitor.EventMonitor;


/**
 * @author esalagea
 * 
 */
public class TestSchedulerProxyUIWithDSSupport extends FunctionalTest {

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

    public static long TIMEOUT = 40000;

    private File inputLocalFolder;
    private File outputLocalFolder;
    private File workLocalFolder;
    // private DataServerProvider dataProvider;
    private String dataServerURI;

    private String push_url = "file://" + dataServerFolderPath;
    private String pull_url = "file://" + dataServerFolderPath;

    private final String inputFileName = "input.txt";
    private File inputFile;

    // the proxy to be tested
    SchedulerProxyUIWithDSupport schedProxy;
    MyEventListener eventListener;

    @Before
    public void init() throws Exception {

        // log all data transfer related events
        ProActiveLogger.getLogger(SchedulerProxyUserInterface.class).setLevel(Level.DEBUG);

        workLocalFolder = new File(workFolderPath);
        inputLocalFolder = new File(workLocalFolder, "input");
        outputLocalFolder = new File(workLocalFolder, "output");

        inputLocalFolder.mkdirs();
        outputLocalFolder.mkdirs();

        // ------------- create an input File ------------
        inputFile = new File(inputLocalFolder, inputFileName);

        FileWriter fw = new FileWriter(inputFile);
        for (int i = 0; i <= 100; i++)
            fw.write("Some random input");
        fw.close();

        // ----------------- start Data Server -------------
        // this simulates a remote data server
        // dataServerURI =
        // dataProvider.deployProActiveDataServer(dataServerFolderPath, "data");
        dataServerURI = "file://" + dataServerFolderPath;

        // start scheduler and nodes
        SchedulerTHelper.startScheduler();

        schedProxy = SchedulerProxyUIWithDSupport.getActiveInstance();

        String schedulerUrl = System.getProperty("url");
        if (schedulerUrl == null || schedulerUrl.equals("${url}")) {
            schedulerUrl = Connection.normalize(null);
        }

        schedProxy.init(schedulerUrl, SchedulerTHelper.username, SchedulerTHelper.password);

        eventListener = new MyEventListener();
        MyEventListener myListenerRemoteReference = PAActiveObject.turnActive(eventListener);
        schedProxy.addEventListener(myListenerRemoteReference);

        // delete files after the test is finihed
        inputFile.deleteOnExit();
        File outputFile = new File(outputLocalFolder, inputFileName + ".out");
        outputFile.deleteOnExit();

        File dataTransfer = new File("dataTransfer.status");
        File dataTransferBak = new File("dataTransfer.status.BAK");
        dataTransfer.deleteOnExit();
        dataTransferBak.deleteOnExit();

    }

    @org.junit.Test
    public void run() throws Throwable {
        Job job = createTestJob();

        submitJobWithDataAndWaitToFinish(job, inputLocalFolder.getAbsolutePath(), outputLocalFolder
                .getAbsolutePath());

        // check that outputLocalFolder contains a file named inputFileName.out

        File f = new File(outputLocalFolder, inputFileName + ".out");
        Assert.assertTrue(f.isFile());
    }

    public void clean() {

    }

    private Job createTestJob() throws UserException {
        TaskFlowJob job = new TaskFlowJob();
        JavaTask testTask = new JavaTask();
        testTask.setName("TestJavaTask");
        testTask.setExecutableClassName(TestDataJavaExecutable.class.getName());
        job.setInputSpace(dataServerURI);
        job.setOutputSpace(dataServerURI);

        // testTask.
        testTask.addInputFiles("*.txt", InputAccessMode.TransferFromInputSpace);
        testTask.addOutputFiles("*.out", OutputAccessMode.TransferToOutputSpace);

        job.addTask(testTask);
        setJobClasPath(job);
        return job;

    }

    public void submitJobWithDataAndWaitToFinish(Job job, String localInputFolderPath,
            String localOutputFolderPath) throws LoginException, SchedulerException, FileSystemException,
            ActiveObjectCreationException, NodeException {

        EventMonitor em = new EventMonitor(null);

        JobId id = schedProxy.submit(job, localInputFolderPath, push_url, localOutputFolderPath, pull_url);

        eventListener.setJobID(id);
        eventListener.setMonitor(em);
        waitWithMonitor(em, TIMEOUT);

    }

    /**
     * This method adds to the job the classpath of the application
     * 
     * @param job
     */
    public void setJobClasPath(Job job) {
        String appClassPath = "";
        try {
            File appMainFolder = new File(this.getClass().getProtectionDomain().getCodeSource().getLocation()
                    .toURI());
            appClassPath = appMainFolder.getAbsolutePath();

        } catch (URISyntaxException e1) {
            System.out
                    .println("Preview of the partial results will not be possible as some ressources could not be found by the system. \nThis will not alterate your results in any way. ");
            System.out
                    .println("JobCreator: The bin folder of the project is null. It is needed to set the job environment. ");
            e1.printStackTrace();
        }

        JobEnvironment je = new JobEnvironment();
        try {
            je.setJobClasspath(new String[] { appClassPath });
        } catch (IOException e) {
            System.out
                    .println("Preview of the partial results will not be possible as the job classpath could not be loaded. \nThis will not alterate your results in any way.");
            System.out.println("Could not add classpath to the job. ");
            e.printStackTrace();
        }
        job.setEnvironment(je);
    }

    private void waitWithMonitor(EventMonitor monitor, long timeout) throws ProActiveTimeoutException {
        TimeoutAccounter counter = TimeoutAccounter.getAccounter(timeout);
        synchronized (monitor) {
            monitor.setTimeouted(false);
            while (!counter.isTimeoutElapsed()) {
                if (monitor.eventOccured())
                    return;
                try {
                    System.out.println("waiting for event monitor " + monitor);
                    // System.out.println("I AM WAITING FOR EVENT : " +
                    // monitor.getWaitedEvent() + " during " +
                    // counter.getRemainingTimeout());
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

}
