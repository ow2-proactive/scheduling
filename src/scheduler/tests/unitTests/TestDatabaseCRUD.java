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

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ow2.proactive.resourcemanager.common.scripting.GenerationScript;
import org.ow2.proactive.scheduler.common.job.JobEnvironment;
import org.ow2.proactive.scheduler.common.job.JobEvent;
import org.ow2.proactive.scheduler.common.job.JobFactory;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobPriority;
import org.ow2.proactive.scheduler.common.job.JobType;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.ForkEnvironment;
import org.ow2.proactive.scheduler.common.task.RestartMode;
import org.ow2.proactive.scheduler.common.task.TaskEvent;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskState;
import org.ow2.proactive.scheduler.common.task.util.BigString;
import org.ow2.proactive.scheduler.common.task.util.BooleanWrapper;
import org.ow2.proactive.scheduler.common.task.util.IntegerWrapper;
import org.ow2.proactive.scheduler.core.db.Condition;
import org.ow2.proactive.scheduler.core.db.ConditionComparator;
import org.ow2.proactive.scheduler.core.db.DatabaseManager;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.job.InternalJobFactory;
import org.ow2.proactive.scheduler.job.InternalTaskFlowJob;
import org.ow2.proactive.scheduler.job.JobDescriptor;
import org.ow2.proactive.scheduler.job.JobResultImpl;
import org.ow2.proactive.scheduler.task.JavaExecutableContainer;
import org.ow2.proactive.scheduler.task.NativeExecutableContainer;
import org.ow2.proactive.scheduler.task.TaskResultImpl;
import org.ow2.proactive.scheduler.task.internal.InternalJavaTask;
import org.ow2.proactive.scheduler.task.internal.InternalTask;

import functionnaltests.FunctionalTDefaultScheduler;


/**
 * This class will test the jobFactory.
 * It will parse job XML descriptors as exhaustive as possible to check that every checks or insertions are managed.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9.1
 */
public class TestDatabaseCRUD {

    private static String functionalTestSchedulerProperties = FunctionalTDefaultScheduler.class.getResource(
            "config/functionalTSchedulerProperties.ini").getPath();
    private static String jobTaskFlowDescriptor = TestJobFactory.class.getResource(
            "/unitTests/descriptors/Job_TaskFlow.xml").getPath();
    private static TaskFlowJob tfJob;
    private static InternalTaskFlowJob itfJob;

    @Before
    public void before() throws Exception {
        PASchedulerProperties.updateProperties(functionalTestSchedulerProperties);
        //build hibernate session
        DatabaseManager.build();
        //create a job
        tfJob = (TaskFlowJob) JobFactory.getFactory().createJob(jobTaskFlowDescriptor);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void run() throws Throwable {
        String URLbegin = System.getProperty("pa.scheduler.home") + "/";
        log("Test CREATE");
        itfJob = (InternalTaskFlowJob) InternalJobFactory.createJob(tfJob);
        itfJob.setId(JobId.nextId(itfJob.getName()));
        itfJob.setOwner("toto");
        //prepare tasks in order to be send into the core
        itfJob.prepareTasks();
        //create job descriptor
        itfJob.setJobDescriptor(new JobDescriptor(itfJob));
        //add a taskResult
        itfJob.setJobResult(new JobResultImpl(itfJob.getId()));
        ((JobResultImpl) itfJob.getJobResult()).addTaskResult("task2", new TaskResultImpl(TaskId
                .nextId(itfJob.getJobInfo().getJobId()), "salut", null), true);
        //register the job
        DatabaseManager.register(itfJob);
        //list of internal job to recover
        log("Test  READ");
        List<InternalJob> recovering = DatabaseManager.recoverAllJobs();
        Assert.assertTrue(recovering.size() == 1);
        itfJob = (InternalTaskFlowJob) recovering.get(0);
        //Check job properties
        Assert.assertEquals(itfJob.getOwner(), "toto");
        Assert.assertEquals(itfJob.getDescription(), "No paquit in its HostName.");
        Assert.assertEquals(itfJob.getName(), "Job_TaskFlow");
        Assert.assertEquals(itfJob.getProjectName(), "My_project");
        Assert.assertEquals(itfJob.getPriority(), JobPriority.NORMAL);
        Assert.assertEquals(itfJob.isCancelJobOnError(), true);
        Assert.assertEquals(itfJob.getMaxNumberOfExecution(), 2);
        Assert.assertEquals(itfJob.getRestartTaskOnError(), RestartMode.ELSEWHERE);
        Assert.assertEquals(itfJob.getType(), JobType.TASKSFLOW);
        Assert.assertEquals(itfJob.getTasks().size(), 4);
        //Check task 1 properties
        for (InternalTask it : itfJob.getTasks()) {
            if (it.getName().equals("task1")) {
                Assert.assertEquals(it.getName(), "task1");
                Assert.assertEquals(it.isCancelJobOnError(), false);
                Assert.assertEquals(it.isPreciousResult(), false);
                Assert.assertEquals(it.getRestartTaskOnError(), RestartMode.ANYWHERE);
                Assert.assertEquals(it.getMaxNumberOfExecution(), 1);
                Assert.assertEquals(it.getDescription(), "Parallel Tasks - Task 1");
                Assert.assertEquals(it.getSelectionScript().isDynamic(), false);
                Assert.assertNotNull(it.getSelectionScript().getScript());
                Assert.assertEquals(it.getSelectionScript().getParameters().length, 1);
                Assert.assertEquals(it.getSelectionScript().getParameters()[0], "paquit");
                Assert.assertTrue(it.getPreScript().getScript().contains("Beginning of Pre-Script"));
                Assert.assertTrue(it.getPostScript().getScript().contains(
                        "Content is equals to " + URLbegin + "jobs_descriptors/unset.js"));
                Assert.assertNull(it.getPostScript().getParameters());
                Assert.assertTrue(it.getCleaningScript().getScript().contains("Beginning of clean script"));
                Assert.assertNull(it.getCleaningScript().getParameters());
                Assert.assertNull(it.getDependences());
                Assert.assertEquals(it.getNumberOfNodesNeeded(), 1);
                Assert.assertNull(it.getResultPreview());
                Assert.assertEquals(it.getWallTime(), 12 * 1000);
                Assert.assertEquals(it.isWallTime(), true);
                Assert.assertEquals(it.getGenericInformations().size(), 0);
                Assert.assertNull(it.getExecutableContainer());
                DatabaseManager.load(it);
                Field f = JavaExecutableContainer.class.getDeclaredField("args");
                f.setAccessible(true);
                Assert.assertEquals(((Map<String, BigString>) f.get(it.getExecutableContainer())).get(
                        "sleepTime").getValue(), "1");
                Assert.assertEquals(((Map<String, BigString>) f.get(it.getExecutableContainer())).get(
                        "number").getValue(), "1");
                f = JavaExecutableContainer.class.getDeclaredField("userExecutableClassName");
                f.setAccessible(true);
                Assert.assertEquals((String) f.get(it.getExecutableContainer()),
                        "org.ow2.proactive.scheduler.examples.WaitAndPrint");
                Assert.assertEquals(((InternalJavaTask) it).isFork(), true);
                Assert.assertNull(((InternalJavaTask) it).getForkEnvironment());
            } else if (it.getName().equals("task2")) {
                //Check task 2 properties
                Assert.assertEquals(it.getName(), "task2");
                Assert.assertEquals(it.isCancelJobOnError(), true);
                Assert.assertEquals(it.isPreciousResult(), false);
                Assert.assertEquals(it.getRestartTaskOnError(), RestartMode.ELSEWHERE);
                Assert.assertEquals(it.getMaxNumberOfExecution(), 2);
                Assert.assertEquals(it.getDescription(), "Parallel Tasks - Task 2");
                Assert.assertNull(it.getSelectionScript());
                Assert.assertTrue(it.getPreScript().getScript().contains("Beginning of Pre-Script"));
                Assert.assertNull(it.getPostScript());
                Assert.assertNull(it.getCleaningScript());
                Assert.assertNull(it.getDependences());
                Assert.assertEquals(it.getNumberOfNodesNeeded(), 1);
                Assert.assertNull(it.getResultPreview());
                Assert.assertEquals(it.getWallTime(), 0);
                Assert.assertEquals(it.isWallTime(), false);
                Assert.assertEquals(it.getGenericInformations().size(), 0);
                Assert.assertNull(it.getExecutableContainer());
                DatabaseManager.load(it);
                Field f = JavaExecutableContainer.class.getDeclaredField("args");
                f.setAccessible(true);
                Assert.assertEquals(((Map<String, BigString>) f.get(it.getExecutableContainer())).get(
                        "sleepTime").getValue(), "12");
                Assert.assertEquals(((Map<String, BigString>) f.get(it.getExecutableContainer())).get(
                        "number").getValue(), "21");
                Assert.assertEquals(((Map<String, BigString>) f.get(it.getExecutableContainer())).get("test")
                        .getValue(), "/bin/java/jdk1.5");
                f = JavaExecutableContainer.class.getDeclaredField("userExecutableClassName");
                f.setAccessible(true);
                Assert.assertEquals((String) f.get(it.getExecutableContainer()),
                        "org.ow2.proactive.scheduler.examples.WaitAndPrint");
                Assert.assertEquals(((InternalJavaTask) it).isFork(), true);
                Assert.assertEquals(((InternalJavaTask) it).isWallTime(), false);
                Assert.assertEquals(((InternalJavaTask) it).getForkEnvironment().getJavaHome(),
                        "/bin/java/jdk1.5");
                Assert.assertEquals(((InternalJavaTask) it).getForkEnvironment().getJVMParameters(),
                        "-dparam=12 -djhome=/bin/java/jdk1.5");
            } else if (it.getName().equals("task3")) {
                //Check task 3 properties
                Assert.assertEquals(it.getName(), "task3");
                Assert.assertEquals(it.isCancelJobOnError(), true);
                Assert.assertEquals(it.isPreciousResult(), false);
                Assert.assertEquals(it.getRestartTaskOnError(), RestartMode.ELSEWHERE);
                Assert.assertEquals(it.getMaxNumberOfExecution(), 2);
                Assert.assertEquals(it.getDescription(), "Dependent Tasks - Task 3");
                Assert.assertNull(it.getSelectionScript());
                Assert.assertNull(it.getPreScript());
                Assert.assertTrue(it.getPostScript().getScript().contains(
                        "Unsetting system property user.property1"));
                Assert.assertNull(it.getPostScript().getParameters());
                Assert.assertNull(it.getCleaningScript());
                Assert.assertEquals(it.getDependences().size(), 2);
                Assert.assertEquals(it.getDependences().get(0).getName(), "task1");
                Assert.assertEquals(it.getDependences().get(1).getName(), "task2");
                Assert.assertEquals(it.getNumberOfNodesNeeded(), 1);
                Assert.assertNull(it.getResultPreview());
                Assert.assertEquals(it.getWallTime(), 10 * 60 * 1000 + 53 * 1000);
                Assert.assertEquals(it.isWallTime(), true);
                Assert.assertEquals(it.getGenericInformations().size(), 0);
                Assert.assertNull(it.getExecutableContainer());
                DatabaseManager.load(it);
                Field f = NativeExecutableContainer.class.getDeclaredField("command");
                f.setAccessible(true);
                Assert.assertEquals(((String[]) f.get(it.getExecutableContainer())).length, 5);
                Assert.assertEquals(((String[]) f.get(it.getExecutableContainer()))[0], URLbegin +
                    "jobs_descriptors/job_native_linux/nativTask");
                Assert.assertEquals(((String[]) f.get(it.getExecutableContainer()))[1], "1");
                Assert.assertEquals(((String[]) f.get(it.getExecutableContainer()))[2], "2 2");
                Assert.assertEquals(((String[]) f.get(it.getExecutableContainer()))[3], "3");
                Assert.assertEquals(((String[]) f.get(it.getExecutableContainer()))[4], "12");
            } else if (it.getName().equals("task4")) {
                //Check task 4 properties
                Assert.assertEquals(it.getName(), "task4");
                Assert.assertEquals(it.isCancelJobOnError(), true);
                Assert.assertEquals(it.isPreciousResult(), true);
                Assert.assertEquals(it.getRestartTaskOnError(), RestartMode.ANYWHERE);
                Assert.assertEquals(it.getMaxNumberOfExecution(), 3);
                Assert.assertNull(it.getDescription());
                Assert.assertNull(it.getSelectionScript());
                Assert.assertNull(it.getPreScript());
                Assert.assertNull(it.getPostScript());
                Assert.assertNull(it.getCleaningScript());
                Assert.assertEquals(it.getDependences().size(), 1);
                Assert.assertEquals(it.getDependences().get(0).getName(), "task3");
                Assert.assertEquals(it.getNumberOfNodesNeeded(), 1);
                Assert.assertEquals(it.getResultPreview(), "tadzaam");
                Assert.assertEquals(it.getWallTime(), 0);
                Assert.assertEquals(it.isWallTime(), false);
                Assert.assertEquals(it.getGenericInformations().get("n11"), "v11");
                Assert.assertEquals(it.getGenericInformations().get("n22"), "v22");
                Assert.assertNull(it.getExecutableContainer());
                DatabaseManager.load(it);
                Field f = NativeExecutableContainer.class.getDeclaredField("generated");
                f.setAccessible(true);
                Assert.assertEquals(((GenerationScript) f.get(it.getExecutableContainer())).getScript(),
                        "command=args[0]+\" 12\";\n");
                Assert.assertEquals(
                        ((GenerationScript) f.get(it.getExecutableContainer())).getParameters()[0], URLbegin +
                            "jobs_descriptors/job_native_linux/nativTask");
                Assert.assertEquals(((GenerationScript) f.get(it.getExecutableContainer())).execute()
                        .getResult(), URLbegin + "jobs_descriptors/job_native_linux/nativTask 12");
            }
        }
        //test job and task result
        Assert.assertEquals(1, itfJob.getJobResult().getAllResults().size());
        Assert.assertEquals(1, itfJob.getJobResult().getPreciousResults().size());
        Assert.assertEquals(0, itfJob.getJobResult().getExceptionResults().size());
        Assert.assertNotNull(itfJob.getJobResult().getAllResults().get("task2"));
        //before loading
        Assert.assertNull(itfJob.getJobResult().getAllResults().get("task2").value());
        DatabaseManager.load(itfJob.getJobResult().getAllResults().get("task2"));
        //after loading
        Assert.assertNotNull(itfJob.getJobResult().getAllResults().get("task2").value());
        Assert.assertEquals("salut", itfJob.getJobResult().getAllResults().get("task2").value());
        Assert.assertNull(itfJob.getJobResult().getAllResults().get("task2").getException());
        log("Test UPDATE");
        //unload
        for (InternalTask it : itfJob.getTasks()) {
            DatabaseManager.unload(it);
            Assert.assertEquals(it.getExecutableContainer(), null);
        }
        //load
        for (InternalTask it : itfJob.getTasks()) {
            DatabaseManager.load(it);
            if (it.getName().equals("task1")) {
                Field f = JavaExecutableContainer.class.getDeclaredField("args");
                f.setAccessible(true);
                Assert.assertEquals(((Map<String, BigString>) f.get(it.getExecutableContainer())).get(
                        "sleepTime").getValue(), "1");
                Assert.assertEquals(((Map<String, BigString>) f.get(it.getExecutableContainer())).get(
                        "number").getValue(), "1");
                f = JavaExecutableContainer.class.getDeclaredField("userExecutableClassName");
                f.setAccessible(true);
                Assert.assertEquals((String) f.get(it.getExecutableContainer()),
                        "org.ow2.proactive.scheduler.examples.WaitAndPrint");
            } else if (it.getName().equals("task2")) {
                Field f = JavaExecutableContainer.class.getDeclaredField("args");
                f.setAccessible(true);
                Assert.assertEquals(((Map<String, BigString>) f.get(it.getExecutableContainer())).get(
                        "sleepTime").getValue(), "12");
                Assert.assertEquals(((Map<String, BigString>) f.get(it.getExecutableContainer())).get(
                        "number").getValue(), "21");
                Assert.assertEquals(((Map<String, BigString>) f.get(it.getExecutableContainer())).get("test")
                        .getValue(), "/bin/java/jdk1.5");
                f = JavaExecutableContainer.class.getDeclaredField("userExecutableClassName");
                f.setAccessible(true);
                Assert.assertEquals((String) f.get(it.getExecutableContainer()),
                        "org.ow2.proactive.scheduler.examples.WaitAndPrint");
            } else if (it.getName().equals("task3")) {
                Field f = NativeExecutableContainer.class.getDeclaredField("command");
                f.setAccessible(true);
                Assert.assertEquals(((String[]) f.get(it.getExecutableContainer())).length, 5);
                Assert.assertEquals(((String[]) f.get(it.getExecutableContainer()))[0], URLbegin +
                    "jobs_descriptors/job_native_linux/nativTask");
                Assert.assertEquals(((String[]) f.get(it.getExecutableContainer()))[1], "1");
                Assert.assertEquals(((String[]) f.get(it.getExecutableContainer()))[2], "2 2");
                Assert.assertEquals(((String[]) f.get(it.getExecutableContainer()))[3], "3");
                Assert.assertEquals(((String[]) f.get(it.getExecutableContainer()))[4], "12");
            } else if (it.getName().equals("task4")) {
                Field f = NativeExecutableContainer.class.getDeclaredField("generated");
                f.setAccessible(true);
                Assert.assertEquals(((GenerationScript) f.get(it.getExecutableContainer())).getScript(),
                        "command=args[0]+\" 12\";\n");
                Assert.assertEquals(
                        ((GenerationScript) f.get(it.getExecutableContainer())).getParameters()[0], URLbegin +
                            "jobs_descriptors/job_native_linux/nativTask");
                Assert.assertEquals(((GenerationScript) f.get(it.getExecutableContainer())).execute()
                        .getResult(), URLbegin + "jobs_descriptors/job_native_linux/nativTask 12");
            }
        }
        //update (1 task is enough)
        TaskEvent infoMem = itfJob.getTasks().get(0).getTaskInfo();
        TaskId id = infoMem.getTaskId();
        new Condition("taskId", ConditionComparator.EQUALS_TO, id);
        //check MEM vs DB instance
        TaskEvent infoDB = DatabaseManager.recover(TaskEvent.class,
                new Condition("taskId", ConditionComparator.EQUALS_TO, id)).get(0);
        Assert.assertEquals(infoDB.getExecutionHostName(), infoMem.getExecutionHostName());
        Assert.assertEquals(infoDB.getFinishedTime(), infoMem.getFinishedTime());
        Assert.assertEquals(infoDB.getNumberOfExecutionLeft(), infoMem.getNumberOfExecutionLeft());
        Assert.assertEquals(infoDB.getNumberOfExecutionOnFailureLeft(), infoMem
                .getNumberOfExecutionOnFailureLeft());
        Assert.assertEquals(infoDB.getStartTime(), infoMem.getStartTime());
        Assert.assertEquals(infoDB.getStatus(), infoMem.getStatus());
        Assert.assertEquals(infoDB.getJobEvent().getJobId(), infoMem.getJobEvent().getJobId());
        //update MEM instance
        infoMem.setExecutionHostName("toto");
        infoMem.setStartTime(1142564);
        infoMem.setStatus(TaskState.RUNNING);
        infoMem.setTaskId(TaskId.nextId(id.getJobId()));
        //synchronize update with database
        DatabaseManager.synchronize(infoMem);
        //re-check MEM vs DB instance
        infoDB = DatabaseManager.recover(TaskEvent.class,
                new Condition("taskId", ConditionComparator.EQUALS_TO, id)).get(0);
        Assert.assertEquals(infoDB.getExecutionHostName(), infoMem.getExecutionHostName());
        Assert.assertEquals(infoDB.getExecutionHostName(), "toto");
        Assert.assertEquals(infoDB.getFinishedTime(), infoMem.getFinishedTime());
        Assert.assertEquals(infoDB.getNumberOfExecutionLeft(), infoMem.getNumberOfExecutionLeft());
        Assert.assertEquals(infoDB.getNumberOfExecutionOnFailureLeft(), infoMem
                .getNumberOfExecutionOnFailureLeft());
        Assert.assertEquals(infoDB.getStartTime(), infoMem.getStartTime());
        Assert.assertEquals(infoDB.getStartTime(), 1142564);
        Assert.assertEquals(infoDB.getStatus(), infoMem.getStatus());
        Assert.assertEquals(infoDB.getJobEvent().getJobId(), infoMem.getJobEvent().getJobId());
        Assert.assertTrue(!infoDB.getTaskId().equals(infoMem.getTaskId()));
        log("Test DELETE");
        infoMem.setTaskId(id);
        DatabaseManager.delete(itfJob);
        Assert.assertEquals(DatabaseManager.recover(InternalJob.class).size(), 0);
        Assert.assertEquals(DatabaseManager.recover(InternalTask.class).size(), 0);
        Assert.assertEquals(DatabaseManager.recover(JobEvent.class).size(), 0);
        Assert.assertEquals(DatabaseManager.recover(TaskEvent.class).size(), 0);
        Assert.assertEquals(DatabaseManager.recover(JobId.class).size(), 0);
        Assert.assertEquals(DatabaseManager.recover(TaskId.class).size(), 0);
        Assert.assertEquals(DatabaseManager.recover(ForkEnvironment.class).size(), 0);
        Assert.assertEquals(DatabaseManager.recover(JobEnvironment.class).size(), 0);
        Assert.assertEquals(DatabaseManager.recover(BigString.class).size(), 0);
        Assert.assertEquals(DatabaseManager.recover(BooleanWrapper.class).size(), 0);
        Assert.assertEquals(DatabaseManager.recover(IntegerWrapper.class).size(), 0);
    }

    @After
    public void after() throws Exception {
        DatabaseManager.close();
    }

    private void log(String s) {
        System.out.println("------------------------------ " + s);
    }

}
