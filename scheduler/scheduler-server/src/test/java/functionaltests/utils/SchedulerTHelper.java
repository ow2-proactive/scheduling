/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2015 INRIA/University of
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
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package functionaltests.utils;

import com.google.common.collect.ImmutableList;
import functionaltests.monitor.RMMonitorsHandler;
import functionaltests.monitor.SchedulerMonitorsHandler;
import org.junit.Assert;
import org.objectweb.proactive.core.ProActiveTimeoutException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.core.process.JVMProcessImpl;
import org.objectweb.proactive.extensions.pnp.PNPConfig;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.common.event.RMNodeEvent;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.resourcemanager.utils.RMNodeStarter;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.SchedulerAuthenticationInterface;
import org.ow2.proactive.scheduler.common.SchedulerEvent;
import org.ow2.proactive.scheduler.common.exception.UnknownJobException;
import org.ow2.proactive.scheduler.common.job.*;
import org.ow2.proactive.scheduler.common.job.factories.JobFactory;
import org.ow2.proactive.scheduler.common.task.TaskInfo;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.TaskState;
import org.ow2.proactive.scheduler.common.task.TaskStatus;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;


/**
 * Static helpers that provide main operations for Scheduler functional test.
 *
 * The Scheduler instance is static, in order to keep it running across tests if possible.
 * If a Scheduler is explicitly started with a configuration different than the running one
 * the Scheduler will kill and started with this new configuration.
 *
 * For waitForEvent**() methods, it acts as Producer-consumer mechanism ;
 * a Scheduler produce events that are memorized,
 * and waiting methods waitForEvent**() are consumers of these events.
 * It means that an event asked to be waited for by a call to waitForEvent**() methods, is removed
 * after its occurrence. On the contrary, an event is kept till a waitForEvent**() for this event
 * has been called.
 *
 * waitForTerminatedJob() method doesn't act as other waitForEvent**() Methods.
 * This method deduce a job finished from current Scheduler's job states and received event.
 * This method can also be used for testing for job submission with killing and restarting
 * Scheduler.
 *
 * @author ProActive team
 * @since ProActive Scheduling 1.0
 */
public class SchedulerTHelper {

    private static TestScheduler scheduler = new TestScheduler();

    public static final String extraNS = "extra";

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    scheduler.kill();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }));
    }


    // can be changed by starting the Scheduler manually
    private SchedulerTestConfiguration currentTestConfiguration = SchedulerTestConfiguration.defaultConfiguration();

    /**
     * Creates a test scheduler
     * @param restart if true then a new scheduler will be recreated
     * @throws Exception
     */
    public SchedulerTHelper(boolean restart) throws Exception {
        this(restart, false);
    }

    /**
     * Creates a test scheduler
     *
     * @param restart if true then a new scheduler will be recreated
     * @param emptyRM if true then a the scheduler must have zero node
     * @throws Exception
     */
    public SchedulerTHelper(boolean restart, boolean emptyRM) throws Exception {
        if (emptyRM) {
            startScheduler(restart, SchedulerTestConfiguration.emptyResourceManager());
        } else {
            startScheduler(restart, SchedulerTestConfiguration.defaultConfiguration());
        }
    }

    /**
     * Creates a test scheduler with a given configuration
     *
     * @param restart       if true then a new scheduler will be recreated
     * @param configuration configuration to use
     * @throws Exception
     */
    public SchedulerTHelper(boolean restart, SchedulerTestConfiguration configuration) throws Exception {
        startScheduler(restart, configuration);
    }

    /**
     * Creates a test scheduler with a given configuration
     *
     * @param configuration the Scheduler configuration file to use (default is
     *                      functionalTSchedulerProperties.ini)
     * 			null to use the default one.
     * @throws Exception if an error occurs.
     */
    public SchedulerTHelper(boolean restart, String configuration) throws Exception {
        startScheduler(restart, SchedulerTestConfiguration.customSchedulerConfig(configuration));
    }

    /**
     * Creates a test scheduler with a given configuration and empty RM
     *
     * @param configuration the Scheduler configuration file to use (default is
     *                      functionalTSchedulerProperties.ini)
     *                      null to use the default one.
     * @throws Exception if an error occurs.
     */
    public SchedulerTHelper(boolean restart, boolean emptyRM, String configuration) throws Exception {
        if (emptyRM) {
            startScheduler(restart, SchedulerTestConfiguration.emptyRMandCustomSchedulerConfig(configuration));
        } else {
            startScheduler(restart, SchedulerTestConfiguration.customSchedulerConfig(configuration));
        }
    }

    /**
     * Creates a test scheduler with a given number of nodes,
     * and given scheduler and resource manager configurations
     * @param localnodes number of nodes to create
     * @param schedPropertiesFilePath configuration file for the scheduler
     * @param rmPropertiesFilePath configuration file for the rm
     * @param rmUrl url of the resource manager
     * @throws Exception
     */
    public SchedulerTHelper(boolean localnodes, String schedPropertiesFilePath,
                            String rmPropertiesFilePath, String rmUrl) throws Exception {
        startScheduler(localnodes, schedPropertiesFilePath, rmPropertiesFilePath, rmUrl);
    }


    private void startScheduler(String configuration) throws Exception {
        startScheduler(true, SchedulerTestConfiguration.customSchedulerConfig(configuration));
    }


    private void startScheduler(boolean localnodes, String schedPropertiesFilePath,
            String rmPropertiesFilePath, String rmUrl) throws Exception {
        SchedulerTestConfiguration configuration = new SchedulerTestConfiguration(schedPropertiesFilePath,
            rmPropertiesFilePath, localnodes, TestScheduler.PNP_PORT, rmUrl);
        startScheduler(true, configuration);
    }

    private void startScheduler(boolean restart, SchedulerTestConfiguration configuration) throws Exception {
        if (restart || !scheduler.isStartedWithSameConfiguration(configuration)) {
            log("Kill previous Scheduler and alive connexions");
            killScheduler();
            log("Starting Scheduler");
            scheduler.start(configuration);
            RMTestUser.getInstance().connect(TestUsers.DEMO, scheduler.getRMUrl());
        }
        currentTestConfiguration = configuration;
    }

    public boolean isStarted() {
        return scheduler.isStarted();
    }

    /**
     * Kill the forked Scheduler if exists.
     */
    public void killScheduler() throws Exception {

        SchedulerTestUser.getInstance().schedulerIsRestarted();
        RMTestUser.getInstance().disconnectFromRM();
        log("Killing scheduler process");
        scheduler.kill();
    }

    /**
     * Restart the scheduler using a forked JVM and all children Nodes.
     * User or administrator interface is not reconnected automatically.
     *
     * @param configuration the Scheduler configuration file to use (default is
     *                      functionalTSchedulerProperties.ini)
     * 			null to use the default one.
     * @throws Exception
     */
    public void killSchedulerAndNodesAndRestart(String configuration) throws Exception {
        killScheduler();
        startScheduler(configuration);
    }

    /**
     * Log a String on console.
     */
    public static void log(String s) {
        System.out.println("------------------------------ " + s);
    }

    public static void log(Exception e) {
        e.printStackTrace();
    }

    /**
     * Return Scheduler authentication interface. Start Scheduler with test
     * configuration file, if scheduler is not yet started.
     * @throws Exception
     */
    public SchedulerAuthenticationInterface getSchedulerAuth() throws Exception {
        return scheduler.getAuth();
    }

    /**
     * Return Scheduler's interface. Start Scheduler if needed,
     * connect as administrator if needed (if not yet connected as user).
     *
     * WARNING : if there was a previous connection as User, this connection is shut down.
     * And so some event can be missed by event receiver, between disconnection and reconnection
     * (only one connection to Scheduler per body is possible).
     *
     * @return scheduler interface
     * @throws Exception if an error occurs.
     */
    public Scheduler getSchedulerInterface() throws Exception {
        getResourceManager(); // get ready to receive RM events as well
        return getSchedulerInterface(TestUsers.DEMO);
    }

    public Scheduler getSchedulerInterface(UserType userType) throws Exception {
        switch (userType) {
            case ADMIN:
                return getSchedulerInterface(TestUsers.ADMIN);
            case USER:
                return getSchedulerInterface(TestUsers.DEMO);
        }
        return null;
    }

    /**
     * Return Scheduler's interface. Start Scheduler if needed,
     * connect as administrator if needed (if not yet connected as user).
     *
     * WARNING : if there was a previous connection as User, this connection is shut down.
     * And so some event can be missed by event receiver, between disconnection and reconnection
     * (only one connection to Scheduler per body is possible).
     *
     * @param user Type of user
     * @return scheduler interface
     * @throws Exception if an error occurs.
     */
    public Scheduler getSchedulerInterface(TestUsers user) throws Exception {


        if (!SchedulerTestUser.getInstance().is(user)) { // changing user on the fly
            SchedulerTestUser.getInstance().disconnectFromScheduler();
            SchedulerTestUser.getInstance().connect(user, scheduler.getUrl());
        }

        if (!SchedulerTestUser.getInstance().isConnected()) {
            SchedulerTestUser.getInstance().connect(user, scheduler.getUrl());
        }

        return SchedulerTestUser.getInstance().getScheduler();
    }

    /**
     * Submit a job, and return immediately.
     * Connect as user if needed (if not yet connected as user).
     * @param jobToSubmit job object to schedule.
     * @return JobId the job's identifier corresponding to submission.
     * @throws Exception if an error occurs at job submission.
     */
    public JobId submitJob(Job jobToSubmit) throws Exception {
        Scheduler userInt = getSchedulerInterface();
        return userInt.submit(jobToSubmit);
    }

    public JobId submitJob(String jobDescPath) throws Exception {
        Job jobToSubmit = JobFactory.getFactory().createJob(jobDescPath);
        Scheduler userInt = getSchedulerInterface();
        return userInt.submit(jobToSubmit);
    }

    /**
     * Submits a job with a list of variables
     *
     * @param jobDescPath
     * @param variables
     * @return
     * @throws Exception
     */
    public JobId submitJob(String jobDescPath, Map<String, String> variables) throws Exception {
        Job jobToSubmit = JobFactory.getFactory().createJob(jobDescPath, variables);
        Scheduler userInt = getSchedulerInterface();
        return userInt.submit(jobToSubmit);
    }

    /**
     * Kills a job
     * @return success or failure at killing the job
     * @throws Exception
     */
    public boolean killJob(String jobId) throws Exception {
        Scheduler userInt = getSchedulerInterface();
        return userInt.killJob(jobId);
    }

    /**
     * Kills a tzsk
     *
     * @return success or failure at killing the task
     * @throws Exception
     */
    public boolean killTask(String jobId, String taskName) throws Exception {
        Scheduler userInt = getSchedulerInterface();
        return userInt.killTask(jobId, taskName);
    }

    /**
     * Remove a job from Scheduler database.
     * connect as user if needed (if not yet connected as user).
     * @param id of the job to remove from database.
     * @throws Exception if an error occurs at job removal.
     */
    public void removeJob(JobId id) throws Exception {
        Scheduler userInt = getSchedulerInterface();
        userInt.removeJob(id);
    }


    /**
     * Creates and submit a job from an XML job descriptor, and check, with assertions,
     * event related to this job submission :
     * 1/ job submitted event
     * 2/ job passing from pending to running (with state set to running).
     * 3/ job passing from running to finished (with state set to finished).
     * 4/ every task finished without error
     *
     * Then returns.
     *
     * This is the simplest events sequence of a job submission. If you need to test
     * specific events or task states (failures, rescheduling etc, you must not use this
     * helper and check events sequence with waitForEvent**() functions.
     *
     * @param jobDescPath path to an XML job descriptor to submit
     * @return JobId, the job's identifier.
     * @throws Exception if an error occurs at job creation/submission, or during
     * verification of events sequence.
     */
    public JobId testJobSubmission(String jobDescPath) throws Exception {
        Job jobToTest = JobFactory.getFactory().createJob(jobDescPath);
        return testJobSubmission(jobToTest, false);
    }

    /**
     * Creates and submit a job from an XML job descriptor, and check, with assertions,
     * event related to this job submission :
     * 1/ job submitted event
     * 2/ job passing from pending to running (with state set to running).
     * 3/ job passing from running to finished (with state set to finished).
     * 4/ every task finished without error
     * <p>
     * Then returns.
     * <p>
     * This is the simplest events sequence of a job submission. If you need to test
     * specific events or task states (failures, rescheduling etc, you must not use this
     * helper and check events sequence with waitForEvent**() functions.
     *
     * @param jobDescPath   path to an XML job descriptor to submit
     * @param acceptSkipped if true then skipped task will not fail the test
     * @return JobId, the job's identifier.
     * @throws Exception if an error occurs at job creation/submission, or during
     *                   verification of events sequence.
     */
    public JobId testJobSubmission(String jobDescPath, boolean acceptSkipped) throws Exception {
        Job jobToTest = JobFactory.getFactory().createJob(jobDescPath);
        return testJobSubmission(jobToTest, acceptSkipped);
    }

    /**
     * Creates and submit a job from an XML job descriptor, and check, with assertions,
     * event related to this job submission :
     * 1/ job submitted event
     * 2/ job passing from pending to running (with state set to running).
     * 3/ job passing from running to finished (with state set to finished).
     * 4/ every task finished without error
     * <p>
     * Then returns.
     * <p>
     * This is the simplest events sequence of a job submission. If you need to test
     * specific events or task states (failures, rescheduling etc, you must not use this
     * helper and check events sequence with waitForEvent**() functions.
     *
     * @param jobToSubmit job object to schedule.
     * @return JobId, the job's identifier.
     * @throws Exception if an error occurs at job submission, or during
     *                   verification of events sequence.
     */
    public JobId testJobSubmission(Job jobToSubmit) throws Exception {
        return testJobSubmission(jobToSubmit, false);
    }

    /**
     * Creates and submit a job from an XML job descriptor, and check, with assertions,
     * event related to this job submission :
     * 1/ job submitted event
     * 2/ job passing from pending to running (with state set to running).
     * 3/ job passing from running to finished (with state set to finished).
     * 4/ every task finished without error
     *
     * Then returns.
     *
     * This is the simplest events sequence of a job submission. If you need to test
     * specific events or task states (failures, rescheduling etc, you must not use this
     * helper and check events sequence with waitForEvent**() functions.
     *
     * @param jobToSubmit job object to schedule.
     * @param acceptSkipped if true then skipped task will not fail the test
     * @return JobId, the job's identifier.
     * @throws Exception if an error occurs at job submission, or during
     * verification of events sequence.
     */
    public JobId testJobSubmission(Job jobToSubmit, boolean acceptSkipped) throws Exception {
        Scheduler userInt = getSchedulerInterface();

        JobId id = userInt.submit(jobToSubmit);

        log("Job submitted, id " + id.toString());

        log("Waiting for jobSubmitted");
        JobState receivedState = waitForEventJobSubmitted(id);
        Assert.assertEquals(id, receivedState.getId());

        log("Waiting for job running");
        JobInfo jInfo = waitForEventJobRunning(id);

        Assert.assertEquals(jInfo.getJobId(), id);
        Assert.assertEquals("Job " + jInfo.getJobId(), JobStatus.RUNNING, jInfo.getStatus());

        log("Waiting for job finished");
        jInfo = waitForEventJobFinished(id);
        Assert.assertEquals("Job " + jInfo.getJobId(), JobStatus.FINISHED, jInfo.getStatus());
        log("Job finished");

        boolean taskError = false;
        String message = "";

        if (jobToSubmit instanceof TaskFlowJob) {

            JobState jobState = userInt.getJobState(id);

            for (TaskState t : jobState.getTasks()) {
                log("Looking at the result of task : " + t.getName());
                if (t.getStatus() == TaskStatus.FAULTY) {
                    TaskResult tres = userInt.getTaskResult(jInfo.getJobId(), t.getName());
                    if (tres == null) {
                        message = "Task result of " + t.getName() + " should not be null.";
                        taskError = true;
                        break;
                    }
                    if (tres.getOutput() != null) {
                        System.err.println("Output of failing task (" + t.getName() + ") :");
                        System.err.println(tres.getOutput().getAllLogs(true));
                    }
                    if (tres.hadException()) {
                        System.err.println("Exception occurred in task (" + t.getName() + ") :");
                        tres.getException().printStackTrace(System.err);
                        message = "Exception occurred in task (" + t.getName() + ")";
                        taskError = true;
                        break;
                    }
                } else if (acceptSkipped && t.getStatus() == TaskStatus.SKIPPED) {
                    // do nothing
                } else if (t.getStatus() != TaskStatus.FINISHED) {
                    message = "Invalid task status for task " + t.getName() + " : " + t.getStatus();
                    taskError = true;
                    break;
                } else {
                    TaskResult tres = userInt.getTaskResult(jInfo.getJobId(), t.getName());
                    System.out.println("Output of task (" + t.getName() + ") :");
                    System.out.println(tres.getOutput().getAllLogs(true));
                }
            }
        }

        if (taskError) {
            fail(message);
        }

        return id;
    }

    /**
     * Get job result form a job Id.
     * Connect as user if needed (if not yet connected as user).
     * @param id job identifier, representing job result.
     * @return JobResult storing results.
     * @throws Exception if an exception occurs in result retrieval
     */
    public JobResult getJobResult(JobId id) throws Exception {
        return getSchedulerInterface().getJobResult(id);
    }

    public TaskResult getTaskResult(JobId jobId, String taskName) throws Exception {
        return getSchedulerInterface().getTaskResult(jobId, taskName);
    }

    //---------------------------------------------------------------//
    // events waiting methods
    //---------------------------------------------------------------//

    /**
     * Wait for a job submission event for a specific job id.
     * If event has been already thrown by scheduler, returns immediately
     * with job object associated to event, otherwise wait for event reception.
     *
     * @param id  job identifier, for which submission event is waited for.
     * @return JobState object corresponding to job submitted event.
     */
    public JobState waitForEventJobSubmitted(JobId id) {
        try {
            return waitForEventJobSubmitted(id, 0);
        } catch (ProActiveTimeoutException e) {
            //unreachable block, 0 means infinite, no timeout, no timeoutExcpetion
            //log sthing ?
            return null;
        }
    }

    /**
     * Wait for a job submission event for a specific job id.
     * If event has been already thrown by scheduler, returns immediately
     * with job object associated to event, otherwise wait for event reception.
     * @param id job identifier, for which submission event is waited for.
     * @param timeout max waiting time in milliseconds.
     * @return Jobstate object corresponding to job submitted event.
     * @throws ProActiveTimeoutException if timeout is reached.
     */
    public JobState waitForEventJobSubmitted(JobId id, long timeout) {
        return getSchedulerMonitorsHandler().waitForEventJobSubmitted(id, timeout);
    }

    /**
     * Wait for a specific job passing from pending state to running state.
     * If corresponding event has been already thrown by scheduler, returns immediately
     * with jobInfo object associated to event, otherwise wait for reception
     * of the corresponding event.
     *
     * @param id job identifier, for which event is waited for.
     * @return JobInfo event's associated object.
     */
    public JobInfo waitForEventJobRunning(JobId id) {
        try {
            return waitForEventJobRunning(id, 0);
        } catch (ProActiveTimeoutException e) {
            //unreachable block, 0 means infinite, no timeout
            //log sthing ?
            return null;
        }
    }

    /**
     * Wait for a job passing from pending to running state.
     * If corresponding event has been already thrown by scheduler, returns immediately
     * with jobInfo object associated to event, otherwise wait for reception
     * of the corresponding event.
     * @param id job identifier, for which event is waited for.
     * @param timeout max waiting time in milliseconds.
     * @return JobInfo event's associated object.
     * @throws ProActiveTimeoutException if timeout is reached.
     */
    public JobInfo waitForEventJobRunning(JobId id, long timeout) {
        return getSchedulerMonitorsHandler().waitForEventJob(SchedulerEvent.JOB_PENDING_TO_RUNNING, id,
                timeout);
    }

    /**
     * Wait for a job passing from running to finished state.
     * If corresponding event has been already thrown by scheduler, returns immediately
     * with jobInfo object associated to event, otherwise wait for reception
     * of the corresponding event.
     * If job is already finished, return immediately.
     * @param id job identifier, for which event is waited for.
     * @return JobInfo event's associated object.
     */
    public JobInfo waitForEventJobFinished(JobId id) throws Exception {
        try {
            return waitForJobEvent(id, 0, JobStatus.FINISHED, SchedulerEvent.JOB_RUNNING_TO_FINISHED);
        } catch (ProActiveTimeoutException e) {
            //unreachable block, 0 means infinite, no timeout
            //log sthing ?
            return null;
        }
    }

    /**
     * Wait for a job passing from running to finished state.
     * If corresponding event has been already thrown by scheduler, returns immediately
     * with JobInfo object associated to event, otherwise wait for event reception.
     *	This method corresponds to the running to finished transition
     *
     * @param id  job identifier, for which event is waited for.
     * @param timeout  max waiting time in milliseconds.
     * @return JobInfo event's associated object.
     * @throws ProActiveTimeoutException if timeout is reached.
     */
    public JobInfo waitForEventJobFinished(JobId id, long timeout) throws Exception {
        return waitForJobEvent(id, timeout, JobStatus.FINISHED, SchedulerEvent.JOB_RUNNING_TO_FINISHED);
    }

    private JobInfo waitForJobEvent(JobId id, long timeout, JobStatus jobStatusAfterEvent,
            SchedulerEvent jobEvent) throws Exception {
        JobState jobState = null;
        try {
            jobState = getSchedulerInterface().getJobState(id);
        } catch (UnknownJobException ignored) {
        }
        if (jobState != null && jobState.getStatus().equals(jobStatusAfterEvent)) {
            System.err.println("Job is already finished - do not wait for the 'job finished' event");
            return jobState.getJobInfo();
        } else {
            try {
                System.err.println("Waiting for the job finished event");
                return getSchedulerMonitorsHandler().waitForEventJob(jobEvent, id, timeout);
            } catch (ProActiveTimeoutException e) {
                //unreachable block, 0 means infinite, no timeout
                //log something ?
                return null;
            }
        }
    }

    public JobInfo waitForEventPendingJobFinished(JobId id, long timeout) throws ProActiveTimeoutException {
        return getSchedulerMonitorsHandler().waitForEventJob(SchedulerEvent.JOB_PENDING_TO_FINISHED, id,
                timeout);
    }

    /**
     * Wait for a job removed from Scheduler's database.
     * If corresponding event has been already thrown by scheduler, returns immediately
     * with JobInfo object associated to event, otherwise wait for event reception.
     *
     * @param id job identifier, for which event is waited for.
     * @return JobInfo event's associated object.
     */
    public JobInfo waitForEventJobRemoved(JobId id) {
        try {
            return waitForEventJobRemoved(id, 0);
        } catch (ProActiveTimeoutException e) {
            //unreachable block, 0 means infinite, no timeout
            //log sthing ?
            return null;
        }
    }

    /**
     * Wait for a event job removed from Scheduler's database.
     * If corresponding event has been already thrown by scheduler, returns immediately
     * with JobInfo object associated to event, otherwise wait for reception
     * of the corresponding event.
     *
     * @param id job identifier, for which event is waited for.
     * @param timeout max waiting time in milliseconds.
     * @return JobInfo event's associated object.
     * @throws ProActiveTimeoutException if timeout is reached.
     */
    public JobInfo waitForEventJobRemoved(JobId id, long timeout) {
        return getSchedulerMonitorsHandler().waitForEventJob(SchedulerEvent.JOB_REMOVE_FINISHED, id, timeout);
    }

    /**
     * Wait for a task passing from pending to running.
     * If corresponding event has been already thrown by scheduler, returns immediately
     * with TaskInfo object associated to event, otherwise wait for reception
     * of the corresponding event.
     *
     * @param jobId job identifier, for which task belongs.
     * @param taskName for which event is waited for.
     * @return TaskInfo event's associated object.
     */
    public TaskInfo waitForEventTaskRunning(JobId jobId, String taskName) {
        try {
            return waitForEventTaskRunning(jobId, taskName, 0);
        } catch (ProActiveTimeoutException e) {
            //unreachable block, 0 means infinite, no timeout
            //log sthing ?
            return null;
        }
    }

    /**
     * Wait for a task passing from pending to running.
     * If corresponding event has been already thrown by scheduler, returns immediately
     * with TaskInfo object associated to event, otherwise wait for reception
     * of the corresponding event.
     *
     * @param jobId job identifier, for which task belongs.
     * @param taskName for which event is waited for.
     * @param timeout max waiting time in milliseconds.
     * @return TaskInfo event's associated object.
     * @throws ProActiveTimeoutException if timeout is reached.
     */
    public TaskInfo waitForEventTaskRunning(JobId jobId, String taskName, long timeout) {
        return getSchedulerMonitorsHandler().waitForEventTask(SchedulerEvent.TASK_PENDING_TO_RUNNING, jobId,
                taskName, timeout);
    }

    /**
     * Wait for a task failed that waits for restart.
     * If corresponding event has been already thrown by scheduler, returns immediately
     * with TaskInfo object associated to event, otherwise wait for reception
     * of the corresponding event.
     *
     * @param jobId job identifier, for which task belongs.
     * @param taskName for which event is waited for.
     * @return TaskInfo event's associated object.
     */
    public TaskInfo waitForEventTaskWaitingForRestart(JobId jobId, String taskName) {
        try {
            return waitForEventTaskWaitingForRestart(jobId, taskName, 0);
        } catch (ProActiveTimeoutException e) {
            //unreachable block, 0 means infinite, no timeout
            //log sthing ?
            return null;
        }
    }

    /**
     * Wait for a task failed that waits for restart.
     * If corresponding event has been already thrown by scheduler, returns immediately
     * with TaskInfo object associated to event, otherwise wait for reception
     * of the corresponding event.
     *
     * @param jobId job identifier, for which task belongs.
     * @param taskName for which event is waited for.
     * @param timeout max waiting time in milliseconds.
     * @return TaskInfo event's associated object.
     * @throws ProActiveTimeoutException if timeout is reached.
     */
    public TaskInfo waitForEventTaskWaitingForRestart(JobId jobId, String taskName, long timeout) {
        return getSchedulerMonitorsHandler().waitForEventTask(SchedulerEvent.TASK_WAITING_FOR_RESTART, jobId,
                taskName, timeout);
    }

    /**
     * Wait for a task passing from running to finished.
     * If corresponding event has been already thrown by scheduler, returns immediately
     * with TaskInfo object associated to event, otherwise wait for reception
     * of the corresponding event.
     *
     * @param jobId  job identifier, for which task belongs.
     * @param taskName for which event is waited for.
     * @return TaskEvent, associated event's object.
     */
    public TaskInfo waitForEventTaskFinished(JobId jobId, String taskName) {
        try {
            return waitForEventTaskFinished(jobId, taskName, 0);
        } catch (ProActiveTimeoutException e) {
            //unreachable block, 0 means infinite, no timeout
            //log sthing ?
            return null;
        }
    }

    /**
     * Wait for a task passing from running to finished.
     * If corresponding event has been already thrown by scheduler, returns immediately
     * with TaskInfo object associated to event, otherwise wait for reception
     * of the corresponding event.
     *
     * @param jobId job identifier, for which task belongs.
     * @param taskName for which event is waited for.
     * @param timeout max waiting time in milliseconds.
     * @return TaskInfo, associated event's object.
     * @throws ProActiveTimeoutException if timeout is reached.
     */
    public TaskInfo waitForEventTaskFinished(JobId jobId, String taskName, long timeout) {
        return getSchedulerMonitorsHandler().waitForEventTask(SchedulerEvent.TASK_RUNNING_TO_FINISHED, jobId,
                taskName, timeout);
    }

    /**
     * Wait for an event regarding Scheduler state : started, resumed, stopped...
     * If a corresponding event has been already thrown by scheduler, returns immediately,
     * otherwise wait for reception of the corresponding event.
     * @param event awaited event.
     */
    public void waitForEventSchedulerState(SchedulerEvent event) {
        try {
            waitForEventSchedulerState(event, 0);
        } catch (ProActiveTimeoutException e) {
            //unreachable block, 0 means infinite, no timeout
            //log sthing ?
        }
    }

    /**
     * Wait for an event regarding Scheduler state : started, resumed, stopped...
     * If a corresponding event has been already thrown by scheduler, returns immediately,
     * otherwise wait for reception of the corresponding event.
     * @param event awaited event.
     * @param timeout in milliseconds
     * @throws ProActiveTimeoutException if timeout is reached
     */
    public void waitForEventSchedulerState(SchedulerEvent event, long timeout) {
        getSchedulerMonitorsHandler().waitForEventSchedulerState(event, timeout);
    }

    //---------------------------------------------------------------//
    // Job finished waiting methods
    //---------------------------------------------------------------//

    /**
     * Wait for a finished job. If Job is already finished, methods return.
     * This method doesn't wait strictly 'job finished event', it looks
     * first if the job is already finished, if yes, returns immediately.
     * Otherwise method performs a wait for job finished event.
     *
     * @param id JobId representing the job awaited to be finished.
     */
    public void waitForFinishedJob(JobId id) {
        try {
            waitForFinishedJob(id, 0);
        } catch (ProActiveTimeoutException e) {
            //unreachable block, 0 means infinite, no timeout
            //log sthing ?
        }
    }

    /**
     * Wait for a finished job. If Job is already finished, methods return.
     * This method doesn't wait strictly 'job finished event', it looks
     * first if the job is already finished, if yes, returns immediately.
     * Otherwise method performs a wait for job finished event.
     *
     * @param id JobId representing the job awaited to be finished.
     * @param timeout in milliseconds
     * @throws ProActiveTimeoutException if timeout is reached
     */
    public void waitForFinishedJob(JobId id, long timeout) {
        SchedulerTestUser.getInstance().getMonitorsHandler().waitForFinishedJob(id, timeout);
    }

    private SchedulerMonitorsHandler getSchedulerMonitorsHandler() {
        return SchedulerTestUser.getInstance().getMonitorsHandler();
    }

    public static void setExecutable(String filesList) throws IOException {
        Runtime.getRuntime().exec("chmod u+x " + filesList);
    }

    public void disconnect() throws Exception {
        if (RMTestUser.getInstance().isConnected()) {
            RMTestUser.getInstance().disconnectFromRM();
        }
        if (SchedulerTestUser.getInstance().isConnected()) {
            SchedulerTestUser.getInstance().disconnectFromScheduler();
        }
    }

    public ResourceManager getResourceManager() throws Exception {
        return getResourceManager(TestUsers.DEMO);
    }

    public ResourceManager getResourceManager(TestUsers user) throws Exception {
        if (!RMTestUser.getInstance().is(user)) { // changing user on the fly
            RMTestUser.getInstance().connect(user, scheduler.getRMUrl());
        }

        if (!RMTestUser.getInstance().isConnected()) {
            RMTestUser.getInstance().connect(user, scheduler.getRMUrl());
        }

        return RMTestUser.getInstance().getResourceManager();
    }

    public static String getLocalUrl() {
        return scheduler.getUrl();
    }

    private RMMonitorsHandler getRMMonitorsHandler() throws Exception {
        return RMTestUser.getInstance().getMonitorsHandler();
    }

    public void createNodeSource(String name, int nbNodes) throws Exception {
        RMTHelper.createNodeSource(name, nbNodes, null, getResourceManager(), getRMMonitorsHandler());
    }

    public void createNodeSource(String name, int nbNodes, List<String> vmOptions) throws Exception {
        RMTHelper.createNodeSource(name, nbNodes, vmOptions, getResourceManager(), getRMMonitorsHandler());
    }

    public List<TestNode> addNodesToDefaultNodeSource(int nbNodes) throws Exception {
        return RMTHelper.addNodesToDefaultNodeSource(nbNodes, null, getResourceManager(), getRMMonitorsHandler());
    }

    public List<TestNode> addNodesToDefaultNodeSource(int nbNodes, List<String> vmOptions) throws Exception {
        return RMTHelper.addNodesToDefaultNodeSource(nbNodes, vmOptions, getResourceManager(), getRMMonitorsHandler());
    }

    public TestNode createNode(String nodeName) throws Exception {
        getResourceManager(); // reconnect with the default user
        return RMTHelper.createNode(nodeName);
    }

    public RMNodeEvent waitForNodeEvent(RMEventType nodeAdded, String nodeUrl, long timeout) throws Exception {
        return RMTHelper.waitForNodeEvent(nodeAdded, nodeUrl, timeout, getRMMonitorsHandler());
    }

    public RMNodeEvent waitForAnyNodeEvent(RMEventType nodeStateChanged, long timeout) throws Exception {
        return RMTHelper.waitForAnyNodeEvent(nodeStateChanged, timeout, getRMMonitorsHandler());
    }

    public void killNode(String url) throws Exception {
        getResourceManager(); // reconnect with the default user
        RMTHelper.killNode(url);
    }

    public TestNode createRMNodeStarterNode(String nodeName) throws Exception {
        int pnpPort = RMTHelper.findFreePort();
        String nodeUrl = "pnp://localhost:" + pnpPort + "/" + nodeName;
        Map<String, String> vmParameters = new HashMap<>();
        vmParameters.put(PNPConfig.PA_PNP_PORT.getName(), Integer.toString(pnpPort));
        JVMProcessImpl nodeProcess = RMTHelper.createJvmProcess(RMNodeStarter.class.getName(),
                Arrays.asList("-n", nodeName, "-r", getLocalUrl(), "-Dproactive.net.nolocal=false",
                        "-Djava.library.path=" + PASchedulerProperties.SCHEDULER_HOME.getValueAsString()
                                + File.separator + "dist" + File.separator + "lib"),
                vmParameters, null);
        return RMTHelper.createNode(nodeName, nodeUrl, nodeProcess);
    }

    public RMNodeEvent waitForAnyNodeEvent(RMEventType nodeStateChanged) throws Exception {
        return RMTHelper.waitForAnyNodeEvent(nodeStateChanged, getRMMonitorsHandler());
    }

    public void waitForNodeSourceEvent(RMEventType nodesourceCreated, String nsName) throws Exception {
        RMTHelper.waitForNodeSourceEvent(nodesourceCreated, nsName, getRMMonitorsHandler());
    }

    public void addExtraNodes(int nbNodes) throws Exception {
        RMTHelper.createNodeSource(extraNS, nbNodes, getResourceManager(), getRMMonitorsHandler());
        RMTHelper.log("Node source \"" + extraNS + "\" created");
    }

    public void removeExtraNodeSource() throws Exception {
        removeNodeSource(extraNS);
    }

    public void removeNodeSource(String nsName) throws Exception {
        try {
            getResourceManager().removeNodeSource(nsName, true).getBooleanValue();
        } catch (Throwable ignored) {
        }
    }

    public void checkNodesAreClean() throws Exception {
        Set<String> nodeUrls = getResourceManager().listAliveNodeUrls();
        // We wait until no active object remain on the nodes.
        // If AO remains the test will fail with a timeout.
        boolean remainingAO = true;

        long wait = 0;
        while (remainingAO && wait < 5000) {
            Thread.sleep(50);
            wait += 50;
            remainingAO = false;
            for (String nodeUrl : nodeUrls) {
                remainingAO = remainingAO || (NodeFactory.getNode(nodeUrl).getNumberOfActiveObjects() > 0);
            }
        }
        if (remainingAO) {
            for (String nodeUrl : nodeUrls) {
                Node node = NodeFactory.getNode(nodeUrl);

                log("Found remaining AOs on node " + node.getNodeInformation().getURL() + " " +
                    Arrays.toString(node.getActiveObjects()));
            }
        }
        assertFalse("No Active Objects should remain", remainingAO);
    }
}
