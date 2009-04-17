/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
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
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.resourcemanager.nodesource.policy;

import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.ow2.proactive.resourcemanager.nodesource.policy.Configurable;
import org.ow2.proactive.resourcemanager.nodesource.policy.NodeSourcePolicy;
import org.ow2.proactive.scheduler.common.NotificationData;
import org.ow2.proactive.scheduler.common.SchedulerAuthenticationInterface;
import org.ow2.proactive.scheduler.common.SchedulerConnection;
import org.ow2.proactive.scheduler.common.SchedulerEvent;
import org.ow2.proactive.scheduler.common.SchedulerEventListener;
import org.ow2.proactive.scheduler.common.SchedulerState;
import org.ow2.proactive.scheduler.common.UserSchedulerInterface;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.UserIdentification;
import org.ow2.proactive.scheduler.common.task.TaskInfo;


public abstract class SchedulerAwarePolicy extends NodeSourcePolicy implements SchedulerEventListener {

    /**
     *
     */
    private static final long serialVersionUID = 10L;
    @Configurable
    protected String url = "";
    @Configurable
    protected String userName = "";
    @Configurable(password = true)
    protected String password = "";
    @Configurable
    protected boolean preemptive = true;

    protected SchedulerState state;
    protected UserSchedulerInterface userInterface;

    public BooleanWrapper activate() {
        SchedulerAuthenticationInterface authentication;
        try {
            authentication = SchedulerConnection.join(url);
            userInterface = authentication.logAsUser(userName, password);
            state = userInterface.addSchedulerEventListener(getSchedulerListener(), true, getEventsList());
        } catch (Exception e) {
            e.printStackTrace();
            return new BooleanWrapper(false);
        }
        return new BooleanWrapper(true);
    }

    public BooleanWrapper disactivate() {
        try {
            userInterface.removeSchedulerEventListener();
            userInterface.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
            return new BooleanWrapper(false);
        }
        return new BooleanWrapper(true);
    }

    public void jobStateUpdatedEvent(NotificationData<JobInfo> notification) {
    }

    public void jobSubmittedEvent(JobState job) {
    }

    public void schedulerStateUpdatedEvent(SchedulerEvent eventType) {
    }

    public void taskStateUpdatedEvent(NotificationData<TaskInfo> notification) {
    }

    public void usersUpdatedEvent(NotificationData<UserIdentification> notification) {
    }

    protected abstract SchedulerEvent[] getEventsList();

    protected abstract SchedulerEventListener getSchedulerListener();
}
