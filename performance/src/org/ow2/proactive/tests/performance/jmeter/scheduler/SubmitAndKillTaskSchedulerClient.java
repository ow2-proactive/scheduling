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

import java.util.ArrayList;
import java.util.List;

import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.job.JobEnvironment;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobPriority;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.job.JobStatus;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.Task;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.tests.performance.scheduler.JobWaitContition;
import org.ow2.proactive.tests.performance.scheduler.SchedulerEventsMonitor;
import org.ow2.proactive.tests.performance.scheduler.SchedulerTestListener;
import org.ow2.proactive.tests.performance.scheduler.SchedulerWaitCondition;
import org.ow2.proactive.tests.performance.scheduler.StartTaskWaitContition;


public class SubmitAndKillTaskSchedulerClient extends BaseJMeterSchedulerClient {

    static final long EXECUTION_START_TIMEOUT = 5 * 60000;

    static final long FINISH_TIMEOUT = 60000;

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

        SchedulerWaitCondition jobCompleteCondition = eventsMonitor.addWaitCondition(new JobWaitContition(
            jobName, JobStatus.FINISHED));

        TaskFlowJob job = new TaskFlowJob();
        job.setName(jobName);
        job.setPriority(JobPriority.NORMAL);
        job.setCancelJobOnError(false);
        job.setDescription("Job with java and native task (tasks sleep forever)");
        job.setMaxNumberOfExecution(1);
        JobEnvironment jobEnv = new JobEnvironment();
        jobEnv.setJobClasspath(new String[] { testsClasspath });
        job.setEnvironment(jobEnv);

        Task javaTask = SubmitAndKillSchedulerClient.createJavaSleepingTask();
        javaTask.setName("Sleeping java task");
        job.addTask(javaTask);

        Task nativeTask = SubmitAndKillSchedulerClient.createNativeSleepingTask(testsSourcePath);
        nativeTask.setName("Sleeping native task");
        job.addTask(nativeTask);

        List<SchedulerWaitCondition> taskStartConditions = new ArrayList<SchedulerWaitCondition>();
        for (Task task : job.getTasks()) {
            taskStartConditions.add(eventsMonitor.addWaitCondition(new StartTaskWaitContition(jobName, task
                    .getName())));
        }

        Scheduler scheduler = getScheduler();
        JobId jobId = scheduler.submit(job);

        logInfo("Killing tasks for job " + jobId + "(" + Thread.currentThread() + ")");

        for (SchedulerWaitCondition taskStartCondition : taskStartConditions) {
            if (!eventsMonitor.waitFor(taskStartCondition, EXECUTION_START_TIMEOUT, getLogger())) {
                logJobResult(jobId);
                SampleResult result = new SampleResult();
                result.setSuccessful(false);
                result.setResponseMessage("Failed to wait for start of task execition");
                return result;
            }
        }

        SampleResult result = new SampleResult();
        result.setSuccessful(true);
        result.sampleStart();

        for (Task task : job.getTasks()) {
            boolean killed = scheduler.killTask(jobId, task.getName());
            if (!killed && result.isSuccessful()) {
                result.setSuccessful(false);
                result.setResponseMessage("Failed to kill task " + task.getName());
            }
        }

        result.sampleEnd();

        if (!eventsMonitor.waitFor(jobCompleteCondition, FINISH_TIMEOUT, getLogger())) {
            if (result.isSuccessful()) {
                result.setSuccessful(false);
                result.setResponseMessage("Job with killed tasks didn't finish as expected");
            }
        }

        JobResult jobResult = scheduler.getJobResult(jobId);
        if (result.isSuccessful()) {
            for (TaskResult taskResult : jobResult.getAllResults().values()) {
                if (!taskResult.hadException()) {
                    result.setSuccessful(false);
                    result.setResponseMessage("Exception is expected for task " +
                        taskResult.getTaskId().getReadableName() + "");
                }
            }
        }

        if (!result.isSuccessful()) {
            if (jobResult != null) {
                logJobResult(jobResult);
            }
        }

        return result;
    }

}
