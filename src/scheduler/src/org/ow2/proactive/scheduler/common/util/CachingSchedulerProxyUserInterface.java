package org.ow2.proactive.scheduler.common.util;

import java.security.KeyException;
import java.security.PublicKey;

import javax.security.auth.login.LoginException;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAFuture;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.scheduler.common.NotificationData;
import org.ow2.proactive.scheduler.common.SchedulerAuthenticationInterface;
import org.ow2.proactive.scheduler.common.SchedulerConnection;
import org.ow2.proactive.scheduler.common.SchedulerEvent;
import org.ow2.proactive.scheduler.common.SchedulerEventListener;
import org.ow2.proactive.scheduler.common.SchedulerState;
import org.ow2.proactive.scheduler.common.UniversalSchedulerListener;
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

    public boolean init(String url, String user, String pwd) throws SchedulerException, LoginException {
        SchedulerAuthenticationInterface auth = SchedulerConnection.join(url);
        PublicKey pubKey = auth.getPublicKey();

        try {
            Credentials cred = Credentials.createCredentials(user, pwd, pubKey);
            this.uischeduler = auth.login(cred);
        } catch (KeyException e) {
            throw new InternalSchedulerException(e);
        }
        CachingSchedulerProxyUserInterface ao = (CachingSchedulerProxyUserInterface) PAActiveObject
                .getStubOnThis();
        schedulerState = this.uischeduler.addEventListener(ao, false, true);
        schedulerState = PAFuture.getFutureValue(schedulerState);
        return true;
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
