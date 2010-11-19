/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
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

import org.junit.Assert;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.dataspaces.InputAccessMode;
import org.ow2.proactive.scheduler.common.task.flow.FlowScript;
import org.ow2.proactive.scheduler.task.launcher.TaskLauncher;
import org.ow2.proactive.scripting.SimpleScript;

import functionalTests.FunctionalTest;


/**
 * Tests that Dataspaces are available in pre/post/flow scripts
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 2.2
 */
public class TestDataspaceScripts extends FunctionalTest {

    private static final String[] fileContent = new String[] { "This is", "the content", "of the",
            "file line", "by line." };

    private static final String fileName = "test";

    private static final String spaceMacro = "!SPACE";

    private static final String scriptContent = "" +
        "importPackage(org.objectweb.proactive.extensions.dataspaces.api); \n" //
        +
        "importPackage(java.io); \n" //
        + "var f = " + spaceMacro + ".resolveFile(\"" + fileName +
        "\"); \n" //
        +
        "var br = new BufferedReader(new InputStreamReader(f.getContent().getInputStream())); \n" //
        + "var out = new PrintWriter(new BufferedWriter(new FileWriter(new File(\"out_" + spaceMacro +
        "\")))); \n" //
        + "var line; \n" //
        + "while ((line = br.readLine()) != null) { \n" //
        + "out.println(line); \n" //
        + "} \n" //
        + "out.close(); \n" //
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
        File input = File.createTempFile("test", ".input");
        input.delete();
        input.mkdir();
        File output = File.createTempFile("test", ".output");
        output.delete();
        output.mkdir();

        /**
         * creates the testfile in both input and output spaces
         */
        BufferedOutputStream inout = new BufferedOutputStream(new FileOutputStream(new File(input
                .getAbsolutePath() +
            File.separator + fileName)));
        BufferedOutputStream outout = new BufferedOutputStream(new FileOutputStream(new File(output
                .getAbsolutePath() +
            File.separator + fileName)));
        for (String line : fileContent) {
            inout.write((line + "\n").getBytes());
            outout.write((line + "\n").getBytes());
        }
        inout.close();
        outout.close();

        /**
         * single job with single empty task
         */
        TaskFlowJob job = new TaskFlowJob();
        job.setInputSpace(input.toURI().toString());
        job.setOutputSpace(output.toURI().toString());

        JavaTask t = new JavaTask();
        job.addTask(t);
        t.setExecutableClassName("org.ow2.proactive.scheduler.examples.EmptyTask");
        t.setName("T");
        t.addInputFiles(fileName, InputAccessMode.TransferFromInputSpace);
        // PRE : reads from INPUT
        t.setPreScript(new SimpleScript(scriptContent.replaceAll(spaceMacro,
                TaskLauncher.DS_INPUT_BINDING_NAME), "javascript"));
        // POST : reads from SCRATCH
        t.setPostScript(new SimpleScript(scriptContent.replaceAll(spaceMacro,
                TaskLauncher.DS_SCRATCH_BINDING_NAME), "javascript"));
        // FLOW : reads from OUTPUT
        t.setFlowScript(FlowScript.createLoopFlowScript(scriptContent.replaceAll(spaceMacro,
                TaskLauncher.DS_OUTPUT_BINDING_NAME), "T"));

        /**
         * job submission, wait on result, removal
         */
        JobId id = SchedulerTHelper.testJobSubmission(job);
        JobResult res = SchedulerTHelper.getJobResult(id);
        Assert.assertFalse(SchedulerTHelper.getJobResult(id).hadException());
        // SchedulerTHelper.removeJob(id);
        // SchedulerTHelper.waitForEventJobRemoved(id);

        /**
         * check content of the files created by the script
         */
        File preFile = new File("out_" + TaskLauncher.DS_INPUT_BINDING_NAME);
        File postFile = new File("out_" + TaskLauncher.DS_SCRATCH_BINDING_NAME);
        File flowFile = new File("out_" + TaskLauncher.DS_OUTPUT_BINDING_NAME);

        checkFile(preFile);
        checkFile(postFile);
        checkFile(flowFile);
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
        f.delete();
    }
}
