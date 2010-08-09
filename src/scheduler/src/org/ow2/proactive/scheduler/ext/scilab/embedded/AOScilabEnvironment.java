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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package org.ow2.proactive.scheduler.ext.scilab.embedded;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.security.KeyException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import javasci.SciData;
import javasci.SciStringMatrix;

import javax.security.auth.login.LoginException;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.body.request.RequestFilter;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.scheduler.common.NotificationData;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.SchedulerAuthenticationInterface;
import org.ow2.proactive.scheduler.common.SchedulerConnection;
import org.ow2.proactive.scheduler.common.SchedulerEvent;
import org.ow2.proactive.scheduler.common.SchedulerEventListener;
import org.ow2.proactive.scheduler.common.exception.SchedulerException;
import org.ow2.proactive.scheduler.common.exception.UserException;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobPriority;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.JobStatus;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.job.UserIdentification;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.TaskInfo;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.util.SchedulerLoggers;
import org.ow2.proactive.scheduler.ext.scilab.ScilabTaskException;
import org.ow2.proactive.scripting.InvalidScriptException;
import org.ow2.proactive.scripting.SelectionScript;


/**
 * This active object handles the interaction between Scilab and the Scheduler it creates Scheduler jobs and receives results
 *
 * @author The ProActive Team
 */
public class AOScilabEnvironment implements Serializable, SchedulerEventListener, InitActive, RunActive {

    /**  */
    private static final long serialVersionUID = 21L;

    private boolean loggedin;

    /**
     * Connection to the scheduler
     */
    private Scheduler scheduler;

    /**
     * Id of the current job running
     */
    private String currentJobId = null;

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

    //private ArrayList<SciData> results;
    private ArrayList<ResultsAndLogs> results;

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

        Credentials creds = null;
        try {
            creds = Credentials.createCredentials(user, passwd, auth.getPublicKey());
        } catch (KeyException e) {
            throw new LoginException("" + e);
        } catch (LoginException e) {
            throw new LoginException("Could not retrieve public key, contact the Scheduler admininistrator" +
                e);
        }
        this.scheduler = auth.login(creds);

        loggedin = true;

        this.scheduler.addEventListener((AOScilabEnvironment) stubOnThis, false,
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
        //results = new ArrayList<SciData>();
        results = new ArrayList<ResultsAndLogs>();
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
            errorToThrow = new IllegalStateException("The scheduler has been stopped");
        } else if (jobKilled) {
            // Job killed
            System.err.println("[AOScilabEnvironment] The job has been killed");
            errorToThrow = new IllegalStateException("The job has been killed");
        }

        try {
            if (errorToThrow != null) {

                // Error inside job
                if (debug) {
                    System.err.println("[AOScilabEnvironment] Scheduler Exception");
                }
                answer.add(new ResultsAndLogs(null, null, errorToThrow, false, true));

            } else {
                // Normal termination
                for (int i = 0; i < results.size(); i++) {
                    answer.add(results.get(i));
                }
            }
        } finally {

            results.clear();
            currentJobId = null;
            errorToThrow = null;
            errorLogs = null;
            jobKilled = false;
        }
        return answer;
    }

    /**
     * Submit a new bunch of tasks to the scheduler, throws a runtime exception if a job is currently running
     *
     * @param inputScripts input scripts (scripts executed before the main one)
     * @param mainScripts  main scripts
     * @param priority     priority of the job
     */
    public ArrayList<ResultsAndLogs> solve(String[] inputScripts, String functionName,
            String functionsDefinition, String mainScripts, URL scriptURL, JobPriority priority, boolean debug)
            throws Throwable {
        if (schedulerStopped) {
            System.err.println("[AOScilabEnvironment] The Scheduler is stopped");
            return new ArrayList<ResultsAndLogs>();
        }

        this.debug = debug;

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
        job.setCancelJobOnError(false);
        job.setDescription("Set of parallel scilab tasks");
        // the external log files as the output is forwarded into Scilab directly,
        // in debug mode you might want to read these files though
        if (debug) {
            File logFile = new File(System.getProperty("java.io.tmpdir"), "Scilab_job_" + lastJobId + ".log");
            System.out.println("[AOScilabEnvironment] Log file created at :" + logFile);
            job.setLogFile(logFile.getPath());
        }
        if (debug) {
            System.out.println("[AOScilabEnvironment] function name:");
            System.out.println(functionName);
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
            schedulerTask.addArgument("functionName", functionName);
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
                schedulerTask.addSelectionScript(sscript);
            }

            try {
                job.addTask(schedulerTask);
            } catch (UserException e) {
                e.printStackTrace();
            }

        }

        try {
            currentJobId = scheduler.submit(job).value();
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
    private void jobDidNotSucceed(String jobId, Throwable ex, boolean printStack, String logs) {
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
                if (debug) {
                    System.out.println("[AOScilabEnvironment] Received Scheduler '" + eventType + "' event");
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
                String jid = info.getJobId().value();
                if (info.getStatus() == JobStatus.KILLED) {
                    if (debug) {
                        System.out.println("[AOScilabEnvironment] Received job killed event...");
                    }

                    // Filtering the right job
                    if ((currentJobId == null) || !jid.equals(currentJobId)) {
                        return;
                    }
                    this.jobKilled = true;
                } else {
                    if (debug) {
                        System.out.println("[AOScilabEnvironment] Received job finished event...");
                    }

                    if (info == null) {
                        if (debug) {
                            System.out.println("[AOScilabEnvironment] Event refused : job info is null...");
                        }
                        return;
                    }

                    // Filtering the right job
                    if (!jid.equals(currentJobId)) {
                        if (debug) {
                            System.out.println("[AOScilabEnvironment] Event refused : event job id is " +
                                jid + " , local job id is " + currentJobId + "...");
                        }
                        return;
                    }
                    // Getting the Job result from the Scheduler
                    JobResult jResult = null;

                    try {
                        jResult = scheduler.getJobResult(jid);
                        if (jResult == null) {
                            jobDidNotSucceed(jid, new RuntimeException("[AOScilabEnvironment] Job id = " +
                                info.getJobId() + " was not returned by the scheduler"), true, null);
                            return;
                        }
                    } catch (SchedulerException e) {
                        jobDidNotSucceed(jid, e, true, null);
                        return;
                    }

                    updateJobResult(jid, jResult);

                    isJobFinished = true;

                }
                break;
        }
    }

    private void updateJobResult(String jid, JobResult jResult) {

        if (debug) {
            System.out.println("[AOScilabEnvironment] Updating results of job: " + jResult.getName() + "(" +
                jid + ")");
        }

        // Geting the task results from the job result
        Map<String, TaskResult> task_results = null;
        //if (jResult.hadException()) {
        //    task_results = jResult.getExceptionResults();
        //} else {
        // sorted results

        task_results = jResult.getAllResults();
        //}
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
                jobDidNotSucceed(jid, new RuntimeException("[AOScilabEnvironment] Task id = " + key +
                    " was not returned by the scheduler"), false, null);

            } else {

                String logs = res.getOutput().getAllLogs(true);
                String logs2 = res.getOutput().getStderrLogs(true);

                if (logs2.length() > 0 && debug) {
                    System.err.println(logs);
                }
                String logs3 = res.getOutput().getStdoutLogs(false);
                if (logs3.length() > 0 && debug) {
                    System.out.println(logs3);
                }

                if (res.hadException()) {
                    //Exception took place inside the framework
                    if (res.getException() instanceof ScilabTaskException) {
                        results.add(new ResultsAndLogs(null, logs, null, true, false));
                        //jobDidNotSucceed(jid, res.getException(), false, logs);
                    } else {
                        results.add(new ResultsAndLogs(null, logs, res.getException(), false, false));
                        //jobDidNotSucceed(jid, res.getException(), true, logs);
                    }

                } else {
                    // Normal success
                    SciData computedResult = null;

                    try {
                        computedResult = ((ArrayList<SciData>) res.value()).get(0);
                        results.add(new ResultsAndLogs(computedResult, logs, null, false, false));

                    } catch (Throwable e2) {
                        jobDidNotSucceed(jid, e2, true, logs);
                    }
                }
            }
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

        /**  */
        private static final long serialVersionUID = 21L;

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
        //ArrayList<ResultsAndLogs> ret = aose.solve(new String[] { "in=2" }, "out=in*in;", null, null,
        //        JobPriority.NORMAL, true);
        //System.out.println(ret);
    }

}
