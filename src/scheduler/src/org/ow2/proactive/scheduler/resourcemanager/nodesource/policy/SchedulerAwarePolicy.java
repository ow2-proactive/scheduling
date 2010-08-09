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
package org.ow2.proactive.scheduler.resourcemanager.nodesource.policy;

import java.io.File;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.resourcemanager.authentication.Client;
import org.ow2.proactive.resourcemanager.nodesource.common.Configurable;
import org.ow2.proactive.resourcemanager.nodesource.policy.NodeSourcePolicy;
import org.ow2.proactive.resourcemanager.utils.RMLoggers;
import org.ow2.proactive.scheduler.common.NotificationData;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.SchedulerAuthenticationInterface;
import org.ow2.proactive.scheduler.common.SchedulerConnection;
import org.ow2.proactive.scheduler.common.SchedulerEvent;
import org.ow2.proactive.scheduler.common.SchedulerEventListener;
import org.ow2.proactive.scheduler.common.SchedulerState;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.UserIdentification;
import org.ow2.proactive.scheduler.common.task.TaskInfo;


public abstract class SchedulerAwarePolicy extends NodeSourcePolicy implements SchedulerEventListener {

    /**  */
    private static final long serialVersionUID = 21L;

    protected static Logger logger = ProActiveLogger.getLogger(RMLoggers.POLICY);

    @Configurable
    protected String schedulerUrl = "";
    @Configurable(credential = true)
    protected File schedulerCredentialsPath;

    protected SchedulerState state;
    protected Scheduler scheduler;

    @Override
    public BooleanWrapper configure(Object... params) {
        super.configure(params);
        SchedulerAuthenticationInterface authentication;

        if (params[3] == null) {
            throw new IllegalArgumentException("Credentials must be specified");
        }

        try {
            authentication = SchedulerConnection.join(params[2].toString());
            Credentials creds = Credentials.getCredentialsBase64((byte[]) params[3]);
            scheduler = authentication.login(creds);
        } catch (Throwable t) {
            throw new IllegalArgumentException(t);
        }

        return new BooleanWrapper(true);
    }

    @Override
    public BooleanWrapper activate() {
        SchedulerAuthenticationInterface authentication;
        try {
            state = scheduler.addEventListener(getSchedulerListener(), false, true, getEventsList());
        } catch (Exception e) {
            logger.error("", e);
            throw new RuntimeException(e.getMessage(), e);
        }
        return new BooleanWrapper(true);
    }

    @Override
    public void shutdown(Client initiator) {
        try {
            scheduler.removeEventListener();
            scheduler.disconnect();
        } catch (Exception e) {
            logger.error("", e);
        }
        super.shutdown(initiator);
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
