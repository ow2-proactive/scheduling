/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.ow2.proactive.resourcemanager.nodesource.infrastructure;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import org.apache.log4j.Logger;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.ssh.SSHClient;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.utils.OperatingSystem;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.utils.RMLoggers;


/**
 * A class where static utility methods are welcome
 */
public class Utils {
    private static final Logger logger = ProActiveLogger.getLogger(RMLoggers.NODESOURCE);

    /**
     * Execute a specific command on a remote host through SSH
     *
     * @param host the remote host on which to execute the command
     * @param cmd the command to execute on the remote host
     * @param sshOptions the options that will be passed to the ssh command
     * @return the Process in which the SSH command is running;
     *          NOT the actual process on the remote host, although it can be used
     *          to read the remote process' output.
     * @throws IOException SSH command execution failed
     */
    public static Process runSSHCommand(InetAddress host, String cmd, String sshOptions) throws IOException {
        // build the SSH command using ProActive's SSH client:
        // will recover keys/identities if they exist
        String sshCmd = null;
        StringBuilder sb = new StringBuilder();
        //building path to java executable
        String javaHome = System.getProperty("java.home");
        if (javaHome.contains(" ")) {
            switch (OperatingSystem.getOperatingSystem()) {
                case unix:
                    javaHome = javaHome.replace(" ", "\\ ");
                    break;
                case windows:
                    sb.append("\"");
                    break;
            }
        }
        sb.append(javaHome);
        sb.append(File.separator);
        sb.append("bin");
        sb.append(File.separator);
        sb.append("java");
        if (javaHome.contains(" ")) {
            switch (OperatingSystem.getOperatingSystem()) {
                case windows:
                    sb.append("\"");
                    break;
            }
        }
        //building classpath
        sb.append(" -cp ");
        sb.append(PAResourceManagerProperties.RM_HOME.getValueAsString());
        sb.append(File.separator);
        sb.append("dist");
        sb.append(File.separator);
        sb.append("lib");
        sb.append(File.separator);
        sb.append("ProActive.jar");
        sb.append(" ");
        //mandatory property
        //exe's name
        sb.append(SSHClient.class.getName());
        //SSH options supplied by user from cli|gui
        sb.append(" ");
        sb.append(sshOptions);
        sb.append(" ");
        //target machine
        sb.append(host.getHostName());
        //the command
        sb.append(" \"");
        sb.append(cmd);
        sb.append("\"");
        sshCmd = sb.toString();

        logger.info("Executing SSH command: '" + sshCmd + "'");

        Process p = null;
        // start the SSH command in a new process and not a thread:
        // easier killing, prevents the client from polluting stdout
        switch (OperatingSystem.getOperatingSystem()) {
            case unix:
                p = Runtime.getRuntime().exec(
                        new String[] { CentralPAPropertyRepository.PA_GCMD_UNIX_SHELL.getValue(), "-c",
                                sshCmd });
                break;
            case windows:
                p = Runtime.getRuntime().exec(sshCmd);
                break;
        }

        return p;
    }

    /**
     * Extract the stacktrace of a throwable object and returns it as a String
     * @param t The throwable object from which one the stacktrace is going to be extracted
     * @return the stacktrace of the parameter as a String
     */
    public static String getStacktrace(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        String result = sw.toString();
        pw.close();
        return result;
    }
}
