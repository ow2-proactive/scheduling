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


/**
 * Test scenario 'Submit dependent tasks' (it executes basic 'submit job'
 * scenario, see BaseJobSubmitClient for details).
 * <p/>
 * Scenario submits job which contains tasks with dependencies (tasks don't do
 * anything just sleep some time and finish). Job contains following tasks: one
 * 'first task', N 'dependent tasks' depending on the 'first task' and one 'last
 * task' depending on the all 'dependent tasks' (number of 'dependent tasks' is
 * configurable).
 * 
 * @author ProActive team
 * 
 */
public class DependentTasksSubmitClient extends BaseJobSubmitClient {

    public static final String PARAM_DEPENDENT_TASK_SUBMIT_TASKS_NUMBER = "dependentTasksSubmitTasksNumber";

    public static final String PARAM_DEPENDENT_TASK_SUBMIT_TASK_TYPE = "dependentTasksSubmitTaskType";

    private TaskType taskType;

    private int dependentTasksNumber;

    @Override
    public Arguments getDefaultParameters() {
        Arguments args = super.getDefaultParameters();
        args.addArgument(PARAM_DEPENDENT_TASK_SUBMIT_TASKS_NUMBER, "${dependentTasksSubmitTasksNumber}");
        args.addArgument(PARAM_DEPENDENT_TASK_SUBMIT_TASK_TYPE, "${dependentTasksSubmitTaskType}");
        return args;
    }

    @Override
    protected void doSetupTest(JavaSamplerContext context) throws Throwable {
        super.doSetupTest(context);

        String taskTypeParam = getRequiredParameter(context, PARAM_DEPENDENT_TASK_SUBMIT_TASK_TYPE);
        taskType = TaskType.valueOf(taskTypeParam);

        dependentTasksNumber = context.getIntParameter(PARAM_DEPENDENT_TASK_SUBMIT_TASKS_NUMBER);
    }

    @Override
    protected TaskFlowJob createJob(String jobName) throws Exception {
        TaskFlowJob job = new TaskFlowJob();
        job.setName(jobName);
        job.setPriority(JobPriority.NORMAL);
        job.setCancelJobOnError(true);
        job.setDescription("Job with " + dependentTasksNumber + " dependent tasks (tasks exit immediately)");
        job.setMaxNumberOfExecution(1);

        JobEnvironment jobEnv = new JobEnvironment();
        jobEnv.setJobClasspath(new String[] { testsClasspath });
        job.setEnvironment(jobEnv);

        Task mainTask = createSimpleTask(taskType, "Dependent tasks: main task");
        Task lastTask = createSimpleTask(taskType, "Dependent tasks: last task");

        for (int i = 0; i < dependentTasksNumber; i++) {
            Task task = createSimpleTask(taskType, "Dependent task-" + i);
            task.addDependence(mainTask);
            job.addTask(task);

            lastTask.addDependence(task);
        }

        job.addTask(mainTask);
        job.addTask(lastTask);

        return job;
    }

}
