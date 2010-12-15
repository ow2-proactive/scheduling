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

import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.api.PARemoteObject;
import org.objectweb.proactive.core.ProActiveException;
import org.ow2.proactive.scheduler.common.NotificationData;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.SchedulerEvent;
import org.ow2.proactive.scheduler.common.SchedulerEventListener;
import org.ow2.proactive.scheduler.common.SchedulerState;
import org.ow2.proactive.scheduler.common.UniversalSchedulerListener;
import org.ow2.proactive.scheduler.common.exception.NotConnectedException;
import org.ow2.proactive.scheduler.common.exception.PermissionException;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.UserIdentification;
import org.ow2.proactive.scheduler.common.task.TaskInfo;


/**
 * When using the caching scheduler proxy user interface, this class
 * acts a listener for all the events sent by the scheduler and updates
 * the schedulerState according to the received events.
 *
 * This implementation uses a remote object as scheduler listener.
 * This implies that updates on the scheduler state can happen at any time,
 * even when the scheduler state is serialized leading to ConcurrentModificationException.
 *
 * If unsure, try using @see org.ow2.proactive.scheduler.common.util.CachingSchedulerProxyUserInterface
 *
 */
public class CachingSchedulerEventListener implements SchedulerEventListener {

    protected SchedulerState schedulerState = null;

    public CachingSchedulerEventListener(Scheduler scheduler) throws ProActiveException,
            NotConnectedException, PermissionException {
        UniversalSchedulerListener usl = new UniversalSchedulerListener(this);
        usl = PARemoteObject.turnRemote(usl);
        schedulerState = scheduler.addEventListener(usl, false, true);
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

    public SchedulerState getSchedulerState() {
        return schedulerState;
    }

    public void jobSubmittedEvent(JobState job) {
        schedulerState.update(job);
    }

}
