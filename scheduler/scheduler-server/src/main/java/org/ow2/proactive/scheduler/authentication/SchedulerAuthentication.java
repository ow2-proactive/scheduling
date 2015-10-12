/*
 * ################################################################
 *
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
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.authentication;

import java.io.IOException;

import javax.management.JMException;
import javax.security.auth.Subject;
import javax.security.auth.login.LoginException;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.extensions.annotation.ActiveObject;
import org.ow2.proactive.authentication.AuthenticationImpl;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.authentication.principals.UserNamePrincipal;
import org.ow2.proactive.jmx.naming.JMXTransportProtocol;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.SchedulerAuthenticationInterface;
import org.ow2.proactive.scheduler.common.exception.AlreadyConnectedException;
import org.ow2.proactive.scheduler.core.SchedulerFrontend;
import org.ow2.proactive.scheduler.core.jmx.SchedulerJMXHelper;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.job.UserIdentificationImpl;
import org.ow2.proactive.utils.Tools;
import org.apache.log4j.Logger;


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
@ActiveObject
public class SchedulerAuthentication extends AuthenticationImpl implements SchedulerAuthenticationInterface {

    /** Scheduler logger */
    public static final Logger logger = Logger.getLogger(SchedulerAuthentication.class);

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
     * {@inheritDoc}
     */
    public Scheduler login(Credentials cred) throws LoginException, AlreadyConnectedException {
        Subject subject = authenticate(cred);

        UserNamePrincipal unPrincipal = subject.getPrincipals(UserNamePrincipal.class).iterator().next();
        String user = unPrincipal.getName();

        logger.info("user : " + user);
        // add this user to the scheduler front-end
        UserIdentificationImpl ident = new UserIdentificationImpl(user, subject);
        ident.setHostName(getSenderHostName());

        this.frontend.connect(PAActiveObject.getContext().getCurrentRequest().getSourceBodyID(), ident, cred);

        try {
            // return the stub on Scheduler interface to keep avoid using server class on client side
            return PAActiveObject.lookupActive(Scheduler.class, PAActiveObject.getUrl(frontend));
        } catch (ActiveObjectCreationException e) {
            rethrowSchedulerStubException(e);
        } catch (IOException e) {
            rethrowSchedulerStubException(e);
        }
        return null;
    }

    private void rethrowSchedulerStubException(Exception e) throws LoginException {
        logger.error("Could not lookup stub for Scheduler interface", e);
        throw new LoginException("Could not lookup stub for Scheduler interface : " + e.getMessage());
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
        return Logger.getLogger(SchedulerAuthentication.class);
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
        try {
            return SchedulerJMXHelper.getInstance().getAddress(JMXTransportProtocol.RMI).toString();
        } catch (JMException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the address of the JMX connector server depending on the specified protocol.
     * 
     * @param protocol the JMX transport protocol
     * @return the address of the anonymous connector server
     * @throws JMException in case of boot sequence failure
     */
    public String getJMXConnectorURL(final JMXTransportProtocol protocol) throws JMException {
        return SchedulerJMXHelper.getInstance().getAddress(protocol).toString();
    }

    /**
     * Return the URL of this Scheduler.
     * This URL must be used to contact this Scheduler.
     *
     * @return the URL of this Scheduler.
     */
    public String getHostURL() {
        return Tools.getHostURL(PAActiveObject.getActiveObjectNodeUrl(PAActiveObject.getStubOnThis()));
    }
}
