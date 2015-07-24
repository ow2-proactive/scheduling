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
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package functionaltests.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.objectweb.proactive.core.ProActiveTimeoutException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.core.process.JVMProcessImpl;
import org.objectweb.proactive.extensions.pnp.PNPConfig;
import org.ow2.proactive.process_tree_killer.ProcessTree;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.common.event.RMNodeEvent;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.resourcemanager.utils.RMNodeStarter;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.SchedulerAuthenticationInterface;
import org.ow2.proactive.scheduler.common.SchedulerEvent;
import org.ow2.proactive.scheduler.common.exception.UnknownJobException;
import org.ow2.proactive.scheduler.common.job.Job;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.JobStatus;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.job.factories.JobFactory;
import org.ow2.proactive.scheduler.common.task.Task;
import org.ow2.proactive.scheduler.common.task.TaskInfo;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.TaskStatus;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Assert;

import functionaltests.monitor.RMMonitorsHandler;
import functionaltests.monitor.SchedulerMonitorsHandler;


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
 * waitForTerminatedJob() method dosen't act as other waitForEvent**() Methods.
 * This method deduce a job finished from current Scheduler's job states and received event.
 * This method can also be used for testing for job submission with killing and restarting
 * Scheduler.
 *
 * @author ProActive team
 * @since ProActive Scheduling 1.0
 */
public class SchedulerTHelper {

    private static TestScheduler scheduler = new TestScheduler();
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
    private static SchedulerTestUser connectedSchedulerUser = new SchedulerTestUser(TestUsers.DEMO);
    private static RMTestUser connectedRMUser = new RMTestUser(TestUsers.DEMO);

    // can be changed by starting the Scheduler manually
    private SchedulerTestConfiguration currentTestConfiguration = SchedulerTestConfiguration
            .defaultConfiguration();

    /**
     * Start the scheduler using a forked JVM and
     * deploys, with its associated Resource manager, 5 local ProActive nodes.
     *
     * @param configuration the Scheduler configuration file to use (default is
     *                      functionalTSchedulerProperties.ini)
     * 			null to use the default one.
     * @throws Exception if an error occurs.
     */
    public void startScheduler(String configuration) throws Exception {
        startScheduler(SchedulerTestConfiguration.customSchedulerConfig(configuration));
    }

    /**
     * Start the scheduler using a forked JVM and
     * deploys, with its associated empty Resource manager.
     *
     * @throws Exception if an error occurs.
     */
    public void startSchedulerWithEmptyResourceManager() throws Exception {
        startScheduler(SchedulerTestConfiguration.emptyResourceManager());
    }

    public void startScheduler(boolean localnodes, String schedPropertiesFilePath,
            String rmPropertiesFilePath, String rmUrl) throws Exception {
        SchedulerTestConfiguration configuration = new SchedulerTestConfiguration(schedPropertiesFilePath,
            rmPropertiesFilePath, localnodes, TestScheduler.PNP_PORT, rmUrl);
        startScheduler(configuration);
    }

    private void startScheduler(SchedulerTestConfiguration configuration) throws Exception {
        if (scheduler.isStartedWithSameConfiguration(configuration)) {
            // log("Scheduler already started with same configuration, keeping started instance");
        } else {
            log("Starting Scheduler");
            scheduler.start(configuration);
        }
        currentTestConfiguration = configuration;
    }

    /**
     * Kill the forked Scheduler if exists.
     */
    public void killScheduler() throws Exception {
        connectedRMUser.disconnect();
        connectedSchedulerUser.disconnect();
        scheduler.kill();
    }

    /**
     * Kill the forked Scheduler and all nodes.
     */
    public void killSchedulerAndNodes() throws Exception {
        Logger.getLogger(ProcessTree.class).setLevel(Level.DEBUG);
        killScheduler();
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
        killSchedulerAndNodes();
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
        startScheduler(currentTestConfiguration);
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
        startScheduler(currentTestConfiguration);

        if (!connectedSchedulerUser.is(user)) { // changing user on the fly
            if (connectedSchedulerUser != null) {
                connectedSchedulerUser.disconnect();
            }
            connectedSchedulerUser = new SchedulerTestUser(user);
            connectedSchedulerUser.connect(scheduler.getAuth());
        }

        if (connectedSchedulerUser.isDisconnected()) {
            connectedSchedulerUser.connect(scheduler.getAuth());
        }

        return connectedSchedulerUser.getScheduler();
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
     * Kills a job
     * @return success or failure at killing the job
     * @throws Exception
     */
    public boolean killJob(String jobId) throws Exception {
        Scheduler userInt = getSchedulerInterface();
        return userInt.killJob(jobId);
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

    public void testJobSubmissionAndVerifyAllResults(String jobDescPath) throws Throwable {
        Job testJob = JobFactory.getFactory().createJob(jobDescPath);
        testJobSubmissionAndVerifyAllResults(testJob, jobDescPath);
    }

    public void testJobSubmissionAndVerifyAllResults(Job testJob, String jobDesc) throws Throwable {
        JobId id = testJobSubmission(testJob);
        // check result are not null
        JobResult res = getJobResult(id);
        Assert.assertFalse("Had Exception : " + jobDesc, getJobResult(id).hadException());

        for (Map.Entry<String, TaskResult> entry : res.getAllResults().entrySet()) {

            Assert.assertFalse("Had Exception (" + jobDesc + ") : " + entry.getKey(), entry.getValue()
                    .hadException());

            Assert.assertNotNull("Result not null (" + jobDesc + ") : " + entry.getKey(), entry.getValue()
                    .value());
        }

        removeJob(id);
        waitForEventJobRemoved(id);
    }

    /**
     * Creates and submit a job from an XML job descriptor, and check
     * event related to this job submission :
     * 1/ job submitted event
     * 2/ job passing from pending to running (with state set to running).
     * 3/ every task passing from pending to running (with state set to running).
     * 4/ every task passing from running to finished (with state set to finished).
     * 5/ and finally job passing from running to finished (with state set to finished).
     * Then returns.
     *
     * This is the simplest events sequence of a job submission. If you need to test
     * specific event or task states (failure, rescheduling etc, you must not use this
     * helper and check events sequence with waitForEvent**() functions.
     *
     * @param jobDescPath path to an XML job descriptor to submit
     * @param user Type of user
     * @return JobId, the job's identifier.
     * @throws Exception if an error occurs at job creation/submission, or during
     * verification of events sequence.
     */
    public JobId testJobSubmission(String jobDescPath) throws Exception {
        Job jobToTest = JobFactory.getFactory().createJob(jobDescPath);
        return testJobSubmission(jobToTest);
    }

    /**
     * Creates and submit a job from an XML job descriptor, and check, with assertions,
     * event related to this job submission :
     * 1/ job submitted event
     * 2/ job passing from pending to running (with state set to running).
     * 3/ every task passing from pending to running (with state set to running).
     * 4/ every task finish without error ; passing from running to finished (with state set to finished).
     * 5/ and finally job passing from running to finished (with state set to finished).
     * Then returns.
     *
     * This is the simplest events sequence of a job submission. If you need to test
     * specific events or task states (failures, rescheduling etc, you must not use this
     * helper and check events sequence with waitForEvent**() functions.
     *
     * @param jobToSubmit job object to schedule.
     * @param mode true if the mode is forked, false if normal mode
     * @return JobId, the job's identifier.
     * @throws Exception if an error occurs at job submission, or during
     * verification of events sequence.
     */
    public JobId testJobSubmission(Job jobToSubmit) throws Exception {
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

        if (jobToSubmit instanceof TaskFlowJob) {

            for (Task t : ((TaskFlowJob) jobToSubmit).getTasks()) {
                log("Waiting for task running : " + t.getName());
                TaskInfo ti = waitForEventTaskRunning(id, t.getName());
                Assert.assertEquals(t.getName(), ti.getTaskId().getReadableName());
                Assert.assertEquals("Task " + t.getName(), TaskStatus.RUNNING, ti.getStatus());
            }

            for (Task t : ((TaskFlowJob) jobToSubmit).getTasks()) {
                log("Waiting for task finished : " + t.getName());
                TaskInfo ti = waitForEventTaskFinished(id, t.getName());
                Assert.assertEquals(t.getName(), ti.getTaskId().getReadableName());
                if (ti.getStatus() == TaskStatus.FAULTY) {
                    TaskResult tres = userInt.getTaskResult(jInfo.getJobId(), t.getName());
                    Assert.assertNotNull("Task result of " + t.getName(), tres);
                    if (tres.getOutput() != null) {
                        System.err.println("Output of failing task (" + t.getName() + ") :");
                        System.err.println(tres.getOutput().getAllLogs(true));
                    }
                    if (tres.hadException()) {
                        System.err.println("Exception occurred in task (" + t.getName() + ") :");
                        tres.getException().printStackTrace(System.err);
                    }

                }
                Assert.assertEquals("Task " + t.getName(), TaskStatus.FINISHED, ti.getStatus());
            }

        }

        log("Waiting for job finished");
        jInfo = waitForEventJobFinished(id);
        Assert.assertEquals("Job " + jInfo.getJobId(), JobStatus.FINISHED, jInfo.getStatus());

        log("Job finished");
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
    public JobState waitForEventJobSubmitted(JobId id, long timeout) throws ProActiveTimeoutException {
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
    public JobInfo waitForEventJobRunning(JobId id, long timeout) throws ProActiveTimeoutException {
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
    public JobInfo waitForEventJobRemoved(JobId id, long timeout) throws ProActiveTimeoutException {
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
    public TaskInfo waitForEventTaskRunning(JobId jobId, String taskName, long timeout)
            throws ProActiveTimeoutException {
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
    public TaskInfo waitForEventTaskWaitingForRestart(JobId jobId, String taskName, long timeout)
            throws ProActiveTimeoutException {
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
    public TaskInfo waitForEventTaskFinished(JobId jobId, String taskName, long timeout)
            throws ProActiveTimeoutException {
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
    public void waitForEventSchedulerState(SchedulerEvent event, long timeout)
            throws ProActiveTimeoutException {
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
    public void waitForFinishedJob(JobId id, long timeout) throws ProActiveTimeoutException {
        connectedSchedulerUser.getMonitorsHandler().waitForFinishedJob(id, timeout);
    }

    private SchedulerMonitorsHandler getSchedulerMonitorsHandler() {
        return connectedSchedulerUser.getMonitorsHandler();
    }

    public static void setExecutable(String filesList) throws IOException {
        Runtime.getRuntime().exec("chmod u+x " + filesList);
    }

    public void disconnect() throws Exception {
        connectedSchedulerUser.disconnect();
        connectedRMUser.disconnect();
    }

    public ResourceManager getResourceManager() throws Exception {
        return getResourceManager(TestUsers.DEMO);
    }

    public ResourceManager getResourceManager(TestUsers user) throws Exception {
        startScheduler(currentTestConfiguration);

        if (!connectedRMUser.is(user)) { // changing user on the fly
            if (connectedRMUser != null) {
                connectedRMUser.disconnect();
            }
            connectedRMUser = new RMTestUser(user);
            connectedRMUser.connect(scheduler.getRMAuth());
        }

        if (connectedRMUser.isDisconnected()) {
            connectedRMUser.connect(scheduler.getRMAuth());
        }

        return connectedRMUser.getResourceManager();
    }

    public static String getLocalUrl() {
        return scheduler.getUrl();
    }

    public void createNodeSource(String nodeSourceName, int nbNodes) throws Exception {
        RMTHelper.createNodeSource(nodeSourceName, nbNodes, getResourceManager(), getRMMonitorsHandler());
    }

    private RMMonitorsHandler getRMMonitorsHandler() throws Exception {
        return connectedRMUser.getMonitorsHandler();
    }

    public void createNodeSource(int nbNodes, List<String> vmOptions) throws Exception {
        RMTHelper.createNodeSource(nbNodes, vmOptions, getResourceManager(), getRMMonitorsHandler());
    }

    public TestNode createNode(String nodeName) throws InterruptedException, NodeException, IOException {
        return RMTHelper.createNode(nodeName);
    }

    public RMNodeEvent waitForNodeEvent(RMEventType nodeAdded, String nodeUrl, long timeout) throws Exception {
        return RMTHelper.waitForNodeEvent(nodeAdded, nodeUrl, timeout, getRMMonitorsHandler());
    }

    public RMNodeEvent waitForAnyNodeEvent(RMEventType nodeStateChanged, long timeout) throws Exception {
        return RMTHelper.waitForAnyNodeEvent(nodeStateChanged, timeout, getRMMonitorsHandler());
    }

    public void killNode(String url) throws NodeException {
        RMTHelper.killNode(url);
    }

    public TestNode createRMNodeStarterNode(String nodeName) throws IOException, NodeException,
            InterruptedException {

        int pnpPort = RMTHelper.findFreePort();
        String nodeUrl = "pnp://localhost:" + pnpPort + "/" + nodeName;
        Map<String, String> vmParameters = new HashMap<>();
        vmParameters.put(PNPConfig.PA_PNP_PORT.getName(), Integer.toString(pnpPort));
        JVMProcessImpl nodeProcess = RMTHelper.createJvmProcess(RMNodeStarter.class.getName(),
                Arrays.asList("-n", nodeName, "-r", getLocalUrl(), "-Dproactive.net.nolocal=false"),
                vmParameters, null);
        return RMTHelper.createNode(nodeName, nodeUrl, nodeProcess);
    }

    /**
     * Returns the alive Nodes accessible by the RM
     * @return list of ProActive Nodes
     */
    public List<Node> listAliveNodes() throws Exception {
        ArrayList<Node> nodes = new ArrayList<>();
        Set<String> urls = getResourceManager().listAliveNodeUrls();
        for (String url : urls) {
            nodes.add(NodeFactory.getNode(url));
        }
        return nodes;
    }

    public RMNodeEvent waitForAnyNodeEvent(RMEventType nodeStateChanged) throws Exception {
        return RMTHelper.waitForAnyNodeEvent(nodeStateChanged, getRMMonitorsHandler());
    }

    public void waitForNodeSourceEvent(RMEventType nodesourceCreated, String nsName) throws Exception {
        RMTHelper.waitForNodeSourceEvent(nodesourceCreated, nsName, getRMMonitorsHandler());
    }
}
