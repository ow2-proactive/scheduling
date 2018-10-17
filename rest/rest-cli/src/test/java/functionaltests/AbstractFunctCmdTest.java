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
package functionaltests;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.SchedulerState;
import org.ow2.proactive.scheduler.common.exception.NotConnectedException;
import org.ow2.proactive.scheduler.common.exception.PermissionException;
import org.ow2.proactive.scheduler.common.exception.UnknownJobException;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.JobStatus;
import org.ow2.proactive_grid_cloud_portal.scheduler.WorkflowSubmitter;

import jline.WindowsTerminal;


/**
 * Created by Sandrine on 18/09/2015.
 */
public class AbstractFunctCmdTest extends AbstractRestFuncTestCase {

    protected static Scheduler scheduler;

    protected StringBuffer userInput;

    protected ByteArrayOutputStream capturedOutput;

    protected PrintStream stdOut;

    public void setUp() throws Exception {
        this.userInput = new StringBuffer();
        typeLine("login('admin')");
        typeLine("admin");
    }

    protected JobId submitJob(String filename, JobStatus waitForStatus) throws Exception {
        File jobFile = new File(this.getClass().getResource("config/" + filename).toURI());
        WorkflowSubmitter submitter = new WorkflowSubmitter(scheduler);
        JobId id = submitter.submit(jobFile, new HashMap<String, String>(), null);
        waitJobState(id, waitForStatus, 500000);
        return id;
    }

    protected void runCli() {
        System.setProperty(WindowsTerminal.DIRECT_CONSOLE, "false"); // to be able to type input on Windows
        capturedOutput = new ByteArrayOutputStream();
        PrintStream captureOutput = new PrintStream(capturedOutput);
        stdOut = System.out;
        System.setOut(captureOutput);
        System.setIn(new ByteArrayInputStream(userInput.toString().getBytes()));
        new TestEntryPoint().runTest("-k", "-u", RestFuncTHelper.getRestServerUrl());
    }

    protected void typeLine(String line) {
        userInput.append(line);
        userInput.append("\n");
    }

    protected void clearAndTypeLine(String line) {
        userInput = new StringBuffer();
        userInput.append(line);
        userInput.append("\n");
    }

    protected void cleanScheduler() throws NotConnectedException, PermissionException, UnknownJobException {
        scheduler = RestFuncTHelper.getScheduler();
        SchedulerState state = scheduler.getState();
        System.out.println("Cleaning scheduler.");
        List<JobState> aliveJobsStates = new ArrayList<>(state.getPendingJobs().size() + state.getRunningJobs().size());
        aliveJobsStates.addAll(state.getPendingJobs());
        aliveJobsStates.addAll(state.getRunningJobs());
        List<JobState> finishedJobsStates = new ArrayList<>(state.getFinishedJobs().size());
        finishedJobsStates.addAll(state.getFinishedJobs());
        for (JobState jobState : aliveJobsStates) {
            JobId jobId = jobState.getId();
            try {
                System.out.println("Killing job " + jobId);
                scheduler.killJob(jobId);
            } catch (Exception ignored) {

            }
            System.out.println("Removing killed job " + jobId);
            scheduler.removeJob(jobId);
        }
        for (JobState jobState : finishedJobsStates) {
            JobId jobId = jobState.getId();
            System.out.println("Removing finished job " + jobId);
            scheduler.removeJob(jobId);
        }
    }
}
