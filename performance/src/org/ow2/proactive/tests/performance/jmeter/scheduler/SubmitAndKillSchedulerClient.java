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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $ACTIVEEON_INITIAL_DEV$
 */
package org.ow2.proactive.tests.performance.jmeter.scheduler;

import java.io.Serializable;

import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.job.JobEnvironment;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobPriority;
import org.ow2.proactive.scheduler.common.job.JobStatus;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.ForkEnvironment;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.executable.JavaExecutable;
import org.ow2.proactive.tests.performance.jmeter.scheduler.SimpleJavaJobSubmitClient.SimpleJavaTask;
import org.ow2.proactive.tests.performance.scheduler.JobWaitContition;
import org.ow2.proactive.tests.performance.scheduler.SchedulerEventsMonitor;
import org.ow2.proactive.tests.performance.scheduler.SchedulerTestListener;
import org.ow2.proactive.tests.performance.scheduler.StartTaskWaitContition;
import org.ow2.proactive.tests.performance.scheduler.SchedulerWaitCondition;


public class SubmitAndKillSchedulerClient extends BaseJMeterSchedulerClient {

    public static class SleepForeverJavaTask extends JavaExecutable {

        @Override
        public Serializable execute(TaskResult... results) throws Throwable {
            System.out.println("Task is going to sleep forever");
            while (true) {
                Thread.sleep(Long.MAX_VALUE);
            }
        }

    }

    private SchedulerEventsMonitor eventsMonitor;

    @Override
    protected void doSetupTest(JavaSamplerContext context) throws Throwable {
        super.doSetupTest(context);

        eventsMonitor = new SchedulerEventsMonitor();
        SchedulerTestListener listener = SchedulerTestListener.createListener(eventsMonitor);
        getScheduler().addEventListener(listener, true);
    }

    @Override
    protected void doTeardownTest(JavaSamplerContext context) throws Exception {
        getScheduler().removeEventListener();

        super.doTeardownTest(context);
    }

    @Override
    protected SampleResult doRunTest(JavaSamplerContext context) throws Throwable {
        String jobName = generateUniqueJobName();
        String taskName = "Task-SubmitAndKillSchedulerClient";

        SchedulerWaitCondition taskStartCondition = eventsMonitor
                .addWaitCondition(new StartTaskWaitContition(jobName, taskName));
        SchedulerWaitCondition jobCompleteCondition = eventsMonitor.addWaitCondition(new JobWaitContition(
            jobName, JobStatus.KILLED));

        TaskFlowJob job = createJob(jobName, taskName);
        Scheduler scheduler = getScheduler();
        JobId jobId = scheduler.submit(job);

        if (!eventsMonitor.waitFor(taskStartCondition, 60000)) {
            logJobResult(jobId);
            SampleResult result = new SampleResult();
            result.setSuccessful(false);
            result.setResponseMessage("Failed to wait for start of task execition");
            return result;
        }

        System.out.println("Killing job " + jobId + "(" + Thread.currentThread() + ")");

        SampleResult result = new SampleResult();
        result.sampleStart();
        boolean killed = scheduler.killJob(jobId);
        result.sampleEnd();
        if (!killed) {
            result.setSuccessful(false);
            result.setResponseMessage("Failed to kill job");
        } else {
            if (!eventsMonitor.waitFor(jobCompleteCondition, 60000)) {
                result.setSuccessful(false);
                result.setResponseMessage("Killed job didn't finish as expected");
            } else {
                result.setSuccessful(true);
            }
        }

        if (!result.isSuccessful()) {
            logJobResult(jobId);
        }

        return result;
    }

    private TaskFlowJob createJob(String jobName, String taskName) throws Exception {
        TaskFlowJob job = new TaskFlowJob();
        job.setName(jobName);
        job.setPriority(JobPriority.NORMAL);
        job.setCancelJobOnError(true);
        job.setDescription("Job with one java task (task sleeps forever)");
        job.setMaxNumberOfExecution(1);

        JobEnvironment jobEnv = new JobEnvironment();
        jobEnv.setJobClasspath(new String[] { testsClasspath });
        job.setEnvironment(jobEnv);

        JavaTask task = new JavaTask();
        task.setExecutableClassName(SimpleJavaTask.class.getName());
        task.setName(taskName);
        task.setDescription("Test java task, sleeps forever");
        task.setMaxNumberOfExecution(1);
        task.setCancelJobOnError(true);

        ForkEnvironment forkEnv = new ForkEnvironment();
        task.setForkEnvironment(forkEnv);

        job.addTask(task);

        return job;
    }

}
