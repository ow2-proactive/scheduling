/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package functionaltests;

import java.io.IOException;
import java.net.URL;

import org.junit.Assert;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.ProActiveTimeoutException;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.OperatingSystem;
import org.objectweb.proactive.core.xml.VariableContractImpl;
import org.objectweb.proactive.core.xml.VariableContractType;
import org.objectweb.proactive.extensions.gcmdeployment.PAGCMDeployment;
import org.objectweb.proactive.gcmdeployment.GCMApplication;
import org.objectweb.proactive.gcmdeployment.GCMVirtualNode;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.SchedulerAuthenticationInterface;
import org.ow2.proactive.scheduler.common.SchedulerConstants;
import org.ow2.proactive.scheduler.common.SchedulerEvent;
import org.ow2.proactive.scheduler.common.SchedulerEventListener;
import org.ow2.proactive.scheduler.common.SchedulerState;
import org.ow2.proactive.scheduler.common.exception.SchedulerException;
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
import org.ow2.proactive.scheduler.common.task.TaskStatus;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;

import functionaltests.monitor.MonitorEventReceiver;
import functionaltests.monitor.SchedulerMonitorsHandler;


/**
 *
 * Static helpers that provide main operations for Scheduler functional test.
 *
 * - helpers for launching a scheduler and a RM in a forked JVM, and deploys 5 local ProActive nodes :
 * Scheduler can be stated with default configuration file, configuration is also
 * defined in $PA_SCHEDULER/config/PAschedulerProperties.ini. If database exists in
 * $PA_SCHEDULER/SCHEDULER_DB/, it recovers it and keeps its state.
 *
 * Scheduler can be started with specific configuration file for tests, in that case :
 * 		- database database is recovered without jobs, in $PA_SCHEDULER/SCHEDULER_DB/
 * 		- removejobdelay property is set to 1.
 * 		- numberofexecutiononfailure property is set to 2
 * 		- initialwaitingtime is set to 10
 *
 * scheduler can also be started with other specific scheduler property file, and GCM deployment file,
 *
 * - helpers to acquire user and administrator interfaces
 * - helper for job submission
 * - helpers for events waiting. Creates if needed an event receiver that receives
 * all Scheduler events, store them until waitForEvent**()methods check theses event.
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
 * WARNING, you cannot get Scheduler user interface and Administrator interface twice ;
 * //TODO solve this, one connection per body allowed
 *
 *
 * @author ProActive team
 * @since ProActive Scheduling 1.0
 *
 */
public class SchedulerTHelper {

    protected static String defaultDescriptor = SchedulerTHelper.class.getResource(
            "config/GCMNodeSourceDeployment.xml").getPath();

    protected static URL startForkedSchedulerApplication = SchedulerTHelper.class
            .getResource("/functionaltests/config/StartForkedSchedulerApplication.xml");

    protected static String functionalTestRMProperties = SchedulerTHelper.class.getResource(
            "config/functionalTRMProperties.ini").getPath();

    protected static String functionalTestSchedulerProperties = SchedulerTHelper.class.getResource(
            "config/functionalTSchedulerProperties.ini").getPath();

    public static String schedulerDefaultURL = "//Localhost/" + SchedulerConstants.SCHEDULER_DEFAULT_NAME;

    protected static final String VAR_OS = "os";

    protected static VariableContractImpl vContract;
    protected static GCMApplication gcmad;

    protected static SchedulerAuthenticationInterface schedulerAuth;

    protected static Scheduler adminSchedInterface;

    protected static SchedulerMonitorsHandler monitorsHandler;

    protected static MonitorEventReceiver eventReceiver;

    public static String username = "demo";
    public static String password = "demo";

    /**
     * Start the scheduler using a forked JVM.
     * It uses Scheduler Properties file designed for tests
     * (database is recovered without jobs).
     *
     * @throws Exception
     */
    public static void startScheduler() throws Exception {
        startScheduler(null);
    }

    /**
     * Start the scheduler using a forked JVM and
     * deploys, with its associated Resource manager, 5 local ProActive nodes.
     *
     * @param configuration the Scheduler configuration file to use (default is functionalTSchedulerProperties.ini)
     * 			null to use the default one.
     * @throws Exception if an error occurs.
     */
    public static void startScheduler(String configuration) throws Exception {
        startScheduler(defaultDescriptor, configuration);
    }

    /**
     * Start the scheduler using a forked JVM and
     * deploys, with its associated empty Resource manager.
     *
     * @param configuration the Scheduler configuration file to use (default is functionalTSchedulerProperties.ini)
     * 			null to use the default one.
     * @throws Exception if an error occurs.
     */
    public static void startSchedulerWithEmptyResourceManager() throws Exception {
        startScheduler(null, null);
    }

    /**
     * Starts Scheduler with a specific GCM deployment descriptor and scheduler properties file,
     * @param GCMDPath path to a GCMD deployment file
     * @param configuration the Scheduler configuration file to use (default is functionalTSchedulerProperties.ini)
     * 			null to use the default one.
     * @throws Exception
     */
    public static void startScheduler(String GCMDPath, String schedPropertiesFilePath) throws Exception {
        if (schedPropertiesFilePath == null) {
            schedPropertiesFilePath = functionalTestSchedulerProperties;
        }
        deploySchedulerGCMA();
        GCMVirtualNode vn = gcmad.getVirtualNode("VN");
        Node node = vn.getANode();
        MyAO myAO = (MyAO) PAActiveObject.newActive(MyAO.class.getName(), null, node);
        schedulerAuth = myAO.createAndJoinForkedScheduler(GCMDPath, schedPropertiesFilePath,
                functionalTestRMProperties);
    }

    /**
     * Kill the forked Scheduler if exists.
     */
    public static void killScheduler() {
        if (gcmad != null) {
            gcmad.kill();
        }
        schedulerAuth = null;
        adminSchedInterface = null;
    }

    /**
     * Kill and restart the scheduler using a forked JVM.
     * Scheduler is restarted with not test configuration properties file
     * and 5 local ProActive nodes.
     *
     * WARNING : User or administrator interface is not reconnected automatically.
     *
     * @throws Exception if an error occurs
     */
    public static void killAndRestartScheduler() throws Exception {
        killAndRestartScheduler(null);
    }

    /**
     * Restart the scheduler using a forked JVM,
     * with default GCM deployment descriptor.
     * User or administrator interface is not reconnected automatically.
     *
     * @param configuration the Scheduler configuration file to use (default is functionalTSchedulerProperties.ini)
     * 			null to use the default one.
     * @throws Exception
     */
    public static void killAndRestartScheduler(String configuration) throws Exception {
        killScheduler();
        deploySchedulerGCMA();
        //let everything be destroyed
        startScheduler(configuration);
    }

    /**
     * Log a String on console.
     * @param s
     */
    public static void log(String s) {
        System.out.println("------------------------------ " + s);
    }

    /**
     * Return Scheduler authentication interface. Start Scheduler with test
     * configuration file, if scheduler is not yet started.
     * @return
     * @throws Exception
     */
    public static SchedulerAuthenticationInterface getSchedulerAuth() throws Exception {
        if (schedulerAuth == null) {
            startScheduler();
        }
        return schedulerAuth;
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
    public static Scheduler getSchedulerInterface() throws Exception {
        if (adminSchedInterface == null) {
            connect();
        }
        return adminSchedInterface;
    }

    /**
     * Creates a job from an XML job descriptor, submit it, and return immediately.
     * connect as user if needed (if not yet connected as user).
     * @param jobDescPath
     * @return JobId the job's identifier corresponding to submission.
     * @throws Exception if an error occurs at job creation/submission.
     */
    public static JobId submitJob(String jobDescPath) throws Exception {
        Job jobToSubmit = JobFactory.getFactory().createJob(jobDescPath);
        return submitJob(jobToSubmit);
    }

    /**
     * Submit a job, and return immediately.
     * Connect as user if needed (if not yet connected as user).
     * @param jobToSubmit job object to schedule.
     * @return JobId the job's identifier corresponding to submission.
     * @throws Exception if an error occurs at job submission.
     */
    public static JobId submitJob(Job jobToSubmit) throws Exception {
        Scheduler userInt = getSchedulerInterface();
        return userInt.submit(jobToSubmit);
    }

    /**
     * Remove a job from Scheduler database.
     * connect as user if needed (if not yet connected as user).
     * @param id of the job to remove from database.
     * @throws Exception if an error occurs at job removal.
     */
    public static void removeJob(JobId id) throws Exception {
        Scheduler userInt = getSchedulerInterface();
        userInt.removeJob(id);
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
     * @return JobId, the job's identifier.
     * @throws Exception if an error occurs at job creation/submission, or during
     * verification of events sequence.
     */
    public static JobId testJobSubmission(String jobDescPath) throws Exception {
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
     * @return JobId, the job's identifier.
     * @throws Exception if an error occurs at job submission, or during
     * verification of events sequence.
     */
    public static JobId testJobSubmission(Job jobToSubmit) throws Exception {
        Scheduler userInt = getSchedulerInterface();

        JobId id = userInt.submit(jobToSubmit);

        log("Job submitted, id " + id.toString());

        log("Waiting for jobSubmitted");
        JobState receivedstate = SchedulerTHelper.waitForEventJobSubmitted(id);
        Assert.assertEquals(id, receivedstate.getId());

        log("Waiting for job running");
        JobInfo jInfo = SchedulerTHelper.waitForEventJobRunning(id);

        Assert.assertEquals(jInfo.getJobId(), id);
        Assert.assertEquals(JobStatus.RUNNING, jInfo.getStatus());

        if (jobToSubmit instanceof TaskFlowJob) {

            for (Task t : ((TaskFlowJob) jobToSubmit).getTasks()) {
                log("Waiting for task running : " + t.getName());
                TaskInfo ti = waitForEventTaskRunning(id, t.getName());
                Assert.assertEquals(t.getName(), ti.getTaskId().getReadableName());
                Assert.assertEquals(TaskStatus.RUNNING, ti.getStatus());
            }

            for (Task t : ((TaskFlowJob) jobToSubmit).getTasks()) {
                log("Waiting for task finished : " + t.getName());
                TaskInfo ti = waitForEventTaskFinished(id, t.getName());
                Assert.assertEquals(t.getName(), ti.getTaskId().getReadableName());
                Assert.assertEquals(TaskStatus.FINISHED, ti.getStatus());
            }

        }

        log("Waiting for job finished");
        jInfo = SchedulerTHelper.waitForEventJobFinished(id);
        Assert.assertEquals(JobStatus.FINISHED, jInfo.getStatus());

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
    public static JobResult getJobResult(JobId id) throws Exception {
        return getSchedulerInterface().getJobResult(id);
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
    public static JobState waitForEventJobSubmitted(JobId id) {
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
    public static JobState waitForEventJobSubmitted(JobId id, long timeout) throws ProActiveTimeoutException {
        return getMonitorsHandler().waitForEventJobSubmitted(id, timeout);
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
    public static JobInfo waitForEventJobRunning(JobId id) {
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
    public static JobInfo waitForEventJobRunning(JobId id, long timeout) throws ProActiveTimeoutException {
        return getMonitorsHandler().waitForEventJob(SchedulerEvent.JOB_PENDING_TO_RUNNING, id, timeout);
    }

    /**
     * Wait for a job passing from running to finished state.
     * If corresponding event has been already thrown by scheduler, returns immediately
     * with jobInfo object associated to event, otherwise wait for reception
     * of the corresponding event.
     * @param id job identifier, for which event is waited for.
     * @return JobInfo event's associated object.
     */
    public static JobInfo waitForEventJobFinished(JobId id) {
        try {
            return waitForEventJobFinished(id, 0);
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
     *
     * @param id  job identifier, for which event is waited for.
     * @param timeout  max waiting time in milliseconds.
     * @return JobInfo event's associated object.
     * @throws ProActiveTimeoutException if timeout is reached.
     */
    public static JobInfo waitForEventJobFinished(JobId id, long timeout) throws ProActiveTimeoutException {
        return getMonitorsHandler().waitForEventJob(SchedulerEvent.JOB_RUNNING_TO_FINISHED, id, timeout);
    }

    /**
     * Wait for a job removed from Scheduler's database.
     * If corresponding event has been already thrown by scheduler, returns immediately
     * with JobInfo object associated to event, otherwise wait for event reception.
     *
     * @param id job identifier, for which event is waited for.
     * @return JobInfo event's associated object.
     */
    public static JobInfo waitForEventJobRemoved(JobId id) {
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
    public static JobInfo waitForEventJobRemoved(JobId id, long timeout) throws ProActiveTimeoutException {
        return getMonitorsHandler().waitForEventJob(SchedulerEvent.JOB_REMOVE_FINISHED, id, timeout);
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
    public static TaskInfo waitForEventTaskRunning(JobId jobId, String taskName) {
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
    public static TaskInfo waitForEventTaskRunning(JobId jobId, String taskName, long timeout)
            throws ProActiveTimeoutException {
        return getMonitorsHandler().waitForEventTask(SchedulerEvent.TASK_PENDING_TO_RUNNING, jobId, taskName,
                timeout);
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
    public static TaskInfo waitForEventTaskWaitingForRestart(JobId jobId, String taskName) {
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
    public static TaskInfo waitForEventTaskWaitingForRestart(JobId jobId, String taskName, long timeout)
            throws ProActiveTimeoutException {
        return getMonitorsHandler().waitForEventTask(SchedulerEvent.TASK_WAITING_FOR_RESTART, jobId,
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
    public static TaskInfo waitForEventTaskFinished(JobId jobId, String taskName) {
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
    public static TaskInfo waitForEventTaskFinished(JobId jobId, String taskName, long timeout)
            throws ProActiveTimeoutException {
        return getMonitorsHandler().waitForEventTask(SchedulerEvent.TASK_RUNNING_TO_FINISHED, jobId,
                taskName, timeout);
    }

    /**
     * Wait for an event regarding Scheduler state : started, resumed, stopped...
     * If a corresponding event has been already thrown by scheduler, returns immediately,
     * otherwise wait for reception of the corresponding event.
     * @param event awaited event.
     */
    public static void waitForEventSchedulerState(SchedulerEvent event) {
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
    public static void waitForEventSchedulerState(SchedulerEvent event, long timeout)
            throws ProActiveTimeoutException {
        getMonitorsHandler().waitForEventSchedulerState(event, timeout);
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
     * So this method is not dedicated to check reception of job finished event,
     * for testing event reception you should use -{@link #SchedulerTHelper.waitForEventJobFinished()}
     *
     * @param id JobId representing the job awaited to be finished.
     */
    public static void waitForFinishedJob(JobId id) {
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
     * So this method is not dedicated to check reception of job finished event
     * for testing event reception you should use -{@link #SchedulerMinitorsHandler.waitForEventJobFinished()}
     * @param id JobId representing the job awaited to be finished.
     * @param timeout in milliseconds
     * @throws ProActiveTimeoutException if timeout is reached
     */
    public static void waitForFinishedJob(JobId id, long timeout) throws ProActiveTimeoutException {
        monitorsHandler.waitForFinishedJob(id, timeout);
    }

    //-------------------------------------------------------------//
    //private methods
    //-------------------------------------------------------------//

    private static void initEventReceiver(Scheduler schedInt) throws NodeException, SchedulerException,
            ActiveObjectCreationException {

        SchedulerMonitorsHandler mHandler = getMonitorsHandler();
        if (eventReceiver == null) {
            /** create event receiver then turnActive to avoid deepCopy of MonitorsHandler object
             * 	(shared instance between event receiver and static helpers).
            */
            MonitorEventReceiver passiveEventReceiver = new MonitorEventReceiver(mHandler);
            eventReceiver = (MonitorEventReceiver) PAActiveObject.turnActive(passiveEventReceiver);

        }
        SchedulerState state = schedInt.addEventListener((SchedulerEventListener) eventReceiver, true, true);
        mHandler.init(state);
    }

    private static void deploySchedulerGCMA() throws ProActiveException {
        vContract = new VariableContractImpl();
        vContract.setVariableFromProgram(VAR_OS, OperatingSystem.getOperatingSystem().name(),
                VariableContractType.DescriptorDefaultVariable);
        StringBuilder properties = new StringBuilder("-Djava.security.manager");
        properties.append(" " + CentralPAPropertyRepository.PA_HOME.getCmdLine() +
            CentralPAPropertyRepository.PA_HOME.getValue());
        properties.append(" " + CentralPAPropertyRepository.JAVA_SECURITY_POLICY.getCmdLine() +
            CentralPAPropertyRepository.JAVA_SECURITY_POLICY.getValue());
        properties.append(" " + CentralPAPropertyRepository.LOG4J.getCmdLine() +
            CentralPAPropertyRepository.LOG4J.getValue());
        properties.append(" " + PASchedulerProperties.SCHEDULER_HOME.getCmdLine() +
            PASchedulerProperties.SCHEDULER_HOME.getValueAsString());
        properties.append(" " + PAResourceManagerProperties.RM_HOME.getCmdLine() +
            PAResourceManagerProperties.RM_HOME.getValueAsString());
        vContract.setVariableFromProgram("jvmargDefinedByTest", properties.toString(),
                VariableContractType.DescriptorDefaultVariable);
        gcmad = PAGCMDeployment.loadApplicationDescriptor(startForkedSchedulerApplication, vContract);
        gcmad.startDeployment();
    }

    /**
     * Init connection as user
     * @throws Exception
     */
    private static void connect() throws Exception {
        SchedulerAuthenticationInterface authInt = getSchedulerAuth();
        Credentials cred = Credentials.createCredentials(username, password, authInt.getPublicKey());
        adminSchedInterface = authInt.login(cred);
        initEventReceiver(adminSchedInterface);
    }

    private static SchedulerMonitorsHandler getMonitorsHandler() {
        if (monitorsHandler == null) {
            monitorsHandler = new SchedulerMonitorsHandler();
        }
        return monitorsHandler;
    }

    public static void setExecutable(String filesList) throws IOException {
        Runtime.getRuntime().exec("chmod u+x " + filesList);
    }

}
