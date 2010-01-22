/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
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
package org.ow2.proactive.scripting.helper.filetransfer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


/**
 * ScriptLoggerHelper...
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 1.0
 */
public class ScriptLoggerHelper {

    static String logsDirName = ".proactive_logs";

    /**
     * If filepath is absolute, we append in that file
     * If it is relative, we append in tmpDir+file.separator+filePath
     * @param filePath
     * @throws IOException
     */
    public static void logToFile(String filePath, String message) throws IOException {
        String logsAbsoluteFilePath;
        File testF = new File(filePath);
        if (testF.isAbsolute())
            logsAbsoluteFilePath = filePath;
        else {
            String tmpDirPath = System.getProperty("java.io.tmpdir");
            if (!tmpDirPath.endsWith(File.separator))
                tmpDirPath += File.separator;

            String logsDirPath = tmpDirPath + logsDirName;
            File logsDir = new File(logsDirPath);
            if (!(logsDir.isDirectory()))
                logsDir.mkdir();
            logsAbsoluteFilePath = logsDirPath + File.separator + filePath;
        }

        File logsFile = new File(logsAbsoluteFilePath);
        if (!logsFile.exists()) {
            logsFile.createNewFile();
        }

        //System.out.println("logging to file "+logsFile+" msg: "+message);

        BufferedWriter bw = new BufferedWriter(new FileWriter(logsFile, true));
        bw.append(message + "\n");
        bw.close();
        //System.out.println("Log ok. ");

    }

}
