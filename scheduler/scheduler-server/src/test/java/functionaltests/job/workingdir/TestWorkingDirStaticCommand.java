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
package functionaltests.job.workingdir;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.objectweb.proactive.utils.OperatingSystem;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.job.factories.StaxJobFactory;
import org.ow2.proactive.scheduler.common.task.NativeTask;

import functionaltests.utils.SchedulerFunctionalTestNoRestart;
import functionaltests.utils.SchedulerTHelper;


/**
 * Test whether attribute 'workingDir' set in native task element in a job descriptor
 * set properly the launching directory of the native executable (equivalent to linux PWD)
 *
 * @author The ProActive Team
 */
public class TestWorkingDirStaticCommand extends SchedulerFunctionalTestNoRestart {

    private static URL jobDescriptor = TestWorkingDirStaticCommand.class.getResource("/functionaltests/descriptors/Job_test_workingDir_static_command.xml");

    private static String executablePathPropertyName = "EXEC_PATH";

    private static URL executablePath = TestWorkingDirStaticCommand.class.getResource("/functionaltests/executables/test_working_dir.sh");

    private static URL executablePathWindows = TestWorkingDirStaticCommand.class.getResource("/functionaltests/executables/test_working_dir.bat");

    private static String WorkingDirPropertyName = "WDIR";

    private static URL workingDirPath = TestWorkingDirStaticCommand.class.getResource("/functionaltests/executables");

    @Test
    public void testWorkingDirStaticCommand() throws Throwable {

        String task1Name = "task1";

        JobId id = null;
        //set system Property for executable path
        switch (OperatingSystem.getOperatingSystem()) {
            case windows:
                System.setProperty(executablePathPropertyName,
                                   new File(executablePathWindows.toURI()).getAbsolutePath());
                System.setProperty(WorkingDirPropertyName, new File(workingDirPath.toURI()).getAbsolutePath());
                //test submission and event reception
                TaskFlowJob job = (TaskFlowJob) StaxJobFactory.getFactory()
                                                              .createJob(new File(jobDescriptor.toURI()).getAbsolutePath());
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

                ((NativeTask) job.getTask("task1")).setCommandLine(command.toArray(new String[command.size()]));
                id = schedulerHelper.testJobSubmission(job);
                break;
            case unix:
                System.setProperty(executablePathPropertyName, new File(executablePath.toURI()).getAbsolutePath());
                System.setProperty(WorkingDirPropertyName, new File(workingDirPath.toURI()).getAbsolutePath());
                SchedulerTHelper.setExecutable(new File(executablePath.toURI()).getAbsolutePath());
                //test submission and event reception
                id = schedulerHelper.testJobSubmission(new File(jobDescriptor.toURI()).getAbsolutePath());
                break;
            default:
                throw new IllegalStateException("Unsupported operating system");
        }

        //remove job
        schedulerHelper.removeJob(id);
        schedulerHelper.waitForEventJobRemoved(id);
    }
}
