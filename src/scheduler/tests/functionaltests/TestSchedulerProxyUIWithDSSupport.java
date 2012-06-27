package functionaltests;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;

import javax.security.auth.login.LoginException;

import org.apache.commons.vfs.FileSystemException;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.node.NodeException;
import org.ow2.proactive.scheduler.common.exception.SchedulerException;
import org.ow2.proactive.scheduler.common.exception.UserException;
import org.ow2.proactive.scheduler.common.job.Job;
import org.ow2.proactive.scheduler.common.job.JobEnvironment;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.dataspaces.InputAccessMode;
import org.ow2.proactive.scheduler.common.task.dataspaces.OutputAccessMode;
import org.ow2.proactive.scheduler.common.task.executable.JavaExecutable;
import org.ow2.proactive.scheduler.common.util.dsclient.SchedulerProxyUIWithDSupport;
import org.ow2.tests.FunctionalTest;


/**
 * @author esalagea
 * 
 */
public class TestSchedulerProxyUIWithDSSupport extends FunctionalTest {

    public static final String workFolderPath = System.getProperty("java.io.tmpdir") + File.separator +
        "testDS_LocalFolder";
    public static final String dataServerFolderPath = System.getProperty("java.io.tmpdir") + File.separator +
        "testDS_remoteFolder";

    private File inputLocalFolder;
    private File outputLocalFolder;
    private File workLocalFolder;
    private DataServerProvider dataProvider;
    private String dataServerURI;

    private String push_url = "file://" + dataServerFolderPath;
    private String pull_url = "file://" + dataServerFolderPath;

    private final String inputFileName = "input.txt";

    //@Before
    public void init() throws Exception {

        workLocalFolder = new File(workFolderPath);
        inputLocalFolder = new File(workLocalFolder, "input");
        outputLocalFolder = new File(workLocalFolder, "output");

        inputLocalFolder.mkdirs();
        outputLocalFolder.mkdirs();

        // ------------- create an input File ------------
        File f = new File(inputLocalFolder, inputFileName);

        FileWriter fw = new FileWriter(f);
        for (int i = 0; i <= 100; i++)
            fw.write("Some random input");
        fw.close();

        // ----------------- start Data Server -------------
        // this simulates a remote data server
        dataServerURI = dataProvider.deployProActiveDataServer(dataServerFolderPath, "data");

        // start scheduler and nodes
        SchedulerTHelper.startScheduler();

    }

    //@After
    public void terminate() throws ProActiveException {
        dataProvider.stopServer();
    }

    @org.junit.Test
    @org.junit.Ignore("This implementation is not yet finihed")
    public void run() throws Throwable {
        Job job = createTestJob();
        try {
            submitJobWithData(job, inputLocalFolder.getAbsolutePath(), outputLocalFolder.getAbsolutePath());
        } catch (LoginException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (FileSystemException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SchedulerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ActiveObjectCreationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NodeException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    private Job createTestJob() {
        TaskFlowJob job = new TaskFlowJob();
        JavaTask testTask = new JavaTask();
        testTask.setName("TestJavaTask");
        testTask.setExecutableClassName(TreatDataJavaExecutable.class.getName());
        job.setInputSpace(dataServerURI);
        job.setOutputSpace(dataServerURI);

        // testTask.
        testTask.addInputFiles("in.txt", InputAccessMode.TransferFromInputSpace);
        testTask.addOutputFiles("*.out", OutputAccessMode.TransferToOutputSpace);

        try {
            job.addTask(testTask);
        } catch (UserException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        setJobClasPath(job);
        return job;

    }

    public void submitJobWithData(Job job, String localInputFolderPath, String localOutputFolderPath)
            throws LoginException, SchedulerException, FileSystemException, ActiveObjectCreationException,
            NodeException {
        SchedulerProxyUIWithDSupport schedProxy = SchedulerProxyUIWithDSupport.getActiveInstance();

        schedProxy.init(SchedulerTHelper.schedulerDefaultURL, SchedulerTHelper.username,
                SchedulerTHelper.password);
        schedProxy.submit(job, localInputFolderPath, push_url, localOutputFolderPath, pull_url);
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

}

class TreatDataJavaExecutable extends JavaExecutable {

    @Override
    public Serializable execute(TaskResult... results) throws Throwable {

        System.out.println("local space real uri: " + this.getLocalSpace().getRealURI());
        System.out.println("local space virtual uri: " + this.getLocalSpace().getVirtualURI());

        File localSpaceFolder = new File(URI.create(this.getLocalSpace().getRealURI()));
        System.out.println("Using localspace folder " + localSpaceFolder.getAbsolutePath());
        File[] files = localSpaceFolder.listFiles();

        for (File file : files) {

            if (file.isFile()) {
                System.out.println("Treating input file " + file.getAbsolutePath());

            } else {
                System.out.println(file.getAbsolutePath() + " is not a file. ");
            }

            File fout = new File(file.getAbsolutePath().concat(".out"));
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            BufferedWriter bw = new BufferedWriter(new FileWriter(fout));

            String line;
            while ((line = br.readLine()) != null) {
                bw.write(line);
                bw.newLine();
            }
            bw.close();
            br.close();
            System.out.println("Written file " + fout.getAbsolutePath());
        }// for

        System.out.println("Task End");
        return "OK";
    }

}
