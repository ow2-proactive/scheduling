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
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package functionaltests.workflow.nativetask;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.objectweb.proactive.utils.OperatingSystem;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.ForkEnvironment;
import org.ow2.proactive.scheduler.common.task.NativeTask;
import org.ow2.proactive.scheduler.common.task.dataspaces.InputAccessMode;
import org.ow2.proactive.scheduler.common.task.dataspaces.OutputAccessMode;

import functionaltests.utils.SchedulerFunctionalTest;


/**
 * Tests JAVA and PROACTIVE_HOME variables in NativeTask
 * <p/>
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 3.4
 */
@Ignore // localspace is current folder
public class TestNativeTaskPaths extends SchedulerFunctionalTest {

    private static final String OutVarsFileC = "outvarsc";
    private static final String OutVarsFileD = "outvarsd";
    private static final String scriptCLinux = "testenv.sh";
    private static final String scriptCLinuxContent = "echo foo > $localspace/" +
        OutVarsFileD + "\n";
    private static final String scriptCWindows = "testenv.bat";
    private static final String scriptCWindowsContent = "echo foo > %localspace%\\" +
        OutVarsFileD + "\n";

    @Test
    public void testNativeTaskPaths() throws Throwable {

        File in = File.createTempFile("input", "space");
        in.delete();
        in.mkdir();
        String inPath = in.getAbsolutePath();

        File out = File.createTempFile("output", "space");
        out.delete();
        out.mkdir();

        File outc = new File(out, OutVarsFileC);
        File outd = new File(out, OutVarsFileD);
        if (outc.exists()) {
            outc.delete();
        }
        if (outd.exists()) {
            outd.delete();
        }

        File scriptTestEnv = null;
        if (OperatingSystem.getOperatingSystem() == OperatingSystem.unix) {
            scriptTestEnv = new File(inPath + File.separator + scriptCLinux);
            scriptTestEnv.createNewFile();
            PrintWriter out3 = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(scriptTestEnv))));
            out3.print(scriptCLinuxContent);
            out3.close();
        } else {
            scriptTestEnv = new File(inPath + File.separator + scriptCWindows);
            scriptTestEnv.createNewFile();
            PrintWriter out3 = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(scriptTestEnv))));
            out3.print(scriptCWindowsContent);
            out3.close();
        }

        TaskFlowJob job = new TaskFlowJob();
        job.setName(this.getClass().getSimpleName());
        job.setInputSpace(in.toURI().toURL().toString());
        job.setOutputSpace(out.toURI().toURL().toString());

//        // testing paths pattern
//        NativeTask C = new NativeTask();
//        C.setName("C");
//        C.addOutputFiles(OutVarsFileC, OutputAccessMode.TransferToOutputSpace);
//        switch (OperatingSystem.getOperatingSystem()) {
//            case windows:
//                C.setCommandLine(new String[] { "cmd", "/C",
//                        "echo \"$JAVA\" \"$PROACTIVE_HOME\" > $LOCALSPACE\\" + OutVarsFileC });
//                break;
//            case unix:
//                C.setCommandLine(new String[] { "/bin/bash", "-c",
//                        "echo \\\"$JAVA\\\" \\\"$PROACTIVE_HOME\\\" > $LOCALSPACE/" + OutVarsFileC });
//                break;
//            default:
//                throw new IllegalStateException("Unsupported operating system");
//        }
//        job.addTask(C);

        // testing $USERSPACE environment variable
        NativeTask D = new NativeTask();
        D.setName("D");
        if (OperatingSystem.getOperatingSystem() == OperatingSystem.unix) {
            D.addInputFiles(scriptCLinux, InputAccessMode.TransferFromInputSpace);
        } else {
            D.addInputFiles(scriptCWindows, InputAccessMode.TransferFromInputSpace);
        }
        D.addOutputFiles(OutVarsFileD, OutputAccessMode.TransferToOutputSpace);
        switch (OperatingSystem.getOperatingSystem()) {
            case windows:
                D.setCommandLine(new String[] { "cmd", "/C", scriptCWindows });
                break;
            case unix:
                D.setCommandLine(new String[] { "/bin/bash", "-c",
                        "chmod u+x $localspace/" + scriptCLinux + "; $localspace/" + scriptCLinux });
                break;
            default:
                throw new IllegalStateException("Unsupported operating system");
        }
        D.setForkEnvironment(new ForkEnvironment("$LOCALSPACE"));
        job.addTask(D);

        Scheduler sched = schedulerHelper.getSchedulerInterface();
        JobId id = sched.submit(job);

        schedulerHelper.waitForEventJobFinished(id);
        String contentExpected = "foo";

        JobResult jr = schedulerHelper.getJobResult(id);
        Assert.assertFalse(jr.hadException());

        logger.info("Expected : '" + contentExpected + "'");

//        logger.info(jr.getAllResults().get("C").getOutput().getAllLogs(true));
//        String receivedc = IOUtils.toString(outc.toURI()).trim();
//        logger.info("Received C : '" + receivedc + "'");
//        Assert.assertEquals(contentExpected.toLowerCase(), receivedc.toLowerCase());

        logger.info(jr.getAllResults().get("D").getOutput().getAllLogs(true));
        String receivedd = IOUtils.toString(outd.toURI()).trim();
        logger.info("Received D : '" + receivedd + "'");
        Assert.assertEquals(contentExpected.toLowerCase(), receivedd.toLowerCase());

    }

}
