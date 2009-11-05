/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of 
 * 						   Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
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
 * If needed, contact us to obtain a release under GPL Version 2. 
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package functionaltests;

import java.io.File;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;
import junit.framework.Assert;

import org.ow2.proactive.scheduler.common.job.Job;
import org.ow2.proactive.scheduler.common.job.JobEnvironment;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.job.JobStatus;
import org.ow2.proactive.scheduler.common.job.factories.JobFactory;
import org.ow2.proactive.scheduler.common.task.TaskInfo;
import org.ow2.proactive.scheduler.common.task.TaskStatus;
import org.ow2.proactive.scheduler.common.task.executable.Executable;
import org.ow2.proactive.scheduler.common.task.executable.JavaExecutable;

import functionalTests.FunctionalTest;


/**
 * This class tests the job classpath feature.
 * It will first start the scheduler, then connect it and submit two different workers.
 * Also test the get[Job/Task]Result(String) methods.
 *
 * (test 1) submit the worker without classpath : must return a classNotFoundException
 * (test 2) submit the worker in classpath a : should return a value
 * (test 3) submit the worker in classpath b : should return a different value even if the scheduler already knows this class name.
 *
 * @author The ProActive Team
 * @date 18 Feb. 09
 * @since ProActive Scheduling 1.0
 */
public class TestJobClasspath extends FunctionalTest {

    private static String jobDescriptor = TestJobClasspath.class.getResource(
            "/functionaltests/descriptors/Job_Test_CP.xml").getPath();
    private static Integer firstValueToTest = 1;
    private static Integer SecondValueToTest = 2;

    /**
     * Tests start here.
     *
     * @throws Throwable any exception that can be thrown during the test.
     */
    @org.junit.Test
    public void run() throws Throwable {

        String taskName = "task1";

        String[] classPathes = createClasses();
        {
            SchedulerTHelper.log("Test 1 : Without classpath...");

            JobId id = SchedulerTHelper.submitJob(jobDescriptor);

            //this task should be faulty
            TaskInfo tInfo = SchedulerTHelper.waitForEventTaskFinished(id, taskName);
            Assert.assertEquals(TaskStatus.FAULTY, tInfo.getStatus());

            JobInfo jInfo = SchedulerTHelper.waitForEventJobFinished(id);

            Assert.assertEquals(JobStatus.FINISHED, jInfo.getStatus());
            JobResult jr = SchedulerTHelper.getJobResult(id);

            Assert.assertTrue(jr.hadException());
            Assert.assertEquals(1, jr.getAllResults().size());
            Assert.assertNotNull(jr.getResult("task1").getException());
        }

        {
            SchedulerTHelper.log("Test 2 : With classpath 1...");
            //job creation
            Job submittedJob = JobFactory.getFactory().createJob(jobDescriptor);
            JobEnvironment env = new JobEnvironment();
            env.setJobClasspath(new String[] { classPathes[0] });
            submittedJob.setEnvironment(env);

            //job submission
            JobId id = SchedulerTHelper.testJobSubmission(submittedJob);

            //get result
            JobResult jr = SchedulerTHelper.getJobResult(id);
            Assert.assertFalse(jr.hadException());
            Assert.assertEquals(1, jr.getAllResults().size());
            Assert.assertEquals(firstValueToTest, (Integer) jr.getResult(taskName).value());
        }

        {
            SchedulerTHelper.log("Test 3 : With classpath 2...");
            //job creation
            Job submittedJob = JobFactory.getFactory().createJob(jobDescriptor);
            JobEnvironment env = new JobEnvironment();
            env.setJobClasspath(new String[] { classPathes[1] });
            submittedJob.setEnvironment(env);

            //job submission
            JobId id = SchedulerTHelper.testJobSubmission(submittedJob);

            //check results
            JobResult jr = SchedulerTHelper.getJobResult(id);
            Assert.assertFalse(jr.hadException());
            Assert.assertEquals(1, jr.getAllResults().size());
            Assert.assertEquals(SecondValueToTest, (Integer) jr.getResult(taskName).value());
        }
    }

    /**
     * Create 2 classes with different return values in 2 different classPathes
     * and return the 2 created classPathes.
     *
     * @return the 2 created classPathes where to find the classes.
     * @throws Exception If the classes cannot be created
     */
    private String[] createClasses() throws Exception {
        String[] classPathes = new String[2];
        String className = "test.Worker";

        ClassPool pool = ClassPool.getDefault();
        //create new classes
        CtClass cc1 = pool.makeClass(className);
        CtClass cc2 = pool.makeClass(className);

        //get super-type and super-super-type
        CtClass upper = pool.get(JavaExecutable.class.getName());
        CtClass upupper = pool.get(Executable.class.getName());

        //get Executable 'execute' method
        CtMethod absExec = upupper.getMethod("execute",
                "([Lorg/ow2/proactive/scheduler/common/task/TaskResult;)Ljava/io/Serializable;");

        //set superclass of new classes
        cc1.setSuperclass(upper);
        cc2.setSuperclass(upper);

        //get a directory in the temp directory
        File tmpdir = new File(System.getProperty("java.io.tmpdir") + File.separator + "SchedTestJob_CP");

        // create first class
        CtMethod exec1 = CtNewMethod.make(pool.makeClass("java.io.Serializable"), absExec.getName(), absExec
                .getParameterTypes(), absExec.getExceptionTypes(), "return new java.lang.Integer(" +
            firstValueToTest + ");", cc1);
        cc1.addMethod(exec1);
        //create first classPath
        File f1 = new File(tmpdir.getAbsolutePath() + File.separator + firstValueToTest);
        f1.mkdirs();
        classPathes[0] = f1.getAbsolutePath();
        cc1.writeFile(classPathes[0]);

        // create second class
        CtMethod exec2 = CtNewMethod.make(pool.makeClass("java.io.Serializable"), absExec.getName(), absExec
                .getParameterTypes(), absExec.getExceptionTypes(), "return new java.lang.Integer(" +
            SecondValueToTest + ");", cc2);
        cc2.addMethod(exec2);
        //create second classPath
        File f2 = new File(tmpdir.getAbsolutePath() + File.separator + SecondValueToTest);
        f2.mkdirs();
        classPathes[1] = f2.getAbsolutePath();
        cc2.writeFile(classPathes[1]);

        return classPathes;
    }
}
