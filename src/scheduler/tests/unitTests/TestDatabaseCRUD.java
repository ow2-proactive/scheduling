/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
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
package unitTests;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.proactive.core.util.converter.ByteToObjectConverter;
import org.ow2.proactive.db.Condition;
import org.ow2.proactive.db.ConditionComparator;
import org.ow2.proactive.db.types.BigString;
import org.ow2.proactive.scheduler.common.job.JobEnvironment;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobPriority;
import org.ow2.proactive.scheduler.common.job.JobType;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.job.factories.JobFactory;
import org.ow2.proactive.scheduler.common.task.ForkEnvironment;
import org.ow2.proactive.scheduler.common.task.RestartMode;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskInfo;
import org.ow2.proactive.scheduler.common.task.TaskStatus;
import org.ow2.proactive.scheduler.common.task.util.BooleanWrapper;
import org.ow2.proactive.scheduler.common.task.util.ByteArrayWrapper;
import org.ow2.proactive.scheduler.common.task.util.IntegerWrapper;
import org.ow2.proactive.scheduler.core.db.DatabaseManager;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.descriptor.JobDescriptorImpl;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.job.InternalJobFactory;
import org.ow2.proactive.scheduler.job.InternalTaskFlowJob;
import org.ow2.proactive.scheduler.job.JobIdImpl;
import org.ow2.proactive.scheduler.job.JobResultImpl;
import org.ow2.proactive.scheduler.task.ForkedJavaExecutableContainer;
import org.ow2.proactive.scheduler.task.JavaExecutableContainer;
import org.ow2.proactive.scheduler.task.NativeExecutableContainer;
import org.ow2.proactive.scheduler.task.TaskIdImpl;
import org.ow2.proactive.scheduler.task.TaskInfoImpl;
import org.ow2.proactive.scheduler.task.TaskResultImpl;
import org.ow2.proactive.scheduler.task.internal.InternalJavaTask;
import org.ow2.proactive.scheduler.task.internal.InternalTask;
import org.ow2.proactive.scripting.GenerationScript;

import functionaltests.SchedulerTHelper;


/**
 * This class will test the jobFactory.
 * It will parse job XML descriptors as exhaustive as possible to check that every checks or insertions are managed.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9.1
 */
public class TestDatabaseCRUD {

    private static URL functionalTestSchedulerProperties = SchedulerTHelper.class
            .getResource("config/functionalTSchedulerProperties.ini");
    private static URL jobTaskFlowDescriptor = TestJobFactory.class
            .getResource("/unitTests/descriptors/Job_TaskFlow.xml");
    private static TaskFlowJob tfJob;
    private static InternalTaskFlowJob itfJob;

    @Before
    public void before() throws Exception {
        PASchedulerProperties.updateProperties(new File(functionalTestSchedulerProperties.toURI())
                .getAbsolutePath());
        //build hibernate session
        DatabaseManager.getInstance().build();
        //create a job
        System.setProperty("jobName", "Job_TaskFlow");
        tfJob = (TaskFlowJob) JobFactory.getFactory().createJob(
                new File(jobTaskFlowDescriptor.toURI()).getAbsolutePath());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void run() throws Throwable {
        String URLbegin = System.getProperty("pa.scheduler.home") + "/";
        log("Test CREATE");
        itfJob = (InternalTaskFlowJob) InternalJobFactory.createJob(tfJob, null);
        itfJob.setId(JobIdImpl.nextId(itfJob.getName()));
        itfJob.setOwner("toto");
        //prepare tasks in order to be send into the core
        itfJob.prepareTasks();
        //create job descriptor
        itfJob.setJobDescriptor(new JobDescriptorImpl(itfJob));
        //add a taskResult
        itfJob.setJobResult(new JobResultImpl(itfJob));
        ((JobResultImpl) itfJob.getJobResult()).addTaskResult("task2", new TaskResultImpl(TaskIdImpl
                .nextId(itfJob.getJobInfo().getJobId()), "salut", null, 1, null), true);
        //register the job
        DatabaseManager.getInstance().register(itfJob);
        //list of internal job to recover
        log("Test  READ");
        List<InternalJob> recovering = DatabaseManager.getInstance().recoverAllJobs();
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
        for (InternalTask it : itfJob.getITasks()) {
            if (it.getName().equals("task1")) {
                Assert.assertEquals(it.getName(), "task1");
                Assert.assertEquals(it.isCancelJobOnError(), false);
                Assert.assertEquals(it.isPreciousResult(), false);
                Assert.assertEquals(it.getRestartTaskOnError(), RestartMode.ANYWHERE);
                Assert.assertEquals(it.getMaxNumberOfExecution(), 1);
                Assert.assertEquals(it.getDescription(), "Parallel Tasks - Task 1");
                Assert.assertEquals(false, it.getSelectionScripts().get(0).isDynamic());
                Assert.assertNotNull(it.getSelectionScripts().get(0).getScript());
                Assert.assertEquals(1, it.getSelectionScripts().get(0).getParameters().length);
                Assert.assertEquals("paquit", it.getSelectionScripts().get(0).getParameters()[0]);
                Assert.assertTrue(it.getPreScript().getScript().contains("Beginning of Pre-Script"));
                Assert.assertTrue(it.getPostScript().getScript().contains(
                        "Content is equals to " + URLbegin + "samples/scripts/misc/unset.js"));
                Assert.assertNull(it.getPostScript().getParameters());
                Assert.assertTrue(it.getCleaningScript().getScript().contains("Beginning of clean script"));
                Assert.assertNull(it.getCleaningScript().getParameters());
                Assert.assertNull(it.getDependences());
                Assert.assertEquals(it.getNumberOfNodesNeeded(), 1);
                Assert.assertNull(it.getResultPreview());
                Assert.assertEquals(it.getWallTime(), 12 * 1000);
                Assert.assertEquals(it.isWallTimeSet(), true);
                Assert.assertEquals(it.getGenericInformations().size(), 0);
                Assert.assertNull(it.getExecutableContainer());
                DatabaseManager.getInstance().load(it);
                Field f = JavaExecutableContainer.class.getDeclaredField("serializedArguments");
                f.setAccessible(true);
                Assert.assertEquals(ByteToObjectConverter.ObjectStream
                        .convert(((Map<String, ByteArrayWrapper>) f.get(it.getExecutableContainer())).get(
                                "sleepTime").getByteArray()), "1");
                Assert.assertEquals(ByteToObjectConverter.ObjectStream
                        .convert(((Map<String, ByteArrayWrapper>) f.get(it.getExecutableContainer())).get(
                                "number").getByteArray()), "1");
                f = JavaExecutableContainer.class.getDeclaredField("userExecutableClassName");
                f.setAccessible(true);
                Assert.assertEquals((String) f.get(it.getExecutableContainer()),
                        "org.ow2.proactive.scheduler.examples.WaitAndPrint");
                Assert.assertTrue(it.getExecutableContainer() instanceof ForkedJavaExecutableContainer);
                Assert.assertNull(((ForkedJavaExecutableContainer) it.getExecutableContainer())
                        .getForkEnvironment());
            } else if (it.getName().equals("task2")) {
                //Check task 2 properties
                Assert.assertEquals(it.getName(), "task2");
                Assert.assertEquals(it.isCancelJobOnError(), true);
                Assert.assertEquals(it.isPreciousResult(), false);
                Assert.assertEquals(it.getRestartTaskOnError(), RestartMode.ELSEWHERE);
                Assert.assertEquals(it.getMaxNumberOfExecution(), 2);
                Assert.assertEquals(it.getDescription(), "Parallel Tasks - Task 2");
                Assert.assertNull(it.getSelectionScripts());
                Assert.assertTrue(it.getPreScript().getScript().contains("Beginning of Pre-Script"));
                Assert.assertNull(it.getPostScript());
                Assert.assertNull(it.getCleaningScript());
                Assert.assertNull(it.getDependences());
                Assert.assertEquals(it.getNumberOfNodesNeeded(), 1);
                Assert.assertNull(it.getResultPreview());
                Assert.assertEquals(it.getWallTime(), 0);
                Assert.assertEquals(it.isWallTimeSet(), false);
                Assert.assertEquals(it.getGenericInformations().size(), 0);
                Assert.assertNull(it.getExecutableContainer());
                DatabaseManager.getInstance().load(it);
                Field f = JavaExecutableContainer.class.getDeclaredField("serializedArguments");
                f.setAccessible(true);
                Assert.assertEquals(ByteToObjectConverter.ObjectStream
                        .convert(((Map<String, ByteArrayWrapper>) f.get(it.getExecutableContainer())).get(
                                "sleepTime").getByteArray()), "12");
                Assert.assertEquals(ByteToObjectConverter.ObjectStream
                        .convert(((Map<String, ByteArrayWrapper>) f.get(it.getExecutableContainer())).get(
                                "number").getByteArray()), "21");
                Assert.assertEquals(ByteToObjectConverter.ObjectStream
                        .convert(((Map<String, ByteArrayWrapper>) f.get(it.getExecutableContainer())).get(
                                "test").getByteArray()), "/bin/java/jdk1.5");
                f = JavaExecutableContainer.class.getDeclaredField("userExecutableClassName");
                f.setAccessible(true);
                Assert.assertEquals((String) f.get(it.getExecutableContainer()),
                        "org.ow2.proactive.scheduler.examples.WaitAndPrint");
                Assert.assertTrue(it.getExecutableContainer() instanceof ForkedJavaExecutableContainer);
                Assert.assertEquals(((InternalJavaTask) it).isWallTimeSet(), false);
                Assert.assertEquals(((ForkedJavaExecutableContainer) it.getExecutableContainer())
                        .getForkEnvironment().getJavaHome(), "/bin/java/jdk1.5");
                Assert.assertEquals(((ForkedJavaExecutableContainer) it.getExecutableContainer())
                        .getForkEnvironment().getJVMArguments().get(0), "-dparam=12");
                Assert.assertEquals(((ForkedJavaExecutableContainer) it.getExecutableContainer())
                        .getForkEnvironment().getJVMArguments().get(1), "-djhome=/bin/java/jdk1.5");
            } else if (it.getName().equals("task3")) {
                //Check task 3 properties
                Assert.assertEquals(it.getName(), "task3");
                Assert.assertEquals(it.isCancelJobOnError(), true);
                Assert.assertEquals(it.isPreciousResult(), false);
                Assert.assertEquals(it.getRestartTaskOnError(), RestartMode.ELSEWHERE);
                Assert.assertEquals(it.getMaxNumberOfExecution(), 2);
                Assert.assertEquals(it.getDescription(), "Dependent Tasks - Task 3");
                Assert.assertNull(it.getSelectionScripts());
                Assert.assertNull(it.getPreScript());
                Assert.assertTrue(it.getPostScript().getScript().contains(
                        "Unsetting system property user.property1"));
                Assert.assertNull(it.getPostScript().getParameters());
                Assert.assertNull(it.getCleaningScript());
                Assert.assertEquals(it.getDependences().size(), 2);
                Assert.assertEquals(it.getDependences().get(0).getName(), "task1");
                Assert.assertEquals(it.getDependences().get(1).getName(), "task2");
                Assert.assertEquals(3, it.getNumberOfNodesNeeded());
                Assert.assertNull(it.getResultPreview());
                Assert.assertEquals(it.getWallTime(), 10 * 60 * 1000 + 53 * 1000);
                Assert.assertEquals(it.isWallTimeSet(), true);
                Assert.assertEquals(it.getGenericInformations().size(), 0);
                Assert.assertNull(it.getExecutableContainer());
                DatabaseManager.getInstance().load(it);
                Field f = NativeExecutableContainer.class.getDeclaredField("command");
                f.setAccessible(true);
                Assert.assertEquals(((String[]) f.get(it.getExecutableContainer())).length, 5);
                Assert.assertEquals(((String[]) f.get(it.getExecutableContainer()))[0], URLbegin +
                    "samples/jobs_descriptors/job_native_linux/nativTask");
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
                Assert.assertNull(it.getSelectionScripts());
                Assert.assertNull(it.getPreScript());
                Assert.assertNull(it.getPostScript());
                Assert.assertNull(it.getCleaningScript());
                Assert.assertEquals(it.getDependences().size(), 1);
                Assert.assertEquals(it.getDependences().get(0).getName(), "task3");
                Assert.assertEquals(10, it.getNumberOfNodesNeeded());
                Assert.assertEquals(it.getResultPreview(), "tadzaam");
                Assert.assertEquals(it.getWallTime(), 0);
                Assert.assertEquals(it.isWallTimeSet(), false);
                Assert.assertEquals(it.getGenericInformations().get("n11"), "v11");
                Assert.assertEquals(it.getGenericInformations().get("n22"), "v22");
                Assert.assertNull(it.getExecutableContainer());
                DatabaseManager.getInstance().load(it);
                Field f = NativeExecutableContainer.class.getDeclaredField("generated");
                f.setAccessible(true);
                Assert.assertEquals(((GenerationScript) f.get(it.getExecutableContainer())).getScript(),
                        "command=args[0]+\" 12\";\n");
                Assert.assertEquals(
                        ((GenerationScript) f.get(it.getExecutableContainer())).getParameters()[0], URLbegin +
                            "samples/jobs_descriptors/job_native_linux/nativTask");
                Assert.assertEquals(((GenerationScript) f.get(it.getExecutableContainer())).execute()
                        .getResult(), URLbegin + "samples/jobs_descriptors/job_native_linux/nativTask 12");
            }
        }
        //test job and task result
        Assert.assertEquals(1, itfJob.getJobResult().getAllResults().size());
        Assert.assertEquals(1, itfJob.getJobResult().getPreciousResults().size());
        Assert.assertEquals(0, itfJob.getJobResult().getExceptionResults().size());
        Assert.assertNotNull(itfJob.getJobResult().getResult("task2"));
        //before loading
        Assert.assertNull(itfJob.getJobResult().getResult("task2").value());
        DatabaseManager.getInstance().load(itfJob.getJobResult().getResult("task2"));
        //after loading
        Assert.assertNotNull(itfJob.getJobResult().getResult("task2").value());
        Assert.assertEquals("salut", itfJob.getJobResult().getResult("task2").value());
        Assert.assertNull(itfJob.getJobResult().getResult("task2").getException());
        log("Test UPDATE");
        //unload
        for (InternalTask it : itfJob.getITasks()) {
            DatabaseManager.getInstance().unload(it);
            Assert.assertEquals(it.getExecutableContainer(), null);
        }
        //load
        for (InternalTask it : itfJob.getITasks()) {

            DatabaseManager.getInstance().load(it);
            if (it.getName().equals("task1")) {
                Field f = JavaExecutableContainer.class.getDeclaredField("serializedArguments");
                f.setAccessible(true);

                Assert.assertEquals(ByteToObjectConverter.ObjectStream
                        .convert(((Map<String, ByteArrayWrapper>) f.get(it.getExecutableContainer())).get(
                                "sleepTime").getByteArray()), "1");
                Assert.assertEquals(ByteToObjectConverter.ObjectStream
                        .convert(((Map<String, ByteArrayWrapper>) f.get(it.getExecutableContainer())).get(
                                "number").getByteArray()), "1");
                f = JavaExecutableContainer.class.getDeclaredField("userExecutableClassName");
                f.setAccessible(true);
                Assert.assertEquals((String) f.get(it.getExecutableContainer()),
                        "org.ow2.proactive.scheduler.examples.WaitAndPrint");
            } else if (it.getName().equals("task2")) {
                Field f = JavaExecutableContainer.class.getDeclaredField("serializedArguments");
                f.setAccessible(true);
                Assert.assertEquals(ByteToObjectConverter.ObjectStream
                        .convert(((Map<String, ByteArrayWrapper>) f.get(it.getExecutableContainer())).get(
                                "sleepTime").getByteArray()), "12");
                Assert.assertEquals(ByteToObjectConverter.ObjectStream
                        .convert(((Map<String, ByteArrayWrapper>) f.get(it.getExecutableContainer())).get(
                                "number").getByteArray()), "21");
                Assert.assertEquals(ByteToObjectConverter.ObjectStream
                        .convert(((Map<String, ByteArrayWrapper>) f.get(it.getExecutableContainer())).get(
                                "test").getByteArray()), "/bin/java/jdk1.5");
                f = JavaExecutableContainer.class.getDeclaredField("userExecutableClassName");
                f.setAccessible(true);
                Assert.assertEquals((String) f.get(it.getExecutableContainer()),
                        "org.ow2.proactive.scheduler.examples.WaitAndPrint");
            } else if (it.getName().equals("task3")) {
                Field f = NativeExecutableContainer.class.getDeclaredField("command");
                f.setAccessible(true);
                Assert.assertEquals(((String[]) f.get(it.getExecutableContainer())).length, 5);
                Assert.assertEquals(((String[]) f.get(it.getExecutableContainer()))[0], URLbegin +
                    "samples/jobs_descriptors/job_native_linux/nativTask");
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
                            "samples/jobs_descriptors/job_native_linux/nativTask");
                Assert.assertEquals(((GenerationScript) f.get(it.getExecutableContainer())).execute()
                        .getResult(), URLbegin + "samples/jobs_descriptors/job_native_linux/nativTask 12");
            }
        }
        //update (1 task is enough)
        TaskInfoImpl infoMem = (TaskInfoImpl) itfJob.getTasks().get(0).getTaskInfo();
        TaskId id = infoMem.getTaskId();
        new Condition("taskId", ConditionComparator.EQUALS_TO, id);
        //check MEM vs DB instance
        TaskInfo infoDB = DatabaseManager.getInstance().recover(TaskInfo.class,
                new Condition("taskId", ConditionComparator.EQUALS_TO, id)).get(0);
        Assert.assertEquals(infoDB.getExecutionHostName(), infoMem.getExecutionHostName());
        Assert.assertEquals(infoDB.getFinishedTime(), infoMem.getFinishedTime());
        Assert.assertEquals(infoDB.getNumberOfExecutionLeft(), infoMem.getNumberOfExecutionLeft());
        Assert.assertEquals(infoDB.getNumberOfExecutionOnFailureLeft(), infoMem
                .getNumberOfExecutionOnFailureLeft());
        Assert.assertEquals(infoDB.getStartTime(), infoMem.getStartTime());
        Assert.assertEquals(infoDB.getStatus(), infoMem.getStatus());
        Assert.assertEquals(infoDB.getJobInfo().getJobId(), infoMem.getJobInfo().getJobId());
        //update MEM instance
        infoMem.setExecutionHostName("toto");
        infoMem.setStartTime(1142564);
        infoMem.setStatus(TaskStatus.RUNNING);
        infoMem.setTaskId(TaskIdImpl.nextId(id.getJobId()));
        //synchronize update with database
        DatabaseManager.getInstance().synchronize(infoMem);
        //re-check MEM vs DB instance
        infoDB = DatabaseManager.getInstance().recover(TaskInfo.class,
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
        Assert.assertEquals(infoDB.getJobInfo().getJobId(), infoMem.getJobInfo().getJobId());
        Assert.assertTrue(!infoDB.getTaskId().equals(infoMem.getTaskId()));
        log("Test DELETE");
        infoMem.setTaskId(id);
        DatabaseManager.getInstance().delete(itfJob);
        Assert.assertEquals(DatabaseManager.getInstance().recover(InternalJob.class).size(), 0);
        Assert.assertEquals(DatabaseManager.getInstance().recover(InternalTask.class).size(), 0);
        Assert.assertEquals(DatabaseManager.getInstance().recover(JobInfo.class).size(), 0);
        Assert.assertEquals(DatabaseManager.getInstance().recover(TaskInfo.class).size(), 0);
        Assert.assertEquals(DatabaseManager.getInstance().recover(JobId.class).size(), 0);
        Assert.assertEquals(DatabaseManager.getInstance().recover(TaskId.class).size(), 0);
        Assert.assertEquals(DatabaseManager.getInstance().recover(ForkEnvironment.class).size(), 0);
        Assert.assertEquals(DatabaseManager.getInstance().recover(JobEnvironment.class).size(), 0);
        Assert.assertEquals(DatabaseManager.getInstance().recover(BigString.class).size(), 0);
        Assert.assertEquals(DatabaseManager.getInstance().recover(BooleanWrapper.class).size(), 0);
        Assert.assertEquals(DatabaseManager.getInstance().recover(IntegerWrapper.class).size(), 0);
    }

    @After
    public void after() throws Exception {
        DatabaseManager.getInstance().close();
    }

    private void log(String s) {
        System.out.println("------------------------------ " + s);
    }

}
