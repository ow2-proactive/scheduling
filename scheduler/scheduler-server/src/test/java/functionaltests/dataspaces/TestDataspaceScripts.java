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
package functionaltests.dataspaces;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;

import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.ForkEnvironment;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.dataspaces.InputAccessMode;
import org.ow2.proactive.scheduler.common.task.dataspaces.OutputAccessMode;
import org.ow2.proactive.scheduler.common.task.flow.FlowScript;
import org.ow2.proactive.scripting.SimpleScript;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import functionaltests.utils.SchedulerFunctionalTest;

import static org.junit.Assert.*;


/**
 * Tests that Dataspaces are available in pre/post/flow scripts
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 2.2
 */
public class TestDataspaceScripts extends SchedulerFunctionalTest {

    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();

    private static final String[] fileContent = new String[] { "This is", "the content", "of the",
            "file line", "by line." };

    private static final String fileName = "test";

    private static final String typeMacro = "!TYPE";
    private static final String folderMacro = "!FOLDER";

    private static final String scriptContent = "" + "def spaces = ['input','output','global','user']; \n" +
        "spaces.each { " +
        "  def f = new File('test_' + it); \n" //
        +
        "  def br = new BufferedReader(new InputStreamReader(f.newInputStream())); \n" //
        + "  def out = new PrintWriter(new BufferedWriter(new FileWriter(new File('res_" + typeMacro +
        "' + f.name)))); \n" //
        + "  def line; \n" //
        + "  while ((line = br.readLine()) != null) { \n" //
        + "    out.println(line); \n" //
        + "   } \n" //
        + "  out.close(); \n" //
        + "} \n" //
        + "loop=false;"; //

    /**
     * Creates a task with a Pre/Post/Flow scripts that copy files from input files to output files
     */
    @Test
    public void run() throws Throwable {
        File input = tmpFolder.newFolder("input");
        File output = tmpFolder.newFolder("output");
        File global = tmpFolder.newFolder("global");
        File user = tmpFolder.newFolder("user");

        /**
         * creates the testfile in both input and output spaces
         */
        BufferedOutputStream inout = new BufferedOutputStream(new FileOutputStream(new File(
            input.getAbsolutePath() + File.separator + fileName + "_input")));
        BufferedOutputStream outout = new BufferedOutputStream(new FileOutputStream(new File(
            output.getAbsolutePath() + File.separator + fileName + "_output")));
        BufferedOutputStream globout = new BufferedOutputStream(new FileOutputStream(new File(
            global.getAbsolutePath() + File.separator + fileName + "_global")));
        BufferedOutputStream userout = new BufferedOutputStream(new FileOutputStream(new File(
            user.getAbsolutePath() + File.separator + fileName + "_user")));
        for (String line : fileContent) {
            inout.write((line + "\n").getBytes());
            outout.write((line + "\n").getBytes());
            globout.write((line + "\n").getBytes());
            userout.write((line + "\n").getBytes());
        }
        inout.close();
        outout.close();
        globout.close();
        userout.close();

        /**
         * single job with single empty task
         */
        TaskFlowJob job = new TaskFlowJob();
        job.setInputSpace(input.toURI().toString());
        job.setOutputSpace(output.toURI().toString());
        job.setGlobalSpace(global.toURI().toString());
        job.setUserSpace(user.toURI().toString());

        JavaTask t = new JavaTask();
        job.addTask(t);
        job.setName(this.getClass().getSimpleName());
        t.setExecutableClassName("org.ow2.proactive.scheduler.examples.EmptyTask");
        t.setName("T");
        t.addInputFiles(fileName + "_input", InputAccessMode.TransferFromInputSpace);
        t.addInputFiles(fileName + "_user", InputAccessMode.TransferFromUserSpace);
        t.addInputFiles(fileName + "_global", InputAccessMode.TransferFromGlobalSpace);
        t.addInputFiles(fileName + "_output", InputAccessMode.TransferFromOutputSpace);

        t.addOutputFiles("res_*", OutputAccessMode.TransferToOutputSpace);
        t.addOutputFiles("res_*", OutputAccessMode.TransferToUserSpace);
        t.addOutputFiles("res_*", OutputAccessMode.TransferToGlobalSpace);

        File results = org.ow2.proactive.utils.FileUtils.createTempDirectory("test", ".results", null);
        String windowsReadyResultsPath = results.getAbsolutePath().replace("\\", "/"); // for the JS engine on Windows
        String scriptContentFiltered = scriptContent.replaceAll(folderMacro, windowsReadyResultsPath);

        t.setPreScript(new SimpleScript(scriptContentFiltered.replaceAll(typeMacro, "pre"), "groovy"));
        t.setPostScript(new SimpleScript(scriptContentFiltered.replaceAll(typeMacro, "post"), "groovy"));
        t.setFlowScript(FlowScript.createLoopFlowScript(scriptContentFiltered.replaceAll(typeMacro, "flow"),
                "groovy", "T"));

        t.setForkEnvironment(new ForkEnvironment());

        /**
         * job submission, wait on result, removal
         */
        JobId id = schedulerHelper.testJobSubmission(job);
        assertFalse(schedulerHelper.getJobResult(id).hadException());

        /**
         * check content of the files created by the script
         */

        checkFile(new File(output, "res_" + "pre" + fileName + "_user"));
        checkFile(new File(output, "res_" + "pre" + fileName + "_global"));
        checkFile(new File(output, "res_" + "pre" + fileName + "_input"));
        checkFile(new File(output, "res_" + "pre" + fileName + "_output"));

        checkFile(new File(output, "res_" + "post" + fileName + "_user"));
        checkFile(new File(output, "res_" + "post" + fileName + "_global"));
        checkFile(new File(output, "res_" + "post" + fileName + "_input"));
        checkFile(new File(output, "res_" + "post" + fileName + "_output"));

        checkFile(new File(output, "res_" + "flow" + fileName + "_user"));
        checkFile(new File(output, "res_" + "flow" + fileName + "_global"));
        checkFile(new File(output, "res_" + "flow" + fileName + "_input"));
        checkFile(new File(output, "res_" + "flow" + fileName + "_output"));

    }

    private void checkFile(File f) throws Throwable {
        assertTrue(f.exists());
        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
        String line;
        int i = 0;
        while ((line = in.readLine()) != null) {
            assertTrue("Original and copied files differ", fileContent[i].equals(line));
            i++;
        }
        in.close();
    }
}
