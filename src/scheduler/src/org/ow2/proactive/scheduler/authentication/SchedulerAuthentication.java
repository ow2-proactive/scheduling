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
package org.ow2.proactive.scheduler.authentication;

import java.security.KeyException;

import javax.security.auth.login.LoginException;

import org.apache.log4j.Logger;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.ow2.proactive.authentication.AuthenticationImpl;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.scheduler.common.AdminSchedulerInterface;
import org.ow2.proactive.scheduler.common.SchedulerAuthenticationInterface;
import org.ow2.proactive.scheduler.common.UserSchedulerInterface;
import org.ow2.proactive.scheduler.common.exception.SchedulerException;
import org.ow2.proactive.scheduler.common.util.SchedulerLoggers;
import org.ow2.proactive.scheduler.core.AdminScheduler;
import org.ow2.proactive.scheduler.core.SchedulerFrontend;
import org.ow2.proactive.scheduler.core.UserScheduler;
import org.ow2.proactive.scheduler.core.jmx.JMXMonitoringHelper;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.job.UserIdentificationImpl;
import org.ow2.proactive.scheduler.util.SchedulerDevLoggers;
import org.ow2.proactive.utils.Tools;


/**
 * This is the authentication class of the scheduler.
 * To get an instance of the scheduler you must ident yourself with this class.
 * Once authenticate, the <code>login</code> method returns a user/admin interface
 * in order to managed the scheduler.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 *
 */
public class SchedulerAuthentication extends AuthenticationImpl implements InitActive,
        SchedulerAuthenticationInterface {

    /**  */
    private static final long serialVersionUID = 200;

    /** Scheduler logger */
    public static final Logger logger_dev = ProActiveLogger.getLogger(SchedulerDevLoggers.CONNECTION);

    /** The scheduler front-end connected to this authentication interface */
    private SchedulerFrontend frontend;

    /**
     * ProActive empty constructor.
     */
    public SchedulerAuthentication() {
    }

    /**
     * Get a new instance of SchedulerAuthentication according to the given logins file.
     * This will also set java.security.auth.login.config property.
     *
     * @param frontend the scheduler front-end on which to connect the user after authentication success.
     */
    public SchedulerAuthentication(SchedulerFrontend frontend) {
        super(PASchedulerProperties.getAbsolutePath(PASchedulerProperties.SCHEDULER_AUTH_JAAS_PATH
                .getValueAsString()), PASchedulerProperties
                .getAbsolutePath(PASchedulerProperties.SCHEDULER_AUTH_PRIVKEY_PATH.getValueAsString()),
                PASchedulerProperties.getAbsolutePath(PASchedulerProperties.SCHEDULER_AUTH_PUBKEY_PATH
                        .getValueAsString()));
        this.frontend = frontend;
    }

    /**
     * @see org.objectweb.proactive.InitActive#initActivity(org.objectweb.proactive.Body)
     */
    public void initActivity(Body body) {
        this.frontend.connect();
    }

    /**
     * 
     * Kept for compatibility reasons, should be removed in future versions.
     * <p>
     * Prefer its secure counterpart,
     * {@link org.ow2.proactive.scheduler.common.SchedulerAuthenticationInterface#logAsUser(Credentials)}
     * 
     * @see org.ow2.proactive.scheduler.common.SchedulerAuthenticationInterface#logAsUser(java.lang.String, java.lang.String)
     */
    @Deprecated
    public UserSchedulerInterface logAsUser(String user, String password) throws LoginException {
        try {
            // encrypting the data here is useless, only done to conform to the
            // signature of the real method
            return logAsUser(Credentials.createCredentials(user, password, publicKeyPath));
        } catch (KeyException e) {
            logger_dev.error("", e);
            throw new LoginException("Could not encrypt credentials: " + e.getMessage());
        }
    }

    /**
     * 
     * Kept for compatibility reasons, should be removed in future versions.
     * <p>
     * Prefer its secure counterpart,
     * {@link org.ow2.proactive.scheduler.common.SchedulerAuthenticationInterface#logAsAdmin(Credentials)}
     * 
     * @see org.ow2.proactive.scheduler.common.SchedulerAuthenticationInterface#logAsAdmin(java.lang.String, java.lang.String)
     */
    @Deprecated
    public AdminSchedulerInterface logAsAdmin(String user, String password) throws LoginException {
        try {
            // encrypting the data here is useless, only done to conform to the
            // signature of the real method
            return logAsAdmin(Credentials.createCredentials(user, password, publicKeyPath));
        } catch (KeyException e) {
            logger_dev.error("", e);
            throw new LoginException("Could not encrypt credentials: " + e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    public UserSchedulerInterface logAsUser(Credentials cred) throws LoginException {

        String user = loginAs("user", new String[] { "user", "admin" }, cred);

        logger_dev.info("user : " + user);
        UserScheduler us = new UserScheduler();
        us.schedulerFrontend = this.frontend;
        // add this user to the scheduler front-end
        UserIdentificationImpl ident = new UserIdentificationImpl(user);
        ident.setHostName(getSenderHostName());
        try {
            this.frontend.connect(PAActiveObject.getContext().getCurrentRequest().getSourceBodyID(), ident);
        } catch (SchedulerException e) {
            logger_dev.error("", e);
            throw new LoginException(e.getMessage());
        }

        // return the created interface
        return us;
    }

    /**
     * {@inheritDoc}
     */
    public AdminSchedulerInterface logAsAdmin(Credentials cred) throws LoginException {

        String user = loginAs("admin", new String[] { "admin" }, cred);

        logger_dev.info("user : " + user);
        AdminScheduler as = new AdminScheduler();
        as.schedulerFrontend = this.frontend;
        // add this user to the scheduler front-end
        UserIdentificationImpl ident = new UserIdentificationImpl(user, true);
        ident.setHostName(getSenderHostName());
        try {
            this.frontend.connect(PAActiveObject.getContext().getCurrentRequest().getSourceBodyID(), ident);
        } catch (SchedulerException e) {
            logger_dev.error("", e);
            throw new LoginException(e.getMessage());
        }

        // return the created interface
        return as;
    }

    /**
     * get the host name of the sender
     * 
     * @return the host name of the sender
     */
    private String getSenderHostName() {
        String senderURL = PAActiveObject.getContext().getCurrentRequest().getSender().getNodeURL();
        senderURL = senderURL.replaceFirst(".*//", "").replaceFirst("/.*", "");
        return senderURL;
    }

    /**
     * @see org.ow2.proactive.authentication.Loggable#getLogger()
     */
    public Logger getLogger() {
        return ProActiveLogger.getLogger(SchedulerLoggers.CONNECTION);
    }

    /**
     * @see org.ow2.proactive.authentication.AuthenticationImpl#getLoginMethod()
     */
    @Override
    protected String getLoginMethod() {
        return PASchedulerProperties.SCHEDULER_LOGIN_METHOD.getValueAsString();
    }

    /**
     * {@inheritDoc}
     */
    public String getJMXConnectorURL() {
        return JMXMonitoringHelper.getDefaultJmxConnectorUrl() +
            PASchedulerProperties.SCHEDULER_JMX_CONNECTOR_NAME.getValueAsString();
    }

    /**
     * {@inheritDoc}
     */
    public String getHostURL() {
        return Tools.getHostURL(PAActiveObject.getActiveObjectNodeUrl(PAActiveObject.getStubOnThis()));
    }
}
