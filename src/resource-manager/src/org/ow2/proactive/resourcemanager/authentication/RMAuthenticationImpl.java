/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
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
package org.ow2.proactive.resourcemanager.authentication;

import java.net.URI;
import java.security.KeyException;

import javax.management.JMException;
import javax.security.auth.login.LoginException;

import org.apache.log4j.Logger;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.ow2.proactive.authentication.AuthenticationImpl;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.resourcemanager.common.RMConstants;
import org.ow2.proactive.resourcemanager.core.RMCoreInterface;
import org.ow2.proactive.resourcemanager.core.jmx.JMXMonitoringHelper;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.frontend.RMAdmin;
import org.ow2.proactive.resourcemanager.frontend.RMAdminImpl;
import org.ow2.proactive.resourcemanager.frontend.RMMonitoring;
import org.ow2.proactive.resourcemanager.frontend.RMUser;
import org.ow2.proactive.resourcemanager.frontend.RMUserImpl;
import org.ow2.proactive.resourcemanager.utils.RMLoggers;


/**
 * RMAuthenticationImpl represents authentication service of the resource manager.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 1.0
 */
public class RMAuthenticationImpl extends AuthenticationImpl implements RMAuthentication, InitActive {

    private static final String ERROR_ALREADY_CONNECTED = "This active object is already connected to the resource manager. Disconnect first.";
    private RMCoreInterface rmcore;

    /**
     * ProActive default constructor
     */
    public RMAuthenticationImpl() {
    }

    public RMAuthenticationImpl(RMCoreInterface rmcore) {
        super(PAResourceManagerProperties.getAbsolutePath(PAResourceManagerProperties.RM_AUTH_JAAS_PATH
                .getValueAsString()), PAResourceManagerProperties
                .getAbsolutePath(PAResourceManagerProperties.RM_AUTH_PRIVKEY_PATH.getValueAsString()),
                PAResourceManagerProperties.getAbsolutePath(PAResourceManagerProperties.RM_AUTH_PUBKEY_PATH
                        .getValueAsString()));
        this.rmcore = rmcore;
    }

    /**
     * Performs user authentication
     */
    @Deprecated
    public RMUser logAsUser(String user, String password) throws LoginException {
        try {
            // encrypting the data here is useless, only done to conform to the
            // signature of the real method
            return logAsUser(Credentials.createCredentials(user, password, publicKeyPath));
        } catch (KeyException e) {
            throw new LoginException("Could not encrypt credentials: " + e.getMessage());
        }
    }

    /**
     * Performs admin authentication
     */
    @Deprecated
    public RMAdmin logAsAdmin(String user, String password) throws LoginException {
        try {
            // encrypting the data here is useless, only done to conform to the
            // signature of the real method
            return logAsAdmin(Credentials.createCredentials(user, password, publicKeyPath));
        } catch (KeyException e) {
            throw new LoginException("Could not encrypt credentials: " + e.getMessage());
        }
    }

    /**
     * Performs admin authentication
     */
    public RMAdmin logAsAdmin(Credentials cred) throws LoginException {
        loginAs("admin", new String[] { "admin" }, cred);

        // login successful
        UniqueID id = PAActiveObject.getContext().getCurrentRequest().getSourceBodyID();
        RMAdminImpl admin = (RMAdminImpl) rmcore.getAdmin();
        if (!admin.connect(id)) {
            throw new LoginException(ERROR_ALREADY_CONNECTED);
        }
        return admin;
    }

    /**
     * Performs user authentication
     */
    public RMUser logAsUser(Credentials cred) throws LoginException {
        loginAs("user", new String[] { "user", "admin" }, cred);

        // login successful
        UniqueID id = PAActiveObject.getContext().getCurrentRequest().getSourceBodyID();
        RMUserImpl userImpl = (RMUserImpl) rmcore.getUser();
        if (!userImpl.connect(id)) {
            throw new LoginException(ERROR_ALREADY_CONNECTED);
        }
        return userImpl;
    }

    /**
     * Returns RM monitor
     */
    public RMMonitoring logAsMonitor() {
        return rmcore.getMonitoring();
    }

    /**
     * Initializes the active object and register it in ProActive runtime
     */
    public void initActivity(Body body) {
        try {
            PAActiveObject.registerByName(PAActiveObject.getStubOnThis(),
                    RMConstants.NAME_ACTIVE_OBJECT_RMAUTHENTICATION);
        } catch (ProActiveException e) {
            e.printStackTrace();
            PAActiveObject.terminateActiveObject(true);
        }
    }

    public Logger getLogger() {
        return ProActiveLogger.getLogger(RMLoggers.CONNECTION);
    }

    @Override
    protected String getLoginMethod() {
        return PAResourceManagerProperties.RM_LOGIN_METHOD.getValueAsString();
    }

    /**
     * {@inheritDoc}
     */
    public String getJMXConnectorURL() throws JMException {
        return JMXMonitoringHelper.getInstance().getAddress().toString();
    }

    /**
     * {@inheritDoc}
     */
    public String getHostURL() {
        return getHostURL(PAActiveObject.getActiveObjectNodeUrl(PAActiveObject.getStubOnThis()));
    }

    /**
     * Normalize the given URL into an URL that only contains protocol://host:port/
     *
     * @param url the url to transform
     * @return an URL that only contains protocol://host:port/
     */
    private static String getHostURL(String url) {
        URI uri = URI.create(url);
        int port = uri.getPort();
        if (port == -1) {
            return uri.getScheme() + "://" + uri.getHost() + "/";
        } else {
            return uri.getScheme() + "://" + uri.getHost() + ":" + uri.getPort() + "/";
        }
    }
}
