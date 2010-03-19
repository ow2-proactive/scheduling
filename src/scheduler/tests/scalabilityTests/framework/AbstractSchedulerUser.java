/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
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
 * $PROACTIVE_INITIAL_DEV$
 */
package scalabilityTests.framework;

import javax.security.auth.login.LoginException;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.extensions.annotation.ActiveObject;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.scheduler.common.SchedulerAuthenticationInterface;
import org.ow2.proactive.scheduler.common.SchedulerConnection;
import org.ow2.proactive.scheduler.common.SchedulerEvent;
import org.ow2.proactive.scheduler.common.SchedulerEventListener;
import org.ow2.proactive.scheduler.common.SchedulerState;
import org.ow2.proactive.scheduler.common.UserSchedulerInterface;
import org.ow2.proactive.scheduler.common.exception.SchedulerException;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobResult;

import scalabilityTests.framework.listeners.SchedulerListenerExposer;


/**
 *
 * Active Actor which simulates
 * the behaviour of a Scheduler User
 *
 * OBS the parameter is a UserSchedulerInterface;
 * 	this would be the defaultParameter's type, and
 * 	will only be made available after connectToScheduler()
 *  method was called
 *
 * Also, manually setting the parameter of the Action is disabled;
 * 	This is because an UserSchedulerInterface has significance only
 * 	on the host which performed the login to the Scheduler
 *
 * @author fabratu
 *
 */
@ActiveObject
public abstract class AbstractSchedulerUser<V> extends ActiveActor<UserSchedulerInterface, V> {

    // Scheduler info
    private final String schedulerURL;
    private final Credentials userCreds;
    // Scheduler interaction internal state
    protected UserSchedulerInterface scheduler = null;
    // Scheduler listener
    protected SchedulerListenerExposer slExposer = null;

    public AbstractSchedulerUser() {
        this.schedulerURL = null;
        this.userCreds = null;
    }

    public AbstractSchedulerUser(String schedulerURL, Credentials userCreds) {
        super();
        this.schedulerURL = schedulerURL;
        this.userCreds = userCreds;
    }

    public AbstractSchedulerUser(Action<UserSchedulerInterface, V> defaultAction, String schedulerURL,
            Credentials userCreds) {
        super(defaultAction);
        this.schedulerURL = schedulerURL;
        this.userCreds = userCreds;
    }

    @Override
    public void doAction() {
        if (this.scheduler == null)
            throw new IllegalStateException("The user is not connected to the Scheduler yet. "
                + "Consider calling the connectToScheduler() method first");
        super.doAction();
    }

    @Override
    public void doAction(Action<UserSchedulerInterface, V> action) {
        if (this.scheduler == null)
            throw new IllegalStateException("The user is not connected to the Scheduler yet. "
                + "Consider calling the connectToScheduler() method first");
        super.doAction(action);
    }

    public void connectToScheduler() throws SchedulerException, LoginException {
        SchedulerAuthenticationInterface auth = SchedulerConnection.join(this.schedulerURL);
        this.scheduler = auth.logAsUser(this.userCreds);
        this.defaultParameter = this.scheduler;
    }

    public void registerListener(boolean getInitialState, boolean myEventsOnly, String listenerClazzName)
            throws SchedulerException, ProActiveException {
        try {
            if (this.scheduler == null)
                throw new IllegalStateException("The user is not connected to the Scheduler yet. "
                    + "Consider calling the connectToScheduler() method first");
            SchedulerEventListener schedulerListener = createEventListener(listenerClazzName);
            logger.trace("Trying to expose the listener as a remote object");
            slExposer = new SchedulerListenerExposer(schedulerListener);
            SchedulerEventListener schedulerListenerRemoteRef = slExposer.createRemoteReference();
            logger.trace("Trying to register the listener to the Scheduler");
            // for the moment, listens only to the Jobs-related events
            SchedulerState initialState = scheduler.addEventListener(
                    schedulerListenerRemoteRef,
                    myEventsOnly,
                    getInitialState,
                    // job-related events
                    SchedulerEvent.JOB_SUBMITTED, SchedulerEvent.JOB_PENDING_TO_RUNNING,
                    SchedulerEvent.JOB_RUNNING_TO_FINISHED,
                    SchedulerEvent.JOB_PAUSED,
                    SchedulerEvent.JOB_RESUMED,
                    SchedulerEvent.JOB_CHANGE_PRIORITY,
                    // task-related events
                    SchedulerEvent.TASK_PENDING_TO_RUNNING, SchedulerEvent.TASK_RUNNING_TO_FINISHED,
                    SchedulerEvent.TASK_WAITING_FOR_RESTART,
                    SchedulerEvent.USERS_UPDATE,
                    // Scheduler state related events
                    SchedulerEvent.FROZEN, SchedulerEvent.RESUMED, SchedulerEvent.SHUTDOWN,
                    SchedulerEvent.SHUTTING_DOWN, SchedulerEvent.STARTED, SchedulerEvent.STOPPED,
                    SchedulerEvent.KILLED);
            if (getInitialState) {
                logger.info("Initial state of the scheduler is: " + initialState);
            }
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Class " + listenerClazzName +
                " is not available on this SchedulerUser's side", e);
        } catch (InstantiationException e) {
            throw new IllegalArgumentException("Cannot instantiate listener of type " + listenerClazzName, e);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Cannot instantiate listener of type " + listenerClazzName, e);
        }
    }

    protected abstract SchedulerEventListener createEventListener(String listenerClazzName)
            throws ClassNotFoundException, InstantiationException, IllegalAccessException;

    /**
     * This method just calls the Scheduler's getJobResult
     * This is needed here, as the UserSchedulerInterface only accepts
     * requests from the Active Objects that have previously called
     * one of the authentication methods logAs*
     */
    public JobResult getJobResult(JobId jobId) throws SchedulerException {
        return this.scheduler.getJobResult(jobId);
    }

    public void disconnect() throws SchedulerException {
        this.scheduler.disconnect();
    }

    @Override
    public void cleanup() {
        try {
            // first, cleanly destroy the listener remote object
            if (this.slExposer != null)
                this.slExposer.destroyRemoteReference();
            disconnect();
        } catch (SchedulerException e) {
            // we don't care
            logger.debug("Cannot disconnect() this user from the Scheduler, reason", e);
        } catch (ProActiveException e) {
            logger.debug("Cannot unregister the scheduler event listener from the Scheduler, reason:", e);
        }
        super.cleanup();
    }

}
