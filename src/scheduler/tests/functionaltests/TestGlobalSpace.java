package functionaltests;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import org.junit.Assert;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.dataspaces.InputAccessMode;
import org.ow2.proactive.scheduler.common.task.dataspaces.OutputAccessMode;
import org.ow2.proactive.scripting.SimpleScript;

import functionalTests.FunctionalTest;


public class TestGlobalSpace extends FunctionalTest {

    private static final String[][] inFiles = { { "A", "Content of A" }, { "B", "not much" },
            { "_1234", "!@#%$@%54vc54\b\t\\\\\nasd123!@#", "res1", "one of the output files" },
            { "res2", "second\noutput\nfile" }, { "__.res_3?", "third\toutput\nfile\t&^%$$#@!\n" } };

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
        "importPackage(java.io);                               \n" + //
        "var out;                                              \n" + //
        "var arr = " + inFileArr + ";                          \n" + //
        "for (var i=0; i < arr.length; i++) {                  \n" + //
        "  var input = localspace.resolveFile(arr[i]);         \n" + //
        "  if (! input) continue;                              \n" + //
        "  var br = input.getContent().getInputStream();       \n" + //
        "  var ff = localspace.resolveFile(                    \n" + //
        "     arr[i] + \".glob.A\");\n                         \n" + //
        "  ff.createFile();                                    \n" + //
        "  out = ff.getContent().getOutputStream();            \n" + //
        "  var c;                                              \n" + //
        "  while ((c = br.read()) > 0) {                       \n" + //
        "    out.write(c);                                     \n" + //
        "  }                                                   \n" + //
        "  out.close();                                        \n" + //
        "}                                                     \n" + //
        "                                                      \n" + //
        "";

    private static final String scriptB = "" + //
        "importPackage(java.io);                               \n" + //
        "var out;                                              \n" + //
        "var arr = " + inFileArr + ";                          \n" + //
        "for (var i=0; i < arr.length; i++) {                  \n" + //
        "  var input = localspace.resolveFile(                 \n" + //
        "      arr[i] + \".glob.A\");                          \n" + //
        "  if (! input) continue;                              \n" + //
        "  var br = input.getContent().getInputStream();       \n" + //
        "  var ff = localspace.resolveFile(                    \n" + //
        "     arr[i] + \".out\");\n                            \n" + //
        "  ff.createFile();                                    \n" + //
        "  out = ff.getContent().getOutputStream();            \n" + //
        "  var c;                                              \n" + //
        "  while ((c = br.read()) > 0) {                       \n" + //
        "    out.write(c);                                     \n" + //
        "  }                                                   \n" + //
        "  out.close();                                        \n" + //
        "}                                                     \n" + //
        "                                                      \n" + //
        "";

    @org.junit.Test
    public void run() throws Throwable {

        File glob = File.createTempFile("global", "space");
        glob.delete();
        glob.mkdir();
        File gloDir = new File(glob.getAbsolutePath() + File.separator + "1");
        gloDir.mkdirs();

        File in = File.createTempFile("input", "space");
        in.delete();
        in.mkdir();
        String inPath = in.getAbsolutePath();

        File out = File.createTempFile("output", "space");
        out.delete();
        out.mkdir();
        String outPath = out.getAbsolutePath();

        /**
         * Writes inFiles in INPUT
         */
        writeFiles(inFiles, inPath);

        TaskFlowJob job = new TaskFlowJob();
        job.setInputSpace(in.toURL().toString());
        job.setOutputSpace(out.toURL().toString());

        JavaTask A = new JavaTask();
        A.setExecutableClassName("org.ow2.proactive.scheduler.examples.EmptyTask");
        A.setName("A");
        for (String[] file : inFiles) {
            A.addInputFiles(file[0], InputAccessMode.TransferFromInputSpace);
            A.addOutputFiles(file[0] + ".glob.A", OutputAccessMode.TransferToGlobalSpace);
        }
        A.setPreScript(new SimpleScript(scriptA, "javascript"));
        job.addTask(A);

        JavaTask B = new JavaTask();
        B.setExecutableClassName("org.ow2.proactive.scheduler.examples.EmptyTask");
        B.setName("B");
        for (String[] file : inFiles) {
            B.addInputFiles(file[0] + ".glob.A", InputAccessMode.TransferFromGlobalSpace);
            B.addOutputFiles(file[0] + ".out", OutputAccessMode.TransferToOutputSpace);
        }
        B.setPreScript(new SimpleScript(scriptB, "javascript"));
        job.addTask(B);

        /**
         * appends GLOBALSPACE property to Scheduler .ini file
         */
        File tmpProps = File.createTempFile("tmp", ".props");
        PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(tmpProps)));
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(
            SchedulerTHelper.functionalTestSchedulerProperties))));
        String line;
        while ((line = br.readLine()) != null) {
            pw.println(line);
        }
        pw.println("pa.scheduler.dataspace.globalurl=" + gloDir.toURL().toString());
        pw.close();
        br.close();

        /**
         * start scheduler, submit job
         */
        SchedulerTHelper.startScheduler(tmpProps.getAbsolutePath());
        JobId id = SchedulerTHelper.getSchedulerInterface().submit(job);
        while (true) {
            try {
                if (SchedulerTHelper.getJobResult(id) != null) {
                    break;
                }
                Thread.sleep(2000);
            } catch (Throwable exc) {
            }
        }

        Assert.assertFalse(SchedulerTHelper.getJobResult(id).hadException());

        /**
         * check: global was cleaned
         */
        Assert.assertTrue("GLOBAL dir " + glob + " was not cleared", gloDir.list().length == 0);

        /**
         * check: inFiles > IN > LOCAL A > GLOBAL > LOCAL B > OUT 
         */
        for (int i = 0; i < inFiles.length; i++) {
            File f = new File(outPath + File.separator + inFiles[i][0] + ".out");
            Assert.assertTrue("File does not exist: " + f.getAbsolutePath(), f.exists());
            Assert.assertEquals("Original and copied files differ", inFiles[i][1], getContent(f));
            f.delete();
            File inf = new File(inPath + File.separator + inFiles[i][0]);
            inf.delete();
        }
    }

    /**
     * @param f a regular file
     * @return the content of the file as a String
     * @throws IOException
     */
    private String getContent(File f) throws IOException {
        InputStream is = new FileInputStream(f);
        String res = "";
        int b;
        while ((b = is.read()) > 0) {
            res += (char) b;
        }

        return res;
    }

    /**
     * @param files Writes files: {{filename1, filecontent1},...,{filenameN, filecontentN}}
     * @param path in this director 
     * @throws IOException
     */
    private void writeFiles(String[][] files, String path) throws IOException {
        for (String[] file : files) {
            File f = new File(path + File.separator + file[0]);
            f.createNewFile();

            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
                f))));
            out.print(file[1]);
            out.close();
        }
    }
}
