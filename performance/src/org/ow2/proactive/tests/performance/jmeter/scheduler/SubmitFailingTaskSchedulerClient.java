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

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.ow2.proactive.scheduler.common.job.JobEnvironment;
import org.ow2.proactive.scheduler.common.job.JobPriority;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.ForkEnvironment;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.NativeTask;
import org.ow2.proactive.scheduler.common.task.RestartMode;
import org.ow2.proactive.scheduler.common.task.Task;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.executable.JavaExecutable;
import org.ow2.proactive.tests.performance.deployment.TestDeployHelper;


public class SubmitFailingTaskSchedulerClient extends BaseJobSubmitClient {

    public static class FailingJavaTask extends JavaExecutable {

        @Override
        public Serializable execute(TaskResult... results) throws Throwable {
            System.out.println("Task throws exception");
            throw new Exception("Test exception");
        }

    }

    public static final String PARAM_FAILING_TASK_SUBMIT_TASK_TYPE = "failingTaskSubmitTaskType";

    public static final String PARAM_FAILING_TASK_SUBMIT_RESTART_MODE = "failingTaskSubmitRestartMode";

    public static final int NUMBER_OF_EXECUTION = 3;

    private static final String taskName = "Task-SubmitFailingTaskSchedulerClient";

    private TaskType taskType;

    private RestartMode restartMode;

    @Override
    public Arguments getDefaultParameters() {
        Arguments args = super.getDefaultParameters();
        args.addArgument(PARAM_FAILING_TASK_SUBMIT_TASK_TYPE, "${failingTaskSubmitTaskType}");
        args.addArgument(PARAM_FAILING_TASK_SUBMIT_RESTART_MODE, "${failingTaskSubmitRestartMode}");
        return args;
    }

    @Override
    protected void doSetupTest(JavaSamplerContext context) throws Throwable {
        super.doSetupTest(context);

        String taskTypeParam = getRequiredParameter(context, PARAM_FAILING_TASK_SUBMIT_TASK_TYPE);
        taskType = TaskType.valueOf(taskTypeParam);

        String restartMode = getRequiredParameter(context, PARAM_FAILING_TASK_SUBMIT_RESTART_MODE);
        if (restartMode.equals("ANYWHERE")) {
            this.restartMode = RestartMode.ANYWHERE;
        } else if (restartMode.equals("ELSEWHERE")) {
            this.restartMode = RestartMode.ELSEWHERE;
        } else {
            throw new IllegalArgumentException("Invalid restart mode: " + restartMode);
        }
    }

    @Override
    protected TaskFlowJob createJob(String jobName) throws Exception {
        TaskFlowJob job = new TaskFlowJob();
        job.setName(jobName);
        job.setPriority(JobPriority.NORMAL);
        job.setCancelJobOnError(false);
        job.setDescription("Job with one task (task always fails), type: " + taskType + ", restart: " +
            restartMode);

        Task task;

        switch (taskType) {
            case java_task:
                task = createJavaTask();
                JobEnvironment jobEnv = new JobEnvironment();
                jobEnv.setJobClasspath(new String[] { testsClasspath });
                job.setEnvironment(jobEnv);
                break;
            case native_task:
                task = createNativeTask(testsSourcePath);
                break;
            default:
                throw new IllegalArgumentException("Invalid task type: " + taskType);
        }

        task.setName(taskName);
        task.setMaxNumberOfExecution(NUMBER_OF_EXECUTION);
        task.setCancelJobOnError(false);
        task.setRestartTaskOnError(restartMode);

        job.addTask(task);

        return job;
    }

    @Override
    protected void checkJobResult(JobResult jobResult, SampleResult result) throws Exception {
        if (jobResult.getAllResults().size() != 1) {
            result.setResponseMessage("One task result is expected instead of " +
                jobResult.getAllResults().size());
            return;
        }

        boolean error = false;

        for (int i = 0; i < NUMBER_OF_EXECUTION; i++) {
            TaskResult taskResult = getScheduler().getTaskResultFromIncarnation(jobResult.getJobId(),
                    taskName, i);
            if (taskType == TaskType.java_task) {
                if (taskResult.getException() == null) {
                    error = true;
                    result.setResponseMessage("Exception is expected in task result");
                    break;
                }
            }
        }

        result.setSuccessful(!error);
    }

    static Task createJavaTask() {
        JavaTask task = new JavaTask();
        task.setExecutableClassName(FailingJavaTask.class.getName());
        task.setDescription("Test java task, throws exception");
        ForkEnvironment forkEnv = new ForkEnvironment();
        forkEnv.addJVMArgument("-D" + TestDeployHelper.TEST_JVM_OPTION);
        task.setForkEnvironment(forkEnv);
        return task;
    }

    static Task createNativeTask(String testsSourcePath) {
        NativeTask task = new NativeTask();
        task.setCommandLine(new String[] { testsSourcePath +
            "/org/ow2/proactive/tests/performance/jmeter/scheduler/failing_nativeTask.sh" });
        task.setDescription("Test native task, exits with code 1");
        return task;
    }

}
