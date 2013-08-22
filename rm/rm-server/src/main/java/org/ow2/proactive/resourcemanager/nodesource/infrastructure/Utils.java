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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;

import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.ssh.SSHClient;
import org.objectweb.proactive.utils.OperatingSystem;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.apache.log4j.Logger;


/**
 * A class where static utility methods are welcome
 */
public class Utils {
    private static final Logger logger = Logger.getLogger(Utils.class);

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
        final String rmHome = PAResourceManagerProperties.RM_HOME.getValueAsString().trim();
        final boolean containsSpace = rmHome.contains(" ");
        if (containsSpace) {
            sb.append("\"");
        }
        sb.append(rmHome);
        sb.append(File.separator);
        sb.append("dist");
        sb.append(File.separator);
        sb.append("lib");
        sb.append(File.separator);
        sb.append("ProActive.jar");
        if (containsSpace) {
            sb.append("\"");
        }
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

    /**
     * Extracts process errput and returns it
     * @param p the remote process frow which one errput will be extracted.
     * @return the remote process' errput
     */
    static String extractProcessErrput(Process p) {
        BufferedReader br = new BufferedReader(new InputStreamReader(p.getErrorStream()));
        StringBuilder sb = new StringBuilder();
        String line = null;
        try {
            String lf = System.getProperty("line.separator");
            while (br.ready()) {
                if ((line = br.readLine()) != null) {
                    sb.append(line);
                    sb.append(lf);
                }
            }
        } catch (IOException e) {
            sb.append("Cannot extract process errput");
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                logger.debug("Cannot close process error stream", e);
            }
        }
        return sb.toString();
    }

    /**
     * Extracts process output and returns it
     * @param p the remote process frow which one output will be extracted.
     * @return the remote process' output
     */
    static String extractProcessOutput(Process p) {
        BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line = null;
        try {
            String lf = System.getProperty("line.separator");
            while (br.ready()) {
                if ((line = br.readLine()) != null) {
                    sb.append(line);
                    sb.append(lf);
                }
            }
        } catch (IOException e) {
            sb.append("Cannot extract process output");
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                logger.debug("Cannot close process output stream", e);
            }
        }
        return sb.toString();
    }

    /**
     * Consumes everything written by a stream. 
     */
    public static void consumeProcessStream(InputStream stream) {
        BufferedReader br = new BufferedReader(new InputStreamReader(stream));
        try {
            while (br.readLine() != null) {
                br.readLine();
            }
        } catch (IOException e) {
        } finally {
            try {
                br.close();
            } catch (IOException e) {
            }
        }
    }
}
