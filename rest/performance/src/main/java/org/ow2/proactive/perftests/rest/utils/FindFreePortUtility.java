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
package org.ow2.proactive.perftests.rest.utils;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.ow2.proactive.process.ProcessExecutor;
import org.ow2.proactive.process.SSHProcessExecutor;
import org.ow2.proactive.tests.performance.deployment.TestExecutionException;


public class FindFreePortUtility {

    public static Integer[] findFreePorts(InetAddress host, String hostJavaPath, String hostClassPath,
            int requiredNumOfPorts) throws InterruptedException {

        List<String> command = new ArrayList<String>();
        command.add(hostJavaPath);
        command.add("-cp");
        command.add(hostClassPath);
        command.add(FindFreePortHelper.class.getName());
        command.add(Integer.toString(requiredNumOfPorts));
        ProcessExecutor executor = SSHProcessExecutor
                .createExecutorSaveOutput("findFreePorts", host, command);
        if (!executor.executeAndWaitCompletion(10000, true)) {
            throw new TestExecutionException("Failed to execute command to find free ports");
        }
        List<String> output = executor.getOutput();
        if (output.isEmpty()) {
            throw new TestExecutionException("Empty output for command finding free ports. Error output: " +
                executor.getErrorOutput());
        }
        List<Integer> freePorts = new ArrayList<Integer>();
        for (Iterator<String> i = output.iterator(); i.hasNext();) {
            try {
                Integer freePort = Integer.valueOf(i.next().trim());
                freePorts.add(freePort);
            } catch (NumberFormatException e) {
                throw new TestExecutionException("Invalid port was detected", e);
            }
        }
        return freePorts.toArray(new Integer[freePorts.size()]);
    }
}
