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
package org.ow2.proactive.scheduler.ext.matlab.embedded;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

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
import org.ow2.proactive.scheduler.common.NotificationData;
import org.ow2.proactive.scheduler.common.SchedulerAuthenticationInterface;
import org.ow2.proactive.scheduler.common.SchedulerConnection;
import org.ow2.proactive.scheduler.common.SchedulerEvent;
import org.ow2.proactive.scheduler.common.SchedulerEventListener;
import org.ow2.proactive.scheduler.common.UserSchedulerInterface;
import org.ow2.proactive.scheduler.common.exception.SchedulerException;
import org.ow2.proactive.scheduler.common.exception.UserException;
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
     *
     */
    private static final long serialVersionUID = 10L;

    /**
     * URL to the scheduler
     */
    private String schedulerUrl;

    /**
     * Connection to the scheduler
     */
    private UserSchedulerInterface scheduler;

    /**
     * Id of the current jobs running
     */
    private HashMap<String, MatlabJobInfo> currentJobs = new HashMap<String, MatlabJobInfo>();
    /**
     * job id of the last submitted job (useful for runactivity method)
     */
    private Queue<String> lastSubJobId = new LinkedList<String>();

    private String waitAllResultsJobID;

    /**
     * Internal job id for job description only
     */
    private long lastGenJobId = 0;

    /**
     * Id of the last task created + 1
     */
    private long lastTaskId = 0;

    /**
     * Access to object output stream to save current jobs
     */
    private File jobLogFile;

    /**
     * log4j logger
     */
    protected static Logger logger = ProActiveLogger.getLogger(SchedulerLoggers.MATLAB);
    private static final boolean debug = logger.isDebugEnabled();

    /**
     * Proactive stub on this AO
     */
    private AOMatlabEnvironment stubOnThis;

    /**
     * Is the AO terminated ?
     */
    private boolean terminated;

    private boolean schedulerStopped = false;

    private SchedulerAuthenticationInterface auth;
    private ArrayList<String> previousJobs;

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

        this.scheduler.addSchedulerEventListener(stubOnThis, false, SchedulerEvent.JOB_RUNNING_TO_FINISHED,
                SchedulerEvent.KILLED, SchedulerEvent.SHUTDOWN, SchedulerEvent.SHUTTING_DOWN);

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

        // Open the file used for persistance
        String tmpPath = System.getProperty("java.io.tmpdir");
        jobLogFile = new File(tmpPath, "matlab_jobs.tmp");
        if (jobLogFile.exists()) {
            try {
                FileInputStream fis = new FileInputStream(jobLogFile);

                ObjectInputStream ois = new ObjectInputStream(fis);

                previousJobs = (ArrayList<String>) ois.readObject();

                if (previousJobs != null && previousJobs.size() > 0) {
                    System.out
                            .println("[AOMatlabEnvironment] " + previousJobs.size() +
                                " jobs were unfinished before shutdown of last Matlab session, trying te retrieve them.");
                }

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            } catch (ClassNotFoundException e2) {
                e2.printStackTrace();
            } finally {
                if (previousJobs == null) {
                    previousJobs = new ArrayList<String>();
                }
            }
        } else {
            try {
                jobLogFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (previousJobs == null) {
                    previousJobs = new ArrayList<String>();
                }
            }
        }

        writeIdListToLog(previousJobs);

    }

    private void writeIdListToLog(Collection<String> idList) {
        ArrayList<String> sorted = new ArrayList<String>(idList);
        Collections.sort(sorted);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(jobLogFile);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(sorted);
            oos.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
     * Try to get the results of jobs submitted before the Matlab client was disconnected
     *
     * @return
     */
    public ArrayList<ArrayList<Token>> getPreviousJobResults() {
        ArrayList<ArrayList<Token>> answer = new ArrayList<ArrayList<Token>>();

        for (String jid : previousJobs) {
            currentJobs.put(jid, new MatlabJobInfo(false));
            JobResult jResult = null;
            try {
                jResult = scheduler.getJobResult(jid);
                if (jResult != null) {
                    // If the result is already received
                    updateJobResult(jid, jResult);
                    answer.add(waitResultOfJob(jid));
                } else {
                    // If the answer is null, this means the job is known but not finished yet
                    // Consequently we delay the answer and put a future
                    lastSubJobId.add(jid);
                    answer.add(stubOnThis.waitAllResults());
                }
            } catch (SchedulerException e) {
                // The job is not known to the scheduler, we choose to ignore this case
            }

        }
        return answer;
    }

    /**
     * Returns all the results in an array or throw a RuntimeException in case of error
     *
     * @return array of ptolemy tokens
     */
    public ArrayList<Token> waitAllResults() {

        return waitResultOfJob(waitAllResultsJobID);
    }

    private ArrayList<Token> waitResultOfJob(String jid) {
        ArrayList<Token> answer = null;

        MatlabJobInfo jinfo = currentJobs.get(jid);
        if (jinfo.isDebugCurrentJob()) {
            System.out.println("[AOMatlabEnvironment] Sending the results of job " + jid + " back...");
        }
        try {
            if (schedulerStopped) {
                System.err.println("[AOMatlabEnvironment] The scheduler has been stopped");
                answer = new ArrayList<Token>();
            } else if (jinfo.getStatus() == JobStatus.KILLED || jinfo.getStatus() == JobStatus.CANCELED) {
                // Job killed
                System.err.println("[AOMatlabEnvironment] The job has been killed");
                answer = new ArrayList<Token>();
            } else if (jinfo.getErrorToThrow() != null) {
                // Error inside job
                if (jinfo.getErrorToThrow() instanceof MatlabTaskException) {
                    System.err.println(jinfo.getErrorToThrow().getMessage());
                    answer = new ArrayList<Token>();
                } else {

                    throw new RuntimeException(jinfo.getErrorToThrow());
                }
            } else {
                // Normal termination
                answer = new ArrayList<Token>(jinfo.getResults());
            }
        } finally {
            currentJobs.remove(jid);
            // updating persistance file
            writeIdListToLog(currentJobs.keySet());
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
    public ArrayList<Token> solve(String[] inputScripts, String[] mainScripts, URL scriptURL,
            String[] scriptParams, JobPriority priority, boolean debugJob) {

        if (schedulerStopped) {
            System.err.println("[AOMatlabEnvironment] the Scheduler is stopped");
            return new ArrayList<Token>();
        }
        // We store the script selecting the nodes to use it later at termination.

        if (debugJob) {
            System.out.println("[AOMatlabEnvironment] Submitting job of " + mainScripts.length + " tasks...");
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
        job.setName("Matlab Environment Job " + lastGenJobId++);
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
            if (debugJob) {
                schedulerTask.addArgument("debug", "true");
            }
            schedulerTask.setExecutableClassName("org.ow2.proactive.scheduler.ext.matlab.MatlabTask");
            if (availableScript != null) {
                SelectionScript sscript = null;
                try {
                    sscript = new SelectionScript(availableScript, scriptParams, true);
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
            String jid = scheduler.submit(job).value();
            lastSubJobId.add(jid);
            if (debugJob) {
                System.out.println("[AOMatlabEnvironment] Job " + jid + " submitted.");
            }
            currentJobs.put(jid, new MatlabJobInfo(debugJob));

            writeIdListToLog(currentJobs.keySet());

        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }

        // The last call puts a method in the RequestQueue 
        // that won't be executed until all the results are received (see runactivity)
        return stubOnThis.waitAllResults();
    }

    /**
     * terminate
     */
    public void terminate() {
        this.terminated = true;
    }

    /**
     * @see org.ow2.proactive.scheduler.common.SchedulerEventListener#schedulerStateUpdatedEvent(org.ow2.proactive.scheduler.common.SchedulerEvent)
     */
    public void schedulerStateUpdatedEvent(SchedulerEvent eventType) {
        switch (eventType) {
            case KILLED:
            case SHUTDOWN:
            case SHUTTING_DOWN:
            case STOPPED:
                if (debug) {
                    logger.debug("[AOMatlabEnvironment] Received " + eventType.toString() + " event");
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
                        System.out.println("[AOMatlabEnvironment] Received job killed event...");
                    }

                    // Filtering the right job
                    if (!currentJobs.containsKey(jid)) {
                        return;
                    }
                    currentJobs.get(jid).setStatus(info.getStatus());
                    currentJobs.get(jid).setJobFinished(true);
                } else {
                    if (debug) {
                        System.out.println("[AOMatlabEnvironment] Received job finished event...");
                    }

                    if (info == null) {
                        return;
                    }

                    // Filtering the right job
                    if (!currentJobs.containsKey(jid)) {
                        return;
                    }
                    JobResult jResult = null;
                    try {
                        jResult = scheduler.getJobResult(jid);
                    } catch (SchedulerException e) {
                        jobDidNotSucceed(jid, e, true, null);
                        return;
                    }
                    updateJobResult(jid, jResult);
                }
                break;
        }

    }

    private void updateJobResult(String jid, JobResult jResult) {
        // Getting the Job result from the Scheduler
        MatlabJobInfo jinfo = currentJobs.get(jid);
        if (jinfo.isDebugCurrentJob()) {
            System.out.println("[AOMatlabEnvironment] Updating results of job: " + jResult.getName() + "(" +
                jid + ")");
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
        ArrayList<Token> results = new ArrayList<Token>();
        for (Integer key : keys) {
            TaskResult res = task_results.get("" + key);
            if (jinfo.isDebugCurrentJob()) {
                System.out.println("[AOMatlabEnvironment] Looking for result of task: " + key);
            }

            // No result received
            if (res == null) {
                jobDidNotSucceed(jid, new RuntimeException("Task id = " + key +
                    " was not returned by the scheduler"), false, null);

            } else {

                String logs = res.getOutput().getAllLogs(false);
                if (res.hadException()) {

                    //Exception took place inside the framework
                    if (res.getException() instanceof ptolemy.kernel.util.IllegalActionException) {
                        // We filter this specific exception which means that the "out" variable was not set by the function
                        // due to an error inside the script or a missing licence

                        jobDidNotSucceed(jid, new MatlabTaskException(logs), false, logs);
                    } else {
                        // For other types of exception we forward it as it is.
                        jobDidNotSucceed(jid, res.getException(), true, logs);
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
                        jobDidNotSucceed(jid, new MatlabTaskException(logs), false, logs);
                    } catch (Throwable e2) {
                        jobDidNotSucceed(jid, e2, true, logs);
                    }
                }
            }
        }

        jinfo.setResults(results);
        jinfo.setStatus(jResult.getJobInfo().getStatus());
        jinfo.setJobFinished(true);
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
     * Handles case of an unsuccessful job
     *
     * @param jobId      id of the job
     * @param ex         exception thrown
     * @param printStack do we print the stack trace ?
     * @param logs       logs of the task creating the problem
     */
    private void jobDidNotSucceed(String jobId, Throwable ex, boolean printStack, String logs) {
        System.err.println("Job did not succeed");
        if (logs.length() > 0) {
            System.out.println(logs);
        }
        if (printStack) {
            ex.printStackTrace();
        }
        MatlabJobInfo jinfo = currentJobs.get(jobId);
        jinfo.setErrorToThrow(ex);
        jinfo.setJobFinished(true);
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
                Request randomRequest = service.getOldest();
                if (randomRequest.getMethodName().equals("waitAllResults")) {
                    // We detect a waitXXX request in the request queue
                    // if there is one request we remove it and store it for later
                    // we look at the last submitted job id
                    currentJobs.get(lastSubJobId.remove()).setPendingRequest(randomRequest);
                    if (debug) {
                        System.out.println("[AOMatlabEnvironment] Removed waitAllResults " + lastSubJobId +
                            " request from the queue");
                    }
                    service.blockingRemoveOldest("waitAllResults");
                } else {
                    service.serveOldest();
                }

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
        if (!currentJobs.isEmpty()) {
            HashMap<String, MatlabJobInfo> clonedJobIds = new HashMap<String, MatlabJobInfo>(currentJobs);
            for (Map.Entry<String, MatlabJobInfo> entry : clonedJobIds.entrySet()) {
                if (entry.getValue().isJobFinished() || schedulerStopped) {
                    if (debug) {
                        System.out.println("[AOMatlabEnvironment] serving waitAllResults " + entry.getKey());
                    }
                    servePending(service, entry.getKey(), entry.getValue());
                }
            }
        }
    }

    /**
     * Serve the pending waitXXX method
     *
     * @param service
     */
    protected void servePending(Service service, String jid, MatlabJobInfo jinfo) {
        Request req = jinfo.getPendingRequest();
        waitAllResultsJobID = jid;
        jinfo.setPendingRequest(null);
        service.serve(req);
    }

    /**
     * @author The ProActive Team
     *         Internal class for filtering requests in the queue
     */
    protected class FindNotWaitFilter implements RequestFilter {

        /**
         *
         */
        private static final long serialVersionUID = 10L;

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

}
