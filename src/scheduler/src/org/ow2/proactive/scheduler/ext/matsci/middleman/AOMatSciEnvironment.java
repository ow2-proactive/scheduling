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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.ext.matsci.middleman;

import org.objectweb.proactive.*;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.body.request.BlockingRequestQueue;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.body.request.RequestFilter;
import org.ow2.proactive.authentication.crypto.CredData;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.scheduler.common.*;
import org.ow2.proactive.scheduler.common.exception.*;
import org.ow2.proactive.scheduler.common.job.*;
import org.ow2.proactive.scheduler.common.job.JobStatus;
import org.ow2.proactive.scheduler.common.task.TaskInfo;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.ext.matsci.client.common.MatSciEnvironment;
import org.ow2.proactive.scheduler.ext.matsci.client.common.data.*;
import org.ow2.proactive.scheduler.ext.matsci.client.common.exception.PASchedulerException;
import org.ow2.proactive.scheduler.util.console.SchedulerModel;
import org.ow2.proactive.utils.console.StdOutConsole;

import javax.security.auth.login.LoginException;
import java.io.*;
import java.security.KeyException;
import java.util.*;
import java.util.concurrent.TimeoutException;


/**
 * AOMatSciEnvironment
 *
 * @author The ProActive Team
 */
public abstract class AOMatSciEnvironment<R, RL> implements MatSciEnvironment, Serializable,
        SchedulerEventListener, InitActive, RunActive, EndActive {

    /**
     * Connection to the scheduler
     */
    protected Scheduler scheduler;

    /**
     * Connection to the scheduler console
     */
    protected SchedulerModel model;

    /**
     * Internal job id for job description only
     */
    protected long lastGenJobId = 0;

    /**
     * Id of this active object
     */
    protected String aoid;

    /**
     * Id of the current jobs running
     */
    protected HashMap<String, MatSciJobVolatileInfo<R>> currentJobs = new HashMap<String, MatSciJobVolatileInfo<R>>();

    /**
     * Ids of finished jobs
     */
    protected HashMap<String, MatSciJobVolatileInfo<R>> finishedJobs = new HashMap<String, MatSciJobVolatileInfo<R>>();

    /**
     * Map storing the pending request for a given job
     */
    protected HashMap<String, Request> pendingRequests = new HashMap<String, Request>();

    /**
     * Index used when executing PAwaitAny (index of the result received)
     */
    protected int lastPAWaitAnyIndex = -1;

    /**
     * timeout given when calling waitAny or waitAll
     */
    protected HashMap<String, Integer> timeouts = new HashMap<String, Integer>();

    /**
     * current time when the waitAny or waitAll requests are received
     */
    protected HashMap<String, Long> beginTimes = new HashMap<String, Long>();

    /**
     * If a timeout occured before serving waitAny or waitAll
     */
    protected HashMap<String, Boolean> timeoutOccured = new HashMap<String, Boolean>();

    /**
     * Debug mode
     */
    protected boolean debug;

    /**
     * Proactive stub on this AO
     */
    protected AOMatSciEnvironment<R, RL> stubOnThis;

    /**
     * Is the AO terminated ?
     */
    protected boolean terminated;

    /**
     * The scheduler has been stopped
     */
    protected boolean schedulerStopped = false;

    /**
     * The scheduler has been killed
     */
    protected boolean schedulerKilled = false;

    /**
     * Is the current session logged into the scheduler
     */
    protected boolean loggedin;

    /**
     * Has the current session successfully joined a scheduler
     */
    protected boolean joined;

    /**
     * joined interface to the scheduler, before authentication
     */
    protected SchedulerAuthenticationInterface auth;

    /**********************************************************************************************************
     ************************************** LOGIN AND CONNECTION *********************************************/

    public AOMatSciEnvironment() {

    }

    public AOMatSciEnvironment(boolean debug) {
        this.debug = debug;
    }

    /** {@inheritDoc} */
    public void login(String user, String passwd) throws PASchedulerException {

        if (scheduler != null) {
            try {
                scheduler.disconnect();
            } catch (Exception e) {

            }
            scheduler = null;
        }

        //System.out.println("Trying to connect with "+user+" " +passwd);
        Credentials creds = null;
        try {
            creds = Credentials.createCredentials(new CredData(CredData.parseLogin(user), CredData
                    .parseDomain(user), passwd), auth.getPublicKey());
        } catch (KeyException e) {
            throw new PASchedulerException(e, PASchedulerException.ExceptionType.KeyException);
        } catch (LoginException e) {
            throw new PASchedulerException(new LoginException(
                "Could not retrieve public key, contact the Scheduler admininistrator\n" + e),
                PASchedulerException.ExceptionType.LoginException);
        }
        initLogin(creds);

    }

    protected void initLogin(Credentials creds) throws PASchedulerException {
        SchedulerStatus status = null;
        try {
            this.scheduler = auth.login(creds);

            loggedin = true;

            // myEventsOnly doesn't work with the disconnected mode, so we disable it

            this.scheduler.addEventListener(stubOnThis, true, SchedulerEvent.JOB_RUNNING_TO_FINISHED,
                    SchedulerEvent.JOB_PENDING_TO_FINISHED, SchedulerEvent.KILLED, SchedulerEvent.SHUTDOWN,
                    SchedulerEvent.SHUTTING_DOWN, SchedulerEvent.STOPPED, SchedulerEvent.RESUMED,
                    SchedulerEvent.TASK_RUNNING_TO_FINISHED);

            status = scheduler.getStatus();
        } catch (NotConnectedException e) {
            throw new PASchedulerException(e, PASchedulerException.ExceptionType.NotConnectedException);
        } catch (PermissionException e) {
            throw new PASchedulerException(e, PASchedulerException.ExceptionType.PermissionException);
        } catch (AlreadyConnectedException e) {
            throw new PASchedulerException(e, PASchedulerException.ExceptionType.AlreadyConnectedException);
        } catch (LoginException e) {
            throw new PASchedulerException(e, PASchedulerException.ExceptionType.LoginException);
        }
        schedulerStopped = (status == SchedulerStatus.STOPPED);
        schedulerKilled = (status == SchedulerStatus.KILLED);

        this.model = SchedulerModel.getModel(false);
        model.connectConsole(new StdOutConsole());
        model.connectScheduler(this.scheduler);

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

            public void run() {
                try {
                    scheduler.disconnect();
                } catch (NotConnectedException e) {
                } catch (PermissionException e) {
                }
            }
        }));

    }

    /** {@inheritDoc} */
    public void login(String credPath) throws PASchedulerException {

        if (scheduler != null) {
            try {
                scheduler.disconnect();
            } catch (Exception e) {

            }
            scheduler = null;
        }

        //System.out.println("Trying to connect with "+user+" " +passwd);
        Credentials creds = null;
        try {
            creds = Credentials.getCredentials(credPath);
        } catch (KeyException e) {
            throw new PASchedulerException(e, PASchedulerException.ExceptionType.KeyException);
        }
        initLogin(creds);

    }

    /** {@inheritDoc} */
    public boolean isLoggedIn() {

        return loggedin && isConnected();
    }

    /** {@inheritDoc} */
    public boolean disconnect() {
        try {
            this.scheduler.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.scheduler = null;
        this.model = null;
        this.auth = null;
        loggedin = false;
        joined = false;
        schedulerKilled = false;
        schedulerStopped = false;
        return true;
    }

    /** {@inheritDoc} */
    public boolean isConnected() {
        try {
            return this.scheduler.isConnected();
        } catch (Exception e) {
            return false;
        }
    }

    /** {@inheritDoc} */
    public void ensureConnection() throws PASchedulerException {
        if (!isLoggedIn()) {
            try {
                this.scheduler.renewSession();
            } catch (Exception e) {
                throw new PASchedulerException(e);
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.objectweb.proactive.InitActive#initActivity(org.objectweb.proactive.Body)
     */
    @SuppressWarnings("unchecked")
    public void initActivity(Body body) {
        stubOnThis = (AOMatSciEnvironment) PAActiveObject.getStubOnThis();
        aoid = PAActiveObject.getBodyOnThis().getID().getVMID().toString();
    }

    public void endActivity(org.objectweb.proactive.Body body) {
        try {
            scheduler.disconnect();
        } catch (NotConnectedException e) {
            e.printStackTrace();
        } catch (PermissionException e) {
            e.printStackTrace();
        }
    }

    /** {@inheritDoc} */
    public boolean join(String url) throws PASchedulerException {
        try {
            auth = SchedulerConnection.join(url);
        } catch (ConnectionException e) {
            throw new PASchedulerException(e);
        }
        this.loggedin = false;
        joined = true;
        return true;
    }

    /** {@inheritDoc} */
    public boolean isJoined() {
        return joined;
    }

    /** {@inheritDoc} */
    public boolean terminate() {
        this.terminated = true;
        return true;
    }

    protected void printLog(final String message) {
        printLog(message, false);
    }

    protected void printLog(final String message, boolean force) {
        if (!debug && !force) {
            return;
        }
        MatSciJVMProcessInterfaceImpl.printLog(this, message);
    }

    protected void printLog(final Throwable ex) {
        printLog(ex, false);
    }

    protected void printLog(final Throwable ex, boolean force) {
        if (!debug && !force) {
            return;
        }
        MatSciJVMProcessInterfaceImpl.printLog(this, ex);
    }

    /**********************************************************************************************************
     ************************************* JOB STATES AND COMMANDS *******************************************/

    private ByteArrayOutputStream redirectStreams() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        System.setOut(ps);
        System.setErr(ps);
        return baos;
    }

    private void resetStreams() {
        System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
        System.setErr(new PrintStream(new FileOutputStream(FileDescriptor.err)));
    }

    /** {@inheritDoc} */
    public String schedulerState() throws PASchedulerException {
        ensureConnection();
        ByteArrayOutputStream baos = redirectStreams();
        model.schedulerState_();
        resetStreams();
        return baos.toString();
    }

    /** {@inheritDoc} */
    public String jobState(String jid) throws PASchedulerException {
        ensureConnection();
        ByteArrayOutputStream baos = redirectStreams();
        model.jobState_(jid);
        resetStreams();
        return baos.toString();
    }

    /** {@inheritDoc} */
    public String jobOutput(String jid) throws PASchedulerException {
        ensureConnection();
        ByteArrayOutputStream baos = redirectStreams();
        model.output_(jid);
        resetStreams();
        return baos.toString();
    }

    /** {@inheritDoc} */
    public String jobResult(String jid) throws PASchedulerException {
        ensureConnection();
        ByteArrayOutputStream baos = redirectStreams();
        model.result_(jid);
        resetStreams();
        return baos.toString();
    }

    /** {@inheritDoc} */
    public String jobRemove(String jid) throws PASchedulerException {
        ensureConnection();
        ByteArrayOutputStream baos = redirectStreams();
        model.remove_(jid);
        resetStreams();
        return baos.toString();
    }

    /** {@inheritDoc} */
    public String pauseJob(String jid) throws PASchedulerException {
        ensureConnection();
        ByteArrayOutputStream baos = redirectStreams();
        model.pause_(jid);
        resetStreams();
        return baos.toString();
    }

    /** {@inheritDoc} */
    public String resumeJob(String jid) throws PASchedulerException {
        ensureConnection();
        ByteArrayOutputStream baos = redirectStreams();
        model.resume_(jid);
        resetStreams();
        return baos.toString();
    }

    /** {@inheritDoc} */
    public String killJob(String jid) throws PASchedulerException {
        ensureConnection();
        ByteArrayOutputStream baos = redirectStreams();
        model.kill_(jid);
        resetStreams();
        return baos.toString();
    }

    /** {@inheritDoc} */
    public String taskOutput(String jid, String tname) throws PASchedulerException {
        ensureConnection();
        ByteArrayOutputStream baos = redirectStreams();
        model.toutput_(jid, tname);
        resetStreams();
        return baos.toString();
    }

    /** {@inheritDoc} */
    public String taskResult(String jid, String tname) throws PASchedulerException {
        ensureConnection();
        ByteArrayOutputStream baos = redirectStreams();
        model.tresult_(jid, tname, "0");
        resetStreams();
        return baos.toString();
    }

    /** {@inheritDoc} */
    public String killTask(String jid, String tname) throws PASchedulerException {
        ensureConnection();
        ByteArrayOutputStream baos = redirectStreams();
        model.killt_(jid, tname);
        resetStreams();
        return baos.toString();
    }

    /**********************************************************************************************************/
    /*********************************************** TASKS  **************************************************/

    /**
     * Updates synchronously the list of results from the given job
     */
    protected void syncRetrieve(MatSciJobPermanentInfo jpinfo) throws PASchedulerException {
        ensureConnection();
        String jid = jpinfo.getJobId();
        MatSciJobVolatileInfo jinfo = new MatSciJobVolatileInfo(jpinfo);
        currentJobs.put(jid, jinfo);
        JobResult jResult = null;
        try {
            jResult = scheduler.getJobResult(jid);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
        if (jResult != null) {
            // full update if the job is finished
            updateJobResult(jid, jResult, jResult.getJobInfo().getStatus());
        }
        // partial update otherwise
        for (String tname : jpinfo.getTaskNames()) {
            TaskResult tres = null;
            try {
                tres = scheduler.getTaskResult(jid, tname);
            } catch (NotConnectedException e) {
                e.printStackTrace();
            } catch (UnknownJobException e) {
                e.printStackTrace();
            } catch (UnknownTaskException e) {
                // ignore this exception
            } catch (PermissionException e) {
                e.printStackTrace();
            }
            if (tres != null) {
                updateTaskResult(null, tres, jid, tname);
            }
        }

    }

    /** {@inheritDoc} */
    public boolean retrieve(MatSciJobPermanentInfo jpinfo) {

        syncRetrieve(jpinfo);
        return true;
    }

    /**
     * updates the result of the given job, using the information received from the scheduler
     *
     * @param jid
     * @param jResult
     * @param status
     */
    protected void updateJobResult(String jid, JobResult jResult, JobStatus status) {
        // Getting the Job result from the Scheduler
        MatSciJobVolatileInfo jinfo = currentJobs.get(jid);
        if (debug) {
            printLog("Updating results of job: " + jid + "(" + jid + ") : " + status);
        }
        jinfo.setStatus(org.ow2.proactive.scheduler.ext.matsci.client.common.data.JobStatus
                .getJobStatus(status.toString()));
        jinfo.setJobFinished(true);

        Throwable mainException = null;
        if (schedulerStopped || schedulerKilled) {
            mainException = new IllegalStateException("The Scheduler has been killed.");
        } else if (status == JobStatus.KILLED) {
            mainException = new IllegalStateException("The Job " + jid + " has been killed.");
        }

        if (schedulerStopped || schedulerKilled || status == JobStatus.KILLED || status == JobStatus.CANCELED) {

            int depth = jinfo.getDepth();
            // Getting the task results from the job result
            TreeMap<String, TaskResult> task_results = null;

            if (jResult != null) {
                task_results = new TreeMap<String, TaskResult>(new TaskNameComparator());
                task_results.putAll(jResult.getAllResults());

                if (debug) {
                    printLog("Updating job " + jResult.getName() + "(" + jid + ") tasks ");
                }

                // Iterating over the task results
                for (String tname : task_results.keySet()) {
                    if (!jinfo.isReceivedResult(tname)) {
                        TaskResult res = task_results.get(tname);
                        if (debug) {
                            printLog("Looking for result of task: " + tname);
                        }

                        updateTaskResult(mainException, res, jid, tname);
                    }

                }
            }
            TreeSet<String> missinglist = jinfo.missingResults();
            for (String missing : missinglist) {
                updateTaskResult(null, null, jid, missing);
            }
        }
    }

    /**
     * Updates the result of a task, using the information retrived from the scheduler
     *
     * @param mainException if an exception occurred globally, such as a job killed
     * @param res           result object received from the scheduler
     * @param jid           job id
     * @param tname         name of the task
     */
    protected void updateTaskResult(Throwable mainException, TaskResult res, String jid, String tname) {
        MatSciJobVolatileInfo jinfo = currentJobs.get(jid);

        boolean intermediate = !jinfo.getFinalTasksNames().contains(tname);
        if (debug) {
            if (intermediate) {
                printLog("Looking for result of intermediate task " + tname + " for job " + jid);
            } else {
                printLog("Looking for result of task " + tname + " for job " + jid);
            }
        }
        String logs = null;
        if (res != null) {

            logs = res.getOutput().getAllLogs(false);

            jinfo.addLogs(tname, logs);
        }
        Throwable ex = mainException;
        if (res == null && mainException == null) {
            ex = new RuntimeException("Task id = " + tname + " was not returned by the scheduler");
        } else if (res != null && res.hadException()) {
            ex = res.getException();
        }
        if (ex != null) {
            if (debug) {
                if (intermediate) {
                    printLog("Intermediate task " + tname + " for job " + jid + " threw an exception : " +
                        ex.getClass() + " " + ex.getMessage());
                } else {
                    printLog("Task " + tname + " for job " + jid + " threw an exception : " + ex.getClass() +
                        " " + ex.getMessage());
                }
            }
            jinfo.setException(tname, new PASchedulerException(ex));

        } else {
            if (!intermediate) {
                // Normal success

                R computedResult = null;
                try {
                    computedResult = (R) res.value();
                    jinfo.setResult(tname, computedResult);
                    //results.add(computedResult);
                    // We print the logs of the job, if any
                    //if (logs.length() > 0) {
                    //   System.out.println(logs);
                    //}
                } catch (Throwable e2) {
                    // should never occur
                    jinfo.setException(tname, e2);
                    //jobDidNotSucceed(jid, e2, true, logs);
                }
            }
        }
        jinfo.addReceivedTask(tname);

    }

    /**
     * gets the result of the given task (the result is already received)
     * Converts exceptions if any occurred
     *
     * @param jid id of the job
     * @param tname name of the task
     * @return an object containing the result, exception and logs
     */
    protected abstract ResultsAndLogs getResultOfTask(String jid, String tname);

    /** {@inheritDoc} */
    public UnReifiable<Pair<ResultsAndLogs, Integer>> waitAny(String jid, ArrayList<String> tnames,
            Integer timeout) throws Exception {
        try {
            Boolean tout = timeoutOccured.get(jid);
            if (tout != null && tout) {
                timeoutOccured.put(jid, false);
                throw new TimeoutException("Timeout occured while executing PAwaitAny for job " + jid);
            }
            return new UnReifiable<Pair<ResultsAndLogs, Integer>>(new Pair<ResultsAndLogs, Integer>(
                getResultOfTask(jid, tnames.get(lastPAWaitAnyIndex)), lastPAWaitAnyIndex));
        } catch (Exception e) {
            printLog(e);
            throw e;
        }
    }

    /** {@inheritDoc} */
    public UnReifiable<ArrayList<ResultsAndLogs>> waitAll(String jid, ArrayList<String> tnames,
            Integer timeout) throws Exception {
        try {
            Boolean tout = timeoutOccured.get(jid);
            if (tout != null && tout) {
                timeoutOccured.put(jid, false);
                throw new TimeoutException("Timeout occured while executing PAwaitAll for job " + jid);
            }
            ArrayList<ResultsAndLogs> answers = new ArrayList<ResultsAndLogs>();
            for (String tname : tnames) {
                answers.add(getResultOfTask(jid, tname));
            }
            return new UnReifiable<ArrayList<ResultsAndLogs>>(answers);
        } catch (Exception e) {
            printLog(e);
            throw e;
        }
    }

    /** {@inheritDoc} */
    public UnReifiable<ArrayList<Boolean>> areAwaited(String jid, ArrayList<String> tnames) {
        try {
            if (currentJobs.containsKey(jid)) {
                return new UnReifiable<ArrayList<Boolean>>(currentJobs.get(jid).areAwaited(tnames));
            } else if (finishedJobs.containsKey(jid)) {
                return new UnReifiable<ArrayList<Boolean>>(finishedJobs.get(jid).areAwaited(tnames));
            } else {
                throw new IllegalArgumentException("Unknown job " + jid);
            }
        } catch (RuntimeException e) {
            printLog(e);
            throw e;
        }
    }

    /**********************************************************************************************************/
    /*********************************************** EVENTS  **************************************************/

    /**
     * @see org.ow2.proactive.scheduler.common.SchedulerEventListener#schedulerStateUpdatedEvent(org.ow2.proactive.scheduler.common.SchedulerEvent)
     */
    public void schedulerStateUpdatedEvent(SchedulerEvent eventType) {
        switch (eventType) {
            case KILLED:
            case SHUTDOWN:
            case SHUTTING_DOWN:
                if (debug) {
                    printLog("Received " + eventType.toString() + " event");
                }
                schedulerKilled = true;
                for (String jid : currentJobs.keySet()) {
                    updateJobResult(jid, null, JobStatus.KILLED);
                }
                break;
            case STOPPED:
                if (debug) {
                    printLog("Received " + eventType.toString() + " event");
                }
                for (String jid : currentJobs.keySet()) {
                    updateJobResult(jid, null, JobStatus.KILLED);
                }
                schedulerStopped = true;
                break;
            case RESUMED:
                if (debug) {
                    printLog("Received " + eventType.toString() + " event");
                }
                schedulerStopped = false;
                break;
        }

    }

    /**
     * @see org.ow2.proactive.scheduler.common.SchedulerEventListener#jobStateUpdatedEvent(org.ow2.proactive.scheduler.common.NotificationData)
     */
    public void jobStateUpdatedEvent(NotificationData<JobInfo> notification) {
        JobInfo info = notification.getData();
        String jid = info.getJobId().value();
        switch (notification.getEventType()) {

            case JOB_PENDING_TO_FINISHED:
                if (info.getStatus() == JobStatus.KILLED) {
                    if (debug) {
                        printLog("Received job " + jid + " killed event...");
                    }

                    // Filtering the right job
                    if (!currentJobs.containsKey(jid)) {
                        return;
                    }

                    updateJobResult(jid, null, JobStatus.KILLED);
                }

            case JOB_RUNNING_TO_FINISHED:

                if (info.getStatus() == JobStatus.KILLED) {
                    if (debug) {
                        printLog("Received job " + jid + " killed event...");
                    }

                    // Filtering the right job
                    if (!currentJobs.containsKey(jid)) {
                        return;
                    }

                    JobResult jResult = null;
                    try {
                        jResult = scheduler.getJobResult(jid);
                    } catch (SchedulerException e) {
                        e.printStackTrace();
                    }
                    updateJobResult(jid, jResult, JobStatus.KILLED);
                } else {
                    if (debug) {
                        printLog("Received job " + jid + " finished event...");
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
                        e.printStackTrace();
                    }
                    updateJobResult(jid, jResult, info.getStatus());
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
        switch (notification.getEventType()) {
            case TASK_RUNNING_TO_FINISHED:
                TaskInfo info = notification.getData();
                String tnm = info.getName();
                String jid = info.getJobId().value();
                if (debug) {
                    printLog("Received task " + tnm + " of Job " + jid + " finished event...");
                }
                // Filtering the right job
                if (!currentJobs.containsKey(jid)) {
                    return;
                }
                TaskResult tres = null;

                try {
                    tres = scheduler.getTaskResult(jid, tnm);
                } catch (NotConnectedException e) {
                    e.printStackTrace();
                } catch (UnknownJobException e) {
                    e.printStackTrace();
                } catch (UnknownTaskException e) {
                    e.printStackTrace();
                } catch (PermissionException e) {
                    e.printStackTrace();
                }
                updateTaskResult(null, tres, jid, tnm);
        }
    }

    /**
     * @see org.ow2.proactive.scheduler.common.SchedulerEventListener#usersUpdatedEvent(org.ow2.proactive.scheduler.common.NotificationData)
     */
    public void usersUpdatedEvent(NotificationData<UserIdentification> notification) {
        // TODO Auto-generated method stub
    }

    /**********************************************************************************************************/
    /*********************************************** ACTIVITY  ************************************************/

    /**
     * {@inheritDoc}
     */
    public void runActivity(final Body body) {
        Service service = new Service(body);

        while (!terminated) {
            try {
                long in = System.currentTimeMillis();
                int tout = computeTimeoutServing();

                if (tout == Integer.MAX_VALUE) {
                    if (debug) {
                        printLog("waiting for request with no timeout");

                    }
                    service.waitForRequest();
                } else {
                    if (debug) {
                        printLog("waiting for request with timeout = " + tout);

                    }
                    BlockingRequestQueue queue = body.getRequestQueue();
                    queue.waitForRequest(tout);
                }
                int time = (int) (System.currentTimeMillis() - in);
                substractTimeout(time);

                if (service.hasRequestToServe()) {

                    Request randomRequest = service.getOldest();
                    if (debug) {
                        printLog("Request received : " + randomRequest.getMethodName());
                    }
                    if (randomRequest.getMethodName().equals("waitAll")) {
                        String jid = (String) randomRequest.getParameter(0);
                        tout = (Integer) randomRequest.getParameter(2);
                        if (tout > 0) {
                            timeouts.put(jid, tout);
                            beginTimes.put(jid, System.currentTimeMillis());
                        } else {
                            timeouts.put(jid, null);
                        }

                        pendingRequests.put(jid, randomRequest);
                        if (debug) {
                            printLog("Removed " + randomRequest.getMethodName() + " for job=" + jid +
                                " request from the queue");
                        }
                        service.blockingRemoveOldest("waitAll");
                    } else if (randomRequest.getMethodName().equals("waitAny")) {
                        String jid = (String) randomRequest.getParameter(0);
                        tout = (Integer) randomRequest.getParameter(2);
                        if (tout > 0) {
                            timeouts.put(jid, tout);
                            beginTimes.put(jid, System.currentTimeMillis());
                        } else {
                            timeouts.put(jid, null);
                        }
                        pendingRequests.put(jid, randomRequest);
                        if (debug) {
                            printLog("Removed " + randomRequest.getMethodName() + " for job=" + jid +
                                " request from the queue");
                        }
                        service.blockingRemoveOldest("waitAny");
                    }
                    if (service.hasRequestToServe()) {
                        service.serveOldest();
                    }
                }

                // we maybe serve the pending waitXXX method if there is one and if the necessary results are collected
                maybeServePending(service);
            } catch (Throwable ex) {
                printLog(ex);
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
     * computes the timeout required by active timed-out requests
     * @return
     */
    private int computeTimeoutServing() {
        int timeout = Integer.MAX_VALUE;
        for (String jid : timeouts.keySet()) {
            Integer tout = timeouts.get(jid);
            if ((tout != null) && (tout < timeout)) {
                timeout = tout;
            }
        }
        return timeout;
    }

    /**
     * substract a waited time to stored requests timeouts
     * @param time time waited
     */
    private void substractTimeout(int time) {
        for (String jid : timeouts.keySet()) {
            Integer tout = timeouts.get(jid);
            if (tout != null) {
                timeouts.put(jid, tout - time);
                beginTimes.put(jid, beginTimes.get(jid) + time);
            }
        }
    }

    /**
     * If there is a pending waitXXX method, we serve it if the necessary results are collected
     *
     * @param service
     */
    protected void maybeServePending(Service service) {
        if (!currentJobs.isEmpty()) {
            Map<String, MatSciJobVolatileInfo<R>> clonedJobIds = new HashMap<String, MatSciJobVolatileInfo<R>>(
                finishedJobs);
            // for the rare case that the scheduler is restarted with a clean database, we must make sure that the current jobs are not overwritten by old finished jobs ids, so we construct the hashmap this way
            clonedJobIds.putAll(currentJobs);
            for (Map.Entry<String, MatSciJobVolatileInfo<R>> entry : clonedJobIds.entrySet()) {
                MatSciJobVolatileInfo<R> jinfo = entry.getValue();
                String jid = entry.getKey();
                Request req = pendingRequests.get(jid);
                if (req != null) {
                    if (req.getMethodName().equals("waitAll")) {
                        ArrayList<String> tnames = (ArrayList<String>) req.getParameter(1);
                        Integer tout = timeouts.get(jid);
                        boolean to = (tout != null) &&
                            (System.currentTimeMillis() - beginTimes.get(jid) >= tout);
                        if (schedulerStopped || schedulerKilled || to || jinfo.areAllReceived(tnames)) {
                            beginTimes.put(jid, null);
                            timeouts.put(jid, null);
                            timeoutOccured.put(jid, to);
                            pendingRequests.put(jid, null);
                            if (debug) {
                                printLog("serving " + req.getMethodName() + " for job " + jid);
                            }
                            service.serve(req);
                        }
                    } else if (req.getMethodName().equals("waitAny")) {
                        ArrayList<String> tnames = (ArrayList<String>) req.getParameter(1);
                        Integer tout = timeouts.get(jid);
                        boolean to = (tout != null) &&
                            (System.currentTimeMillis() - beginTimes.get(jid) >= tout);
                        int any = jinfo.isAnyReceivedResult(tnames);
                        if (schedulerStopped || schedulerKilled || to || any >= 0) {
                            beginTimes.put(jid, null);
                            timeouts.put(jid, null);
                            timeoutOccured.put(jid, to);
                            pendingRequests.put(jid, null);
                            if (debug) {
                                printLog("serving " + req.getMethodName() + " for job " + jid);
                            }
                            lastPAWaitAnyIndex = any;
                            service.serve(req);
                        }
                    }
                }
            }
        }
    }

    /**
     * @author The ProActive Team
     *         Internal class for filtering requests in the queue
     */
    private class FindNotWaitFilter implements RequestFilter {

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
            if (name.equals("waitAny")) {
                return false;
            } else if (name.equals("waitAll")) {
                return false;
            }
            return true;
        }
    }

}
