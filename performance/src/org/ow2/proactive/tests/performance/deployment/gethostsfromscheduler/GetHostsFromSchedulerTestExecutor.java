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
package org.ow2.proactive.tests.performance.deployment.gethostsfromscheduler;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import org.objectweb.proactive.api.PAActiveObject;
import org.ow2.proactive.authentication.crypto.CredData;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.SchedulerAuthenticationInterface;
import org.ow2.proactive.scheduler.common.SchedulerConnection;
import org.ow2.proactive.scheduler.common.job.JobEnvironment;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobPriority;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.ForkEnvironment;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.ParallelEnvironment;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.util.logforwarder.LogForwardingService;
import org.ow2.proactive.scheduler.common.util.logforwarder.providers.ProActiveBasedForwardingProvider;
import org.ow2.proactive.tests.performance.deployment.TestEnv;
import org.ow2.proactive.tests.performance.scheduler.JobWaitContition;
import org.ow2.proactive.tests.performance.scheduler.SchedulerEventsMonitor;
import org.ow2.proactive.tests.performance.scheduler.SchedulerTestListener;
import org.ow2.proactive.tests.performance.scheduler.SchedulerWaitCondition;
import org.ow2.proactive.tests.performance.utils.TestUtils;
import org.ow2.proactive.topology.descriptor.TopologyDescriptor;


public class GetHostsFromSchedulerTestExecutor extends Thread {

    public static final long SCHEDULER_CONNECT_TIMEOUT = 60000;

    private final String schedulerUrl;

    private final CredData credData;

    private final String targetToRun;

    private final Integer rmHostsNumber;

    private final TestEnv rmNodesEnv;

    private final int createNodeSorceHostsNumber;

    private boolean executedSuccessfully;

    public GetHostsFromSchedulerTestExecutor(String schedulerUrl, TestEnv rmNodesEnv, int rmHostsNumber,
            int createNodeSorceHostsNumber, CredData credData, String targetToRun) {
        this.rmNodesEnv = rmNodesEnv;
        this.rmHostsNumber = rmHostsNumber;
        this.createNodeSorceHostsNumber = createNodeSorceHostsNumber;
        this.schedulerUrl = schedulerUrl;
        this.credData = credData;
        this.targetToRun = targetToRun;
    }

    private Scheduler connectToScheduler() throws Exception {
        System.out.println("Connecting to the scheduler: " + schedulerUrl);

        SchedulerAuthenticationInterface auth = SchedulerConnection.waitAndJoin(schedulerUrl,
                SCHEDULER_CONNECT_TIMEOUT);
        Credentials cred = Credentials.createCredentials(credData, auth.getPublicKey());
        Scheduler scheduler = auth.login(cred);

        System.out.println("Connected to the scheduler, status: " + scheduler.getStatus());

        return scheduler;
    }

    public void run() {
        executedSuccessfully = runTest();
    }

    public boolean isFinishedSuccessfully() {
        return executedSuccessfully;
    }

    private boolean runTest() {
        final String jobName = "RM/Scheduler tests job (" + new Date() + ")";
        final int hostsNumber = rmHostsNumber + createNodeSorceHostsNumber + 2;

        Scheduler scheduler = null;
        try {
            LogForwardingService lfs = new LogForwardingService(ProActiveBasedForwardingProvider.class
                    .getName());
            lfs.initialize();

            TaskCommunicationObject communicationObject = new TaskCommunicationObject();
            communicationObject = PAActiveObject.turnActive(communicationObject);
            String communicationObjectUrl = PAActiveObject.getUrl(communicationObject);

            System.out.println("Creating task requesting " + hostsNumber + " hosts");
            TaskFlowJob job = createTestExecutionJob(jobName, hostsNumber, communicationObjectUrl);

            scheduler = connectToScheduler();

            SchedulerEventsMonitor monitor = new SchedulerEventsMonitor();
            SchedulerTestListener listener = SchedulerTestListener.createListener(monitor);
            scheduler.addEventListener(listener, true);

            SchedulerWaitCondition waitCondition = monitor.addWaitCondition(new JobWaitContition(jobName));

            JobId jobId = scheduler.submit(job);
            System.out.println("Job was submitted: " + jobId);

            scheduler.listenJobLogs(jobId, lfs.getAppenderProvider());

            communicationObject.setCanRunTest();

            boolean ok = monitor.waitFor(waitCondition, Long.MAX_VALUE, null);

            JobResult result = scheduler.getJobResult(jobId);

            if (result == null) {
                System.out.println("No result for test job");
                return false;
            }

            System.out.println("Result for test job:");
            for (TaskResult taskResult : result.getAllResults().values()) {
                System.out.println("Task: " + taskResult.getTaskId());
                System.out.println("Task has exception: " + taskResult.getException());
                // System.out.println("Task std out:\n" + taskResult.getOutput().getStdoutLogs(true));
                // System.out.println("Task std err:\n" + taskResult.getOutput().getStderrLogs(true));
            }

            return ok;
        } catch (Throwable t) {
            System.out.println("Failed to run test: " + t);
            t.printStackTrace(System.out);
            return false;
        } finally {
            if (scheduler != null) {
                try {
                    scheduler.disconnect();
                } catch (Throwable t) {
                    System.out.println("Failed to disconnect from scheduler: " + t);
                }
            }
        }
    }

    private TaskFlowJob createTestExecutionJob(String jobName, int hostsNumber, String communicationObjectUrl)
            throws Exception {
        TaskFlowJob job = new TaskFlowJob();
        job.setName(jobName);
        job.setPriority(JobPriority.NORMAL);
        job.setCancelJobOnError(true);
        job.setDescription("Job executing RM/Scheduler tests with " + hostsNumber + " hosts");

        JobEnvironment je = new JobEnvironment();
        je.setJobClasspath(new String[] { rmNodesEnv.getSchedulingFolder().getPerformanceClassesDir()
                .getAbsolutePath() });
        job.setEnvironment(je);

        JavaTask task = new JavaTask();
        task.setExecutableClassName(TestExecutionTask.class.getName());
        task.setName("Tests execution");
        task.setDescription("Task executing RM/Scheduler tests with " + hostsNumber + " hosts");
        task.setMaxNumberOfExecution(1);
        task.setCancelJobOnError(true);

        ForkEnvironment forkEnv = new ForkEnvironment();
        forkEnv.addAdditionalClasspath(rmNodesEnv.getSchedulingFolder().getPerformanceClassesDir()
                .getAbsolutePath());
        task.setForkEnvironment(forkEnv);

        task.addArgument("rmHostsNumber", rmHostsNumber);
        task.addArgument("targetToRun", targetToRun);
        task
                .addArgument("antScriptPath", rmNodesEnv.getSchedulingFolder().getPerformanceDir() +
                    "/build.xml");
        task.addArgument("communicationObjectUrl", communicationObjectUrl);
        task.addArgument("jmeterhome", TestUtils.getRequiredProperty("get.hosts.from.scheduler.jmeterhome"));
        String antPath = System.getProperty("get.hosts.from.scheduler.antPath");
        if (antPath != null && !antPath.trim().isEmpty()) {
            task.addArgument("antPath", antPath);
        }

        System.out.println("Task arguments:");
        for (Map.Entry<String, Serializable> arg : task.getArguments().entrySet()) {
            System.out.println(arg.getKey() + "=" + arg.getValue());
        }

        ParallelEnvironment parallelEnv = new ParallelEnvironment(hostsNumber,
            TopologyDescriptor.DIFFERENT_HOSTS_EXCLUSIVE);
        task.setParallelEnvironment(parallelEnv);

        job.addTask(task);

        return job;
    }

    public static void main(String args[]) throws Exception {
        String url = TestUtils.getRequiredProperty("get.hosts.from.scheduler.schedulerUrl");
        String name = TestUtils.getRequiredProperty("get.hosts.from.scheduler.schedulerLogin");
        String password = TestUtils.getRequiredProperty("get.hosts.from.scheduler.schedulerPassword");
        String targetToRun = TestUtils.getRequiredProperty("get.hosts.from.scheduler.targetToRun");

        int rmHostsNumber = Integer.valueOf(TestUtils
                .getRequiredProperty("get.hosts.from.scheduler.rmHostsNumber"));
        int createNodeSorceHostsNumber = Integer.valueOf(TestUtils
                .getRequiredProperty("get.hosts.from.scheduler.rmAdditionalHostsNumber"));

        TestEnv rmNodesEnv = TestEnv.getEnvUsingSystemProperties("rmNodes");

        GetHostsFromSchedulerTestExecutor executor = new GetHostsFromSchedulerTestExecutor(url, rmNodesEnv,
            rmHostsNumber, createNodeSorceHostsNumber, new CredData(name, password), targetToRun);
        executor.start();
        executor.join();
        if (executor.isFinishedSuccessfully()) {
            System.out.println("Test finished successfully");
            System.exit(0);
        } else {
            System.out.println("Test execution failed, see logs for more details");
            System.exit(1);
        }
    }

}
