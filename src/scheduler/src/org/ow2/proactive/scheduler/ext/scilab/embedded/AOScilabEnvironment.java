/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
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
package org.ow2.proactive.scheduler.ext.scilab.embedded;

import javasci.SciData;
import javasci.SciStringMatrix;
import org.apache.log4j.Logger;
import org.objectweb.proactive.*;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.body.request.RequestFilter;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.ow2.proactive.scheduler.common.*;
import org.ow2.proactive.scheduler.common.exception.SchedulerException;
import org.ow2.proactive.scheduler.common.exception.UserException;
import org.ow2.proactive.scheduler.common.job.*;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.TaskInfo;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.util.SchedulerLoggers;
import org.ow2.proactive.scripting.InvalidScriptException;
import org.ow2.proactive.scripting.SelectionScript;

import javax.security.auth.login.LoginException;
import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;


/**
 * This active object handles the interaction between Scilab and the Scheduler it creates Scheduler jobs and receives results
 *
 * @author The ProActive Team
 */
public class AOScilabEnvironment implements Serializable, SchedulerEventListener, InitActive, RunActive {

    private boolean loggedin;

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

    private ArrayList<SciData> results;

    private ArrayList<String> job_logs;

    /**
     * WaitAllResults Request waiting to be served
     */
    protected Request pendingRequest;

    /**
     * log4j logger
     */
    protected static Logger logger = ProActiveLogger.getLogger(SchedulerLoggers.SCILAB);

    /**
     * Proactive stub on this AO
     */
    private AOScilabEnvironment stubOnThis;

    /**
     * Is the AO terminated ?
     */
    private boolean terminated;

    /**
     * Is the current job finished ?
     */
    private boolean isJobFinished;

    /**
     * Current debug mode
     */
    private boolean debug;

    private boolean jobKilled = false;
    private boolean schedulerStopped = false;

    /**
     * Exception to throw in case of error
     */
    private Throwable errorToThrow;
    private String errorLogs;
    private SchedulerAuthenticationInterface auth;

    public AOScilabEnvironment() {
        super();
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

        loggedin = true;

        this.scheduler.addSchedulerEventListener((AOScilabEnvironment) stubOnThis, false,
                SchedulerEvent.JOB_RUNNING_TO_FINISHED, SchedulerEvent.KILLED, SchedulerEvent.SHUTDOWN,
                SchedulerEvent.SHUTTING_DOWN);

    }

    public void startLogin() {
        loggedin = false;
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                LoginFrame lf = new LoginFrame(stubOnThis);
                lf.start();
            }
        });
    }

    public boolean isLoggedIn() {
        return loggedin;
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
        stubOnThis = (AOScilabEnvironment) PAActiveObject.getStubOnThis();
        results = new ArrayList<SciData>();
        job_logs = new ArrayList<String>();
    }

    /**
     * Request to join the scheduler at the given url
     *
     * @param url url of the scheduler
     * @return true if success, false otherwise
     */
    public void join(String url) throws SchedulerException {
        auth = SchedulerConnection.join(url);
    }

    /**
     * Returns all the results in an array or throw a RuntimeException in case of error
     *
     * @return array of SciData tokens
     */
    public ArrayList<ResultsAndLogs> waitAllResults() {
        ArrayList<ResultsAndLogs> answer = new ArrayList<ResultsAndLogs>();
        if (debug) {
            logger.info("[AOScilabEnvironment] Receiving results back...");
        }

        if (schedulerStopped) {
            System.err.println("[AOScilabEnvironment] The scheduler has been stopped");
        } else if (jobKilled) {
            // Job killed
            System.err.println("[AOScilabEnvironment] The job has been killed");
        } else if (errorToThrow != null) {
            // Error inside job
            if (debug) {
                System.err.println("[AOScilabEnvironment] Exception inside the job");
            }
            answer.add(new ResultsAndLogs(new SciStringMatrix("out", 0, 0), errorLogs, errorToThrow));

        } else {
            // Normal termination
            for (int i = 0; i < results.size(); i++) {
                answer.add(new ResultsAndLogs(results.get(i), job_logs.get(i), null));
            }
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
    public ArrayList<ResultsAndLogs> solve(String[] inputScripts, String functionsDefinition,
            String mainScripts, URL scriptURL, JobPriority priority, boolean debug) {
        if (schedulerStopped) {
            System.err.println("[AOScilabEnvironment] The Scheduler is stopped");
            return new ArrayList<ResultsAndLogs>();
        }

        this.debug = debug;
        this.job_logs.clear();
        // We store the script selecting the nodes to use it later at termination.

        if (currentJobId != null) {
            throw new RuntimeException("[AOScilabEnvironment] The Scheduler is already busy with one job");
        }

        if (debug) {
            System.out
                    .println("[AOScilabEnvironment] Submitting job of " + inputScripts.length + " tasks...");
        }

        // We verify that the script is available (otherwise we just ignore it)
        URL availableScript = null;
        if (scriptURL != null) {
            try {
                InputStream is = scriptURL.openStream();
                is.close();
                availableScript = scriptURL;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Creating a task flow job
        TaskFlowJob job = new TaskFlowJob();
        job.setName("Scilab Environment Job " + lastJobId++);
        job.setPriority(priority);
        job.setCancelJobOnError(true);
        job.setDescription("Set of parallel scilab tasks");
        // the external log files as the output is forwarded into Scilab directly,
        // in debug mode you might want to read these files though
        if (debug) {
            System.out.println("[AOScilabEnvironment] Log file created at :" +
                new File("Scilab_job_" + lastJobId + ".log"));
            job.setLogFile("Scilab_job_" + lastJobId + ".log");
        }
        if (debug) {
            System.out.println("[AOScilabEnvironment] function definition:");
            System.out.println(functionsDefinition);
            System.out.println("[AOScilabEnvironment] main script:");
            System.out.println(mainScripts);
            System.out.println("[AOScilabEnvironment] input scripts:");
        }
        for (int i = 0; i < inputScripts.length; i++) {

            JavaTask schedulerTask = new JavaTask();

            schedulerTask.setName("" + lastTaskId++);
            schedulerTask.setPreciousResult(true);
            schedulerTask.addArgument("input", inputScripts[i]);

            if (debug) {
                schedulerTask.addArgument("debug", "true");
                System.out.println(inputScripts[i]);
            }
            schedulerTask.addArgument("functionsDefinition", functionsDefinition);
            schedulerTask.addArgument("script", mainScripts);

            schedulerTask.addArgument("outputs", "out");
            schedulerTask.setExecutableClassName("org.ow2.proactive.scheduler.ext.scilab.ScilabTask");
            if (availableScript != null) {
                SelectionScript sscript = null;
                try {
                    sscript = new SelectionScript(scriptURL, null, true);
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
        this.errorLogs = null;
        // The last call puts a method in the RequestQueue
        // that won't be executed until all the results are received (see runactivity)
        return stubOnThis.waitAllResults();
    }

    public void terminate() {
        this.terminated = true;
    }

    /**
     * Handles case of an unsuccessful job
     *
     * @param jobId      id of the job
     * @param ex         exception thrown
     * @param printStack do we print the stack trace ?
     * @param logs       logs of the task creating the problem
     */
    private void jobDidNotSucceed(JobId jobId, Throwable ex, boolean printStack, String logs) {
        System.err.println("[AOScilabEnvironment] Job did not succeed");
        if (printStack) {
            ex.printStackTrace();
        }
        if (errorToThrow == null) {
            errorToThrow = ex;
        }
        errorLogs = logs;
        isJobFinished = true;
    }

    /**
    * @see org.ow2.proactive.scheduler.common.SchedulerEventListener#schedulerStateUpdatedEvent(org.ow2.proactive.scheduler.common.SchedulerEvent)
    */
    public void schedulerStateUpdatedEvent(SchedulerEvent eventType) {
        switch (eventType) {
            case KILLED:
            case SHUTDOWN:
            case SHUTTING_DOWN:
                if (logger.isDebugEnabled()) {
                    logger.info("Received Scheduler '" + eventType + "' event");
                }
                schedulerStopped = true;
                break;
        }

    }

    /**
     * @see org.ow2.proactive.scheduler.common.SchedulerEventListener#jobStateUpdatedEvent(org.ow2.proactive.scheduler.common.NotificationData)
     */
    public void jobStateUpdatedEvent(NotificationData<JobInfo> notification) {
        switch (notification.getEventType()) {
            case JOB_RUNNING_TO_FINISHED:
                JobInfo info = notification.getData();
                if (info.getStatus() == JobStatus.KILLED) {
                    if (logger.isDebugEnabled()) {
                        logger.info("Received job killed event...");
                    }

                    // Filtering the right job
                    if ((currentJobId == null) || !info.getJobId().equals(currentJobId)) {
                        return;
                    }
                    this.jobKilled = true;
                } else {
                    if (logger.isDebugEnabled()) {
                        logger.info("Received job finished event...");
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
                        if (jResult == null) {
                            jobDidNotSucceed(info.getJobId(), new RuntimeException(
                                "[AOScilabEnvironment] Job id = " + info.getJobId() +
                                    " was not returned by the scheduler"), false, null);
                            return;
                        }
                    } catch (SchedulerException e) {
                        jobDidNotSucceed(info.getJobId(), e, true, null);
                        return;
                    }

                    if (debug) {
                        System.out.println("[AOScilabEnvironment] Updating results of job: " +
                            jResult.getName() + "(" + info.getJobId() + ")");
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
                        if (debug) {
                            System.out.println("[AOScilabEnvironment] Looking for result of task: " + key);
                        }

                        // No result received
                        if (res == null) {
                            jobDidNotSucceed(info.getJobId(), new RuntimeException(
                                "[AOScilabEnvironment] Task id = " + key +
                                    " was not returned by the scheduler"), false, null);

                        } else if (res.hadException()) {
                            //Exception took place inside the framework
                            jobDidNotSucceed(info.getJobId(), res.getException(), true, res.getOutput()
                                    .getAllLogs(true));

                        } else {
                            // Normal success
                            SciData computedResult = null;
                            String logs = null;
                            try {
                                logs = res.getOutput().getAllLogs(true);
                                computedResult = ((ArrayList<SciData>) res.value()).get(0);
                                results.add(computedResult);
                                // We print the logs of the job, if any
                                if (debug && logs.length() > 0) {
                                    System.out.println(logs);
                                }

                                job_logs.add(logs);

                            } catch (Throwable e2) {
                                jobDidNotSucceed(info.getJobId(), e2, true, logs);
                            }
                        }
                    }

                    isJobFinished = true;

                }
                break;
        }
    }

    /**
     * @see org.ow2.proactive.scheduler.common.SchedulerEventListener#jobSubmittedEvent(org.ow2.proactive.scheduler.common.job.JobState)
     */
    public void jobSubmittedEvent(JobState job) {
        // TODO Auto-generated method stub

    }

    /**
     * @see org.ow2.proactive.scheduler.common.SchedulerEventListener#taskStateUpdatedEvent(org.ow2.proactive.scheduler.common.NotificationData)
     */
    public void taskStateUpdatedEvent(NotificationData<TaskInfo> notification) {
        // TODO Auto-generated method stub

    }

    /**
     * @see org.ow2.proactive.scheduler.common.SchedulerEventListener#usersUpdatedEvent(org.ow2.proactive.scheduler.common.NotificationData)
     */
    public void usersUpdatedEvent(NotificationData<UserIdentification> notification) {
        // TODO Auto-generated method stub
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
                    System.out.println("[AOScilabEnvironment] Request received");
                }
                // We detect a waitXXX request in the request queue
                Request waitRequest = service.getOldest("waitAllResults");
                if (waitRequest != null) {
                    if (pendingRequest == null) {
                        // if there is one and there was none previously found we remove it and store it for later
                        pendingRequest = waitRequest;
                        if (debug) {
                            System.out.println("[AOScilabEnvironment] Blocking removing waitAllResults");
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

    public static void main(String[] args) throws ActiveObjectCreationException, NodeException,
            LoginException, SchedulerException {

        AOScilabEnvironment aose = (AOScilabEnvironment) PAActiveObject.newActive(AOScilabEnvironment.class
                .getName(), new Object[] {});
        aose.join("//localhost");
        aose.login("demo", "demo");
        ArrayList<ResultsAndLogs> ret = aose.solve(new String[] { "in=2" }, "out=in*in;", null, null,
                JobPriority.NORMAL, true);
        System.out.println(ret);
    }

}
