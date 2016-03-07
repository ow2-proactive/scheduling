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

import functionaltests.utils.SchedulerFunctionalTestWithRestart;
import functionaltests.utils.SchedulerTHelper;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;
import org.objectweb.proactive.utils.OperatingSystem;
import org.ow2.proactive.resourcemanager.common.NodeState;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.common.event.RMNodeEvent;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.job.factories.StaxJobFactory;
import org.ow2.proactive.scheduler.common.task.ForkEnvironment;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.dataspaces.OutputAccessMode;
import org.ow2.proactive.scheduler.common.task.executable.JavaExecutable;
import org.ow2.proactive.scheduler.util.FileLock;

import java.io.File;
import java.io.Serializable;
import java.net.URI;
import java.nio.file.Path;

import static org.junit.Assert.assertTrue;
import static org.ow2.proactive.utils.FileUtils.createTempDirectory;


public class TestForkedTaskWorkingDir extends SchedulerFunctionalTestWithRestart {

    @Test
    public void input_files_are_in_working_dir_for_forked_tasks() throws Throwable {
        scriptTask();
        nativeTask();
        javaTaskTaskRestartedAnotherNode();
    }

    private void scriptTask() throws Exception {
        File input = createTempDirectory("test", ".input_script", null);
        File output = createTempDirectory("test", ".output_script", null);

        FileUtils.touch(new File(input, "inputFile_script.txt"));

        TaskFlowJob job = (TaskFlowJob) StaxJobFactory.getFactory().createJob(
                new File(TestForkedTaskWorkingDir.class.getResource(
                        "/functionaltests/descriptors/Job_forked_script_task_working_dir.xml").toURI())
                        .getAbsolutePath());

        job.setInputSpace(input.toURI().toString());
        job.setOutputSpace(output.toURI().toString());

        schedulerHelper.testJobSubmission(job);

        assertTrue(new File(output, "outputFile_script.txt").exists());
    }

    private void nativeTask() throws Exception {
        if (OperatingSystem.getOperatingSystem() == OperatingSystem.unix) {

            File input = createTempDirectory("test", ".input_native", null);
            File output = createTempDirectory("test", ".output_native", null);

            FileUtils.touch(new File(input, "inputFile_native.txt"));

            TaskFlowJob job = (TaskFlowJob) StaxJobFactory.getFactory().createJob(
                    new File(TestForkedTaskWorkingDir.class.getResource(
                            "/functionaltests/descriptors/Job_forked_native_task_working_dir.xml").toURI())
                            .getAbsolutePath());

            job.setInputSpace(input.toURI().toString());
            job.setOutputSpace(output.toURI().toString());

            schedulerHelper.testJobSubmission(job);

            assertTrue(new File(output, "outputFile_native.txt").exists());
        }
    }

    /*
     * SCHEDULING-2129 Mapping for a given space URI is already registered
     * 
     * Run a task, kill the node,let it restart on another node and check the the shared scratch
     * space was correctly setup by transferring a file created in working dir from the task
     */
    private void javaTaskTaskRestartedAnotherNode() throws Exception {
        FileLock blockTaskFromTest = new FileLock();
        Path blockTaskFromTestPath = blockTaskFromTest.lock();

        FileLock blockTestBeforeKillingNode = new FileLock();
        Path blockTestBeforeKillingNodePath = blockTestBeforeKillingNode.lock();

        TaskFlowJob job =
                createFileInLocalSpaceJob(
                        blockTaskFromTestPath.toString(), blockTestBeforeKillingNodePath.toString());

        JobId idJ1 = schedulerHelper.submitJob(job);

        SchedulerTHelper.log("Wait until task is in the middle of the run");
        final String taskNodeUrl = findNodeRunningTask();
        schedulerHelper.waitForEventTaskRunning(idJ1, "task1");
        FileLock.waitUntilUnlocked(blockTestBeforeKillingNodePath);

        SchedulerTHelper.log("Kill the node running the task");
        schedulerHelper.killNode(taskNodeUrl);

        SchedulerTHelper.log("Let the task finish");
        blockTaskFromTest.unlock();

        SchedulerTHelper.log("Waiting for job 1 to finish");
        schedulerHelper.waitForEventJobFinished(idJ1);

        String userSpaceUri = URI.create(schedulerHelper.getSchedulerInterface().getUserSpaceURIs().get(0))
                .getPath();
        assertTrue("Could not find expected output file", new File(userSpaceUri, "output_file.txt").exists());
    }

    private TaskFlowJob createFileInLocalSpaceJob(String blockTaskFromTestUrl,
            String blockTestBeforeKillingNodeUrl) throws Exception {
        TaskFlowJob job = new TaskFlowJob();

        JavaTask task1 = new JavaTask();
        task1.setForkEnvironment(new ForkEnvironment());
        task1.setName("task1");
        task1.setExecutableClassName(CreateFileInLocalSpaceTask.class.getName());
        task1.addArgument("blockTaskFromTestUrl", blockTaskFromTestUrl);
        task1.addArgument("blockTestBeforeKillingNodeUrl", blockTestBeforeKillingNodeUrl);
        task1.addOutputFiles("output_file.txt", OutputAccessMode.TransferToUserSpace);

        job.addTask(task1);
        return job;
    }

    private String findNodeRunningTask() throws Exception {
        RMNodeEvent event;
        do {
            event = schedulerHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED, 30 * 1000);
        } while (!event.getNodeState().equals(NodeState.BUSY));
        return event.getNodeUrl();
    }

    public static class CreateFileInLocalSpaceTask extends JavaExecutable {

        private String blockTaskFromTestUrl;
        private String blockTestBeforeKillingNodeUrl;

        @Override
        public Serializable execute(TaskResult... results) throws Throwable {
            CentralPAPropertyRepository.PA_CLASSLOADING_USEHTTP.setValue(false);

            FileLock.unlock(blockTestBeforeKillingNodeUrl);
            // for the first execution, the node will be killed here
            FileLock.waitUntilUnlocked(blockTaskFromTestUrl);

            ProActiveRuntimeImpl.getProActiveRuntime().cleanJvmFromPA();

            return new File("output_file.txt").createNewFile();
        }
    }

}
