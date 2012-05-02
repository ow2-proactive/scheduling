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
import org.apache.jmeter.samplers.SampleResult;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.ForkEnvironment;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.NativeTask;
import org.ow2.proactive.scheduler.common.task.Task;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scripting.SelectionScript;
import org.ow2.proactive.tests.performance.deployment.TestDeployer;
import org.ow2.proactive.tests.performance.scheduler.JobWaitContition;
import org.ow2.proactive.tests.performance.scheduler.SchedulerEventsMonitor;
import org.ow2.proactive.tests.performance.scheduler.SchedulerTestListener;
import org.ow2.proactive.tests.performance.scheduler.SchedulerWaitCondition;
import org.ow2.proactive.tests.performance.scheduler.TestSchedulerProxy;
import org.ow2.proactive.tests.performance.utils.TestUtils;


/**
 * Base abstract class for the group of JavaSamplers executing the same
 * scenario: submit some job to the Scheduler and wait when job execution
 * completes (tests measure time required to execute Scheduler.submit). All
 * these scenarios differ only in the type of the submitted job, and subclasses
 * should implement method 'createJob' which creates scenario-specific job.
 * 
 * @author ProActive team
 * 
 */
public abstract class BaseJobSubmitClient extends BaseJMeterSchedulerClient {

    public static final String PARAM_SUBMIT_USE_SELECTION_SCRIPT = "submitUseSelectionScript";

    public static final String PARAM_SUBMIT_SELECTION_SCRIPT_DYNAMIC_CONTENT = "submitSelectionScriptDynamicContent";

    public static final String PARAM_SUBMIT_SELECTION_SCRIPT_TYPE_DYNAMIC = "submitSelectionScriptTypeDynamic";

    public static final long DEFAULT_JOB_COMPLETE_TIMEOUT = 5 * 60000;

    private SchedulerEventsMonitor eventsMonitor;

    private SelectionScript selectionScript;

    @Override
    public Arguments getDefaultParameters() {
        Arguments args = super.getDefaultParameters();
        args.addArgument(PARAM_SUBMIT_USE_SELECTION_SCRIPT, "${submitUseSelectionScript}");
        args.addArgument(PARAM_SUBMIT_SELECTION_SCRIPT_DYNAMIC_CONTENT,
                "${submitSelectionScriptDynamicContent}");
        args.addArgument(PARAM_SUBMIT_SELECTION_SCRIPT_TYPE_DYNAMIC, "${submitSelectionScriptTypeDynamic}");
        return args;
    }

    @Override
    protected void doSetupTest(JavaSamplerContext context) throws Throwable {
        super.doSetupTest(context);

        eventsMonitor = new SchedulerEventsMonitor();
        SchedulerTestListener listener = SchedulerTestListener.createListener(eventsMonitor);
        getScheduler().addEventListener(listener, true);

        if (getBooleanParameter(context, PARAM_SUBMIT_USE_SELECTION_SCRIPT)) {
            selectionScript = TestUtils.createSimpleSelectionScript(true, getBooleanParameter(context,
                    PARAM_SUBMIT_SELECTION_SCRIPT_DYNAMIC_CONTENT), getBooleanParameter(context,
                    PARAM_SUBMIT_SELECTION_SCRIPT_TYPE_DYNAMIC));
        } else {
            selectionScript = null;
        }
    }

    @Override
    protected void doTeardownTest(JavaSamplerContext context) throws Exception {
        getScheduler().removeEventListener();

        super.doTeardownTest(context);
    }

    @Override
    protected SampleResult doRunTest(JavaSamplerContext context) throws Throwable {
        TestSchedulerProxy scheduler = getScheduler();

        String jobName = generateUniqueJobName();
        TaskFlowJob job = createJob(jobName);
        boolean useSelectionScript = selectionScript != null;
        if (useSelectionScript) {
            for (Task task : job.getTasks()) {
                task.setSelectionScript(selectionScript);
            }
        }

        SchedulerWaitCondition waitCondition = eventsMonitor.addWaitCondition(new JobWaitContition(jobName));

        SampleResult result = new SampleResult();
        result.sampleStart();
        JobId jobId = scheduler.submit(job);
        result.sampleEnd();

        logInfo(String.format("Submitted job (id: %s): %s, selectionScript: %s (%s)", job.getDescription(),
                jobId.toString(), String.valueOf(useSelectionScript), Thread.currentThread().toString()));

        boolean waitOK = eventsMonitor.waitFor(waitCondition, getJobCompleteTimeout(), getLogger());

        JobResult jobResult = scheduler.getJobResult(jobId);
        if (waitOK) {
            checkJobResult(jobResult, result);
        } else {
            result.setSuccessful(false);
            logError("Job " + jobId + " was submitted, but didn't finish as expected");
            result.setResponseMessage("Job was submitted, but didn't finish as expected");
        }

        if (!result.isSuccessful()) {
            if (jobResult != null) {
                logError("Job execution failed (" + jobId + "), job result:");
                logJobResult(jobResult);
            } else {
                logError("Job execution failed and job result isn't available, job: " + jobId);
            }
        }

        return result;
    }

    protected void checkJobResult(JobResult jobResult, SampleResult result) throws Exception {
        boolean hasErrors = false;
        for (TaskResult taskResult : jobResult.getAllResults().values()) {
            if (taskResult.hadException()) {
                logError("Task " + taskResult.getTaskId() + " had exception, job: " + jobResult.getJobId());
                hasErrors = true;
                break;
            }
        }
        result.setSuccessful(!hasErrors);
        result.setResponseMessage("Some job tasks had exceptions");
    }

    protected long getJobCompleteTimeout() {
        return DEFAULT_JOB_COMPLETE_TIMEOUT;
    }

    protected final JavaTask createSimpleJavaTask(boolean fork) {
        JavaTask task = new JavaTask();
        task.setExecutableClassName(SimpleJavaTask.class.getName());
        task.setMaxNumberOfExecution(1);
        task.setCancelJobOnError(true);

        if (fork) {
            ForkEnvironment forkEnv = new ForkEnvironment();
            task.setForkEnvironment(forkEnv);
            forkEnv.addJVMArgument("-D" + TestDeployer.TEST_JVM_OPTION);
        }

        return task;
    }

    protected final NativeTask createSimpleNativeTask() {
        NativeTask task = new NativeTask();
        task.setCommandLine(new String[] { testsSourcePath +
            "/org/ow2/proactive/tests/performance/jmeter/scheduler/nativeTask.sh" });
        task.setMaxNumberOfExecution(1);
        task.setCancelJobOnError(true);
        return task;
    }

    protected Task createSimpleTask(TaskType taskType, String name) {
        Task task;
        switch (taskType) {
            case java_task:
                task = createSimpleJavaTask(true);
                break;
            case native_task:
                task = createSimpleNativeTask();
                break;
            default:
                throw new IllegalArgumentException("Invalid task type: " + taskType);
        }
        task.setName(name);

        return task;
    }

    protected abstract TaskFlowJob createJob(String jobName) throws Exception;

}
