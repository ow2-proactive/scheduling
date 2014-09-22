/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
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
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.resourcemanager.authentication;

import java.io.IOException;
import java.net.URI;

import javax.management.JMException;
import javax.security.auth.login.LoginException;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.extensions.annotation.ActiveObject;
import org.ow2.proactive.authentication.AuthenticationImpl;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.jmx.naming.JMXTransportProtocol;
import org.ow2.proactive.resourcemanager.common.RMConstants;
import org.ow2.proactive.resourcemanager.core.RMCore;
import org.ow2.proactive.resourcemanager.core.history.UserHistory;
import org.ow2.proactive.resourcemanager.core.jmx.RMJMXHelper;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.db.RMDBManager;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.apache.log4j.Logger;


/**
 * RMAuthenticationImpl represents authentication service of the resource manager.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 1.0
 */
@ActiveObject
public class RMAuthenticationImpl extends AuthenticationImpl implements RMAuthentication, InitActive {

    private static final long serialVersionUID = 60L;

    private static final String ERROR_ALREADY_CONNECTED = "This active object is already connected to the resource manager. Disconnect first.";
    private final static Logger logger = Logger.getLogger(RMAuthenticationImpl.class);
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
    public ResourceManager login(Credentials cred) throws LoginException {
        Client client = new Client(authenticate(cred), true);
        client.setCredentials(cred);

        if (RMCore.clients.containsKey(client.getId())) {
            throw new LoginException(ERROR_ALREADY_CONNECTED);
        }

        RMCore.clients.put(client.getId(), client);
        UserHistory history = new UserHistory(client);
        RMDBManager.getInstance().saveUserHistory(history);

        client.setHistory(history);

        logger.info(client + " connected from " + client.getId().shortString());
        try {
            // return the stub on ResourceManager interface to keep avoid using server class on client side
            return PAActiveObject.lookupActive(ResourceManager.class, PAActiveObject.getUrl(rmcore));
        } catch (ActiveObjectCreationException e) {
            rethrowStubException(e);
        } catch (IOException e) {
            rethrowStubException(e);
        }
        return null;
    }

    private void rethrowStubException(Exception e) throws LoginException {
        logger.error("Could not lookup stub for ResourceManager interface", e);
        throw new LoginException("Could not lookup stub for ResourceManager interface : " + e.getMessage());
    }

    /**
     * Initializes the active object and register it in ProActive runtime
     */
    public void initActivity(Body body) {
        try {
            PAActiveObject.registerByName(PAActiveObject.getStubOnThis(),
                    RMConstants.NAME_ACTIVE_OBJECT_RMAUTHENTICATION);
        } catch (ProActiveException e) {
            logger.error(e.getMessage(), e);
            PAActiveObject.terminateActiveObject(true);
        }
    }

    public Logger getLogger() {
        return Logger.getLogger(RMAuthenticationImpl.class);
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
