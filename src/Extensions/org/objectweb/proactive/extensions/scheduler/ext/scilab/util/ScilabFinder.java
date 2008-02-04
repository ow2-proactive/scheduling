package org.objectweb.proactive.extensions.scheduler.ext.scilab.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import org.objectweb.proactive.core.util.OperatingSystem;
import org.objectweb.proactive.extensions.scheduler.ext.common.util.IOTools;
import org.objectweb.proactive.extensions.scheduler.ext.scilab.SimpleScilab;
import org.objectweb.proactive.extensions.scheduler.ext.scilab.exception.ScilabInitException;
import org.objectweb.proactive.extensions.scheduler.util.LinuxShellExecuter;
import org.objectweb.proactive.extensions.scheduler.util.Shell;


public class ScilabFinder {

    // the OS where this JVM is running
    private static OperatingSystem os = OperatingSystem.getOperatingSystem();

    /**
     * Utility function to find Scilab
     * @throws IOException
     * @throws InterruptedException
     * @throws ScilabInitException
     */
    public static final ScilabConfiguration findScilab() throws IOException, InterruptedException,
            ScilabInitException {

        Process p1 = null;
        ScilabConfiguration answer = null;

        if (os.equals(OperatingSystem.unix)) {
            // Under linux we launch an instance of the Shell
            // and then pipe to it the script's content
            InputStream is = SimpleScilab.class.getResourceAsStream("find_scilab_command.sh");
            p1 = LinuxShellExecuter.executeShellScript(is, Shell.Bash);
        } else if (os.equals(OperatingSystem.windows)) {
            // We can't execute the script on Windows the same way,
            // we need to write the content of the batch file locally and then launch the file
            InputStream is = SimpleScilab.class.getResourceAsStream("find_scilab_command.bat");

            // Code for writing the content of the stream inside a local file
            List<String> inputLines = IOTools.getContentAsList(is);
            File batchFile = new File("find_scilab_command.bat");

            if (batchFile.exists()) {
                batchFile.delete();
            }

            batchFile.createNewFile();
            batchFile.deleteOnExit();

            if (batchFile.canWrite()) {
                PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(batchFile)));

                for (String line : inputLines) {
                    pw.println(line);
                    pw.flush();
                }

                pw.close();
            } else {
                throw new ScilabInitException("can't write in : " + batchFile);
            }

            // End of this code

            // finally we launch the batch file
            p1 = Runtime.getRuntime().exec("find_scilab_command.bat");
        } else {
            throw new UnsupportedOperationException("Finding Scilab on " + os + " is not supported yet");
        }

        List<String> lines = IOTools.getContentAsList(p1.getInputStream());

        for (String ln : lines) {
            System.out.println(ln);
        }

        // The batch file is supposed to write, if it's successful, two lines :
        // 1st line : the full path to the scilab command
        // 2nd line : the name of the os-dependant arch dir
        if (p1.waitFor() == 0) {
            String scilabHome = lines.get(0);
            answer = new ScilabConfiguration(scilabHome);

        } else {
            StringWriter error_message = new StringWriter();
            PrintWriter pw = new PrintWriter(error_message);
            pw.println("Error during find_scilab script execution:");

            for (String l : lines) {
                pw.println(l);
            }

            throw new ScilabInitException(error_message.toString());
        }
        return answer;
    }

}
