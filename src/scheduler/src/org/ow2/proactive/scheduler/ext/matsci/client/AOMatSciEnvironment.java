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
package org.ow2.proactive.scheduler.ext.matsci.client;

import org.apache.log4j.Logger;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.body.request.RequestFilter;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.ow2.proactive.authentication.crypto.CredData;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.scheduler.common.*;
import org.ow2.proactive.scheduler.common.exception.*;
import org.ow2.proactive.scheduler.common.job.*;
import org.ow2.proactive.scheduler.common.task.TaskInfo;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.util.SchedulerLoggers;

import javax.security.auth.login.LoginException;
import javax.swing.*;
import java.io.*;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.KeyException;
import java.util.*;


/**
 * AOMatSciEnvironment
 *
 * @author The ProActive Team
 */
public abstract class AOMatSciEnvironment<R, RL> implements Serializable, SchedulerEventListener, InitActive,
        RunActive {

    /**
	 * 
	 */
	private static final long serialVersionUID = 30L;

	/**
     * Connection to the scheduler
     */
    protected Scheduler scheduler;

    protected String waitAllResultsJobID;

    /**
     * Internal job id for job description only
     */
    protected long lastGenJobId = 0;

    /**
     * URL to the scheduler
     */
    private String schedulerUrl;

    /**
     * Id of the current jobs running
     */
    protected HashMap<String, MatSciJobVolatileInfo<R>> currentJobs = new HashMap<String, MatSciJobVolatileInfo<R>>();

    protected static Logger logger = ProActiveLogger.getLogger(SchedulerLoggers.MATSCI);
    protected static boolean debug = logger.isDebugEnabled();

    /**
     * Access to object output stream to save current jobs
     */
    protected File jobLogFile;

    protected static String host = null;

    static {
        if (host == null) {
            try {
                host = java.net.InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Proactive stub on this AO
     */
    protected AOMatSciEnvironment<R, RL> stubOnThis;

    /**
     * Is the AO terminated ?
     */
    protected boolean terminated;

    protected boolean schedulerStopped = false;

    protected boolean loggedin;

    protected boolean joined;

    protected SchedulerAuthenticationInterface auth;

    /**
     * Trys to log into the scheduler, using the provided user and password
     *
     * @param user   username
     * @param passwd password
     * @throws javax.security.auth.login.LoginException
     *          if the login fails
     * @throws org.ow2.proactive.scheduler.common.exception.SchedulerException
     *          if an other error occurs
     */
    public void login(String user, String passwd) throws LoginException, SchedulerException {

        //System.out.println("Trying to connect with "+user+" " +passwd);
        Credentials creds = null;
        try {
            creds = Credentials.createCredentials(new CredData(user, passwd), auth.getPublicKey());
        } catch (KeyException e) {
            throw new LoginException("" + e);
        } catch (LoginException e) {
            throw new LoginException("Could not retrieve public key, contact the Scheduler admininistrator" +
                e);
        }
        this.scheduler = auth.login(creds);

        loggedin = true;

        // myEventsOnly doesn't work with the disconnected mode, so we disable it
        this.scheduler.addEventListener(stubOnThis, false, SchedulerEvent.JOB_RUNNING_TO_FINISHED,
                SchedulerEvent.JOB_PENDING_TO_FINISHED, SchedulerEvent.KILLED, SchedulerEvent.SHUTDOWN,
                SchedulerEvent.SHUTTING_DOWN, SchedulerEvent.TASK_RUNNING_TO_FINISHED);

    }

    public void startLogin() {
        loggedin = false;
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                LoginFrame lf = new LoginFrame(stubOnThis, true);
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
        stubOnThis = (AOMatSciEnvironment) PAActiveObject.getStubOnThis();

    }

    /**
     * Request to join the scheduler at the given url
     *
     * @param url url of the scheduler
     * @return true if success, false otherwise
     */
    public boolean join(String url) throws SchedulerException {
        auth = SchedulerConnection.join(url);
        this.schedulerUrl = url;
        joined = true;
        return true;
    }

    public boolean isJoined() {
        return joined;
    }

    /**
     * Returns all the results in an array or throw a RuntimeException in case of error
     *
     * @return array of ptolemy tokens
     */

    protected void syncRetrieve(MatSciJobPermanentInfo jpinfo) {
        String jid = jpinfo.getJobId();
        MatSciJobVolatileInfo jinfo = new MatSciJobVolatileInfo(jpinfo);
        if (currentJobs.get(jid) != null) {
            throw new IllegalStateException("Wrong usage of retrieve");
        }
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

    protected void updateJobResult(String jid, JobResult jResult, JobStatus status) {
        // Getting the Job result from the Scheduler
        MatSciJobVolatileInfo jinfo = currentJobs.get(jid);
        if (jinfo.isDebugCurrentJob()) {
            System.out.println("[AOMatlabEnvironment] Updating results of job: " + jid + "(" + jid + ") : " +
                status);
        }
        jinfo.setStatus(status);
        jinfo.setJobFinished(true);

        Throwable mainException = null;
        if (schedulerStopped) {
            mainException = new IllegalStateException("The Scheduler has been killed.");
        } else if (jinfo.getStatus() == JobStatus.KILLED) {
            mainException = new IllegalStateException("The Job " + jid + " has been killed.");
        }

        if (schedulerStopped || jinfo.getStatus() == JobStatus.KILLED ||
            jinfo.getStatus() == JobStatus.CANCELED) {

            int depth = jinfo.getDepth();
            // Getting the task results from the job result
            TreeMap<String, TaskResult> task_results = null;

            if (jResult != null) {
                task_results = new TreeMap<String, TaskResult>(new TaskNameComparator());
                task_results.putAll(jResult.getAllResults());

                if (jinfo.isDebugCurrentJob()) {
                    System.out.println("[AOMatSciEnvironment] Updating job " + jResult.getName() + "(" + jid +
                        ") tasks ");
                }

                // Iterating over the task results
                for (String tname : task_results.keySet()) {
                    if (!jinfo.isReceivedResult(tname)) {
                        TaskResult res = task_results.get(tname);
                        if (jinfo.isDebugCurrentJob()) {
                            System.out.println("[AOMatSciEnvironment] Looking for result of task: " + tname);
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

    //abstract protected void handleResult(Throwable mainException, TaskResult res, String jid, int tid, int d);

    protected void updateTaskResult(Throwable mainException, TaskResult res, String jid, String tname) {
        MatSciJobVolatileInfo jinfo = currentJobs.get(jid);

        boolean intermediate = !jinfo.getFinalTasksNames().contains(tname);
        if (jinfo.isDebugCurrentJob()) {
            if (intermediate) {
                System.out.println("[AOMatSciEnvironment] Looking for result of intermediate task " + tname +
                    " for job " + jid);
            } else {
                System.out.println("[AOMatSciEnvironment] Looking for result of task " + tname + " for job " +
                    jid);
            }
        }
        String logs = null;
        if (res != null) {
            if (!jinfo.isTimeStamp()) {
                logs = res.getOutput().getAllLogs(false);
            } else {
                logs = res.getOutput().getAllLogs(true);
            }

            jinfo.addLogs(tname, logs);
        }
        Throwable ex = mainException;
        if (res == null && mainException == null) {
            ex = new RuntimeException("Task id = " + tname + " was not returned by the scheduler");
        } else if (res != null && res.hadException()) {
            ex = res.getException();
        }
        if (ex != null) {
            if (jinfo.isDebugCurrentJob()) {
                if (intermediate) {
                    System.out.println("[AOMatSciEnvironment] Intermediate task " + tname + " for job " +
                        jid + " threw an exception : " + ex.getClass() + " " + ex.getMessage());
                } else {
                    System.out.println("[AOMatSciEnvironment] Task " + tname + " for job " + jid +
                        " threw an exception : " + ex.getClass() + " " + ex.getMessage());
                }
            }
            jinfo.setException(tname, ex);

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

    protected boolean checkScript(URL script) throws Exception {
        // We verify that the scripts are available (otherwise we just ignore it)
        boolean answer = true;

        // The following code is commented because apparently File handles didn't close properly on windows

        //        InputStream is = null;
        //        try {
        //            is = script.openStream();
        //        } catch (Exception e) {
        //            answer = false;
        //        } finally {
        //            if (is != null) {
        //                try {
        //                    is.close();
        //                } catch (Exception e2) {
        //
        //                }
        //            }
        //        }

        return answer;
    }

    /**
     * User-side waiting for the results of one task
     * @param jid
     * @param tname
     * @return
     */
    protected abstract RL waitResultOfTask(String jid, String tname);

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
                    logger.debug("[AOMatSciEnvironment] Received " + eventType.toString() + " event");
                }
                schedulerStopped = true;
                for (String jid : currentJobs.keySet()) {
                    updateJobResult(jid, null, JobStatus.KILLED);
                }
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
                        System.out.println("[AOMatSciEnvironment] Received job " + jid + " killed event...");
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
                        System.out.println("[AOMatSciEnvironment] Received job " + jid + " killed event...");
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
                        System.out
                                .println("[AOMatSciEnvironment] Received job " + jid + " finished event...");
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
                    System.out.println("[AOMatSciEnvironment] Received task " + tnm + " of Job " + jid +
                        " finished event...");
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
                if (randomRequest.getMethodName().equals("waitResult")) {
                    //System.out.println("lastSubJobId.peek() => "+lastSubJobId.peek() );
                    String jid = (String) randomRequest.getParameter(0);
                    String tname = (String) randomRequest.getParameter(1);
                    MatSciJobVolatileInfo<R> jinfo = currentJobs.get(jid);
                    jinfo.setPendingRequest(tname, randomRequest);
                    if (debug) {
                        System.out.println("[AOMatSciEnvironment] Removed waitResult id=" + tname +
                            " for job=" + jid + " request from the queue");
                    }
                    service.blockingRemoveOldest("waitResult");
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
            Map<String, MatSciJobVolatileInfo<R>> clonedJobIds = new HashMap<String, MatSciJobVolatileInfo<R>>(
                currentJobs);
            for (Map.Entry<String, MatSciJobVolatileInfo<R>> entry : clonedJobIds.entrySet()) {
                MatSciJobVolatileInfo<R> jinfo = entry.getValue();
                String jid = entry.getKey();
                if (jinfo.isJobFinished() || schedulerStopped) {

                    servePendingWaitAll(service, jid, jinfo);
                }
                for (String tname : jinfo.getFinalTasksNames()) {
                    if (jinfo.isJobFinished() || schedulerStopped || jinfo.getResult(tname) != null ||
                        jinfo.getException(tname) != null) {

                        servePending(service, jid, jinfo, tname);
                    }
                }

            }
        }
    }

    /**
     * Serve the pending waitXXX method
     *
     * @param service
     */
    protected void servePendingWaitAll(Service service, String jid, MatSciJobVolatileInfo<R> jinfo) {
        Request req = jinfo.getPendingWaitAllRequest();
        if (req != null) {
            if (debug) {
                System.out.println("[AOMatSciEnvironment] serving waitAllResults for job " + jid);
            }
            waitAllResultsJobID = jid;
            jinfo.setPendingWaitAllRequest(null);
            service.serve(req);
        }
    }

    /**
     * Serve the pending waitXXX method
     *
     * @param service
     */
    protected void servePending(Service service, String jid, MatSciJobVolatileInfo<R> jinfo, String tname) {
        Request req = jinfo.getPendingRequest(tname);
        if (req != null) {
            if (debug) {
                System.out.println("[AOMatSciEnvironment] serving waitResult " + tname + " for job " + jid);
            }
            waitAllResultsJobID = jid;
            jinfo.setPendingRequest(tname, null);
            service.serve(req);
        }
    }

    /**
     * @author The ProActive Team
     *         Internal class for filtering requests in the queue
     */
    protected class FindNotWaitFilter implements RequestFilter {

        /**
		 * 
		 */
		private static final long serialVersionUID = 30L;

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
            } else if (name.equals("waitResult")) {
                return false;
            }
            return true;
        }
    }

    protected class IntStrComparator implements Comparator<String>, Serializable {

        /**
		 * 
		 */
		private static final long serialVersionUID = 30L;

		public IntStrComparator() {

        }

        public int compare(String o1, String o2) {
            if (o1 == null && o2 == null)
                return 0;
            // assuming you want null values shown last
            if (o1 != null && o2 == null)
                return -1;
            if (o1 == null && o2 != null)
                return 1;
            int answer = 0;
            try {
                answer = Integer.parseInt(o1) - Integer.parseInt(o2);
            } catch (NumberFormatException e) {
                answer = o1.compareTo(o2);
            }
            return answer;
        }
    }

    public static class TaskNameComparator implements Comparator<String>, Serializable {

        /**
		 * 
		 */
		private static final long serialVersionUID = 30L;

		public TaskNameComparator() {

        }

        public int compare(String o1, String o2) {
            if (o1 == null && o2 == null)
                return 0;
            // assuming you want null values shown last
            if (o1 != null && o2 == null)
                return -1;
            if (o1 == null && o2 != null)
                return 1;
            int answer = 0;
            try {
                String[] arr1 = o1.split("_");
                String[] arr2 = o2.split("_");
                answer = Integer.parseInt(arr1[1]) - Integer.parseInt(arr2[1]);
                if (answer == 0) {
                    answer = Integer.parseInt(arr1[0]) - Integer.parseInt(arr2[0]);
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
            return answer;
        }
    }

}
