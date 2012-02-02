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
import org.ow2.proactive.scheduler.common.job.JobPriority;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.ForkEnvironment;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.NativeTask;
import org.ow2.proactive.scheduler.common.task.ParallelEnvironment;
import org.ow2.proactive.scheduler.common.task.Task;
import org.ow2.proactive.tests.performance.jmeter.scheduler.SimpleJavaJobSubmitClient.SimpleJavaTask;
import org.ow2.proactive.tests.performance.utils.TestUtils;
import org.ow2.proactive.topology.descriptor.TopologyDescriptor;


public class MultiNodeJobSubmitClient extends BaseJobSubmitClient {

    public static final String PARAM_MULTI_NODE_SUBMIT_TASK_TYPE = "multiNodeSubmitTaskType";

    public static final String PARAM_MULTI_NODE_SUBMIT_NODES_NUMBER = "multiNodeSubmitNodesNumber";

    public static final String PARAM_MULTI_NODE_SUBMIT_TOPOLOGY = "multiNodeSubmitTopology";

    static enum TaskType {
        java_task, native_task
    }

    private TaskType taskType;

    private int nodesNumber;

    private TopologyDescriptor topology;

    @Override
    public Arguments getDefaultParameters() {
        Arguments args = super.getDefaultParameters();
        args.addArgument(PARAM_MULTI_NODE_SUBMIT_TASK_TYPE, "${multiNodeSubmitTaskType}");
        args.addArgument(PARAM_MULTI_NODE_SUBMIT_NODES_NUMBER, "${multiNodeSubmitNodesNumber}");
        args.addArgument(PARAM_MULTI_NODE_SUBMIT_TOPOLOGY, "${multiNodeSubmitTopology}");
        return args;
    }

    @Override
    protected void doSetupTest(JavaSamplerContext context) throws Throwable {
        super.doSetupTest(context);

        String taskTypeParam = getRequiredParameter(context, PARAM_MULTI_NODE_SUBMIT_TASK_TYPE);
        if (taskTypeParam == null) {
            throw new IllegalArgumentException("Task type not specified");
        }
        taskType = TaskType.valueOf(taskTypeParam);

        nodesNumber = context.getIntParameter(PARAM_MULTI_NODE_SUBMIT_NODES_NUMBER);

        topology = TestUtils.getTopologyDescriptor(context.getParameter(PARAM_MULTI_NODE_SUBMIT_TOPOLOGY));
    }

    @Override
    protected TaskFlowJob createJob(String jobName) throws Exception {
        TaskFlowJob job = new TaskFlowJob();
        job.setName(jobName);
        job.setPriority(JobPriority.NORMAL);
        job.setCancelJobOnError(true);
        job.setDescription("Multinode job with one task, task exits immediately (" + taskType + ", nodes: " +
            nodesNumber + ", topology: " + topology.getClass().getSimpleName() + ")");
        job.setMaxNumberOfExecution(1);

        Task task;
        switch (taskType) {
            case java_task:
                task = createJavaTask();
                break;
            case native_task:
                task = createNativeTask();
                break;
            default:
                throw new IllegalArgumentException("Invalid task type: " + taskType);
        }

        task.setDescription("Test multinode task, exits immediately");
        task.setMaxNumberOfExecution(1);
        task.setCancelJobOnError(true);
        task.setName("Test task");

        ParallelEnvironment parallelEnv = new ParallelEnvironment(nodesNumber, topology);
        task.setParallelEnvironment(parallelEnv);

        job.addTask(task);

        return job;
    }

    private Task createJavaTask() {
        JavaTask task = new JavaTask();
        task.setExecutableClassName(SimpleJavaTask.class.getName());

        ForkEnvironment forkEnv = new ForkEnvironment();
        forkEnv.addAdditionalClasspath(testsClasspath);
        task.setForkEnvironment(forkEnv);
        return task;
    }

    private Task createNativeTask() {
        NativeTask task = new NativeTask();
        task.setCommandLine(new String[] { testsSourcePath +
            "/org/ow2/proactive/tests/performance/jmeter/scheduler/nativeTask.sh" });
        return task;
    }
}
