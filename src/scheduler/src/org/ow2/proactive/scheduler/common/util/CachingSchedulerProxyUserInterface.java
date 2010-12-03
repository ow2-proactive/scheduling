/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2010 INRIA/University of
 *              Nice-Sophia Antipolis/ActiveEon
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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.common.util;

import java.security.KeyException;
import java.security.PublicKey;

import javax.security.auth.login.LoginException;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAFuture;
import org.ow2.proactive.authentication.crypto.CredData;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.scheduler.common.NotificationData;
import org.ow2.proactive.scheduler.common.SchedulerAuthenticationInterface;
import org.ow2.proactive.scheduler.common.SchedulerConnection;
import org.ow2.proactive.scheduler.common.SchedulerEvent;
import org.ow2.proactive.scheduler.common.SchedulerEventListener;
import org.ow2.proactive.scheduler.common.SchedulerState;
import org.ow2.proactive.scheduler.common.exception.InternalSchedulerException;
import org.ow2.proactive.scheduler.common.exception.NotConnectedException;
import org.ow2.proactive.scheduler.common.exception.PermissionException;
import org.ow2.proactive.scheduler.common.exception.SchedulerException;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.UserIdentification;
import org.ow2.proactive.scheduler.common.task.TaskInfo;


/**
 *  A scheduler proxy that is also a listener to scheduler events.
 *  It caches the scheduler state at creation time and updates it
 *  through events received from the scheduler.
 *
 */
public class CachingSchedulerProxyUserInterface extends SchedulerProxyUserInterface implements
        SchedulerEventListener {
    protected boolean isCachingEnabled = false;

    protected SchedulerState schedulerState = null;

    public SchedulerState getState() throws NotConnectedException, PermissionException {
        return schedulerState;
    }

    /**
     * initialize the connection the scheduler. 
     * Must be called only once
     * @param url the scheduler's url 
     * @param credentials the credential to be passed to the scheduler
     * @throws SchedulerException thrown if the scheduler is not available
     * @throws LoginException thrown if the credential is invalid
     */
    public void init(String url, Credentials credentials) throws SchedulerException, LoginException {
        SchedulerAuthenticationInterface auth = SchedulerConnection.join(url);
        this.uischeduler = auth.login(credentials);
        CachingSchedulerProxyUserInterface ao = (CachingSchedulerProxyUserInterface) PAActiveObject
                .getStubOnThis();
        schedulerState = this.uischeduler.addEventListener(ao, false, true);
        schedulerState = PAFuture.getFutureValue(schedulerState);
    }

    /**
     * initialize the connection the scheduler. 
     * Must be called only once.
     * Create the corresponding credential object before sending it
     * to the scheduler.
     * @param url the scheduler's url 
     * @param user the username to use
     * @param pwd the password to use
     * @throws SchedulerException thrown if the scheduler is not available
     * @throws LoginException if the couple username/password is invalid
     */
    public void init(String url, String user, String pwd) throws SchedulerException, LoginException {
        SchedulerAuthenticationInterface auth = SchedulerConnection.join(url);
        PublicKey pubKey = auth.getPublicKey();

        try {
            Credentials cred = Credentials.createCredentials(new CredData(user, pwd), pubKey);
            this.uischeduler = auth.login(cred);
        } catch (KeyException e) {
            throw new InternalSchedulerException(e);
        }
        CachingSchedulerProxyUserInterface ao = (CachingSchedulerProxyUserInterface) PAActiveObject
                .getStubOnThis();
        schedulerState = this.uischeduler.addEventListener(ao, false, true);
        schedulerState = PAFuture.getFutureValue(schedulerState);
    }

    public void schedulerStateUpdatedEvent(SchedulerEvent eventType) {
        schedulerState.update(eventType);
    }

    public void jobSubmittedEvent(NotificationData<JobState> job) {
        schedulerState.update(job);

    }

    public void jobStateUpdatedEvent(NotificationData<JobInfo> notification) {
        schedulerState.update(notification);
    }

    public void taskStateUpdatedEvent(NotificationData<TaskInfo> notification) {
        schedulerState.update(notification);

    }

    public void usersUpdatedEvent(NotificationData<UserIdentification> notification) {
        schedulerState.update(notification);
    }

    public void jobSubmittedEvent(JobState job) {
        schedulerState.update(job);
    }

}
