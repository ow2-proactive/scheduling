/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import javax.security.auth.login.LoginException;

import org.objectweb.proactive.annotation.ImmediateService;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.extensions.annotation.ActiveObject;
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
 *  this implementation alsp provides a simple version control system
 *  of the scheduler state. Each time
 *  the scheduler state is modified, the revision version is increased. This
 *  allows us knowing if the scheduler state has been modified within having
 *  to get it from the active object as results are deep-copied.
 */

@ActiveObject
public class CachingSchedulerProxyUserInterface extends SchedulerProxyUserInterface implements
        SchedulerEventListener {
    /**  */
    private static final long serialVersionUID = 31L;

    protected boolean isCachingEnabled = false;

    /**
     * keep the
     */
    private AtomicLong schedulerStateRevision = new AtomicLong(0);

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
            Credentials cred = Credentials.createCredentials(new CredData(CredData.parseLogin(user), CredData
                    .parseDomain(user), pwd), pubKey);
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
        increaseVersion();
    }

    public void jobSubmittedEvent(NotificationData<JobState> job) {
        schedulerState.update(job);
        increaseVersion();

    }

    public void jobStateUpdatedEvent(NotificationData<JobInfo> notification) {
        schedulerState.update(notification);
        increaseVersion();
    }

    public void taskStateUpdatedEvent(NotificationData<TaskInfo> notification) {
        schedulerState.update(notification);
        increaseVersion();

    }

    public void usersUpdatedEvent(NotificationData<UserIdentification> notification) {
        schedulerState.update(notification);
        increaseVersion();
    }

    public void jobSubmittedEvent(JobState job) {
        schedulerState.update(job);
        increaseVersion();
    }

    /**
     * increase the revision number
     */
    private void increaseVersion() {
        if (schedulerStateRevision.longValue() == Long.MAX_VALUE) {
            schedulerStateRevision.set(0L);
        } else {
            schedulerStateRevision.incrementAndGet();
        }
    }

    /**
     * When Long.MAX_VALUE is reached, the counter is reseted to 0L and start
     * increasing again. Hopefully this does not occur often.
     * @return returns the revision number of the last scheduler state.
     */
    @ImmediateService
    public long getSchedulerStateRevision() {
        return schedulerStateRevision.longValue();
    }

    /**
     * return the revision version and the scheduler state at once. Only one entry
     * in the map.
     * @return a map containing only one entry the reversion as key and the
     * scheduler state as content
     */
    public Map<AtomicLong, SchedulerState> getRevisionVersionAndSchedulerState() {
        HashMap<AtomicLong, SchedulerState> s = new HashMap<AtomicLong, SchedulerState>();
        s.put(new AtomicLong(schedulerStateRevision.longValue()), schedulerState);
        return s;
    }

}
