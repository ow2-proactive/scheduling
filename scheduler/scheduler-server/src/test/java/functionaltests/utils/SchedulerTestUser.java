/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package functionaltests.utils;

import java.security.KeyException;

import javax.security.auth.login.LoginException;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.NodeException;
import org.ow2.proactive.authentication.crypto.CredData;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.SchedulerState;
import org.ow2.proactive.scheduler.common.exception.NotConnectedException;
import org.ow2.proactive.scheduler.common.exception.PermissionException;
import org.ow2.proactive.scheduler.common.exception.SchedulerException;
import org.ow2.proactive.scheduler.common.util.SchedulerProxyUserInterface;

import functionaltests.monitor.MonitorEventReceiver;
import functionaltests.monitor.SchedulerMonitorsHandler;


public class SchedulerTestUser {

    private String connectedUserName = null;

    private String connectedUserPassword = null;

    private byte[] connectedUserKey = null;

    private static SchedulerTestUser instance = null;

    private SchedulerMonitorsHandler monitorsHandler;

    private MonitorEventReceiver eventReceiver;

    private SchedulerProxyUserInterface schedulerProxy;

    public SchedulerTestUser() {

    }

    public static SchedulerTestUser getInstance() {
        if (instance == null) {
            instance = new SchedulerTestUser();
        }
        return instance;
    }

    public boolean is(TestUsers user) {
        return user.username.equals(connectedUserName);
    }

    public boolean is(String username, String password) {
        return username.equals(connectedUserName) && password.equals(connectedUserPassword);
    }

    /**
     * Connects the singleton using the given user
     */
    public boolean connect(String username, String password, byte[] key, String schedulerUrl)
            throws LoginException, KeyException, ActiveObjectCreationException, NodeException, SchedulerException {
        this.connectedUserName = username;
        this.connectedUserPassword = password;
        this.connectedUserKey = key;

        disconnectFromScheduler();

        if (schedulerProxy == null) {
            schedulerProxy = PAActiveObject.newActive(SchedulerProxyUserInterface.class, new Object[0]);
        }

        SchedulerTHelper.log("Connecting user " + connectedUserName + " to the Scheduler at url " + schedulerUrl);
        CredData connectedUserCreds = new CredData(CredData.parseLogin(connectedUserName),
                                                   CredData.parseDomain(connectedUserName),
                                                   connectedUserPassword,
                                                   connectedUserKey);

        schedulerProxy.init(schedulerUrl, connectedUserCreds);

        monitorsHandler = new SchedulerMonitorsHandler();

        if (eventReceiver != null) {
            PAActiveObject.terminateActiveObject(eventReceiver, true);
        }

        /** create event receiver then turnActive to avoid deepCopy of MonitorsHandler object
         * 	(shared instance between event receiver and static helpers).
         */
        MonitorEventReceiver passiveEventReceiver = new MonitorEventReceiver(monitorsHandler);
        eventReceiver = PAActiveObject.turnActive(passiveEventReceiver);

        SchedulerState state = schedulerProxy.addEventListener(eventReceiver, true, true);
        monitorsHandler.init(state);
        return true;
    }

    /**
     * Connects the singleton using the given user
     */
    public boolean connect(TestUsers user, String schedulerUrl)
            throws LoginException, KeyException, ActiveObjectCreationException, NodeException, SchedulerException {
        return connect(user.username, user.password, null, schedulerUrl);
    }

    public void schedulerIsRestarted() {
        RMTHelper.log("Scheduler has been restarted.");
        if (schedulerProxy != null) {
            PAActiveObject.terminateActiveObject(schedulerProxy, true);
            schedulerProxy = null;
        }
        if (eventReceiver != null) {
            PAActiveObject.terminateActiveObject(eventReceiver, true);
            eventReceiver = null;
        }
    }

    public Scheduler getScheduler() {
        return schedulerProxy;
    }

    public boolean isConnected() {
        try {
            return schedulerProxy != null && schedulerProxy.isConnected();
        } catch (Exception e) {
            return false;
        }
    }

    public void disconnectFromScheduler() throws NotConnectedException, PermissionException {
        if (isConnected()) {
            SchedulerTHelper.log("Disconnecting user " + connectedUserName + " from the Scheduler");
            try {
                schedulerProxy.removeEventListener();
            } catch (Exception ignored) {

            }
            if (eventReceiver != null) {
                PAActiveObject.terminateActiveObject(eventReceiver, true);
                eventReceiver = null;
            }
            try {
                schedulerProxy.disconnect();
            } catch (Exception ignored) {

            }
        }
    }

    /**
     * Returns a direct reference to the monitors handler object, the monitor can be used to wait for specific events
     *
     * @return
     */
    public SchedulerMonitorsHandler getMonitorsHandler() {
        return monitorsHandler;
    }

    /**
     * Returns a stub to the event receiver (active object)
     *
     * @return
     */
    public MonitorEventReceiver getEventReceiver() {
        return eventReceiver;
    }

}
