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
     * Connection to the scheduler
     */
    protected Scheduler scheduler;

    /**
     * job id of the last submitted job (useful for runactivity method)
     */
    protected Queue<String> lastSubJobId = new LinkedList<String>();

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

    protected SchedulerAuthenticationInterface auth;
    protected TreeMap<String, Integer[]> previousJobs;

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

        // myEventsOnly doesn't work with the deconnected mode, so we disable it
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

        //        // Open the file used for persistance
        //        String tmpPath = System.getProperty("java.io.tmpdir");
        //        jobLogFile = new File(tmpPath, "matsci_jobs.tmp");
        //        if (jobLogFile.exists()) {
        //            try {
        //                FileInputStream fis = new FileInputStream(jobLogFile);
        //
        //                ObjectInputStream ois = new ObjectInputStream(fis);
        //
        //                previousJobs = (TreeMap<String, Integer[]>) ois.readObject();
        //
        //                if (previousJobs != null && previousJobs.size() > 0) {
        //                    System.out
        //                            .println("[AOMatSciEnvironment] " + previousJobs.size() +
        //                                " jobs were unfinished before shutdown of last Matlab session, trying te retrieve them.");
        //                }
        //
        //            } catch (FileNotFoundException e) {
        //                e.printStackTrace();
        //            } catch (IOException e1) {
        //                e1.printStackTrace();
        //            } catch (ClassNotFoundException e2) {
        //                System.out
        //                        .println("[AOMatSciEnvironment] Warning : Previous jobs contained invalid classes definitions:");
        //                System.out.println(e2.getMessage());
        //            }
        //        } else {
        //            try {
        //                jobLogFile.createNewFile();
        //            } catch (IOException e) {
        //                e.printStackTrace();
        //            }
        //        }
        //
        //        if (previousJobs == null) {
        //            previousJobs = new TreeMap<String, Integer[]>(new IntStrComparator());
        //        }
        //
        //        writeIdListToLog(previousJobs);

    }

    protected void writeCjobsToLog(HashMap<String, MatSciJobVolatileInfo<R>> cjobs) {
        //        TreeMap<String, Integer[]> idList = new TreeMap<String, Integer[]>(new IntStrComparator());
        //
        //        for (String key : cjobs.keySet()) {
        //            idList.put(key, new Integer[] { cjobs.get(key).nbResults(), cjobs.get(key).getDepth() });
        //        }
        //        writeIdListToLog(idList);
    }

    protected void writeIdListToLog(TreeMap<String, Integer[]> idList) {
        //        FileOutputStream fos = null;
        //        try {
        //            fos = new FileOutputStream(jobLogFile);
        //            ObjectOutputStream oos = new ObjectOutputStream(fos);
        //            oos.writeObject(idList);
        //            oos.close();
        //
        //        } catch (FileNotFoundException e) {
        //            e.printStackTrace();
        //        } catch (IOException e) {
        //            e.printStackTrace();
        //        }
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
        return true;
    }

    /**
     * Try to get the results of jobs submitted before the Matlab client was disconnected
     *
     * @return
     */
    //    public ArrayList<ArrayList<RL>> getPreviousJobResults() {
    //        ArrayList<ArrayList<RL>> answer = new ArrayList<ArrayList<RL>>();
    //
    //        for (String jid : previousJobs.keySet()) {
    //            Integer[] nbres = previousJobs.get(jid);
    //            currentJobs.put(jid, new MatSciJobVolatileInfo<R>(false, true, nbres[0], nbres[1]));
    //            JobResult jResult = null;
    //            try {
    //                jResult = scheduler.getJobResult(jid);
    //                if (jResult != null) {
    //                    // If the result is already received
    //                    updateJobResult(jid, jResult);
    //                    answer.add(waitResultOfJob(jid));
    //                } else {
    //                    // If the answer is null, this means the job is known but not finished yet
    //                    // Consequently we delay the answer and put a future
    //                    lastSubJobId.add(jid);
    //                    //answer.add(stubOnThis.waitAllResults());
    //                }
    //            } catch (SchedulerException e) {
    //                // The job is not known to the scheduler, we choose to ignore this case
    //            }
    //
    //        }
    //        return answer;
    //    }
    /**
     * Returns all the results in an array or throw a RuntimeException in case of error
     *
     * @return array of ptolemy tokens
     */

    protected void updateJobResult(String jid, JobResult jResult) {
        // Getting the Job result from the Scheduler
        MatSciJobVolatileInfo jinfo = currentJobs.get(jid);
        if (jinfo.isDebugCurrentJob()) {
            System.out.println("[AOMatlabEnvironment] Updating results of job: " + jResult.getName() + "(" +
                jid + ") : " + jinfo.getStatus());
        }

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

                        handleResult(mainException, res, jid, tname);
                    }

                }
            }
            TreeSet<String> missinglist = jinfo.missingResults();
            for (String missing : missinglist) {
                handleResult(null, null, jid, missing);
            }

        }
        if (jResult != null) {
            jinfo.setStatus(jResult.getJobInfo().getStatus());
            jinfo.setJobFinished(true);
        }
    }

    //abstract protected void handleResult(Throwable mainException, TaskResult res, String jid, int tid, int d);

    protected void handleResult(Throwable mainException, TaskResult res, String jid, String tname) {
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
                        jid + " threw an exception : " + ex.getMessage());
                } else {
                    System.out.println("[AOMatSciEnvironment] Task " + tname + " for job " + jid +
                        " threw an exception : " + ex.getMessage());
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
        boolean answer = false;

        InputStream is = script.openStream();
        is.close();
        answer = true;

        return answer;
    }

    /**
     * Returns all the results in an array or throw a RuntimeException in case of error
     *
     * @return array of ptolemy tokens
     */
    //    public ArrayList<R> waitAllResults() {
    //
    //        return waitResultOfJob(waitAllResultsJobID);
    //    }
    //
    //    public R waitResult(int tid) {
    //        return waitResultOfTask(waitAllResultsJobID, tid);
    //    }
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
                    currentJobs.get(jid).setStatus(JobStatus.KILLED);
                    currentJobs.get(jid).setJobFinished(true);

                    updateJobResult(jid, null);
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

                    currentJobs.get(jid).setStatus(info.getStatus());
                    currentJobs.get(jid).setJobFinished(true);

                    updateJobResult(jid, null);
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

                    currentJobs.get(jid).setStatus(info.getStatus());
                    currentJobs.get(jid).setJobFinished(true);
                    JobResult jResult = null;
                    try {
                        jResult = scheduler.getJobResult(jid);
                    } catch (SchedulerException e) {
                        e.printStackTrace();
                    }
                    updateJobResult(jid, jResult);
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
                    currentJobs.get(jid).setStatus(info.getStatus());
                    currentJobs.get(jid).setJobFinished(true);
                    JobResult jResult = null;
                    try {
                        jResult = scheduler.getJobResult(jid);
                    } catch (SchedulerException e) {
                        e.printStackTrace();
                    }
                    updateJobResult(jid, jResult);
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
                handleResult(null, tres, jid, tnm);
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
        int cpt = 0;
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
                    currentJobs.get(lastSubJobId.remove()).setPendingWaitAllRequest(randomRequest);
                    if (debug) {
                        System.out.println("[AOMatSciEnvironment] Removed waitAllResults " + lastSubJobId +
                            " request from the queue");
                    }
                    service.blockingRemoveOldest("waitAllResults");
                } else if (randomRequest.getMethodName().equals("waitResult")) {
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
                    cpt++;
                    if (cpt == jinfo.nbResults()) {
                        cpt = 0;
                        lastSubJobId.remove();
                    }

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
