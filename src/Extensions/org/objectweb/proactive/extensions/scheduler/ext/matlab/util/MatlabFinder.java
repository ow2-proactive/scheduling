package org.objectweb.proactive.extensions.scheduler.ext.matlab.util;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.config.PAProperties;
import org.objectweb.proactive.core.util.OperatingSystem;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.scheduler.ext.common.util.IOTools;
import org.objectweb.proactive.extensions.scheduler.ext.matlab.exception.MatlabInitException;
import org.objectweb.proactive.extensions.scheduler.util.LinuxShellExecuter;
import org.objectweb.proactive.extensions.scheduler.util.Shell;

import java.io.*;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class MatlabFinder {

    /** logger **/
    protected static Logger logger = ProActiveLogger.getLogger(Loggers.SCHEDULER_MATLAB_EXT);

    /** the OS where this JVM is running **/
    private static OperatingSystem os = OperatingSystem.getOperatingSystem();

    /**
     * Utility function to find Matlab
     * @throws IOException
     * @throws InterruptedException
     * @throws MatlabInitException
     */
    public static MatlabConfiguration findMatlab() throws IOException, InterruptedException,
            MatlabInitException {

        Process p1 = null;
        MatlabConfiguration answer = null;

        if (os.equals(OperatingSystem.unix)) {
            // Under linux we launch an instance of the Shell
            // and then pipe to it the script's content
            InputStream is = MatlabFinder.class
                    .getResourceAsStream(PAProperties.PA_SCHEDULER_EXT_MATLAB_SCRIPT_LINUX.getValue());
            p1 = LinuxShellExecuter.executeShellScript(is, Shell.Bash);
        } else if (os.equals(OperatingSystem.windows)) {
            // We can't execute the script on Windows the same way,
            // we need to write the content of the batch file locally and then launch the file
            InputStream is = MatlabFinder.class
                    .getResourceAsStream(PAProperties.PA_SCHEDULER_EXT_MATLAB_SCRIPT_WINDOWS.getValue());

            // Code for writing the content of the stream inside a local file
            List<String> inputLines = IOTools.getContentAsList(is);
            File batchFile = new File("find_matlab_command.bat");

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
                throw new MatlabInitException("can't write in : " + batchFile);
            }

            // End of this code

            // finally we launch the batch file
            p1 = Runtime.getRuntime().exec("find_matlab_command.bat");
        } else {
            throw new UnsupportedOperationException("Finding Matlab on " + os + " is not supported yet");
        }

        ArrayList<String> lines = IOTools.getContentAsList(p1.getInputStream());

        if (logger.isDebugEnabled()) {
            System.out.println("Result of script :");
            for (String ln : lines) {
                System.out.println(ln);
            }
        }

        // The batch file is supposed to write, if it's successful, two lines :
        // 1st line : the full path to the matlab command
        // 2nd line : the name of the os-dependant arch dir
        if (p1.waitFor() == 0) {
            String full_command = lines.get(0);

            File file = new File(full_command);
            String matlabCommandName = file.getName();
            String matlabHome = file.getParentFile().getParentFile().getAbsolutePath();
            String matlabLibDirName = lines.get(1);
            String matlabVersion = lines.get(2);
            String ptolemyPath;
            try {
                ptolemyPath = findPtolemyLibDir(matlabVersion, matlabLibDirName);
            } catch (URISyntaxException e) {
                throw new MatlabInitException(e);
            }

            answer = new MatlabConfiguration(matlabHome, matlabVersion, matlabLibDirName, matlabCommandName,
                ptolemyPath);

        } else {
            StringWriter error_message = new StringWriter();
            PrintWriter pw = new PrintWriter(error_message);
            pw.println("Error during find_matlab script execution:");

            for (String l : lines) {
                pw.println(l);
            }

            throw new MatlabInitException(error_message.toString());
        }
        return answer;
    }

    /**
     * Finds where the ptolemy library is installed for this specific architecture
     * @return a path to ptolemy libraries
     * @throws IOException
     * @throws URISyntaxException
     * @throws MatlabInitException
     * @throws IOException 
     * @throws URISyntaxException 
     */
    private static String findPtolemyLibDir(String matlabVersion, String matlabLibDirName)
            throws MatlabInitException, IOException, URISyntaxException {
        JarURLConnection conn = (JarURLConnection) MatlabFinder.class.getResource("/ptolemy/matlab")
                .openConnection();
        URL jarFileURL = conn.getJarFileURL();

        File jarFile = new File(jarFileURL.toURI());
        File libDirFile = jarFile.getParentFile();
        URI ptolemyLibDirURI = libDirFile.toURI().resolve(
                matlabVersion + os.fileSeparator() + matlabLibDirName + os.fileSeparator());
        File answer = new File(ptolemyLibDirURI);

        if (!answer.exists() || !answer.canRead()) {
            throw new MatlabInitException("Can't find ptolemy native library at " + answer +
                ". The native library is generated from scripts in PROACTIVE/scripts/unix/matlab. Refer to README file.");
        } else {
            File libraryFile = new File(ptolemyLibDirURI.resolve(System.mapLibraryName("ptmatlab")));
            if (!libraryFile.exists() || !libraryFile.canRead()) {
                throw new MatlabInitException("Can't find ptolemy native library at " + libraryFile +
                    ". The native library is generated from scripts in PROACTIVE/scripts/unix/matlab. Refer to README file.");
            }
        }
        return answer.getAbsolutePath();
    }

    public static void main(String[] args) throws MatlabInitException, IOException, InterruptedException {
        MatlabFinder.findMatlab();
    }

}
