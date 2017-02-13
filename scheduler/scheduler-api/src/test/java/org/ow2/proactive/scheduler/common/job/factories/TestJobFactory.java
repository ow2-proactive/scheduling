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
package org.ow2.proactive.scheduler.common.job.factories;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.ow2.proactive.scheduler.common.SchedulerConstants;
import org.ow2.proactive.scheduler.common.exception.JobCreationException;
import org.ow2.proactive.scheduler.common.job.JobPriority;
import org.ow2.proactive.scheduler.common.job.JobType;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.NativeTask;
import org.ow2.proactive.scheduler.common.task.OnTaskError;
import org.ow2.proactive.scheduler.common.task.RestartMode;
import org.ow2.proactive.scheduler.common.task.Task;
import org.ow2.proactive.scheduler.common.task.dataspaces.InputAccessMode;
import org.ow2.proactive.scheduler.common.task.dataspaces.OutputAccessMode;

import com.google.common.collect.ImmutableMap;


/**
 * This class will test the jobFactory.
 * It will parse job XML descriptors as exhaustively as possible to check that every features and insertions are managed.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9.1
 */
public class TestJobFactory {

    private static URL jobTaskFlowDescriptor = TestJobFactory.class.getResource("Job_TaskFlow.xml");

    private static URL jobMultiNodesDescriptor = TestJobFactory.class.getResource("Job_MultiNodes.xml");

    @Test
    public void testJobFactory() throws Throwable {
        //test default behavior as well (null is STAX)
        log("TEST jobFactory STAX");
        run_(null);
    }

    private void run_(String impl) throws Throwable {
        String scriptFolder = new File(getClass().getResource("scripts/").toURI()).getAbsolutePath();
        System.setProperty("jobName", "Job_TaskFlow");
        log("Test Job TASK_FLOW");
        TaskFlowJob tfJob = getJob(jobTaskFlowDescriptor, impl, scriptFolder);

        //Check job properties
        assertEquals("No paquit in its HostName.", tfJob.getDescription());
        assertEquals("Job_TaskFlow", tfJob.getName());
        assertEquals("My_project", tfJob.getProjectName());
        assertEquals(JobPriority.HIGH, tfJob.getPriority());
        assertEquals(OnTaskError.CANCEL_JOB, tfJob.getOnTaskErrorProperty().getValue());
        assertEquals(2, tfJob.getMaxNumberOfExecution());
        assertEquals(RestartMode.ELSEWHERE, tfJob.getRestartTaskOnError());
        assertEquals(JobType.TASKSFLOW, tfJob.getType());
        assertEquals(4, tfJob.getTasks().size());
        assertEquals("input/space", tfJob.getInputSpace());
        assertEquals("output/space", tfJob.getOutputSpace());

        //Check task 1 properties
        Task task1 = tfJob.getTask("task1");

        assertEquals("task1", task1.getName());
        assertEquals(OnTaskError.NONE, task1.getOnTaskErrorProperty().getValue()); // The job factory overwrites the task settings with the job settings if they are set to none.
        assertEquals(false, task1.isPreciousResult());
        assertEquals(RestartMode.ANYWHERE, task1.getRestartTaskOnError());
        assertEquals(1, task1.getMaxNumberOfExecution());
        assertEquals("Parallel Tasks - Task 1", task1.getDescription());
        assertEquals(2, task1.getSelectionScripts().size());
        assertEquals(false, task1.getSelectionScripts().get(0).isDynamic());
        Assert.assertTrue(task1.getSelectionScripts().get(0).getScript() != null);
        assertEquals(1, task1.getSelectionScripts().get(0).getParameters().length);
        assertEquals("paquit", task1.getSelectionScripts().get(0).getParameters()[0]);
        assertEquals(true, task1.getSelectionScripts().get(1).isDynamic());
        Assert.assertTrue(task1.getSelectionScripts().get(1).getScript() != null);
        assertEquals(2, task1.getSelectionScripts().get(1).getParameters().length);
        assertEquals("test1", task1.getSelectionScripts().get(1).getParameters()[0]);
        assertEquals("test2", task1.getSelectionScripts().get(1).getParameters()[1]);
        Assert.assertTrue(task1.getPreScript().getScript().contains("Beginning of Pre-Script"));
        Assert.assertTrue(task1.getPostScript()
                               .getScript()
                               .contains("Content is equals to " + scriptFolder + "/unset.js"));
        assertNull(task1.getPostScript().getParameters());
        Assert.assertTrue(task1.getCleaningScript().getScript().contains("Beginning of clean script"));
        assertNull(task1.getCleaningScript().getParameters());
        assertEquals(null, task1.getDependencesList());
        assertEquals(1, task1.getNumberOfNodesNeeded());
        assertEquals(12 * 1000, task1.getWallTime());
        assertEquals(true, task1.isWallTimeSet());
        assertEquals(0, task1.getGenericInformation().size());
        assertNull(task1.getInputFilesList());
        assertNull(task1.getOutputFilesList());
        assertEquals("1", ((JavaTask) task1).getArgument("sleepTime"));
        assertEquals("1", ((JavaTask) task1).getArgument("number"));
        assertEquals("org.ow2.proactive.scheduler.examples.WaitAndPrint", ((JavaTask) task1).getExecutableClassName());
        assertEquals(true, ((JavaTask) task1).isFork());
        assertEquals(null, task1.getForkEnvironment());

        //Check task 2 properties
        Task task2 = tfJob.getTask("task2");
        assertEquals("task2", task2.getName());
        assertEquals(OnTaskError.NONE, task2.getOnTaskErrorProperty().getValue());
        //the following commented check fails, it is what we expect, because replacement is done in the internal factory.
        //it avoids complex changes during user job creation process.
        //this property is tested in the TestDatabaseCRUD
        //Assert.assertEquals(tfJob.getTask("task2").isCancelJobOnError(),true);
        assertEquals(false, task2.isPreciousResult());
        assertEquals(RestartMode.ELSEWHERE, task2.getRestartTaskOnError());
        assertEquals(2, task2.getMaxNumberOfExecution());
        assertEquals("Parallel Tasks - Task 2", task2.getDescription());
        assertEquals(null, task2.getSelectionScripts());
        Assert.assertTrue(task2.getPreScript().getScript().contains("Beginning of Pre-Script"));
        assertEquals(null, task2.getPostScript());
        assertEquals(null, task2.getCleaningScript());
        assertEquals(null, task2.getDependencesList());
        assertEquals(1, task2.getNumberOfNodesNeeded());
        assertEquals(0, task2.getWallTime());
        assertEquals(false, task2.isWallTimeSet());
        assertEquals(0, task2.getGenericInformation().size());
        Assert.assertTrue(task2.getInputFilesList().get(0).getInputFiles().getIncludes().contains("tata*"));
        Assert.assertTrue(task2.getInputFilesList().get(0).getInputFiles().getExcludes().contains("tata*1"));
        assertEquals(InputAccessMode.TransferFromInputSpace, task2.getInputFilesList().get(0).getMode());
        Assert.assertTrue(task2.getInputFilesList().get(1).getInputFiles().getIncludes().contains("toto*.txt"));
        Assert.assertTrue(task2.getInputFilesList().get(1).getInputFiles().getExcludes().contains("toto*2.txt"));
        assertEquals(InputAccessMode.TransferFromOutputSpace, task2.getInputFilesList().get(1).getMode());
        Assert.assertTrue(task2.getOutputFilesList().get(0).getOutputFiles().getIncludes().contains("titi*"));
        Assert.assertTrue(task2.getOutputFilesList().get(0).getOutputFiles().getExcludes().contains("titi*1"));
        assertEquals(OutputAccessMode.TransferToOutputSpace, task2.getOutputFilesList().get(0).getMode());
        Assert.assertTrue(task2.getOutputFilesList().get(1).getOutputFiles().getIncludes().contains("titi*.txt"));
        Assert.assertTrue(task2.getOutputFilesList().get(1).getOutputFiles().getExcludes().contains("titi*3.txt"));
        assertEquals(OutputAccessMode.TransferToOutputSpace, task2.getOutputFilesList().get(1).getMode());
        assertEquals("12", ((JavaTask) task2).getArgument("sleepTime"));
        assertEquals("21", ((JavaTask) task2).getArgument("number"));
        assertEquals("/bin/java/jdk1.5", ((JavaTask) task2).getArgument("test"));
        assertEquals("org.ow2.proactive.scheduler.examples.WaitAndPrint", ((JavaTask) task2).getExecutableClassName());
        assertEquals(true, ((JavaTask) task2).isFork());
        assertEquals(false, task2.isWallTimeSet());
        assertEquals("/bin/java/jdk1.5", task2.getForkEnvironment().getJavaHome());
        assertEquals("/bin/java/jdk1.5/toto", task2.getForkEnvironment().getWorkingDir());
        assertEquals("-dparam=12", task2.getForkEnvironment().getJVMArguments().get(0));
        assertEquals("-djhome=/bin/java/jdk1.5", task2.getForkEnvironment().getJVMArguments().get(1));
        Map<String, String> props = task2.getForkEnvironment().getSystemEnvironment();
        assertEquals(2, props.size());
        assertEquals("ioi", props.get("toto"));
        assertEquals("456", props.get("tata"));

        assertEquals("ioi", task2.getForkEnvironment().getSystemEnvironmentVariable("toto"));
        assertEquals("456", task2.getForkEnvironment().getSystemEnvironmentVariable("tata"));
        List<String> addcp = task2.getForkEnvironment().getAdditionalClasspath();
        assertEquals(2, addcp.size());
        assertEquals("a", addcp.get(0));
        assertEquals("b", addcp.get(1));
        Assert.assertNotNull(task2.getForkEnvironment().getEnvScript());

        //Check task 3 properties
        Task task3 = tfJob.getTask("task3");
        assertEquals("task3", task3.getName());
        assertEquals(OnTaskError.NONE, task3.getOnTaskErrorProperty().getValue());
        //the following commented check fails, it is what we expect, because replacement is done in the
        // internal factory.
        //it avoids complex changes during user job creation process.
        //this property is tested in the TestDatabaseCRUD
        //Assert.assertEquals(tfJob.getTask("task3").isCancelJobOnError(),true);
        assertEquals(false, task3.isPreciousResult());
        //the following commented check fails, it is what we expect, because replacement is done in the
        // internal factory.
        //it avoids complex changes during user job creation process.
        //this property is tested in the TestDatabaseCRUD
        //Assert.assertEquals(tfJob.getTask("task3").getRestartTaskOnError(),RestartMode.ELSEWHERE);
        //the following commented check fails, it is what we expect, because replacement is done in the internal factory.
        //it avoids complex changes during user job creation process.
        //this property is tested in the TestDatabaseCRUD
        //Assert.assertEquals(tfJob.getTask("task3").getMaxNumberOfExecution(),2);
        assertEquals("Dependent Tasks - Task 3", task3.getDescription());
        assertEquals(null, task3.getSelectionScripts());
        assertEquals(null, task3.getPreScript());
        Assert.assertTrue(task3.getPostScript().getScript().contains("Unsetting system property user.property1"));
        assertNull(task3.getPostScript().getParameters());
        assertEquals(null, task3.getCleaningScript());
        assertEquals(2, task3.getDependencesList().size());
        assertEquals("task1", task3.getDependencesList().get(0).getName());
        assertEquals("task2", task3.getDependencesList().get(1).getName());
        assertEquals(10 * 60 * 1000 + 53 * 1000, task3.getWallTime());
        assertEquals(true, task3.isWallTimeSet());
        assertEquals(0, task3.getGenericInformation().size());
        assertEquals(1, task3.getInputFilesList().size());
        Assert.assertTrue(task3.getInputFilesList().get(0).getInputFiles().getIncludes().contains("tata*"));
        Assert.assertTrue(task3.getInputFilesList().get(0).getInputFiles().getExcludes().isEmpty());
        assertEquals(InputAccessMode.none, task3.getInputFilesList().get(0).getMode());
        assertNull(task3.getOutputFilesList());
        assertEquals(5, ((NativeTask) task3).getCommandLine().length);
        assertEquals("1", ((NativeTask) task3).getCommandLine()[1]);
        assertEquals("2 2", ((NativeTask) task3).getCommandLine()[2]);
        assertEquals("3", ((NativeTask) task3).getCommandLine()[3]);
        assertEquals("12", ((NativeTask) task3).getCommandLine()[4]);
        assertNull(((NativeTask) task3).getWorkingDir());
        assertEquals(3, task3.getNumberOfNodesNeeded());

        //Check task 4 properties
        Task task4 = tfJob.getTask("task4");
        assertEquals("task4", task4.getName());
        assertEquals(OnTaskError.CANCEL_JOB, task4.getOnTaskErrorProperty().getValue());
        assertEquals(true, task4.isPreciousResult());
        assertEquals(RestartMode.ANYWHERE, task4.getRestartTaskOnError());
        assertEquals(3, task4.getMaxNumberOfExecution());
        assertEquals(null, task4.getDescription());
        assertEquals(null, task4.getSelectionScripts());
        assertEquals(null, task4.getPreScript());
        assertEquals(null, task4.getPostScript());
        assertEquals(null, task4.getCleaningScript());
        assertEquals(1, task4.getDependencesList().size());
        assertEquals("task3", task4.getDependencesList().get(0).getName());
        assertEquals(0, task4.getWallTime());
        assertEquals(false, task4.isWallTimeSet());
        assertEquals("v11", task4.getGenericInformation().get("n11"));
        assertEquals("v22", task4.getGenericInformation().get("n22"));
        assertNull(task4.getInputFilesList());
        assertEquals(5, task4.getOutputFilesList().size());
        Assert.assertTrue(task4.getOutputFilesList().get(0).getOutputFiles().getIncludes().contains("a"));
        Assert.assertTrue(task4.getOutputFilesList().get(1).getOutputFiles().getIncludes().contains("b"));
        Assert.assertTrue(task4.getOutputFilesList().get(2).getOutputFiles().getIncludes().contains("c"));
        Assert.assertTrue(task4.getOutputFilesList().get(3).getOutputFiles().getIncludes().contains("d"));
        Assert.assertTrue(task4.getOutputFilesList().get(4).getOutputFiles().getIncludes().contains("e"));
        Assert.assertTrue(task4.getOutputFilesList().get(0).getOutputFiles().getExcludes().contains("f"));
        Assert.assertTrue(task4.getOutputFilesList().get(1).getOutputFiles().getExcludes().contains("g"));
        Assert.assertTrue(task4.getOutputFilesList().get(2).getOutputFiles().getExcludes().isEmpty());
        Assert.assertTrue(task4.getOutputFilesList().get(3).getOutputFiles().getExcludes().contains("h"));
        Assert.assertTrue(task4.getOutputFilesList().get(4).getOutputFiles().getExcludes().contains("i"));
        assertEquals(OutputAccessMode.TransferToOutputSpace, task4.getOutputFilesList().get(0).getMode());
        assertEquals(OutputAccessMode.none, task4.getOutputFilesList().get(1).getMode());
        assertEquals(OutputAccessMode.none, task4.getOutputFilesList().get(2).getMode());
        assertEquals(OutputAccessMode.TransferToOutputSpace, task4.getOutputFilesList().get(3).getMode());
        assertEquals(OutputAccessMode.none, task4.getOutputFilesList().get(4).getMode());
        assertEquals(null, ((NativeTask) task4).getWorkingDir());
        assertEquals(10, task4.getNumberOfNodesNeeded());

        log("Test Job MULTI_NODES");
        TaskFlowJob mnJob = getJob(jobMultiNodesDescriptor, impl, scriptFolder);
        //Check job properties
        assertEquals("No description", mnJob.getDescription());
        assertEquals("job_multiNodes", mnJob.getName());
        assertEquals(JobPriority.LOW, mnJob.getPriority());
        assertEquals(OnTaskError.NONE, mnJob.getOnTaskErrorProperty().getValue());
        assertEquals(1, mnJob.getMaxNumberOfExecution());
        assertEquals(RestartMode.ANYWHERE, mnJob.getRestartTaskOnError());
        assertEquals(JobType.TASKSFLOW, mnJob.getType());
        assertEquals("v1", mnJob.getGenericInformation().get("n1"));
        assertEquals("v2", mnJob.getGenericInformation().get("n2"));
        //Check task properties
        JavaTask jt = (JavaTask) mnJob.getTask("Controller");
        assertEquals("100", jt.getArgument("numberToFind"));
        assertEquals(OnTaskError.NONE, jt.getOnTaskErrorProperty().getValue());
        assertEquals(null, jt.getCleaningScript());
        assertEquals(null, jt.getDependencesList());
        assertEquals("Will control the workers in order to find the prime number", jt.getDescription());
        assertEquals("org.ow2.proactive.scheduler.examples.MultiNodeExample", jt.getExecutableClassName());
        assertEquals("v11", jt.getGenericInformation().get("n11"));
        assertEquals("v22", jt.getGenericInformation().get("n22"));
        assertEquals(1, jt.getMaxNumberOfExecution());
        assertEquals("Controller", jt.getName());
        assertEquals(3, jt.getNumberOfNodesNeeded());
        assertEquals(null, jt.getPreScript());
        assertEquals(null, jt.getPostScript());
        assertEquals(RestartMode.ANYWHERE, jt.getRestartTaskOnError());
        assertEquals(null, jt.getSelectionScripts());
        Assert.assertTrue(jt.isPreciousResult());

        log("Test generated task name");
        TaskFlowJob job = new TaskFlowJob();
        for (int i = 0; i < 4; i++) {
            Task t = new NativeTask();
            job.addTask(t);
        }
        Task t1, t2, t3, t4;
        assertNull(job.getTask(SchedulerConstants.TASK_NAME_IFNOTSET + "0"));
        Assert.assertNotNull(t1 = job.getTask(SchedulerConstants.TASK_NAME_IFNOTSET + "1"));
        Assert.assertNotNull(t2 = job.getTask(SchedulerConstants.TASK_NAME_IFNOTSET + "2"));
        Assert.assertNotNull(t3 = job.getTask(SchedulerConstants.TASK_NAME_IFNOTSET + "3"));
        Assert.assertNotNull(t4 = job.getTask(SchedulerConstants.TASK_NAME_IFNOTSET + "4"));
        assertNull(job.getTask(SchedulerConstants.TASK_NAME_IFNOTSET + "5"));
        Assert.assertTrue(t1 != t2);
        Assert.assertTrue(t1 != t3);
        Assert.assertTrue(t1 != t4);
        Assert.assertTrue(t2 != t3);
        Assert.assertTrue(t2 != t4);
        Assert.assertTrue(t3 != t4);
    }

    private void log(String s) {
        System.out.println("------------------------------ " + s);
    }

    private TaskFlowJob getJob(URL jobDesc, String jobFacImpl, String scriptFolder)
            throws JobCreationException, URISyntaxException {
        return ScriptUpdateUtil.resolveScripts((TaskFlowJob) JobFactory.getFactory(jobFacImpl)
                                                                       .createJob(new File(jobDesc.toURI()).getAbsolutePath(),
                                                                                  ImmutableMap.of("scripts.folder",
                                                                                                  scriptFolder,
                                                                                                  "priority",
                                                                                                  "high",
                                                                                                  "nb.execution",
                                                                                                  "2")));
    }

}
