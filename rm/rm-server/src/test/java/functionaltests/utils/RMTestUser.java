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

import functionaltests.monitor.RMMonitorEventReceiver;
import functionaltests.monitor.RMMonitorsHandler;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.NodeException;
import org.ow2.proactive.authentication.crypto.CredData;
import org.ow2.proactive.resourcemanager.exception.RMException;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;

import javax.security.auth.login.LoginException;
import java.security.KeyException;


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

    public void connect(TestUsers user, String rmUrl) throws RMException, KeyException, LoginException, ActiveObjectCreationException, NodeException {
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

        CredData credData = new CredData(CredData.parseLogin(connectedUserName), CredData.parseDomain(connectedUserName),
                connectedUserPassword);

        rmProxy.init(rmUrl, credData);
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
                rmProxy.disconnect().getBooleanValue();
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
