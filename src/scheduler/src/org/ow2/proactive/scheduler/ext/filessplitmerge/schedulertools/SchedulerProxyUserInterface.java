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
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.scheduler.common.SchedulerAuthenticationInterface;
import org.ow2.proactive.scheduler.common.SchedulerConnection;
import org.ow2.proactive.scheduler.common.SchedulerEvent;
import org.ow2.proactive.scheduler.common.SchedulerEventListener;
import org.ow2.proactive.scheduler.common.SchedulerState;
import org.ow2.proactive.scheduler.common.SchedulerStatus;
import org.ow2.proactive.scheduler.common.UserSchedulerInterface;
import org.ow2.proactive.scheduler.common.exception.InternalSchedulerException;
import org.ow2.proactive.scheduler.common.exception.SchedulerException;
import org.ow2.proactive.scheduler.common.job.Job;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobPriority;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.job.JobState;
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
public class SchedulerProxyUserInterface implements UserSchedulerInterface {

    private UserSchedulerInterface uischeduler;

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
            this.uischeduler = auth.logAsUser(cred);
        } catch (KeyException e) {
            throw new InternalSchedulerException(e);
        }

        //LoggerManager.getInstane().info("Connection to the scheduler successfully established. ");
        return true;
    }

    //@Override
    /**
     * Subscribes a listener to the Scheduler
     */
    public SchedulerState addSchedulerEventListener(SchedulerEventListener sel, boolean myEventsOnly,
            SchedulerEvent... events) throws SchedulerException {

        if (uischeduler == null)
            throw new InternalSchedulerException("Not connected to the schecduler.");

        //return uischeduler.addSchedulerEventListener(arg0, onlyMyEvents, arg1);
        return uischeduler.addEventListener(sel, myEventsOnly, true, events);
    }

    //@Override
    public void disconnect() throws SchedulerException {
        if (uischeduler == null)
            throw new InternalSchedulerException("Not connected to the schecduler.");

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
    public BooleanWrapper isConnected() {
        if (uischeduler == null) {
            return new BooleanWrapper(false);
        } else
            try {
                return uischeduler.isConnected();
            } catch (Exception e) {
                LoggerManager.getLogger().error(
                        "Error when callling " + this.getClass().getCanonicalName() +
                            " -> isConnected() method: " + e.getMessage() +
                            ". The connection is considered lost. ");
                return new BooleanWrapper(false);
            }
    }

    //@Override
    public void removeSchedulerEventListener() throws SchedulerException {
        if (uischeduler == null)
            throw new InternalSchedulerException("Not connected to the schecduler.");

        //uischeduler.removeSchedulerEventListener();
        uischeduler.removeEventListener();

    }

    //@Override
    public JobId submit(Job arg0) throws SchedulerException {
        if (uischeduler == null)
            throw new InternalSchedulerException("Not connected to the schecduler.");

        return uischeduler.submit(arg0);
    }

    //@Override
    public void changePriority(JobId arg0, JobPriority arg1) throws SchedulerException {
        if (uischeduler == null)
            throw new InternalSchedulerException("Not connected to the schecduler.");

        uischeduler.changePriority(arg0, arg1);

    }

    //@Override
    public JobResult getJobResult(String arg0) throws SchedulerException {
        if (uischeduler == null)
            throw new InternalSchedulerException("Not connected to the schecduler.");

        return uischeduler.getJobResult(arg0);
    }

    //@Override
    public JobResult getJobResult(JobId arg0) throws SchedulerException {
        if (uischeduler == null)
            throw new InternalSchedulerException("Not connected to the schecduler.");

        return uischeduler.getJobResult(arg0);
    }

    //@Override
    public TaskResult getTaskResult(JobId jobId, String arg1) throws SchedulerException {
        if (uischeduler == null)
            throw new InternalSchedulerException("Not connected to the schecduler.");

        return uischeduler.getTaskResult(jobId, arg1);
    }

    //@Override
    public TaskResult getTaskResult(String jobId, String arg1) throws SchedulerException {
        if (uischeduler == null)
            throw new InternalSchedulerException("Not connected to the schecduler.");

        return uischeduler.getTaskResult(jobId, arg1);
    }

    //@Override
    public BooleanWrapper kill(JobId arg0) throws SchedulerException {
        if (uischeduler == null)
            throw new InternalSchedulerException("Not connected to the schecduler.");

        return uischeduler.kill(arg0);
    }

    //@Override
    public void listenLog(JobId arg0, AppenderProvider arg1) throws SchedulerException {
        if (uischeduler == null)
            throw new InternalSchedulerException("Not connected to the schecduler.");

        uischeduler.listenLog(arg0, arg1);

    }

    //@Override
    public BooleanWrapper pause(JobId arg0) throws SchedulerException {
        if (uischeduler == null)
            throw new InternalSchedulerException("Not connected to the schecduler.");

        return uischeduler.pause(arg0);

    }

    //@Override
    public void remove(JobId arg0) throws SchedulerException {
        if (uischeduler == null)
            throw new InternalSchedulerException("Not connected to the schecduler.");

        uischeduler.remove(arg0);

    }

    //@Override
    public BooleanWrapper resume(JobId arg0) throws SchedulerException {
        if (uischeduler == null)
            throw new InternalSchedulerException("Not connected to the schecduler.");

        return uischeduler.resume(arg0);
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
    public void changePriority(String arg0, JobPriority arg1) throws SchedulerException {
        uischeduler.changePriority(arg0, arg1);

    }

    //@Override
    public SchedulerStatus getStatus() throws SchedulerException {
        return uischeduler.getSchedulerStatus();
    }

    //@Override
    public BooleanWrapper kill(String arg0) throws SchedulerException {

        return uischeduler.kill(arg0);
    }

    //@Override
    public BooleanWrapper pause(String arg0) throws SchedulerException {

        return uischeduler.pause(arg0);
    }

    //@Override
    public void remove(String arg0) throws SchedulerException {
        uischeduler.remove(arg0);

    }

    //@Override
    public BooleanWrapper resume(String arg0) throws SchedulerException {

        return uischeduler.resume(arg0);
    }

    //@Override
    public void addEventListener(SchedulerEventListener arg0, boolean arg1, SchedulerEvent... arg2)
            throws SchedulerException {
        // TODO Auto-generated method stub

    }

    //@Override
    public SchedulerState addEventListener(SchedulerEventListener arg0, boolean arg1, boolean arg2,
            SchedulerEvent... arg3) throws SchedulerException {
        // TODO Auto-generated method stub
        return null;
    }

    //@Override
    public SchedulerStatus getSchedulerStatus() throws SchedulerException {
        // TODO Auto-generated method stub
        return null;
    }

    //@Override
    public void listenLog(String arg0, AppenderProvider arg1) throws SchedulerException {
        // TODO Auto-generated method stub

    }

    //@Override
    public void removeEventListener() throws SchedulerException {
        // TODO Auto-generated method stub

    }

    //@Override
    public JobState getJobState(String arg0) throws SchedulerException {
        // TODO Auto-generated method stub
        return null;
    }

    //@Override
    public JobState getJobState(JobId arg0) throws SchedulerException {
        // TODO Auto-generated method stub
        return null;
    }

    //@Override
    public SchedulerState getSchedulerState() throws SchedulerException {
        // TODO Auto-generated method stub
        return null;
    }

}
