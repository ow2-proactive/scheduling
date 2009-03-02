/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package org.ow2.proactive.scheduler.ext.matlab.embedded;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import javax.security.auth.login.LoginException;

import org.apache.log4j.Logger;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.body.request.RequestFilter;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.ow2.proactive.scheduler.common.SchedulerAuthenticationInterface;
import org.ow2.proactive.scheduler.common.SchedulerConnection;
import org.ow2.proactive.scheduler.common.SchedulerEvent;
import org.ow2.proactive.scheduler.common.SchedulerEventListener;
import org.ow2.proactive.scheduler.common.UserSchedulerInterface;
import org.ow2.proactive.scheduler.common.exception.SchedulerException;
import org.ow2.proactive.scheduler.common.exception.UserException;
import org.ow2.proactive.scheduler.common.job.Job;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobPriority;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.job.UserIdentification;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.TaskInfo;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.util.SchedulerLoggers;
import org.ow2.proactive.scheduler.ext.matlab.exception.MatlabTaskException;
import org.ow2.proactive.scripting.InvalidScriptException;
import org.ow2.proactive.scripting.SelectionScript;

import ptolemy.data.Token;


/**
 * This active object handles the connection between Matlab and the Scheduler directly from the Matlab environment
 *
 * @author The ProActive Team
 */
public class AOMatlabEnvironment implements Serializable, SchedulerEventListener, InitActive, RunActive {

    /**
     * URL to the scheduler
     */
    private String schedulerUrl;

    /**
     * Connection to the scheduler
     */
    private UserSchedulerInterface scheduler;

    /**
     * Id of the current job running
     */
    private JobId currentJobId = null;

    /**
     * Internal job id for job description only
     */
    private long lastJobId = 0;

    /**
     * Id of the last task created + 1
     */
    private long lastTaskId = 0;

    /**
     * Results gathered
     */

    private ArrayList<Token> results;

    /**
     * WaitAllResults Request waiting to be served
     */
    protected Request pendingRequest;

    /**
     * log4j logger
     */
    protected static Logger logger = ProActiveLogger.getLogger(SchedulerLoggers.MATLAB);
    private static final boolean debug = logger.isDebugEnabled();
    private boolean debugCurrentJob;

    /**
     * Proactive stub on this AO
     */
    private AOMatlabEnvironment stubOnThis;

    /**
     * Is the AO terminated ?
     */
    private boolean terminated;

    /**
     * Is the current job finished ?
     */
    private boolean isJobFinished;

    private boolean jobKilled = false;
    private boolean schedulerStopped = false;

    /**
     * Exception to throw in case of error
     */
    private Throwable errorToThrow;
    private SchedulerAuthenticationInterface auth;

    /**
     * Constructs the environment AO
     */
    public AOMatlabEnvironment() {

    }

    /**
     * Trys to log into the scheduler, using the provided user and password
     *
     * @param user   username
     * @param passwd password
     * @throws LoginException     if the login fails
     * @throws SchedulerException if an other error occurs
     */
    public void login(String user, String passwd) throws LoginException, SchedulerException {

        this.scheduler = auth.logAsUser(user, passwd);

        this.scheduler.addSchedulerEventListener((AOMatlabEnvironment) stubOnThis,
                SchedulerEvent.JOB_RUNNING_TO_FINISHED, SchedulerEvent.KILLED, SchedulerEvent.SHUTDOWN,
                SchedulerEvent.SHUTTING_DOWN);

    }

    /**
     * Tells if we are connected to the scheduler or not
     *
     * @return answer
     */
    public boolean isConnected() {
        return this.scheduler != null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.proactive.InitActive#initActivity(org.objectweb.proactive.Body)
     */
    @SuppressWarnings("unchecked")
    public void initActivity(Body body) {
        stubOnThis = (AOMatlabEnvironment) PAActiveObject.getStubOnThis();
        results = new ArrayList<Token>();
    }

    /**
     * Request to join the scheduler at the given url
     *
     * @param url url of the scheduler
     * @return true if success, false otherwise
     */
    public boolean join(String url) {
        try {
            auth = SchedulerConnection.join(url);
        } catch (Exception e1) {
            System.err.println(e1.getMessage());
            return false;
        }
        this.schedulerUrl = url;
        return true;
    }

    /**
     * Returns all the results in an array or throw a RuntimeException in case of error
     *
     * @return array of ptolemy tokens
     */
    public ArrayList<Token> waitAllResults() {
        ArrayList<Token> answer = null;
        if (debugCurrentJob) {
            System.out.println("Sending the results back...");
        }

        if (schedulerStopped) {
            System.err.println("The scheduler has been stopped");
            answer = new ArrayList<Token>();
        } else if (jobKilled) {
            // Job killed 
            System.err.println("The job has been killed");
            answer = new ArrayList<Token>();
        } else if (errorToThrow != null) {
            // Error inside job
            if (errorToThrow instanceof MatlabTaskException) {
                System.err.println(errorToThrow.getMessage());
                answer = new ArrayList<Token>();
            } else {
                results.clear();
                currentJobId = null;
                jobKilled = false;
                throw new RuntimeException(errorToThrow);
            }
        } else {
            // Normal termination
            answer = new ArrayList<Token>(results);
        }

        results.clear();
        currentJobId = null;
        jobKilled = false;
        return answer;
    }

    /**
     * Submit a new bunch of tasks to the scheduler, throws a runtime exception if a job is currently running
     *
     * @param inputScripts input scripts (scripts executed before the main one)
     * @param mainScripts  main scripts
     * @param priority     priority of the job
     */
    public ArrayList<Token> solve(String[] inputScripts, String[] mainScripts, URL scriptURL,
            JobPriority priority, boolean debug) {
        debugCurrentJob = debug;

        if (schedulerStopped) {
            System.err.println("The Scheduler is stopped");
            return new ArrayList<Token>();
        }
        // We store the script selecting the nodes to use it later at termination.

        if (currentJobId != null) {
            throw new RuntimeException("The Scheduler is already busy with one job");
        }

        if (debugCurrentJob) {
            System.out.println("Submitting job of " + mainScripts.length + " tasks...");
        }

        // We verify that the script is available (otherwise we just ignore it)
        URL availableScript = null;
        try {
            InputStream is = scriptURL.openStream();
            is.close();
            availableScript = scriptURL;
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Creating a task flow job
        TaskFlowJob job = new TaskFlowJob();
        job.setName("Matlab Environment Job " + lastJobId++);
        job.setPriority(priority);
        job.setCancelJobOnError(true);
        job.setDescription("Set of parallel matlab tasks");
        for (int i = 0; i < mainScripts.length; i++) {

            JavaTask schedulerTask = new JavaTask();

            schedulerTask.setName("" + lastTaskId++);
            schedulerTask.setPreciousResult(true);
            schedulerTask.addArgument("input", inputScripts[i]);
            schedulerTask.addArgument("script", mainScripts[i]);
            schedulerTask.setDescription(mainScripts[i]);
            if (debugCurrentJob) {
                schedulerTask.addArgument("debug", "true");
            }
            schedulerTask.setExecutableClassName("org.ow2.proactive.scheduler.ext.matlab.SimpleMatlab");
            if (availableScript != null) {
                SelectionScript sscript = null;
                try {
                    sscript = new SelectionScript(availableScript, new String[] {}, true);
                } catch (InvalidScriptException e1) {
                    throw new RuntimeException(e1);
                }
                schedulerTask.setSelectionScript(sscript);
            }

            try {
                job.addTask(schedulerTask);
            } catch (UserException e) {
                e.printStackTrace();
            }

        }

        try {
            currentJobId = scheduler.submit(job);
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }

        this.isJobFinished = false;
        this.jobKilled = false;
        this.errorToThrow = null;
        // The last call puts a method in the RequestQueue 
        // that won't be executed until all the results are received (see runactivity)
        return stubOnThis.waitAllResults();
    }

    public void terminate() {
        this.terminated = true;
    }

    public void jobChangePriorityEvent(JobInfo info) {
        // TODO Auto-generated method stub

    }

    public void jobPausedEvent(JobInfo info) {
        // TODO Auto-generated method stub

    }

    public void jobPendingToRunningEvent(JobInfo info) {
        // TODO Auto-generated method stub

    }

    public void jobRemoveFinishedEvent(JobInfo info) {
        // TODO Auto-generated method stub

    }

    public void jobResumedEvent(JobInfo info) {
        // TODO Auto-generated method stub

    }

    public void jobRunningToFinishedEvent(JobInfo info) {
        if (info.getState() == JobState.KILLED) {
            if (debugCurrentJob) {
                System.out.println("Received job killed event...");
            }

            // Filtering the right job
            if ((currentJobId == null) || !info.getJobId().equals(currentJobId)) {
                return;
            }
            this.jobKilled = true;
        } else {
            if (debugCurrentJob) {
                System.out.println("Received job finished event...");
            }

            if (info == null) {
                return;
            }

            // Filtering the right job
            if (!info.getJobId().equals(currentJobId)) {
                return;
            }
            // Getting the Job result from the Scheduler
            JobResult jResult = null;

            try {
                jResult = scheduler.getJobResult(info.getJobId());
            } catch (SchedulerException e) {
                jobDidNotSucceed(info.getJobId(), e, true, null);
                return;
            }

            if (debugCurrentJob) {
                System.out.println("Updating results of job: " + jResult.getName() + "(" + info.getJobId() +
                    ")");
            }

            // Geting the task results from the job result
            Map<String, TaskResult> task_results = null;
            if (jResult.hadException()) {
                task_results = jResult.getExceptionResults();
            } else {
                // sorted results

                task_results = jResult.getAllResults();
            }
            ArrayList<Integer> keys = new ArrayList<Integer>();
            for (String key : task_results.keySet()) {
                keys.add(Integer.parseInt(key));
            }
            Collections.sort(keys);
            // Iterating over the task results
            for (Integer key : keys) {
                TaskResult res = task_results.get("" + key);
                if (debugCurrentJob) {
                    System.out.println("Looking for result of task: " + key);
                }
                String logs = res.getOutput().getAllLogs(false);

                // No result received
                if (res == null) {
                    jobDidNotSucceed(info.getJobId(), new RuntimeException("Task id = " + key +
                        " was not returned by the scheduler"), false, null);

                } else if (res.hadException()) {

                    //Exception took place inside the framework
                    if (res.getException() instanceof ptolemy.kernel.util.IllegalActionException) {
                        // We filter this specific exception which means that the "out" variable was not set by the function 
                        // due to an error inside the script or a missing licence 

                        jobDidNotSucceed(info.getJobId(), new MatlabTaskException(logs), false, logs);
                    } else {
                        // For other types of exception we forward it as it is.
                        jobDidNotSucceed(info.getJobId(), res.getException(), true, logs);
                    }
                } else {
                    // Normal success
                    Token computedResult = null;
                    try {
                        computedResult = (Token) res.value();
                        results.add(computedResult);
                        // We print the logs of the job, if any
                        if (logs.length() > 0) {
                            System.out.println(logs);
                        }
                    } catch (ptolemy.kernel.util.IllegalActionException e1) {
                        jobDidNotSucceed(info.getJobId(), new MatlabTaskException(logs), false, logs);
                    } catch (Throwable e2) {
                        jobDidNotSucceed(info.getJobId(), e2, true, logs);
                    }
                }
            }

            isJobFinished = true;
        }
    }

    public void jobSubmittedEvent(Job job) {
        // TODO Auto-generated method stub

    }

    public void schedulerFrozenEvent() {
        // TODO Auto-generated method stub

    }

    public void schedulerKilledEvent() {
        if (debug) {
            logger.debug("Received Scheduler killed event");
        }
        schedulerStopped = true;

    }

    public void schedulerPausedEvent() {
        // TODO Auto-generated method stub

    }

    public void schedulerResumedEvent() {
        // TODO Auto-generated method stub

    }

    public void schedulerShutDownEvent() {
        if (debug) {
            logger.debug("Received Scheduler ShutDown event");
        }
        schedulerStopped = true;

    }

    public void schedulerShuttingDownEvent() {
        if (debug) {
            logger.debug("Received Scheduler Shutting Down event");
        }
        schedulerStopped = true;
    }

    public void schedulerStartedEvent() {
        // TODO Auto-generated method stub

    }

    public void schedulerStoppedEvent() {
        if (debug) {
            logger.debug("Received Scheduler Stop event");
        }
        schedulerStopped = true;
    }

    public void taskPendingToRunningEvent(TaskInfo info) {
        // TODO Auto-generated method stub

    }

    public void taskRunningToFinishedEvent(TaskInfo info) {
        // TODO Auto-generated method stub

    }

    /**
     * Handles case of an unsucessful job
     *
     * @param jobId      id of the job
     * @param ex         exception thrown
     * @param printStack do we print the stack trace ?
     * @param logs       logs of the task creating the problem
     */
    private void jobDidNotSucceed(JobId jobId, Throwable ex, boolean printStack, String logs) {
        System.err.println("Job did not succeed");
        if (logs.length() > 0) {
            System.out.println(logs);
        }
        if (printStack) {
            ex.printStackTrace();
        }
        if (errorToThrow == null) {
            errorToThrow = ex;
        }
        isJobFinished = true;
    }

    /**
     * {@inheritDoc}
     */
    public void runActivity(final Body body) {
        Service service = new Service(body);
        while (!terminated) {
            try {

                service.waitForRequest();
                if (debug) {
                    logger.debug("Request received");
                }
                // We detect a waitXXX request in the request queue
                Request waitRequest = service.getOldest("waitAllResults");
                if (waitRequest != null) {
                    if (pendingRequest == null) {
                        // if there is one and there was none previously found we remove it and store it for later
                        pendingRequest = waitRequest;
                        if (debug) {
                            logger.debug("Blocking removing waitAllResults");
                        }
                        service.blockingRemoveOldest("waitAllResults");
                        //Request submitRequest = buildRequest(body);
                        //service.serve(submitRequest);
                    } else {
                        // if there is one and there was another one pending, we serve it immediately (it's an error)
                        service.serveOldest("waitAllResults");
                    }
                }

                // we serve everything else which is not a waitXXX method
                // Careful, the order is very important here, we need to serve the solve method before the waitXXX
                service.serveAll(new FindNotWaitFilter());

                // we maybe serve the pending waitXXX method if there is one and if the necessary results are collected
                maybeServePending(service);
            } catch (Throwable ex) {
                ex.printStackTrace();
            }
        }

        // we clear the service to avoid dirty pending requests 
        service.flushAll();
        // we block the communications because a getTask request might still be coming from a worker created just before the master termination
        body.blockCommunication();
        // we finally terminate the master
        body.terminate();
    }

    /**
     * If there is a pending waitXXX method, we serve it if the necessary results are collected
     *
     * @param service
     */
    protected void maybeServePending(Service service) {
        if (pendingRequest != null) {
            if (isJobFinished() || jobKilled || schedulerStopped) {
                servePending(service);
            }
        }
    }

    /**
     * Serve the pending waitXXX method
     *
     * @param service
     */
    protected void servePending(Service service) {
        Request req = pendingRequest;
        pendingRequest = null;
        service.serve(req);
    }

    protected boolean isJobFinished() {
        return this.isJobFinished;
    }

    /**
     * @author The ProActive Team
     *         Internal class for filtering requests in the queue
     */
    protected class FindNotWaitFilter implements RequestFilter {

        /**
         * Creates the filter
         */
        public FindNotWaitFilter() {
        }

        /**
         * {@inheritDoc}
         */
        public boolean acceptRequest(final Request request) {
            // We find all the requests which can't be served yet
            String name = request.getMethodName();
            if (name.equals("waitAllResults")) {
                return false;
            }
            return true;
        }
    }

    public void schedulerRMDownEvent() {
        // TODO Auto-generated method stub

    }

    public void schedulerRMUpEvent() {
        // TODO Auto-generated method stub

    }

    public void usersUpdate(UserIdentification userIdentification) {
        // TODO Auto-generated method stub

    }

    public void taskWaitingForRestart(TaskInfo info) {
        // TODO Auto-generated method stub

    }

    public void schedulerPolicyChangedEvent(String newPolicyName) {
        // TODO Auto-generated method stub

    }

}
