/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package functionaltests;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.net.URI;
import java.util.Map.Entry;

import org.junit.Assert;
import org.objectweb.proactive.extensions.vfsprovider.FileSystemServerDeployer;
import org.ow2.proactive.scheduler.common.job.JobEnvironment;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.NativeTask;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.dataspaces.FileSelector;
import org.ow2.proactive.scheduler.common.task.dataspaces.InputAccessMode;
import org.ow2.proactive.scheduler.common.task.dataspaces.OutputAccessMode;

import functionalTests.FunctionalTest;
import functionaltests.executables.DSWorker;


/**
 * This class tests a basic actions of a job submission to ProActive scheduler :
 * Connection to scheduler, with authentication
 * Register a monitor to Scheduler in order to receive events concerning
 * job submission.
 *
 * Submit a Task flow job (test 1).
 * After the job submission, the test monitor all jobs states changes, in order
 * to observe its execution :
 * job submitted (test 2),
 * job pending to running (test 3),
 * all task pending to running, and all tasks running to finished (test 4),
 * job running to finished (test 5).
 * After it retrieves job's result and check that all
 * tasks results are available (test 6).
 *
 * @author The ProActive Team
 * @date 2 jun 08
 * @since ProActive Scheduling 1.0
 */
public class TestJobDataspaceSubmission extends FunctionalTest {

    private static String IOSPACE = System.getProperty("java.io.tmpdir") + File.separator + "scheduler_test" +
        File.separator;
    private static String IN = "input";
    private static String OUT = "output";

    private static String in1 = "ia.txt";
    private static String in2 = "ib.txt";
    private static String out1 = "pa.txt";
    private static String out2 = "pb.txt";
    private static String out3 = "pc.txt";
    private static String out4 = "pd.txt";

    File inputDir;
    File outputDir;

    /**
    * Tests start here.
    *
    * @throws Throwable any exception that can be thrown during the test.
    */
    @org.junit.Test
    public void run() throws Throwable {

        //create initial directories and files
        setup();

        TaskFlowJob job = new TaskFlowJob();
        FileSystemServerDeployer filesServer = new FileSystemServerDeployer(IN, IOSPACE + IN, true);
        String url = filesServer.getVFSRootURL();
        job.setInputSpace(url);
        filesServer = new FileSystemServerDeployer(OUT, IOSPACE + OUT, true);
        url = filesServer.getVFSRootURL();
        job.setOutputSpace(url);
        job.setName("DS_test");

        NativeTask t = new NativeTask();
        t.addInputFiles(in1, InputAccessMode.TransferFromInputSpace);
        t.addOutputFiles(out1, OutputAccessMode.TransferToOutputSpace);
        t.setName("native_java1");
        t.setCommandLine(new String[] { "/bin/bash", "-c",
                "cat $LOCALSPACE/" + in1 + " > $LOCALSPACE/" + out1 });
        job.addTask(t);

        t = new NativeTask();
        t.addInputFiles(in2, InputAccessMode.TransferFromOutputSpace);
        t.addOutputFiles(out2, OutputAccessMode.TransferToOutputSpace);
        t.setName("native_java2");
        t.setCommandLine(new String[] { "/bin/bash", "-c",
                "cat $LOCALSPACE/" + in2 + " > $LOCALSPACE/" + out2 });
        job.addTask(t);

        NativeTask t1 = new NativeTask();
        t1.addDependence(t);
        t1.addInputFiles(in1, InputAccessMode.TransferFromInputSpace);
        t1.addInputFiles(new FileSelector(new String[] { "*.txt" }, new String[] { "*a.txt" }),
                InputAccessMode.TransferFromOutputSpace);
        t1.addOutputFiles("*c.txt", OutputAccessMode.TransferToOutputSpace);
        t1.setName("native_java3");
        t1.setCommandLine(new String[] { "/bin/bash", "-c",
                "cat $LOCALSPACE/" + in1 + " $LOCALSPACE/" + out2 + " > $LOCALSPACE/" + out3 });
        job.addTask(t1);

        JavaTask jt = new JavaTask();
        jt.addDependence(t1);
        jt.setName("java_task");
        jt.setExecutableClassName(DSWorker.class.getName());
        jt.addArgument("pc", out3);
        jt.addArgument("pd", out4);
        job.addTask(jt);
        JobEnvironment env = new JobEnvironment();
        final URI uri = DSWorker.class.getProtectionDomain().getCodeSource().getLocation().toURI();
        env.setJobClasspath(new String[] { new File(uri).getAbsolutePath() });
        job.setEnvironment(env);

        JobId id = SchedulerTHelper.testJobSubmission(job);

        // check results are 0
        JobResult res = SchedulerTHelper.getJobResult(id);
        Assert.assertFalse(SchedulerTHelper.getJobResult(id).hadException());
        for (Entry<String, TaskResult> entry : res.getAllResults().entrySet()) {
            Assert.assertEquals(0, entry.getValue().value());
        }

        //remove job
        SchedulerTHelper.removeJob(id);
        SchedulerTHelper.waitForEventJobRemoved(id);

        //check files
        File fout = new File(outputDir.getAbsolutePath() + File.separator + out1);
        Assert.assertEquals(in1, getContent(fout));
        fout = new File(outputDir.getAbsolutePath() + File.separator + out2);
        Assert.assertEquals(in2, getContent(fout));
        fout = new File(outputDir.getAbsolutePath() + File.separator + out3);
        Assert.assertEquals(in1 + in2, getContent(fout));
        fout = new File(outputDir.getAbsolutePath() + File.separator + out4);
        Assert.assertEquals(in1 + in2, getContent(fout));

        //and clean tmp space
        clean();
    }

    private void setup() throws Exception {
        inputDir = new File(IOSPACE + IN);
        outputDir = new File(IOSPACE + OUT);
        inputDir.mkdirs();
        outputDir.mkdirs();
        File fi1 = new File(inputDir.getAbsolutePath() + File.separator + in1);
        fi1.createNewFile();
        File fi2 = new File(outputDir.getAbsolutePath() + File.separator + in2);
        fi2.createNewFile();
        putContent(in1, fi1);
        putContent(in2, fi2);
    }

    private void clean() {
        for (File f : inputDir.listFiles()) {
            f.delete();
        }
        for (File f : outputDir.listFiles()) {
            f.delete();
        }
        inputDir.delete();
        outputDir.delete();
        new File(IOSPACE).delete();
    }

    private void putContent(String s, File f) throws Exception {
        FileWriter fw = new FileWriter(f);
        fw.write(s);
        fw.close();
    }

    private String getContent(File f) throws Exception {
        FileReader fr = null;
        BufferedReader br = null;
        try {
            fr = new FileReader(f);
            br = new BufferedReader(fr);
            return br.readLine();
        } finally {
            fr.close();
            br.close();
        }
    }
}