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
package functionaltests.job.taskkill;

import static functionaltests.utils.SchedulerTHelper.log;
import io.github.pixee.security.BoundedLineReader;
import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import org.objectweb.proactive.utils.OperatingSystem;
import org.ow2.proactive.scheduler.common.exception.UserException;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.ForkEnvironment;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;


public class TestProcessTreeKillerUtil {

    public static final int detachedProcNumber = 4;

    private final static int wait_kill_time = 60000;

    private static final String unixSleepName = "sleep";

    private static final String windowsSleepName = "TestSleep.exe";

    static final int NB_ITERATIONS = 5;

    public static URL launchersDir = TestProcessTreeKillerUtil.class.getResource("/functionaltests/executables/TestSleep.exe");

    public static TaskFlowJob createJavaExecutableJob(String name, boolean forked) throws UserException {
        TaskFlowJob job = new TaskFlowJob();
        job.setName(name);
        job.setDescription("A command that spawns processes");

        JavaTask task = new JavaTask();
        if (forked) {
            task.setForkEnvironment(new ForkEnvironment());
        }
        task.addArgument("sleep", 3);
        task.addArgument("tname", name);
        task.addArgument("home", PASchedulerProperties.SCHEDULER_HOME.getValueAsString());
        task.setName(name);
        task.setExecutableClassName(JavaSpawnExecutable.class.getName());
        task.setPreciousLogs(true);
        job.addTask(task);
        return job;
    }

    /*
     * Process are killed asynchronously, need wait some time
     */
    public static void waitUntilForkedProcessesAreRunning(int expectedNumber) throws Exception {
        log("************** Waiting until " + expectedNumber + " processes are left *************");
        int runningDetachedProcNumber = 0;
        long stopTime = System.currentTimeMillis() + wait_kill_time;
        while (System.currentTimeMillis() < stopTime) {
            runningDetachedProcNumber = countProcesses();

            if (runningDetachedProcNumber == expectedNumber) {
                break;
            } else {
                Thread.sleep(500);
            }
        }
        assertEquals(expectedNumber, runningDetachedProcNumber);
        log("************** " + expectedNumber + " processes are now running *************");
    }

    public static void waitUntilAllForkedProcessesAreKilled() throws Exception {
        waitUntilForkedProcessesAreRunning(0);
    }

    /*
     * Process are killed asynchronously, need wait some time
     */
    public static int countProcesses() throws Exception {
        switch (OperatingSystem.getOperatingSystem()) {
            case windows:
                return getProcessNumberWindows(windowsSleepName);
            case unix:
                return getProcessNumber(unixSleepName);
            default:
                throw new IllegalStateException("Unsupported operating system");
        }
    }

    public static int getProcessNumber(String executableName) throws IOException {
        int toReturn = 0;
        String line;
        ProcessBuilder processBuilder = new ProcessBuilder("/bin/sh", "-c", "ps -f -u $(whoami)");
        processBuilder.redirectErrorStream();
        Process p = processBuilder.start();
        BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
        log("Scanning processes");
        while ((line = BoundedLineReader.readLine(input, 5_000_000)) != null) {
            log("Process: " + line);
            if (line.contains(executableName)) {
                toReturn++;
            }
        }
        input.close();
        return toReturn;
    }

    public static int getProcessNumberWindows(String executableName) throws IOException {
        int toReturn = 0;
        String line;
        Process p = Runtime.getRuntime().exec("tasklist");
        BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
        while ((line = BoundedLineReader.readLine(input, 5_000_000)) != null) {
            if (line.toLowerCase().contains(executableName.toLowerCase())) {
                toReturn++;
            }
        }
        input.close();
        return toReturn;
    }
}
