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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package functionaltests.dataspaces;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Map;

import org.junit.Assert;
import org.objectweb.proactive.extensions.dataspaces.vfs.selector.FileSelector;
import org.objectweb.proactive.extensions.vfsprovider.FileSystemServerDeployer;
import org.objectweb.proactive.utils.OperatingSystem;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.NativeTask;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.dataspaces.InputAccessMode;
import org.ow2.proactive.scheduler.common.task.dataspaces.OutputAccessMode;

import functionaltests.utils.SchedulerFunctionalTest;


/**
 * This class tests a job submission using Dataspaces. It verifies that the transfer to and from all dataspaces are functional,
 * for Java and native tasks
 *
 * @author The ProActive Team
 * @date 2 jun 08
 * @since ProActive Scheduling 1.0
 */
public class TestJobDataspaceSubmission extends SchedulerFunctionalTest {

    private static String IOSPACE =
            System.getProperty("java.io.tmpdir") + File.separator + "scheduler_test" + File.separator;

    private static String IN = "input";
    private static String OUT = "output";
    private static String GLOB = "global";
    private static String USER = "user";

    private static String in1 = "ia.txt";
    private static String in2 = "ib.txt";
    private static String in3 = "ic.txt";
    private static String in4 = "id.txt";
    private static String out1 = "pa.txt";
    private static String out2 = "pb.txt";
    private static String out3 = "pc.txt";
    private static String out4 = "pd.txt";

    private static String out10 = "paa.txt";
    private static String out11 = "pbb.txt";
    private static String out12 = "pcc.txt";

    File inputDir;
    File outputDir;
    File globalDir;
    File userDir;

    /**
     * Tests start here.
     *
     * @throws Throwable any exception that can be thrown during the test.
     */
    @org.junit.Test
    public void testJobDataspaceSubmission() throws Throwable {

        //create initial directories and files
        setup();

        TaskFlowJob job = new TaskFlowJob();
        FileSystemServerDeployer filesServerIn = new FileSystemServerDeployer(IN, IOSPACE + IN, true, true);
        String url = filesServerIn.getVFSRootURL();
        job.setInputSpace(url);
        FileSystemServerDeployer filesServerOut = new FileSystemServerDeployer(OUT, IOSPACE + OUT, true);
        url = filesServerOut.getVFSRootURL();
        job.setOutputSpace(url);
        FileSystemServerDeployer filesServerGlob = new FileSystemServerDeployer(GLOB, IOSPACE + GLOB, true);
        url = filesServerGlob.getVFSRootURL();
        job.setGlobalSpace(url);
        FileSystemServerDeployer filesServerUser = new FileSystemServerDeployer(USER, IOSPACE + USER, true);
        url = filesServerUser.getVFSRootURL();
        job.setUserSpace(url);
        job.setName(this.getClass().getSimpleName());

        NativeTask t1 = new NativeTask();
        t1.addInputFiles(in1, InputAccessMode.TransferFromInputSpace);
        t1.addOutputFiles(out1, OutputAccessMode.TransferToOutputSpace);
        t1.setName("native_java1");
        switch (OperatingSystem.getOperatingSystem()) {
            case windows:
                t1.setCommandLine(new String[] { "cmd", "/C",
                        "type " + in1 + " > " + out1 });
                break;
            case unix:
                t1.setCommandLine(new String[] { "/bin/bash", "-c",
                        "cat " + in1 + " > " + out1 });
                break;
            default:
                throw new IllegalStateException("Unsupported operating system");
        }

        job.addTask(t1);

        NativeTask t2 = new NativeTask();
        t2.addInputFiles(in2, InputAccessMode.TransferFromOutputSpace);
        t2.addOutputFiles(out2, OutputAccessMode.TransferToOutputSpace);
        t2.setName("native_java2");
        switch (OperatingSystem.getOperatingSystem()) {
            case windows:
                t2.setCommandLine(new String[] { "cmd", "/C",
                        "type " + in2 + " > " + out2 });
                break;
            case unix:
                t2.setCommandLine(new String[] { "/bin/bash", "-c",
                        "cat " + in2 + " > " + out2 });
                break;
            default:
                throw new IllegalStateException("Unsupported operating system");
        }
        job.addTask(t2);

        NativeTask t3 = new NativeTask();
        t3.addInputFiles(in3, InputAccessMode.TransferFromGlobalSpace);
        t3.addOutputFiles(out3, OutputAccessMode.TransferToGlobalSpace);
        t3.setName("native_java3");
        switch (OperatingSystem.getOperatingSystem()) {
            case windows:
                t3.setCommandLine(new String[] { "cmd", "/C",
                        "type " + in3 + " > " + out3 });
                break;
            case unix:
                t3.setCommandLine(new String[] { "/bin/bash", "-c",
                        "cat " + in3 + " > " + out3 });
                break;
            default:
                throw new IllegalStateException("Unsupported operating system");
        }
        job.addTask(t3);

        NativeTask t4 = new NativeTask();
        t4.addInputFiles(in4, InputAccessMode.TransferFromUserSpace);
        t4.addOutputFiles(out4, OutputAccessMode.TransferToUserSpace);
        t4.setName("native_java4");
        switch (OperatingSystem.getOperatingSystem()) {
            case windows:
                t4.setCommandLine(new String[] { "cmd", "/C",
                        "type " + in4 + " > " + out4 });
                break;
            case unix:
                t4.setCommandLine(new String[] { "/bin/bash", "-c",
                        "cat " + in4 + " > " + out4 });
                break;
            default:
                throw new IllegalStateException("Unsupported operating system");
        }
        job.addTask(t4);

        NativeTask t10 = new NativeTask();
        t10.addDependence(t2);
        t10.addInputFiles(in1, InputAccessMode.TransferFromInputSpace);
        t10.addInputFiles(new FileSelector("*b.txt"),
                InputAccessMode.TransferFromOutputSpace);
        t10.addOutputFiles("*aa.txt", OutputAccessMode.TransferToOutputSpace);
        t10.setName("native_java10");
        switch (OperatingSystem.getOperatingSystem()) {
            case windows:
                t10.setCommandLine(new String[] { "cmd", "/C",
                        "type " + in1 + " " + out2 + " > " + out10 });
                break;
            case unix:
                t10.setCommandLine(new String[] { "/bin/bash", "-c",
                        "cat " + in1 + " " + out2 + " > " + out10 });
                break;
            default:
                throw new IllegalStateException("Unsupported operating system");
        }
        job.addTask(t10);

        NativeTask t11 = new NativeTask();
        t11.addDependence(t3);
        t11.addInputFiles(in1, InputAccessMode.TransferFromInputSpace);
        t11.addInputFiles(new FileSelector("*c.txt"),
                InputAccessMode.TransferFromGlobalSpace);
        t11.addOutputFiles("*bb.txt", OutputAccessMode.TransferToGlobalSpace);
        t11.setName("native_java11");
        switch (OperatingSystem.getOperatingSystem()) {
            case windows:
                t11.setCommandLine(new String[] { "cmd", "/C",
                        "type " + in1 + " " + out3 + " > " + out11 });
                break;
            case unix:
                t11.setCommandLine(new String[] { "/bin/bash", "-c",
                        "cat " + in1 + " " + out3 + " > " + out11 });
                break;
            default:
                throw new IllegalStateException("Unsupported operating system");
        }
        job.addTask(t11);

        NativeTask t12 = new NativeTask();
        t12.addDependence(t4);
        t12.addInputFiles(in1, InputAccessMode.TransferFromInputSpace);
        t12.addInputFiles(new FileSelector("*d.txt"), InputAccessMode.TransferFromUserSpace);
        t12.addOutputFiles("*cc.txt", OutputAccessMode.TransferToUserSpace);
        t12.setName("native_java7");
        switch (OperatingSystem.getOperatingSystem()) {
            case windows:
                t12.setCommandLine(new String[] { "cmd", "/C",
                        "type " + in1 + " " + out4 + " > " + out12 });
                break;
            case unix:
                t12.setCommandLine(new String[] { "/bin/bash", "-c",
                        "cat " + in1 + " " + out4 + " > " + out12 });
                break;
            default:
                throw new IllegalStateException("Unsupported operating system");
        }
        job.addTask(t12);

        JobId id = schedulerHelper.testJobSubmission(job);

        // check results are 0
        JobResult res = schedulerHelper.getJobResult(id);
        Assert.assertFalse(schedulerHelper.getJobResult(id).hadException());
        for (Map.Entry<String, TaskResult> entry : res.getAllResults().entrySet()) {
            Assert.assertEquals(0, entry.getValue().value());
        }

        //remove job
        schedulerHelper.removeJob(id);
        schedulerHelper.waitForEventJobRemoved(id);

        //check files
        File fout = new File(outputDir.getAbsolutePath() + File.separator + out1);
        Assert.assertEquals(in1, getContent(fout));
        fout = new File(outputDir.getAbsolutePath() + File.separator + out2);
        Assert.assertEquals(in2, getContent(fout));
        fout = new File(globalDir.getAbsolutePath() + File.separator + out3);
        Assert.assertEquals(in3, getContent(fout));
        fout = new File(userDir.getAbsolutePath() + File.separator + out4);
        Assert.assertEquals(in4, getContent(fout));

        fout = new File(outputDir.getAbsolutePath() + File.separator + out10);
        Assert.assertEquals(in1 + in2, getContent(fout));
        fout = new File(globalDir.getAbsolutePath() + File.separator + out11);
        Assert.assertEquals(in1 + in3, getContent(fout));
        fout = new File(userDir.getAbsolutePath() + File.separator + out12);
        Assert.assertEquals(in1 + in4, getContent(fout));

//        fout = new File(outputDir.getAbsolutePath() + File.separator + out20);
//        Assert.assertEquals(in1 + in2, getContent(fout));
//        fout = new File(globalDir.getAbsolutePath() + File.separator + out21);
//        Assert.assertEquals(in1 + in3, getContent(fout));
//        fout = new File(userDir.getAbsolutePath() + File.separator + out22);
//        Assert.assertEquals(in1 + in4, getContent(fout));

        filesServerIn.terminate();
        filesServerOut.terminate();
        filesServerGlob.terminate();
        filesServerUser.terminate();
        //and clean tmp space
        clean();
    }

    private void setup() throws Exception {
        inputDir = new File(IOSPACE + IN);
        outputDir = new File(IOSPACE + OUT);
        globalDir = new File(IOSPACE + GLOB);
        userDir = new File(IOSPACE + USER);
        inputDir.mkdirs();
        outputDir.mkdirs();
        globalDir.mkdirs();
        userDir.mkdirs();
        File fi1 = new File(inputDir.getAbsolutePath() + File.separator + in1);
        fi1.createNewFile();
        File fi2 = new File(outputDir.getAbsolutePath() + File.separator + in2);
        fi2.createNewFile();
        File fi3 = new File(globalDir.getAbsolutePath() + File.separator + in3);
        fi2.createNewFile();
        File fi4 = new File(userDir.getAbsolutePath() + File.separator + in4);
        fi2.createNewFile();
        putContent(in1, fi1);
        putContent(in2, fi2);
        putContent(in3, fi3);
        putContent(in4, fi4);
    }

    private void clean() {
        for (File f : inputDir.listFiles()) {
            f.delete();
        }
        for (File f : outputDir.listFiles()) {
            f.delete();
        }
        for (File f : globalDir.listFiles()) {
            f.delete();
        }
        for (File f : userDir.listFiles()) {
            f.delete();
        }
        inputDir.delete();
        outputDir.delete();
        globalDir.delete();
        userDir.delete();
        new File(IOSPACE).delete();
    }

    private void putContent(String s, File f) throws Exception {
        FileWriter fw = new FileWriter(f);
        fw.write(s);
        fw.close();
    }

    private String getContent(File f) throws Exception {
        try (FileReader fr = new FileReader(f); BufferedReader br = new BufferedReader(fr)) {
            return br.readLine();
        }
    }

}