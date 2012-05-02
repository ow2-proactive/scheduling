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

import java.io.File;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.objectweb.proactive.core.UniqueID;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.tests.performance.jmeter.BaseJMeterClient;
import org.ow2.proactive.tests.performance.scheduler.TestSchedulerProxy;


/**
 * Base JavaSampler for all samplers working with Scheduler (it handles
 * connection to the Scheduler and provides some common utility methods).
 * 
 * @author ProActive team
 * 
 */
public abstract class BaseJMeterSchedulerClient extends BaseJMeterClient {

    private TestSchedulerProxy schedulerProxy;

    public static final String PARAM_SCHEDULING_PATH = "schedulingPath";

    protected String schedulingPath;

    protected String testsClasspath;

    protected String testsSourcePath;

    @Override
    public Arguments getDefaultParameters() {
        Arguments args = super.getDefaultParameters();
        SchedulerConnectionParameters.getDefaultParameters(args);
        args.addArgument(PARAM_SCHEDULING_PATH, "${schedulingPath}");
        return args;
    }

    @Override
    protected void doSetupTest(JavaSamplerContext context) throws Throwable {
        schedulingPath = context.getParameter(PARAM_SCHEDULING_PATH);
        if (schedulingPath == null || schedulingPath.trim().isEmpty()) {
            throw new IllegalArgumentException(PARAM_SCHEDULING_PATH + " not specified");
        }
        File schedulingDir = new File(schedulingPath);
        if (!schedulingDir.isDirectory()) {
            throw new IllegalArgumentException("Invalid scheduling path: " + schedulingPath);
        }
        File testsClasspathDir = new File(schedulingPath, "classes/performance");
        if (!testsClasspathDir.isDirectory()) {
            throw new IllegalArgumentException("Can't find tests classpath: " +
                testsClasspathDir.getAbsolutePath());
        }
        testsClasspath = testsClasspathDir.getAbsolutePath();

        File testsSrcDir = new File(schedulingPath, "performance/src");
        if (!testsSrcDir.isDirectory()) {
            throw new IllegalArgumentException("Can't find tests sources: " + testsSrcDir.getAbsolutePath());
        }
        testsSourcePath = testsSrcDir.getAbsolutePath();

        SchedulerConnectionParameters parameters = new SchedulerConnectionParameters(context);
        logInfo(String.format("Connecting to the Scheduler (%s, %s, %s, %s)", parameters.getSchedulerUrl(),
                parameters.getSchedulerLogin(), parameters.getSchedulerPassword(), Thread.currentThread()));
        schedulerProxy = parameters.connectWithProxy(10000);
    }

    protected final TestSchedulerProxy getScheduler() {
        return schedulerProxy;
    }

    @Override
    public final void teardownTest(JavaSamplerContext context) {
        try {
            doTeardownTest(context);
            if (schedulerProxy != null) {
                schedulerProxy.disconnect();
            }
        } catch (Exception e) {
            logError("Teardown failed: " + e, e);
        }
    }

    protected void doTeardownTest(JavaSamplerContext context) throws Exception {
    }

    protected String generateUniqueJobName() {
        return getClass().getSimpleName() + ": " + new UniqueID().getCanonString();
    }

    protected void logJobResult(JobId jobId) throws Exception {
        logError("Trying to get result for job " + jobId);
        JobResult result = getScheduler().getJobResult(jobId);
        if (result != null) {
            logJobResult(result);
        } else {
            logError("No result for job " + jobId);
        }
    }

    protected void logJobResult(JobResult result) throws Exception {
        logError("Job result(" + result.getJobId() + "):");
        for (TaskResult r : result.getAllResults().values()) {
            logError("Task: " + r.getTaskId());
            if (r.getException() != null) {
                logError("Task exception: " + r.getException(), r.getException());
            }
            logError("SO:\n" + r.getOutput().getStdoutLogs(true));
            logError("SERR:\n" + r.getOutput().getStderrLogs(true));
        }
    }

}
