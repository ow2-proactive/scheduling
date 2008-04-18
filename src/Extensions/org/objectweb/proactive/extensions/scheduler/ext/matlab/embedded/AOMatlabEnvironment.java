package org.objectweb.proactive.extensions.scheduler.ext.matlab.embedded;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.security.auth.login.LoginException;

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
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.scheduler.common.exception.SchedulerException;
import org.objectweb.proactive.extensions.scheduler.common.exception.UserException;
import org.objectweb.proactive.extensions.scheduler.common.job.Job;
import org.objectweb.proactive.extensions.scheduler.common.job.JobEvent;
import org.objectweb.proactive.extensions.scheduler.common.job.JobId;
import org.objectweb.proactive.extensions.scheduler.common.job.JobPriority;
import org.objectweb.proactive.extensions.scheduler.common.job.JobResult;
import org.objectweb.proactive.extensions.scheduler.common.job.TaskFlowJob;
import org.objectweb.proactive.extensions.scheduler.common.job.UserIdentification;
import org.objectweb.proactive.extensions.scheduler.common.scheduler.SchedulerAuthenticationInterface;
import org.objectweb.proactive.extensions.scheduler.common.scheduler.SchedulerConnection;
import org.objectweb.proactive.extensions.scheduler.common.scheduler.SchedulerEvent;
import org.objectweb.proactive.extensions.scheduler.common.scheduler.SchedulerEventListener;
import org.objectweb.proactive.extensions.scheduler.common.scheduler.UserSchedulerInterface;
import org.objectweb.proactive.extensions.scheduler.common.task.JavaTask;
import org.objectweb.proactive.extensions.scheduler.common.task.TaskEvent;
import org.objectweb.proactive.extensions.scheduler.common.task.TaskResult;
import org.objectweb.proactive.extensions.scheduler.ext.matlab.SimpleMatlab;

import ptolemy.data.Token;


/**
 * This active object handles the connection between Matlab and the Scheduler directly from the Matlab environment
 * @author The ProActive Team
 *
 */
public class AOMatlabEnvironment implements Serializable, SchedulerEventListener, InitActive, RunActive {

    /**
     * URL to the scheduler
     */
    private String schedulerUrl;
    private String password;
    private String user;

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
    private TreeMap<String, Token> results;

    /**
     * WaitAllResults Request waiting to be served
     */
    protected Request pendingRequest;

    /**
     * log4j logger 
     */
    protected static Logger logger = ProActiveLogger.getLogger(Loggers.SCHEDULER_MATLAB_EXT);

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

    /**
     * Exception to throw in case of error
     */
    private Throwable errorToThrow;

    /**
     * ProActive no arg constructor
     */
    @Deprecated
    public AOMatlabEnvironment() {

    }

    /**
     * Creates a connection to the scheduler
     * @param schedulerUrl url of the scheduler
     * @param user username
     * @param passwd password
     */
    public AOMatlabEnvironment(String schedulerUrl, String user, String passwd) {
        this.schedulerUrl = schedulerUrl;
        this.user = user;
        this.password = passwd;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.proactive.InitActive#initActivity(org.objectweb.proactive.Body)
     */
    @SuppressWarnings("unchecked")
    public void initActivity(Body body) {
        stubOnThis = (AOMatlabEnvironment) PAActiveObject.getStubOnThis();
        results = new TreeMap<String, Token>();

        SchedulerAuthenticationInterface auth;
        try {
            auth = SchedulerConnection.join(schedulerUrl);

            this.scheduler = auth.logAsUser(user, password);
        } catch (LoginException e) {
            e.printStackTrace();
            terminated = true;
            return;
        } catch (SchedulerException e1) {
            e1.printStackTrace();
            terminated = true;
            return;
        }

        try {
            this.scheduler.addSchedulerEventListener((AOMatlabEnvironment) stubOnThis,
                    SchedulerEvent.JOB_KILLED, SchedulerEvent.JOB_RUNNING_TO_FINISHED, SchedulerEvent.KILLED,
                    SchedulerEvent.SHUTDOWN, SchedulerEvent.SHUTTING_DOWN);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }

    }

    /**
     * Returns all the results in an array or throw a RuntimeException in case of error
     * @return array of ptolemy tokens
     */
    public ArrayList<Token> waitAllResults() {
        if (logger.isDebugEnabled()) {
            logger.debug("Sending the results back...");
        }
        //Token[] answer = (new ArrayList<Token>(results)).toArray(new Token[0]);
        ArrayList<Token> answer = new ArrayList<Token>(results.values());
        results.clear();
        currentJobId = null;
        if (errorToThrow != null)
            throw new RuntimeException(errorToThrow);
        return answer;
    }

    /**
     * Submit a new bunch of tasks to the scheduler, throws a runtime exception if a job is currently running
     * @param tasks tasks to solve
     * @param priority priority of the job
     */
    public ArrayList<Token> solve(SimpleMatlab[] tasks, JobPriority priority) {
        //submit(tasks,priority);
        if (currentJobId != null) {
            throw new RuntimeException("The Scheduler is already busy with one job");
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Submitting job of " + tasks.length + " tasks...");
        }

        TaskFlowJob job = new TaskFlowJob();
        job.setName("Matlab Environment Job " + lastJobId++);
        job.setPriority(priority);
        job.setCancelOnException(true);
        job.setDescription("Set of parallel matlab tasks");
        job.setLogFile("Matlab_job_log_" + lastJobId + ".txt");
        for (SimpleMatlab task : tasks) {

            JavaTask schedulerTask = new JavaTask();

            schedulerTask.setName("" + lastTaskId++);
            schedulerTask.setPreciousResult(true);
            schedulerTask.setTaskInstance(task);

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
        this.errorToThrow = null;
        return stubOnThis.waitAllResults();
    }

    public void jobChangePriorityEvent(JobEvent event) {
        // TODO Auto-generated method stub

    }

    public void jobKilledEvent(JobId jobId) {
        // TODO Auto-generated method stub

    }

    public void jobPausedEvent(JobEvent event) {
        // TODO Auto-generated method stub

    }

    public void jobPendingToRunningEvent(JobEvent event) {
        // TODO Auto-generated method stub

    }

    public void jobRemoveFinishedEvent(JobEvent event) {
        // TODO Auto-generated method stub

    }

    public void jobResumedEvent(JobEvent event) {
        // TODO Auto-generated method stub

    }

    public void jobRunningToFinishedEvent(JobEvent event) {
        if (logger.isDebugEnabled()) {
            logger.debug("Received job finished event...");
        }

        if (event == null) {
            return;
        }

        if (!event.getJobId().equals(currentJobId)) {
            return;
        }

        JobResult jResult = null;

        try {
            jResult = scheduler.getJobResult(event.getJobId());
        } catch (SchedulerException e) {
            jobDidNotSucceed(event.getJobId(), e, true);
            return;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Updating results of job: " + jResult.getName());
        }

        HashMap<String, TaskResult> task_results = null;
        if (jResult.hadException()) {
            task_results = jResult.getExceptionResults();
        } else {
            task_results = jResult.getAllResults();
        }

        for (Map.Entry<String, TaskResult> res : task_results.entrySet()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Looking for result of task: " + res.getKey());
            }

            if (res.getValue() == null) {
                jobDidNotSucceed(event.getJobId(), new SchedulerException("Task id = " + res.getKey() +
                    " was not returned by the scheduler"), false);

            } else if (res.getValue().hadException()) { //Exception took place inside the framework
                jobDidNotSucceed(event.getJobId(), res.getValue().getException(), true);
            } else {

                Token computedResult = null;
                try {
                    computedResult = (Token) res.getValue().value();
                    results.put(res.getKey(), computedResult);
                } catch (Throwable e) {
                    jobDidNotSucceed(event.getJobId(), e, true);
                }
            }
        }

        isJobFinished = true;

    }

    public void jobSubmittedEvent(Job job) {
        // TODO Auto-generated method stub

    }

    public void schedulerImmediatePausedEvent() {
        // TODO Auto-generated method stub

    }

    public void schedulerKilledEvent() {
        // TODO Auto-generated method stub

    }

    public void schedulerPausedEvent() {
        // TODO Auto-generated method stub

    }

    public void schedulerResumedEvent() {
        // TODO Auto-generated method stub

    }

    public void schedulerShutDownEvent() {
        // TODO Auto-generated method stub

    }

    public void schedulerShuttingDownEvent() {
        // TODO Auto-generated method stub

    }

    public void schedulerStartedEvent() {
        // TODO Auto-generated method stub

    }

    public void schedulerStoppedEvent() {
        // TODO Auto-generated method stub

    }

    public void taskPendingToRunningEvent(TaskEvent event) {
        // TODO Auto-generated method stub

    }

    public void taskRunningToFinishedEvent(TaskEvent event) {
        // TODO Auto-generated method stub

    }

    private void jobDidNotSucceed(JobId jobId, Throwable ex, boolean printStack) {
        logger.error("Job did not succeed:" + ex.getMessage());
        if (printStack) {
            ex.printStackTrace();
        }

        if (errorToThrow == null) {
            errorToThrow = ex;
        }

        isJobFinished = true;

    }

    public void terminate() {
        this.terminated = true;
    }

    /**
     * {@inheritDoc}
     */
    public void runActivity(final Body body) {
        Service service = new Service(body);
        while (!terminated) {
            try {

                service.waitForRequest();
                // We detect a waitXXX request in the request queue
                Request waitRequest = service.getOldest("waitAllResults");
                if (waitRequest != null) {
                    if (pendingRequest == null) {
                        // if there is one and there was none previously found we remove it and store it for later
                        pendingRequest = waitRequest;
                        if (logger.isDebugEnabled()) {
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

    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws ActiveObjectCreationException, NodeException {

        AOMatlabEnvironment solver = (AOMatlabEnvironment) PAActiveObject.newActive(
                "org.objectweb.proactive.extensions.scheduler.ext.matlab.embedded.AOMatlabEnvironment",
                new Object[] { "//localhost", "user1", "pwd1" });
        //        SimpleMatlab[] tasks = new SimpleMatlab[100];
        //        for (int i=0; i < tasks.length; i++) {
        //            tasks[i] = new SimpleMatlab("in=0;","out=0;");
        //        }
        //        ArrayList<Token> res = solver.solve(tasks, org.objectweb.proactive.extensions.scheduler.common.job.JobPriority.NORMAL);
        //        res = (ArrayList<Token>) org.objectweb.proactive.api.PAFuture.getFutureValue(res);

        for (int j = 0; j < 10; j++) {
            try {
                SimpleMatlab[] tasks2 = new SimpleMatlab[5];
                for (int i = 0; i < tasks2.length; i++) {
                    tasks2[i] = new SimpleMatlab("in=" + i + 1 + ";", "i=in+1;");
                }
                ArrayList<Token> res2 = solver.solve(tasks2,
                        org.objectweb.proactive.extensions.scheduler.common.job.JobPriority.NORMAL);
                res2 = (ArrayList<Token>) org.objectweb.proactive.api.PAFuture.getFutureValue(res2);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * If there is a pending waitXXX method, we serve it if the necessary results are collected
     * @param body 
     */
    protected void maybeServePending(Service service) {
        if (pendingRequest != null) {
            if (isJobFinished()) {
                servePending(service);
            }
        }
    }

    /**
     * Serve the pending waitXXX method
     * @param body 
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
     * Internal class for filtering requests in the queue
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

}
