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
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.Task;
import org.ow2.proactive.tests.performance.jmeter.scheduler.BaseJobSubmitClient;


public class SubmitHugeJobSchedulerClient extends BaseJobSubmitClient {

    public static final String PARAM_HUGE_JOB_TASKS_NUMBER = "hugeJobTasksNumber";

    public static final String PARAM_HUGE_JOB_TASKS_COMPLETE_TIMEOUT = "hugeJobCompleteTimeout";

    private int tasksNumber;

    private long jobCompleteTimeout;

    @Override
    public Arguments getDefaultParameters() {
        Arguments args = super.getDefaultParameters();
        args.addArgument(PARAM_HUGE_JOB_TASKS_NUMBER, "${hugeJobTasksNumber}");
        args.addArgument(PARAM_HUGE_JOB_TASKS_COMPLETE_TIMEOUT, "${hugeJobCompleteTimeout}");
        return args;
    }

    @Override
    protected void doSetupTest(JavaSamplerContext context) throws Throwable {
        super.doSetupTest(context);
        tasksNumber = context.getIntParameter(PARAM_HUGE_JOB_TASKS_NUMBER);
        jobCompleteTimeout = context.getLongParameter(PARAM_HUGE_JOB_TASKS_COMPLETE_TIMEOUT) * 60000;
    }

    @Override
    protected TaskFlowJob createJob(String jobName) throws Exception {
        TaskFlowJob job = new TaskFlowJob();
        job.setName(jobName);
        job.setDescription("Job with " + tasksNumber + " tasks, timeout " + jobCompleteTimeout);

        JobEnvironment jobEnv = new JobEnvironment();
        jobEnv.setJobClasspath(new String[] { testsClasspath });
        job.setEnvironment(jobEnv);

        for (int i = 0; i < tasksNumber; i++) {
            Task task;
            if (i % 2 == 0) {
                task = createSimpleJavaTask(true);
            } else {
                task = createSimpleNativeTask();
            }
            task.setName("SubmitHugeJobSchedulerClientTask-" + i);
            job.addTask(task);
        }

        return job;
    }

    @Override
    protected long getJobCompleteTimeout() {
        return jobCompleteTimeout;
    }

}
