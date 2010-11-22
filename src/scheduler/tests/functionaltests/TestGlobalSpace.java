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
            { "_1234", "!@#%$@%54vc54\b\t\\\\\nasd123!@#" } };
    private static final String[][] outFiles = { { "res1", "one of the output files" },
            { "res2", "second\noutput\nfile" }, { "__.res_3?", "third\toutput\nfile\t&^%$$#@!\n" }, };

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

    private static final String script = "" + //
        "importPackage(java.io);                               \n" + //
        "var out;                                              \n" + //
        "for (var i=0; i < args.length; i++) {                 \n" + //
        "  if (i % 2 == 0) {                                   \n" + //
        "    var ff = globalspace.resolveFile(args[i]);        \n" + //
        "    ff.createFile();                                  \n" + //
        "    out = new PrintWriter(new OutputStreamWriter(     \n" + //
        "      ff.getContent().getOutputStream()));            \n" + //
        "  } else {                                            \n" + //
        "    out.print(args[i]);                               \n" + //
        "    out.close();                                      \n" + //
        "  }                                                   \n" + //
        "}                                                     \n" + //
        "var arr = " + inFileArr + ";                          \n" + //
        "for (var i=0; i < arr.length; i++) {                  \n" + //
        "  var input = localspace.resolveFile(arr[i]);         \n" + //
        "  if (! input) continue;                              \n" + //
        "  var br = input.getContent().getInputStream();       \n" + //
        "  var ff = globalspace.resolveFile(arr[i] + \".dup\");\n" + //
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

        /**
         * Writes inFiles in GLOBAL
         */
        writeFiles(inFiles, glob.getAbsolutePath());

        /**
         * The Job : one task with a POST script
         * transfers inFiles from GLOBAL to SCRATCH
         * writes outFiles to SCRATCH
         * transfers outFiles from SCRATCH to GLOBAL
         * copies inFiles+".dup" from SCRATCH to GLOBAL
         */
        TaskFlowJob job = new TaskFlowJob();
        JavaTask t = new JavaTask();
        job.addTask(t);
        job.setLogFile("/tmp/LOGGRR");
        t.setExecutableClassName("org.ow2.proactive.scheduler.examples.EmptyTask");
        t.setName("T");
        for (String[] file : inFiles) {
            t.addInputFiles(file[0], InputAccessMode.TransferFromGlobalSpace);
        }
        for (String[] file : outFiles) {
            t.addOutputFiles(file[0], OutputAccessMode.TransferToGlobalSpace);
        }
        String[] params = new String[outFiles.length * 2];
        for (int i = 0; i < outFiles.length; i++) {
            params[i * 2] = outFiles[i][0];
            params[i * 2 + 1] = outFiles[i][1];
        }
        t.setPostScript(new SimpleScript(script, "javascript", params));

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
        pw.println("pa.scheduler.dataspace.globalurl=" + glob.toURL().toString());
        pw.close();
        br.close();

        /**
         * start scheduler, submit job
         */
        SchedulerTHelper.startScheduler(tmpProps.getAbsolutePath());
        JobId id = SchedulerTHelper.getSchedulerInterface().submit(job);
        // JobId id = SchedulerTHelper.testJobSubmission(job);
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
         * check that outFiles > POST > SCRATCH > GLOBAL went ok
         */
        for (int i = 0; i < outFiles.length; i++) {
            File f = new File(glob.getAbsolutePath() + File.separator + outFiles[i][0]);
            Assert.assertTrue("File does not exist: " + outFiles[i][0], f.exists());
            Assert.assertEquals("Original and copied files differ", outFiles[i][1], getContent(f));
            f.delete();
        }

        /**
         * check that inFiles > GLOBAL > SCRATCH > GLOBAL+".dup" went ok
         */
        for (int i = 0; i < inFiles.length; i++) {
            File f = new File(glob.getAbsolutePath() + File.separator + inFiles[i][0] + ".dup");
            Assert.assertTrue("File does not exist: " + f.getAbsolutePath(), f.exists());
            Assert.assertEquals("Original and copied files differ", inFiles[i][1], getContent(f));
            f.delete();
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
