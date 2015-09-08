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
package functionaltests.dataspaces;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;

import org.objectweb.proactive.utils.OperatingSystem;
import org.ow2.proactive.process_tree_killer.ProcessTree;
import functionaltests.utils.SchedulerFunctionalTest;
import junit.framework.Assert;

import static org.junit.Assume.assumeTrue;


/**
 * This class tests a job submission using Dataspaces. It verifies that the transfer to and from dataspaces are functional with special characters.
 *
 * @author The ProActive Team
 * @date 31 aug 15
 * @since ProActive Scheduling 1.0
 */
public class TestSpecialCharacterFileName extends SchedulerFunctionalTest {

    private static String wrongFileNameWithAccent = "myfile-Ã©";
    private static String fileNameWithAccent = "myfile-é";
    private static String inputSpace = "data\\defaultinput\\user";
    private static String outputSpace = "data\\defaultoutput\\user";

    private static String schedulerStarterBatPath = "bin\\proactive-server.bat";
    private static String clientBatPath = "bin\\proactive-client.bat";
    private static String jobXmlPath = "scheduler\\scheduler-server\\src\\test\\resources\\functionaltests\\dataspaces\\Job_SpecialCharacterFileName.xml";

    private static int TIMEOUT = 30; // in seconds
    private static final String ERROR_COMMAND_EXECUTION = "Error command execution";

    private static String returnExprInResultBeforeTimeout(InputStream inputStream, String expr, int timeout) throws Exception {
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        long startTime = System.currentTimeMillis();

        while ((line = br.readLine()) != null && (System.currentTimeMillis() - startTime) / 1000 < timeout) {
            sb.append(line + System.getProperty("line.separator"));

            if (line.contains(expr)) {
                br.close();
                logger.debug("HERE0 " + sb);
                logger.info("HERE0 " + sb);
                return sb.toString();
            }
        }
        br.close();
        logger.debug("HERE1 " + sb);
        logger.info("HERE1 " + sb);
        return null;
    }

    File fileWithAccentIn;
    File fileWithAccentOut;
    File wrongFileWithAccentOut;

    @org.junit.Before
    public void OnlyOnWindows() throws IOException {
        assumeTrue(OperatingSystem.getOperatingSystem() == OperatingSystem.windows);
//        assumeTrue(!System.getProperty("os.name").equalsIgnoreCase("windows 7"));

        // In some cases, the current directory can be scheduler-server/.
        // So we have to set it to the project root dir
        if(new File(".").getAbsolutePath().contains("scheduler-server"))
        {
            String pathCorrector = ".." + File.separator + ".." + File.separator;
            inputSpace = pathCorrector + inputSpace;
            schedulerStarterBatPath = pathCorrector + schedulerStarterBatPath;
            clientBatPath = pathCorrector + clientBatPath;
            jobXmlPath = pathCorrector + jobXmlPath;
            outputSpace = pathCorrector + outputSpace;
        }

        File inputSpaceDir = new File(inputSpace);
        inputSpaceDir.mkdirs();
        String inputSpaceDirPath = inputSpaceDir.getAbsolutePath();
        fileWithAccentIn = new File(inputSpaceDirPath + File.separator + fileNameWithAccent);
        fileWithAccentIn.createNewFile();
    }

    /**
     * Tests start here.
     *
     * @throws Throwable any exception that can be thrown during the test.
     */
//    @Ignore
    @org.junit.Test
    public void run() throws Throwable {


        // Now we launch the scheduler from the generated script, to consider the -Dfile.encoding parameter
        schedulerHelper.killScheduler();

        // Start the scheduler
        ArrayList<String> schedulerCommand = new ArrayList<>();
        schedulerCommand.add(schedulerStarterBatPath);
        ProcessBuilder schedulerProcessBuilder = new ProcessBuilder(schedulerCommand);
        schedulerProcessBuilder.environment().put("processID", "0");
        if(returnExprInResultBeforeTimeout(schedulerProcessBuilder.start().getInputStream(), "started", TIMEOUT) == null)
        {
            // Kill & Clean
            ProcessTree.get().killAll(Collections.singletonMap("processID", "0"));
            fileWithAccentIn.delete();
            throw new Exception(ERROR_COMMAND_EXECUTION);
        }
        System.out.println("FINISHED0");

        // Start the proactive client to submit the job
        ArrayList<String> clientCommand = new ArrayList<>();
        clientCommand.add(clientBatPath);
        clientCommand.add("-l");
        clientCommand.add("user");
        clientCommand.add("-p");
        clientCommand.add("pwd");
        clientCommand.add("-s");
        clientCommand.add(jobXmlPath);
        ProcessBuilder jobSubmissionProcessBuilder = new ProcessBuilder(clientCommand);
        String jobSubmissionStr;

        if ((jobSubmissionStr= returnExprInResultBeforeTimeout(
                jobSubmissionProcessBuilder.start().getInputStream(), "submitted", TIMEOUT))==null)
        {
            // Kill & Clean
            ProcessTree.get().killAll(Collections.singletonMap("processID", "0"));
            fileWithAccentIn.delete();
            throw new Exception(ERROR_COMMAND_EXECUTION);
        }
        System.out.println("FINISHED1");

        // Retrieve the jobId
        String[] result = jobSubmissionStr.split("'");
        String jobId = result[result.length - 2];

        // Ensure the job is finished
        ArrayList<String> jobStatusCommand = new ArrayList<>();
        jobStatusCommand.add(clientBatPath);
        jobStatusCommand.add("-js");
        jobStatusCommand.add(jobId);
        ProcessBuilder jobStatusProcessBuilder = new ProcessBuilder(jobStatusCommand);

        long startTime = System.currentTimeMillis();
        boolean jobFinished = false;

        while (!jobFinished && ((System.currentTimeMillis() - startTime) / 1000) < 5 * TIMEOUT) {
            System.out.println("SLEEP");
            Thread.sleep(5000);
            jobFinished = (returnExprInResultBeforeTimeout(jobStatusProcessBuilder.start().getInputStream(),"FINISHED", TIMEOUT) != null);
        }

        if (!jobFinished)
        {
            // Kill & Clean
            ProcessTree.get().killAll(Collections.singletonMap("processID", "0"));
            fileWithAccentIn.delete();
            throw new Exception(ERROR_COMMAND_EXECUTION);
        }
        System.out.println("FINISHED2");

        // Assertion
        //Assert.assertTrue(new File(IOSPACE + OUT + File.separator + out).exists());
        File outputSpaceDir = new File(outputSpace);
        fileWithAccentOut = new File(outputSpaceDir.getAbsolutePath() + File.separator + fileNameWithAccent);
        wrongFileWithAccentOut = new File(outputSpaceDir.getAbsolutePath() + File.separator + wrongFileNameWithAccent);

        try {
            //Assert.assertTrue(wrongFileWithAccentOut.exists());
            Assert.assertTrue(fileWithAccentOut.exists());
        }finally {
            // Kill & Clean
            ProcessTree.get().killAll(Collections.singletonMap("processID", "0"));
            fileWithAccentIn.delete();
            fileWithAccentOut.delete();
        }
    }
}
