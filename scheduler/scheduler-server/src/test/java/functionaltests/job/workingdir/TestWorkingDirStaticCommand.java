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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package functionaltests.job.workingdir;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.objectweb.proactive.utils.OperatingSystem;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.JobStatus;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.job.factories.StaxJobFactory;
import org.ow2.proactive.scheduler.common.task.NativeTask;
import org.ow2.proactive.scheduler.common.task.TaskInfo;
import org.ow2.proactive.scheduler.common.task.TaskStatus;
import org.junit.Assert;
import org.junit.Test;

import functionaltests.utils.SchedulerFunctionalTest;
import functionaltests.utils.SchedulerTHelper;


/**
 * Test whether attribute 'workingDir' set in native task element in a job descriptor
 * set properly the launching directory of the native executable (equivalent to linux PWD)
 *
 * @author The ProActive Team
 */
public class TestWorkingDirStaticCommand extends SchedulerFunctionalTest {

    private static URL jobDescriptor = TestWorkingDirStaticCommand.class
            .getResource("/functionaltests/descriptors/Job_test_workingDir_static_command.xml");

    private static String executablePathPropertyName = "EXEC_PATH";

    private static URL executablePath = TestWorkingDirStaticCommand.class
            .getResource("/functionaltests/executables/test_working_dir.sh");

    private static URL executablePathWindows = TestWorkingDirStaticCommand.class
            .getResource("/functionaltests/executables/test_working_dir.bat");

    private static String WorkingDirPropertyName = "WDIR";

    private static URL workingDirPath = TestWorkingDirStaticCommand.class
            .getResource("/functionaltests/executables");

    @Test
    public void run() throws Throwable {

        String task1Name = "task1";

        JobId id = null;
        //set system Property for executable path
        switch (OperatingSystem.getOperatingSystem()) {
            case windows:
                System.setProperty(executablePathPropertyName, new File(executablePathWindows.toURI())
                        .getAbsolutePath());
                System
                        .setProperty(WorkingDirPropertyName, new File(workingDirPath.toURI())
                                .getAbsolutePath());
                //test submission and event reception
                TaskFlowJob job = (TaskFlowJob) StaxJobFactory.getFactory().createJob(
                        new File(jobDescriptor.toURI()).getAbsolutePath());
                List<String> command = new ArrayList<>();
                command.add("cmd");
                command.add("/C");
                String[] tabCommand = ((NativeTask) job.getTask("task1")).getCommandLine();
                for (int i = 0; i < tabCommand.length; i++) {
                    if (i == 0)
                        command.add("\"\"" + tabCommand[i] + "\"");
                    else if (i == tabCommand.length - 1)
                        command.add("\"" + tabCommand[i] + "\"\"");
                    else
                        command.add("\"" + tabCommand[i] + "\"");
                }

                ((NativeTask) job.getTask("task1")).setCommandLine(command
                        .toArray(new String[command.size()]));
                id = schedulerHelper.submitJob(job);
                break;
            case unix:
                System.setProperty(executablePathPropertyName, new File(executablePath.toURI())
                        .getAbsolutePath());
                System
                        .setProperty(WorkingDirPropertyName, new File(workingDirPath.toURI())
                                .getAbsolutePath());
                SchedulerTHelper.setExecutable(new File(executablePath.toURI()).getAbsolutePath());
                //test submission and event reception
                id = schedulerHelper.submitJob(new File(jobDescriptor.toURI()).getAbsolutePath());
                break;
            default:
                throw new IllegalStateException("Unsupported operating system");
        }

        SchedulerTHelper.log("Job submitted, id " + id.toString());

        SchedulerTHelper.log("Waiting for jobSubmitted Event");
        JobState receivedState = schedulerHelper.waitForEventJobSubmitted(id);

        Assert.assertEquals(receivedState.getId(), id);

        SchedulerTHelper.log("Waiting for job running");
        JobInfo jInfo = schedulerHelper.waitForEventJobRunning(id);
        Assert.assertEquals(jInfo.getJobId(), id);
        Assert.assertEquals(JobStatus.RUNNING, jInfo.getStatus());

        schedulerHelper.waitForEventTaskRunning(id, task1Name);
        TaskInfo tInfo = schedulerHelper.waitForEventTaskFinished(id, task1Name);

        SchedulerTHelper.log(schedulerHelper.getSchedulerInterface().getTaskResult(id, "task1").getOutput()
                .getAllLogs(false));

        Assert.assertEquals(TaskStatus.FINISHED, tInfo.getStatus());

        schedulerHelper.waitForEventJobFinished(id);
        JobResult res = schedulerHelper.getJobResult(id);

        //check that there is one exception in results
        Assert.assertTrue(res.getExceptionResults().size() == 0);

        //remove job
        schedulerHelper.removeJob(id);
        schedulerHelper.waitForEventJobRemoved(id);
    }
}