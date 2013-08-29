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
 * $ACTIVEEON_INITIAL_DEV$
 */
package org.ow2.proactive.tests.performance.deployment;

import java.io.File;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import org.ow2.proactive.process.ProcessExecutor;
import org.ow2.proactive.process.SSHProcessExecutor;


public class HostTestEnv {

    private final InetAddress host;

    private final TestEnv env;

    public HostTestEnv(String hostName, TestEnv env) throws Exception {
        this.host = env.validateEnv(hostName);
        this.env = env;
    }

    public HostTestEnv(String hostName, String path, String javaPath) throws Exception {
        this(hostName, new TestEnv(path, javaPath));
    }

    public InetAddress getHost() {
        return host;
    }

    public TestEnv getEnv() {
        return env;
    }

    public ProcessExecutor runCommandSaveOutput(String commandName, List<String> command) {
        return SSHProcessExecutor.createExecutorSaveOutput(commandName, host, command);
    }

    public ProcessExecutor runCommandPrintOutput(String commandName, List<String> command) {
        return SSHProcessExecutor.createExecutorPrintOutput(commandName, host, command);
    }

    public String copyFileFromLocalEnv(TestEnv localEnv, File localFile) throws Exception {
        String fileName = localEnv.convertFileNameForEnv(localFile, env);
        createFileInEnv(localFile, fileName);
        return fileName;
    }

    public void createFileInEnv(File localFile, String targetFileName) throws Exception {
        if (host.isLoopbackAddress()) {
            File targetFile = new File(targetFileName);
            if (!localFile.equals(targetFile)) {
                List<String> command = new ArrayList<String>();
                command.add("cp");
                command.add(localFile.getAbsolutePath());
                command.add(targetFileName);
                ProcessExecutor cp = new ProcessExecutor("cp", command, false, true);
                if (!cp.executeAndWaitCompletion(10000, true)) {
                    throw new TestExecutionException("Failed to copy file '" + localFile.getAbsolutePath() +
                        "' to the " + targetFileName);
                }
            }
        } else {
            List<String> command = new ArrayList<String>();
            command.add("rsync");
            command.add(localFile.getAbsolutePath());
            command.add(host.getHostName() + ":" + targetFileName);
            ProcessExecutor rsync = new ProcessExecutor("rsync", command, false, true);
            if (!rsync.executeAndWaitCompletion(10000, true)) {
                throw new TestExecutionException("Failed to copy file '" + localFile.getAbsolutePath() +
                    "' to the " + host + ":" + targetFileName);
            }
        }
    }
}
