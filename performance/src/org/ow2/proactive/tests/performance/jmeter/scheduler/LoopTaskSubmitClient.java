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

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.ow2.proactive.scheduler.common.job.JobEnvironment;
import org.ow2.proactive.scheduler.common.job.JobPriority;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.Task;
import org.ow2.proactive.scheduler.common.task.flow.FlowScript;


/**
 * Scenario submits job with task executed in loop.
 * 
 */
public class LoopTaskSubmitClient extends BaseJobSubmitClient {

    public static final String LOOP_TASK_SUBMIT_ITERATIONS_NUMBER = "loopTasksSubmitIterationsNumber";

    public static final String LOOP_TASK_SUBMIT_TASK_TYPE = "loopTasksSubmitTaskType";

    private TaskType taskType;

    private int iterationsNumber;

    @Override
    public Arguments getDefaultParameters() {
        Arguments args = super.getDefaultParameters();
        args.addArgument(LOOP_TASK_SUBMIT_ITERATIONS_NUMBER, "${loopTasksSubmitIterationsNumber}");
        args.addArgument(LOOP_TASK_SUBMIT_TASK_TYPE, "${loopTasksSubmitTaskType}");
        return args;
    }

    @Override
    protected void doSetupTest(JavaSamplerContext context) throws Throwable {
        super.doSetupTest(context);

        String taskTypeParam = getRequiredParameter(context, LOOP_TASK_SUBMIT_TASK_TYPE);
        taskType = TaskType.valueOf(taskTypeParam);

        iterationsNumber = context.getIntParameter(LOOP_TASK_SUBMIT_ITERATIONS_NUMBER);
    }

    @Override
    protected TaskFlowJob createJob(String jobName) throws Exception {
        TaskFlowJob job = new TaskFlowJob();
        job.setName(jobName);
        job.setPriority(JobPriority.NORMAL);
        job.setCancelJobOnError(true);
        job.setDescription("Job executes tasks in the loop, iterations: " + iterationsNumber +
            "  (tasks exit immediately)");
        job.setMaxNumberOfExecution(1);

        JobEnvironment jobEnv = new JobEnvironment();
        jobEnv.setJobClasspath(new String[] { testsClasspath });
        job.setEnvironment(jobEnv);

        Task mainTask = createSimpleTask(taskType, "Loop main task");
        job.addTask(mainTask);

        String loopScript = String.format("if ($IT < %d) { loop = true; } else { loop = false; }",
                iterationsNumber);
        Task loopTask = createSimpleTask(taskType, "Loop task");
        loopTask.addDependence(mainTask);
        loopTask.setFlowScript(FlowScript.createLoopFlowScript(loopScript, loopTask.getName()));
        job.addTask(loopTask);

        return job;
    }

}
