/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2015 INRIA/University of
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
package org.ow2.proactive.process;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class SSHProcessExecutor extends ProcessExecutor {

    private static final String SSH_PATH = "ssh";

    public static ProcessExecutor createExecutorSaveOutput(String commandName, InetAddress host,
            String... command) {
        return createExecutorSaveOutput(commandName, host, Arrays.asList(command));
    }

    public static ProcessExecutor createExecutorSaveOutput(String commandName, InetAddress host,
            List<String> command) {
        return createExecutor(commandName, host, false, true, command);
    }

    public static ProcessExecutor createExecutorPrintOutput(String commandName, InetAddress host,
            String... command) {
        return createExecutorPrintOutput(commandName, host, Arrays.asList(command));
    }

    public static ProcessExecutor createExecutorPrintOutput(String commandName, InetAddress host,
            List<String> command) {
        return createExecutor(commandName, host, true, false, command);
    }

    private static ProcessExecutor createExecutor(String commandName, InetAddress host, boolean printOutput,
            boolean saveOutput, List<String> command) {
        if (host.isLoopbackAddress()) {
            ProcessExecutor executor = new ProcessExecutor(commandName, command, printOutput, saveOutput);
            return executor;
        } else {
            List<String> sshCommand = new ArrayList<>(command.size() + 4);
            sshCommand.add(SSH_PATH);
            sshCommand.add("-o");
            sshCommand.add("StrictHostKeyChecking no");
            sshCommand.add(host.getHostName());
            sshCommand.addAll(command);
            return new SSHProcessExecutor("'" + commandName + " on " + host + "'", sshCommand, printOutput,
                saveOutput);
        }
    }

    private SSHProcessExecutor(String commandName, List<String> command, boolean printOutput,
            boolean saveOutput) {
        super(commandName, command, printOutput, saveOutput);
    }

    public boolean isRunningRemotely() {
        return true;
    }

}
