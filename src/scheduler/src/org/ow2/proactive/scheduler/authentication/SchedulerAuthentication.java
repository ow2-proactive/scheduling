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
package org.ow2.proactive.scheduler.authentication;

import javax.security.auth.login.LoginException;

import org.apache.log4j.Logger;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.ow2.proactive.authentication.AuthenticationImpl;
import org.ow2.proactive.scheduler.common.AdminSchedulerInterface;
import org.ow2.proactive.scheduler.common.SchedulerAuthenticationInterface;
import org.ow2.proactive.scheduler.common.UserSchedulerInterface;
import org.ow2.proactive.scheduler.common.exception.SchedulerException;
import org.ow2.proactive.scheduler.common.util.SchedulerLoggers;
import org.ow2.proactive.scheduler.core.AdminScheduler;
import org.ow2.proactive.scheduler.core.SchedulerFrontend;
import org.ow2.proactive.scheduler.core.UserScheduler;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.job.UserIdentificationImpl;
import org.ow2.proactive.scheduler.util.SchedulerDevLoggers;


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
        this.frontend = frontend;
    }

    /**
     * @see org.objectweb.proactive.InitActive#initActivity(org.objectweb.proactive.Body)
     */
    public void initActivity(Body body) {
        this.frontend.connect();
    }

    /**
     * @see org.ow2.proactive.scheduler.common.SchedulerAuthenticationInterface#logAsUser(java.lang.String, java.lang.String)
     */
    public UserSchedulerInterface logAsUser(String user, String password) throws LoginException {
        logger_dev.info("user : " + user);

        loginAs("user", new String[] { "user", "admin" }, user, password);

        UserScheduler us = new UserScheduler();
        us.schedulerFrontend = this.frontend;
        //add this user to the scheduler front-end
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
     * @see org.ow2.proactive.scheduler.common.SchedulerAuthenticationInterface#logAsAdmin(java.lang.String, java.lang.String)
     */
    public AdminSchedulerInterface logAsAdmin(String user, String password) throws LoginException {
        logger_dev.info("admin : " + user);

        loginAs("admin", new String[] { "admin" }, user, password);

        AdminScheduler as = new AdminScheduler();
        as.schedulerFrontend = this.frontend;
        //add this user to the scheduler front-end
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

    //get the host name of the sender.
    private String getSenderHostName() {
        String senderURL = PAActiveObject.getContext().getCurrentRequest().getSender().getNodeURL();
        senderURL = senderURL.replaceFirst(".*//", "").replaceFirst("/.*", "");
        return senderURL;
    }

    public Logger getLogger() {
        return ProActiveLogger.getLogger(SchedulerLoggers.CONNECTION);
    }

    protected String getLoginMethod() {
        return PASchedulerProperties.SCHEDULER_LOGIN_METHOD.getValueAsString();
    }
}
