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
package org.ow2.proactive.scheduler.resourcemanager.nodesource.policy;

import java.io.File;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.resourcemanager.exception.RMException;
import org.ow2.proactive.resourcemanager.nodesource.common.Configurable;
import org.ow2.proactive.resourcemanager.nodesource.policy.NodeSourcePolicy;
import org.ow2.proactive.resourcemanager.utils.RMLoggers;
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

    /**  */
    private static final long serialVersionUID = 200;

    protected static Logger logger = ProActiveLogger.getLogger(RMLoggers.POLICY);

    @Configurable
    protected String url = "";
    @Configurable(credential = true)
    protected File schedulerCredentialsPath;
    @Configurable
    protected boolean preemptive = false;

    protected SchedulerState state;
    protected UserSchedulerInterface userInterface;

    @Override
    public void configure(Object... params) throws RMException {
        SchedulerAuthenticationInterface authentication;

        if (params[1] == null) {
            throw new RMException("Credentials must be specified");
        }

        try {
            authentication = SchedulerConnection.join(params[0].toString());
            Credentials creds = Credentials.getCredentialsBase64((byte[]) params[1]);
            userInterface = authentication.logAsUser(creds);
            preemptive = Boolean.parseBoolean(params[2].toString());
        } catch (Throwable t) {
            throw new RMException(t);
        }
    }

    @Override
    public BooleanWrapper activate() {
        SchedulerAuthenticationInterface authentication;
        try {
            state = userInterface.addEventListener(getSchedulerListener(), false, true, getEventsList());
        } catch (Exception e) {
            logger.error("", e);
            return new BooleanWrapper(false);
        }
        return new BooleanWrapper(true);
    }

    @Override
    public void shutdown() {
        try {
            userInterface.removeEventListener();
            userInterface.disconnect();
        } catch (Exception e) {
            logger.error("", e);
        }
        super.shutdown();
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
