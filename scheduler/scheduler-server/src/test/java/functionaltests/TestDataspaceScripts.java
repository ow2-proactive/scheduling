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
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package functionaltests;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;

import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.dataspaces.InputAccessMode;
import org.ow2.proactive.scheduler.common.task.flow.FlowScript;
import org.ow2.proactive.scheduler.task.TaskLauncher;
import org.ow2.proactive.scripting.SimpleScript;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;


/**
 * Tests that Dataspaces are available in pre/post/flow scripts
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 2.2
 */
public class TestDataspaceScripts extends SchedulerConsecutive {

    private static final String[] fileContent = new String[] { "This is", "the content", "of the",
            "file line", "by line." };

    private static final String fileName = "test";

    private static final String typeMacro = "!TYPE";
    private static final String folderMacro = "!FOLDER";

    private static final String scriptContent = "" +
        "def spaces = [input:input, localspace:localspace, output:output, user:user, global:global]; \n" +
        "spaces.each { " + "  def f = it.value.resolveFile(\"" + fileName +
        "\"); \n" //
        +
        "  def br = new BufferedReader(new InputStreamReader(f.getContent().getInputStream())); \n" //
        + "  def out = new PrintWriter(new BufferedWriter(new FileWriter(new File(new File('" + folderMacro +
        "'),\"out_" + typeMacro + "_\"+it.key)))); \n" //
        + "  def line; \n" //
        + "  while ((line = br.readLine()) != null) { \n" //
        + "    out.println(line); \n" //
        + "   } \n" //
        + "  out.close(); \n" //
        + "} \n" //
        + "loop=false;"; //

    /**
     * Creates a task with a Pre/Post/Flow script that opens a file in
     * the Input/Output/Local space, copies its content, and checks both are identical
     */
    @org.junit.Test
    public void run() throws Throwable {

        /**
         * creates input and output spaces in temporary dirs
         */
        File input = org.ow2.proactive.utils.FileUtils.createTempDirectory("test", ".input", null);
        File output = org.ow2.proactive.utils.FileUtils.createTempDirectory("test", ".output", null);
        File global = org.ow2.proactive.utils.FileUtils.createTempDirectory("test", ".global", null);
        File user = org.ow2.proactive.utils.FileUtils.createTempDirectory("test", ".user", null);

        /**
         * creates the testfile in both input and output spaces
         */
        BufferedOutputStream inout = new BufferedOutputStream(new FileOutputStream(new File(input
                .getAbsolutePath() +
            File.separator + fileName)));
        BufferedOutputStream outout = new BufferedOutputStream(new FileOutputStream(new File(output
                .getAbsolutePath() +
            File.separator + fileName)));
        BufferedOutputStream globout = new BufferedOutputStream(new FileOutputStream(new File(global
                .getAbsolutePath() +
            File.separator + fileName)));
        BufferedOutputStream userout = new BufferedOutputStream(new FileOutputStream(new File(user
                .getAbsolutePath() +
            File.separator + fileName)));
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
        t.addInputFiles(fileName, InputAccessMode.TransferFromInputSpace);

        File results = org.ow2.proactive.utils.FileUtils.createTempDirectory("test", ".results", null);
        String windowsReadyResultsPath = results.getAbsolutePath().replace("\\", "/"); // for the JS engine on Windows
        String scriptContentFiltered = scriptContent.replaceAll(folderMacro, windowsReadyResultsPath);

        t.setPreScript(new SimpleScript(scriptContentFiltered.replaceAll(typeMacro, "pre"), "groovy"));
        t.setPostScript(new SimpleScript(scriptContentFiltered.replaceAll(typeMacro, "post"), "groovy"));
        t.setFlowScript(FlowScript.createLoopFlowScript(scriptContentFiltered.replaceAll(typeMacro, "flow"),
                "groovy", "T"));

        /**
         * job submission, wait on result, removal
         */
        JobId id = SchedulerTHelper.testJobSubmission(job);
        Assert.assertFalse(SchedulerTHelper.getJobResult(id).hadException());

        /**
         * check content of the files created by the script
         */
        File preinFile = new File(results, "out_pre_" + TaskLauncher.DS_INPUT_BINDING_NAME);
        File prescratchFile = new File(results, "out_pre_" + TaskLauncher.DS_SCRATCH_BINDING_NAME);
        File preoutFile = new File(results, "out_pre_" + TaskLauncher.DS_OUTPUT_BINDING_NAME);
        File preglobFile = new File(results, "out_pre_" + TaskLauncher.DS_GLOBAL_BINDING_NAME);
        File preuserFile = new File(results, "out_pre_" + TaskLauncher.DS_USER_BINDING_NAME);
        File postinFile = new File(results, "out_post_" + TaskLauncher.DS_INPUT_BINDING_NAME);
        File postscratchFile = new File(results, "out_post_" + TaskLauncher.DS_SCRATCH_BINDING_NAME);
        File postoutFile = new File(results, "out_post_" + TaskLauncher.DS_OUTPUT_BINDING_NAME);
        File postglobFile = new File(results, "out_post_" + TaskLauncher.DS_GLOBAL_BINDING_NAME);
        File postuserFile = new File(results, "out_post_" + TaskLauncher.DS_USER_BINDING_NAME);
        File flowinFile = new File(results, "out_flow_" + TaskLauncher.DS_INPUT_BINDING_NAME);
        File flowscratchFile = new File(results, "out_flow_" + TaskLauncher.DS_SCRATCH_BINDING_NAME);
        File flowoutFile = new File(results, "out_flow_" + TaskLauncher.DS_OUTPUT_BINDING_NAME);
        File flowglobFile = new File(results, "out_flow_" + TaskLauncher.DS_GLOBAL_BINDING_NAME);
        File flowuserFile = new File(results, "out_flow_" + TaskLauncher.DS_USER_BINDING_NAME);

        checkFile(preinFile);
        checkFile(prescratchFile);
        checkFile(preoutFile);
        checkFile(preglobFile);
        checkFile(preuserFile);
        checkFile(postinFile);
        checkFile(postscratchFile);
        checkFile(postoutFile);
        checkFile(postglobFile);
        checkFile(postuserFile);
        checkFile(flowinFile);
        checkFile(flowscratchFile);
        checkFile(flowoutFile);
        checkFile(flowglobFile);
        checkFile(flowuserFile);
    }

    private void checkFile(File f) throws Throwable {
        Assert.assertTrue(f.exists());
        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
        String line;
        int i = 0;
        while ((line = in.readLine()) != null) {
            Assert.assertTrue("Original and copied files differ", fileContent[i].equals(line));
            i++;
        }
        in.close();
        FileUtils.deleteQuietly(f);
    }
}
