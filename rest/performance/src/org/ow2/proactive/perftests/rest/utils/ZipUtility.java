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

import java.util.ArrayList;
import java.util.List;

import org.ow2.proactive.tests.performance.deployment.HostTestEnv;
import org.ow2.proactive.tests.performance.deployment.TestExecutionException;
import org.ow2.proactive.tests.performance.deployment.process.ProcessExecutor;

public class ZipUtility {

    public static void unzipFile(String zipFile, String outputDirectory,
            HostTestEnv env) throws InterruptedException {

        List<String> command = new ArrayList<String>();
        command.add(env.getEnv().getJavaPath());
        command.add("-cp");
        command.add(env.getEnv().getSchedulingFolder()
                .getPerformanceClassesDir().getAbsolutePath());
        command.add(ZipUtilityHelper.class.getName());
        command.add(zipFile);
        command.add(outputDirectory);
        ProcessExecutor executor = env.runCommandSaveOutput("unzipFile",
                command);
        if (!executor.executeAndWaitCompletion(10000, true)) {
            throw new TestExecutionException(
                    "Failed to execute command to unzipFile");
        }
        List<String> output = executor.getOutput();
        if (!output.isEmpty()) {
            throw new TestExecutionException(
                    String.format(
                            "An error has occurred while executing ZipUtilityHelper class at %s. Error output: %n%s",
                            env.getHost().getHostName(), output));
        }
    }

}
