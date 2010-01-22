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
package org.ow2.proactive.scheduler.ext.common.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.ow2.proactive.scheduler.common.util.SchedulerLoggers;


public class LinuxShellExecuter {
    static Logger logger = ProActiveLogger.getLogger(SchedulerLoggers.UTIL);

    static public Process executeShellScript(File file, Shell shell) throws IOException {
        if (!file.exists() || !file.canRead()) {
            throw new IOException("Read error on : " + file);
        }

        return executeShellScript(new FileInputStream(file), shell);
    }

    static public Process executeShellScript(InputStream input, Shell shell) throws IOException {
        InputStreamReader isr = new InputStreamReader(input);
        BufferedReader in = new BufferedReader(isr);
        Process pshell = Runtime.getRuntime().exec(shell.command());
        PrintWriter out = new PrintWriter(new OutputStreamWriter(pshell.getOutputStream()));

        try {
            String line;

            while ((line = in.readLine()) != null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("[SHELL SCRIPT] " + line);
                }

                out.println(line);
            }

            in.close();
        } catch (IOException ex) {
        }

        if (logger.isDebugEnabled()) {
            logger.debug("[SHELL SCRIPT] exit");
        }

        out.println("exit");
        out.flush();
        out.close();

        return pshell;
    }
}
