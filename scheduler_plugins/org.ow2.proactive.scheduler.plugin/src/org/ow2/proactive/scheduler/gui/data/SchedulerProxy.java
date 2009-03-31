/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 */
package org.ow2.proactive.scheduler.gui.data;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Observer;

import javax.security.auth.login.LoginException;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.ow2.proactive.scheduler.common.AdminSchedulerInterface;
import org.ow2.proactive.scheduler.common.SchedulerAuthenticationInterface;
import org.ow2.proactive.scheduler.common.SchedulerConnection;
import org.ow2.proactive.scheduler.common.SchedulerEvent;
import org.ow2.proactive.scheduler.common.SchedulerEventListener;
import org.ow2.proactive.scheduler.common.SchedulerState;
import org.ow2.proactive.scheduler.common.UserSchedulerInterface;
import org.ow2.proactive.scheduler.common.exception.SchedulerException;
import org.ow2.proactive.scheduler.common.job.Job;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobPriority;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.policy.Policy;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.gui.dialog.SelectSchedulerDialogResult;
import org.ow2.proactive.scheduler.gui.listeners.SchedulerConnectionListener;


/**
 *
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
public class SchedulerProxy implements AdminSchedulerInterface {
    public static final int CONNECTED = 0;
    public static final int LOGIN_OR_PASSWORD_WRONG = 1;
    public static final int COULD_NOT_CONNECT_SCHEDULER = 2;
    public static final int CONNECTION_REFUSED = 3;
    private static SchedulerProxy instance = null;
    private UserSchedulerInterface scheduler = null;
    private String userName = null;
    private Boolean logAsAdmin = false;

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
    /**
     * @see org.ow2.proactive.scheduler.common.UserSchedulerInterface#addSchedulerEventListener(org.ow2.proactive.scheduler.common.SchedulerEventListener, boolean, org.ow2.proactive.scheduler.common.SchedulerEvent[])
     */
    public SchedulerState addSchedulerEventListener(SchedulerEventListener listener, boolean myEventsOnly,
            SchedulerEvent... events) {
        try {
            return (SchedulerState) scheduler.addSchedulerEventListener(listener, myEventsOnly, events);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @see org.ow2.proactive.scheduler.common.UserSchedulerInterface#removeSchedulerEventListener()
     */
    public void removeSchedulerEventListener() throws SchedulerException {
        //not used for the GUI
    }

    /**
     * @see org.ow2.proactive.scheduler.common.UserSchedulerInterface#disconnect()
     */
    public void disconnect() {
        try {
            if (scheduler != null) {
                scheduler.disconnect();
                sendConnectionLostEvent();

            }
        } catch (SchedulerException e) {
            // Nothing to do
            // e.printStackTrace();
        }
    }

    /**
     * @see org.ow2.proactive.scheduler.common.UserSchedulerInterface_#getJobResult(org.ow2.proactive.scheduler.common.job.JobId)
     */
    public JobResult getJobResult(JobId jobId) {
        try {
            return scheduler.getJobResult(jobId);
        } catch (SchedulerException e) {
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
            e.printStackTrace();
        }
        return new BooleanWrapper(false);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.UserSchedulerInterface_#listenLog(org.ow2.proactive.scheduler.common.job.JobId, java.lang.String, int)
     */
    public void listenLog(JobId jobId, String hostname, int port) {
        try {
            scheduler.listenLog(jobId, hostname, port);
        } catch (SchedulerException e) {
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
            e.printStackTrace();
        }
    }

    /**
     * @see org.ow2.proactive.scheduler.common.AdminMethodsInterface#kill()
     */
    public BooleanWrapper kill() {
        try {
            return ((AdminSchedulerInterface) scheduler).kill();
        } catch (SchedulerException e) {
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
            e.printStackTrace();
        }
        return new BooleanWrapper(false);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.AdminMethodsInterface#shutdown()
     */
    public BooleanWrapper shutdown() {
        try {
            return ((AdminSchedulerInterface) scheduler).shutdown();
        } catch (SchedulerException e) {
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
            e.printStackTrace();
        }
    }

    /**
     * @see org.ow2.proactive.scheduler.common.AdminMethodsInterface#changePolicy(java.lang.Class)
     */
    public BooleanWrapper changePolicy(Class<? extends Policy> newPolicyFile) throws SchedulerException {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @see org.ow2.proactive.scheduler.common.AdminMethodsInterface#linkResourceManager(java.lang.String)
     */
    public BooleanWrapper linkResourceManager(String rmURL) throws SchedulerException {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @see org.ow2.proactive.scheduler.common.UserSchedulerInterface#isConnected()
     */
    public BooleanWrapper isConnected() {
        // TODO Auto-generated method stub
        return null;
    }

    // -------------------------------------------------------------------- //
    // ------------------------------ public ------------------------------ //
    // -------------------------------------------------------------------- //
    public int connectToScheduler(SelectSchedulerDialogResult dialogResult) {
        try {
            userName = dialogResult.getLogin();
            logAsAdmin = dialogResult.isLogAsAdmin();
            SchedulerAuthenticationInterface sai = SchedulerConnection.join(dialogResult.getUrl());
            if (logAsAdmin) {
                scheduler = sai.logAsAdmin(userName, dialogResult.getPassword());
            } else {
                scheduler = sai.logAsUser(userName, dialogResult.getPassword());
            }
            sendConnectionCreatedEvent(dialogResult.getUrl(), userName, dialogResult.getPassword());
            return CONNECTED;
        } catch (SchedulerException e) {
            e.printStackTrace();
            userName = null;
            logAsAdmin = false;
            return COULD_NOT_CONNECT_SCHEDULER;
        } catch (LoginException e) {
            // exception is handled by the GUI
            userName = null;
            logAsAdmin = false;
            return LOGIN_OR_PASSWORD_WRONG;
        } catch (Throwable t) {
            t.printStackTrace();
            userName = null;
            logAsAdmin = false;
            return CONNECTION_REFUSED;
        }
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
                t.printStackTrace();
            }
        }
        return instance;
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

    public JobResult getJobResult(String arg0) throws SchedulerException {
        // TODO Auto-generated method stub
        return null;
    }

    public TaskResult getTaskResult(String arg0, String arg1) throws SchedulerException {
        // TODO Auto-generated method stub
        return null;
    }

    public void changePriority(String arg0, JobPriority arg1) throws SchedulerException {
        // TODO Auto-generated method stub

    }

    public BooleanWrapper kill(String arg0) throws SchedulerException {
        // TODO Auto-generated method stub
        return null;
    }

    public BooleanWrapper pause(String arg0) throws SchedulerException {
        // TODO Auto-generated method stub
        return null;
    }

    public void remove(String arg0) throws SchedulerException {
        // TODO Auto-generated method stub

    }

    public BooleanWrapper resume(String arg0) throws SchedulerException {
        // TODO Auto-generated method stub
        return null;
    }

}
