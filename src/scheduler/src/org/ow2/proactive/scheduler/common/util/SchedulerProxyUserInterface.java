package org.ow2.proactive.scheduler.common.util;

import java.io.Serializable;
import java.security.KeyException;
import java.security.PublicKey;

import javax.security.auth.login.LoginException;

import org.objectweb.proactive.extensions.annotation.ActiveObject;
import org.ow2.proactive.authentication.crypto.CredData;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.SchedulerAuthenticationInterface;
import org.ow2.proactive.scheduler.common.SchedulerConnection;
import org.ow2.proactive.scheduler.common.SchedulerEvent;
import org.ow2.proactive.scheduler.common.SchedulerEventListener;
import org.ow2.proactive.scheduler.common.SchedulerState;
import org.ow2.proactive.scheduler.common.SchedulerStatus;
import org.ow2.proactive.scheduler.common.exception.InternalSchedulerException;
import org.ow2.proactive.scheduler.common.exception.JobAlreadyFinishedException;
import org.ow2.proactive.scheduler.common.exception.JobCreationException;
import org.ow2.proactive.scheduler.common.exception.NotConnectedException;
import org.ow2.proactive.scheduler.common.exception.PermissionException;
import org.ow2.proactive.scheduler.common.exception.SchedulerException;
import org.ow2.proactive.scheduler.common.exception.SubmissionClosedException;
import org.ow2.proactive.scheduler.common.exception.UnknownJobException;
import org.ow2.proactive.scheduler.common.exception.UnknownTaskException;
import org.ow2.proactive.scheduler.common.job.Job;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobPriority;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.policy.Policy;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.util.logforwarder.AppenderProvider;
import org.ow2.proactive.scheduler.ext.filessplitmerge.logging.LoggerManager;


/**
 * This class implements an active object managing a connection to the Scheduler (a proxy to the Scheduler)
 * You must init the proxy by calling the {@link #init(String, String, String)} method after having created it
 */
@ActiveObject
public class SchedulerProxyUserInterface implements Scheduler,Serializable{

    protected Scheduler uischeduler;

    public SchedulerProxyUserInterface() {

    }

    public boolean init(String url, String user, String pwd) throws SchedulerException, LoginException {

        SchedulerAuthenticationInterface auth = SchedulerConnection.join(url);
        PublicKey pubKey = auth.getPublicKey();

        try {
            Credentials cred = Credentials.createCredentials(new CredData(user, pwd), pubKey);
            this.uischeduler = auth.login(cred);
        } catch (KeyException e) {
            throw new InternalSchedulerException(e);
        }

        return true;
    }

    /**
     * Subscribes a listener to the Scheduler
     */
    //@Override
    public SchedulerState addEventListener(SchedulerEventListener sel, boolean myEventsOnly,
            boolean getCurrentState, SchedulerEvent... events) throws NotConnectedException,
            PermissionException {
        if (uischeduler == null) {
            throw new NotConnectedException("Not connected to the scheduler.");
        }

        return uischeduler.addEventListener(sel, myEventsOnly, true, events);

    }

    //@Override
    public void disconnect() throws NotConnectedException, PermissionException {
        if (uischeduler == null)
            throw new NotConnectedException("Not connected to the schecduler.");

        uischeduler.disconnect();

    }

    //@Override
    public boolean isConnected() {
        if (uischeduler == null) {
            return false;
        } else
            try {
                return uischeduler.isConnected();
            } catch (Exception e) {
                LoggerManager.getLogger().error(
                        "Error when callling " + this.getClass().getCanonicalName() +
                            " -> isConnected() method: " + e.getMessage() +
                            ". The connection is considered lost. ");
                return false;
            }
    }

    //@Override
    public void removeEventListener() throws NotConnectedException, PermissionException {
        if (uischeduler == null) {
            throw new NotConnectedException("Not connected to the schecduler.");
        }

        uischeduler.removeEventListener();

    }

    //@Override
    public JobId submit(Job job) throws NotConnectedException, PermissionException,
            SubmissionClosedException, JobCreationException {
        if (uischeduler == null) {
            throw new NotConnectedException("Not connected to the schecduler.");
        }

        return uischeduler.submit(job);
    }

    //@Override
    public void changeJobPriority(JobId jobId, JobPriority priority) throws NotConnectedException,
            UnknownJobException, PermissionException, JobAlreadyFinishedException {
        if (uischeduler == null) {
            throw new NotConnectedException("Not connected to the schecduler.");
        }

        uischeduler.changeJobPriority(jobId, priority);

    }

    //@Override
    public JobResult getJobResult(String jobId) throws NotConnectedException, PermissionException,
            UnknownJobException {
        if (uischeduler == null) {
            throw new NotConnectedException("Not connected to the schecduler.");
        }

        return uischeduler.getJobResult(jobId);
    }

    //@Override
    public JobResult getJobResult(JobId jobId) throws NotConnectedException, PermissionException,
            UnknownJobException {
        if (uischeduler == null) {
            throw new NotConnectedException("Not connected to the schecduler.");
        }

        return uischeduler.getJobResult(jobId);
    }

    //@Override
    public TaskResult getTaskResult(String jobId, String taskName) throws NotConnectedException,
            UnknownJobException, UnknownTaskException, PermissionException {
        if (uischeduler == null) {
            throw new NotConnectedException("Not connected to the schecduler.");
        }

        return uischeduler.getTaskResult(jobId, taskName);
    }

    //@Override
    public TaskResult getTaskResult(JobId jobId, String taskName) throws NotConnectedException,
            UnknownJobException, UnknownTaskException, PermissionException {
        if (uischeduler == null) {
            throw new NotConnectedException("Not connected to the schecduler.");
        }

        return uischeduler.getTaskResult(jobId, taskName);
    }

    //@Override
    public boolean killJob(JobId jobId) throws NotConnectedException, UnknownJobException,
            PermissionException {
        if (uischeduler == null) {
            throw new NotConnectedException("Not connected to the schecduler.");
        }

        return uischeduler.killJob(jobId);
    }

    //@Override
    public void listenJobLogs(JobId jobId, AppenderProvider appenderProvider) throws NotConnectedException,
            UnknownJobException, PermissionException {
        if (uischeduler == null) {
            throw new NotConnectedException("Not connected to the schecduler.");
        }

        uischeduler.listenJobLogs(jobId, appenderProvider);

    }

    //@Override
    public boolean pauseJob(JobId jobId) throws NotConnectedException, UnknownJobException,
            PermissionException {
        if (uischeduler == null) {
            throw new NotConnectedException("Not connected to the schecduler.");
        }

        return uischeduler.pauseJob(jobId);

    }

    //@Override
    public boolean removeJob(JobId jobId) throws NotConnectedException, UnknownJobException,
            PermissionException {
        if (uischeduler == null) {
            throw new NotConnectedException("Not connected to the schecduler.");
        }

        return uischeduler.removeJob(jobId);

    }

    //@Override
    public boolean resumeJob(JobId jobId) throws NotConnectedException, UnknownJobException,
            PermissionException {
        if (uischeduler == null) {
            throw new NotConnectedException("Not connected to the scheduler.");
        }
        return uischeduler.resumeJob(jobId);
    }

    //@Override
    public void changeJobPriority(String jobId, JobPriority priority) throws NotConnectedException,
            UnknownJobException, PermissionException, JobAlreadyFinishedException {
        uischeduler.changeJobPriority(jobId, priority);

    }

    //@Override
    public SchedulerStatus getStatus() throws NotConnectedException, PermissionException {
        return uischeduler.getStatus();
    }

    //@Override
    public boolean killJob(String jobId) throws NotConnectedException, UnknownJobException,
            PermissionException {
        return uischeduler.killJob(jobId);
    }

    //@Override
    public boolean pauseJob(String jobId) throws NotConnectedException, UnknownJobException,
            PermissionException {
        return uischeduler.pauseJob(jobId);
    }

    //@Override
    public boolean removeJob(String jobId) throws NotConnectedException, UnknownJobException,
            PermissionException {
        return uischeduler.removeJob(jobId);
    }

    //@Override
    public boolean resumeJob(String jobId) throws NotConnectedException, UnknownJobException,
            PermissionException {
        return uischeduler.resumeJob(jobId);
    }

    public void addEventListener(SchedulerEventListener sel, boolean myEventsOnly, SchedulerEvent... events)
            throws NotConnectedException, PermissionException {
        uischeduler.addEventListener(sel, myEventsOnly, events);
    }

    public JobState getJobState(String jobId) throws NotConnectedException, UnknownJobException,
            PermissionException {
        return uischeduler.getJobState(jobId);
    }

    public void listenJobLogs(String jobId, AppenderProvider appenderProvider) throws NotConnectedException,
            UnknownJobException, PermissionException {
        uischeduler.listenJobLogs(jobId, appenderProvider);
    }

    public boolean changePolicy(Class<? extends Policy> newPolicyFile) throws NotConnectedException,
            PermissionException {
        return uischeduler.changePolicy(newPolicyFile);
    }

    public boolean freeze() throws NotConnectedException, PermissionException {
        return uischeduler.freeze();
    }

    public JobState getJobState(JobId jobId) throws NotConnectedException, UnknownJobException,
            PermissionException {
        return uischeduler.getJobState(jobId);
    }

    public SchedulerState getState() throws NotConnectedException, PermissionException {
        return uischeduler.getState();
    }

    public boolean kill() throws NotConnectedException, PermissionException {
        return uischeduler.kill();
    }

    public boolean linkResourceManager(String rmURL) throws NotConnectedException, PermissionException {
        return uischeduler.linkResourceManager(rmURL);
    }

    public boolean pause() throws NotConnectedException, PermissionException {
        return uischeduler.pause();
    }

    public boolean resume() throws NotConnectedException, PermissionException {
        return uischeduler.resume();
    }

    public boolean shutdown() throws NotConnectedException, PermissionException {
        return uischeduler.shutdown();
    }

    public boolean start() throws NotConnectedException, PermissionException {
        return uischeduler.start();
    }

    public boolean stop() throws NotConnectedException, PermissionException {
        return uischeduler.stop();
    }

    public SchedulerState getState(boolean myJobsOnly) throws NotConnectedException, PermissionException {
        return uischeduler.getState(myJobsOnly);
    }


}
