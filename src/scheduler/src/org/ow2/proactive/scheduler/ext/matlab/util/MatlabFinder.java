/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package org.ow2.proactive.scheduler.ext.matlab.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.OperatingSystem;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.ext.common.util.IOTools;
import org.ow2.proactive.scheduler.ext.matlab.exception.MatlabInitException;
import org.ow2.proactive.scheduler.util.LinuxShellExecuter;
import org.ow2.proactive.scheduler.util.SchedulerLoggers;
import org.ow2.proactive.scheduler.util.Shell;


public class MatlabFinder {

    /** logger **/
    protected static Logger logger = ProActiveLogger.getLogger(SchedulerLoggers.MATLAB);

    /** the OS where this JVM is running **/
    private static OperatingSystem os = OperatingSystem.getOperatingSystem();

    /**
     * Utility function to find Matlab
     * @throws IOException
     * @throws InterruptedException
     * @throws MatlabInitException
     */
    public static MatlabConfiguration findMatlab(boolean debug) throws IOException, InterruptedException,
            MatlabInitException {

        Process p1 = null;
        MatlabConfiguration answer = null;

        if (os.equals(OperatingSystem.unix)) {
            // Under linux we launch an instance of the Shell
            // and then pipe to it the script's content
            if (debug) {
                System.out.println("Using script at " +
                    PASchedulerProperties.MATLAB_SCRIPT_LINUX.getValueAsString());
            }
            InputStream is = MatlabFinder.class.getResourceAsStream(PASchedulerProperties.MATLAB_SCRIPT_LINUX
                    .getValueAsString());
            p1 = LinuxShellExecuter.executeShellScript(is, Shell.Bash);
        } else if (os.equals(OperatingSystem.windows)) {
            // We can't execute the script on Windows the same way,
            // we need to write the content of the batch file locally and then launch the file
            if (debug) {
                System.out.println("Using script at " +
                    PASchedulerProperties.MATLAB_SCRIPT_WINDOWS.getValueAsString());
            }
            InputStream is = MatlabFinder.class
                    .getResourceAsStream(PASchedulerProperties.MATLAB_SCRIPT_WINDOWS.getValueAsString());

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

        if (debug) {
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
                matlabVersion + "/" + matlabLibDirName.replace("\\", "/") + "/");
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
        MatlabFinder.findMatlab(true);
    }

}
