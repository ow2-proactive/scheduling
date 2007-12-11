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
package org.objectweb.proactive.extensions.scheduler.gui.data;

import javax.security.auth.login.LoginException;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.extensions.scheduler.common.exception.SchedulerException;
import org.objectweb.proactive.extensions.scheduler.common.job.Job;
import org.objectweb.proactive.extensions.scheduler.common.job.JobId;
import org.objectweb.proactive.extensions.scheduler.common.job.JobPriority;
import org.objectweb.proactive.extensions.scheduler.common.job.JobResult;
import org.objectweb.proactive.extensions.scheduler.common.scheduler.AdminSchedulerInterface;
import org.objectweb.proactive.extensions.scheduler.common.scheduler.SchedulerAuthenticationInterface;
import org.objectweb.proactive.extensions.scheduler.common.scheduler.SchedulerConnection;
import org.objectweb.proactive.extensions.scheduler.common.scheduler.SchedulerEvent;
import org.objectweb.proactive.extensions.scheduler.common.scheduler.SchedulerEventListener;
import org.objectweb.proactive.extensions.scheduler.common.scheduler.SchedulerInitialState;
import org.objectweb.proactive.extensions.scheduler.common.scheduler.Stats;
import org.objectweb.proactive.extensions.scheduler.common.scheduler.UserSchedulerInterface;
import org.objectweb.proactive.extensions.scheduler.common.task.TaskResult;
import org.objectweb.proactive.extensions.scheduler.gui.dialog.SelectSchedulerDialogResult;
import org.objectweb.proactive.extensions.scheduler.job.InternalJob;
import org.objectweb.proactive.extensions.scheduler.policy.PolicyInterface;


/**
 *
 *
 * @author FRADJ Johann
 * @version 1.0, Aug 1, 2007
 * @since ProActive 3.2
 */
public class SchedulerProxy implements AdminSchedulerInterface {
    public static final int CONNECTED = 0;
    public static final int LOGIN_OR_PASSWORD_WRONG = 1;
    public static final int COULD_NOT_CONNECT_SCHEDULER = 2;
    public static final int CONNECTION_REFUSED = 3;
    private static final long serialVersionUID = 3783194235036041589L;
    private static SchedulerProxy instance = null;
    private UserSchedulerInterface scheduler = null;
    private String userName = null;
    private Boolean logAsAdmin = false;

    // -------------------------------------------------------------------- //
    // --------------------------- constructor ---------------------------- //
    // -------------------------------------------------------------------- //
    public SchedulerProxy() {
    }

    // -------------------------------------------------------------------- //
    // ---------------- implements AdminSchedulerInterface ---------------- //
    // -------------------------------------------------------------------- //
    /**
     * @see org.objectweb.proactive.extensions.scheduler.userAPI.UserSchedulerInterface#addSchedulerEventListener(org.objectweb.proactive.extra.scheduler.userAPI.SchedulerEventListener)
     */
    public SchedulerInitialState<InternalJob> addSchedulerEventListener(
        SchedulerEventListener<?extends Job> listener, SchedulerEvent... events) {
        try {
            return (SchedulerInitialState<InternalJob>) scheduler.addSchedulerEventListener(listener,
                events);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @see org.objectweb.proactive.extensions.scheduler.userAPI.UserSchedulerInterface#disconnect()
     */
    public void disconnect() {
        try {
            if (scheduler != null) {
                scheduler.disconnect();
            }
        } catch (SchedulerException e) {
            // Nothing to do
            // e.printStackTrace();
        }
    }

    /**
     * @see org.objectweb.proactive.extensions.scheduler.userAPI.UserSchedulerInterface#getResult(org.objectweb.proactive.extra.scheduler.job.JobId)
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
     * @see org.objectweb.proactive.extensions.scheduler.common.scheduler.UserSchedulerInterface#getTaskResult(org.objectweb.proactive.extra.scheduler.common.job.JobId,
     *      java.lang.String)
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
     * @see org.objectweb.proactive.extensions.scheduler.userAPI.UserSchedulerInterface#getStats()
     */
    public Stats getStats() {
        try {
            return scheduler.getStats();
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @see org.objectweb.proactive.extensions.scheduler.userAPI.UserSchedulerInterface#kill(org.objectweb.proactive.extra.scheduler.job.JobId)
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
     * @see org.objectweb.proactive.extensions.scheduler.userAPI.UserSchedulerInterface#listenLog(org.objectweb.proactive.extra.scheduler.job.JobId,
     *      java.lang.String, int)
     */
    public void listenLog(JobId jobId, String hostname, int port) {
        try {
            scheduler.listenLog(jobId, hostname, port);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    /**
     * @see org.objectweb.proactive.extensions.scheduler.userAPI.UserSchedulerInterface#pause(org.objectweb.proactive.extra.scheduler.job.JobId)
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
     * @see org.objectweb.proactive.extensions.scheduler.userAPI.UserSchedulerInterface#resume(org.objectweb.proactive.extra.scheduler.job.JobId)
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
     * @see org.objectweb.proactive.extensions.scheduler.userAPI.UserSchedulerInterface#submit(org.objectweb.proactive.extra.scheduler.job.Job)
     */
    public JobId submit(Job job) {
        try {
            return scheduler.submit(job);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @see org.objectweb.proactive.extensions.scheduler.core.AdminSchedulerInterface#kill()
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
     * @see org.objectweb.proactive.extensions.scheduler.core.AdminSchedulerInterface#pause()
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
     * @see org.objectweb.proactive.extensions.scheduler.core.AdminSchedulerInterface#pauseImmediate()
     */
    public BooleanWrapper pauseImmediate() {
        try {
            return ((AdminSchedulerInterface) scheduler).pauseImmediate();
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
        return new BooleanWrapper(false);
    }

    /**
     * @see org.objectweb.proactive.extensions.scheduler.core.AdminSchedulerInterface#resume()
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
     * @see org.objectweb.proactive.extensions.scheduler.core.AdminSchedulerInterface#shutdown()
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
     * @see org.objectweb.proactive.extensions.scheduler.core.AdminSchedulerInterface#start()
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
     * @see org.objectweb.proactive.extensions.scheduler.core.AdminSchedulerInterface#stop()
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
     * @see org.objectweb.proactive.extensions.scheduler.userAPI.UserSchedulerInterface#changePriority(org.objectweb.proactive.extra.scheduler.job.JobId,
     *      org.objectweb.proactive.extensions.scheduler.job.JobPriority)
     */
    public void changePriority(JobId jobId, JobPriority priority) {
        try {
            scheduler.changePriority(jobId, priority);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    public BooleanWrapper changePolicy(
        Class<?extends PolicyInterface> newPolicyFile)
        throws SchedulerException {
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
            return CONNECTED;
        } catch (SchedulerException e) {
            userName = null;
            logAsAdmin = false;
            return COULD_NOT_CONNECT_SCHEDULER;
        } catch (LoginException e) {
            userName = null;
            logAsAdmin = false;
            return LOGIN_OR_PASSWORD_WRONG;
        } catch (Exception e) {
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
    public static SchedulerProxy getInstance() {
        if (instance == null) {
            try {
                instance = (SchedulerProxy) PAActiveObject.newActive(SchedulerProxy.class.getName(),
                        null);
            } catch (ActiveObjectCreationException e) {
                e.printStackTrace();
            } catch (NodeException e) {
                e.printStackTrace();
            }
        }
        return instance;
    }

    public static void clearInstance() {
        instance = null;
    }
}
