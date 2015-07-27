/*
 *  *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2015 INRIA/University of
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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 *  * $$ACTIVEEON_INITIAL_DEV$$
 */
package functionaltests.utils;

import java.security.KeyException;

import javax.security.auth.login.LoginException;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.NodeException;
import org.ow2.proactive.authentication.crypto.CredData;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.SchedulerAuthenticationInterface;
import org.ow2.proactive.scheduler.common.SchedulerState;
import org.ow2.proactive.scheduler.common.exception.AlreadyConnectedException;
import org.ow2.proactive.scheduler.common.exception.NotConnectedException;
import org.ow2.proactive.scheduler.common.exception.PermissionException;

import functionaltests.monitor.MonitorEventReceiver;
import functionaltests.monitor.SchedulerMonitorsHandler;


public class SchedulerTestUser {

    private String connectedUserName = null;
    private String connectedUserPassword = null;

    private Scheduler scheduler;

    private SchedulerMonitorsHandler monitorsHandler;
    private MonitorEventReceiver eventReceiver;

    public SchedulerTestUser(TestUsers user) {
        this.connectedUserName = user.username;
        this.connectedUserPassword = user.password;
    }

    public boolean is(TestUsers user) {
        return user.username.equals(connectedUserName);
    }

    public Scheduler connect(SchedulerAuthenticationInterface schedulerAuthentication) throws LoginException,
            KeyException, ActiveObjectCreationException, NodeException, AlreadyConnectedException,
            NotConnectedException, PermissionException {
        Credentials connectedUserCreds = Credentials.createCredentials(
                new CredData(CredData.parseLogin(connectedUserName), CredData.parseDomain(connectedUserName),
                    connectedUserPassword), schedulerAuthentication.getPublicKey());
        scheduler = schedulerAuthentication.login(connectedUserCreds);

        monitorsHandler = new SchedulerMonitorsHandler();
        if (eventReceiver == null) {
            /** create event receiver then turnActive to avoid deepCopy of MonitorsHandler object
             * 	(shared instance between event receiver and static helpers).
             */
            MonitorEventReceiver passiveEventReceiver = new MonitorEventReceiver(monitorsHandler);
            eventReceiver = PAActiveObject.turnActive(passiveEventReceiver);

        }
        SchedulerState state = scheduler.addEventListener(eventReceiver, true, true);
        monitorsHandler.init(state);

        return scheduler;
    }

    public boolean isConnected() {
        return scheduler != null;
    }

    public void disconnect() throws NotConnectedException, PermissionException {
        try {
            if (scheduler != null && scheduler.isConnected()) {
                scheduler.removeEventListener();
                scheduler.disconnect();
            }
        } catch (Throwable schedulerKilled) {
        }

        scheduler = null;
        eventReceiver = null;
        monitorsHandler = null;
    }

    public Scheduler getScheduler() {
        return scheduler;
    }

    public SchedulerMonitorsHandler getMonitorsHandler() {
        return monitorsHandler;
    }

    public MonitorEventReceiver getEventReceiver() {
        return eventReceiver;
    }
}
