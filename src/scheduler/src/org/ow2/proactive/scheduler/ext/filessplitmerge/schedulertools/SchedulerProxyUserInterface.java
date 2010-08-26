//============================================================================
// Name        : ProActive Files Split-Merge Framework
// Author      : Emil Salageanu, ActiveEon team
// Version     : 0.1
// Copyright   : Copyright ActiveEon 2008-2009, Tous Droits Réservés (All Rights Reserved)
// Description : Framework for building distribution layers for native applications
//================================================================================

package org.ow2.proactive.scheduler.ext.filessplitmerge.schedulertools;

import java.security.KeyException;
import java.security.PublicKey;

import javax.security.auth.login.LoginException;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.NodeException;
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
 * You must init this  proxy (once) by calling the {@link #init(String, String, String)} method after the first call to 
 * the static method {@link #getActiveInstance()}
 * Next, any entity can obtain the active instance of this object and perform requests on it    
 * @author esalagea
 *
 */
public class SchedulerProxyUserInterface implements Scheduler {

    private Scheduler uischeduler;

    private String schedulerUrl;
    private String userName;
    private String password;

    private static SchedulerProxyUserInterface activeInstance;

    public SchedulerProxyUserInterface() {

    }

    public static SchedulerProxyUserInterface getActiveInstance() throws ActiveObjectCreationException,
            NodeException {
        if (activeInstance == null) {
            activeInstance = (SchedulerProxyUserInterface) PAActiveObject.newActive(
                    SchedulerProxyUserInterface.class.getName(), new Object[] {});
        }

        return activeInstance;
    }

    public boolean init(String url, String user, String pwd) throws SchedulerException, LoginException {
        this.schedulerUrl = url;
        this.userName = user;
        this.password = pwd;

        SchedulerAuthenticationInterface auth = SchedulerConnection.join(url);
        //this.uischeduler = auth.logAsUser(userName, passwd);
        PublicKey pubKey = auth.getPublicKey();

        try {
            Credentials cred = Credentials.createCredentials(user, pwd, pubKey);
            this.uischeduler = auth.login(cred);
        } catch (KeyException e) {
            throw new InternalSchedulerException(e);
        }

        //LoggerManager.getInstane().info("Connection to the scheduler successfully established. ");
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

        //return uischeduler.addSchedulerEventListener(arg0, onlyMyEvents, arg1);
        return uischeduler.addEventListener(sel, myEventsOnly, true, events);

    }

    //@Override
    public void disconnect() throws NotConnectedException, PermissionException {
        if (uischeduler == null)
            throw new NotConnectedException("Not connected to the schecduler.");

        uischeduler.disconnect();

    }

    //	//@Override
    //	public Stats getStats() throws SchedulerException {
    //		if (uischeduler==null)
    //			throw new SchedulerException("Not connected to the schecduler.");
    //
    //		return uischeduler.getStats();
    //	}

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

        //uischeduler.removeSchedulerEventListener();
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
            throw new NotConnectedException("Not connected to the schecduler.");
        }

        return uischeduler.resumeJob(jobId);
    }

    public String getSchedulerUrl() {
        return schedulerUrl;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
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
        // TODO Auto-generated method stub

    }

    public JobState getJobState(String jobId) throws NotConnectedException, UnknownJobException,
            PermissionException {
        // TODO Auto-generated method stub
        return null;
    }

    public void listenJobLogs(String jobId, AppenderProvider appenderProvider) throws NotConnectedException,
            UnknownJobException, PermissionException {
        // TODO Auto-generated method stub

    }

    public boolean changePolicy(Class<? extends Policy> newPolicyFile) throws NotConnectedException,
            PermissionException {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean freeze() throws NotConnectedException, PermissionException {
        // TODO Auto-generated method stub
        return false;
    }

    public JobState getJobState(JobId jobId) throws NotConnectedException, UnknownJobException,
            PermissionException {
        // TODO Auto-generated method stub
        return null;
    }

    public SchedulerState getState() throws NotConnectedException, PermissionException {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean kill() throws NotConnectedException, PermissionException {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean linkResourceManager(String rmURL) throws NotConnectedException, PermissionException {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean pause() throws NotConnectedException, PermissionException {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean resume() throws NotConnectedException, PermissionException {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean shutdown() throws NotConnectedException, PermissionException {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean start() throws NotConnectedException, PermissionException {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean stop() throws NotConnectedException, PermissionException {
        // TODO Auto-generated method stub
        return false;
    }

}
