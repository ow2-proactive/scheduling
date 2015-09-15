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
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.node.NodeException;
import org.ow2.proactive.authentication.crypto.CredData;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.resourcemanager.authentication.RMAuthentication;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;

import functionaltests.monitor.RMMonitorEventReceiver;
import functionaltests.monitor.RMMonitorsHandler;


public class RMTestUser {

    private String connectedUserName = null;
    private String connectedUserPassword = null;

    private ResourceManager resourceManager;

    private RMMonitorsHandler monitorsHandler;
    private RMMonitorEventReceiver eventReceiver;

    public RMTestUser(TestUsers user) {
        this.connectedUserName = user.username;
        this.connectedUserPassword = user.password;
    }

    public boolean is(TestUsers user) {
        return user.username.equals(connectedUserName);
    }

    public ResourceManager connect(RMAuthentication rmAuth) throws LoginException, KeyException,
            ActiveObjectCreationException, NodeException {
        disconnect();
        Credentials connectedUserCreds = Credentials.createCredentials(
                new CredData(CredData.parseLogin(connectedUserName), CredData.parseDomain(connectedUserName),
                    connectedUserPassword), rmAuth.getPublicKey());
        resourceManager = rmAuth.login(connectedUserCreds);

        monitorsHandler = new RMMonitorsHandler();
        /** create event receiver then turnActive to avoid deepCopy of MonitorsHandler object
         * 	(shared instance between event receiver and static helpers).
         */
        RMMonitorEventReceiver passiveEventReceiver = new RMMonitorEventReceiver(monitorsHandler);
        eventReceiver = PAActiveObject.turnActive(passiveEventReceiver);
        PAFuture.waitFor(resourceManager.getMonitoring().addRMEventListener(eventReceiver));

        return resourceManager;
    }

    public boolean isConnected() {
        return resourceManager != null;
    }

    public void disconnect() {
        try {
            if (resourceManager != null) {
                resourceManager.getMonitoring().removeRMEventListener();
            }
        } catch (Throwable rmHasBeenKilled) {
        }

        try {
            if (resourceManager != null) {
                resourceManager.disconnect().getBooleanValue();
            }
        } catch (Throwable rmHasBeenKilled) {
        }

        resourceManager = null;
        eventReceiver = null;
        monitorsHandler = null;
    }

    public ResourceManager getResourceManager() {
        return resourceManager;
    }

    public RMMonitorsHandler getMonitorsHandler() {
        return monitorsHandler;
    }

    public RMMonitorEventReceiver getEventReceiver() {
        return eventReceiver;
    }
}
