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
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.common.job.factories;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
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


/**
 * This class will test the jobFactory.
 * It will parse job XML descriptors as exhaustively as possible to check that every features and insertions are managed.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9.1
 */
public class TestJobFactory {

    private static URL jobTaskFlowDescriptor = TestJobFactory.class
            .getResource("Job_TaskFlow.xml");
    private static URL jobMultiNodesDescriptor = TestJobFactory.class
            .getResource("Job_MultiNodes.xml");

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
        assertEquals(tfJob.getDescription(), "No paquit in its HostName.");
        assertEquals(tfJob.getName(), "Job_TaskFlow");
        assertEquals(tfJob.getProjectName(), "My_project");
        assertEquals(tfJob.getPriority(), JobPriority.NORMAL);
        assertEquals(tfJob.getOnTaskErrorProperty().getValue(), OnTaskError.CANCEL_JOB);
        assertEquals(tfJob.getMaxNumberOfExecution(), 2);
        assertEquals(tfJob.getRestartTaskOnError(), RestartMode.ELSEWHERE);
        assertEquals(tfJob.getType(), JobType.TASKSFLOW);
        assertEquals(tfJob.getTasks().size(), 4);
        assertEquals("input/space", tfJob.getInputSpace());
        assertEquals("output/space", tfJob.getOutputSpace());
        //Check task 1 properties
        assertEquals(tfJob.getTask("task1").getName(), "task1");
        assertEquals(tfJob.getTask("task1").getOnTaskErrorProperty().getValue(), OnTaskError.CANCEL_JOB); // The job factory overwrites the task settings with the job settings if they are set to none.
        assertEquals(tfJob.getTask("task1").isPreciousResult(), false);
        assertEquals(tfJob.getTask("task1").getRestartTaskOnError(), RestartMode.ANYWHERE);
        assertEquals(tfJob.getTask("task1").getMaxNumberOfExecution(), 1);
        assertEquals(tfJob.getTask("task1").getDescription(), "Parallel Tasks - Task 1");
        assertEquals(2, tfJob.getTask("task1").getSelectionScripts().size());
        assertEquals(false, tfJob.getTask("task1").getSelectionScripts().get(0).isDynamic());
        Assert.assertTrue(tfJob.getTask("task1").getSelectionScripts().get(0).getScript() != null);
        assertEquals(1, tfJob.getTask("task1").getSelectionScripts().get(0).getParameters().length);
        assertEquals("paquit", tfJob.getTask("task1").getSelectionScripts().get(0).getParameters()[0]);
        assertEquals(true, tfJob.getTask("task1").getSelectionScripts().get(1).isDynamic());
        Assert.assertTrue(tfJob.getTask("task1").getSelectionScripts().get(1).getScript() != null);
        assertEquals(2, tfJob.getTask("task1").getSelectionScripts().get(1).getParameters().length);
        assertEquals("test1", tfJob.getTask("task1").getSelectionScripts().get(1).getParameters()[0]);
        assertEquals("test2", tfJob.getTask("task1").getSelectionScripts().get(1).getParameters()[1]);
        Assert.assertTrue(tfJob.getTask("task1").getPreScript().getScript().contains(
                "Beginning of Pre-Script"));
        Assert.assertTrue(tfJob.getTask("task1").getPostScript().getScript().contains(
                "Content is equals to " + scriptFolder + "/unset.js"));
        assertNull(tfJob.getTask("task1").getPostScript().getParameters());
        Assert.assertTrue(tfJob.getTask("task1").getCleaningScript().getScript().contains(
                "Beginning of clean script"));
        assertNull(tfJob.getTask("task1").getCleaningScript().getParameters());
        assertEquals(tfJob.getTask("task1").getDependencesList(), null);
        assertEquals(tfJob.getTask("task1").getNumberOfNodesNeeded(), 1);
        assertEquals(tfJob.getTask("task1").getWallTime(), 12 * 1000);
        assertEquals(tfJob.getTask("task1").isWallTimeSet(), true);
        assertEquals(tfJob.getTask("task1").getGenericInformation().size(), 0);
        assertNull(tfJob.getTask("task1").getInputFilesList());
        assertNull(tfJob.getTask("task1").getOutputFilesList());
        assertEquals(((JavaTask) tfJob.getTask("task1")).getArgument("sleepTime"), "1");
        assertEquals(((JavaTask) tfJob.getTask("task1")).getArgument("number"), "1");
        assertEquals(((JavaTask) tfJob.getTask("task1")).getExecutableClassName(),
          "org.ow2.proactive.scheduler.examples.WaitAndPrint");
        assertEquals(((JavaTask) tfJob.getTask("task1")).isFork(), true);
        assertEquals((tfJob.getTask("task1")).getForkEnvironment(), null);
        //Check task 2 properties
        assertEquals(tfJob.getTask("task2").getName(), "task2");
        //the following commented check fails, it is what we expect, because replacement is done in the internal factory.
        //it avoids complex changes during user job creation process.
        //this property is tested in the TestDatabaseCRUD
        //Assert.assertEquals(tfJob.getTask("task2").isCancelJobOnError(),true);
        assertEquals(tfJob.getTask("task2").isPreciousResult(), false);
        assertEquals(tfJob.getTask("task2").getRestartTaskOnError(), RestartMode.ELSEWHERE);
        assertEquals(tfJob.getTask("task2").getMaxNumberOfExecution(), 2);
        assertEquals(tfJob.getTask("task2").getDescription(), "Parallel Tasks - Task 2");
        assertEquals(tfJob.getTask("task2").getSelectionScripts(), null);
        Assert.assertTrue(tfJob.getTask("task2").getPreScript().getScript().contains(
                "Beginning of Pre-Script"));
        assertEquals(tfJob.getTask("task2").getPostScript(), null);
        assertEquals(tfJob.getTask("task2").getCleaningScript(), null);
        assertEquals(tfJob.getTask("task2").getDependencesList(), null);
        assertEquals(tfJob.getTask("task2").getNumberOfNodesNeeded(), 1);
        assertEquals(tfJob.getTask("task2").getWallTime(), 0);
        assertEquals(tfJob.getTask("task2").isWallTimeSet(), false);
        assertEquals(tfJob.getTask("task2").getGenericInformation().size(), 0);
        Assert.assertTrue(tfJob.getTask("task2").getInputFilesList().get(0).getInputFiles()
                .getIncludes().contains("tata*"));
        Assert.assertTrue(tfJob.getTask("task2").getInputFilesList().get(0).getInputFiles()
                .getExcludes().contains("tata*1"));
        assertEquals(InputAccessMode.TransferFromInputSpace,
          tfJob.getTask("task2").getInputFilesList().get(0).getMode());
        Assert.assertTrue(tfJob.getTask("task2").getInputFilesList().get(1).getInputFiles()
                .getIncludes().contains("toto*.txt"));
        Assert.assertTrue(tfJob.getTask("task2").getInputFilesList().get(1).getInputFiles()
                .getExcludes().contains("toto*2.txt"));
        assertEquals(InputAccessMode.TransferFromOutputSpace,
          tfJob.getTask("task2").getInputFilesList().get(1).getMode());
        Assert.assertTrue(tfJob.getTask("task2").getOutputFilesList().get(0).getOutputFiles()
                .getIncludes().contains("titi*"));
        Assert.assertTrue(tfJob.getTask("task2").getOutputFilesList().get(0).getOutputFiles()
                .getExcludes().contains("titi*1"));
        assertEquals(OutputAccessMode.TransferToOutputSpace,
          tfJob.getTask("task2").getOutputFilesList().get(0).getMode());
        Assert.assertTrue(tfJob.getTask("task2").getOutputFilesList().get(1).getOutputFiles()
                .getIncludes().contains("titi*.txt"));
        Assert.assertTrue(tfJob.getTask("task2").getOutputFilesList().get(1).getOutputFiles()
                .getExcludes().contains("titi*3.txt"));
        assertEquals(OutputAccessMode.TransferToOutputSpace,
          tfJob.getTask("task2").getOutputFilesList().get(1).getMode());
        assertEquals(((JavaTask) tfJob.getTask("task2")).getArgument("sleepTime"), "12");
        assertEquals(((JavaTask) tfJob.getTask("task2")).getArgument("number"), "21");
        assertEquals(((JavaTask) tfJob.getTask("task2")).getArgument("test"), "/bin/java/jdk1.5");
        assertEquals(((JavaTask) tfJob.getTask("task2")).getExecutableClassName(),
          "org.ow2.proactive.scheduler.examples.WaitAndPrint");
        assertEquals(((JavaTask) tfJob.getTask("task2")).isFork(), true);
        assertEquals((tfJob.getTask("task2")).isWallTimeSet(), false);
        assertEquals((tfJob.getTask("task2")).getForkEnvironment().getJavaHome(), "/bin/java/jdk1.5");
        assertEquals((tfJob.getTask("task2")).getForkEnvironment().getWorkingDir(), "/bin/java/jdk1.5/toto");
        assertEquals((tfJob.getTask("task2")).getForkEnvironment().getJVMArguments().get(0), "-dparam=12");
        assertEquals((tfJob.getTask("task2")).getForkEnvironment().getJVMArguments().get(1),
          "-djhome=/bin/java/jdk1.5");
        Map<String, String> props = ( tfJob.getTask("task2")).getForkEnvironment()
                .getSystemEnvironment();
        assertEquals(2, props.size());
        assertEquals("ioi", props.get("toto"));
        assertEquals("456", props.get("tata"));

        assertEquals("ioi",
          (tfJob.getTask("task2")).getForkEnvironment().getSystemEnvironmentVariable("toto"));
        assertEquals("456",
          (tfJob.getTask("task2")).getForkEnvironment().getSystemEnvironmentVariable("tata"));
        List<String> addcp = (tfJob.getTask("task2")).getForkEnvironment()
                .getAdditionalClasspath();
        assertEquals(2, addcp.size());
        assertEquals("a", addcp.get(0));
        assertEquals("b", addcp.get(1));
        Assert.assertNotNull((tfJob.getTask("task2")).getForkEnvironment().getEnvScript());
        //Check task 3 properties
        assertEquals(tfJob.getTask("task3").getName(), "task3");
        //the following commented check fails, it is what we expect, because replacement is done in the
        // internal factory.
        //it avoids complex changes during user job creation process.
        //this property is tested in the TestDatabaseCRUD
        //Assert.assertEquals(tfJob.getTask("task3").isCancelJobOnError(),true);
        assertEquals(tfJob.getTask("task3").isPreciousResult(), false);
        //the following commented check fails, it is what we expect, because replacement is done in the
        // internal factory.
        //it avoids complex changes during user job creation process.
        //this property is tested in the TestDatabaseCRUD
        //Assert.assertEquals(tfJob.getTask("task3").getRestartTaskOnError(),RestartMode.ELSEWHERE);
        //the following commented check fails, it is what we expect, because replacement is done in the internal factory.
        //it avoids complex changes during user job creation process.
        //this property is tested in the TestDatabaseCRUD
        //Assert.assertEquals(tfJob.getTask("task3").getMaxNumberOfExecution(),2);
        assertEquals(tfJob.getTask("task3").getDescription(), "Dependent Tasks - Task 3");
        assertEquals(tfJob.getTask("task3").getSelectionScripts(), null);
        assertEquals(tfJob.getTask("task3").getPreScript(), null);
        Assert.assertTrue(tfJob.getTask("task3").getPostScript().getScript().contains(
                "Unsetting system property user.property1"));
        assertEquals(tfJob.getTask("task3").getPostScript().getParameters(), null);
        assertEquals(tfJob.getTask("task3").getCleaningScript(), null);
        assertEquals(tfJob.getTask("task3").getDependencesList().size(), 2);
        assertEquals(tfJob.getTask("task3").getDependencesList().get(0).getName(), "task1");
        assertEquals(tfJob.getTask("task3").getDependencesList().get(1).getName(), "task2");
        assertEquals(tfJob.getTask("task3").getWallTime(), 10 * 60 * 1000 + 53 * 1000);
        assertEquals(tfJob.getTask("task3").isWallTimeSet(), true);
        assertEquals(tfJob.getTask("task3").getGenericInformation().size(), 0);
        assertEquals(1, tfJob.getTask("task3").getInputFilesList().size());
        Assert.assertTrue(tfJob.getTask("task3").getInputFilesList().get(0).getInputFiles()
                .getIncludes().contains("tata*"));
        Assert.assertTrue(
                tfJob.getTask("task3").getInputFilesList().get(0).getInputFiles().getExcludes().isEmpty());
        assertEquals(InputAccessMode.none, tfJob.getTask("task3").getInputFilesList().get(0).getMode());
        assertNull(tfJob.getTask("task3").getOutputFilesList());
        assertEquals(((NativeTask) tfJob.getTask("task3")).getCommandLine().length, 5);
        assertEquals(((NativeTask) tfJob.getTask("task3")).getCommandLine()[1], "1");
        assertEquals(((NativeTask) tfJob.getTask("task3")).getCommandLine()[2], "2 2");
        assertEquals(((NativeTask) tfJob.getTask("task3")).getCommandLine()[3], "3");
        assertEquals(((NativeTask) tfJob.getTask("task3")).getCommandLine()[4], "12");
        assertNull(((NativeTask) tfJob.getTask("task3")).getWorkingDir());
        assertEquals((tfJob.getTask("task3")).getNumberOfNodesNeeded(), 3);
        //Check task 4 properties
        assertEquals(tfJob.getTask("task4").getName(), "task4");
        assertEquals(tfJob.getTask("task4").getOnTaskErrorProperty().getValue().toString(), OnTaskError.CANCEL_JOB.toString());
        assertEquals(tfJob.getTask("task4").isPreciousResult(), true);
        assertEquals(tfJob.getTask("task4").getRestartTaskOnError(), RestartMode.ANYWHERE);
        assertEquals(tfJob.getTask("task4").getMaxNumberOfExecution(), 3);
        assertEquals(tfJob.getTask("task4").getDescription(), null);
        assertEquals(tfJob.getTask("task4").getSelectionScripts(), null);
        assertEquals(tfJob.getTask("task4").getPreScript(), null);
        assertEquals(tfJob.getTask("task4").getPostScript(), null);
        assertEquals(tfJob.getTask("task4").getCleaningScript(), null);
        assertEquals(tfJob.getTask("task4").getDependencesList().size(), 1);
        assertEquals(tfJob.getTask("task4").getDependencesList().get(0).getName(), "task3");
        assertEquals(tfJob.getTask("task4").getWallTime(), 0);
        assertEquals(tfJob.getTask("task4").isWallTimeSet(), false);
        assertEquals(tfJob.getTask("task4").getGenericInformation().get("n11"), "v11");
        assertEquals(tfJob.getTask("task4").getGenericInformation().get("n22"), "v22");
        assertNull(tfJob.getTask("task4").getInputFilesList());
        assertEquals(5, tfJob.getTask("task4").getOutputFilesList().size());
        Assert.assertTrue(tfJob.getTask("task4").getOutputFilesList().get(0).getOutputFiles()
                .getIncludes().contains("a"));
        Assert.assertTrue(tfJob.getTask("task4").getOutputFilesList().get(1).getOutputFiles()
                .getIncludes().contains("b"));
        Assert.assertTrue(tfJob.getTask("task4").getOutputFilesList().get(2).getOutputFiles()
                .getIncludes().contains("c"));
        Assert.assertTrue(tfJob.getTask("task4").getOutputFilesList().get(3).getOutputFiles()
                .getIncludes().contains("d"));
        Assert.assertTrue(tfJob.getTask("task4").getOutputFilesList().get(4).getOutputFiles()
                .getIncludes().contains("e"));
        Assert.assertTrue(tfJob.getTask("task4").getOutputFilesList().get(0).getOutputFiles()
                .getExcludes().contains("f"));
        Assert.assertTrue(tfJob.getTask("task4").getOutputFilesList().get(1).getOutputFiles()
                .getExcludes().contains("g"));
        Assert.assertTrue(tfJob.getTask("task4").getOutputFilesList().get(2).getOutputFiles().getExcludes().isEmpty());
        Assert.assertTrue(tfJob.getTask("task4").getOutputFilesList().get(3).getOutputFiles()
                .getExcludes().contains("h"));
        Assert.assertTrue(tfJob.getTask("task4").getOutputFilesList().get(4).getOutputFiles()
                .getExcludes().contains("i"));
        assertEquals(OutputAccessMode.TransferToOutputSpace,
          tfJob.getTask("task4").getOutputFilesList().get(0).getMode());
        assertEquals(OutputAccessMode.none, tfJob.getTask("task4").getOutputFilesList().get(1).getMode());
        assertEquals(OutputAccessMode.none, tfJob.getTask("task4").getOutputFilesList().get(2).getMode());
        assertEquals(OutputAccessMode.TransferToOutputSpace,
          tfJob.getTask("task4").getOutputFilesList().get(3).getMode());
        assertEquals(OutputAccessMode.none, tfJob.getTask("task4").getOutputFilesList().get(4).getMode());
        assertEquals(null, ((NativeTask) tfJob.getTask("task4")).getWorkingDir());
        assertEquals((tfJob.getTask("task4")).getNumberOfNodesNeeded(), 10);

        log("Test Job MULTI_NODES");
        TaskFlowJob mnJob = getJob(jobMultiNodesDescriptor, impl, scriptFolder);
        //Check job properties
        assertEquals(mnJob.getDescription(), "No description");
        assertEquals(mnJob.getName(), "job_multiNodes");
        assertEquals(mnJob.getPriority(), JobPriority.LOW);
        assertEquals(mnJob.getOnTaskErrorProperty().getValue(), OnTaskError.NONE);
        assertEquals(mnJob.getMaxNumberOfExecution(), 1);
        assertEquals(mnJob.getRestartTaskOnError(), RestartMode.ANYWHERE);
        assertEquals(mnJob.getType(), JobType.TASKSFLOW);
        assertEquals(mnJob.getGenericInformation().get("n1"), "v1");
        assertEquals(mnJob.getGenericInformation().get("n2"), "v2");
        //Check task properties
        JavaTask jt = (JavaTask) mnJob.getTask("Controller");
        assertEquals(jt.getArgument("numberToFind"), "100");
        assertEquals(jt.getOnTaskErrorProperty().getValue(), OnTaskError.NONE);
        assertEquals(jt.getCleaningScript(), null);
        assertEquals(jt.getDependencesList(), null);
        assertEquals(jt.getDescription(), "Will control the workers in order to find the prime number");
        assertEquals(jt.getExecutableClassName(), "org.ow2.proactive.scheduler.examples.MultiNodeExample");
        assertEquals(jt.getGenericInformation().get("n11"), "v11");
        assertEquals(jt.getGenericInformation().get("n22"), "v22");
        assertEquals(jt.getMaxNumberOfExecution(), 1);
        assertEquals(jt.getName(), "Controller");
        assertEquals(jt.getNumberOfNodesNeeded(), 3);
        assertEquals(jt.getPreScript(), null);
        assertEquals(jt.getPostScript(), null);
        assertEquals(jt.getRestartTaskOnError(), RestartMode.ANYWHERE);
        assertEquals(jt.getSelectionScripts(), null);
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

    private TaskFlowJob getJob(URL jobDesc, String jobFacImpl, String scriptFolder) throws JobCreationException,
            URISyntaxException {
        return ScriptUpdateUtil.resolveScripts((TaskFlowJob) JobFactory.getFactory(jobFacImpl).createJob(
          new File(jobDesc.toURI()).getAbsolutePath(), Collections.singletonMap("scripts.folder", scriptFolder)));
    }

}
