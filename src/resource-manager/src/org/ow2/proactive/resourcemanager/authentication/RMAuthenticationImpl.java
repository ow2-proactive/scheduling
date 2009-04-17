/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.resourcemanager.authentication;

import javax.security.auth.login.LoginException;

import org.apache.log4j.Logger;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.ow2.proactive.authentication.AuthenticationImpl;
import org.ow2.proactive.resourcemanager.common.RMConstants;
import org.ow2.proactive.resourcemanager.core.RMCoreInterface;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.frontend.RMAdmin;
import org.ow2.proactive.resourcemanager.frontend.RMAdminImpl;
import org.ow2.proactive.resourcemanager.frontend.RMMonitoring;
import org.ow2.proactive.resourcemanager.frontend.RMUser;
import org.ow2.proactive.resourcemanager.frontend.RMUserImpl;
import org.ow2.proactive.resourcemanager.utils.RMLoggers;


/**
 *
 * Class represents authentication service of the resource manager.
 *
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
        this.rmcore = rmcore;
    }

    /**
     * Performs user authentication
     */
    public RMUser logAsUser(String user, String password) throws LoginException {

        loginAs("user", new String[] { "user", "admin" }, user, password);

        // login successful
        UniqueID id = PAActiveObject.getContext().getCurrentRequest().getSourceBodyID();
        RMUserImpl userImpl = (RMUserImpl) rmcore.getUser();
        if (!userImpl.connect(id)) {
            throw new LoginException(ERROR_ALREADY_CONNECTED);
        }
        return userImpl;
    }

    /**
     * Performs admin authentication
     */
    public RMAdmin logAsAdmin(String user, String password) throws LoginException {

        loginAs("admin", new String[] { "admin" }, user, password);

        // login successful
        UniqueID id = PAActiveObject.getContext().getCurrentRequest().getSourceBodyID();
        RMAdminImpl admin = (RMAdminImpl) rmcore.getAdmin();
        if (!admin.connect(id)) {
            throw new LoginException(ERROR_ALREADY_CONNECTED);
        }
        return admin;
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

    protected String getLoginMethod() {
        return PAResourceManagerProperties.RM_LOGIN_METHOD.getValueAsString();
    }

}
