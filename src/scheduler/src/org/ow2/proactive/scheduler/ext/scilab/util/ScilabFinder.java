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
package org.ow2.proactive.scheduler.ext.scilab.util;

import org.objectweb.proactive.core.util.OperatingSystem;
import org.ow2.proactive.scheduler.ext.common.util.IOTools;
import org.ow2.proactive.scheduler.ext.scilab.exception.ScilabInitException;
import org.ow2.proactive.scheduler.util.LinuxShellExecuter;
import org.ow2.proactive.scheduler.util.Shell;

import java.io.*;
import java.util.List;


public class ScilabFinder {

    // the OS where this JVM is running
    private static OperatingSystem os = OperatingSystem.getOperatingSystem();

    private static String SCILAB_SCRIPT_LINUX = "find_matlab_command.sh";
    private static String SCILAB_SCRIPT_WINDOWS = "find_matlab_command.bat";

    /**
     * Utility function to find Scilab
     * @throws IOException
     * @throws InterruptedException
     * @throws ScilabInitException
     */
    public static final ScilabConfiguration findScilab(boolean debug) throws IOException,
            InterruptedException, ScilabInitException {

        Process p1 = null;
        ScilabConfiguration answer = null;

        if (os.equals(OperatingSystem.unix)) {
            // Under linux we launch an instance of the Shell
            // and then pipe to it the script's content
            InputStream is = ScilabFinder.class.getResourceAsStream(SCILAB_SCRIPT_LINUX);
            p1 = LinuxShellExecuter.executeShellScript(is, Shell.Bash);
        } else if (os.equals(OperatingSystem.windows)) {
            // We can't execute the script on Windows the same way,
            // we need to write the content of the batch file locally and then launch the file
            InputStream is = ScilabFinder.class.getResourceAsStream(SCILAB_SCRIPT_WINDOWS);

            // Code for writing the content of the stream inside a local file
            List<String> inputLines = IOTools.getContentAsList(is);
            File batchFile = new File(SCILAB_SCRIPT_WINDOWS);

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
            p1 = Runtime.getRuntime().exec(SCILAB_SCRIPT_WINDOWS);
        } else {
            throw new UnsupportedOperationException("Finding Scilab on " + os + " is not supported yet");
        }

        List<String> lines = IOTools.getContentAsList(p1.getInputStream());

        if (debug) {
            System.out.println("Result of script :");
            for (String ln : lines) {
                System.out.println(ln);
            }
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
