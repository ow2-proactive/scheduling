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
package org.ow2.proactive.resourcemanager.authentication;

import java.net.URI;
import java.security.KeyException;
import java.util.Set;

import javax.management.JMException;
import javax.security.auth.login.LoginException;

import org.apache.log4j.Logger;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.ow2.proactive.authentication.AuthenticationImpl;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.authentication.principals.GroupNamePrincipal;
import org.ow2.proactive.jmx.naming.JMXTransportProtocol;
import org.ow2.proactive.resourcemanager.common.RMConstants;
import org.ow2.proactive.resourcemanager.core.RMCore;
import org.ow2.proactive.resourcemanager.core.jmx.RMJMXHelper;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.frontend.RMAdmin;
import org.ow2.proactive.resourcemanager.frontend.RMMonitoring;
import org.ow2.proactive.resourcemanager.frontend.RMUser;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.resourcemanager.utils.RMLoggers;


/**
 * RMAuthenticationImpl represents authentication service of the resource manager.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 1.0
 */
public class RMAuthenticationImpl extends AuthenticationImpl implements RMAuthentication, InitActive {

    /**  */
    private static final long serialVersionUID = 21L;
    private static final String ERROR_ALREADY_CONNECTED = "This active object is already connected to the resource manager. Disconnect first.";
    private final static Logger logger = ProActiveLogger.getLogger(RMLoggers.CONNECTION);
    private RMCore rmcore;

    /**
     * ProActive default constructor
     */
    public RMAuthenticationImpl() {
    }

    public RMAuthenticationImpl(RMCore rmcore) {
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
    @Deprecated
    public RMAdmin logAsAdmin(Credentials cred) throws LoginException {
        Client client = new Client(authenticate(cred), true);

        Set<GroupNamePrincipal> groupPrincipals = client.getSubject().getPrincipals(GroupNamePrincipal.class);
        boolean userOrAdmin = groupPrincipals.contains(new GroupNamePrincipal("admin"));
        if (!userOrAdmin) {
            throw new LoginException("User does not belong to \"admins\" group");
        }

        if (RMCore.clients.containsKey(client.getId())) {
            throw new LoginException(ERROR_ALREADY_CONNECTED);
        }

        RMCore.clients.put(client.getId(), client);
        logger.info(client + " connected");
        return rmcore;
    }

    /**
     * Performs user authentication
     */
    @Deprecated
    public RMUser logAsUser(Credentials cred) throws LoginException {
        Client client = new Client(authenticate(cred), true);

        Set<GroupNamePrincipal> groupPrincipals = client.getSubject().getPrincipals(GroupNamePrincipal.class);
        boolean userOrAdmin = groupPrincipals.contains(new GroupNamePrincipal("user")) ||
            groupPrincipals.contains(new GroupNamePrincipal("admin"));
        if (!userOrAdmin) {
            throw new LoginException("User does not belong to either \"users\" or \"admins\" group");
        }

        if (RMCore.clients.containsKey(client.getId())) {
            throw new LoginException(ERROR_ALREADY_CONNECTED);
        }

        RMCore.clients.put(client.getId(), client);
        logger.info(client + " connected");
        return rmcore;
    }

    /**
     * Performs client authentication
     */
    public ResourceManager login(Credentials cred) throws LoginException {
        Client client = new Client(authenticate(cred), true);

        if (RMCore.clients.containsKey(client.getId())) {
            throw new LoginException(ERROR_ALREADY_CONNECTED);
        }

        RMCore.clients.put(client.getId(), client);
        logger.info(client + " connected");
        return rmcore;
    }

    /**
     * Returns RM monitor
     */
    @Deprecated
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
        return RMJMXHelper.getInstance().getAddress(JMXTransportProtocol.RMI).toString();
    }

    /**
     * Returns the address of the JMX connector server depending on the specified protocol.
     * 
     * @param protocol the JMX transport protocol
     * @return the address of the anonymous connector server
     * @throws JMException in case of boot sequence failure
     */
    public String getJMXConnectorURL(final JMXTransportProtocol protocol) throws JMException {
        return RMJMXHelper.getInstance().getAddress(protocol).toString();
    }

    /**
     * Return the URL of this Resource Manager.
     * This URL must be used to contact this Resource Manager.
     *
     * @return the URL of this Resource Manager.
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
