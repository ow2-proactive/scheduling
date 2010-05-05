/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
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
 * If needed, contact us to obtain a release under GPL Version 2 
 * or a different license than the GPL.
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
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.SchedulerAuthenticationInterface;
import org.ow2.proactive.scheduler.common.SchedulerConnection;
import org.ow2.proactive.scheduler.common.SchedulerEvent;
import org.ow2.proactive.scheduler.common.SchedulerEventListener;
import org.ow2.proactive.scheduler.common.SchedulerState;
import org.ow2.proactive.scheduler.common.SchedulerStatus;
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
import org.ow2.proactive.scheduler.common.util.logforwarder.LogForwardingException;
import org.ow2.proactive.scheduler.gui.Activator;
import org.ow2.proactive.scheduler.gui.actions.JMXActionsManager;
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
public class SchedulerProxy implements Scheduler {

    private static final long SCHEDULER_SERVER_PING_FREQUENCY = 5000;
    public static final int CONNECTED = 1;
    public static final int LOGIN_OR_PASSWORD_WRONG = 2;
    private static SchedulerProxy instance = null;
    private SchedulerAuthenticationInterface sai;
    private Scheduler scheduler = null;
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

    private void displayError(final String message) {
        Display.getDefault().asyncExec(new Runnable() {
            public void run() {
                MessageDialog.openError(Display.getDefault().getActiveShell(), "Error !", message);
            }
        });
    }

    // -------------------------------------------------------------------- //
    // ---------------- implements AdminSchedulerInterface ---------------- //
    // -------------------------------------------------------------------- //

    /**
     * {@inheritDoc}
     */
    public void addEventListener(SchedulerEventListener sel, boolean myEventsOnly, SchedulerEvent... events) {
        // Do nothing (unused)
    }

    /**
     * {@inheritDoc}
     */
    public SchedulerState addEventListener(SchedulerEventListener listener, boolean myEventsOnly,
            boolean getSchedulerState, SchedulerEvent... events) {
        try {
            return (SchedulerState) scheduler.addEventListener(listener, myEventsOnly, getSchedulerState,
                    events);
        } catch (SchedulerException e) {
            Activator.log(IStatus.ERROR, "Error in Scheduler Proxy ", e);
            displayError(e.getMessage());
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public void removeEventListener() {
        //not used for the GUI
    }

    /**
     * {@inheritDoc}
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
                displayError(e.getMessage());
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
     * {@inheritDoc}
     */
    public JobResult getJobResult(JobId jobId) {
        try {
            return scheduler.getJobResult(jobId);
        } catch (SchedulerException e) {
            Activator.log(IStatus.ERROR, "- Scheduler Proxy: Error when getting job result", e);
            displayError(e.getMessage());
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public TaskResult getTaskResult(JobId jobId, String taskName) {
        try {
            return scheduler.getTaskResult(jobId, taskName);
        } catch (SchedulerException e) {
            Activator.log(IStatus.ERROR, "- Scheduler Proxy: Error when getting task result", e);
            displayError(e.getMessage());
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public boolean killJob(JobId jobId) {
        try {
            return scheduler.killJob(jobId);
        } catch (SchedulerException e) {
            Activator.log(IStatus.ERROR, "- Scheduler Proxy: Error on kill job ", e);
            displayError(e.getMessage());
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public void listenJobLogs(JobId jobId, AppenderProvider appenderProvider) {
        try {
            scheduler.listenJobLogs(jobId, appenderProvider);
        } catch (SchedulerException e) {
            Activator.log(IStatus.ERROR, "- Scheduler Proxy: Error on listen log", e);
            displayError(e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean pauseJob(JobId jobId) {
        try {
            return scheduler.pauseJob(jobId);
        } catch (SchedulerException e) {
            Activator.log(IStatus.ERROR, "- Scheduler Proxy: Error on pause", e);
            displayError(e.getMessage());
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean resumeJob(JobId jobId) {
        try {
            return scheduler.resumeJob(jobId);
        } catch (SchedulerException e) {
            Activator.log(IStatus.ERROR, "- Scheduler Proxy: Error on resume action", e);
            displayError(e.getMessage());
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public JobId submit(Job job) throws NotConnectedException, PermissionException,
            SubmissionClosedException, JobCreationException {
        return scheduler.submit(job);
    }

    /**
     * {@inheritDoc}
     */
    public void removeJob(JobId jobId) {
        try {
            scheduler.removeJob(jobId);
        } catch (SchedulerException e) {
            Activator.log(IStatus.ERROR, "- Scheduler Proxy: Error on remove job action ", e);
            displayError(e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean kill() {
        if (pinger != null) {
            pinger.interrupt();
        }
        try {
            return scheduler.kill();
        } catch (SchedulerException e) {
            Activator.log(IStatus.ERROR, "- Scheduler Proxy: Error on kill action ", e);
            displayError(e.getMessage());
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean pause() {
        try {
            return scheduler.pause();
        } catch (SchedulerException e) {
            Activator.log(IStatus.ERROR, "- Scheduler Proxy: Error on pause action ", e);
            displayError(e.getMessage());
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean freeze() {
        try {
            return scheduler.freeze();
        } catch (SchedulerException e) {
            Activator.log(IStatus.ERROR, "- Scheduler Proxy: Error on freeze action ", e);
            displayError(e.getMessage());
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean resume() {
        try {
            return scheduler.resume();
        } catch (SchedulerException e) {
            Activator.log(IStatus.ERROR, "- Scheduler Proxy: Error on resume action ", e);
            displayError(e.getMessage());
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean shutdown() {
        if (pinger != null) {
            pinger.interrupt();
        }
        try {
            return scheduler.shutdown();
        } catch (SchedulerException e) {
            Activator.log(IStatus.ERROR, "- Scheduler Proxy: Error on shut down action ", e);
            displayError(e.getMessage());
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean start() {
        try {
            return scheduler.start();
        } catch (SchedulerException e) {
            Activator.log(IStatus.ERROR, "- Scheduler Proxy: Error on start action", e);
            displayError(e.getMessage());
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean stop() {
        try {
            return scheduler.stop();
        } catch (SchedulerException e) {
            Activator.log(IStatus.ERROR, "- Scheduler Proxy: Error on stop action ", e);
            displayError(e.getMessage());
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public void changeJobPriority(JobId jobId, JobPriority priority) {
        try {
            scheduler.changeJobPriority(jobId, priority);
        } catch (SchedulerException e) {
            Activator.log(IStatus.ERROR, "- Scheduler Proxy: Error on change priority action ", e);
            displayError(e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean changePolicy(Class<? extends Policy> newPolicyFile) {
        try {
            return scheduler.changePolicy(newPolicyFile);
        } catch (SchedulerException e) {
            Activator.log(IStatus.ERROR, "- Scheduler Proxy: Error on change Policy action", e);
            displayError(e.getMessage());
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean linkResourceManager(String rmURL) {
        try {
            return scheduler.linkResourceManager(rmURL);
        } catch (SchedulerException e) {
            Activator.log(IStatus.ERROR, "- Scheduler Proxy: Error on link Resource Manager action", e);
            displayError(e.getMessage());
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isConnected() {
        return scheduler.isConnected();
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

            if (scheduler == null || !scheduler.isConnected()) { // If escape key was pressed during the init of the scheduler
                //new authentication does not care about admin behavior or not
                //but RCP kept the admin/user behavior (in the way it hides button or not)
                scheduler = sai.login(creds);
            }

            sendConnectionCreatedEvent(dialogResult.getUrl(), userName, dialogResult.getPassword());

            startPinger();
            connected = true;
            ControllerView.getInstance().connectedEvent(logAsAdmin);

            JMXActionsManager.getInstance().initJMXClient(sai,
                    new Object[] { dialogResult.getLogin(), creds });
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

        class Pinger extends Thread {
            private SchedulerProxy stubOnSchedulerProxy;

            public Pinger(SchedulerProxy stubOnSchedulerProxy) {
                this.stubOnSchedulerProxy = stubOnSchedulerProxy;
            }

            @Override
            public void run() {
                while (!isInterrupted()) {
                    try {
                        Thread.sleep(SCHEDULER_SERVER_PING_FREQUENCY);
                        //try to ping Scheduler server
                        try {
                            boolean ping = this.stubOnSchedulerProxy.isConnected();
                            if (ping) {
                                //if OK continue
                                continue;
                            } else {
                                throw new Throwable();
                            }
                        } catch (Throwable t) {
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
        }
        pinger = new Pinger((SchedulerProxy) PAActiveObject.getStubOnThis());
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

    public Scheduler getScheduler() {
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
     * {@inheritDoc}
     */
    public JobResult getJobResult(String jobId) throws NotConnectedException, PermissionException,
            UnknownJobException {
        return scheduler.getJobResult(jobId);
    }

    /**
     * {@inheritDoc}
     */
    public TaskResult getTaskResult(String jobId, String taskName) throws NotConnectedException,
            UnknownJobException, UnknownTaskException, PermissionException {
        return scheduler.getTaskResult(jobId, taskName);
    }

    /**
     * {@inheritDoc}
     */
    public void changeJobPriority(String jobId, JobPriority priority) throws NotConnectedException,
            UnknownJobException, PermissionException, JobAlreadyFinishedException {
        scheduler.changeJobPriority(jobId, priority);
    }

    /**
     * {@inheritDoc}
     */
    public boolean killJob(String jobId) throws NotConnectedException, UnknownJobException,
            PermissionException {
        return scheduler.killJob(jobId);
    }

    /**
     * {@inheritDoc}
     */
    public boolean pauseJob(String jobId) throws NotConnectedException, UnknownJobException,
            PermissionException {
        return scheduler.pauseJob(jobId);
    }

    /**
     * {@inheritDoc}
     */
    public void removeJob(String jobId) throws NotConnectedException, UnknownJobException,
            PermissionException {
        scheduler.removeJob(jobId);
    }

    /**
     * {@inheritDoc}
     */
    public boolean resumeJob(String jobId) throws NotConnectedException, UnknownJobException,
            PermissionException {
        return scheduler.resumeJob(jobId);
    }

    /**
     * {@inheritDoc}
     */
    public void listenJobLogs(String jobId, AppenderProvider appender) throws NotConnectedException,
            UnknownJobException, PermissionException {
        scheduler.listenJobLogs(jobId, appender);
    }

    /**
     * {@inheritDoc}
     */
    public SchedulerStatus getStatus() throws NotConnectedException, PermissionException {
        try {
            return scheduler.getStatus();
        } catch (SchedulerException e) {
            Activator.log(IStatus.ERROR, "- Scheduler Proxy: Error while getting status", e);
            e.printStackTrace();
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public JobState getJobState(String jobId) throws NotConnectedException, UnknownJobException,
            PermissionException {
        return scheduler.getJobState(jobId);
    }

    /**
     * {@inheritDoc}
     */
    public JobState getJobState(JobId jobId) throws NotConnectedException, UnknownJobException,
            PermissionException {
        return scheduler.getJobState(jobId);
    }

    /**
     * {@inheritDoc}
     */
    public SchedulerState getState() throws NotConnectedException, PermissionException {
        return scheduler.getState();
    }

}
