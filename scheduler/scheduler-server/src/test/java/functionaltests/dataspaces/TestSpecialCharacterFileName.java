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
import org.ow2.tests.FunctionalTest;
import junit.framework.Assert;

import static org.junit.Assume.assumeTrue;


/**
 * This class tests a job submission using Dataspaces. It verifies that the transfer to and from all dataspaces are functional,
 * for Java and native tasks
 *
 * @author The ProActive Team
 * @date 2 jun 08
 * @since ProActive Scheduling 1.0
 */
public class TestSpecialCharacterFileName extends FunctionalTest {

    private static String fileNameWithAccent = "myfile-é";
    private static String inputSpace = "data\\defaultinput\\user";
    private static String outputSpace = "data\\defaultoutput\\user";

    private static String schedulerStarterBatPath = "bin\\proactive-server.bat";
    private static String clientBatPath = "bin\\proactive-client.bat";
    private static String jobXmlPath = "scheduler\\scheduler-server\\src\\test\\resources\\functionaltests\\dataspaces\\Job_SpecialCharacterFileName.xml";

    private static int TIMEOUT = 20; // in seconds
    private static final String ERROR_COMMAND_EXECUTION = "Error command execution";

    private static String outputResultOrThrow(InputStream inputStream, String expr, int timeout) throws Exception {
        StringBuilder sb = new StringBuilder();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(inputStream));
            String line = null;
            long startTime = System.currentTimeMillis();
            while ((line = br.readLine()) != null &&
                    (System.currentTimeMillis() - startTime) / 1000 < timeout) {
                System.out.println("** "+ line +" **");
                sb.append(line + System.getProperty("line.separator"));
                if (line.contains(expr)) {
                    br.close();
                    return sb.toString();
                }
            }
        } finally {
            br.close();
        }
        throw new Exception(ERROR_COMMAND_EXECUTION);
    }

    File fileWithAccentIn;
    File fileWithAccentOut;

    @org.junit.Before
    public void OnlyOnWindows() throws IOException {
        assumeTrue(OperatingSystem.getOperatingSystem() == OperatingSystem.windows);

        File inputSpaceDir = new File(inputSpace);
        fileWithAccentIn = new File(inputSpaceDir.getAbsolutePath() + File.separator + fileNameWithAccent);
        fileWithAccentIn.createNewFile();
    }

    /**
     * Tests start here.
     *
     * @throws Throwable any exception that can be thrown during the test.
     */
    @org.junit.Test
    public void run() throws Throwable {

        // Start the scheduler
        ArrayList<String> schedulerCommand = new ArrayList<>();
        schedulerCommand.add(schedulerStarterBatPath);
        ProcessBuilder schedulerProcessBuilder = new ProcessBuilder(schedulerCommand);
        schedulerProcessBuilder.environment().put("processID", "0");
        outputResultOrThrow(schedulerProcessBuilder.start().getInputStream(), "started", TIMEOUT);

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
        String jobSubmissionStr = outputResultOrThrow(jobSubmissionProcessBuilder.start().getInputStream(), "submitted", TIMEOUT);

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

        while (!jobFinished && ((System.currentTimeMillis() - startTime) / 1000) < 2 * TIMEOUT)
        {
            try {
                jobFinished = (outputResultOrThrow(jobStatusProcessBuilder.start().getInputStream(), "FINISHED", TIMEOUT) != null);
            }catch (Exception e){}
        }

        if (!jobFinished)
            throw new Exception(ERROR_COMMAND_EXECUTION);

        // Assertion
        //Assert.assertTrue(new File(IOSPACE + OUT + File.separator + out).exists());
        File outputSpaceDir = new File(outputSpace);
        fileWithAccentOut = new File(outputSpaceDir.getAbsolutePath() + File.separator + fileNameWithAccent);
        Assert.assertTrue(fileWithAccentOut.exists());

        // Kill
        ProcessTree.get().killAll(Collections.singletonMap("processID", "0"));

        // Clean
        fileWithAccentIn.delete();
        fileWithAccentOut.delete();
    }
}
