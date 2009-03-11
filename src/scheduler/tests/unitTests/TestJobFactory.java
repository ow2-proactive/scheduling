/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $PROACTIVE_INITIAL_DEV$
 */
package unitTests;

import junit.framework.Assert;

import org.junit.Test;
import org.ow2.proactive.scheduler.common.job.JobPriority;
import org.ow2.proactive.scheduler.common.job.JobType;
import org.ow2.proactive.scheduler.common.job.ProActiveJob;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.job.factories.JobFactory;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.NativeTask;
import org.ow2.proactive.scheduler.common.task.RestartMode;


/**
 * This class will test the jobFactory.
 * It will parse job XML descriptors as exhaustively as possible to check that every features and insertions are managed.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9.1
 */
public class TestJobFactory {

    private static String jobTaskFlowDescriptor = TestJobFactory.class.getResource(
            "/unitTests/descriptors/Job_TaskFlow.xml").getPath();
    private static String jobProActiveDescriptor = TestJobFactory.class.getResource(
            "/unitTests/descriptors/Job_ProActive.xml").getPath();

    @Test
    public void run() throws Throwable {
        log("TEST jobFactory STAX");
        //test default behavior as well (null is STAX)
        run1(null);
        log("TEST jobFactory XPATH");
        //test XPATH factory
        run1(org.ow2.proactive.scheduler.common.job.factories.JobFactory_xpath.class.getCanonicalName());
    }

    private void run1(String impl) throws Throwable {
        String URLbegin = System.getProperty("pa.scheduler.home") + "/";
        log("Test Job TASKFLOW");
        TaskFlowJob tfJob = (TaskFlowJob) JobFactory.getFactory(impl).createJob(jobTaskFlowDescriptor);
        //Check job properties
        Assert.assertEquals(tfJob.getDescription(), "No paquit in its HostName.");
        Assert.assertEquals(tfJob.getName(), "Job_TaskFlow");
        Assert.assertEquals(tfJob.getProjectName(), "My_project");
        Assert.assertEquals(tfJob.getPriority(), JobPriority.NORMAL);
        Assert.assertEquals(tfJob.isCancelJobOnError(), true);
        Assert.assertEquals(tfJob.getMaxNumberOfExecution(), 2);
        Assert.assertEquals(tfJob.getRestartTaskOnError(), RestartMode.ELSEWHERE);
        Assert.assertEquals(tfJob.getType(), JobType.TASKSFLOW);
        Assert.assertEquals(tfJob.getTasks().size(), 4);
        //Check task 1 properties
        Assert.assertEquals(tfJob.getTask("task1").getName(), "task1");
        Assert.assertEquals(tfJob.getTask("task1").isCancelJobOnError(), false);
        Assert.assertEquals(tfJob.getTask("task1").isPreciousResult(), false);
        Assert.assertEquals(tfJob.getTask("task1").getRestartTaskOnError(), RestartMode.ANYWHERE);
        Assert.assertEquals(tfJob.getTask("task1").getMaxNumberOfExecution(), 1);
        Assert.assertEquals(tfJob.getTask("task1").getDescription(), "Parallel Tasks - Task 1");
        Assert.assertEquals(tfJob.getTask("task1").getSelectionScript().isDynamic(), false);
        Assert.assertTrue(tfJob.getTask("task1").getSelectionScript().getScript() != null);
        Assert.assertEquals(tfJob.getTask("task1").getSelectionScript().getParameters().length, 1);
        Assert.assertEquals(tfJob.getTask("task1").getSelectionScript().getParameters()[0], "paquit");
        Assert.assertTrue(tfJob.getTask("task1").getPreScript().getScript().contains(
                "Beginning of Pre-Script"));
        Assert.assertTrue(tfJob.getTask("task1").getPostScript().getScript().contains(
                "Content is equals to " + URLbegin + "sample/jobs_descriptors/unset.js"));
        Assert.assertEquals(tfJob.getTask("task1").getPostScript().getParameters(), null);
        Assert.assertTrue(tfJob.getTask("task1").getCleaningScript().getScript().contains(
                "Beginning of clean script"));
        Assert.assertEquals(tfJob.getTask("task1").getCleaningScript().getParameters(), null);
        Assert.assertEquals(tfJob.getTask("task1").getDependencesList(), null);
        Assert.assertEquals(tfJob.getTask("task1").getNumberOfNodesNeeded(), 1);
        Assert.assertEquals(tfJob.getTask("task1").getResultPreview(), null);
        Assert.assertEquals(tfJob.getTask("task1").getWallTime(), 12 * 1000);
        Assert.assertEquals(tfJob.getTask("task1").isWallTime(), true);
        Assert.assertEquals(tfJob.getTask("task1").getGenericInformations().size(), 0);
        Assert.assertEquals(((JavaTask) tfJob.getTask("task1")).getArguments().get("sleepTime"), "1");
        Assert.assertEquals(((JavaTask) tfJob.getTask("task1")).getArguments().get("number"), "1");
        Assert.assertEquals(((JavaTask) tfJob.getTask("task1")).getExecutableClassName(),
                "org.ow2.proactive.scheduler.examples.WaitAndPrint");
        Assert.assertEquals(((JavaTask) tfJob.getTask("task1")).isFork(), true);
        Assert.assertEquals(((JavaTask) tfJob.getTask("task1")).getForkEnvironment(), null);
        //Check task 2 properties
        Assert.assertEquals(tfJob.getTask("task2").getName(), "task2");
        //the following commented check fails, it is what we expect, because replacement is done in the internal factory.
        //it avoids complex changes during user job creation process.
        //this property is tested in the TestDatabaseCRUD
        //Assert.assertEquals(tfJob.getTask("task2").isCancelJobOnError(),true);
        Assert.assertEquals(tfJob.getTask("task2").isPreciousResult(), false);
        Assert.assertEquals(tfJob.getTask("task2").getRestartTaskOnError(), RestartMode.ELSEWHERE);
        Assert.assertEquals(tfJob.getTask("task2").getMaxNumberOfExecution(), 2);
        Assert.assertEquals(tfJob.getTask("task2").getDescription(), "Parallel Tasks - Task 2");
        Assert.assertEquals(tfJob.getTask("task2").getSelectionScript(), null);
        Assert.assertTrue(tfJob.getTask("task2").getPreScript().getScript().contains(
                "Beginning of Pre-Script"));
        Assert.assertEquals(tfJob.getTask("task2").getPostScript(), null);
        Assert.assertEquals(tfJob.getTask("task2").getCleaningScript(), null);
        Assert.assertEquals(tfJob.getTask("task2").getDependencesList(), null);
        Assert.assertEquals(tfJob.getTask("task2").getNumberOfNodesNeeded(), 1);
        Assert.assertEquals(tfJob.getTask("task2").getResultPreview(), null);
        Assert.assertEquals(tfJob.getTask("task2").getWallTime(), 0);
        Assert.assertEquals(tfJob.getTask("task2").isWallTime(), false);
        Assert.assertEquals(tfJob.getTask("task2").getGenericInformations().size(), 0);
        Assert.assertEquals(((JavaTask) tfJob.getTask("task2")).getArguments().get("sleepTime"), "12");
        Assert.assertEquals(((JavaTask) tfJob.getTask("task2")).getArguments().get("number"), "21");
        Assert.assertEquals(((JavaTask) tfJob.getTask("task2")).getArguments().get("test"),
                "/bin/java/jdk1.5");
        Assert.assertEquals(((JavaTask) tfJob.getTask("task2")).getExecutableClassName(),
                "org.ow2.proactive.scheduler.examples.WaitAndPrint");
        Assert.assertEquals(((JavaTask) tfJob.getTask("task2")).isFork(), true);
        Assert.assertEquals(((JavaTask) tfJob.getTask("task2")).isWallTime(), false);
        Assert.assertEquals(((JavaTask) tfJob.getTask("task2")).getForkEnvironment().getJavaHome(),
                "/bin/java/jdk1.5");
        Assert.assertEquals(((JavaTask) tfJob.getTask("task2")).getForkEnvironment().getJVMParameters(),
                "-dparam=12 -djhome=/bin/java/jdk1.5");
        //Check task 3 properties
        Assert.assertEquals(tfJob.getTask("task3").getName(), "task3");
        //the following commented check fails, it is what we expect, because replacement is done in the internal factory.
        //it avoids complex changes during user job creation process.
        //this property is tested in the TestDatabaseCRUD
        //Assert.assertEquals(tfJob.getTask("task3").isCancelJobOnError(),true);
        Assert.assertEquals(tfJob.getTask("task3").isPreciousResult(), false);
        //the following commented check fails, it is what we expect, because replacement is done in the internal factory.
        //it avoids complex changes during user job creation process.
        //this property is tested in the TestDatabaseCRUD
        //Assert.assertEquals(tfJob.getTask("task3").getRestartTaskOnError(),RestartMode.ELSEWHERE);
        //the following commented check fails, it is what we expect, because replacement is done in the internal factory.
        //it avoids complex changes during user job creation process.
        //this property is tested in the TestDatabaseCRUD
        //Assert.assertEquals(tfJob.getTask("task3").getMaxNumberOfExecution(),2);
        Assert.assertEquals(tfJob.getTask("task3").getDescription(), "Dependent Tasks - Task 3");
        Assert.assertEquals(tfJob.getTask("task3").getSelectionScript(), null);
        Assert.assertEquals(tfJob.getTask("task3").getPreScript(), null);
        Assert.assertTrue(tfJob.getTask("task3").getPostScript().getScript().contains(
                "Unsetting system property user.property1"));
        Assert.assertEquals(tfJob.getTask("task3").getPostScript().getParameters(), null);
        Assert.assertEquals(tfJob.getTask("task3").getCleaningScript(), null);
        Assert.assertEquals(tfJob.getTask("task3").getDependencesList().size(), 2);
        Assert.assertEquals(tfJob.getTask("task3").getDependencesList().get(0).getName(), "task1");
        Assert.assertEquals(tfJob.getTask("task3").getDependencesList().get(1).getName(), "task2");
        Assert.assertEquals(tfJob.getTask("task3").getNumberOfNodesNeeded(), 1);
        Assert.assertEquals(tfJob.getTask("task3").getResultPreview(), null);
        Assert.assertEquals(tfJob.getTask("task3").getWallTime(), 10 * 60 * 1000 + 53 * 1000);
        Assert.assertEquals(tfJob.getTask("task3").isWallTime(), true);
        Assert.assertEquals(tfJob.getTask("task3").getGenericInformations().size(), 0);
        Assert.assertEquals(((NativeTask) tfJob.getTask("task3")).getCommandLine().length, 5);
        Assert.assertEquals(((NativeTask) tfJob.getTask("task3")).getCommandLine()[0], URLbegin +
            "sample/jobs_descriptors/job_native_linux/nativTask");
        Assert.assertEquals(((NativeTask) tfJob.getTask("task3")).getCommandLine()[1], "1");
        Assert.assertEquals(((NativeTask) tfJob.getTask("task3")).getCommandLine()[2], "2 2");
        Assert.assertEquals(((NativeTask) tfJob.getTask("task3")).getCommandLine()[3], "3");
        Assert.assertEquals(((NativeTask) tfJob.getTask("task3")).getCommandLine()[4], "12");
        //Check task 4 properties
        Assert.assertEquals(tfJob.getTask("task4").getName(), "task4");
        Assert.assertEquals(tfJob.getTask("task4").isCancelJobOnError(), true);
        Assert.assertEquals(tfJob.getTask("task4").isPreciousResult(), true);
        Assert.assertEquals(tfJob.getTask("task4").getRestartTaskOnError(), RestartMode.ANYWHERE);
        Assert.assertEquals(tfJob.getTask("task4").getMaxNumberOfExecution(), 3);
        Assert.assertEquals(tfJob.getTask("task4").getDescription(), null);
        Assert.assertEquals(tfJob.getTask("task4").getSelectionScript(), null);
        Assert.assertEquals(tfJob.getTask("task4").getPreScript(), null);
        Assert.assertEquals(tfJob.getTask("task4").getPostScript(), null);
        Assert.assertEquals(tfJob.getTask("task4").getCleaningScript(), null);
        Assert.assertEquals(tfJob.getTask("task4").getDependencesList().size(), 1);
        Assert.assertEquals(tfJob.getTask("task4").getDependencesList().get(0).getName(), "task3");
        Assert.assertEquals(tfJob.getTask("task4").getNumberOfNodesNeeded(), 1);
        Assert.assertEquals(tfJob.getTask("task4").getResultPreview(), "tadzaam");
        Assert.assertEquals(tfJob.getTask("task4").getWallTime(), 0);
        Assert.assertEquals(tfJob.getTask("task4").isWallTime(), false);
        Assert.assertEquals(tfJob.getTask("task4").getGenericInformations().get("n11"), "v11");
        Assert.assertEquals(tfJob.getTask("task4").getGenericInformations().get("n22"), "v22");
        Assert.assertEquals(((NativeTask) tfJob.getTask("task4")).getGenerationScript().getScript(),
                "command=args[0]+\" 12\";\n");
        Assert.assertEquals(((NativeTask) tfJob.getTask("task4")).getGenerationScript().getParameters()[0],
                URLbegin + "sample/jobs_descriptors/job_native_linux/nativTask");
        Assert.assertEquals(
                ((NativeTask) tfJob.getTask("task4")).getGenerationScript().execute().getResult(), URLbegin +
                    "sample/jobs_descriptors/job_native_linux/nativTask 12");

        log("Test Job PROACTIVE");
        ProActiveJob paJob = (ProActiveJob) JobFactory.getFactory().createJob(jobProActiveDescriptor);
        //Check job properties
        Assert.assertEquals(paJob.getDescription(), "No description");
        Assert.assertEquals(paJob.getName(), "job_proActive");
        Assert.assertEquals(paJob.getPriority(), JobPriority.LOW);
        Assert.assertEquals(paJob.getLogFile(), "one/file/to/log");
        Assert.assertEquals(paJob.getEnvironment().getJobClasspath()[0], "one/two/");
        Assert.assertEquals(paJob.getEnvironment().getJobClasspath()[1], "three");
        Assert.assertEquals(paJob.isCancelJobOnError(), false);
        Assert.assertEquals(paJob.getMaxNumberOfExecution(), 1);
        Assert.assertEquals(paJob.getRestartTaskOnError(), RestartMode.ANYWHERE);
        Assert.assertEquals(paJob.getType(), JobType.PROACTIVE);
        Assert.assertEquals(paJob.getGenericInformations().get("n1"), "v1");
        Assert.assertEquals(paJob.getGenericInformations().get("n2"), "v2");
        //Check task properties
        Assert.assertEquals(paJob.getTask().getArguments().get("numberToFind"), "100");
        Assert.assertEquals(paJob.getTask().isCancelJobOnError(), false);
        Assert.assertEquals(paJob.getTask().getCleaningScript(), null);
        Assert.assertEquals(paJob.getTask().getDependencesList(), null);
        Assert.assertEquals(paJob.getTask().getDescription(),
                "Will control the workers in order to find the prime number");
        Assert.assertEquals(paJob.getTask().getExecutableClassName(),
                "org.ow2.proactive.scheduler.examples.ProActiveExample");
        Assert.assertEquals(paJob.getTask().getGenericInformations().get("n11"), "v11");
        Assert.assertEquals(paJob.getTask().getGenericInformations().get("n22"), "v22");
        Assert.assertEquals(paJob.getTask().getMaxNumberOfExecution(), 1);
        Assert.assertEquals(paJob.getTask().getName(), "Controller");
        Assert.assertEquals(paJob.getTask().getNumberOfNodesNeeded(), 3);
        Assert.assertEquals(paJob.getTask().getPreScript(), null);
        Assert.assertEquals(paJob.getTask().getPostScript(), null);
        Assert.assertEquals(paJob.getTask().getRestartTaskOnError(), RestartMode.ANYWHERE);
        Assert.assertEquals(paJob.getTask().getResultPreview(), "path.to.package.class");
        Assert.assertEquals(paJob.getTask().getSelectionScript(), null);
        Assert.assertEquals(paJob.getTask().isPreciousResult(), true);
    }

    private void log(String s) {
        System.out.println("------------------------------ " + s);
    }

}
