/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package functionaltests.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.sshd.common.util.OsUtils;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.utils.OperatingSystem;
import org.ow2.tests.ProcessKiller;


/**
 * @author ActiveEon Team
 * @since 03/07/17
 */
public class NodesRecoveryProcessHelper {

    public static void findRmPidAndSendSigKill(String javaProcessName)
            throws IOException, InterruptedException, ProcessNotFoundException {
        int pidToKill;
        OperatingSystem os = OperatingSystem.UNIX;
        if (System.getProperty("os.name").contains("Windows")) {
            os = OperatingSystem.WINDOWS;
        }
        switch (os) {
            case WINDOWS:
                pidToKill = getWindowsFirstJavaProcessPidWithName(javaProcessName);
                break;
            case UNIX:
                pidToKill = getUnixFirstJavaProcessPidWithName(javaProcessName);
                break;
            default:
                throw new IllegalStateException("Unsupported operating system");
        }

        // a process killer suitable for windows or unix operating systems
        ProcessKiller processKiller = ProcessKiller.get();
        processKiller.kill(pidToKill);
    }

    private static String buildJpsCommand() {
        OperatingSystem os;
        String jpsCommandPath;
        if (OsUtils.isWin32()) {
            os = OperatingSystem.WINDOWS;
            jpsCommandPath = "\"" + System.getProperty("java.home") + os.fs + ".." + os.fs + "bin" + os.fs +
                             "jps.exe\"";
        } else if (OsUtils.isUNIX()) {
            os = OperatingSystem.UNIX;
            jpsCommandPath = System.getProperty("java.home") + os.fs + ".." + os.fs + "bin" + os.fs + "jps";
        } else {
            throw new IllegalStateException("Operating system is not recognized");
        }
        return jpsCommandPath;
    }

    private static int getUnixFirstJavaProcessPidWithName(String processName)
            throws IOException, ProcessNotFoundException {
        String line;
        ProcessBuilder processBuilder = new ProcessBuilder("/bin/sh", "-c", buildJpsCommand());
        Process p = processBuilder.start();
        BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
        StringBuilder stringBuilder = new StringBuilder();
        while ((line = input.readLine()) != null) {
            stringBuilder.append(line).append(",");
            if (line.contains(processName)) {
                String pidString = line.split(" ")[0];
                input.close();
                return Integer.parseInt(pidString);
            }
        }
        throw new ProcessNotFoundException(processName, stringBuilder);
    }

    private static int getWindowsFirstJavaProcessPidWithName(String processName)
            throws IOException, ProcessNotFoundException {
        String line;
        Process p = Runtime.getRuntime().exec(buildJpsCommand());
        BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
        StringBuilder stringBuilder = new StringBuilder();
        while ((line = input.readLine()) != null) {
            stringBuilder.append(line).append(",");
            if (line.toLowerCase().contains(processName.toLowerCase())) {
                String pidString = line.split(" ")[0];
                return Integer.parseInt(pidString);
            }
        }
        throw new ProcessNotFoundException(processName, stringBuilder);
    }

    static private class ProcessNotFoundException extends Exception {

        ProcessNotFoundException(String processName, StringBuilder stringBuilder) {
            // build sentence in reverse manner
            super(stringBuilder.insert(0, " could not be found to be killed. Java processes found are: ")
                               .insert(0, processName)
                               .insert(0, "Java process with name: ")
                               .toString());
        }

    }

}
