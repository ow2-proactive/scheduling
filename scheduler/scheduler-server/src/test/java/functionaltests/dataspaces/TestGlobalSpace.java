/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package functionaltests.dataspaces;

import static functionaltests.utils.SchedulerTHelper.log;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.*;
import java.net.URI;

import org.apache.commons.io.FileUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.objectweb.proactive.extensions.dataspaces.vfs.VFSFactory;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.ForkEnvironment;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.dataspaces.InputAccessMode;
import org.ow2.proactive.scheduler.common.task.dataspaces.OutputAccessMode;
import org.ow2.proactive.scripting.SimpleScript;

import functionaltests.utils.SchedulerFunctionalTestWithRestart;


/**
 * Submits a job using the default Global User Space, it checks at the end that the output files can be accessed in the Global Space
 * <p>
 * This test does :
 * <ul><li>write inFiles to INPUT
 * <li>task A: transfer inFiles from INPUT to SCRATCH
 * <li>task A: copy SCRATCH/inFiles to SCRATCH/inFiles.glob.A in pre-script
 * <li>task A: transfer inFiles.glob.A from SCRATCH to GLOBAL
 * <li>task B: transfer inFiles.glob.A from GLOBAL to SCRATCH
 * <li>task B: copy SCRATCH/inFiles.glob.A to SCRATCH/inFiles.out in pre-script
 * <li>task B: transfer inFiles.out from SCRATCH to OUTPUT
 * </ul>
 * Then, the test checks that the GLOBAL space has been cleared; and that inFiles.out have
 * been copied to OUTPUT and are identical to the ones written in the INPUT.
 * 
 * 
 * @author The ProActive Team
 * @since ProActive Scheduling 2.2
 */
public class TestGlobalSpace extends SchedulerFunctionalTestWithRestart {

    private static final String[][] inFiles = { { "A", "Content of A" }, { "B", "not much" },
                                                { "_1234", "!@#%$@%54vc54\b\t\\\\\nasd123!@#", "res1",
                                                  "one of the output files" },
                                                { "res2", "second\noutput\nfile" },
                                                { "__.res_3", "third\toutput\nfile\t&^%$$#@!\n" } };

    private static String inFileArr = "";
    static {
        inFileArr += "[";
        for (int i = 0; i < inFiles.length; i++) {
            inFileArr += "\"" + inFiles[i][0] + "\"";
            if (i < inFiles.length - 1) {
                inFileArr += ",";
            }
        }
        inFileArr += "]";
    }

    private static final String scriptA = "" + //
                                          "def out;                                              \n" + //
                                          "def arr = " + inFileArr + ";                          \n" + //
                                          "for (def i=0; i < arr.size(); i++) {                  \n" + //
                                          "  def input = new File(arr[i]);         \n" + //
                                          "  if (! input) continue;                              \n" + //
                                          "  def br = input.newInputStream();       \n" + //
                                          "  def ff = new File(                    \n" + //
                                          "     arr[i] + \".glob.A\");\n                         \n" + //
                                          "  out = ff.newOutputStream();            \n" + //
                                          "  def c;                                              \n" + //
                                          "  while ((c = br.read()) > 0) {                       \n" + //
                                          "    out.write(c);                                     \n" + //
                                          "  }                                                   \n" + //
                                          "  out.close();                                        \n" + //
                                          "}                                                     \n" + //
                                          "                                                      \n" + //
                                          "";

    private static final String scriptB = "" + //
                                          "def out;                                              \n" + //
                                          "def arr = " + inFileArr + ";                          \n" + //
                                          "for (def i=0; i < arr.size(); i++) {                  \n" + //
                                          "  def input = new File(                 \n" + //
                                          "      arr[i] + \".glob.A\");                          \n" + //
                                          "  if (! input.exists()) {                             \n" + //
                                          "    continue;                                         \n" + //
                                          "  }                                                   \n" + //
                                          "  def br = input.newInputStream();       \n" + //
                                          "  def ff = new File(                    \n" + //
                                          "     arr[i] + \".out\");\n                            \n" + //
                                          "  out = ff.newOutputStream();            \n" + //
                                          "  def c;                                              \n" + //
                                          "  while ((c = br.read()) > 0) {                       \n" + //
                                          "    out.write(c);                                     \n" + //
                                          "  }                                                   \n" + //
                                          "  out.close();                                        \n" + //
                                          "}                                                     \n" + //
                                          "                                                      \n" + //
                                          "";

    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();

    @Test
    public void testGlobalSpace() throws Throwable {

        File in = tmpFolder.newFolder("input_space");
        String inPath = in.getAbsolutePath();

        File out = tmpFolder.newFolder("output_space");
        String outPath = out.getAbsolutePath();

        writeFiles(inFiles, inPath);

        TaskFlowJob job = new TaskFlowJob();
        job.setName(this.getClass().getSimpleName());
        job.setInputSpace(in.toURI().toURL().toString());
        job.setOutputSpace(out.toURI().toURL().toString());

        JavaTask A = new JavaTask();
        A.setExecutableClassName("org.ow2.proactive.scheduler.examples.EmptyTask");
        A.setName("A");
        for (String[] file : inFiles) {
            A.addInputFiles(file[0], InputAccessMode.TransferFromInputSpace);
            A.addOutputFiles(file[0] + ".glob.A", OutputAccessMode.TransferToGlobalSpace);
        }
        A.setPreScript(new SimpleScript(scriptA, "groovy"));
        A.setForkEnvironment(new ForkEnvironment());
        job.addTask(A);

        JavaTask B = new JavaTask();
        B.setExecutableClassName("org.ow2.proactive.scheduler.examples.EmptyTask");
        B.setName("B");
        B.addDependence(A);
        for (String[] file : inFiles) {
            B.addInputFiles(file[0] + ".glob.A", InputAccessMode.TransferFromGlobalSpace);
            B.addOutputFiles(file[0] + ".out", OutputAccessMode.TransferToOutputSpace);
        }
        B.setPreScript(new SimpleScript(scriptB, "groovy"));
        B.setForkEnvironment(new ForkEnvironment());
        job.addTask(B);

        Scheduler scheduler = schedulerHelper.getSchedulerInterface();
        JobId id = scheduler.submit(job);

        schedulerHelper.waitForEventJobFinished(id);
        assertFalse(schedulerHelper.getJobResult(id).hadException());

        /**
         * check: inFiles > IN > LOCAL A > GLOBAL > LOCAL B > OUT 
         */
        for (String[] inFile : inFiles) {
            File f = new File(outPath + File.separator + inFile[0] + ".out");
            assertTrue("File does not exist: " + f.getAbsolutePath(), f.exists());
            Assert.assertEquals("Original and copied files differ", inFile[1], FileUtils.readFileToString(f));
            f.delete();
            File inf = new File(inPath + File.separator + inFile[0]);
            inf.delete();
        }

        /**
         * check that the file produced is accessible in the global user space via the scheduler API
         */
        String globalURI = scheduler.getGlobalSpaceURIs().get(0);
        assertTrue(globalURI.startsWith("file:"));
        String globalPath = new File(new URI(globalURI)).getAbsolutePath();

        FileSystemManager fsManager = VFSFactory.createDefaultFileSystemManager();
        for (String[] file : inFiles) {
            FileObject outFile = fsManager.resolveFile(globalURI + "/" + file[0] + ".glob.A");
            log("Checking existence of " + outFile.getURL());
            assertTrue(outFile.getURL() + " exists", outFile.exists());

            File outFile2 = new File(globalPath, file[0] + ".glob.A");
            log("Checking existence of " + outFile2);
            assertTrue(outFile2 + " exists", outFile2.exists());
        }
    }

    /**
     * @param files Writes files: {{filename1, filecontent1},...,{filenameN, filecontentN}}
     * @param path in this director 
     * @throws IOException
     */
    private void writeFiles(String[][] files, String path) throws IOException {
        for (String[] file : files) {
            File f = new File(path + File.separator + file[0]);
            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f))));
            out.print(file[1]);
            out.close();
        }
    }
}
