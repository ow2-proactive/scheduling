/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of 
 * 						   Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org
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
 * If needed, contact us to obtain a release under GPL Version 2. 
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.gui.data;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Observer;

import javax.security.auth.login.LoginException;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.scheduler.common.AdminSchedulerInterface;
import org.ow2.proactive.scheduler.common.SchedulerAuthenticationInterface;
import org.ow2.proactive.scheduler.common.SchedulerConnection;
import org.ow2.proactive.scheduler.common.SchedulerEvent;
import org.ow2.proactive.scheduler.common.SchedulerEventListener;
import org.ow2.proactive.scheduler.common.SchedulerState;
import org.ow2.proactive.scheduler.common.SchedulerStatus;
import org.ow2.proactive.scheduler.common.UserSchedulerInterface;
import org.ow2.proactive.scheduler.common.exception.SchedulerException;
import org.ow2.proactive.scheduler.common.job.Job;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobPriority;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.policy.Policy;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.util.logforwarder.AppenderProvider;
import org.ow2.proactive.scheduler.common.util.logforwarder.LogForwardingException;
import org.ow2.proactive.scheduler.gui.Activator;
import org.ow2.proactive.scheduler.gui.actions.JMXChartItAction;
import org.ow2.proactive.scheduler.gui.composite.StatusLabel;
import org.ow2.proactive.scheduler.gui.dialog.SelectSchedulerDialogResult;
import org.ow2.proactive.scheduler.gui.listeners.SchedulerConnectionListener;
import org.ow2.proactive.scheduler.gui.views.ControllerView;
import org.ow2.proactive.scheduler.gui.views.SeparatedJobView;


/**
 * SchedulerProxy...
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
public class SchedulerProxy implements AdminSchedulerInterface {

    /**  */
    private static final long serialVersionUID = 20;
    private static final long SCHEDULER_SERVER_PING_FREQUENCY = 5000;
    public static final int CONNECTED = 0;
    public static final int LOGIN_OR_PASSWORD_WRONG = 1;
    private static SchedulerProxy instance = null;
    private SchedulerAuthenticationInterface sai;
    private UserSchedulerInterface scheduler = null;
    private String userName = null;
    private Boolean logAsAdmin = false;
    private Thread pinger;
    private String schedulerURL;
    private boolean connected = false;

    List<SchedulerConnectionListener> observers;

    // -------------------------------------------------------------------- //
    // --------------------------- constructor ---------------------------- //
    // -------------------------------------------------------------------- //
    /**
     * Create a new instance of SchedulerProxy.
     *
     */
    public SchedulerProxy() {
        observers = new LinkedList<SchedulerConnectionListener>();
    }

    // -------------------------------------------------------------------- //
    // ---------------- implements AdminSchedulerInterface ---------------- //
    // -------------------------------------------------------------------- //
    @Deprecated
    public SchedulerState addSchedulerEventListener(SchedulerEventListener listener, boolean myEventsOnly,
            SchedulerEvent... events) {
        // Do nothing
        return null;
    }

    public void addEventListener(SchedulerEventListener listener, boolean myEventsOnly,
            SchedulerEvent... events) throws SchedulerException {
        // Do nothing (unused)
    }

    /**
     * @see org.ow2.proactive.scheduler.common.UserSchedulerInterface#addEventListener(org.ow2.proactive.scheduler.common.SchedulerEventListener, boolean, boolean, org.ow2.proactive.scheduler.common.SchedulerEvent[])
     */
    public SchedulerState addEventListener(SchedulerEventListener listener, boolean myEventsOnly,
            boolean getSchedulerState, SchedulerEvent... events) {
        try {
            return (SchedulerState) scheduler.addEventListener(listener, myEventsOnly, getSchedulerState,
                    events);
        } catch (SchedulerException e) {
            Activator.log(IStatus.ERROR, "Error in Scheduler Proxy ", e);
            e.printStackTrace();
        }
        return null;
    }

    @Deprecated
    public void removeSchedulerEventListener() throws SchedulerException {
        //unused anymore
    }

    /**
     * @see org.ow2.proactive.scheduler.common.UserSchedulerInterface#removeEventListener()
     */
    public void removeEventListener() throws SchedulerException {
        //not used for the GUI
    }

    /**
     * @see org.ow2.proactive.scheduler.common.UserSchedulerInterface#disconnect()
     */
    public void disconnect() {
        if (pinger != null) {
            pinger.interrupt();
        }
        if (scheduler != null) {
            try {
                //disconnect scheduler if it is not dead
                //protect disconnection with a try catch
                scheduler.disconnect();
                connected = false;
            } catch (Exception e) {
                // Nothing to do
                Activator.log(IStatus.ERROR, "- Scheduler Proxy: Error in  disconnect action", e);
            }
            sendConnectionLostEvent();
        }
    }

    public void serverDown() {
        pinger.interrupt();
        Display.getDefault().asyncExec(new Runnable() {
            public void run() {
                MessageDialog.openInformation(SeparatedJobView.getSchedulerShell(), "Scheduler server down",
                        "Scheduler  '" + schedulerURL + "'  seems to be down, now disconnect.");
                StatusLabel.getInstance().disconnect();
                // stop log server
                try {
                    Activator.terminateLoggerServer();
                } catch (LogForwardingException e) {
                    Activator.log(IStatus.ERROR,
                            "- Scheduler Proxy: Error while terminating the logger server", e);
                    e.printStackTrace();
                }
                SeparatedJobView.clearOnDisconnection(true);
                connected = false;
            }
        });
    }

    /**
     * @see org.ow2.proactive.scheduler.common.UserSchedulerInterface_#getJobResult(org.ow2.proactive.scheduler.common.job.JobId)
     */
    public JobResult getJobResult(JobId jobId) {
        try {
            return scheduler.getJobResult(jobId);
        } catch (SchedulerException e) {
            Activator.log(IStatus.ERROR, "- Scheduler Proxy: Error when getting job result", e);
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @see org.ow2.proactive.scheduler.common.UserSchedulerInterface_#getTaskResult(org.ow2.proactive.scheduler.common.job.JobId, java.lang.String)
     */
    public TaskResult getTaskResult(JobId jobId, String taskName) {
        try {
            return scheduler.getTaskResult(jobId, taskName);
        } catch (SchedulerException e) {
            Activator.log(IStatus.ERROR, "- Scheduler Proxy: Error when getting task result", e);
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @see org.ow2.proactive.scheduler.common.UserSchedulerInterface_#kill(org.ow2.proactive.scheduler.common.job.JobId)
     */
    public BooleanWrapper kill(JobId jobId) {
        try {
            return scheduler.kill(jobId);
        } catch (SchedulerException e) {
            Activator.log(IStatus.ERROR, "- Scheduler Proxy: Error on kill job ", e);
            e.printStackTrace();
        }
        return new BooleanWrapper(false);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.UserSchedulerInterface_#listenLog(org.ow2.proactive.scheduler.common.job.JobId, AppenderProvider appenderProvider)
     */
    public void listenLog(JobId jobId, AppenderProvider appenderProvider) {
        try {
            scheduler.listenLog(jobId, appenderProvider);
        } catch (SchedulerException e) {
            Activator.log(IStatus.ERROR, "- Scheduler Proxy: Error on listen log", e);
            e.printStackTrace();
        }
    }

    /**
     * @see org.ow2.proactive.scheduler.common.UserSchedulerInterface_#pause(org.ow2.proactive.scheduler.common.job.JobId)
     */
    public BooleanWrapper pause(JobId jobId) {
        try {
            return scheduler.pause(jobId);
        } catch (SchedulerException e) {
            Activator.log(IStatus.ERROR, "- Scheduler Proxy: Error on pause", e);
            e.printStackTrace();
        }
        return new BooleanWrapper(false);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.UserSchedulerInterface_#resume(org.ow2.proactive.scheduler.common.job.JobId)
     */
    public BooleanWrapper resume(JobId jobId) {
        try {
            return scheduler.resume(jobId);
        } catch (SchedulerException e) {
            Activator.log(IStatus.ERROR, "- Scheduler Proxy: Error on resume action", e);
            e.printStackTrace();
        }
        return new BooleanWrapper(false);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.UserSchedulerInterface#submit(org.ow2.proactive.scheduler.common.job.Job)
     */
    public JobId submit(Job job) throws SchedulerException {
        return scheduler.submit(job);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.UserSchedulerInterface_#remove(org.ow2.proactive.scheduler.common.job.JobId)
     */
    public void remove(JobId jobId) {
        try {
            scheduler.remove(jobId);
        } catch (SchedulerException e) {
            Activator.log(IStatus.ERROR, "- Scheduler Proxy: Error on remove job action ", e);
            e.printStackTrace();
        }
    }

    /**
     * @see org.ow2.proactive.scheduler.common.AdminMethodsInterface#kill()
     */
    public BooleanWrapper kill() {
        if (pinger != null) {
            pinger.interrupt();
        }
        try {
            return ((AdminSchedulerInterface) scheduler).kill();
        } catch (SchedulerException e) {
            Activator.log(IStatus.ERROR, "- Scheduler Proxy: Error on kill action ", e);
            e.printStackTrace();
        }
        return new BooleanWrapper(false);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.AdminMethodsInterface#pause()
     */
    public BooleanWrapper pause() {
        try {
            return ((AdminSchedulerInterface) scheduler).pause();
        } catch (SchedulerException e) {
            Activator.log(IStatus.ERROR, "- Scheduler Proxy: Error on pause action ", e);
            e.printStackTrace();
        }
        return new BooleanWrapper(false);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.AdminMethodsInterface#freeze()
     */
    public BooleanWrapper freeze() {
        try {
            return ((AdminSchedulerInterface) scheduler).freeze();
        } catch (SchedulerException e) {
            Activator.log(IStatus.ERROR, "- Scheduler Proxy: Error on freeze action ", e);
            e.printStackTrace();
        }
        return new BooleanWrapper(false);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.AdminMethodsInterface#resume()
     */
    public BooleanWrapper resume() {
        try {
            return ((AdminSchedulerInterface) scheduler).resume();
        } catch (SchedulerException e) {
            Activator.log(IStatus.ERROR, "- Scheduler Proxy: Error on resume action ", e);
            e.printStackTrace();
        }
        return new BooleanWrapper(false);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.AdminMethodsInterface#shutdown()
     */
    public BooleanWrapper shutdown() {
        if (pinger != null) {
            pinger.interrupt();
        }
        try {
            return ((AdminSchedulerInterface) scheduler).shutdown();
        } catch (SchedulerException e) {
            Activator.log(IStatus.ERROR, "- Scheduler Proxy: Error on shut down action ", e);
            e.printStackTrace();
        }
        return new BooleanWrapper(false);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.AdminMethodsInterface#start()
     */
    public BooleanWrapper start() {
        try {
            return ((AdminSchedulerInterface) scheduler).start();
        } catch (SchedulerException e) {
            Activator.log(IStatus.ERROR, "- Scheduler Proxy: Error on start action", e);
            e.printStackTrace();
        }
        return new BooleanWrapper(false);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.AdminMethodsInterface#stop()
     */
    public BooleanWrapper stop() {
        try {
            return ((AdminSchedulerInterface) scheduler).stop();
        } catch (SchedulerException e) {
            Activator.log(IStatus.ERROR, "- Scheduler Proxy: Error on stop action ", e);
            e.printStackTrace();
        }
        return new BooleanWrapper(false);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.UserSchedulerInterface_#changePriority(org.ow2.proactive.scheduler.common.job.JobId, org.ow2.proactive.scheduler.common.job.JobPriority)
     */
    public void changePriority(JobId jobId, JobPriority priority) {
        try {
            scheduler.changePriority(jobId, priority);
        } catch (SchedulerException e) {
            Activator.log(IStatus.ERROR, "- Scheduler Proxy: Error on change priority action ", e);
            e.printStackTrace();
        }
    }

    /**
     * @see org.ow2.proactive.scheduler.common.AdminMethodsInterface#changePolicy(java.lang.Class)
     */
    public BooleanWrapper changePolicy(Class<? extends Policy> newPolicyFile) throws SchedulerException {
        try {
            return ((AdminSchedulerInterface) scheduler).changePolicy(newPolicyFile);
        } catch (SchedulerException e) {
            Activator.log(IStatus.ERROR, "- Scheduler Proxy: Error on change Policy action", e);
            e.printStackTrace();
        }
        return new BooleanWrapper(false);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.AdminMethodsInterface#linkResourceManager(java.lang.String)
     */
    public BooleanWrapper linkResourceManager(String rmURL) throws SchedulerException {
        try {
            return ((AdminSchedulerInterface) scheduler).linkResourceManager(rmURL);
        } catch (SchedulerException e) {
            Activator.log(IStatus.ERROR, "- Scheduler Proxy: Error on link Resource Manager action", e);
            e.printStackTrace();
        }
        return new BooleanWrapper(false);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.UserSchedulerInterface#isConnected()
     */
    public BooleanWrapper isConnected() {
        return ((AdminSchedulerInterface) scheduler).isConnected();
    }

    // -------------------------------------------------------------------- //
    // ------------------------------ public ------------------------------ //
    // -------------------------------------------------------------------- //
    public int connectToScheduler(SelectSchedulerDialogResult dialogResult) throws Throwable {
        try {
            userName = dialogResult.getLogin();
            logAsAdmin = dialogResult.isLogAsAdmin();
            schedulerURL = dialogResult.getUrl();
            sai = SchedulerConnection.join(schedulerURL);
            final Credentials creds = Credentials.createCredentials(userName, dialogResult.getPassword(), sai
                    .getPublicKey());
            if (logAsAdmin) {
                scheduler = sai.logAsAdmin(creds);
            } else {
                scheduler = sai.logAsUser(creds);
            }
            sendConnectionCreatedEvent(dialogResult.getUrl(), userName, dialogResult.getPassword());
            startPinger();
            connected = true;
            ControllerView.getInstance().connectedEvent(logAsAdmin);
            JMXChartItAction.getInstance().initJMXClient(sai, dialogResult.getLogin(),
                    dialogResult.getPassword(), logAsAdmin);
            return CONNECTED;
        } catch (LoginException e) {
            e.printStackTrace();
            // exception is handled by the GUI
            userName = null;
            logAsAdmin = false;
            return LOGIN_OR_PASSWORD_WRONG;
        } catch (Throwable t) {
            Activator.log(IStatus.ERROR, "- Error when connecting to the scheduler ", t);
            userName = null;
            logAsAdmin = false;
            throw t;
        }
    }

    private void startPinger() {
        //final SchedulerProxy thisStub = (SchedulerProxy) PAActiveObject.getStubOnThis();
        pinger = new Thread() {
            @Override
            public void run() {
                while (!pinger.isInterrupted()) {
                    try {
                        Thread.sleep(SCHEDULER_SERVER_PING_FREQUENCY);
                        //try to ping Scheduler server
                        if (PAActiveObject.pingActiveObject(sai)) {
                            //if OK continue
                            continue;
                        } else {
                            //if not, shutdown Scheduler
                            try {
                                serverDown();
                            } catch (Exception e) {
                                //thisStub has already been killed, shutdown, or disconnected
                                break;
                            }
                        }
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }
        };
        pinger.start();
    }

    public Boolean isItHisJob(String userName) {
        if (logAsAdmin) {
            return true;
        }
        if ((this.userName == null) || (userName == null)) {
            return false;
        }
        return this.userName.equals(userName);
    }

    public boolean isAnAdmin() {
        return logAsAdmin;
    }

    // -------------------------------------------------------------------- //
    // ------------------------------ Static ------------------------------ //
    // -------------------------------------------------------------------- //
    public static SchedulerProxy getInstanceWithException() throws Throwable {
        if (instance == null) {
            instance = (SchedulerProxy) PAActiveObject.newActive(SchedulerProxy.class.getName(), null);
        }
        return instance;
    }

    public static SchedulerProxy getInstance() {
        if (instance == null) {
            try {
                instance = getInstanceWithException();
            } catch (Throwable t) {
                Activator.log(IStatus.ERROR, "- Scheduler Proxy: Error on get instance ", t);
                //t.printStackTrace();
            }
        }
        return instance;
    }

    public boolean isProxyConnected() {
        return connected;
    }

    public UserSchedulerInterface getScheduler() {
        return scheduler;
    }

    public static void clearInstance() {
        instance = null;
    }

    public void addConnectionListener(SchedulerConnectionListener obs) {
        this.observers.add(obs);
    }

    public void removeObserver(Observer obs) {
        this.observers.remove(obs);
    }

    public void sendConnectionCreatedEvent(String schedulerUrl, String user, String password) {
        Iterator<SchedulerConnectionListener> it = this.observers.iterator();
        while (it.hasNext()) {
            SchedulerConnectionListener o = it.next();
            o.connectionCreatedEvent(schedulerUrl, user, password);
        }
    }

    public void sendConnectionLostEvent() {
        Iterator<SchedulerConnectionListener> it = this.observers.iterator();
        while (it.hasNext()) {
            SchedulerConnectionListener o = it.next();
            o.connectionLostEvent();
        }
    }

    /**
     * @see org.ow2.proactive.scheduler.common.UserSchedulerInterface#getJobResult(java.lang.String)
     */
    public JobResult getJobResult(String jobId) throws SchedulerException {
        return scheduler.getJobResult(jobId);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.UserSchedulerInterface#getTaskResult(java.lang.String, java.lang.String)
     */
    public TaskResult getTaskResult(String jobId, String taskName) throws SchedulerException {
        return scheduler.getTaskResult(jobId, taskName);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.UserSchedulerInterface#changePriority(java.lang.String, org.ow2.proactive.scheduler.common.job.JobPriority)
     */
    public void changePriority(String jobId, JobPriority newPrio) throws SchedulerException {
        scheduler.changePriority(jobId, newPrio);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.UserSchedulerInterface#kill(java.lang.String)
     */
    public BooleanWrapper kill(String jobId) throws SchedulerException {
        return scheduler.kill(jobId);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.UserSchedulerInterface#pause(java.lang.String)
     */
    public BooleanWrapper pause(String jobId) throws SchedulerException {
        return scheduler.pause(jobId);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.UserSchedulerInterface#remove(java.lang.String)
     */
    public void remove(String jobId) throws SchedulerException {
        scheduler.remove(jobId);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.UserSchedulerInterface#resume(java.lang.String)
     */
    public BooleanWrapper resume(String jobId) throws SchedulerException {
        return scheduler.resume(jobId);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.UserSchedulerInterface#listenLog(java.lang.String, org.ow2.proactive.scheduler.common.util.logforwarder.AppenderProvider)
     */
    public void listenLog(String jobId, AppenderProvider appender) throws SchedulerException {
        scheduler.listenLog(jobId, appender);
    }

    @Deprecated
    public SchedulerStatus getStatus() {
        //unused anymore
        return null;
    }

    /**
     * @see org.ow2.proactive.scheduler.common.UserSchedulerInterface#getSchedulerStatus()
     */
    public SchedulerStatus getSchedulerStatus() throws SchedulerException {
        try {
            return scheduler.getSchedulerStatus();
        } catch (SchedulerException e) {
            Activator.log(IStatus.ERROR, "- Scheduler Proxy: Error while getting status", e);
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @see org.ow2.proactive.scheduler.common.UserSchedulerInterface#getJobState(java.lang.String)
     */
    public JobState getJobState(String id) throws SchedulerException {
        return scheduler.getJobState(id);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.UserSchedulerInterface_#getJobState(org.ow2.proactive.scheduler.common.job.JobId)
     */
    public JobState getJobState(JobId id) throws SchedulerException {
        return scheduler.getJobState(id);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.UserSchedulerInterface#getSchedulerState()
     */
    public SchedulerState getSchedulerState() throws SchedulerException {
        return scheduler.getSchedulerState();
    }

}
