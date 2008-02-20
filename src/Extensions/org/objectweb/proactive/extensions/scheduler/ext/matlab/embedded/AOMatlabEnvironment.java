package org.objectweb.proactive.extensions.scheduler.ext.matlab.embedded;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.security.auth.login.LoginException;

import org.apache.log4j.Logger;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.body.request.Request;
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
 * @author fviale
 *
 */
public class AOMatlabEnvironment implements Serializable, SchedulerEventListener, InitActive, RunActive {

    private String password;
    private String schedulerUrl;
    private String user;
    private UserSchedulerInterface scheduler;
    private JobId currentJobId = null;
    private long taskIdStart = -1;
    private ArrayList<Token> results;

    protected Request pendingRequest;

    /**
     * log4j logger 
     */
    protected static Logger logger = ProActiveLogger.getLogger(Loggers.SCHEDULER_MATLAB_EXT);

    private long lastJobId = 0;
    private long lastTaskId = 0;
    private AOMatlabEnvironment stubOnThis;
    private boolean terminated;
    private boolean areAllResultsAvailable;
    private Throwable errorToThrow;

    /**
     * ProActive no arg constructor
     */
    @Deprecated
    public AOMatlabEnvironment() {

    }

    public AOMatlabEnvironment(String schedulerUrl, String user, String passwd) {
        this.schedulerUrl = schedulerUrl;
        this.user = user;
        this.password = passwd;
    }

    @SuppressWarnings("unchecked")
    public void initActivity(Body body) {
        stubOnThis = (AOMatlabEnvironment) PAActiveObject.getStubOnThis();
        results = new ArrayList<Token>();

        SchedulerAuthenticationInterface auth;
        try {
            auth = SchedulerConnection.join(schedulerUrl);

            this.scheduler = auth.logAsUser(user, password);
        } catch (LoginException e) {
            e.printStackTrace();
            return;
        } catch (SchedulerException e1) {
            e1.printStackTrace();
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

    public Token[] waitAllResults() {
        if (errorToThrow != null)
            throw new RuntimeException(errorToThrow);
        List<Token> answer = new ArrayList<Token>(results);
        results.clear();
        return answer.toArray(new Token[0]);
    }

    public void solve(SimpleMatlab[] tasks, JobPriority priority) {

        TaskFlowJob job = new TaskFlowJob();
        job.setName("Matlab Environment Job " + lastJobId++);
        job.setPriority(priority);
        job.setCancelOnError(true);
        job.setDescription("Set of parallel matlab tasks");
        taskIdStart = lastTaskId;
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
            e.printStackTrace();
        }

        this.areAllResultsAvailable = false;
        this.errorToThrow = null;

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
            System.out.println("---- HERE WE ARE 1");
            jobDidNotSucceed(event.getJobId(), e);
            return;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Updating results of job: " + jResult.getName());
        }

        for (long id = taskIdStart; (taskIdStart <= lastTaskId) ? (id < lastTaskId) : (id > taskIdStart) ||
            (id < lastTaskId); id++) {
            if (logger.isDebugEnabled()) {
                logger.debug("Looking for result of task: " + id);
            }
            TaskResult result = jResult.getAllResults().get("" + id);

            if (result == null) {
                System.out.println("---- HERE WE ARE 2");
                jobDidNotSucceed(event.getJobId(), new SchedulerException("Task id=" + id +
                    " was not returned by the scheduler"));

            } else if (result.hadException()) { //Exception took place inside the framework
                System.out.println("---- HERE WE ARE 3");
                jobDidNotSucceed(event.getJobId(), result.getException());
            } else {

                Token computedResult = null;
                try {
                    computedResult = (Token) result.value();
                    results.add(computedResult);
                } catch (Throwable e) {
                    System.out.println("---- HERE WE ARE 4");
                    jobDidNotSucceed(event.getJobId(), e);
                }
            }
        }

        areAllResultsAvailable = true;

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

    private void jobDidNotSucceed(JobId jobId, Throwable ex) {
        logger.error("Job did not succeed:" + ex.getMessage());
        ex.printStackTrace();

        if (errorToThrow == null) {
            errorToThrow = ex;
        }

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
            service.waitForRequest();
            // We detect a waitXXX request in the request queue
            Request waitRequest = service.getOldest("waitAllResults");
            if (waitRequest != null) {
                if (pendingRequest == null) {
                    // if there is one and there was none previously found we remove it and store it for later
                    pendingRequest = waitRequest;
                    service.blockingRemoveOldest("waitAllResults");
                } else {
                    // if there is one and there was another one pending, we serve it immediately (it's an error)
                    service.serveOldest("waitAllResults");
                }
            }

            // we serve everything else which is not a waitXXX method
            // Careful, the order is very important here, we need to serve the solve method before the waitXXX
            while (service.hasRequestToServe()) {
                service.serveOldest();
            }

            // we maybe serve the pending waitXXX method if there is one and if the necessary results are collected
            maybeServePending();
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
     */
    protected void maybeServePending() {
        if (pendingRequest != null) {
            if (areAllResultsAvailable()) {
                servePending();
            }
        }
    }

    /**
     * Serve the pending waitXXX method
     */
    protected void servePending() {
        Body body = PAActiveObject.getBodyOnThis();
        Request req = pendingRequest;
        pendingRequest = null;
        body.serve(req);
    }

    protected boolean areAllResultsAvailable() {
        return this.areAllResultsAvailable;
    }

    public void schedulerRMDownEvent() {
        // TODO Auto-generated method stub

    }

    public void schedulerRMUpEvent() {
        // TODO Auto-generated method stub

    }

}
