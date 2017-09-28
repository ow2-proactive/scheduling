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
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.node.NodeException;
import org.ow2.proactive.authentication.crypto.CredData;
import org.ow2.proactive.resourcemanager.exception.RMException;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;

import functionaltests.monitor.RMMonitorEventReceiver;
import functionaltests.monitor.RMMonitorsHandler;


public class RMTestUser {

    private static RMTestUser instance = null;

    private String connectedUserName = null;

    private String connectedUserPassword = null;

    private RMMonitorsHandler monitorsHandler;

    private String rmUrl;

    private RMMonitorEventReceiver rmProxy;

    public RMTestUser() {

    }

    public static RMTestUser getInstance() {
        if (instance == null) {
            instance = new RMTestUser();
        }
        return instance;
    }

    public boolean is(TestUsers user) {
        return user.username.equals(connectedUserName);
    }

    public void connect(TestUsers user, String rmUrl)
            throws RMException, KeyException, LoginException, ActiveObjectCreationException, NodeException {
        this.connectedUserName = user.username;
        this.connectedUserPassword = user.password;
        this.rmUrl = rmUrl;

        disconnectFromRM();

        if (rmProxy == null) {
            monitorsHandler = new RMMonitorsHandler();
            RMMonitorEventReceiver passiveEventReceiver = new RMMonitorEventReceiver(monitorsHandler);
            rmProxy = PAActiveObject.turnActive(passiveEventReceiver);
            RMTHelper.log("RM Proxy initialized : " + PAActiveObject.getUrl(rmProxy));
        }

        RMTHelper.log("Connecting user " + connectedUserName + " to the Resource Manager at " + rmUrl);

        CredData credData = new CredData(CredData.parseLogin(connectedUserName),
                                         CredData.parseDomain(connectedUserName),
                                         connectedUserPassword);

        // this is to prevent starting working with the rmProxy on the calling
        // thread whereas it has not finished to be initialized
        PAFuture.waitFor(rmProxy.init(rmUrl, credData));
    }

    public ResourceManager getResourceManager() {
        return rmProxy;
    }

    public boolean isConnected() {
        return rmProxy != null;
    }

    public void disconnectFromRM() {
        if (isConnected()) {
            RMTHelper.log("Disconnecting user " + connectedUserName + " from the Resource Manager");
            killRMProxy();
        }
    }

    private void killRMProxy() {
        if (rmProxy != null) {
            RMTHelper.log("Kill RM Proxy");
            try {
                PAFuture.waitFor(rmProxy.disconnect());
            } catch (Exception ignored) {
            }
            PAActiveObject.terminateActiveObject(rmProxy, true);
            rmProxy = null;
        }
    }

    public RMMonitorsHandler getMonitorsHandler() {
        return monitorsHandler;
    }

    public RMMonitorEventReceiver getEventReceiver() {
        return rmProxy;
    }
}
