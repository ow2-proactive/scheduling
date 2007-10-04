/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
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
 */
package org.objectweb.proactive.extra.scheduler.core;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.security.auth.login.LoginException;

import org.apache.log4j.Logger;
import org.objectweb.proactive.api.ProActiveObject;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extra.scheduler.common.exception.SchedulerException;
import org.objectweb.proactive.extra.scheduler.common.scheduler.SchedulerAuthenticationInterface;
import org.objectweb.proactive.extra.scheduler.common.scheduler.UserSchedulerInterface;
import org.objectweb.proactive.extra.scheduler.job.UserIdentification;
import org.objectweb.proactive.extra.security.FileLogin;
import org.objectweb.proactive.extra.security.Login;


/**
 * This is the authentication class of the scheduler.
 * To get an instance of the scheduler you must ident yourselfwith this class.
 * Once authenticate, the <code>login</code> method returns a user/admin interface
 * in order to managed the scheduler.
 *
 * @author ProActive Team
 * @version 1.0, Jul 23, 2007
 * @since ProActive 3.2
 *
 */
public class SchedulerAuthentication implements SchedulerAuthenticationInterface {

    /** Serial version UID */
    private static final long serialVersionUID = -3143047028779653795L;

    /** Scheduler logger */
    private static Logger logger = ProActiveLogger.getLogger(Loggers.SCHEDULER);

    /** The file where to store the allowed user//password */
    private String loginFile;

    /** The file where to store group management */
    private String groupFile;

    /** The scheduler frontend connected to this authentication interface */
    private SchedulerFrontend scheduler;

    /**
     * ProActive empty constructor.
     */
    public SchedulerAuthentication() {
    }

    /**
     * Get a new instance of SchedulerAuthentication according to the given logins file.
     * This will also set java.security.auth.login.config property.
     *
     * @param loginFile the file path where to check if a username//password is correct.
     * @param groupFile the file path where to check the membership of a user.
     * @param scheduler the scheduler front-end on which to connect the user after authentication success.
     */
    public SchedulerAuthentication(String loginFile, String groupFile,
        SchedulerFrontend scheduler) {
        URL jaasConfig = Login.class.getResource("jaas.config");
        if (jaasConfig == null) {
            throw new RuntimeException(
                "The file 'jaas.config' has not been found and have to be at the following directory :\n" +
                "\tclasses/Extra/org/objectweb/proactive/extra/security/");
        }
        System.setProperty("java.security.auth.login.config",
            jaasConfig.getFile());
        this.loginFile = loginFile;
        this.groupFile = groupFile;
        this.scheduler = scheduler;
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.common.scheduler.SchedulerAuthenticationInterface#logAsUser(java.lang.String, java.lang.String)
     */
    public UserSchedulerInterface logAsUser(String user, String password)
        throws LoginException, SchedulerException {
        // Verify that this user//password can connect to this existing scheduler
        logger.info(user + " is trying to connect...");
        logger.info("Verifying user name and password...");
        Map<String, Object> params = new HashMap<String, Object>(6);
        params.put("username", user);
        params.put("pw", password);
        params.put("path", loginFile);
        params.put("group", "user");
        params.put("groupsFilePath", groupFile);
        params.put("groupsHierarchy", new String[] { "user", "admin" });
        FileLogin.login(params);
        logger.info("Logging successfull for user : " + user);
        // create user scheduler interface
        logger.info("Connecting to the scheduler...");
        UserScheduler us = new UserScheduler();
        us.schedulerFrontend = scheduler;
        //add this user to the scheduler front-end
        scheduler.connect(ProActiveObject.getContext().getCurrentRequest()
                                         .getSourceBodyID(),
            new UserIdentification(user));
        // return the created interface
        return us;
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.common.scheduler.SchedulerAuthenticationInterface#logAsAdmin(java.lang.String, java.lang.String)
     */
    public AdminSchedulerInterface logAsAdmin(String user, String password)
        throws LoginException, SchedulerException {
        // Verify that this user//password can connect (as admin) to this existing scheduler.
        logger.info("Verifying admin name and password...");
        Map<String, Object> params = new HashMap<String, Object>(6);
        params.put("username", user);
        params.put("pw", password);
        params.put("path", loginFile);
        params.put("group", "admin");
        params.put("groupsFilePath", groupFile);
        params.put("groupsHierarchy", new String[] { "admin" });
        FileLogin.login(params);
        logger.info("Logging successfull for user : " + user);
        // create admin scheduler interface
        logger.info("Connecting to the scheduler...");
        AdminScheduler as = new AdminScheduler();
        as.schedulerFrontend = scheduler;
        //add this user to the scheduler front-end
        scheduler.connect(ProActiveObject.getContext().getCurrentRequest()
                                         .getSourceBodyID(),
            new UserIdentification(user, true));
        // return the created interface
        return as;
    }

    /**
     * Terminate the SchedulerAuthentication active object.
     */
    public boolean terminate() {
        ProActiveObject.terminateActiveObject(false);
        logger.info("Scheduler authentication is now shutdown !");
        return true;
    }
}
